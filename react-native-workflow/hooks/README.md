# React Native Custom Hooks Library

This directory contains reusable custom hooks designed for React Native applications. These hooks follow best practices for performance, type safety, and reusability across the 15-phase workflow.

## Core Hooks

### 1. `useAppState.ts`
Track application state (active, background, inactive) to handle lifecycle events.

```typescript
import { useEffect, useState } from 'react';
import { AppState, AppStateStatus } from 'react-native';

export function useAppState() {
  const [appState, setAppState] = useState<AppStateStatus>(AppState.currentState);

  useEffect(() => {
    const subscription = AppState.addEventListener('change', nextAppState => {
      setAppState(nextAppState);
    });

    return () => subscription.remove();
  }, []);

  return appState;
}
```

### 2. `useOnlineStatus.ts`
Monitor network connectivity status with offline detection.

```typescript
import { useEffect, useState } from 'react';
import NetInfo from '@react-native-community/netinfo';

export function useOnlineStatus() {
  const [isConnected, setIsConnected] = useState<boolean>(true);

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener(state => {
      setIsConnected(!!state.isConnected);
    });

    // Get initial state
    NetInfo.fetch().then(state => {
      setIsConnected(!!state.isConnected);
    });

    return () => unsubscribe();
  }, []);

  return isConnected;
}
```

### 3. `useSecureStorage.ts`
Securely store and retrieve sensitive data using encrypted storage.

```typescript
import { useState, useCallback } from 'react';
import * as SecureStore from 'expo-secure-store';

export function useSecureStorage(key: string) {
  const [value, setValue] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const loadValue = useCallback(async () => {
    try {
      const storedValue = await SecureStore.getItemAsync(key);
      setValue(storedValue);
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  }, [key]);

  const setValueSecure = useCallback(async (newValue: string) => {
    try {
      await SecureStore.setItemAsync(key, newValue);
      setValue(newValue);
      setError(null);
    } catch (err) {
      setError(err as Error);
    }
  }, [key]);

  const deleteValue = useCallback(async () => {
    try {
      await SecureStore.deleteItemAsync(key);
      setValue(null);
      setError(null);
    } catch (err) {
      setError(err as Error);
    }
  }, [key]);

  useEffect(() => {
    loadValue();
  }, [loadValue]);

  return { value, loading, error, setValue: setValueSecure, deleteValue, refresh: loadValue };
}
```

### 4. `useBiometricAuth.ts`
Handle biometric authentication (Face ID, Touch ID, Fingerprint).

```typescript
import { useState, useCallback } from 'react';
import * as LocalAuthentication from 'expo-local-authentication';

export function useBiometricAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [hasHardware, setHasHardware] = useState(false);
  const [supportedTypes, setSupportedTypes] = useState<LocalAuthentication.AuthenticationType[]>([]);

  const checkHardware = useCallback(async () => {
    const hasHardware = await LocalAuthentication.hasHardwareAsync();
    const supportedTypes = await LocalAuthentication.supportedAuthenticationTypesAsync();
    setHasHardware(hasHardware);
    setSupportedTypes(supportedTypes);
    return hasHardware;
  }, []);

  const authenticate = useCallback(async (promptMessage: string = 'Authenticate') => {
    try {
      const hasHardware = await checkHardware();
      if (!hasHardware) {
        throw new Error('Biometric hardware not available');
      }

      const result = await LocalAuthentication.authenticateAsync({
        promptMessage,
        fallbackLabel: 'Use Passcode',
      });

      setIsAuthenticated(result.success);
      return result.success;
    } catch (error) {
      console.error('Biometric auth failed:', error);
      return false;
    }
  }, [checkHardware]);

  return { isAuthenticated, hasHardware, supportedTypes, authenticate, checkHardware };
}
```

### 5. `useDebounce.ts`
Debounce values for search inputs and API calls.

```typescript
import { useState, useEffect } from 'react';

export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}
```

### 6. `useImageOptimization.ts`
Optimize image loading with caching and progressive loading.

```typescript
import { useState, useEffect } from 'react';
import { Image } from 'react-native';
import * as FileSystem from 'expo-file-system';

interface UseImageOptimizationResult {
  uri: string;
  isLoading: boolean;
  error: Error | null;
  cached: boolean;
}

export function useImageOptimization(sourceUri: string, cacheKey?: string): UseImageOptimizationResult {
  const [uri, setUri] = useState(sourceUri);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [cached, setCached] = useState(false);

  useEffect(() => {
    const loadOptimizedImage = async () => {
      try {
        const cacheKeyParam = cacheKey || sourceUri;
        const cacheFilename = cacheKeyParam.replace(/[^a-zA-Z0-9]/g, '_');
        const localUri = `${FileSystem.cacheDirectory}${cacheFilename}`;

        const fileInfo = await FileSystem.getInfoAsync(localUri);
        
        if (fileInfo.exists) {
          setUri(localUri);
          setCached(true);
        } else {
          const downloadResult = await FileSystem.downloadAsync(sourceUri, localUri);
          if (downloadResult.status === 200) {
            setUri(downloadResult.uri);
            setCached(true);
          }
        }
      } catch (err) {
        setError(err as Error);
        setUri(sourceUri); // Fallback to original
      } finally {
        setIsLoading(false);
      }
    };

    loadOptimizedImage();
  }, [sourceUri, cacheKey]);

  return { uri, isLoading, error, cached };
}
```

