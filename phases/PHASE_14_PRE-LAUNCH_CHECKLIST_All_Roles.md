<a name="phase-14"></a>📌 PHASE 14: PRE-LAUNCH CHECKLIST (All Roles)

> **Next.js Version:** This phase uses Next.js (latest stable). See Phase 0, Prompt 0.7 for the version compatibility table.

### Prompt 14.1: Comprehensive Pre-launch Checklist
You are a Product Manager. Create a comprehensive pre-launch checklist covering all aspects of the Next.js application.

> **Priority Levels:** P0 = Must fix before launch (blocker), P1 = Should fix before launch, P2 = Nice to have / can follow up

Checklist Categories:

1. **Security Audit**:
   - [ ] **P0** — Penetration testing completed (automated scan + manual review)
   - [ ] **P0** — Security headers confirmed (HSTS, CSP strict — see Phase 8)
   - [ ] **P0** — SSL/TLS grade A+ verified (ssllabs.com)
   - [ ] **P0** — All Server Actions verify authentication and authorization
   - [ ] **P0** — Input validation (Zod) on ALL forms, Server Actions, and API routes
   - [ ] **P0** — No secrets in client bundles (grep `NEXT_PUBLIC_` env vars)
   - [ ] **P1** — Dependency vulnerability scan clean (`pnpm audit` / Socket.dev)
   - [ ] **P1** — API rate limiting verified (Upstash / custom middleware)
   - [ ] **P1** — Authentication flows tested (login, signup, password reset, MFA)
   - [ ] **P1** — CSRF protection on all mutation endpoints
   - [ ] **P1** — API key rotation procedure documented
   - [ ] **P2** — Pre-commit secret scanning enabled (TruffleHog / Husky)

2. **Performance**:
   - [ ] **P0** — Core Web Vitals passing (LCP < 2.5s, INP < 200ms, CLS < 0.1)
   - [ ] **P0** — Lighthouse scores > 90 (Performance, Accessibility, SEO, Best Practices)
   - [ ] **P1** — Bundle size check (initial JS < 200KB gzipped)
   - [ ] **P1** — Image optimization verified (AVIF/WebP via `next/image`)
   - [ ] **P1** — Font optimization verified (`next/font` with `display: swap`)
   - [ ] **P1** — Database query performance audited (indexes, no N+1 queries)
   - [ ] **P1** — Caching headers verified (stale-while-revalidate, ISR configured)
   - [ ] **P1** — React Compiler compatibility verified (no manual `useMemo`/`useCallback` needed)
   - [ ] **P2** — PPR (Partial Prerendering) enabled for eligible routes
   - [ ] **P2** — Streaming/Suspense boundaries verified (no unnecessary waterfalls)
   - [ ] **P2** — Third-party scripts loaded via Partytown or `afterInteractive`

3. **Functionality**:
   - [ ] **P0** — All user stories accepted and tested
   - [ ] **P0** — Error pages customized (`not-found.tsx`, `error.tsx`, `global-error.tsx`)
   - [ ] **P0** — Form validation working properly (client + server)
   - [ ] **P1** — Cross-browser testing (Chrome, Firefox, Safari, Edge)
   - [ ] **P1** — Mobile responsiveness verified (320px–2560px viewports)
   - [ ] **P1** — Server Action error states handled gracefully (loading, error, success)
   - [ ] **P1** — File uploads working properly (size limits, type validation)
   - [ ] **P1** — Search functionality working properly
   - [ ] **P1** — Internationalization (i18n) verified (if applicable — Phase 9)
   - [ ] **P2** — Streaming responses render progressively (Suspense fallbacks present)
   - [ ] **P2** — Deep links / shareable URLs work correctly

4. **Reliability & Operations**:
   - [ ] **P0** — Database backups configured (point-in-time recovery)
   - [ ] **P0** — Monitoring active (Sentry error tracking + uptime monitoring)
   - [ ] **P0** — Rollback procedure tested (Vercel Instant Rollback or equivalent)
   - [ ] **P1** — Structured logging active (Pino / JSON logs with correlation IDs)
   - [ ] **P1** — Uptime monitoring configured (BetterStack / UptimeRobot)
   - [ ] **P1** — Auto-scaling configured (Vercel / AWS / Kubernetes)
   - [ ] **P1** — Alert routing configured (P0 → PagerDuty, P1 → Slack)
   - [ ] **P2** — Graceful shutdown handler implemented (SIGTERM/SIGINT)
   - [ ] **P2** — Health check endpoint responding (`/api/health`)

5. **Legal & Compliance**:
   - [ ] **P0** — Privacy Policy published and linked in footer
   - [ ] **P0** — Terms of Service published and linked in footer
   - [ ] **P0** — Cookie consent banner implemented (Phase 18) — required if using cookies
   - [ ] **P1** — Data retention policy implemented and documented
   - [ ] **P1** — Account deletion flow works (GDPR right to erasure — Phase 4)
   - [ ] **P1** — Analytics respects user opt-out (consent-gated — Phase 18)
   - [ ] **P2** — CCPA "Do Not Sell" link present (if applicable)
   - [ ] **P2** — Data processing agreements with third parties reviewed

