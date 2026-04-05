<a name="phase-19"></a>📌 PHASE 19: API DOCUMENTATION & VERSIONING (Backend Engineer)

> **Next.js Version:** This phase uses Next.js (latest stable). See Phase 0, Prompt 0.7 for the version compatibility table.

### Prompt 19.1: OpenAPI Specification & Documentation UI

You are a Backend Engineer. Set up API documentation with OpenAPI specification and a modern documentation UI.

Tool: **Scalar** (Modern API documentation UI, successor to Swagger UI)
Spec: **OpenAPI 3.1** (Latest standard)
Types: **openapi-typescript** (Generate TypeScript types from spec)

```bash
pnpm add @scalar/nextjs-api-reference
pnpm add -D openapi-typescript
```

```typescript
// lib/openapi.ts — OpenAPI specification
import type { OpenAPIV3_1 } from 'openapi-types'

export const openApiSpec: OpenAPIV3_1.Document = {
  openapi: '3.1.0',
  info: {
    title: 'My App API',
    version: '1.0.0',
    description: 'API documentation for My App',
    contact: {
      name: 'API Support',
      email: 'api@myapp.com',
    },
  },
  servers: [
    { url: 'https://myapp.com', description: 'Production' },
    { url: 'http://localhost:3000', description: 'Development' },
  ],
  components: {
    securitySchemes: {
      bearerAuth: {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
      },
      cookieAuth: {
        type: 'apiKey',
        in: 'cookie',
        name: 'session_token',
      },
    },
    schemas: {
      Error: {
        type: 'object',
        properties: {
          error: { type: 'string' },
          message: { type: 'string' },
          statusCode: { type: 'integer' },
        },
        required: ['error', 'message', 'statusCode'],
      },
      Pagination: {
        type: 'object',
        properties: {
          page: { type: 'integer', minimum: 1 },
          limit: { type: 'integer', minimum: 1, maximum: 100 },
          total: { type: 'integer' },
          totalPages: { type: 'integer' },
        },
      },
      User: {
        type: 'object',
        properties: {
          id: { type: 'string', format: 'uuid' },
          email: { type: 'string', format: 'email' },
          name: { type: 'string' },
          role: { type: 'string', enum: ['user', 'admin'] },
          createdAt: { type: 'string', format: 'date-time' },
        },
        required: ['id', 'email', 'name', 'role'],
      },
    },
  },
  paths: {
    '/api/v1/users': {
      get: {
        operationId: 'listUsers',
        summary: 'List users',
        tags: ['Users'],
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: 'page', in: 'query', schema: { type: 'integer', default: 1 } },
          { name: 'limit', in: 'query', schema: { type: 'integer', default: 20 } },
          { name: 'search', in: 'query', schema: { type: 'string' } },
        ],
        responses: {
          '200': {
            description: 'Paginated list of users',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    data: { type: 'array', items: { $ref: '#/components/schemas/User' } },
                    pagination: { $ref: '#/components/schemas/Pagination' },
                  },
                },
              },
            },
          },
          '401': {
            description: 'Unauthorized',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Error' },
              },
            },
          },
        },
      },
    },
    // Add more paths as you build endpoints...
  },
}
```

```typescript
// app/api/docs/route.ts — Serve OpenAPI spec as JSON
import { openApiSpec } from '@/lib/openapi'

export function GET() {
  return Response.json(openApiSpec)
}
```

```tsx
// app/api/docs/ui/route.ts — Scalar documentation UI
import { ApiReference } from '@scalar/nextjs-api-reference'

const config = {
  spec: {
    url: '/api/docs',
  },
  theme: 'kepler', // or 'default', 'moon', 'purple', 'solarized'
  layout: 'modern',
  darkMode: true,
  hideModels: false,
  metaData: {
    title: 'My App — API Reference',
  },
}

export const GET = ApiReference(config)
```

```typescript
// Generate TypeScript types from spec
// package.json scripts:
// "generate:api-types": "openapi-typescript ./lib/openapi.ts -o ./types/api.d.ts"
```

```bash
# Generate types (run after updating spec)
pnpm openapi-typescript http://localhost:3000/api/docs -o types/api.d.ts
```

---

### Prompt 19.2: API Versioning Strategy

Implement API versioning to support backward-compatible changes and smooth migrations.

**Option A: URL Prefix** (Recommended — most explicit)
**Option B: Header-based** (Cleaner URLs, more complex routing)

