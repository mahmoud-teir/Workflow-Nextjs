# Phase 9: Testing Strategy

## Overview
Implement comprehensive testing including unit tests, integration tests, component tests, and E2E tests.

## Key Activities

### 9.1 Install Testing Dependencies
```bash
# Jest & React Native Testing Library
npm install -D jest @types/jest @testing-library/react-native @testing-library/jest-native
npm install -D react-test-renderer

# Mocking
npm install -D jest-expo expo-test-utils

# E2E Testing
npm install -D detox @types/detox
npm install -D appium webdriverio # Alternative E2E option

# Coverage
npm install -D nyc istanbul-lib-coverage
```

### 9.2 Jest Configuration
Create `jest.config.js`:
```javascript
module.exports = {
  preset: 'jest-expo',
  setupFilesAfterEnv: ['@testing-library/jest-native/extend-expect'],
  transformIgnorePatterns: [
    'node_modules/(?!((jest-)?react-native|@react-native(-community)?)|expo(nent)?|@expo(nent)?/.*|@expo-google-fonts/.*|react-navigation|@react-navigation/.*|@unimodules/.*|unimodules|sentry-expo|native-base|react-native-svg)',
  ],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
    '^@components/(.*)$': '<rootDir>/src/components/$1',
    '^@screens/(.*)$': '<rootDir>/src/screens/$1',
    '^@hooks/(.*)$': '<rootDir>/src/hooks/$1',
    '^@utils/(.*)$': '<rootDir>/src/utils/$1',
  },
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/**/__tests__/**',
  ],
  coverageThreshold: {
    global: {
      branches: 70,
      functions: 70,
      lines: 70,
      statements: 70,
    },
  },
};
```

### 9.3 Unit Tests Example
Create `src/utils/__tests__/formatDate.test.ts`:
```typescript
import { formatDate, formatRelativeTime } from '../formatDate';

describe('formatDate', () => {
  it('formats date correctly', () => {
    const date = new Date('2024-01-15T10:30:00Z');
    expect(formatDate(date)).toBe('Jan 15, 2024');
  });

  it('handles invalid date', () => {
    expect(formatDate(null)).toBe('');
  });
});

describe('formatRelativeTime', () => {
  it('shows "just now" for recent dates', () => {
    const now = new Date();
    expect(formatRelativeTime(now)).toBe('Just now');
  });

  it('shows minutes ago', () => {
    const fiveMinAgo = new Date(Date.now() - 5 * 60 * 1000);
    expect(formatRelativeTime(fiveMinAgo)).toBe('5m ago');
  });

  it('shows hours ago', () => {
    const twoHoursAgo = new Date(Date.now() - 2 * 60 * 60 * 1000);
    expect(formatRelativeTime(twoHoursAgo)).toBe('2h ago');
  });
});
```

### 9.4 Component Tests
Create `src/components/ui/__tests__/Button.test.tsx`:
```typescript
import { render, screen, fireEvent } from '@testing-library/react-native';
import { Button } from '../Button';

describe('Button', () => {
  it('renders correctly with title', () => {
    render(<Button title="Click Me" onPress={() => {}} />);
    expect(screen.getByText('Click Me')).toBeTruthy();
  });

  it('calls onPress when pressed', () => {
    const mockOnPress = jest.fn();
    render(<Button title="Click Me" onPress={mockOnPress} />);
    
    fireEvent.press(screen.getByText('Click Me'));
    expect(mockOnPress).toHaveBeenCalledTimes(1);
  });

  it('disables press when loading', () => {
    const mockOnPress = jest.fn();
    render(<Button title="Click Me" onPress={mockOnPress} loading />);
    
    fireEvent.press(screen.getByText('Click Me'));
    expect(mockOnPress).not.toHaveBeenCalled();
  });

  it('applies disabled styles', () => {
    const { getByText } = render(
      <Button title="Click Me" onPress={() => {}} disabled />
    );
    
    expect(getByText('Click Me')).toHaveStyle({ opacity: 0.5 });
  });

  it('shows different variants', () => {
    const { rerender } = render(
      <Button title="Primary" onPress={() => {}} variant="primary" />
    );
    expect(screen.getByText('Primary')).toHaveStyle({ backgroundColor: '#2563eb' });

    rerender(<Button title="Outline" onPress={() => {}} variant="outline" />);
    expect(screen.getByText('Outline')).toHaveStyle({ backgroundColor: 'transparent' });
  });
});
```

