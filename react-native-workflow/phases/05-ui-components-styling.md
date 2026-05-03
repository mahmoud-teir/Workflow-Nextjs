# Phase 5: UI Components & Styling System

## Overview
Build a consistent, reusable component library with a modern styling approach using NativeWind (Tailwind CSS for React Native) or Tamagui.

## Key Activities

### 5.1 Choose Styling Solution

#### Option A: NativeWind (Tailwind CSS)
- Pros: Familiar Tailwind syntax, fast development, small bundle
- Cons: Less theming flexibility than Tamagui

#### Option B: Tamagui
- Pros: Excellent theming, optimized performance, universal (web + native)
- Cons: Steeper learning curve, larger bundle

### 5.2 NativeWind Setup

Install dependencies:
```bash
npm install nativewind
npm install -D tailwindcss
npx tailwindcss init
```

Configure `tailwind.config.js`:
```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./App.{js,jsx,ts,tsx}",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        },
        secondary: {
          500: '#8b5cf6',
          600: '#7c3aed',
        },
      },
      fontFamily: {
        sans: ['Inter', 'System'],
        heading: ['Poppins', 'System'],
      },
    },
  },
  plugins: [],
};
```

Configure `babel.config.js`:
```javascript
module.exports = function (api) {
  api.cache(true);
  return {
    presets: ['babel-preset-expo'],
    plugins: ['nativewind/babel'],
  };
};
```

Create `nativewind-env.d.ts`:
```typescript
/// <reference types="nativewind/types" />
```

### 5.3 Core Component Library

Create `src/components/ui/Button.tsx`:
```typescript
import { TouchableOpacity, Text, ActivityIndicator } from 'react-native';
import { twMerge } from 'tailwind-merge';
import type { ViewStyle, TextStyle } from 'react-native';

interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  disabled?: boolean;
  className?: string;
  style?: ViewStyle;
  textStyle?: TextStyle;
}

export function Button({
  title,
  onPress,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  className = '',
  style,
  textStyle,
}: ButtonProps) {
  const baseStyles = 'rounded-lg items-center justify-center flex-row';
  
  const variantStyles = {
    primary: 'bg-primary-600 active:bg-primary-700',
    secondary: 'bg-secondary-600 active:bg-secondary-700',
    outline: 'border-2 border-primary-600 bg-transparent',
    ghost: 'bg-transparent active:bg-gray-100',
  };

  const sizeStyles = {
    sm: 'px-3 py-2',
    md: 'px-4 py-3',
    lg: 'px-6 py-4',
  };

  const textVariantStyles = {
    primary: 'text-white font-semibold',
    secondary: 'text-white font-semibold',
    outline: 'text-primary-600 font-semibold',
    ghost: 'text-gray-700 font-medium',
  };

  const textSizeStyles = {
    sm: 'text-sm',
    md: 'text-base',
    lg: 'text-lg',
  };

  const disabledStyles = disabled ? 'opacity-50' : '';

  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled || loading}
      activeOpacity={0.7}
      className={twMerge(
        baseStyles,
        variantStyles[variant],
        sizeStyles[size],
        disabledStyles,
        className
      )}
      style={style}
    >
      {loading ? (
        <ActivityIndicator 
          color={variant === 'outline' || variant === 'ghost' ? '#000' : '#fff'} 
          size="small"
        />
      ) : (
        <Text
          className={twMerge(
            textVariantStyles[variant],
            textSizeStyles[size],
            textStyle ? '' : ''
          )}
          style={textStyle}
        >
          {title}
        </Text>
      )}
    </TouchableOpacity>
  );
}
```

Create `src/components/ui/Input.tsx`:
```typescript
import { TextInput, View, Text } from 'react-native';
import { twMerge } from 'tailwind-merge';
import { useState } from 'react';

interface InputProps {
  label?: string;
  placeholder?: string;
  value: string;
  onChangeText: (text: string) => void;
  error?: string;
  secureTextEntry?: boolean;
  multiline?: boolean;
  numberOfLines?: number;
  keyboardType?: 'default' | 'email-address' | 'numeric' | 'phone-pad';
  autoCapitalize?: 'none' | 'sentences' | 'words' | 'characters';
  editable?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  className?: string;
}

export function Input({
  label,
  placeholder,
  value,
  onChangeText,
  error,
  secureTextEntry = false,
  multiline = false,
  numberOfLines = 1,
  keyboardType = 'default',
  autoCapitalize = 'sentences',
  editable = true,
  leftIcon,
  rightIcon,
  className = '',
}: InputProps) {
  const [isFocused, setIsFocused] = useState(false);

  return (
    <View className={twMerge('mb-4', className)}>
      {label && (
        <Text className="text-gray-700 font-medium mb-2 text-sm">
          {label}
        </Text>
      )}
      <View
        className={twMerge(
          'flex-row items-center bg-white border rounded-lg px-4',
          error ? 'border-red-500' : isFocused ? 'border-primary-600' : 'border-gray-300',
          multiline ? 'py-3' : 'h-12'
        )}
      >
        {leftIcon && <View className="mr-3">{leftIcon}</View>}
        <TextInput
          className="flex-1 text-base text-gray-900"
          placeholder={placeholder}
          placeholderTextColor="#9CA3AF"
          value={value}
          onChangeText={onChangeText}
          secureTextEntry={secureTextEntry}
          multiline={multiline}
          numberOfLines={numberOfLines}
          keyboardType={keyboardType}
          autoCapitalize={autoCapitalize}
          editable={editable}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          textAlignVertical={multiline ? 'top' : 'center'}
        />
        {rightIcon && <View className="ml-3">{rightIcon}</View>}
      </View>
      {error && (
        <Text className="text-red-500 text-xs mt-1 ml-1">
          {error}
        </Text>
      )}
    </View>
  );
}
```

