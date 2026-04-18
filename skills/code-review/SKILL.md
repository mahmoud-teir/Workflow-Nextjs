---
name: code-review
description: Use this skill after writing or modifying code. Performs severity-based review with confidence filtering for quality, security, and maintainability.
origin: ECC
stack: Next.js 15/16+, React, TypeScript
---

# Code Review Skill

Ensures high standards of code quality and catches issues before they reach production.

## When to Activate

- After writing or modifying code (ALWAYS)
- Before creating a commit or pull request
- When reviewing AI-generated code changes
- During refactoring sessions
- Before merging any branch

## Review Process

1. **Gather context** — Review `git diff --staged` and `git diff` to see all changes
2. **Understand scope** — Identify which files changed and how they connect
3. **Read surrounding code** — Don't review changes in isolation
4. **Apply review checklist** — Work through CRITICAL → HIGH → MEDIUM → LOW
5. **Report findings** — Use structured output format

## Confidence-Based Filtering

Report ONLY issues you're >80% confident about:

- **Report** — Real issues with clear evidence
- **Skip** — Stylistic preferences (unless violating project conventions)
- **Skip** — Issues in unchanged code (unless CRITICAL security)
- **Consolidate** — Similar issues (e.g., "5 functions missing error handling")
- **Prioritize** — Bugs, security vulnerabilities, data loss risks

## Review Checklist

### Security (CRITICAL)
- Hardcoded credentials (API keys, passwords, tokens)
- SQL injection (string concatenation in queries)
- XSS vulnerabilities (unescaped user input in HTML/JSX)
- Path traversal (user-controlled file paths without sanitization)
- Authentication bypasses (missing auth checks on protected routes)
- Exposed secrets in logs (logging sensitive data)

### Code Quality (HIGH)
- Large functions (>50 lines) — Split into smaller, focused functions
- Large files (>800 lines) — Extract modules by responsibility
- Deep nesting (>4 levels) — Use early returns, extract helpers
- Missing error handling — Unhandled promise rejections, empty catch blocks
- Mutation patterns — Prefer immutable operations
- `console.log` statements — Remove debug logging before merge
- Missing tests — New code paths without test coverage
- Dead code — Commented-out code, unused imports

### React/Next.js Patterns (HIGH)
- Missing dependency arrays in `useEffect`/`useMemo`/`useCallback`
- State updates in render (causes infinite loops)
- Missing keys in lists (or using array index as key)
- Prop drilling (3+ levels → use context or composition)
- Client/server boundary violations (`useState` in Server Components)
- Missing loading/error states for data fetching

### Backend Patterns (HIGH)
- Unvalidated input (request body/params without schema validation)
- Missing rate limiting on public endpoints
- Unbounded queries (`SELECT *` without LIMIT)
- N+1 queries (fetching related data in a loop)
- Missing timeouts on external HTTP calls
- Error message leakage (sending internal errors to clients)

### Performance (MEDIUM)
- Inefficient algorithms (O(n²) when O(n log n) is possible)
- Large bundle sizes (importing entire libraries)
- Missing caching for expensive computations
- Unoptimized images (no compression, no lazy loading)

### Best Practices (LOW)
- TODO/FIXME without ticket references
- Missing JSDoc for public APIs
- Poor naming (single-letter variables in non-trivial contexts)
- Magic numbers without explanation
- Inconsistent formatting

## AI-Generated Code Review Addendum

When reviewing AI-generated changes, additionally prioritize:
1. Behavioral regressions and edge-case handling
2. Security assumptions and trust boundaries
3. Hidden coupling or accidental architecture drift
4. Unnecessary complexity from model generation patterns

## Output Format

```
## Review Summary

| Severity | Count | Status |
|----------|-------|--------|
| CRITICAL | 0     | pass   |
| HIGH     | 2     | warn   |
| MEDIUM   | 3     | info   |
| LOW      | 1     | note   |

Verdict: [APPROVE/WARNING/BLOCK]
```

## Approval Criteria

| Status | Meaning |
|--------|---------|
| **APPROVE** | No CRITICAL or HIGH issues |
| **WARNING** | HIGH issues only (can merge with caution) |
| **BLOCK** | CRITICAL issues found — must fix before merge |

## Agent Support

- **code-reviewer** agent — Automated code review with confidence filtering
- **security-reviewer** agent — Deep security-focused review

---

**Remember**: Every line of code is a potential bug. Review with the assumption that bugs exist — your job is to find them before users do.
