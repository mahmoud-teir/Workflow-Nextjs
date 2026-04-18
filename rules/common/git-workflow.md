---
name: git-workflow
description: Git conventions for commits, branches, and PR workflow.
origin: ECC
---

# Git Workflow

## Commit Format
Use conventional commits:
```
type(scope): description

Types: feat, fix, refactor, test, docs, chore, style, perf, ci
```

Examples:
```
feat(auth): implement email/password login
fix(cart): prevent negative quantities
refactor(api): extract rate limiting middleware
test(checkout): add E2E tests for payment flow
docs(readme): add deployment instructions
chore(deps): upgrade Next.js to 16.1
```

## Branch Strategy
```
main (production)
├── develop (staging)
│   ├── feat/user-auth
│   ├── feat/dashboard
│   ├── fix/cart-quantity
│   └── refactor/api-middleware
```

## TDD Checkpoint Commits
During TDD workflow, make checkpoint commits:
```
test: add failing tests for product search    (RED)
feat: implement product search by category     (GREEN)
refactor: clean up product search implementation (REFACTOR)
```

## PR Workflow
1. Create feature branch from `develop`
2. Implement with TDD (commit at each stage)
3. Run verification loop before PR
4. Create PR with description template
5. Address review feedback
6. Squash merge to `develop`

## Rules
- Never force-push to `main` or `develop`
- Keep PRs focused (<400 lines changed)
- Include test coverage in every PR
- Reference issue numbers in commit messages
