# Phase 11: Security Hardening

## Overview
Implement comprehensive security measures to protect user data, prevent reverse engineering, and ensure secure communication.

## Key Security Areas

### 1. Secure Storage
```typescript
// services/secureStorage.ts
import * as ExpoSecureStore from 'expo-secure-store';
import AsyncStorage from '@react-native-async-storage/async-storage';

const SecureStorage = {
  async setItem(key: string, value: string): Promise<void> {
    await ExpoSecureStore.setItemAsync(key, value);
  },

  async getItem(key: string): Promise<string | null> {
    return await ExpoSecureStore.getItemAsync(key);
  },

  async deleteItem(key: string): Promise<void> {
    await ExpoSecureStore.deleteItemAsync(key);
  },

  // For non-sensitive data
  async setCachedData(key: string, value: string): Promise<void> {
    await AsyncStorage.setItem(key, value);
  },

  async getCachedData(key: string): Promise<string | null> {
    return await AsyncStorage.getItem(key);
  }
};

export default SecureStorage;
```

### 2. Certificate Pinning
```typescript
// utils/certificatePinning.ts
import axios from 'axios';

const SSL_PINNING_CONFIG = {
  allowedHosts: ['api.yourapp.com'],
  certificateHashes: [
    'sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA='
  ]
};

export const createSecureClient = () => {
  const client = axios.create({
    baseURL: 'https://api.yourapp.com',
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json'
    }
  });

  // Implement certificate validation in production
  client.interceptors.response.use(
    response => response,
    error => {
      if (error.code === 'CERTIFICATE_VERIFY_FAILED') {
        console.error('Certificate verification failed');
        throw new Error('Security violation detected');
      }
      throw error;
    }
  );

  return client;
};
```

### 3. Obfuscation & Anti-Tampering
```javascript
// babel.config.js for obfuscation
module.exports = {
  presets: ['module:@react-native/babel-preset'],
  plugins: [
    'transform-remove-console',
    [
      'babel-plugin-transform-inline-environment-variables',
      {
        include: ['API_URL', 'APP_ENV']
      }
    ]
  ]
};
```

```typescript
// utils/integrityCheck.ts
import DeviceInfo from 'react-native-device-info';
import { Platform } from 'react-native';

export const checkAppIntegrity = (): boolean => {
  // Check if app is running in emulator
  const isEmulator = DeviceInfo.isEmulator();
  
  // Check if device is rooted/jailbroken
  const isRooted = DeviceInfo.isRooted();
  
  // Check for debugging
  const isDebugging = __DEV__;

  if (isEmulator && !__DEV__) {
    console.warn('App running on emulator in production');
    return false;
  }

  if (isRooted && Platform.OS === 'android') {
    console.warn('App running on rooted device');
    return false;
  }

  return true;
};
```

### 4. Biometric Authentication Integration
```typescript
// hooks/useBiometricAuth.ts
import * as LocalAuthentication from 'expo-local-authentication';
import { useCallback } from 'react';

export const useBiometricAuth = () => {
  const authenticate = useCallback(async (): Promise<boolean> => {
    const hasHardware = await LocalAuthentication.hasHardwareAsync();
    const isEnrolled = await LocalAuthentication.isEnrolledAsync();

    if (!hasHardware || !isEnrolled) {
      return false;
    }

    const result = await LocalAuthentication.authenticateAsync({
      promptMessage: 'Authenticate to continue',
      fallbackLabel: 'Use Passcode',
      cancelLabel: 'Cancel',
      disableDeviceFallback: false
    });

    return result.success;
  }, []);

  const getBiometryType = useCallback(async () => {
    const types = await LocalAuthentication.supportedAuthenticationTypesAsync();
    return types;
  }, []);

  return { authenticate, getBiometryType };
};
```

### 5. Secure API Communication
```typescript
// services/apiSecurity.ts
import { createSecureClient } from './certificatePinning';
import SecureStorage from './secureStorage';

class SecureAPIService {
  private client;

  constructor() {
    this.client = createSecureClient();
    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - add auth token
    this.client.interceptors.request.use(
      async config => {
        const token = await SecureStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        
        // Add request timestamp to prevent replay attacks
        config.headers['X-Request-Time'] = Date.now().toString();
        
        return config;
      },
      error => Promise.reject(error)
    );

    // Response interceptor - handle security errors
    this.client.interceptors.response.use(
      response => response,
      error => {
        if (error.response?.status === 401) {
          // Clear sensitive data on unauthorized
          SecureStorage.deleteItem('authToken');
          SecureStorage.deleteItem('refreshToken');
        }
        return Promise.reject(error);
      }
    );
  }

  async get<T>(url: string): Promise<T> {
    const response = await this.client.get<T>(url);
    return response.data;
  }

  async post<T>(url: string, data: any): Promise<T> {
    const response = await this.client.post<T>(url, data);
    return response.data;
  }
}

export const secureAPI = new SecureAPIService();
```

### 6. Environment Variable Protection
```typescript
// config/env.ts
import Constants from 'expo-constants';

const ENV = {
  development: {
    API_URL: Constants.expoConfig?.extra?.devApiUrl,
    APP_ENV: 'development'
  },
  staging: {
    API_URL: Constants.expoConfig?.extra?.stagingApiUrl,
    APP_ENV: 'staging'
  },
  production: {
    API_URL: Constants.expoConfig?.extra?.prodApiUrl,
    APP_ENV: 'production'
  }
};

const getEnv = () => {
  if (__DEV__) {
    return ENV.development;
  }
  return ENV.production; // Default to production in builds
};

export const config = getEnv();
```

## Deliverables Checklist

- [ ] Secure storage implementation with Expo SecureStore
- [ ] Certificate pinning configuration
- [ ] Code obfuscation setup
- [ ] Anti-tampering checks (emulator, root detection)
- [ ] Biometric authentication integration
- [ ] Secure API client with token management
- [ ] Environment variable protection
- [ ] Security audit documentation
- [ ] Penetration testing plan

## Best Practices

1. **Never store sensitive data in AsyncStorage** - Use Expo SecureStore
2. **Implement certificate pinning** for all API communications
3. **Obfuscate production code** to prevent reverse engineering
4. **Validate app integrity** before allowing sensitive operations
5. **Use short-lived tokens** with refresh token rotation
6. **Implement rate limiting** on the client side
7. **Clear sensitive data** on logout or session expiry
8. **Regular security audits** and dependency updates

## Next Steps
- Conduct security penetration testing
- Set up automated security scanning in CI/CD
- Prepare security documentation for app store submission
