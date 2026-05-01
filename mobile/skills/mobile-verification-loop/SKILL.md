---
name: mobile-verification-loop
description: Android CI/CD validation. Runs detekt, ktlint, and tests.
origin: Mobile ECC
stack: Gradle, Detekt, JUnit, Maestro
---

# Android Verification Loop

Run these 4 gates before committing.

## Gate 1: Static Analysis
```bash
./gradlew detekt ktlintCheck
```
**Pass criteria:** Zero errors.

## Gate 2: Unit Tests
```bash
./gradlew testDebugUnitTest
```
**Pass criteria:** All tests pass.

## Gate 3: UI/E2E Tests
```bash
maestro test .maestro/
```
**Pass criteria:** All flows pass on emulator.

## Gate 4: Security Scan
```bash
# Check for secrets
grep -rn --include="*.kt" --include="*.xml" -E "(API_KEY|SECRET)=" --exclude-dir=build .
```
