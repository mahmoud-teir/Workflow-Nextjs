# Phase 3: Navigation Architecture

## Overview
Implement a robust navigation system using React Navigation with proper type safety, deep linking, and navigation patterns.

## Key Activities

### 3.1 Install Navigation Dependencies
```bash
npm install @react-navigation/native
npm install react-native-screens react-native-safe-area-context
npm install @react-navigation/native-stack
npm install @react-navigation/bottom-tabs
npm install @react-navigation/drawer # if needed
npm install react-native-gesture-handler react-native-reanimated
```

### 3.2 Root Navigation Setup
Create `src/navigation/RootNavigator.tsx`:
```typescript
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { BottomTabNavigator } from './BottomTabNavigator';
import { AuthScreen, OnboardingScreen, ModalScreen } from '../screens';
import type { RootStackParamList } from '../types/navigation';

const RootStack = createNativeStackNavigator<RootStackParamList>();

export function RootNavigator() {
  return (
    <NavigationContainer>
      <RootStack.Navigator screenOptions={{ headerShown: false }}>
        <RootStack.Screen name="Onboarding" component={OnboardingScreen} />
        <RootStack.Screen name="Auth" component={AuthScreen} />
        <RootStack.Screen 
          name="MainTabs" 
          component={BottomTabNavigator}
          options={{ gestureEnabled: false }}
        />
        <RootStack.Group screenOptions={{ presentation: 'modal' }}>
          <RootStack.Screen name="SettingsModal" component={ModalScreen} />
        </RootStack.Group>
      </RootStack.Navigator>
    </NavigationContainer>
  );
}
```

### 3.3 Type-Safe Navigation
Create `src/types/navigation.ts`:
```typescript
export type RootStackParamList = {
  Onboarding: undefined;
  Auth: { initialScreen?: 'Login' | 'Signup' };
  MainTabs: undefined;
  SettingsModal: { settingId?: string };
};

export type BottomTabParamList = {
  Home: undefined;
  Search: undefined;
  Profile: { userId?: string };
  Notifications: undefined;
};

export type HomeStackParamList = {
  HomeFeed: undefined;
  PostDetail: { postId: string };
  UserProfile: { userId: string };
};

declare global {
  namespace ReactNavigation {
    interface RootParamList extends RootStackParamList {}
  }
}
```

### 3.4 Bottom Tab Navigator
Create `src/navigation/BottomTabNavigator.tsx`:
```typescript
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import { HomeStack } from './HomeStack';
import { SearchScreen, ProfileScreen, NotificationsScreen } from '../screens';
import type { BottomTabParamList } from '../types/navigation';

const Tab = createBottomTabNavigator<BottomTabParamList>();

export function BottomTabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap;

          if (route.name === 'Home') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Search') {
            iconName = focused ? 'search' : 'search-outline';
          } else if (route.name === 'Profile') {
            iconName = focused ? 'person' : 'person-outline';
          } else {
            iconName = focused ? 'notifications' : 'notifications-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#007AFF',
        tabBarInactiveTintColor: '#8E8E93',
        headerShown: false,
      })}
    >
      <Tab.Screen name="Home" component={HomeStack} />
      <Tab.Screen name="Search" component={SearchScreen} />
      <Tab.Screen name="Profile" component={ProfileScreen} />
      <Tab.Screen name="Notifications" component={NotificationsScreen} />
    </Tab.Navigator>
  );
}
```

### 3.5 Nested Stack Navigators
Create `src/navigation/HomeStack.tsx`:
```typescript
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { HomeFeedScreen, PostDetailScreen, UserProfileScreen } from '../screens';
import type { HomeStackParamList } from '../types/navigation';

const HomeStackScreen = createNativeStackNavigator<HomeStackParamList>();

export function HomeStack() {
  return (
    <HomeStackScreen.Navigator screenOptions={{ headerShown: false }}>
      <HomeStackScreen.Screen name="HomeFeed" component={HomeFeedScreen} />
      <HomeStackScreen.Screen name="PostDetail" component={PostDetailScreen} />
      <HomeStackScreen.Screen name="UserProfile" component={UserProfileScreen} />
    </HomeStackScreen.Navigator>
  );
}
```

### 3.6 Deep Linking Configuration
Configure in `app.json` (Expo) or `AndroidManifest.xml` / `Info.plist`:
```json
{
  "expo": {
    "scheme": "myapp",
    "web": {
      "bundler": "metro"
    },
    "plugins": [
      [
        "expo-router",
        {
          "origin": "https://myapp.com"
        }
      ]
    ]
  }
}
```

Update NavigationContainer:
```typescript
<NavigationContainer
  linking={{
    prefixes: ['myapp://', 'https://myapp.com'],
    config: {
      screens: {
        PostDetail: 'post/:postId',
        UserProfile: 'user/:userId',
      },
    },
  }}
>
  {/* navigators */}
</NavigationContainer>
```

### 3.7 Navigation Utilities & Hooks
Create `src/hooks/useNavigation.ts`:
```typescript
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '../types/navigation';

export function useAppNavigation() {
  return useNavigation<NativeStackNavigationProp<RootStackParamList>>();
}
```

Create `src/utils/navigationHelpers.ts`:
```typescript
import { CommonActions } from '@react-navigation/native';

export const navigationHelpers = {
  resetToScreen: (screenName: string) =>
    CommonActions.reset({
      index: 0,
      routes: [{ name: screenName }],
    }),
  
  navigateAndClearHistory: (screenName: string, params?: object) =>
    CommonActions.reset({
      index: 0,
      routes: [{ name: screenName, params }],
    }),
};
```

## Deliverables
- [ ] Root navigator configured
- [ ] Type-safe navigation setup
- [ ] Bottom tabs implemented
- [ ] Nested stacks configured
- [ ] Deep linking enabled
- [ ] Custom navigation hooks created
- [ ] Navigation utilities implemented

## Best Practices
- Always use TypeScript for navigation types
- Keep navigation logic separate from business logic
- Use `useFocusEffect` for screen-specific side effects
- Implement proper loading states during navigation
- Handle navigation errors gracefully
- Test deep links on both platforms
