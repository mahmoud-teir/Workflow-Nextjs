<a name="phase-7"></a>
# 📌 PHASE 7: TESTING & QA (QA Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 7.1: Unit & Integration Testing (Vitest)

```text
You are a QA Engineer. Set up unit testing for a Next.js application.

Tool: **Vitest** (Faster, modern replacement for Jest, uses Vite)
Library: **React Testing Library**
Mocking: **MSW (Mock Service Worker)** for API mocking

Required:
1. Vitest configuration (`vitest.config.ts`)
2. Testing environment setup (JSDOM/HappyDOM)
3. React Testing Library helpers
4. MSW for mocking API routes
5. Component testing with `render`
6. Hook testing with `renderHook`
7. Server Action testing patterns
8. Test naming conventions and organization
```

```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, '.') },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./tests/setup.ts'],
    include: ['**/*.test.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'tests/',
        'e2e/',
        '**/*.d.ts',
        'components/ui/', // Shadcn components (tested upstream)
      ],
    },
  },
})
```

```typescript
// tests/setup.ts
import '@testing-library/jest-dom/vitest'
import { vi } from 'vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'
import { server } from './mocks/server'

// Clean up after each test
afterEach(() => {
  cleanup()
})

// MSW setup
beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

// Mock next/navigation
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    refresh: vi.fn(),
    back: vi.fn(),
    prefetch: vi.fn(),
  }),
  useSearchParams: () => new URLSearchParams(),
  usePathname: () => '/',
  redirect: vi.fn(),
}))

// Mock next/headers (for Server Component testing)
vi.mock('next/headers', () => ({
  headers: () => new Map(),
  cookies: () => ({
    get: vi.fn(),
    set: vi.fn(),
    delete: vi.fn(),
  }),
}))
```

#### MSW (Mock Service Worker) Setup:

```typescript
// tests/mocks/handlers.ts
import { http, HttpResponse } from 'msw'

export const handlers = [
  http.get('/api/users', () => {
    return HttpResponse.json({
      data: [
        { id: '1', name: 'Alice', email: 'alice@example.com' },
        { id: '2', name: 'Bob', email: 'bob@example.com' },
      ],
    })
  }),

  http.post('/api/users', async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json(
      { data: { id: '3', ...body } },
      { status: 201 }
    )
  }),
]
```

```typescript
// tests/mocks/server.ts
import { setupServer } from 'msw/node'
import { handlers } from './handlers'

export const server = setupServer(...handlers)
```

#### Component Test Examples:

```tsx
// components/user-list.test.tsx
import { render, screen } from '@testing-library/react'
import { UserList } from '@/components/user-list'
import { describe, it, expect } from 'vitest'

describe('UserList', () => {
  it('renders user names and emails', () => {
    const users = [
      { id: '1', name: 'Alice', email: 'alice@example.com' },
      { id: '2', name: 'Bob', email: 'bob@example.com' },
    ]

    render(<UserList users={users} />)

    expect(screen.getByText('Alice')).toBeInTheDocument()
    expect(screen.getByText('bob@example.com')).toBeInTheDocument()
  })

  it('shows empty state when no users', () => {
    render(<UserList users={[]} />)
    expect(screen.getByText(/no users/i)).toBeInTheDocument()
  })
})
```

#### Server Action Testing:

```typescript
// app/actions/user.test.ts
import { describe, it, expect, vi } from 'vitest'
import { createUser } from '@/app/actions/user'

// Mock the database
vi.mock('@/lib/db', () => ({
  db: {
    user: {
      create: vi.fn().mockResolvedValue({ id: '1', name: 'Test', email: 'test@example.com' }),
    },
  },
}))

// Mock auth
vi.mock('@/lib/auth', () => ({
  auth: vi.fn().mockResolvedValue({ user: { id: 'user1', role: 'ADMIN' } }),
}))

describe('createUser action', () => {
  it('creates a user with valid data', async () => {
    const formData = new FormData()
    formData.set('name', 'Test User')
    formData.set('email', 'test@example.com')

    const result = await createUser({ success: false, error: '' }, formData)
    expect(result.success).toBe(true)
  })

  it('returns validation errors for invalid email', async () => {
    const formData = new FormData()
    formData.set('name', 'Test')
    formData.set('email', 'not-an-email')

    const result = await createUser({ success: false, error: '' }, formData)
    expect(result.success).toBe(false)
    expect(result.fieldErrors?.email).toBeDefined()
  })
})
```

#### Test Naming Conventions:

```text
File organization:
- Unit tests: co-located with source files (user-list.test.tsx next to user-list.tsx)
- Integration tests: tests/ directory
- E2E tests: e2e/ directory

Naming pattern:
- describe('ComponentName') or describe('functionName')
- it('should [expected behavior] when [condition]')
- Use arrange/act/assert pattern
```

---

### Prompt 7.2: End-to-End Testing (Playwright)

```text
You are a QA Automation Engineer. Set up E2E testing for critical user flows.

Tool: **Playwright** (Modern, fast, multi-browser)

Required:
1. Playwright configuration (`playwright.config.ts`)
2. Authentication state setup (bypass login for tests)
3. Test scenarios for critical flows
4. Visual regression testing (screenshot comparison)
5. Test database setup for CI
```