Create `src/components/ui/Card.tsx`:
```typescript
import { View, TouchableOpacity } from 'react-native';
import { twMerge } from 'tailwind-merge';
import type { ViewStyle } from 'react-native';

interface CardProps {
  children: React.ReactNode;
  onPress?: () => void;
  className?: string;
  style?: ViewStyle;
  variant?: 'elevated' | 'outlined' | 'filled';
}

export function Card({
  children,
  onPress,
  className = '',
  style,
  variant = 'elevated',
}: CardProps) {
  const variantStyles = {
    elevated: 'bg-white shadow-sm',
    outlined: 'bg-white border border-gray-200',
    filled: 'bg-gray-50',
  };

  const Container = onPress ? TouchableOpacity : View;

  return (
    <Container
      className={twMerge(
        'rounded-xl p-4',
        variantStyles[variant],
        className
      )}
      style={style}
      onPress={onPress}
      activeOpacity={onPress ? 0.7 : 1}
    >
      {children}
    </Container>
  );
}
```

Create `src/components/ui/Avatar.tsx`:
```typescript
import { Image, View, Text } from 'react-native';
import { twMerge } from 'tailwind-merge';

interface AvatarProps {
  source?: { uri: string };
  alt?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  showFallback?: boolean;
  className?: string;
}

const sizeMap = {
  sm: { container: 'w-8 h-8', text: 'text-xs' },
  md: { container: 'w-10 h-10', text: 'text-sm' },
  lg: { container: 'w-14 h-14', text: 'text-base' },
  xl: { container: 'w-20 h-20', text: 'text-lg' },
};

export function Avatar({
  source,
  alt = '',
  size = 'md',
  showFallback = true,
  className = '',
}: AvatarProps) {
  const sizes = sizeMap[size];

  if (source) {
    return (
      <Image
        source={source}
        alt={alt}
        className={twMerge('rounded-full', sizes.container, className)}
        resizeMode="cover"
      />
    );
  }

  if (showFallback && alt) {
    return (
      <View
        className={twMerge(
          'rounded-full bg-primary-600 items-center justify-center',
          sizes.container,
          className
        )}
      >
        <Text className={twMerge('text-white font-semibold', sizes.text)}>
          {alt.charAt(0).toUpperCase()}
        </Text>
      </View>
    );
  }

  return (
    <View
      className={twMerge(
        'rounded-full bg-gray-300',
        sizes.container,
        className
      )}
    />
  );
}
```

Create `src/components/ui/index.ts`:
```typescript
export { Button } from './Button';
export { Input } from './Input';
export { Card } from './Card';
export { Avatar } from './Avatar';
export { Badge } from './Badge';
export { IconButton } from './IconButton';
export { Divider } from './Divider';
export { Skeleton } from './Skeleton';
```

### 5.4 Theme Configuration

Create `src/constants/theme.ts`:
```typescript
export const theme = {
  colors: {
    primary: {
      50: '#eff6ff',
      100: '#dbeafe',
      200: '#bfdbfe',
      300: '#93c5fd',
      400: '#60a5fa',
      500: '#3b82f6',
      600: '#2563eb',
      700: '#1d4ed8',
      800: '#1e40af',
      900: '#1e3a8a',
    },
    semantic: {
      background: '#FFFFFF',
      backgroundSecondary: '#F9FAFB',
      text: '#111827',
      textSecondary: '#6B7280',
      border: '#E5E7EB',
      error: '#EF4444',
      success: '#10B981',
      warning: '#F59E0B',
      info: '#3B82F6',
    },
  },
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32,
    '2xl': 48,
  },
  borderRadius: {
    sm: 4,
    md: 8,
    lg: 12,
    xl: 16,
    full: 9999,
  },
  fontSize: {
    xs: 12,
    sm: 14,
    md: 16,
    lg: 18,
    xl: 20,
    '2xl': 24,
    '3xl': 30,
  },
};
```

### 5.5 Dark Mode Support

Update `tailwind.config.js`:
```javascript
module.exports = {
  darkMode: 'class',
  // ... rest of config
};
```

Create hook `src/hooks/useTheme.ts`:
```typescript
import { useColorScheme } from 'react-native';
import { useAppStore } from '../store/useAppStore';

export function useTheme() {
  const systemColorScheme = useColorScheme();
  const { theme: storedTheme } = useAppStore();

  const effectiveTheme = storedTheme === 'system' 
    ? systemColorScheme 
    : storedTheme;

  return {
    theme: effectiveTheme,
    isDark: effectiveTheme === 'dark',
    setTheme: useAppStore.getState().setTheme,
  };
}
```

## Deliverables
- [ ] Styling solution configured (NativeWind/Tamagui)
- [ ] Core UI components created (Button, Input, Card, Avatar)
- [ ] Theme constants defined
- [ ] Dark mode support implemented
- [ ] Component index exports configured
- [ ] Custom Tailwind configuration complete

## Best Practices
- Use `twMerge` for conditional class merging
- Keep components composable and single-responsibility
- Implement proper accessibility labels
- Test components on both iOS and Android
- Document component props with TypeScript interfaces
- Use consistent naming conventions
- Create storybook/documentation for components
