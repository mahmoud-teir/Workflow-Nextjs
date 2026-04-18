---
name: tdd-workflow
description: Use this skill when writing new features, fixing bugs, or refactoring code. Enforces test-driven development with 80%+ coverage including unit, integration, and E2E tests.
origin: ECC
stack: Next.js 15/16+, Vitest, Playwright
---

# Test-Driven Development Workflow

This skill ensures all code development follows TDD principles with comprehensive test coverage.

## When to Activate

- Writing new features or functionality
- Fixing bugs or issues
- Refactoring existing code
- Adding API endpoints or Server Actions
- Creating new React components

## Core Principles

### 1. Tests BEFORE Code
ALWAYS write tests first, then implement code to make tests pass.

### 2. Coverage Requirements
- Minimum 80% coverage (unit + integration + E2E)
- All edge cases covered
- Error scenarios tested
- Boundary conditions verified

### 3. Test Types

#### Unit Tests (Vitest + Testing Library)
- Individual functions and utilities
- Component rendering and interactions
- Server Action validation logic
- Pure helper functions

#### Integration Tests (Vitest + MSW)
- API Route Handlers (`app/api/**/route.ts`)
- Server Action database operations
- Service layer with mocked dependencies
- External API integrations

#### E2E Tests (Playwright)
- Critical user flows (auth, CRUD, payments)
- Cross-browser validation
- Accessibility assertions via `@axe-core/playwright`
- Visual regression snapshots

### 4. Git Checkpoints
- Create a checkpoint commit after each TDD stage
- Do not squash checkpoint commits until the workflow is complete
- Each checkpoint commit must describe the stage and evidence captured
- The preferred compact workflow is:
  - One commit for failing test added and RED validated
  - One commit for minimal fix applied and GREEN validated
  - One optional commit for refactor complete

## TDD Workflow Steps

### Step 1: Write User Journeys
```
As a [role], I want to [action], so that [benefit]

Example:
As a user, I want to search products by category,
so that I can find relevant items without browsing all pages.
```

### Step 2: Generate Test Cases
For each user journey, create comprehensive test cases:

```typescript
describe('Product Search', () => {
  it('returns relevant products for a category query', async () => {
    // Arrange
    const category = 'electronics'
    // Act
    const results = await searchProducts({ category })
    // Assert
    expect(results).toHaveLength(5)
    expect(results.every(r => r.category === category)).toBe(true)
  })

  it('handles empty query gracefully', async () => {
    const results = await searchProducts({ category: '' })
    expect(results).toEqual([])
  })

  it('returns empty array when no products match', async () => {
    const results = await searchProducts({ category: 'nonexistent' })
    expect(results).toEqual([])
  })
})
```

### Step 3: Run Tests — They MUST Fail (RED)
```bash
pnpm test
# Tests should fail — we haven't implemented yet
```

This step is mandatory and is the RED gate for all production changes.

Before modifying business logic or production code, you must verify a valid RED state:
- **Runtime RED:** The test compiles, executes, and fails
- **Compile-time RED:** The compile failure is itself the intended RED signal
- The failure is caused by the intended missing implementation, NOT by unrelated syntax errors or broken test setup

A test that was only written but not compiled and executed does not count as RED.

**Do not edit production code until RED is confirmed.**

If under Git, create a checkpoint commit:
```
test: add reproducer for product search feature
```

### Step 4: Implement Minimal Code (GREEN)
Write the minimum code to make tests pass:

```typescript
export async function searchProducts({ category }: { category: string }) {
  if (!category) return []
  return await db.products.findMany({
    where: { category },
    take: 20,
  })
}
```

### Step 5: Run Tests — They MUST Pass (GREEN)
```bash
pnpm test
# Tests should now pass
```

Only after a valid GREEN result may you proceed to refactor.

Checkpoint commit:
```
feat: implement product search by category
```

### Step 6: Refactor (IMPROVE)
Improve code quality while keeping tests green:
- Remove duplication
- Improve naming
- Optimize performance
- Enhance readability
- Extract shared utilities

Checkpoint commit:
```
refactor: clean up product search implementation
```

### Step 7: Verify Coverage
```bash
pnpm test:coverage
# Verify 80%+ coverage achieved
```

## Testing Patterns

