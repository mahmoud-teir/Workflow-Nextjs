---
name: typescript-patterns
description: TypeScript-specific patterns and generics.
origin: ECC
stack: TypeScript, React, Zod
---

# TypeScript Patterns

## API Response Generic
```typescript
type ApiResponse<T> =
  | { success: true; data: T }
  | { success: false; error: string; errors?: Record<string, string[]> }

// Usage
async function getUser(id: string): Promise<ApiResponse<User>> {
  try {
    const user = await db.users.findUnique({ where: { id } })
    if (!user) return { success: false, error: 'User not found' }
    return { success: true, data: user }
  } catch {
    return { success: false, error: 'An error occurred' }
  }
}
```

## Custom Hook Pattern
```typescript
function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value)

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay)
    return () => clearTimeout(timer)
  }, [value, delay])

  return debouncedValue
}
```

## Repository Interface
```typescript
interface Repository<T, CreateInput, UpdateInput> {
  findById(id: string): Promise<T | null>
  findMany(filter?: Partial<T>): Promise<T[]>
  create(data: CreateInput): Promise<T>
  update(id: string, data: UpdateInput): Promise<T>
  delete(id: string): Promise<void>
}
```

## Zod Schema Inference
```typescript
import { z } from 'zod'

const UserSchema = z.object({
  email: z.string().email(),
  name: z.string().min(1).max(100),
  role: z.enum(['admin', 'user', 'viewer']),
})

type User = z.infer<typeof UserSchema>
// { email: string; name: string; role: 'admin' | 'user' | 'viewer' }
```

## Discriminated Union Pattern
```typescript
type Result<T, E = Error> =
  | { ok: true; value: T }
  | { ok: false; error: E }

function divide(a: number, b: number): Result<number, string> {
  if (b === 0) return { ok: false, error: 'Division by zero' }
  return { ok: true, value: a / b }
}
```
