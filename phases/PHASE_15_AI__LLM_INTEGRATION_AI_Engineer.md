<a name="phase-15"></a>
# 📌 PHASE 15: AI & LLM INTEGRATION (AI Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 15.1: Vercel AI SDK Integration

```text
You are an AI Engineer. Integrate the Vercel AI SDK into a Next.js application.

Tool: **Vercel AI SDK** (Standard for Next.js AI)
LLM: **OpenAI GPT-4o** / **Anthropic Claude Sonnet 4** / **Claude Opus 4** (via API)

Required:
1. API Route for chat completion (streaming)
2. `useChat` hook for UI
3. Tool calling (function calling) setup
4. Structured outputs (Zod schema for responses)
5. Rate limiting for AI endpoints
6. Token usage tracking and cost monitoring
7. Multi-model fallback pattern
```

```typescript
// app/api/chat/route.ts
import { openai } from '@ai-sdk/openai'
import { anthropic } from '@ai-sdk/anthropic'
import { streamText } from 'ai'
import { z } from 'zod'
import { checkRateLimit } from '@/lib/rate-limit'
import { requireAuth } from '@/lib/action-utils'

export const maxDuration = 30

export async function POST(req: Request) {
  // Auth + rate limit
  const session = await requireAuth()
  if ('error' in session) {
    return new Response('Unauthorized', { status: 401 })
  }
  await checkRateLimit(`ai:${session.user.id}`)

  const { messages } = await req.json()

  const result = streamText({
    model: openai('gpt-4o'), // or anthropic('claude-sonnet-4-20250514')
    messages,
    tools: {
      getWeather: {
        description: 'Get the weather for a location',
        parameters: z.object({
          city: z.string().describe('The city to get weather for'),
        }),
        execute: async ({ city }) => {
          const weather = await fetchWeather(city)
          return { temperature: weather.temp, unit: 'C', city }
        },
      },
    },
    // Track token usage
    onFinish: async ({ usage }) => {
      await trackAIUsage({
        userId: session.user.id,
        model: 'gpt-4o',
        promptTokens: usage.promptTokens,
        completionTokens: usage.completionTokens,
        totalTokens: usage.totalTokens,
      })
    },
  })

  return result.toDataStreamResponse()
}
```

```tsx
// components/chat.tsx
'use client'

import { useChat } from 'ai/react'

export function Chat() {
  const { messages, input, handleInputChange, handleSubmit, isLoading, error } = useChat()

  return (
    <div className="flex flex-col w-full max-w-2xl mx-auto">
      <div className="flex-1 overflow-y-auto space-y-4 p-4">
        {messages.map(m => (
          <div key={m.id} className={cn(
            'p-3 rounded-lg',
            m.role === 'user' ? 'bg-primary text-primary-foreground ml-auto max-w-[80%]' : 'bg-muted max-w-[80%]'
          )}>
            <div className="prose prose-sm dark:prose-invert">
              {m.content}
            </div>

            {/* Render tool invocations */}
            {m.toolInvocations?.map(tool => (
              <div key={tool.toolCallId} className="mt-2 p-2 bg-background rounded text-xs">
                <span className="font-medium">Tool: {tool.toolName}</span>
                {tool.state === 'result' && (
                  <pre className="mt-1">{JSON.stringify(tool.result, null, 2)}</pre>
                )}
              </div>
            ))}
          </div>
        ))}
      </div>

      {error && (
        <p className="text-sm text-destructive px-4">{error.message}</p>
      )}

      <form onSubmit={handleSubmit} className="border-t p-4">
        <div className="flex gap-2">
          <input
            className="flex-1 rounded-md border p-2"
            value={input}
            placeholder="Ask something..."
            onChange={handleInputChange}
            disabled={isLoading}
          />
          <button
            type="submit"
            disabled={isLoading}
            className="px-4 py-2 bg-primary text-primary-foreground rounded-md disabled:opacity-50"
          >
            {isLoading ? '...' : 'Send'}
          </button>
        </div>
      </form>
    </div>
  )
}
```

#### Structured Outputs (Zod Schema):

```typescript
// app/actions/ai-extract.ts
'use server'

import { generateObject } from 'ai'
import { openai } from '@ai-sdk/openai'
import { z } from 'zod'

const recipeSchema = z.object({
  name: z.string(),
  ingredients: z.array(z.object({
    name: z.string(),
    amount: z.string(),
  })),
  steps: z.array(z.string()),
  prepTime: z.number().describe('Prep time in minutes'),
})

export async function extractRecipe(text: string) {
  const { object } = await generateObject({
    model: openai('gpt-4o'),
    schema: recipeSchema,
    prompt: `Extract the recipe from this text:\n\n${text}`,
  })

  return object // Fully typed: { name, ingredients, steps, prepTime }
}
```

