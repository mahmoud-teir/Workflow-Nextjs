# Native Navigation with React Navigation

## Overview
Complete guide to implementing type-safe navigation in React Native using React Navigation v6+ with Expo Router support.

## Installation

```bash
npx expo install @react-navigation/native @react-navigation/native-stack @react-navigation/bottom-tabs @react-navigation/drawer react-native-screens react-native-safe-area-context
```

## Stack Navigator Setup

### Basic Configuration
```typescript
// navigation/RootNavigator.tsx
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { RootStackParamList } from './types';

import HomeScreen from '../screens/HomeScreen';
import ProfileScreen from '../screens/ProfileScreen';
import ArticleScreen from '../screens/ArticleScreen';
import SettingsScreen from '../screens/SettingsScreen';

const Stack = createNativeStackNavigator<RootStackParamList>();

export const RootNavigator = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="Home"
        screenOptions={{
          headerShown: true,
          headerStyle: {
            backgroundColor: '#fff',
            elevation: 0,
            shadowOpacity: 0,
          },
          headerTintColor: '#000',
          headerTitleStyle: {
            fontWeight: '600',
          },
          contentStyle: { backgroundColor: '#f5f5f5' },
          animation: 'slide_from_right',
        }}
      >
        <Stack.Screen 
          name="Home" 
          component={HomeScreen}
          options={{ title: 'Home' }}
        />
        <Stack.Screen 
          name="Profile" 
          component={ProfileScreen}
          options={({ route }) => ({ 
            title: `Profile`,
            headerBackTitle: 'Back'
          })}
        />
        <Stack.Screen 
          name="Article" 
          component={ArticleScreen}
          options={{ 
            title: 'Article',
            presentation: 'card',
          }}
        />
        <Stack.Screen 
          name="Settings" 
          component={SettingsScreen}
          options={{ 
            title: 'Settings',
            presentation: 'modal',
            animation: 'slide_from_bottom',
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};
```

## Bottom Tab Navigator

### Tab Configuration with Icons
```typescript
// navigation/MainTabs.tsx
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { MainTabParamList } from './types';
import Ionicons from '@expo/vector-icons/Ionicons';

import HomeTabScreen from '../screens/tabs/HomeTabScreen';
import SearchScreen from '../screens/tabs/SearchScreen';
import ProfileTabScreen from '../screens/tabs/ProfileTabScreen';

const Tab = createBottomTabNavigator<MainTabParamList>();

export const MainTabs = () => {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap;

          if (route.name === 'HomeTab') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Search') {
            iconName = focused ? 'search' : 'search-outline';
          } else if (route.name === 'ProfileTab') {
            iconName = focused ? 'person' : 'person-outline';
          } else {
            iconName = 'help-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#007AFF',
        tabBarInactiveTintColor: '#8E8E93',
        tabBarStyle: {
          backgroundColor: '#fff',
          borderTopWidth: 1,
          borderTopColor: '#E5E5EA',
          paddingBottom: 5,
          paddingTop: 5,
          height: 60,
        },
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '600',
        },
        headerShown: false,
      })}
    >
      <Tab.Screen 
        name="HomeTab" 
        component={HomeTabScreen}
        options={{ title: 'Home' }}
      />
      <Tab.Screen 
        name="Search" 
        component={SearchScreen}
        options={{ title: 'Search' }}
      />
      <Tab.Screen 
        name="ProfileTab" 
        component={ProfileTabScreen}
        options={{ title: 'Profile' }}
      />
    </Tab.Navigator>
  );
};
```

## Nested Navigators

