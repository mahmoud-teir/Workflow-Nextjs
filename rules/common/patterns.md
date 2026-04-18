---
name: patterns
description: Common architectural patterns for Next.js applications.
origin: ECC
---

# Common Patterns

## Repository Pattern
Centralize data access in a service/repository layer:
```typescript
// lib/repositories/user-repository.ts
export const userRepository = {
  findById: (id: string) => db.users.findUnique({ where: { id } }),
  findByEmail: (email: string) => db.users.findUnique({ where: { email } }),
  create: (data: CreateUserInput) => db.users.create({ data }),
  update: (id: string, data: UpdateUserInput) => db.users.update({ where: { id }, data }),
}
```

## API Response Envelope
Consistent API response format:
```typescript
type ApiResponse<T> = 
  | { success: true; data: T }
  | { success: false; error: string; errors?: Record<string, string[]> }
```

## Server Action Pattern
Every Server Action follows: Authenticate → Validate → Authorize → Execute:
```typescript
'use server'
export async function action(formData: FormData) {
  const session = await getSession()        // Authenticate
  if (!session) return { success: false, error: 'Unauthorized' }
  
  const parsed = schema.safeParse(...)       // Validate
  if (!parsed.success) return { success: false, errors: parsed.error.flatten().fieldErrors }
  
  const resource = await findResource(...)   // Authorize
  if (resource.userId !== session.user.id) return { success: false, error: 'Forbidden' }
  
  await performAction(parsed.data)           // Execute
  revalidatePath('/')
  return { success: true }
}
```

## Error Boundary Pattern
Always provide error boundaries for graceful degradation:
```
app/
├── error.tsx          # Root error boundary
├── not-found.tsx      # 404 page
└── dashboard/
    ├── error.tsx      # Dashboard-specific error boundary
    └── loading.tsx    # Dashboard loading state
```

## Skeleton Project Strategy
Before building from scratch, search for:
1. Official Next.js examples (`npx create-next-app --example`)
2. Vercel templates
3. Open-source implementations of similar features
4. Package-provided starter code
