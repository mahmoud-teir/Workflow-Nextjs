<a name="phase-8"></a>
# 📌 PHASE 8: SECURITY AUTOMATION (DevSecOps)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.
>
> **Note:** This phase is the single source of truth for CSP headers and security middleware. Phase 4 handles auth-specific middleware only.

---

### Prompt 8.1: CI/CD Security Scanning

```text
You are a DevSecOps Engineer. Implement automated security scanning in the CI/CD pipeline using GitHub Actions.

Tools:
- **Dependency Scanning**: npm audit, Dependabot, Renovate, Socket.dev
- **SAST (Static Application Security Testing)**: CodeQL, Snyk
- **Secret Scanning**: TruffleHog, GitGuardian
- **Container Scanning**: Trivy
- **Supply Chain**: npm provenance verification

Required:
1. GitHub Action workflow for security checks
2. CodeQL initialization and analysis
3. Secret scanning pre-commit hook
4. Dependency vulnerability check (npm audit + Socket.dev)
5. Container vulnerability scanning
6. Report generation and blocking of critical vulnerabilities
```

```yaml
# .github/workflows/security.yml
name: Security Scan

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  schedule:
    - cron: '30 1 * * 0' # Weekly scan

permissions:
  contents: read
  security-events: write

jobs:
  dependency-audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version-file: '.nvmrc'

      - name: Install dependencies
        run: npm ci

      - name: Run npm audit
        run: npm audit --audit-level=high
        continue-on-error: true

      - name: Run Socket.dev Security Check
        uses: SocketDev/socket-security-action@v1
        if: github.event_name == 'pull_request'

  codeql:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Initialize CodeQL
        uses: github/codeql-action/init@v3
        with:
          languages: javascript-typescript
          queries: security-extended

      - name: Perform CodeQL Analysis
        uses: github/codeql-action/analyze@v3

  secret-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Run TruffleHog Secret Scan
        uses: trufflesecurity/trufflehog@main
        with:
          extra_args: --results=verified,unknown

  snyk:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run Snyk to check for vulnerabilities
        uses: snyk/actions/node@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=high

  container-scan:
    runs-on: ubuntu-latest
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4

      - name: Build Docker image
        run: docker build -t app:${{ github.sha }} .

      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'app:${{ github.sha }}'
          format: 'sarif'
          output: 'trivy-results.sarif'
          severity: 'CRITICAL,HIGH'

      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: 'trivy-results.sarif'
```

#### Pre-commit Hook Setup (Husky + TruffleHog):

```bash
# Setup commands
npm install -D husky
npx husky init
```

```bash
# .husky/pre-commit
npx biome check --staged
npx trufflehog filesystem --directory=. --only-verified --fail
```

---

### Prompt 8.2: Content Security Policy & Security Headers (Consolidated)

```text
Implement strict Content Security Policy (CSP) and all security headers using Next.js Middleware.

This is the SINGLE location for security headers — do not duplicate in Phase 4.

Required:
1. Generate nonce for every request
2. Set CSP headers with strict-dynamic for scripts
3. Configure all security headers (HSTS, X-Frame-Options, etc.)
4. Handle external scripts (Analytics, Stripe, Sentry, PostHog)
5. CORS configuration for API routes
6. SRI (Subresource Integrity) guidance for external scripts
```

```typescript
// middleware.ts — Unified security + auth middleware
import { NextResponse, type NextRequest } from 'next/server'

// --------------- Security Headers ---------------

function addSecurityHeaders(request: NextRequest, response: NextResponse): NextResponse {
  const nonce = Buffer.from(crypto.randomUUID()).toString('base64')

  // CSP Directives — add domains for your external services
  const cspHeader = `
    default-src 'self';
    script-src 'self' 'nonce-${nonce}' 'strict-dynamic' https://js.stripe.com;
    style-src 'self' 'unsafe-inline';
    img-src 'self' blob: data: https:;
    font-src 'self';
    connect-src 'self' https://api.stripe.com https://*.sentry.io https://*.posthog.com https://*.ingest.us.sentry.io;
    frame-src 'self' https://js.stripe.com https://hooks.stripe.com;
    frame-ancestors 'none';
    form-action 'self';
    base-uri 'self';
    object-src 'none';
    upgrade-insecure-requests;
  `.replace(/\s{2,}/g, ' ').trim()

  // Set nonce for downstream use (layout.tsx reads this)
  response.headers.set('x-nonce', nonce)

  // Security headers
  response.headers.set('Content-Security-Policy', cspHeader)
  response.headers.set('Strict-Transport-Security', 'max-age=63072000; includeSubDomains; preload')
  response.headers.set('X-Content-Type-Options', 'nosniff')
  response.headers.set('X-Frame-Options', 'DENY')
  response.headers.set('X-XSS-Protection', '0') // Disabled in favor of CSP
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
  response.headers.set('Permissions-Policy', 'camera=(), microphone=(), geolocation=()')
  // Remove server identification headers
  response.headers.delete('X-Powered-By')

  return response
}

// --------------- CORS for API Routes ---------------

const ALLOWED_ORIGINS = [
  process.env.NEXT_PUBLIC_APP_URL,
  // Add other allowed origins
].filter(Boolean) as string[]

function handleCors(request: NextRequest, response: NextResponse): NextResponse {
  const origin = request.headers.get('origin')

  if (origin && ALLOWED_ORIGINS.includes(origin)) {
    response.headers.set('Access-Control-Allow-Origin', origin)
    response.headers.set('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS')
    response.headers.set('Access-Control-Allow-Headers', 'Content-Type, Authorization')
    response.headers.set('Access-Control-Allow-Credentials', 'true')
    response.headers.set('Access-Control-Max-Age', '86400')
  }

  return response
}

// --------------- Auth Protection ---------------

const protectedRoutes = ['/dashboard', '/settings', '/admin']
const authRoutes = ['/login', '/register']

function handleAuth(request: NextRequest): NextResponse | null {
  const { pathname } = request.nextUrl
  const sessionToken = request.cookies.get('session_token')?.value

  if (protectedRoutes.some(route => pathname.startsWith(route)) && !sessionToken) {
    const loginUrl = new URL('/login', request.url)
    loginUrl.searchParams.set('callbackUrl', pathname)
    return NextResponse.redirect(loginUrl)
  }

  if (authRoutes.some(route => pathname.startsWith(route)) && sessionToken) {
    return NextResponse.redirect(new URL('/dashboard', request.url))
  }

  return null // Continue to next middleware
}

// --------------- Main Middleware ---------------

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  // Handle CORS preflight for API routes
  if (pathname.startsWith('/api') && request.method === 'OPTIONS') {
    const response = new NextResponse(null, { status: 204 })
    return handleCors(request, response)
  }

  // Auth redirects
  const authResponse = handleAuth(request)
  if (authResponse) return addSecurityHeaders(request, authResponse)

  // Default: add security headers + CORS
  const response = NextResponse.next()
  addSecurityHeaders(request, response)
  if (pathname.startsWith('/api')) handleCors(request, response)

  return response
}

export const config = {
  matcher: [
    {
      source: '/((?!_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt).*)',
      missing: [
        { type: 'header', key: 'next-router-prefetch' },
        { type: 'header', key: 'purpose', value: 'prefetch' },
      ],
    },
  ],
}
```

