---
name: typescript-coding-style
description: TypeScript coding conventions and patterns.
origin: ECC
stack: TypeScript, React, Next.js
---

# TypeScript Coding Style

## Type Safety
- Use `type` for object shapes and unions
- Use `interface` for extendable contracts and class implementations
- NEVER use `any` — use `unknown` and narrow, or define precise types
- Use Zod for runtime validation at boundaries (API, forms, env)

## Function Style
```typescript
// ✅ Prefer named exports
export function calculateTotal(items: CartItem[]): number {
  return items.reduce((sum, item) => sum + item.price * item.quantity, 0)
}

// ✅ Arrow functions for inline callbacks
const activeUsers = users.filter((user) => user.isActive)
```

## Immutability
```typescript
// ✅ Spread operator over mutation
const updated = { ...user, name: 'New Name' }
const filtered = items.filter((item) => item.active)
const mapped = items.map((item) => ({ ...item, processed: true }))

// ❌ Never mutate
user.name = 'New Name'
items.push(newItem)
```

## React Component Props
```typescript
type ProductCardProps = {
  product: Product
  onSelect?: (id: string) => void
  className?: string
}

export function ProductCard({ product, onSelect, className }: ProductCardProps) {
  return (...)
}
```

## File Size Limits
- Target: 200-400 lines per file
- Maximum: 800 lines
- If a file exceeds 400 lines, consider extracting modules

## Naming Conventions
- Components: `PascalCase` (`ProductCard.tsx`)
- Functions/variables: `camelCase` (`calculateTotal`)
- Types/interfaces: `PascalCase` (`CartItem`)
- Constants: `SCREAMING_SNAKE_CASE` (`MAX_RETRY_COUNT`)
- Files: `kebab-case` for non-components (`user-repository.ts`)
