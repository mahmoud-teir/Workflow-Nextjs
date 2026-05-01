<a name="phase-m9"></a>
# 📌 MOBILE PHASE M9: TESTING & QA (QA Engineer)

> **Testing Stack:** Jest + React Native Testing Library (unit/integration) + Maestro (E2E) — the gold standard for Expo Managed Workflow projects in 2025.

---

### Prompt M9.1: Jest + React Native Testing Library Setup

```text
You are a React Native QA Automation Lead. Set up Jest + React Native Testing Library (RNTL) for [AppName].

Constraints:
- Test behaviors, NOT implementation details (no testing state variable names).
- Mock all native modules (Camera, SecureStore, Haptics, etc.) — they don't run in Node.js.
- Use `user-event` from RNTL for realistic user interaction simulation.
- Maintain ≥80% coverage on core business logic (lib/, not components/).

Required Output Format: Provide complete code for:

1. Installation:
```bash
npm install --save-dev @testing-library/react-native @testing-library/user-event
npm install --save-dev jest-expo
```

2. `jest.config.js`:
```javascript
module.exports = {
  preset: 'jest-expo',
  setupFilesAfterFramework: ['<rootDir>/tests/setup.ts'],
  transformIgnorePatterns: [
    'node_modules/(?!((jest-)?react-native|@react-native(-community)?)|expo(nent)?|@expo(nent)?/.*|@expo-google-fonts/.*|react-navigation|@react-navigation/.*|@unimodules/.*|unimodules|sentry-expo|native-base|react-native-svg|react-native-reanimated)',
  ],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/$1',  // Path alias resolution
  },
  coverageThreshold: {
    global: { branches: 80, functions: 80, lines: 80, statements: 80 },
  },
}
```

3. `tests/setup.ts` — Mocks for all native modules:
```typescript
import '@testing-library/react-native/extend-expect'

// Mock Expo modules
jest.mock('expo-secure-store', () => ({
  getItemAsync: jest.fn().mockResolvedValue(null),
  setItemAsync: jest.fn().mockResolvedValue(undefined),
  deleteItemAsync: jest.fn().mockResolvedValue(undefined),
}))

jest.mock('expo-local-authentication', () => ({
  hasHardwareAsync: jest.fn().mockResolvedValue(true),
  isEnrolledAsync: jest.fn().mockResolvedValue(true),
  authenticateAsync: jest.fn().mockResolvedValue({ success: true }),
}))

jest.mock('expo-haptics', () => ({
  impactAsync: jest.fn(),
  notificationAsync: jest.fn(),
  selectionAsync: jest.fn(),
}))

jest.mock('react-native-mmkv', () => ({
  MMKV: jest.fn().mockImplementation(() => ({
    set: jest.fn(),
    getString: jest.fn(),
    getBoolean: jest.fn(),
    delete: jest.fn(),
  })),
}))

jest.mock('@react-native-community/netinfo', () => ({
  addEventListener: jest.fn(() => jest.fn()),
  fetch: jest.fn().mockResolvedValue({ isConnected: true, isInternetReachable: true }),
}))

// Mock react-native-reanimated
jest.mock('react-native-reanimated', () => {
  const Reanimated = require('react-native-reanimated/mock')
  Reanimated.default.call = () => {}
  return Reanimated
})
```

4. Component test example:
```typescript
import { render, screen, userEvent } from '@testing-library/react-native'
import { Button } from '@/components/ui/Button'

describe('Button', () => {
  it('renders label text', () => {
    render(<Button>Click me</Button>)
    expect(screen.getByText('Click me')).toBeOnTheScreen()
  })

  it('calls onPress when tapped', async () => {
    const user = userEvent.setup()
    const onPress = jest.fn()
    render(<Button onPress={onPress}>Click me</Button>)
    await user.press(screen.getByRole('button'))
    expect(onPress).toHaveBeenCalledTimes(1)
  })

  it('shows loading spinner and disables press when isLoading', async () => {
    const user = userEvent.setup()
    const onPress = jest.fn()
    render(<Button onPress={onPress} isLoading>Click me</Button>)
    await user.press(screen.getByRole('button'))
    expect(onPress).not.toHaveBeenCalled()
    expect(screen.getByRole('button')).toHaveAccessibilityState({ disabled: true })
  })
})
```

5. Zustand store test:
```typescript
import { renderHook, act } from '@testing-library/react-native'
import { useAuthStore } from '@/lib/store/auth'

describe('AuthStore', () => {
  beforeEach(() => {
    useAuthStore.setState({ user: null, isAuthenticated: false })
  })

  it('sets user and marks authenticated on setUser', () => {
    const { result } = renderHook(() => useAuthStore())
    const mockUser = { id: '1', email: 'test@test.com', name: 'Test', role: 'user' as const }

    act(() => result.current.setUser(mockUser))

    expect(result.current.user).toEqual(mockUser)
    expect(result.current.isAuthenticated).toBe(true)
  })

  it('clears user and tokens on logout', async () => {
    const { result } = renderHook(() => useAuthStore())
    act(() => result.current.setUser({ id: '1', email: 'test@test.com', name: 'Test', role: 'user' }))

    await act(() => result.current.logout())

    expect(result.current.user).toBeNull()
    expect(result.current.isAuthenticated).toBe(false)
  })
})
```

⚠️ Common Pitfalls:
- Pitfall: `Cannot use import statement outside a module` error for Expo packages.
- Solution: Add the failing package to `transformIgnorePatterns` whitelist in jest.config.js.
- Pitfall: Reanimated causing `Animated.call is not a function` in tests.
- Solution: Use `react-native-reanimated/mock` as shown in setup.ts.
```