#### Option A: URL Prefix Versioning

```
/api/v1/users     → Version 1 (current stable)
/api/v2/users     → Version 2 (new features)
```

```
app/
  api/
    v1/
      users/
        route.ts          # GET /api/v1/users
      users/[id]/
        route.ts          # GET/PATCH/DELETE /api/v1/users/:id
    v2/
      users/
        route.ts          # GET /api/v2/users (new response format)
```

```typescript
// app/api/v1/users/route.ts
import { NextRequest } from 'next/server'
import { listUsers } from '@/lib/services/users'

export async function GET(request: NextRequest) {
  const { searchParams } = request.nextUrl
  const page = Number(searchParams.get('page') || '1')
  const limit = Number(searchParams.get('limit') || '20')

  const result = await listUsers({ page, limit })

  return Response.json({
    data: result.users,
    pagination: {
      page,
      limit,
      total: result.total,
      totalPages: Math.ceil(result.total / limit),
    },
  })
}
```

```typescript
// app/api/v2/users/route.ts — V2 with different response shape
import { NextRequest } from 'next/server'
import { listUsers } from '@/lib/services/users'

export async function GET(request: NextRequest) {
  const { searchParams } = request.nextUrl
  const cursor = searchParams.get('cursor')
  const limit = Number(searchParams.get('limit') || '20')

  // V2 uses cursor-based pagination instead of offset
  const result = await listUsers({ cursor, limit })

  return Response.json({
    data: result.users,
    meta: {
      nextCursor: result.nextCursor,
      hasMore: result.hasMore,
    },
  })
}
```

```typescript
// lib/api-versioning.ts — Shared version utilities
export const API_VERSIONS = {
  v1: { status: 'stable', sunset: null },
  v2: { status: 'beta', sunset: null },
} as const

export type ApiVersion = keyof typeof API_VERSIONS

// Deprecation headers for sunset versions
export function withDeprecationHeaders(response: Response, version: ApiVersion): Response {
  const versionInfo = API_VERSIONS[version]

  if (versionInfo.sunset) {
    response.headers.set('Sunset', new Date(versionInfo.sunset).toUTCString())
    response.headers.set('Deprecation', 'true')
    response.headers.set(
      'Link',
      `</api/v2>; rel="successor-version"`
    )
  }

  response.headers.set('X-API-Version', version)
  return response
}
```

#### Option B: Header-based Versioning

```typescript
// middleware.ts — Route to correct version based on Accept header
import { NextRequest, NextResponse } from 'next/server'

export function middleware(request: NextRequest) {
  if (request.nextUrl.pathname.startsWith('/api/')) {
    const accept = request.headers.get('accept') || ''
    const versionMatch = accept.match(/application\/vnd\.myapp\.v(\d+)\+json/)
    const version = versionMatch ? `v${versionMatch[1]}` : 'v1'

    // Rewrite to versioned path internally
    const url = request.nextUrl.clone()
    url.pathname = `/api/${version}${request.nextUrl.pathname.replace('/api', '')}`
    return NextResponse.rewrite(url)
  }
}
```

---

### Prompt 19.3: Type-safe API Client (SDK Generation)

Generate a type-safe API client from your OpenAPI spec using `openapi-fetch`.

```bash
pnpm add openapi-fetch
pnpm add -D openapi-typescript
```

```typescript
// types/api.d.ts — Auto-generated (do not edit manually)
// Run: pnpm openapi-typescript http://localhost:3000/api/docs -o types/api.d.ts

// Example generated output:
export interface paths {
  '/api/v1/users': {
    get: {
      parameters: {
        query?: {
          page?: number
          limit?: number
          search?: string
        }
      }
      responses: {
        200: {
          content: {
            'application/json': {
              data: components['schemas']['User'][]
              pagination: components['schemas']['Pagination']
            }
          }
        }
        401: {
          content: {
            'application/json': components['schemas']['Error']
          }
        }
      }
    }
  }
}

export interface components {
  schemas: {
    User: {
      id: string
      email: string
      name: string
      role: 'user' | 'admin'
      createdAt?: string
    }
    Pagination: {
      page: number
      limit: number
      total: number
      totalPages: number
    }
    Error: {
      error: string
      message: string
      statusCode: number
    }
  }
}
```