6. **Business & Marketing**:
   - [ ] **P0** — SEO meta tags optimized (Title, Description, OG Image per page)
   - [ ] **P0** — `sitemap.xml` and `robots.txt` generated and verified
   - [ ] **P1** — Analytics configured and consent-gated (Phase 18)
   - [ ] **P1** — Favicon and app icons configured (all sizes)
   - [ ] **P1** — Structured data (JSON-LD) for relevant pages
   - [ ] **P2** — Support channels established (email, chat, docs)
   - [ ] **P2** — Onboarding flow tested end-to-end

7. **AI Safety** (if using Phase 15 — AI/LLM Integration):
   - [ ] **P0** — Prompt injection prevention tested (user input sanitization)
   - [ ] **P0** — Content moderation active (block harmful outputs)
   - [ ] **P0** — AI responses include disclaimer where appropriate
   - [ ] **P1** — Token budget enforced per request and per user
   - [ ] **P1** — Model fallback chain working (primary → fallback → error message)
   - [ ] **P1** — AI rate limiting active (separate from general API limits)
   - [ ] **P2** — Privacy policy covers AI data usage and model training opt-out
   - [ ] **P2** — AI usage costs monitored with alerts

8. **Privacy & Consent**:
   - [ ] **P0** — Cookie consent banner appears on first visit
   - [ ] **P0** — No tracking fires before consent is granted
   - [ ] **P0** — Users can change consent preference in settings
   - [ ] **P1** — Session recordings respect consent (PostHog opt-out)
   - [ ] **P1** — No PII in analytics events (emails, names, IPs stripped)
   - [ ] **P2** — Data export functionality available (GDPR data portability)

---

### Prompt 14.2: Debugging Guide
Troubleshoots common Next.js issues encountered during pre-launch testing.

**Issue: Hydration Mismatch**
```
Error: Text content does not match server-rendered HTML
```
- Ensure HTML structure matches (no `<div>` inside `<p>`, no `<a>` inside `<a>`)
- Handle dynamic values with `suppressHydrationWarning` (dates, random IDs)
- Use `useEffect` for browser-only rendering (window dimensions, localStorage)
- Check browser extensions modifying DOM (disable extensions to test)
- Check timezone differences — use UTC for server-rendered dates
- React 19: Use `--experimental-https` for local dev if CSP issues cause mismatches

```tsx
// Safe pattern for browser-only content
'use client'
import { useEffect, useState } from 'react'

function BrowserOnly({ children }: { children: React.ReactNode }) {
  const [mounted, setMounted] = useState(false)
  useEffect(() => setMounted(true), [])
  if (!mounted) return null // or a skeleton
  return <>{children}</>
}
```

**Issue: `searchParams` / `params` Type Errors (Next.js 15+)**
```
Type 'Promise<{ slug: string }>' is not assignable to type '{ slug: string }'
```
- In Next.js 15+, `params` and `searchParams` are Promises — you must `await` them
```tsx
// ❌ Old pattern (Next.js 14)
export default function Page({ params }: { params: { slug: string } }) { ... }

// ✅ New pattern (Next.js 15+)
export default async function Page({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params
  // ...
}
```

**Issue: `unstable_cache` Behavior Changes**
- `unstable_cache` may be renamed to `cache` or replaced by `cacheLife`/`cacheTag` in future versions
- Always check the Next.js canary release notes before upgrading
- Consider wrapping in a utility function for easy migration:
```typescript
// lib/cache.ts — Wrapper for easy migration
import { unstable_cache } from 'next/cache'

export const appCache = unstable_cache
// When API stabilizes, update only this line
```

**Issue: API Route Timeout (Vercel)**
- Default Vercel function timeout: 10s (Hobby), 60s (Pro), 900s (Enterprise)
- Options:
  1. Use `after()` API to offload work after response is sent
  2. Switch to Edge Runtime: `export const runtime = 'edge'`
  3. Break into smaller operations
  4. Use background jobs (BullMQ) for heavy processing

**Issue: Large Bundle Size**
- Use dynamic imports: `const Component = dynamic(() => import(...))`
- Analyze bundle: `ANALYZE=true pnpm build` (with `@next/bundle-analyzer`)
- Check for accidentally bundled server code (look for `fs`, `crypto` in client chunks)
- Tree-shake barrel exports — import from specific paths, not index files:
```typescript
// ❌ Imports everything
import { Button } from '@/components/ui'

// ✅ Imports only Button
import { Button } from '@/components/ui/button'
```

