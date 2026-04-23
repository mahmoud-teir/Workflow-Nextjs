---
name: deployment-patterns
description: Pre-deployment workflow including bundle analysis, Lighthouse CI, environment validation, security header verification, and smoke testing.
origin: ECC
stack: Next.js 15/16+, Vercel, GitHub Actions
---

# Deployment Patterns Skill

Ensures smooth, validated deployments from staging to production.

## When to Activate

- Preparing for a deployment
- Setting up CI/CD pipelines (Phase 13)
- Running pre-launch checks (Phase 14)
- Debugging deployment failures
- Optimizing production performance

## Pre-Deployment Checklist

### 1. Build Verification
```bash
pnpm build
# Must succeed with zero errors. Ensure `prisma generate` is part of the build script 
# (e.g., "prisma generate && next build") to prevent "Prisma Client not found" errors in production.
# Check for warnings that indicate potential issues
```

### 2. Bundle Analysis
```bash
ANALYZE=true pnpm build
# Or use Next.js built-in analyzer
# Check for:
# - Bundles > 250KB (first-load JS)
# - Unnecessary large dependencies
# - Duplicate packages in bundle
```

Target thresholds:
- First Load JS: < 100KB per route
- Total shared JS: < 250KB
- Largest page bundle: < 150KB

### 3. Environment Validation
```bash
# Verify all required env vars are set
node -e "
  const required = [
    'DATABASE_URL',
    'NEXTAUTH_SECRET',
    'NEXTAUTH_URL',
  ];
  const missing = required.filter(key => !process.env[key]);
  if (missing.length) {
    console.error('Missing env vars:', missing);
    process.exit(1);
  }
  console.log('All env vars present');
"
```

### 4. Security Headers Verification
After deploying to staging, verify headers:
```bash
curl -I https://staging.example.com | grep -i "x-frame-options\|content-security-policy\|x-content-type\|referrer-policy\|permissions-policy"
```

Expected headers:
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Content-Security-Policy: ...`

### 5. Lighthouse CI
```bash
# Install
npx lhci autorun

# Or run manually
npx lighthouse https://staging.example.com \
  --output json \
  --chrome-flags="--headless"
```

Target scores:
- Performance: > 90
- Accessibility: > 95
- Best Practices: > 95
- SEO: > 95

### 6. Smoke Testing
After deployment, verify critical paths:
```bash
# Health check
curl -f https://production.example.com/api/health

# Auth flow
# Manually verify login → dashboard → protected route

# Core feature
# Manually verify primary user journey
```

## CI/CD Pipeline Template (GitHub Actions)

```yaml
name: CI/CD Pipeline
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: pnpm/action-setup@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 22
          cache: pnpm

      - run: pnpm install --frozen-lockfile

      # Phase 1: Types
      - run: pnpm tsc --noEmit

      # Phase 2: Lint
      - run: pnpm biome check .

      # Phase 3: Test
      - run: pnpm test --coverage
        env:
          DATABASE_URL: ${{ secrets.DATABASE_URL }}

      # Phase 4: Build
      - run: pnpm build

      # Phase 5: Security
      - run: npm audit --audit-level=high

  deploy:
    needs: quality
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
```

## Deployment Strategies

### Preview Deployments (PRs)
- Every PR gets a preview URL via Vercel
- Run E2E tests against preview URL
- Require review approval before merge

### Staging Environment
- Mirror production configuration
- Test with production-like data (sanitized)
- Validate integrations (Stripe, auth, email)

### Production Deployment
- Deploy from `main` branch only
- Monitor error rates for 15 minutes post-deploy
- Keep rollback ready (Vercel instant rollback)

## Post-Deployment Monitoring

After deploying, monitor:
- Error rates (Sentry, Vercel)
- Response times (Vercel Analytics)
- Core Web Vitals (Vercel Speed Insights)
- Database query times

---

**Remember**: A deployment without verification is a gamble. The checklist exists to make deployments boring and predictable.