### 7. `useKeyboard.ts`
Handle keyboard events for responsive layouts.

```typescript
import { useState, useEffect } from 'react';
import { Keyboard, KeyboardEvent, EmitterSubscription } from 'react-native';

export function useKeyboard() {
  const [keyboardVisible, setKeyboardVisible] = useState(false);
  const [keyboardHeight, setKeyboardHeight] = useState(0);

  useEffect(() => {
    const keyboardDidShowListener = Keyboard.addListener(
      'keyboardDidShow',
      (event: KeyboardEvent) => {
        setKeyboardVisible(true);
        setKeyboardHeight(event.endCoordinates.height);
      }
    );

    const keyboardDidHideListener = Keyboard.addListener(
      'keyboardDidHide',
      () => {
        setKeyboardVisible(false);
        setKeyboardHeight(0);
      }
    );

    return () => {
      keyboardDidShowListener.remove();
      keyboardDidHideListener.remove();
    };
  }, []);

  return { keyboardVisible, keyboardHeight };
}
```

### 8. `usePermissions.ts`
Manage runtime permissions for camera, location, notifications, etc.

```typescript
import { useState, useCallback } from 'react';
import { Platform } from 'react-native';
import * as Camera from 'expo-camera';
import * as Location from 'expo-location';
import * as Notifications from 'expo-notifications';

type PermissionType = 'camera' | 'location' | 'notifications' | 'microphone' | 'photos';

interface PermissionStatus {
  granted: boolean;
  denied: boolean;
  canRequest: boolean;
}

export function usePermissions() {
  const [permissions, setPermissions] = useState<Record<PermissionType, PermissionStatus>>({
    camera: { granted: false, denied: false, canRequest: true },
    location: { granted: false, denied: false, canRequest: true },
    notifications: { granted: false, denied: false, canRequest: true },
    microphone: { granted: false, denied: false, canRequest: true },
    photos: { granted: false, denied: false, canRequest: true },
  });

  const requestPermission = useCallback(async (type: PermissionType) => {
    try {
      let status;
      
      switch (type) {
        case 'camera':
          status = await Camera.requestCameraPermissionsAsync();
          break;
        case 'location':
          status = await Location.requestForegroundPermissionsAsync();
          break;
        case 'notifications':
          status = await Notifications.requestPermissionsAsync();
          break;
        case 'microphone':
          status = await Camera.requestMicrophonePermissionsAsync();
          break;
        case 'photos':
          status = await MediaLibrary.requestPermissionsAsync();
          break;
        default:
          throw new Error(`Unknown permission type: ${type}`);
      }

      setPermissions(prev => ({
        ...prev,
        [type]: {
          granted: status.granted,
          denied: !status.granted && !status.canAskAgain,
          canRequest: status.canAskAgain,
        },
      }));

      return status.granted;
    } catch (error) {
      console.error(`Failed to request ${type} permission:`, error);
      return false;
    }
  }, []);

  const checkPermission = useCallback(async (type: PermissionType) => {
    let status;
    
    switch (type) {
      case 'camera':
        status = await Camera.getCameraPermissionsAsync();
        break;
      case 'location':
        status = await Location.getForegroundPermissionsAsync();
        break;
      case 'notifications':
        status = await Notifications.getPermissionsAsync();
        break;
      case 'microphone':
        status = await Camera.getMicrophonePermissionsAsync();
        break;
      case 'photos':
        status = await MediaLibrary.getPermissionsAsync();
        break;
      default:
        return { granted: false, denied: false, canRequest: true };
    }

    return {
      granted: status.granted,
      denied: !status.granted && !status.canAskAgain,
      canRequest: status.canAskAgain,
    };
  }, []);

  return { permissions, requestPermission, checkPermission };
}
```

### 9. `useAnimation.ts`
Simplify Reanimated animations with pre-built patterns.

