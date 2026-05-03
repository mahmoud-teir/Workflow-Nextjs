# Phase 8: Native Modules & Platform-Specific Code

## Overview
Integrate native functionality through Expo modules, React Native libraries, and custom native code when needed.

## Key Activities

### 8.1 Camera & Image Picker
```bash
npm install expo-image-picker expo-camera expo-media-library
```

Create `src/hooks/useCamera.ts`:
```typescript
import { useState } from 'react';
import * as ImagePicker from 'expo-image-picker';
import * as MediaLibrary from 'expo-media-library';

export function useCamera() {
  const [permission, requestPermission] = ImagePicker.useCameraPermissions();
  const [mediaPermission, requestMediaPermission] = MediaLibrary.usePermissions();

  const takePhoto = async (): Promise<string | null> => {
    if (!permission?.granted) {
      const { granted } = await requestPermission();
      if (!granted) throw new Error('Camera permission denied');
    }

    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      aspect: [4, 3],
      quality: 1,
    });

    if (!result.canceled && result.assets[0]) {
      return result.assets[0].uri;
    }
    return null;
  };

  const pickImage = async (): Promise<string | null> => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 1,
    });

    if (!result.canceled && result.assets[0]) {
      return result.assets[0].uri;
    }
    return null;
  };

  const saveToGallery = async (uri: string) => {
    if (!mediaPermission?.granted) {
      const { granted } = await requestMediaPermission();
      if (!granted) throw new Error('Media library permission denied');
    }

    await MediaLibrary.saveToLibraryAsync(uri);
  };

  return {
    permission: permission?.granted,
    mediaPermission: mediaPermission?.granted,
    takePhoto,
    pickImage,
    saveToGallery,
  };
}
```

### 8.2 Location Services
```bash
npm install expo-location
```

Create `src/hooks/useLocation.ts`:
```typescript
import { useState, useEffect } from 'react';
import * as Location from 'expo-location';

export function useLocation() {
  const [location, setLocation] = useState<Location.LocationObject | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const getCurrentLocation = async () => {
    setLoading(true);
    setError(null);

    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        setError('Location permission denied');
        return;
      }

      const currentLocation = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy.High,
      });

      setLocation(currentLocation);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const watchLocation = () => {
    Location.watchPositionAsync(
      {
        accuracy: Location.Accuracy.High,
        timeInterval: 5000,
        distanceInterval: 10,
      },
      (loc) => setLocation(loc)
    );
  };

  const getAddress = async (latitude: number, longitude: number) => {
    const address = await Location.reverseGeocodeAsync({ latitude, longitude });
    return address[0];
  };

  return {
    location,
    error,
    loading,
    getCurrentLocation,
    watchLocation,
    getAddress,
  };
}
```

### 8.3 Push Notifications
```bash
npm install expo-notifications
```

Create `src/services/notifications/PushNotificationService.ts`:
```typescript
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

export class PushNotificationService {
  static async registerForPushNotifications(): Promise<string | null> {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;

    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync();
      finalStatus = status;
    }

    if (finalStatus !== 'granted') {
      console.log('Failed to get push token for push notification!');
      return null;
    }

    const projectId =
      Constants?.expoConfig?.extra?.eas?.projectId ?? Constants?.easConfig?.projectId;
    
    if (!projectId) {
      console.log('Project ID not found');
      return null;
    }

    const token = (
      await Notifications.getExpoPushTokenAsync({
        projectId,
      })
    ).data;

    if (Platform.OS === 'android') {
      await Notifications.setNotificationChannelAsync('default', {
        name: 'default',
        importance: Notifications.AndroidImportance.MAX,
        vibrationPattern: [0, 250, 250, 250],
        lightColor: '#FF231F7C',
      });
    }

    return token;
  }

  static async scheduleLocalNotification(title: string, body: string, delaySeconds: number = 1) {
    await Notifications.scheduleNotificationAsync({
      content: {
        title,
        body,
        data: { type: 'local' },
      },
      trigger: {
        seconds: delaySeconds,
      },
    });
  }

  static async scheduleRepeatingNotification(
    title: string,
    body: string,
    hour: number,
    minute: number
  ) {
    await Notifications.scheduleNotificationAsync({
      content: {
        title,
        body,
      },
      trigger: {
        hour,
        minute,
        repeats: true,
      },
    });
  }

  static async cancelAllNotifications() {
    await Notifications.cancelAllScheduledNotificationsAsync();
  }
}
```

### 8.4 Biometric Authentication
```bash
npm install expo-local-authentication
```

Create `src/hooks/useBiometrics.ts`:
```typescript
import { useState } from 'react';
import * as LocalAuthentication from 'expo-local-authentication';

export function useBiometrics() {
  const [hasHardware, setHasHardware] = useState<boolean | null>(null);
  const [isEnrolled, setIsEnrolled] = useState<boolean | null>(null);

  const checkBiometrics = async () => {
    const hardware = await LocalAuthentication.hasHardwareAsync();
    const enrolled = await LocalAuthentication.isEnrolledAsync();
    
    setHasHardware(hardware);
    setIsEnrolled(enrolled);
    
    return { hardware, enrolled };
  };

  const authenticate = async (promptMessage: string = 'Authenticate'): Promise<boolean> => {
    try {
      const result = await LocalAuthentication.authenticateAsync({
        promptMessage,
        fallbackLabel: 'Use Passcode',
        cancelLabel: 'Cancel',
      });

      return result.success;
    } catch (error) {
      console.error('Biometric authentication failed:', error);
      return false;
    }
  };

  return {
    hasHardware,
    isEnrolled,
    checkBiometrics,
    authenticate,
  };
}
```

### 8.5 File System Access
```bash
npm install expo-file-system expo-document-picker expo-sharing
```

Create `src/hooks/useFileSystem.ts`:
```typescript
import * as FileSystem from 'expo-file-system';
import * as DocumentPicker from 'expo-document-picker';
import * as Sharing from 'expo-sharing';

export function useFileSystem() {
  const downloadFile = async (url: string, filename: string) => {
    const fileUri = FileSystem.documentDirectory + filename;
    
    const { uri } = await FileSystem.downloadAsync(url, fileUri);
    return uri;
  };

  const pickDocument = async (type: string[] = ['*/*']) => {
    const result = await DocumentPicker.getDocumentAsync({
      type,
      copyToCacheDirectory: true,
    });

    if (!result.canceled && result.assets[0]) {
      return result.assets[0];
    }
    return null;
  };

  const shareFile = async (fileUri: string, title: string = 'Share File') => {
    const canShare = await Sharing.isAvailableAsync();
    
    if (canShare) {
      await Sharing.shareAsync(fileUri, {
        dialogTitle: title,
        mimeType: 'application/octet-stream',
      });
    }
  };

  const getFileSize = async (fileUri: string): Promise<number> => {
    const info = await FileSystem.getInfoAsync(fileUri);
    return info.size || 0;
  };

  const deleteFile = async (fileUri: string): Promise<void> => {
    await FileSystem.deleteAsync(fileUri, { idempotent: true });
  };

  return {
    downloadFile,
    pickDocument,
    shareFile,
    getFileSize,
    deleteFile,
  };
}
```

## Deliverables
- [ ] Camera integration complete
- [ ] Location services configured
- [ ] Push notifications working
- [ ] Biometric auth implemented
- [ ] File system access enabled
- [ ] Platform-specific code handled

## Best Practices
- Always request permissions before using native features
- Handle permission denial gracefully
- Test on both iOS and Android
- Use Expo modules when possible
- Abstract platform differences behind hooks
- Provide fallbacks for unsupported features