**Issue: `useFormStatus` Not Working**
- `useFormStatus` must be used inside a component that is a **child** of `<form>`
- It won't work if called in the same component that renders the `<form>`
```tsx
// ❌ Won't work — same component
function MyForm() {
  const { pending } = useFormStatus() // Always false!
  return <form action={myAction}><button disabled={pending}>Submit</button></form>
}

// ✅ Works — child component
function SubmitButton() {
  const { pending } = useFormStatus()
  return <button disabled={pending}>{pending ? 'Saving...' : 'Submit'}</button>
}

function MyForm() {
  return <form action={myAction}><SubmitButton /></form>
}
```

**Issue: Server Component Importing Client Code**
```
Error: Cannot use useState/useEffect in a Server Component
```
- Add `'use client'` to the component using React hooks
- Or refactor: keep the data-fetching in a Server Component, pass data as props to a Client Component

---

### Prompt 14.3: Automated Pre-launch Validation Script

A bash script to automate verifiable checklist items. Run before every production deployment.

```bash
#!/usr/bin/env bash
# scripts/pre-launch-check.sh
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASS=0
WARN=0
FAIL=0

check() {
  local label="$1"
  local result="$2" # 0=pass, 1=warn, 2=fail
  local detail="${3:-}"

  if [ "$result" -eq 0 ]; then
    echo -e "${GREEN}✓${NC} $label"
    ((PASS++))
  elif [ "$result" -eq 1 ]; then
    echo -e "${YELLOW}⚠${NC} $label — $detail"
    ((WARN++))
  else
    echo -e "${RED}✗${NC} $label — $detail"
    ((FAIL++))
  fi
}

echo "=== Pre-launch Validation ==="
echo ""

# 1. TypeScript
echo "--- Type Check ---"
if pnpm tsc --noEmit 2>/dev/null; then
  check "TypeScript compiles" 0
else
  check "TypeScript compiles" 2 "Fix type errors first"
fi

# 2. Lint
echo "--- Lint ---"
if pnpm biome check . 2>/dev/null; then
  check "Biome lint passes" 0
else
  check "Biome lint passes" 2 "Run: pnpm biome check --write ."
fi

# 3. Tests
echo "--- Tests ---"
if pnpm vitest run 2>/dev/null; then
  check "Unit tests pass" 0
else
  check "Unit tests pass" 2 "Fix failing tests"
fi

# 4. Build
echo "--- Build ---"
if pnpm build 2>/dev/null; then
  check "Production build succeeds" 0
else
  check "Production build succeeds" 2 "Fix build errors"
fi

# 5. Bundle size
echo "--- Bundle Size ---"
if [ -f ".next/analyze/client.html" ]; then
  check "Bundle analyzer available" 0
else
  check "Bundle analyzer available" 1 "Run: ANALYZE=true pnpm build"
fi

# 6. Security
echo "--- Security ---"
AUDIT_OUTPUT=$(pnpm audit 2>&1 || true)
if echo "$AUDIT_OUTPUT" | grep -q "0 vulnerabilities"; then
  check "No dependency vulnerabilities" 0
else
  HIGH=$(echo "$AUDIT_OUTPUT" | grep -c "high" || true)
  CRITICAL=$(echo "$AUDIT_OUTPUT" | grep -c "critical" || true)
  if [ "$CRITICAL" -gt 0 ]; then
    check "Dependency vulnerabilities" 2 "$CRITICAL critical, $HIGH high"
  elif [ "$HIGH" -gt 0 ]; then
    check "Dependency vulnerabilities" 1 "$HIGH high severity"
  else
    check "Dependency vulnerabilities" 0
  fi
fi

# 7. Environment variables
echo "--- Environment ---"
REQUIRED_VARS=(DATABASE_URL BETTER_AUTH_SECRET NEXT_PUBLIC_APP_URL)
for var in "${REQUIRED_VARS[@]}"; do
  if [ -n "${!var:-}" ]; then
    check "Env: $var set" 0
  else
    check "Env: $var set" 2 "Missing required variable"
  fi
done

# Summary
echo ""
echo "=== Results ==="
echo -e "${GREEN}Passed: $PASS${NC} | ${YELLOW}Warnings: $WARN${NC} | ${RED}Failed: $FAIL${NC}"

if [ "$FAIL" -gt 0 ]; then
  echo -e "\n${RED}❌ Pre-launch check FAILED. Fix issues above before deploying.${NC}"
  exit 1
elif [ "$WARN" -gt 0 ]; then
  echo -e "\n${YELLOW}⚠️ Pre-launch check passed with warnings. Review before deploying.${NC}"
  exit 0
else
  echo -e "\n${GREEN}✅ All pre-launch checks passed!${NC}"
  exit 0
fi
```

```bash
# One-time setup — make script executable before first run
chmod +x scripts/pre-launch-check.sh
```

```json
// Add to package.json scripts
{
  "scripts": {
    "prelaunch": "bash scripts/pre-launch-check.sh"
  }
}
```

Implement full checklist review with priority levels, automated validation, and modern debugging guide before production deployment.
