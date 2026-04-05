---
phase: 15
title: AI & LLM Integration
role: AI Engineer
dependencies: [Phase 2, Phase 4, Phase 5]
estimated_time: 3-5 hours
---

# Phase 15: AI & LLM Integration — Agent Workflow

## Prerequisites
- [ ] Phase 5 completed (frontend ready for chat UI)
- [ ] Required env vars: `OPENAI_API_KEY` and/or `ANTHROPIC_API_KEY`
- [ ] Required packages: `ai`, `@ai-sdk/openai`, `@ai-sdk/anthropic`

## Step-by-Step Execution

### Step 1: Install Vercel AI SDK
**Action:** `run_command`
```bash
pnpm add ai @ai-sdk/openai @ai-sdk/anthropic
```

### Step 2: Write Streaming Chat API Route
**Action:** `write_to_file`
**File:** `app/api/chat/route.ts`
**Description:** POST handler using `streamText` with system prompt, message history, and token usage tracking in `onFinish` callback. Use Claude Sonnet 4 as primary model.

### Step 3: Write Structured Output Endpoint
**Action:** `write_to_file`
**File:** `app/api/ai/generate/route.ts`
**Description:** Use `generateObject` with Zod schema for type-safe structured AI responses.

### Step 4: Write Multi-Model Fallback
**Action:** `write_to_file`
**File:** `lib/ai/fallback.ts`
**Description:** Try primary model (Claude Sonnet 4), fall back to secondary (GPT-4o), then tertiary (GPT-4o-mini) on failure.

### Step 5: Write Chat UI Component
**Action:** `write_to_file`
**File:** `components/chat.tsx`
**Description:** Client component using `useChat` hook from AI SDK. Include message list, input form, loading state, and error handling.

### Step 6: Add AI Rate Limiting
**Action:** `write_to_file`
**File:** `lib/ai/rate-limit.ts`
**Description:** Token bucket rate limiter for AI endpoints. Separate limits per user.

### Step 7: Add AI Safety Guardrails
**Action:** `write_to_file`
**File:** `lib/ai/safety.ts`
**Description:** Prompt injection detection (regex patterns for common attacks), content moderation on outputs, token budget enforcement per request and per user.

### Step 8: Add RAG Pipeline (Optional)
**Action:** `write_to_file`
**Files:** `lib/ai/embeddings.ts`, `lib/ai/rag.ts`
**Description:** Generate embeddings, store in pgvector, retrieve relevant context for augmented generation.

## Verification
- [ ] Chat streams responses in real-time
- [ ] Structured outputs match Zod schema
- [ ] Model fallback works when primary is down
- [ ] Rate limiting blocks excessive requests
- [ ] Prompt injection attempts are caught
- [ ] Token usage is tracked

## Troubleshooting
- **Issue:** Streaming not working
  **Fix:** Ensure API route returns `result.toDataStreamResponse()`, not `Response.json()`.
- **Issue:** Model fallback not triggering
  **Fix:** Check that catch block creates a new request to the fallback model, not retrying the same one.