```typescript
// lib/api-client.ts — Type-safe client
import createClient from 'openapi-fetch'
import type { paths } from '@/types/api'

export const api = createClient<paths>({
  baseUrl: process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3000',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Usage:
// const { data, error } = await api.GET('/api/v1/users', {
//   params: { query: { page: 1, limit: 20 } },
// })
// data is fully typed: { data: User[], pagination: Pagination }
```

```typescript
// lib/api-client-auth.ts — Client with auth token injection
import createClient from 'openapi-fetch'
import type { paths } from '@/types/api'

export function createAuthenticatedClient(token: string) {
  return createClient<paths>({
    baseUrl: process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3000',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  })
}
```

```json
// package.json — Add generation scripts
{
  "scripts": {
    "generate:api-types": "openapi-typescript http://localhost:3000/api/docs -o types/api.d.ts",
    "generate:api-types:ci": "openapi-typescript ./lib/openapi.ts -o types/api.d.ts"
  }
}
```

---

### Prompt 19.4: API Changelog & Deprecation Workflow

Maintain a changelog for API changes and manage version deprecation gracefully.

#### Changelog Format

```markdown
<!-- CHANGELOG-API.md -->
# API Changelog

All notable changes to the API are documented here.
Format based on [Keep a Changelog](https://keepachangelog.com/).

## [v2] — 2025-06-01

### Added
- Cursor-based pagination on `/api/v2/users` (replaces offset pagination)
- `GET /api/v2/users/:id/activity` — User activity timeline

### Changed
- Response envelope changed: `pagination` → `meta` with cursor fields

### Migration Guide
See [V1 to V2 Migration Guide](./docs/api-migration-v1-v2.md)

## [v1] — 2025-01-15

### Added
- Initial API release
- `GET /api/v1/users` — List users with offset pagination
- `GET /api/v1/users/:id` — Get user by ID
- `PATCH /api/v1/users/:id` — Update user
```

#### Deprecation Timeline Template

```markdown
<!-- docs/api-deprecation-policy.md -->
# API Deprecation Policy

## Timeline
1. **Announcement** (Day 0): Add `Deprecation` header + changelog entry
2. **Migration period** (6 months): Both versions available, docs link to migration guide
3. **Warning phase** (Month 5): Return `Warning` header with sunset date
4. **Sunset** (Month 6): Return 410 Gone with migration instructions

## Communication
- Email all API consumers with deprecation notice
- Add banner to API documentation UI
- Log usage of deprecated endpoints for targeted outreach
```

#### Breaking Change Detection in CI

```yaml
# .github/workflows/api-compat.yml
name: API Compatibility Check

on:
  pull_request:
    paths:
      - 'lib/openapi.ts'
      - 'app/api/**'

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Check for breaking changes
        run: |
          # Export current spec
          git stash
          node -e "
            const { openApiSpec } = require('./lib/openapi');
            require('fs').writeFileSync('/tmp/spec-base.json', JSON.stringify(openApiSpec));
          "
          git stash pop

          # Export PR spec
          node -e "
            const { openApiSpec } = require('./lib/openapi');
            require('fs').writeFileSync('/tmp/spec-pr.json', JSON.stringify(openApiSpec));
          "

          # Compare (using oasdiff or similar tool)
          npx oasdiff breaking /tmp/spec-base.json /tmp/spec-pr.json

      - name: Comment on PR if breaking changes found
        if: failure()
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: '⚠️ **Breaking API changes detected.** Please update the API version and add a migration guide.'
            })
```

#### Migration Guide Template

```markdown
<!-- docs/api-migration-v1-v2.md -->
# Migrating from API v1 to v2

## Breaking Changes

### 1. Pagination
**Before (v1):** Offset-based
```json
GET /api/v1/users?page=2&limit=20

{ "data": [...], "pagination": { "page": 2, "limit": 20, "total": 100 } }
```

**After (v2):** Cursor-based
```json
GET /api/v2/users?cursor=abc123&limit=20

{ "data": [...], "meta": { "nextCursor": "def456", "hasMore": true } }
```

### 2. Response Envelope
- `pagination` → `meta`
- `totalPages` removed (not applicable with cursors)

## Step-by-step Migration
1. Update API client base URL from `/api/v1` to `/api/v2`
2. Replace `page` parameter with `cursor`
3. Update pagination UI to use "Load More" instead of page numbers
4. Test all API calls end-to-end
```

Implement comprehensive API documentation with Scalar UI, type-safe client generation, versioning strategy, and a deprecation workflow for long-term API maintenance.