#### Multi-Model Fallback Pattern:

```typescript
// lib/ai/model-fallback.ts
import { openai } from '@ai-sdk/openai'
import { anthropic } from '@ai-sdk/anthropic'
import { generateText } from 'ai'

const models = [
  { provider: openai('gpt-4o'), name: 'gpt-4o' },
  { provider: anthropic('claude-sonnet-4-20250514'), name: 'claude-sonnet-4' },
  { provider: openai('gpt-4o-mini'), name: 'gpt-4o-mini' },
]

export async function generateWithFallback(prompt: string) {
  for (const model of models) {
    try {
      const result = await generateText({
        model: model.provider,
        prompt,
      })
      return { text: result.text, model: model.name }
    } catch (error) {
      console.warn(`Model ${model.name} failed, trying next...`)
      continue
    }
  }
  throw new Error('All AI models failed')
}
```

---

### Prompt 15.2: RAG Pipeline (Retrieval-Augmented Generation)

```text
Implement a RAG pipeline for querying custom data.

Database: **PostgreSQL (pgvector)** via Drizzle ORM
Embeddings: **OpenAI text-embedding-3-small**
```

```typescript
// app/actions/rag.ts
'use server'

import { embed, generateText } from 'ai'
import { openai } from '@ai-sdk/openai'
import { db } from '@/lib/db'
import { cosineDistance, desc, gt, sql } from 'drizzle-orm'
import { documents } from '@/drizzle/schema'

export async function generateAnswer(question: string) {
  // 1. Embed user question
  const { embedding } = await embed({
    model: openai.embedding('text-embedding-3-small'),
    value: question,
  })

  // 2. Find relevant context via vector similarity
  const similarity = sql<number>`1 - (${cosineDistance(documents.embedding, embedding)})`
  const context = await db
    .select({ content: documents.content, similarity })
    .from(documents)
    .where(gt(similarity, 0.5))
    .orderBy(desc(similarity))
    .limit(3)

  // 3. Generate answer with context
  const { text } = await generateText({
    model: openai('gpt-4o'),
    messages: [
      {
        role: 'system',
        content: `Answer based on the following context. If the context doesn't contain relevant information, say so.\n\nContext:\n${context.map(c => c.content).join('\n\n')}`,
      },
      { role: 'user', content: question },
    ],
  })

  return { answer: text, sources: context }
}
```

---

### Prompt 15.3: AI Safety & Guardrails

```text
Implement safety measures for AI features.

Required:
1. Input sanitization (prevent prompt injection)
2. Output content filtering
3. Rate limiting per user (token budget)
4. Cost monitoring and alerts
5. Logging all AI interactions for audit
```

```typescript
// lib/ai/guardrails.ts
import { generateText } from 'ai'
import { openai } from '@ai-sdk/openai'

// Simple prompt injection detection
const INJECTION_PATTERNS = [
  /ignore\s+(all\s+)?previous\s+instructions/i,
  /you\s+are\s+now\s+/i,
  /system\s*:\s*/i,
  /\<\|.*?\|\>/i, // Token manipulation attempts
]

export function detectPromptInjection(input: string): boolean {
  return INJECTION_PATTERNS.some(pattern => pattern.test(input))
}

// Content moderation
export async function moderateContent(content: string): Promise<{
  safe: boolean
  reason?: string
}> {
  const response = await openai.responses.create({
    model: 'omni-moderation-latest',
    input: content,
  })

  const flagged = response.results[0]?.flagged ?? false
  return {
    safe: !flagged,
    reason: flagged ? 'Content flagged by moderation' : undefined,
  }
}

// Token budget per user (daily limit)
export async function checkTokenBudget(userId: string): Promise<boolean> {
  const today = new Date().toISOString().split('T')[0]
  const usage = await db.aiUsage.aggregate({
    where: { userId, date: today },
    _sum: { totalTokens: true },
  })

  const dailyLimit = 100_000 // 100K tokens per day
  return (usage._sum.totalTokens || 0) < dailyLimit
}
```

```text
Implement AI features with Vercel AI SDK, safety guardrails, structured outputs, and cost monitoring.
```