### Stack Inside Tabs
```typescript
// navigation/AppNavigator.tsx
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { RootStackParamList, MainTabParamList, ProfileStackParamList } from './types';
import { MainTabs } from './MainTabs';
import { ProfileStack } from './ProfileStack';
import ArticleScreen from '../screens/ArticleScreen';

const RootStack = createNativeStackNavigator<RootStackParamList>();

export const AppNavigator = () => {
  return (
    <RootStack.Navigator screenOptions={{ headerShown: false }}>
      <RootStack.Screen name="MainTabs" component={MainTabs} />
      <RootStack.Screen 
        name="Article" 
        component={ArticleScreen}
        options={{ 
          presentation: 'card',
          animation: 'slide_from_right',
        }}
      />
      <RootStack.Screen 
        name="ProfileStack" 
        component={ProfileStack}
        options={{ 
          presentation: 'modal',
          animation: 'slide_from_bottom',
        }}
      />
    </RootStack.Navigator>
  );
};
```

## Deep Linking Configuration

### Setup Deep Links
```typescript
// navigation/linking.ts
import { LinkingOptions } from '@react-navigation/native';
import { RootStackParamList } from './types';

const linking: LinkingOptions<RootStackParamList> = {
  prefixes: [
    'myapp://',
    'https://myapp.com',
  ],
  config: {
    screens: {
      Home: '',
      Profile: 'user/:userId',
      Article: 'article/:articleId',
      Settings: 'settings',
      MainTabs: {
        screens: {
          HomeTab: 'home',
          Search: 'search',
          ProfileTab: {
            screens: {
              MyProfile: 'profile',
              EditProfile: 'profile/edit',
            },
          },
        },
      },
    },
  },
  subscribe: (listener) => {
    const onReceiveURL = ({ url }: { url: string }) => listener(url);

    const linkingSubscription = Linking.addEventListener('url', onReceiveURL);

    return () => {
      linkingSubscription.remove();
    };
  },
  getInitialURL: async () => {
    const url = await Linking.getInitialURL();
    return url;
  },
};

export default linking;
```

### Usage in NavigationContainer
```typescript
// App.tsx
import { NavigationContainer } from '@react-navigation/native';
import linking from './navigation/linking';
import { AppNavigator } from './navigation/AppNavigator';

export default function App() {
  return (
    <NavigationContainer linking={linking}>
      <AppNavigator />
    </NavigationContainer>
  );
}
```

## Authentication Flow

### Auth Stack Pattern
```typescript
// navigation/AuthNavigator.tsx
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { AuthStackParamList } from './types';

import LoginScreen from '../screens/auth/LoginScreen';
import RegisterScreen from '../screens/auth/RegisterScreen';
import ForgotPasswordScreen from '../screens/auth/ForgotPasswordScreen';

const AuthStack = createNativeStackNavigator<AuthStackParamList>();

export const AuthNavigator = () => {
  return (
    <AuthStack.Navigator
      screenOptions={{
        headerShown: false,
        animation: 'slide_from_right',
      }}
    >
      <AuthStack.Screen name="Login" component={LoginScreen} />
      <AuthStack.Screen 
        name="Register" 
        component={RegisterScreen}
        options={{
          presentation: 'modal',
          animation: 'slide_from_bottom',
        }}
      />
      <AuthStack.Screen 
        name="ForgotPassword" 
        component={ForgotPasswordScreen}
        options={{
          presentation: 'card',
        }}
      />
    </AuthStack.Navigator>
  );
};
```

### Conditional Rendering Based on Auth State
```typescript
// navigation/AppNavigator.tsx
import { useAuth } from '../hooks/useAuth';
import { AuthNavigator } from './AuthNavigator';
import { MainNavigator } from './MainNavigator';
import SplashScreen from '../screens/SplashScreen';

export const AppNavigator = () => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <SplashScreen />;
  }

  return user ? <MainNavigator /> : <AuthNavigator />;
};
```

## Custom Header Components

