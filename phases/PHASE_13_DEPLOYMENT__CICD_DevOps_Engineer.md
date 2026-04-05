<a name="phase-13"></a>
# 📌 PHASE 13: DEPLOYMENT & CI/CD (DevOps Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 13.1: GitHub Actions CI/CD Pipeline

```text
You are a DevOps Engineer. Create a robust GitHub Actions workflow for building, testing, and deploying a Next.js application.

Tool: **GitHub Actions** + (**Vercel CLI** or **AWS**)

Required:
1. CI steps: Install, Lint (Biome), Typecheck, Test (Vitest), Build, E2E (Playwright)
2. Caching: dependencies + Next.js build cache
3. Security scanning: npm audit + CodeQL
4. Test coverage reporting
5. Deployment trigger (only on main branch)
6. Preview deployments on PRs
7. Deployment notifications
```

```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  lint-and-typecheck:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v4
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version-file: '.nvmrc'
          cache: 'pnpm'

      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Lint (Biome)
        run: pnpm run lint

      - name: Typecheck
        run: pnpm run typecheck

  test:
    runs-on: ubuntu-latest
    needs: lint-and-typecheck
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v4
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version-file: '.nvmrc'
          cache: 'pnpm'

      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Run unit tests with coverage
        run: pnpm run test:coverage

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          token: ${{ secrets.CODECOV_TOKEN }}
          files: ./coverage/lcov.info

  build:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v4
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version-file: '.nvmrc'
          cache: 'pnpm'

      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Cache Next.js build
        uses: actions/cache@v4
        with:
          path: .next/cache
          key: ${{ runner.os }}-nextjs-${{ hashFiles('**/pnpm-lock.yaml') }}-${{ hashFiles('**/*.ts', '**/*.tsx') }}
          restore-keys: |
            ${{ runner.os }}-nextjs-${{ hashFiles('**/pnpm-lock.yaml') }}-

      - name: Build Next.js
        run: pnpm run build

      - name: Upload build artifact
        uses: actions/upload-artifact@v4
        with:
          name: nextjs-build
          path: .next/

  e2e:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v4
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version-file: '.nvmrc'
          cache: 'pnpm'

      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Download build artifact
        uses: actions/download-artifact@v4
        with:
          name: nextjs-build
          path: .next/

      - name: Install Playwright browsers
        run: pnpm exec playwright install --with-deps chromium

      - name: Run E2E tests
        run: pnpm run test:e2e

      - name: Upload Playwright report
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 14

  security:
    runs-on: ubuntu-latest
    if: github.event_name == 'pull_request'
    steps:
      - uses: actions/checkout@v4

      - name: Run npm audit
        run: npx audit-ci --high

      - name: Initialize CodeQL
        uses: github/codeql-action/init@v3
        with:
          languages: javascript-typescript

      - name: Perform CodeQL Analysis
        uses: github/codeql-action/analyze@v3

  deploy:
    runs-on: ubuntu-latest
    needs: [e2e]
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    steps:
      - uses: actions/checkout@v4

      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          vercel-args: '--prod'

      - name: Notify deployment
        if: always()
        uses: rtCamp/action-slack-notify@v2
        env:
          SLACK_WEBHOOK: ${{ secrets.SLACK_WEBHOOK }}
          SLACK_CHANNEL: deployments
          SLACK_TITLE: 'Deployment ${{ job.status }}'
          SLACK_MESSAGE: '${{ github.sha }} deployed to production'
          SLACK_COLOR: ${{ job.status }}
```

#### Branch Protection Rules (Recommended):

```text
Configure on GitHub → Settings → Branches → Branch protection rules:

For `main` branch:
- ✅ Require status checks to pass (lint-and-typecheck, test, build, e2e)
- ✅ Require branches to be up to date before merging
- ✅ Require pull request reviews (1+ approver)
- ✅ Require conversation resolution before merging
- ✅ Require signed commits (optional but recommended)
- ❌ Allow force pushes (never on main)
```

---

### Prompt 13.2: Automated Releases (Semantic Versioning)

```text
Configure semantic-release for automated versioning and changelog generation.

Tool: **semantic-release**
Convention: **Conventional Commits** (feat, fix, docs, refactor)
```

```json
// .releaserc.json
{
  "branches": ["main"],
  "plugins": [
    "@semantic-release/commit-analyzer",
    "@semantic-release/release-notes-generator",
    ["@semantic-release/changelog", { "changelogFile": "CHANGELOG.md" }],
    ["@semantic-release/npm", { "npmPublish": false }],
    ["@semantic-release/git", {
      "assets": ["CHANGELOG.md", "package.json"],
      "message": "chore(release): ${nextRelease.version} [skip ci]"
    }],
    "@semantic-release/github"
  ]
}
```

```yaml
# .github/workflows/release.yml
name: Release
on:
  push:
    branches: [main]

permissions:
  contents: write
  issues: write
  pull-requests: write

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
          persist-credentials: false

      - uses: actions/setup-node@v4
        with:
          node-version-file: '.nvmrc'

      - name: Semantic Release
        run: npx semantic-release
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

### Prompt 13.3: Deployment to Vercel (Preview & Production)

```text
Configure automated Vercel deployments.

Strategy:
1. **Preview Deployments**: Automatic per PR (Vercel GitHub Integration)
2. **Production Deployments**: Merge to `main` branch
3. **Environment Sync**: Vercel CLI for secrets management
4. **Rollback**: Instant rollback to previous deployment
```

```json
// vercel.json
{
  "buildCommand": "pnpm build",
  "devCommand": "pnpm dev",
  "installCommand": "pnpm install",
  "framework": "nextjs",
  "regions": ["iad1"],
  "functions": {
    "app/api/**/*.ts": {
      "maxDuration": 60,
      "memory": 1024
    }
  },
  "crons": [
    {
      "path": "/api/cron/cleanup",
      "schedule": "0 0 * * *"
    }
  ]
}
```

#### Rollback Strategy:

```text
Vercel:
- Instant rollback via Vercel Dashboard → Deployments → Promote previous deployment
- CLI: `vercel rollback` or `vercel promote <deployment-url>`
- Automatic rollback trigger: if health check fails within 5 min of deploy

ECS:
- Rolling update with health check (old tasks stay until new ones are healthy)
- Circuit breaker: auto-rollback if >50% of tasks fail to start
- Manual: `aws ecs update-service --force-new-deployment --task-definition <previous-version>`

General:
- Always keep last 3 deployments available for rollback
- Database migrations must be backward-compatible (see Phase 3 rollback strategy)
- Feature flags (Phase 18) allow disabling features without rollback
```

#### Environment Variable Management:

```bash
# Pull env vars from Vercel
vercel env pull .env.local

# Add a new env var
vercel env add SECRET_KEY production

# List all env vars
vercel env ls
```

```text
Implement fully automated CI/CD pipeline with security scanning, test coverage, preview deployments, and instant rollback capability.
```
