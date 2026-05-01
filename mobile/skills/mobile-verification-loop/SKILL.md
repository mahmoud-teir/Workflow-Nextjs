---
name: mobile-verification-loop
description: Use this skill after completing any mobile feature or before any store submission. Runs a 5-gate quality pipeline specific to React Native / Expo projects.
origin: Mobile ECC
stack: React Native, Expo, Jest, Maestro, TypeScript
---

# Mobile Verification Loop

Run these 5 gates in order. Do NOT skip gates. Each gate must PASS before proceeding.

## Gate 1: TypeScript
```bash
npx tsc --noEmit
```
**Pass criteria:** Zero TypeScript errors.
**Common fixes:**
- Check `tsconfig.json` paths match actual directory structure
- Ensure all Expo SDK types are installed
- Add proper type imports from library definitions

## Gate 2: Lint & Format
```bash
npx biome check . --apply
# OR if using ESLint:
npx eslint . --ext .ts,.tsx --fix
```
**Pass criteria:** Zero lint errors (warnings acceptable).
**Common fixes:**
- Remove unused imports
- Fix missing `key` props on lists
- Fix hook dependency arrays

## Gate 3: Tests
```bash
npm test -- --coverage --passWithNoTests
```
**Pass criteria:** All tests pass + ≥80% coverage on `lib/` directory.
**Common fixes:**
- Add missing native module mocks to `tests/setup.ts`
- Fix broken Zustand store state resets in `beforeEach`
- Update snapshot tests if UI changed intentionally

## Gate 4: Expo Doctor
```bash
npx expo-doctor
```
**Pass criteria:** No critical warnings.
**Common issues:**
- Mismatched Expo SDK versions
- Incompatible package versions
- Missing required config keys in `app.json`

## Gate 5: Security Scan
```bash
# Check for hardcoded secrets
grep -rn --include="*.ts" --include="*.tsx" --include="*.json" \
  -E "(API_KEY|SECRET|PASSWORD|TOKEN)\s*=\s*['\"][^$]" \
  --exclude-dir=node_modules --exclude-dir=.git .

# Check for AsyncStorage with sensitive keys
grep -rn --include="*.ts" --include="*.tsx" \
  "AsyncStorage.setItem.*[tT]oken\|AsyncStorage.setItem.*[pP]assword" \
  --exclude-dir=node_modules .
```
**Pass criteria:** No matches found.
**Common fixes:**
- Move hardcoded keys to `.env` / EAS Secrets
- Replace AsyncStorage token storage with SecureStore

## Pre-Submission Additional Gates

Before EAS Build for production, also run:

### Gate 6: Native Module Compatibility
```bash
npx expo install --check
```
Verifies all native packages are compatible with current Expo SDK.

### Gate 7: Maestro E2E
```bash
maestro test .maestro/ --format=junit
```
**Pass criteria:** All critical user flows pass on both iOS and Android.

## Verification Summary Template

```markdown
## Verification Report — [Feature/Phase]
Date: [date]

| Gate | Status | Notes |
|------|--------|-------|
| TypeScript | ✅ PASS | 0 errors |
| Lint | ✅ PASS | 2 warnings (non-blocking) |
| Tests | ✅ PASS | 84% coverage on lib/ |
| Expo Doctor | ✅ PASS | No issues |
| Security Scan | ✅ PASS | No secrets found |
| Maestro E2E | ✅ PASS | 3/3 flows pass |

**Overall: READY FOR PRODUCTION BUILD**
```

## Common Failure Scenarios

### TypeScript fails with Expo module types
```
Error: Cannot find module 'expo-camera' or its corresponding type declarations
```
Fix: `npx expo install expo-camera` (not `npm install`)

### Jest fails with native module
```
Error: Cannot use import statement outside a module
```
Fix: Add the module to `transformIgnorePatterns` in `jest.config.js`

### Maestro can't find element
```
Element with id 'submit-button' not found
```
Fix: Add `testID="submit-button"` to the element in your component

### Security scan finds secrets in .env
```
API_KEY = "sk-..." found in .env
```
Fix: This is OK if `.env` is in `.gitignore`. Verify with `git check-ignore -v .env`