### Custom Header with Actions
```typescript
// components/CustomHeader.tsx
import { useNavigation } from '@react-navigation/native';
import { TouchableOpacity, View, Text, StyleSheet } from 'react-native';
import Ionicons from '@expo/vector-icons/Ionicons';

interface CustomHeaderProps {
  title: string;
  showBack?: boolean;
  rightAction?: () => void;
  rightIcon?: string;
}

export const CustomHeader = ({ 
  title, 
  showBack = true,
  rightAction,
  rightIcon 
}: CustomHeaderProps) => {
  const navigation = useNavigation();

  return (
    <View style={styles.container}>
      <View style={styles.left}>
        {showBack && (
          <TouchableOpacity 
            onPress={() => navigation.goBack()}
            style={styles.iconButton}
          >
            <Ionicons name="chevron-back" size={24} color="#000" />
          </TouchableOpacity>
        )}
      </View>
      
      <Text style={styles.title}>{title}</Text>
      
      <View style={styles.right}>
        {rightAction && rightIcon && (
          <TouchableOpacity onPress={rightAction} style={styles.iconButton}>
            <Ionicons name={rightIcon} size={24} color="#007AFF" />
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 50,
    paddingBottom: 10,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5EA',
  },
  left: {
    width: 40,
  },
  right: {
    width: 40,
    alignItems: 'flex-end',
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    flex: 1,
    textAlign: 'center',
  },
  iconButton: {
    padding: 8,
  },
});
```

### Using Custom Header in Screen Options
```typescript
<Stack.Screen
  name="Article"
  component={ArticleScreen}
  options={({ navigation }) => ({
    header: () => (
      <CustomHeader
        title="Article Details"
        rightAction={() => console.log('Share pressed')}
        rightIcon="share-outline"
      />
    ),
  })}
/>
```

## Navigation Guards

### Protected Route HOC
```typescript
// hoc/withAuth.tsx
import React, { useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useNavigation } from '@react-navigation/native';

export function withAuth<P extends object>(
  WrappedComponent: React.ComponentType<P>
) {
  return function ProtectedComponent(props: P) {
    const { user, isLoading } = useAuth();
    const navigation = useNavigation();

    useEffect(() => {
      if (!isLoading && !user) {
        navigation.navigate('Login' as never);
      }
    }, [user, isLoading]);

    if (isLoading || !user) {
      return null;
    }

    return <WrappedComponent {...props} />;
  };
}

// Usage
const ProfileScreen = () => {
  // Screen implementation
};

export default withAuth(ProfileScreen);
```

## Best Practices

1. **Type Safety**: Always define ParamLists for all navigators
2. **Consistent Patterns**: Use the same navigation patterns across the app
3. **Lazy Loading**: Load screens lazily for better performance
4. **Deep Links**: Configure deep linking for all important screens
5. **Animation**: Choose appropriate animations for each navigation type
6. **Header Management**: Centralize header configuration
7. **Auth Flow**: Separate auth and main navigators clearly

## Common Patterns

### Passing Callback Functions
```typescript
// Define in types
export type RootStackParamList = {
  SelectItem: { 
    onSelect: (item: Item) => void;
    multiple?: boolean;
  };
};

// Navigate with callback
navigation.navigate('SelectItem', {
  onSelect: (selectedItem) => {
    setSelectedItem(selectedItem);
  },
  multiple: false,
});
```

### Preventing Back Navigation
```typescript
<Stack.Screen
  name="Login"
  component={LoginScreen}
  options={{
    gestureEnabled: false,
    headerLeft: () => null,
  }}
/>
```

### Dynamic Screen Options
```typescript
<Stack.Screen
  name="Article"
  component={ArticleScreen}
  options={({ route }) => ({
    title: route.params.title,
    headerRight: () => (
      <TouchableOpacity onPress={handleShare}>
        <Ionicons name="share-outline" size={24} color="#007AFF" />
      </TouchableOpacity>
    ),
  })}
/>
```

## Resources
- [React Navigation Docs](https://reactnavigation.org/)
- [TypeScript Guide](https://reactnavigation.org/docs/typescript/)
- [Deep Linking Guide](https://reactnavigation.org/docs/deep-linking/)
