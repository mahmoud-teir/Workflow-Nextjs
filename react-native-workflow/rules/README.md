# React Native Development Rules

This directory contains coding standards, best practices, and enforced rules for React Native development following the 15-phase workflow.

## Table of Contents

1. [Code Style Rules](#code-style-rules)
2. [Component Rules](#component-rules)
3. [TypeScript Rules](#typescript-rules)
4. [State Management Rules](#state-management-rules)
5. [Navigation Rules](#navigation-rules)
6. [API Integration Rules](#api-integration-rules)
7. [Testing Rules](#testing-rules)
8. [Performance Rules](#performance-rules)
9. [Security Rules](#security-rules)
10. [File Organization Rules](#file-organization-rules)

---

## Code Style Rules

### RULE-001: Use Functional Components with Hooks
**Status:** ✅ Required  
**Description:** All components must be functional components using React hooks. Class components are prohibited.

```typescript
// ✅ Good
const MyComponent = ({ title }: { title: string }) => {
  const [count, setCount] = useState(0);
  return <Text>{title}: {count}</Text>;
};

// ❌ Bad
class MyComponent extends React.Component {
  state = { count: 0 };
  render() {
    return <Text>{this.props.title}: {this.state.count}</Text>;
  }
}
```

### RULE-002: Use TypeScript Strict Mode
**Status:** ✅ Required  
**Description:** Enable strict TypeScript configuration in `tsconfig.json`.

```json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "strictFunctionTypes": true,
    "strictBindCallApply": true,
    "strictPropertyInitialization": true,
    "noImplicitThis": true,
    "alwaysStrict": true
  }
}
```

### RULE-003: Use Named Exports for Components
**Status:** ✅ Required  
**Description:** Prefer named exports over default exports for better tree-shaking and refactoring.

```typescript
// ✅ Good
export const Button = () => { ... };
export const Input = () => { ... };

// ❌ Bad
const Button = () => { ... };
export default Button;
```

### RULE-004: Use Descriptive Variable Names
**Status:** ✅ Required  
**Description:** Variables and functions must have descriptive names that indicate their purpose.

```typescript
// ✅ Good
const isLoadingUserData = true;
const handleUserSubmission = () => { ... };

// ❌ Bad
const loading = true;
const handleSubmit = () => { ... };
```

---

## Component Rules

### RULE-101: Single Responsibility Principle
**Status:** ✅ Required  
**Description:** Each component should have one responsibility. Split large components into smaller ones.

```typescript
// ✅ Good - Separated concerns
export const UserProfile = ({ user }) => (
  <View>
    <UserAvatar uri={user.avatar} />
    <UserInfo name={user.name} email={user.email} />
    <UserActions userId={user.id} />
  </View>
);

// ❌ Bad - Multiple responsibilities
export const UserProfile = ({ user }) => {
  // Avatar logic
  // Info display logic
  // Action handlers
  // API calls
  return <View>...</View>;
};
```

### RULE-102: Prop Types Must Be Explicit
**Status:** ✅ Required  
**Description:** All component props must have explicit TypeScript interfaces or types.

```typescript
// ✅ Good
interface ButtonProps {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  variant?: 'primary' | 'secondary';
}

export const Button = ({ title, onPress, disabled = false }: ButtonProps) => { ... };

// ❌ Bad
export const Button = (props: any) => { ... };
```

### RULE-103: Avoid Inline Styles
**Status:** ✅ Required  
**Description:** Use NativeWind, Tamagui, or StyleSheet.create instead of inline styles.

```typescript
// ✅ Good - Using NativeWind
<TouchableOpacity className="bg-blue-500 px-4 py-2 rounded">
  <Text className="text-white font-semibold">Press Me</Text>
</TouchableOpacity>

// ✅ Good - Using StyleSheet
const styles = StyleSheet.create({
  button: { backgroundColor: '#3b82f6', padding: 8, borderRadius: 4 },
});
<TouchableOpacity style={styles.button}>...</TouchableOpacity>

// ❌ Bad - Inline styles
<TouchableOpacity style={{ backgroundColor: '#3b82f6', padding: 8, borderRadius: 4 }}>
  <Text style={{ color: 'white', fontWeight: '600' }}>Press Me</Text>
</TouchableOpacity>
```

### RULE-104: Memoize Expensive Components
**Status:** ⚠️ Recommended  
**Description:** Use `React.memo` for components that re-render frequently with the same props.

```typescript
// ✅ Good for expensive components
export const ProductList = memo(({ products }: { products: Product[] }) => {
  return products.map(product => <ProductCard key={product.id} product={product} />);
});

// ❌ Unnecessary for simple components
export const Label = memo(({ text }: { text: string }) => <Text>{text}</Text>);
```

---

## TypeScript Rules

### RULE-201: No `any` Type
**Status:** ✅ Required  
**Description:** Never use the `any` type. Use `unknown`, specific types, or generics instead.

```typescript
// ✅ Good
const data: unknown = await fetchData();
if (typeof data === 'object' && data !== null && 'id' in data) { ... }

// ✅ Good with generics
function identity<T>(arg: T): T { return arg; }

// ❌ Bad
const data: any = await fetchData();
```

### RULE-202: Define Response Types for API Calls
**Status:** ✅ Required  
**Description:** All API responses must have explicit TypeScript interfaces.

```typescript
// ✅ Good
interface UserResponse {
  id: number;
  name: string;
  email: string;
  createdAt: string;
}

async function fetchUser(id: number): Promise<UserResponse> {
  const response = await api.get(`/users/${id}`);
  return response.data;
}

// ❌ Bad
async function fetchUser(id: number) {
  const response = await api.get(`/users/${id}`);
  return response.data; // Returns any
}
```

### RULE-203: Use Type Guards for Runtime Validation
**Status:** ⚠️ Recommended  
**Description:** Implement type guards when dealing with external data.

```typescript
// ✅ Good
function isUser(data: unknown): data is User {
  return (
    typeof data === 'object' &&
    data !== null &&
    'id' in data &&
    'name' in data &&
    typeof (data as User).id === 'number' &&
    typeof (data as User).name === 'string'
  );
}

// Usage
if (isUser(apiResponse)) {
  // TypeScript knows this is User
  console.log(apiResponse.name);
}
```

---

## State Management Rules

### RULE-301: Use Zustand for Global State
**Status:** ✅ Required  
**Description:** Use Zustand for global state management. Redux is prohibited unless migrating legacy code.

```typescript
// ✅ Good - Zustand store
interface UserStore {
  user: User | null;
  setUser: (user: User) => void;
  logout: () => void;
}

export const useUserStore = create<UserStore>(set => ({
  user: null,
  setUser: (user) => set({ user }),
  logout: () => set({ user: null }),
}));

// ❌ Bad - Redux boilerplate
const STORE = createStore rootReducer...
```

### RULE-302: Use TanStack Query for Server State
**Status:** ✅ Required  
**Description:** Use TanStack Query (React Query) for server state, not global state managers.

```typescript
// ✅ Good
const { data: users, isLoading } = useQuery({
  queryKey: ['users'],
  queryFn: fetchUsers,
});

// ❌ Bad - Storing server state in Zustand/Redux
const { users } = useUserStore(); // Should use useQuery
```

### RULE-303: Keep Local State Local
**Status:** ✅ Required  
**Description:** Don't put state in global stores if it's only used by one component.

```typescript
// ✅ Good - Local state for local needs
const SearchScreen = () => {
  const [searchQuery, setSearchQuery] = useState(''); // Local is fine
  return <SearchBar value={searchQuery} onChange={setSearchQuery} />;
};

// ❌ Bad - Global state for local needs
const useSearchStore = create(() => ({ query: '' })); // Unnecessary
```

---

## Navigation Rules

### RULE-401: Type-Safe Navigation
**Status:** ✅ Required  
**Description:** All navigation must be fully typed with TypeScript.

```typescript
// ✅ Good
type RootStackParamList = {
  Home: undefined;
  Profile: { userId: number };
  Settings: { initialTab?: 'account' | 'privacy' };
};

const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
navigation.navigate('Profile', { userId: 123 });

// ❌ Bad
navigation.navigate('Profile', { userId: 123 }); // No type safety
```

### RULE-402: Use Navigation Constants
**Status:** ⚠️ Recommended  
**Description:** Define route names as constants to avoid typos.

```typescript
// ✅ Good
export const ROUTES = {
  HOME: 'Home',
  PROFILE: 'Profile',
  SETTINGS: 'Settings',
} as const;

navigation.navigate(ROUTES.PROFILE, { userId: 123 });

// ❌ Bad
navigation.navigate('Proifle', { userId: 123 }); // Typo not caught
```

### RULE-403: Avoid Deep Nesting
**Status:** ✅ Required  
**Description:** Keep navigation hierarchy flat. Max 3 levels of nested navigators.

```typescript
// ✅ Good - Flat structure
RootStack
  ├── AuthStack
  ├── MainTabs
  └── ModalStack

// ❌ Bad - Deep nesting
RootStack
  └── MainStack
      └── TabNavigator
          └── StackNavigator
              └── AnotherStackNavigator
```

---

## API Integration Rules

### RULE-501: Use Axios Interceptors
**Status:** ✅ Required  
**Description:** Configure Axios interceptors for auth tokens and error handling.

```typescript
// ✅ Good
const api = axios.create({ baseURL: API_BASE_URL });

api.interceptors.request.use(config => {
  const token = getAuthToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Handle unauthorized
    }
    return Promise.reject(error);
  }
);

// ❌ Bad - Setting tokens manually in every request
axios.get('/users', { headers: { Authorization: `Bearer ${token}` } });
```

### RULE-502: Handle All Error Cases
**Status:** ✅ Required  
**Description:** Every API call must handle network errors, timeouts, and HTTP errors.

```typescript
// ✅ Good
try {
  const response = await api.get('/users');
  return response.data;
} catch (error) {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') {
      throw new TimeoutError('Request timed out');
    }
    if (error.response?.status === 404) {
      throw new NotFoundError('User not found');
    }
  }
  throw new NetworkError('Network error occurred');
}

// ❌ Bad
const response = await api.get('/users');
return response.data; // No error handling
```

### RULE-503: Use Environment Variables
**Status:** ✅ Required  
**Description:** Store API URLs and secrets in environment variables, never hardcode.

```typescript
// ✅ Good
const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL;

// ❌ Bad
const API_BASE_URL = 'https://api.production.com';
```

---

## Testing Rules

### RULE-601: Minimum Test Coverage
**Status:** ✅ Required  
**Description:** Maintain minimum 80% code coverage for all source files.

```bash
# Run tests with coverage
npm run test -- --coverage

# Check coverage thresholds
{
  "coverageThreshold": {
    "global": {
      "branches": 80,
      "functions": 80,
      "lines": 80,
      "statements": 80
    }
  }
}
```

### RULE-602: Test Critical User Flows
**Status:** ✅ Required  
**Description:** All critical user flows must have E2E tests with Detox.

```typescript
// ✅ Good - E2E test for login flow
describe('Login Flow', () => {
  it('should login successfully with valid credentials', async () => {
    await element(by.id('emailInput')).typeText('user@example.com');
    await element(by.id('passwordInput')).typeText('password123');
    await element(by.id('loginButton')).tap();
    await expect(element(by.id('homeScreen'))).toBeVisible();
  });
});
```

### RULE-603: Mock External Dependencies
**Status:** ✅ Required  
**Description:** Mock API calls, navigation, and native modules in unit tests.

```typescript
// ✅ Good
jest.mock('@react-navigation/native', () => ({
  useNavigation: () => ({ navigate: jest.fn() }),
}));

jest.mock('../api/client', () => ({
  get: jest.fn(),
  post: jest.fn(),
}));

// ❌ Bad - Real API calls in tests
```

---

## Performance Rules

### RULE-701: Optimize Image Loading
**Status:** ✅ Required  
**Description:** Use cached images, proper dimensions, and WebP format.

```typescript
// ✅ Good
import FastImage from 'react-native-fast-image';

<FastImage
  style={{ width: 100, height: 100 }}
  source={{
    uri: imageUrl,
    priority: FastImage.priority.normal,
    cache: FastImage.cacheControl.immutable,
  }}
/>

// ❌ Bad
<Image source={{ uri: imageUrl }} style={{ width: 100, height: 100 }} />
```

### RULE-702: Use FlatList for Long Lists
**Status:** ✅ Required  
**Description:** Always use FlatList or SectionList for rendering lists with more than 10 items.

```typescript
// ✅ Good
<FlatList
  data={items}
  renderItem={({ item }) => <ItemCard item={item} />}
  keyExtractor={item => item.id}
  initialNumToRender={10}
  maxToRenderPerBatch={10}
  windowSize={5}
  removeClippedSubviews={true}
/>

// ❌ Bad
{items.map(item => <ItemCard key={item.id} item={item} />)}
```

### RULE-703: Avoid Unnecessary Re-renders
**Status:** ✅ Required  
**Description:** Use `useCallback`, `useMemo`, and `React.memo` appropriately.

```typescript
// ✅ Good
const Parent = () => {
  const [count, setCount] = useState(0);
  const handleClick = useCallback(() => setCount(c => c + 1), []);
  
  return <Child onClick={handleClick} />;
};

const Child = memo(({ onClick }: { onClick: () => void }) => {
  return <Button onPress={onClick} />;
});

// ❌ Bad - Child re-renders on every parent render
const Child = ({ onClick }) => <Button onPress={onClick} />;
```

---

## Security Rules

### RULE-801: Never Store Sensitive Data in AsyncStorage
**Status:** ✅ Required  
**Description:** Use Expo SecureStore or encrypted storage for tokens and sensitive data.

```typescript
// ✅ Good
import * as SecureStore from 'expo-secure-store';
await SecureStore.setItemAsync('auth_token', token);

// ❌ Bad
import AsyncStorage from '@react-native-async-storage/async-storage';
await AsyncStorage.setItem('auth_token', token);
```

### RULE-802: Validate All User Inputs
**Status:** ✅ Required  
**Description:** Validate and sanitize all user inputs on client and server.

```typescript
// ✅ Good - Using Zod
const userSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8).regex(/[A-Z]/, 'Must contain uppercase'),
  age: z.number().min(18).max(120),
});

const validatedData = userSchema.parse(formData);

// ❌ Bad - No validation
const email = formData.email; // Could be anything
```

### RULE-803: Implement Certificate Pinning
**Status:** ⚠️ Recommended for High-Security Apps  
**Description:** Use certificate pinning for sensitive applications.

```typescript
// ✅ Good - Configure in app.config.js
{
  "plugins": [
    ["expo-network", {
      "sslPinning": {
        "domains": ["api.yourapp.com"]
      }
    }]
  ]
}
```

---

## File Organization Rules

### RULE-901: Follow Feature-Based Structure
**Status:** ✅ Required  
**Description:** Organize files by feature, not by type.

```
✅ Good Structure:
src/
├── features/
│   ├── auth/
│   │   ├── components/
│   │   ├── screens/
│   │   ├── hooks/
│   │   ├── store.ts
│   │   └── api.ts
│   ├── profile/
│   └── settings/
├── shared/
│   ├── components/
│   ├── hooks/
│   └── utils/
└── App.tsx

❌ Bad Structure:
src/
├── components/
├── screens/
├── hooks/
├── store/
└── api/
```

### RULE-902: Limit File Size
**Status:** ⚠️ Recommended  
**Description:** No file should exceed 300 lines. Split larger files.

```typescript
// ✅ Good - Split into multiple files
// LoginForm.tsx (150 lines)
// LoginSchema.ts (50 lines)
// LoginHooks.ts (80 lines)

// ❌ Bad - Single massive file
// LoginFeature.tsx (800 lines)
```

### RULE-903: Use Index Files for Exports
**Status:** ⚠️ Recommended  
**Description:** Create index.ts files for clean imports.

```typescript
// ✅ Good
// features/auth/index.ts
export * from './components/LoginForm';
export * from './screens/LoginScreen';
export * from './hooks/useAuth';

// Import
import { LoginForm, LoginScreen, useAuth } from '@/features/auth';

// ❌ Bad
import LoginForm from '../features/auth/components/LoginForm';
import LoginScreen from '../features/auth/screens/LoginScreen';
```

---

## Enforcement

### Automated Checks
- **ESLint**: Enforces code style rules
- **Prettier**: Ensures consistent formatting
- **TypeScript Compiler**: Catches type errors
- **Husky Pre-commit Hooks**: Runs linting before commits

### Manual Reviews
- **Code Reviews**: All PRs require review
- **Architecture Reviews**: Major changes need architect approval
- **Security Audits**: Regular security reviews

### Violation Handling
1. **Warning**: First violation gets a warning
2. **Fix Required**: PR cannot merge until fixed
3. **Team Discussion**: Repeated violations discussed in team meeting

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-01-01 | Initial release |

---

## Contact

For questions about these rules, contact the mobile architecture team or open an issue in the repository.
