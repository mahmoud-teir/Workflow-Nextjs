<a name="phase-13"></a>
# 📌 PHASE 13: DEPLOYMENT & CI/CD (DevOps Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 13.1: GitHub Actions CI/CD Pipeline

```text
You are a CI/CD Automation Expert. Create a battle-tested GitHub Actions workflow for building and verifying a Next.js application.

Constraints:
- The pipeline MUST run in parallel where possible (e.g., Linting and Testing run concurrently).
- Cache the `.next/cache` and `pnpm` store aggressively to cut build times natively.
- Provide a clear separation between PR validation (build + test) and Main branch operations (deploy).

Required Output Format: Provide complete YAML code for:
1. `.github/workflows/ci.yml`: A workflow triggering on PRs and Main pushes.
2. Steps including: OS dependency setup, Node caching, Biome linting, Typechecking, Vitest coverage, Next.js build.
3. Documentation outlining Branch Protection Rules you recommend enforcing on `main`.

⚠️ Common Pitfalls:
- **Pitfall:** Failing to properly key the Next.js cache action, resulting in Next.js rebuilding every single page from scratch on every commit.
- **Solution:** Use `${{ runner.os }}-nextjs-${{ hashFiles('**/pnpm-lock.yaml') }}` as the exact cache key block.
```

✅ **Verification Checklist:**
- [ ] Push a branch with a deliberate TypeScript error. The CI pipeline MUST fail and block the PR merge.
- [ ] Check the execution time of the second pipeline run on a branch; it should be >30% faster due to `.next/cache` hits.

---

### Prompt 13.2: Automated Releases (Semantic Versioning)

```text
You are a Release Manager. Configure `semantic-release` for automated versioning.

Constraints:
- Rely strictly on Conventional Commits (`feat:`, `fix:`) to determine the SemVer increment.
- Auto-generate a `CHANGELOG.md` upon every successful release to `main`.
- Prevent accidental npm publishing unless the app is genuinely an SDK/Library.

Required Output Format:
1. `.releaserc.json` configuration file.
2. `.github/workflows/release.yml` executing the release command.
```

✅ **Verification Checklist:**
- [ ] Create a commit titled `feat: add new dashboard`. Verify semantic-release bumps the minor version automatically upon merge.

---

### Prompt 13.3: Deployment Automation (Vercel)

```text
You are a Vercel Deployment Specialist. Configure automated deployments to Vercel (or alternative).

Constraints:
- Override default commands in `vercel.json` if building a monorepo.
- Ensure Edge Functions and Serverless Functions have explicitly defined max-durations if needed (preventing 10s timeouts).
- Document the emergency rollback command strategy.

Required Output Format:
1. `vercel.json`: Outlining custom build configs, framework specs, and routing headers.
2. Rollback strategy explanation (via UI or CLI).
3. Secrets sync guidelines (`vercel env pull`).

⚠️ Common Pitfalls:
- **Pitfall:** Heavy API routes timing out on Vercel's Hobby (10s) or Pro (60s) tiers without warning.
- **Solution:** Define `"maxDuration"` in the config, and utilize the Next.js `after()` API or background BullMQ queues for long tasks.
```

✅ **Verification Checklist:**
- [ ] Execute `vercel env pull .env.local` to securely pull production secrets locally.
- [ ] Deploy the app. Verify the Vercel dashboard reports the deployment as healthy.

---
📎 **Related Phases:**
- Prerequisites: [Phase 12: Observability & Monitoring](./PHASE_12_OBSERVABILITY__MONITORING_DevOps_SRE.md)
- Proceeds to: [Phase 14: Pre-Launch Checklist](./PHASE_14_PRE-LAUNCH_CHECKLIST_All_Roles.md)
