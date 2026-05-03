# Expo Configuration Skill

## Overview
Complete guide to configuring Expo SDK 52+ for production React Native applications.

## Quick Start

### Initialize Project
```bash
npx create-expo-app@latest my-app --template blank-typescript
cd my-app
npx expo install expo-dev-client expo-config-plugins
```

### app.json Configuration
```json
{
  "expo": {
    "name": "MyApp",
    "slug": "my-app",
    "version": "1.0.0",
    "orientation": "portrait",
    "icon": "./assets/icon.png",
    "userInterfaceStyle": "automatic",
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#ffffff"
    },
    "assetBundlePatterns": ["**/*"],
    "ios": {
      "supportsTablet": true,
      "bundleIdentifier": "com.company.myapp",
      "buildNumber": "1",
      "infoPlist": {
        "NSCameraUsageDescription": "This app uses camera for scanning",
        "NSLocationWhenInUseUsageDescription": "Location needed for features"
      }
    },
    "android": {
      "adaptiveIcon": {
        "foregroundImage": "./assets/adaptive-icon.png",
        "backgroundColor": "#ffffff"
      },
      "package": "com.company.myapp",
      "versionCode": 1,
      "permissions": [
        "CAMERA",
        "ACCESS_FINE_LOCATION",
        "ACCESS_COARSE_LOCATION"
      ]
    },
    "web": {
      "favicon": "./assets/favicon.png",
      "bundler": "metro"
    },
    "plugins": [
      "expo-router",
      [
        "expo-splash-screen",
        {
          "backgroundColor": "#ffffff",
          "image": "./assets/splash.png",
          "imageWidth": 200
        }
      ]
    ],
    "extra": {
      "eas": {
        "projectId": "your-project-id"
      }
    },
    "owner": "your-username"
  }
}
```

### eas.json Build Configuration
```json
{
  "cli": {
    "version": ">= 7.0.0"
  },
  "build": {
    "development": {
      "developmentClient": true,
      "distribution": "internal",
      "env": {
        "APP_ENV": "development"
      }
    },
    "preview": {
      "distribution": "internal",
      "android": {
        "buildType": "apk"
      },
      "env": {
        "APP_ENV": "staging"
      }
    },
    "production": {
      "android": {
        "buildType": "app-bundle"
      },
      "ios": {
        "resourceClass": "m-medium"
      },
      "env": {
        "APP_ENV": "production"
      }
    }
  },
  "submit": {
    "production": {
      "ios": {
        "appleId": "your-apple-id",
        "ascAppId": "your-app-store-id"
      },
      "android": {
        "serviceAccountKeyPath": "./google-service-account.json",
        "track": "internal"
      }
    }
  }
}
```

## Config Plugins

### Custom Config Plugin Example
```javascript
// plugins/with-custom-config.js
const { withInfoPlist, withAndroidManifest } = require('@expo/config-plugins');

module.exports = function withCustomConfig(config) {
  config = withInfoPlist(config, (config) => {
    config.modResults['NSFaceIDUsageDescription'] = 
      'Allow $(PRODUCT_NAME) to use Face ID';
    return config;
  });

  config = withAndroidManifest(config, (config) => {
    const mainActivity = config.modResults.manifest.application[0].activity.find(
      (a) => a.$['android:name'] === '.MainActivity'
    );
    if (mainActivity) {
      mainActivity.$['android:launchMode'] = 'singleTask';
    }
    return config;
  });

  return config;
};
```

### Using Config Plugins
```json
{
  "expo": {
    "plugins": [
      "./plugins/with-custom-config.js",
      [
        "expo-location",
        {
          "locationAlwaysAndWhenInUsePermission": "Allow location access"
        }
      ]
    ]
  }
}
```

## Environment Variables

### babel.config.js
```javascript
module.exports = function (api) {
  api.cache(true);
  return {
    presets: ['babel-preset-expo'],
    plugins: [
      [
        'module:react-native-dotenv',
        {
          envName: 'APP_ENV',
          moduleName: '@env',
          path: '.env',
          blocklist: null,
          allowlist: null,
          safe: false,
          allowUndefined: true,
        },
      ],
    ],
  };
};
```

### .env.example
```bash
API_URL=https://api.example.com
SENTRY_DSN=https://xxx@sentry.io/xxx
FIREBASE_API_KEY=xxx
APP_ENV=development
```

## Development Client Setup

### Install Dev Client
```bash
npx expo install expo-dev-client
```

### Prebuild Command
```bash
npx expo prebuild --clean
npx expo run:ios
npx expo run:android
```

## Best Practices

1. **Version Management**: Always pin Expo SDK version in package.json
2. **Build Profiles**: Separate development, preview, and production builds
3. **Permissions**: Request only necessary permissions with clear descriptions
4. **Icons & Splash**: Use multiple resolutions for all platforms
5. **Environment Separation**: Different configs for dev/staging/prod
6. **Plugin Order**: Load plugins in correct dependency order
7. **Native Code**: Minimize custom native code, prefer config plugins

## Common Issues

### Issue: Build fails with missing entitlements
**Solution**: Add entitlements file in ios folder:
```xml
<!-- ios/yourapp.entitlements -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>aps-environment</key>
    <string>production</string>
</dict>
</plist>
```

### Issue: Android build size too large
**Solution**: Enable ProGuard and resource shrinking:
```json
{
  "android": {
    "buildConfiguration": {
      "proguard": true,
      "useLegacyPackaging": false
    }
  }
}
```

## Resources
- [Expo Documentation](https://docs.expo.dev/)
- [EAS Build Docs](https://docs.expo.dev/build/introduction/)
- [Config Plugins Guide](https://docs.expo.dev/config-plugins/introduction/)