```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html'],
    ['json', { outputFile: 'test-results/results.json' }],
  ],
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    // Auth setup project (runs first)
    { name: 'setup', testMatch: /.*\.setup\.ts/ },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'e2e/.auth/user.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'firefox',
      use: {
        ...devices['Desktop Firefox'],
        storageState: 'e2e/.auth/user.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'mobile',
      use: {
        ...devices['Pixel 5'],
        storageState: 'e2e/.auth/user.json',
      },
      dependencies: ['setup'],
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
  },
})
```

```typescript
// e2e/auth.setup.ts — Authenticate once per worker
import { test as setup, expect } from '@playwright/test'

setup('authenticate', async ({ page }) => {
  await page.goto('/login')
  await page.fill('input[name="email"]', 'test@example.com')
  await page.fill('input[name="password"]', 'Test123!@#')
  await page.click('button[type="submit"]')

  // Wait for redirect to dashboard
  await expect(page).toHaveURL(/\/dashboard/)

  // Save auth state for other tests
  await page.context().storageState({ path: 'e2e/.auth/user.json' })
})
```

```typescript
// e2e/dashboard.spec.ts
import { test, expect } from '@playwright/test'

test.describe('Dashboard', () => {
  test('displays user stats', async ({ page }) => {
    await page.goto('/dashboard')
    await expect(page.locator('h1')).toContainText('Dashboard')
    await expect(page.locator('[data-testid="stats-card"]')).toBeVisible()
  })

  test('can create a new post', async ({ page }) => {
    await page.goto('/dashboard')
    await page.click('text=New Post')
    await page.fill('input[name="title"]', 'Test Post')
    await page.fill('textarea[name="content"]', 'Test content')
    await page.click('button[type="submit"]')

    await expect(page.locator('text=Post created')).toBeVisible()
  })
})
```

#### Visual Regression Testing:

```typescript
// e2e/visual.spec.ts
import { test, expect } from '@playwright/test'

test('home page visual regression', async ({ page }) => {
  await page.goto('/')
  // Wait for fonts and images to load
  await page.waitForLoadState('networkidle')

  await expect(page).toHaveScreenshot('home-page.png', {
    maxDiffPixelRatio: 0.01, // Allow 1% pixel difference
  })
})
```

#### Test Database Setup (Docker for CI):

```yaml
# docker-compose.test.yml
services:
  test-db:
    image: postgres:16-alpine
    environment:
      POSTGRES_USER: test
      POSTGRES_PASSWORD: test
      POSTGRES_DB: testdb
    ports:
      - "5433:5432"
    tmpfs:
      - /var/lib/postgresql/data # RAM for speed
```

```text
# CI script
docker compose -f docker-compose.test.yml up -d
DATABASE_URL="postgresql://test:test@localhost:5433/testdb" npx prisma migrate deploy
DATABASE_URL="postgresql://test:test@localhost:5433/testdb" npx prisma db seed
npm run test
docker compose -f docker-compose.test.yml down
```

---

### Prompt 7.3: Performance Testing (Lighthouse CI + k6)

```text
Set up performance auditing and load testing.

Tool: **Lighthouse CI** (Frontend performance)
Tool: **k6** (Backend load testing)
```

```json
// lighthouserc.json
{
  "ci": {
    "collect": {
      "url": ["http://localhost:3000/", "http://localhost:3000/dashboard"],
      "startServerCommand": "npm run start",
      "numberOfRuns": 3
    },
    "assert": {
      "preset": "lighthouse:recommended",
      "assertions": {
        "categories:performance": ["error", { "minScore": 0.9 }],
        "categories:accessibility": ["error", { "minScore": 0.9 }],
        "categories:best-practices": ["error", { "minScore": 0.9 }],
        "first-contentful-paint": ["warn", { "maxNumericValue": 2500 }],
        "interactive": ["warn", { "maxNumericValue": 5000 }]
      }
    },
    "upload": {
      "target": "temporary-public-storage"
    }
  }
}
```

```javascript
// k6/load-test.js
import http from 'k6/http'
import { check, sleep } from 'k6'

export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m', target: 20 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
}

export default function () {
  const res = http.get('http://localhost:3000/api/posts')

  check(res, {
    'status is 200': (r) => r.status === 200,
    'transaction time < 500ms': (r) => r.timings.duration < 500,
    'body is not empty': (r) => r.body.length > 0,
  })

  sleep(1)
}
```

#### Email Template Snapshot Testing:

```typescript
// emails/welcome.test.tsx
import { describe, it, expect } from 'vitest'
import { render } from '@react-email/render'
import WelcomeEmail from '@/emails/welcome'

describe('WelcomeEmail', () => {
  it('matches snapshot', async () => {
    const html = await render(
      WelcomeEmail({ name: 'Test User', url: 'https://example.com' })
    )
    expect(html).toMatchSnapshot()
  })

  it('includes user name', async () => {
    const html = await render(
      WelcomeEmail({ name: 'Alice', url: 'https://example.com' })
    )
    expect(html).toContain('Alice')
  })
})
```

```text
Implement comprehensive testing suite covering unit (Vitest + MSW), E2E (Playwright), visual regression, and performance monitoring.
```