✅ **Verification Checklist:**
- [ ] `npm test` runs without native module errors.
- [ ] All secure store mocks intercept real calls.
- [ ] Button component test passes (render + press + loading state).
- [ ] Store tests reset state in `beforeEach`.

---

### Prompt M9.2: Maestro E2E Testing

```text
You are a Mobile E2E Testing Specialist. Set up Maestro for end-to-end testing of [AppName].

Why Maestro over Detox?
- Maestro works with Expo Managed Workflow (no bare workflow needed).
- YAML-based flows are readable by non-engineers.
- Much simpler setup — no native configuration required.
- Cross-platform: same YAML flows run on iOS and Android.

Constraints:
- E2E tests must run against a real build (development build, not Expo Go).
- Cover critical user flows: auth, core feature loop, logout.
- Use `data-testid` attributes for element selection.

Required Output Format: Provide complete Maestro flows for:

1. Installation:
```bash
# macOS / Linux
curl -Ls "https://get.maestro.mobile.dev" | bash

# Verify
maestro --version
```

2. Auth flow `.maestro/auth_flow.yaml`:
```yaml
appId: com.[company].[appname]
---
- launchApp:
    clearState: true

# Should show login screen
- assertVisible: "Welcome to [AppName]"
- assertVisible:
    id: "email-input"

# Login with test credentials
- tapOn:
    id: "email-input"
- inputText: "test@example.com"
- tapOn:
    id: "password-input"
- inputText: "TestPassword123!"
- tapOn:
    id: "login-button"

# Verify successful login
- waitForAnimationToEnd
- assertVisible: "Home"
- assertNotVisible: "Welcome to [AppName]"
```

3. Core feature flow `.maestro/create_post_flow.yaml`:
```yaml
appId: com.[company].[appname]
---
- launchApp

# Navigate to create
- tapOn:
    id: "create-post-button"

# Fill form
- tapOn:
    id: "title-input"
- inputText: "My Test Post"
- tapOn:
    id: "body-input"
- inputText: "This is the body of my test post with enough content to pass validation."

# Submit
- tapOn:
    id: "submit-post-button"

# Wait for success
- waitForAnimationToEnd
- assertVisible: "Post created!"
- assertVisible: "My Test Post"
```

4. Offline flow `.maestro/offline_flow.yaml`:
```yaml
appId: com.[company].[appname]
---
- launchApp

# Verify offline banner appears when network disabled
- setAirplaneMode: true
- waitForAnimationToEnd
- assertVisible: "You're offline"

# App should still show cached content
- assertVisible: "Home"

# Restore network
- setAirplaneMode: false
- waitForAnimationToEnd
- assertNotVisible: "You're offline"
```

5. Running Maestro:
```bash
# Run all flows
maestro test .maestro/

# Run specific flow
maestro test .maestro/auth_flow.yaml

# Run on iOS Simulator
maestro test --device-id booted .maestro/auth_flow.yaml

# Run with Maestro Cloud (CI)
maestro cloud --apiKey $MAESTRO_API_KEY .maestro/
```

⚠️ Common Pitfalls:
- Pitfall: Running Maestro against Expo Go — it won't work.
- Solution: Build a development build with `eas build --profile development --platform ios` first.
- Pitfall: Maestro can't find elements by text when they're inside complex custom components.
- Solution: Add `testID` props to all interactive and assertable elements.
```

✅ **Verification Checklist:**
- [ ] `maestro test .maestro/auth_flow.yaml` passes on iOS Simulator.
- [ ] `maestro test .maestro/auth_flow.yaml` passes on Android Emulator.
- [ ] Offline flow correctly detects airplane mode and shows banner.
- [ ] All interactive elements have `testID` attributes.

---

### Prompt M9.3: Mobile Testing ECC Integration

```text
Follow the Mobile TDD workflow:

1. RED Phase — Write a failing Jest test for the feature behavior.
2. GREEN Phase — Write minimum code to make the test pass.
3. REFACTOR Phase — Clean up while keeping tests green.
4. E2E Phase — Write a Maestro flow for the critical user journey.

Coverage Requirements:
- Unit/Integration tests: ≥80% coverage on lib/ directory
- E2E: Auth flow + core feature flow + edge case (offline) covered by Maestro

Use the verification loop after each test suite:
→ `npm test -- --coverage` → `maestro test .maestro/` → `npx tsc --noEmit`

Agent Commands:
- `/mobile-tdd-guide` — Enforces RED→GREEN→REFACTOR with mobile-specific patterns
- `/rn-reviewer` — Reviews test code quality and coverage gaps
- `/verify` — Runs the 5-gate mobile quality pipeline
```

---

📎 **Related Phases:**
- Prerequisites: [Phase M8: State Management](./MOBILE_PHASE_8_STATE_MANAGEMENT_Full_Stack_Mobile.md)
- Proceeds to: [Phase M10: Performance Optimization](./MOBILE_PHASE_10_PERFORMANCE_OPTIMIZATION_Mobile_Developer.md)