```tsx
// app/layout.tsx — Reading the nonce for inline scripts
import { headers } from 'next/headers'

export default async function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const nonce = (await headers()).get('x-nonce') ?? ''

  return (
    <html lang="en">
      <body>
        {children}
        {/* Every inline <script> must carry the nonce — browsers block the rest */}
        <script
          nonce={nonce}
          dangerouslySetInnerHTML={{ __html: 'console.log("secure")' }}
        />
        {/* Pass nonce to third-party script components (Sentry, PostHog, etc.)
            e.g. <Script nonce={nonce} src="..." strategy="afterInteractive" /> */}
      </body>
    </html>
  )
}
```

#### SRI (Subresource Integrity) for External Scripts:

```text
When loading scripts from CDNs, use SRI to verify integrity:

<script
  src="https://cdn.example.com/lib.js"
  integrity="sha384-abc123..."
  crossOrigin="anonymous"
  nonce={nonce}
/>

Generate SRI hashes with: `openssl dgst -sha384 -binary < file.js | openssl base64 -A`
Or use: https://www.srihash.org/

For Next.js: prefer npm packages over CDN scripts when possible (bundled = no SRI needed).
```

---

### Prompt 8.3: Penetration Testing Checklist

```text
Create a checklist for manual and automated penetration testing.

1. **Authentication**:
   - [ ] Test for brute force attacks (rate limiting verified?)
   - [ ] Test for session fixation
   - [ ] Test password reset token expiration (tokens expire after use & time)
   - [ ] Verify 2FA bypass attempts
   - [ ] Test passkey registration/authentication edge cases
   - [ ] Verify account lockout after failed attempts

2. **Authorization (IDOR)**:
   - [ ] Test accessing other users' data by changing IDs in URL
   - [ ] Test accessing admin routes with user role
   - [ ] Test vertical & horizontal privilege escalation
   - [ ] Test Server Action authorization (call directly without UI)

3. **Input Validation**:
   - [ ] Test SQL Injection on all search inputs (should fail with ORM)
   - [ ] Test XSS payloads in comments/profile fields
   - [ ] Test file upload vulnerability (malicious file types, oversized files)
   - [ ] Test prototype pollution in JSON inputs

4. **API Security**:
   - [ ] Test broken object level authorization (BOLA)
   - [ ] Test excessive data exposure (are sensitive fields stripped?)
   - [ ] Test mass assignment (can users set their own role?)
   - [ ] Verify rate limiting on all public endpoints

5. **Configuration**:
   - [ ] Verify no sensitive headers (Server, X-Powered-By removed)
   - [ ] Verify SSL/TLS configuration (strong ciphers only, HSTS)
   - [ ] Verify Content-Security-Policy header present and strict
   - [ ] Verify Strict-Transport-Security header with preload
   - [ ] Check .env files are not accessible via URL

6. **Business Logic**:
   - [ ] Test bypassing payment steps
   - [ ] Test cart manipulation (negative quantities, price tampering)
   - [ ] Test race conditions in concurrent operations

7. **Privacy & Compliance**:
   - [ ] Verify account deletion fully removes PII
   - [ ] Test data export functionality (GDPR data portability)
   - [ ] Verify cookie consent is enforced before tracking

Tools to use:
- **OWASP ZAP** (Automated scanning)
- **Burp Suite** (Manual testing)
- **Nmap** (Port scanning)
- **sqlmap** (SQL injection testing)
- **nuclei** (Vulnerability scanner with templates)
```

```text
Implement automated security workflows and comprehensive manual audit procedures.
```