```typescript
import { useRef, useEffect } from 'react';
import { useSharedValue, withSpring, withTiming, cancelAnimation } from 'react-native-reanimated';

interface AnimationOptions {
  duration?: number;
  damping?: number;
  stiffness?: number;
}

export function useAnimation(initialValue: number = 0) {
  const animatedValue = useSharedValue(initialValue);

  const spring = (toValue: number, options: AnimationOptions = {}) => {
    cancelAnimation(animatedValue);
    animatedValue.value = withSpring(toValue, {
      damping: options.damping ?? 10,
      stiffness: options.stiffness ?? 100,
    });
  };

  const timing = (toValue: number, options: AnimationOptions = {}) => {
    cancelAnimation(animatedValue);
    animatedValue.value = withTiming(toValue, {
      duration: options.duration ?? 300,
    });
  };

  const reset = () => {
    cancelAnimation(animatedValue);
    animatedValue.value = initialValue;
  };

  return { animatedValue, spring, timing, reset };
}
```

### 10. `useFormValidation.ts`
Handle form validation with Zod schemas.

```typescript
import { useState, useCallback } from 'react';
import { z, ZodSchema, ZodError } from 'zod';

interface UseFormValidationResult<T> {
  values: T;
  errors: Partial<Record<keyof T, string>>;
  isValid: boolean;
  touched: Partial<Record<keyof T, boolean>>;
  setFieldValue: (field: keyof T, value: any) => void;
  validateField: (field: keyof T) => boolean;
  validateAll: () => boolean;
  resetForm: () => void;
  setValues: (newValues: T) => void;
}

export function useFormValidation<T extends Record<string, any>>(
  initialValues: T,
  schema: ZodSchema<T>
): UseFormValidationResult<T> {
  const [values, setValues] = useState<T>(initialValues);
  const [errors, setErrors] = useState<Partial<Record<keyof T, string>>>({});
  const [touched, setTouched] = useState<Partial<Record<keyof T, boolean>>>({});
  const [isValid, setIsValid] = useState(true);

  const validateField = useCallback((field: keyof T): boolean => {
    try {
      schema.parse({ [field]: values[field] });
      setErrors(prev => ({ ...prev, [field]: undefined }));
      return true;
    } catch (error) {
      if (error instanceof ZodError) {
        const errorMessage = error.errors[0]?.message;
        setErrors(prev => ({ ...prev, [field]: errorMessage }));
        return false;
      }
      return false;
    }
  }, [values, schema]);

  const validateAll = useCallback((): boolean => {
    try {
      schema.parse(values);
      setErrors({});
      setIsValid(true);
      return true;
    } catch (error) {
      if (error instanceof ZodError) {
        const fieldErrors: Partial<Record<keyof T, string>> = {};
        error.errors.forEach(err => {
          const field = err.path[0] as keyof T;
          fieldErrors[field] = err.message;
        });
        setErrors(fieldErrors);
        setIsValid(false);
        return false;
      }
      setIsValid(false);
      return false;
    }
  }, [values, schema]);

  const setFieldValue = useCallback((field: keyof T, value: any) => {
    setValues(prev => ({ ...prev, [field]: value }));
    setTouched(prev => ({ ...prev, [field]: true }));
  }, []);

  const resetForm = useCallback(() => {
    setValues(initialValues);
    setErrors({});
    setTouched({});
    setIsValid(true);
  }, [initialValues]);

  return {
    values,
    errors,
    isValid,
    touched,
    setFieldValue,
    validateField,
    validateAll,
    resetForm,
    setValues,
  };
}
```

## Usage Example

```typescript
// App.tsx
import { useOnlineStatus } from './hooks/useOnlineStatus';
import { useSecureStorage } from './hooks/useSecureStorage';
import { useBiometricAuth } from './hooks/useBiometricAuth';

function App() {
  const isConnected = useOnlineStatus();
  const { value: authToken, setValue: setAuthToken } = useSecureStorage('auth_token');
  const { isAuthenticated, authenticate } = useBiometricAuth();

  if (!isConnected) {
    return <OfflineScreen />;
  }

  if (!authToken) {
    return <LoginScreen onSaveToken={setAuthToken} />;
  }

  if (!isAuthenticated) {
    return <BiometricPrompt onAuthenticate={authenticate} />;
  }

  return <MainApp />;
}
```

## Best Practices

1. **Type Safety**: All hooks are fully typed with TypeScript
2. **Error Handling**: Comprehensive error handling in each hook
3. **Performance**: Optimized re-renders using useCallback and useMemo
4. **Cleanup**: Proper cleanup of subscriptions and listeners in useEffect
5. **Platform Awareness**: Handle platform-specific differences where needed
6. **Testing**: Each hook should have corresponding unit tests

## Adding New Hooks

When adding new hooks:
1. Follow the naming convention: `use<PascalCaseName>.ts`
2. Include comprehensive JSDoc comments
3. Provide usage examples
4. Add TypeScript types for all parameters and return values
5. Handle edge cases and error scenarios
6. Test on both iOS and Android platforms
