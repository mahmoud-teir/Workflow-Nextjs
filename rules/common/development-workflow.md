---
name: development-workflow
description: The 5-step development workflow. All development must follow this sequence.
origin: ECC
---

# Development Workflow

Every feature, bug fix, or refactor follows this 5-step workflow:

## Step 1: Search First
Before writing any code, search existing patterns:
- **Codebase**: Search for similar implementations using Grep/Glob
- **Documentation**: Check official docs and `llms.txt` endpoints
- **Packages**: Prefer battle-tested libraries over hand-rolled code
- **Skeleton projects**: Look for 80%+ solutions to adapt

## Step 2: Plan
Use the **planner** agent or `planner-workflow` skill:
- Analyze requirements thoroughly
- Design the solution with specific file paths
- Consider edge cases and error handling
- Define testing strategy
- Get approval before implementation

## Step 3: TDD
Use the **tdd-guide** agent or `tdd-workflow` skill:
- Write tests FIRST (RED)
- Implement minimal code (GREEN)
- Refactor for quality (IMPROVE)
- Commit at each stage

## Step 4: Code Review
Use the **code-reviewer** agent or `code-review` skill:
- Review all changes before committing
- Apply severity-based filtering
- Fix CRITICAL and HIGH issues immediately
- Address MEDIUM issues when convenient

## Step 5: Commit
Follow conventional commit format:
```
type(scope): description

feat(auth): add OAuth2 login flow
fix(api): handle null response from payment gateway
refactor(db): extract repository pattern for users
test(e2e): add checkout flow E2E test
```