### Server Action Test Pattern
```typescript
import { describe, it, expect, vi } from 'vitest'
import { createProduct } from '@/app/actions/products'

vi.mock('@/lib/db', () => ({
  db: {
    products: {
      create: vi.fn().mockResolvedValue({ id: '1', name: 'Test' }),
    },
  },
}))

vi.mock('@/lib/auth', () => ({
  getSession: vi.fn().mockResolvedValue({ user: { id: 'user-1' } }),
}))

describe('createProduct Server Action', () => {
  it('creates a product with valid input', async () => {
    const formData = new FormData()
    formData.set('name', 'Test Product')
    formData.set('price', '29.99')

    const result = await createProduct(formData)

    expect(result.success).toBe(true)
    expect(result.data?.name).toBe('Test')
  })

  it('rejects unauthenticated requests', async () => {
    vi.mocked(getSession).mockResolvedValueOnce(null)

    const formData = new FormData()
    formData.set('name', 'Test Product')

    const result = await createProduct(formData)

    expect(result.success).toBe(false)
    expect(result.error).toBe('Unauthorized')
  })

  it('validates input with Zod', async () => {
    const formData = new FormData()
    formData.set('name', '') // Invalid: empty name
    formData.set('price', 'not-a-number')

    const result = await createProduct(formData)

    expect(result.success).toBe(false)
    expect(result.errors).toBeDefined()
  })
})
```

### E2E Test Pattern (Playwright)
```typescript
import { test, expect } from '@playwright/test'

test.describe('Product Search Flow', () => {
  test('user can search and filter products', async ({ page }) => {
    await page.goto('/products')
    await expect(page.locator('h1')).toContainText('Products')

    await page.fill('[data-testid="search-input"]', 'electronics')
    await page.waitForTimeout(500) // debounce

    const results = page.locator('[data-testid="product-card"]')
    await expect(results.first()).toBeVisible()

    // Verify accessibility
    const violations = await new AxeBuilder({ page }).analyze()
    expect(violations.violations).toHaveLength(0)
  })
})
```

### Component Test Pattern
```typescript
import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { ProductCard } from './ProductCard'

describe('ProductCard', () => {
  const mockProduct = {
    id: '1',
    name: 'Test Product',
    price: 29.99,
    image: '/test.jpg',
  }

  it('renders product information', () => {
    render(<ProductCard product={mockProduct} />)
    expect(screen.getByText('Test Product')).toBeInTheDocument()
    expect(screen.getByText('$29.99')).toBeInTheDocument()
  })

  it('calls onSelect when clicked', async () => {
    const onSelect = vi.fn()
    render(<ProductCard product={mockProduct} onSelect={onSelect} />)

    fireEvent.click(screen.getByRole('button'))
    expect(onSelect).toHaveBeenCalledWith('1')
  })
})
```

## Test File Organization

```
project/
├── tests/
│   ├── unit/
│   │   ├── services/           # Service layer tests
│   │   ├── actions/            # Server Action tests
│   │   └── utils/              # Utility function tests
│   ├── components/             # Component tests (co-located also OK)
│   ├── integration/            # API route tests
│   ├── e2e/
│   │   ├── auth.setup.ts       # Auth state setup
│   │   ├── auth.spec.ts        # Auth flow E2E
│   │   └── products.spec.ts    # Product flow E2E
│   └── mocks/
│       ├── handlers.ts         # MSW handlers
│       └── server.ts           # MSW server setup
├── vitest.config.ts
├── vitest.setup.ts
└── playwright.config.ts
```

## Common Testing Mistakes to Avoid

### WRONG: Testing Implementation Details
```typescript
expect(component.state.count).toBe(5)
```

### CORRECT: Test User-Visible Behavior
```typescript
expect(screen.getByText('Count: 5')).toBeInTheDocument()
```

### WRONG: Brittle Selectors
```typescript
await page.click('.css-class-xyz')
```

### CORRECT: Semantic Selectors
```typescript
await page.click('button:has-text("Submit")')
await page.click('[data-testid="submit-button"]')
```

### WRONG: No Test Isolation
```typescript
test('creates user', () => { /* ... */ })
test('updates same user', () => { /* depends on previous test */ })
```

### CORRECT: Independent Tests (AAA Pattern)
```typescript
test('creates user', () => {
  // Arrange
  const userData = createTestUser()
  // Act
  const result = createUser(userData)
  // Assert
  expect(result.id).toBeDefined()
})
```

## Agent Support

- **tdd-guide** agent — Use PROACTIVELY for new features, enforces write-tests-first
- **e2e-runner** agent — Playwright E2E testing specialist
- **build-error-resolver** agent — Fix test/build failures

## Success Metrics

- 80%+ code coverage achieved
- All tests passing (green)
- No skipped or disabled tests
- Fast test execution (<30s for unit tests)
- E2E tests cover critical user flows
- Git checkpoint commits document RED → GREEN → REFACTOR

---

**Remember**: Tests are not optional. They are the safety net that enables confident refactoring, rapid development, and production reliability.