### 9.5 Hook Tests
Create `src/hooks/__tests__/useNetworkStatus.test.ts`:
```typescript
import { renderHook, waitFor } from '@testing-library/react-native';
import NetInfo from '@react-native-community/netinfo';
import { useNetworkStatus } from '../useNetworkStatus';

jest.mock('@react-native-community/netinfo');

describe('useNetworkStatus', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('returns initial disconnected state', () => {
    (NetInfo.addEventListener as jest.Mock).mockImplementation((callback) => {
      callback({ isConnected: false, isInternetReachable: false, type: 'none' });
      return () => {};
    });

    const { result } = renderHook(() => useNetworkStatus());

    expect(result.current.isConnected).toBe(false);
  });

  it('updates when network status changes', async () => {
    let listener: any;
    (NetInfo.addEventListener as jest.Mock).mockImplementation((callback) => {
      listener = callback;
      callback({ isConnected: true, isInternetReachable: true, type: 'wifi' });
      return () => {};
    });

    const { result } = renderHook(() => useNetworkStatus());

    expect(result.current.isConnected).toBe(true);

    // Simulate network change
    listener({ isConnected: false, isInternetReachable: false, type: 'none' });

    await waitFor(() => {
      expect(result.current.isConnected).toBe(false);
    });
  });
});
```

### 9.6 Integration Tests
Create `src/screens/__tests__/LoginScreen.test.tsx`:
```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { LoginScreen } from '../LoginScreen';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { authApi } from '../../services/api/authApi';

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
};

describe('LoginScreen', () => {
  it('renders login form', () => {
    render(<LoginScreen />, { wrapper: createWrapper() });
    
    expect(screen.getByPlaceholderText(/email/i)).toBeTruthy();
    expect(screen.getByPlaceholderText(/password/i)).toBeTruthy();
    expect(screen.getByText(/login/i)).toBeTruthy();
  });

  it('shows validation errors for empty fields', async () => {
    render(<LoginScreen />, { wrapper: createWrapper() });
    
    fireEvent.press(screen.getByText(/login/i));
    
    await waitFor(() => {
      expect(screen.getByText(/email is required/i)).toBeTruthy();
    });
  });

  it('calls login API with correct credentials', async () => {
    const mockLogin = jest.spyOn(authApi, 'login').mockResolvedValue({
      token: 'test-token',
      user: { id: '1', email: 'test@example.com', name: 'Test User' },
    });

    render(<LoginScreen />, { wrapper: createWrapper() });
    
    fireEvent.changeText(screen.getByPlaceholderText(/email/i), 'test@example.com');
    fireEvent.changeText(screen.getByPlaceholderText(/password/i), 'password123');
    fireEvent.press(screen.getByText(/login/i));
    
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
    });
  });
});
```

### 9.7 E2E Tests with Detox
Create `e2e/login.spec.js`:
```javascript
describe('Login Flow', () => {
  beforeEach(async () => {
    await device.reloadReactNative();
  });

  it('should show login screen on launch', async () => {
    await expect(element(by.text('Login'))).toBeVisible();
  });

  it('should login successfully with valid credentials', async () => {
    await element(by.id('email-input')).typeText('test@example.com');
    await element(by.id('password-input')).typeText('password123');
    await element(by.id('login-button')).tap();

    await expect(element(by.text('Welcome'))).toBeVisible();
  });

  it('should show error with invalid credentials', async () => {
    await element(by.id('email-input')).typeText('wrong@example.com');
    await element(by.id('password-input')).typeText('wrongpass');
    await element(by.id('login-button')).tap();

    await expect(element(by.text('Invalid credentials'))).toBeVisible();
  });

  it('should navigate to signup screen', async () => {
    await element(by.text("Don't have an account? Sign up")).tap();
    await expect(element(by.text('Sign Up'))).toBeVisible();
  });
});
```

## Deliverables
- [ ] Jest configured
- [ ] Unit tests written for utilities
- [ ] Component tests implemented
- [ ] Hook tests created
- [ ] Integration tests for screens
- [ ] E2E test suite set up
- [ ] CI integration for tests

## Best Practices
- Write tests alongside features
- Mock external dependencies
- Test edge cases and error states
- Maintain good code coverage (>70%)
- Use meaningful test descriptions
- Keep tests independent and isolated
- Run tests in CI pipeline
