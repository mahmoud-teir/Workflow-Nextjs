---
name: typescript-security
description: TypeScript-specific security patterns including environment variable management.
origin: ECC
stack: TypeScript, Next.js, Zod
---

# TypeScript Security

## Environment Variable Validation

Create `env.ts` to validate and type environment variables:

```typescript
import { z } from 'zod'

const envSchema = z.object({
  DATABASE_URL: z.string().url(),
  NEXTAUTH_SECRET: z.string().min(32),
  NEXTAUTH_URL: z.string().url(),
  STRIPE_SECRET_KEY: z.string().startsWith('sk_'),
  STRIPE_WEBHOOK_SECRET: z.string().startsWith('whsec_'),
})

export const env = envSchema.parse(process.env)
```

## Type-Safe API Clients
```typescript
// Validate external API responses
const ExternalApiResponseSchema = z.object({
  id: z.string(),
  status: z.enum(['active', 'inactive']),
  data: z.unknown(),
})

async function fetchExternal(url: string) {
  const response = await fetch(url)
  const json = await response.json()
  return ExternalApiResponseSchema.parse(json) // Throws if invalid
}
```

## NEVER Trust User Input Types
```typescript
// ❌ WRONG: Trusting request body type assertion
const body = await request.json() as CreateUserInput

// ✅ CORRECT: Validate at runtime with Zod
const body = CreateUserSchema.parse(await request.json())
```
