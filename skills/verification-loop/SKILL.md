---
name: verification-loop
description: A comprehensive 6-phase verification system. Run after completing a feature, before creating a PR, or as a pre-launch gate.
origin: ECC
stack: Next.js 15/16+, Biome, Vitest, Playwright
---

# Verification Loop Skill

A comprehensive verification system that runs 6 sequential quality gates to ensure code is production-ready.

## When to Use

- After completing a feature or significant code change
- Before creating a pull request
- After refactoring
- As a mandatory pre-launch gate (Phase 14)
- When you want to ensure all quality gates pass

## Verification Phases

### Phase 1: Build Verification
```bash
pnpm build 2>&1 | tail -20
```

If build fails, **STOP and fix before continuing**. A broken build blocks all other verification.

### Phase 2: Type Check
```bash
npx tsc --noEmit 2>&1 | head -30
```

Report all type errors. Fix critical ones before continuing. Zero type errors is the target.

### Phase 3: Lint Check
```bash
pnpm biome check . 2>&1 | head -30
# Or if using ESLint:
pnpm lint 2>&1 | head -30
```

Fix all lint errors. Warnings should be reviewed but may be acceptable.

### Phase 4: Test Suite
```bash
pnpm test -- --coverage 2>&1 | tail -50
```

Report:
- Total tests: X
- Passed: X
- Failed: X
- Coverage: X%

Target: 80% minimum coverage. All tests must pass.

### Phase 5: Security Scan
```bash
# Check for hardcoded secrets
grep -rn "sk-\|sk_live\|sk_test" --include="*.ts" --include="*.tsx" . 2>/dev/null | grep -v node_modules | head -10

# Check for NEXT_PUBLIC_ secrets
grep -rn "NEXT_PUBLIC_.*SECRET\|NEXT_PUBLIC_.*PASSWORD\|NEXT_PUBLIC_.*KEY.*sk" --include="*.ts" --include="*.tsx" . 2>/dev/null | head -10

# Check for console.log in production code
grep -rn "console\.log" --include="*.ts" --include="*.tsx" app/ lib/ components/ 2>/dev/null | grep -v "__tests__\|\.test\.\|\.spec\." | head -10

# Dependency audit
npm audit --audit-level=high 2>&1 | tail -10
```

### Phase 6: Diff Review
```bash
git diff --stat
git diff HEAD~1 --name-only
```

Review each changed file for:
- Unintended changes
- Missing error handling
- Potential edge cases
- Removed test coverage
- Accidental debug code

## Output Format

After running all phases, produce a verification report:

```
╔══════════════════════════════════════╗
║       VERIFICATION REPORT           ║
╠══════════════════════════════════════╣
║                                      ║
║  Build:     [PASS/FAIL]             ║
║  Types:     [PASS/FAIL] (X errors)  ║
║  Lint:      [PASS/FAIL] (X warns)   ║
║  Tests:     [PASS/FAIL] (X/Y, Z%)  ║
║  Security:  [PASS/FAIL] (X issues)  ║
║  Diff:      [X files changed]       ║
║                                      ║
║  Overall:   [READY/NOT READY]       ║
║                                      ║
╚══════════════════════════════════════╝

Issues to Fix:
1. ...
2. ...
```

## Approval Criteria

| Result | Meaning |
|--------|---------|
| **READY** | All phases pass — safe to merge/deploy |
| **NOT READY** | One or more phases failed — fix before proceeding |

A single FAIL in Build, Types, or Security blocks the entire verification.

## Continuous Mode

For long sessions, run verification every 15 minutes or after major changes:

```
Mental checkpoints:
- After completing each function or component
- After finishing an API route or Server Action
- Before moving to the next task
- Before any commit

Run: verification-loop skill
```

## Integration with Phases

| Phase | Verification Use |
|-------|-----------------|
| Phase 7 (Testing) | Run after writing all tests |
| Phase 8 (Security) | Run security phase only |
| Phase 13 (CI/CD) | Integrate as GitHub Action step |
| Phase 14 (Pre-Launch) | Run FULL loop as mandatory gate |

## Integration with Hooks

This skill complements PostToolUse hooks but provides deeper verification. Hooks catch issues immediately (per-file); this skill provides comprehensive review (full-project).

---

**Remember**: Never skip verification. The 5 minutes it takes to run saves hours of debugging in production.
