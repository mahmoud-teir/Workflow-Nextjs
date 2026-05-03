# Phase 13: App Store Preparation

## Overview
Prepare your React Native app for submission to Apple App Store and Google Play Store with all required assets, configurations, and compliance.

## App Store Requirements

### 1. Apple App Store (iOS)

#### Required Assets
```markdown
- App Icon: 1024x1024px (no transparency)
- Screenshots:
  - 6.7" (1284x2778) - iPhone 14 Pro Max
  - 6.5" (1242x2688) - iPhone XS Max
  - 5.5" (1242x2208) - iPhone 8 Plus
  - iPad Pro 12.9" (2048x2732)
- Preview Video: 30 seconds max (optional but recommended)
- Privacy Policy URL
- Support URL
- Marketing URL (optional)
```

#### App Store Connect Configuration
```javascript
// eas.json - iOS configuration
{
  "cli": {
    "version": ">= 5.0.0"
  },
  "build": {
    "production": {
      "ios": {
        "resourceClass": "m-medium",
        "image": "latest",
        "provisioningProfile": true,
        "enterpriseProvisioning": "adhoc"
      },
      "distribution": "store",
      "channel": "production"
    }
  },
  "submit": {
    "production": {
      "ios": {
        "appleId": "your-apple-id@example.com",
        "ascAppId": "your-app-store-connect-app-id",
        "appleTeamId": "your-team-id"
      }
    }
  }
}
```

#### Info.plist Customizations
```xml
<!-- app.config.js -->
export default {
  expo: {
    ios: {
      infoPlist: {
        NSCameraUsageDescription: "We need camera access to scan QR codes",
        NSPhotoLibraryUsageDescription: "We need photo library access to upload images",
        NSLocationWhenInUseUsageDescription: "We need your location to show nearby stores",
        NSFaceIDUsageDescription: "We use Face ID to secure your account",
        ITSAppUsesNonExemptEncryption: false,
        LSApplicationQueriesSchemes: ["whatsapp", "tel", "mailto"],
        UIBackgroundModes: ["remote-notification", "fetch"],
        CFBundleLocalizations: ["en", "ar", "fr"]
      }
    }
  }
};
```

### 2. Google Play Store (Android)

#### Required Assets
```markdown
- App Icon: 512x512px (PNG, no transparency)
- Feature Graphic: 1024x500px
- Screenshots:
  - Phone: 1080x1920 or 1920x1080 (minimum 2)
  - Tablet: 1920x1080 or 1080x1920 (if supporting tablets)
- Promo Video: YouTube URL (optional)
- Privacy Policy URL
- Short Description: 80 characters max
- Full Description: 4000 characters max
```

#### Google Play Configuration
```javascript
// eas.json - Android configuration
{
  "build": {
    "production": {
      "android": {
        "buildType": "app-bundle",
        "gradleCommand": ":app:bundleRelease",
        "image": "latest"
      },
      "distribution": "store",
      "channel": "production"
    }
  },
  "submit": {
    "production": {
      "android": {
        "serviceAccountKeyPath": "./google-service-account.json",
        "track": "production",
        "releaseNotes": "Bug fixes and performance improvements"
      }
    }
  }
}
```

#### AndroidManifest.xml Permissions
```xml
<!-- app.config.js -->
export default {
  expo: {
    android: {
      permissions: [
        "CAMERA",
        "READ_EXTERNAL_STORAGE",
        "WRITE_EXTERNAL_STORAGE",
        "ACCESS_FINE_LOCATION",
        "ACCESS_COARSE_LOCATION",
        "VIBRATE",
        "RECEIVE_BOOT_COMPLETED",
        "FOREGROUND_SERVICE"
      ],
      adaptiveIcon: {
        foregroundImage: "./assets/adaptive-icon.png",
        backgroundColor: "#FFFFFF"
      },
      package: "com.yourcompany.yourapp",
      versionCode: 1,
      googleServicesFile: "./google-services.json"
    }
  }
};
```

### 3. Version Management Script
```typescript
// scripts/version-management.ts
import fs from 'fs';
import path from 'path';

interface VersionConfig {
  version: string;
  buildNumber: number;
  releaseNotes: string;
}

class VersionManager {
  private configPath = path.join(process.cwd(), 'app.json');
  
  readVersion(): VersionConfig {
    const config = JSON.parse(fs.readFileSync(this.configPath, 'utf8'));
    return {
      version: config.expo.version,
      buildNumber: config.expo.android?.versionCode || 
                   config.expo.ios?.buildNumber || 1,
      releaseNotes: ''
    };
  }

  incrementBuildNumber(): void {
    const config = JSON.parse(fs.readFileSync(this.configPath, 'utf8'));
    const currentBuild = config.expo.android?.versionCode || 1;
    
    config.expo.android = {
      ...config.expo.android,
      versionCode: currentBuild + 1
    };
    
    config.expo.ios = {
      ...config.expo.ios,
      buildNumber: String(currentBuild + 1)
    };

    fs.writeFileSync(this.configPath, JSON.stringify(config, null, 2));
    console.log(`Build number incremented to ${currentBuild + 1}`);
  }

  setVersion(version: string): void {
    const config = JSON.parse(fs.readFileSync(this.configPath, 'utf8'));
    config.expo.version = version;
    fs.writeFileSync(this.configPath, JSON.stringify(config, null, 2));
    console.log(`Version set to ${version}`);
  }
}

export const versionManager = new VersionManager();
```

### 4. Store Listing Generator
```typescript
// scripts/generate-store-listings.ts
interface StoreListing {
  title: string;
  subtitle?: string;
  description: string;
  keywords: string[];
  supportUrl: string;
  privacyUrl: string;
  marketingUrl?: string;
}

const listings: Record<string, StoreListing> = {
  en: {
    title: "Your App Name",
    subtitle: "Your tagline here",
    description: `
Discover the best way to manage your tasks with Your App Name.

KEY FEATURES:
✓ Easy task management
✓ Real-time synchronization
✓ Offline support
✓ Secure authentication
✓ Beautiful interface

Download now and boost your productivity!
    `,
    keywords: ['productivity', 'tasks', 'organization', 'management'],
    supportUrl: 'https://support.yourapp.com',
    privacyUrl: 'https://yourapp.com/privacy',
    marketingUrl: 'https://yourapp.com'
  },
  ar: {
    title: "اسم تطبيقك",
    description: `
اكتشف أفضل طريقة لإدارة مهامك مع اسم تطبيقك.

الميزات الرئيسية:
✓ إدارة مهام سهلة
✓ مزامنة في الوقت الفعلي
✓ دعم غير متصل
✓ مصادقة آمنة
✓ واجهة جميلة

حمّل الآن وزد إنتاجيتك!
    `,
    keywords: ['إنتاجية', 'مهام', 'تنظيم'],
    supportUrl: 'https://support.yourapp.com/ar',
    privacyUrl: 'https://yourapp.com/ar/privacy'
  }
};

export const generateStoreListings = () => {
  // Generate localized store listings
  Object.entries(listings).forEach(([locale, listing]) => {
    console.log(`Generating ${locale} store listing...`);
    // Write to files or API
  });
};
```

### 5. Compliance Checklist

#### Apple App Store Guidelines
- [ ] No broken links or placeholder content
- [ ] All features are functional
- [ ] Privacy policy is accessible
- [ ] In-app purchases are clearly disclosed
- [ ] No mention of Android or other platforms
- [ ] Age rating is accurate
- [ ] Content meets community guidelines
- [ ] No copyrighted material without permission

#### Google Play Policies
- [ ] Target API level is up to date (API 33+)
- [ ] 64-bit support enabled
- [ ] Privacy policy is accessible
- [ ] Data safety form completed
- [ ] Content rating questionnaire completed
- [ ] No policy violations
- [ ] App signing by Google Play enabled

## Deliverables Checklist

- [ ] App icons for all required sizes
- [ ] Screenshots for all device types
- [ ] App preview video (optional)
- [ ] Privacy policy published
- [ ] Support page created
- [ ] App Store Connect account configured
- [ ] Google Play Console account configured
- [ ] EAS Build configuration complete
- [ ] Signing certificates generated
- [ ] Store listings written (all languages)
- [ ] Keywords researched and optimized
- [ ] Compliance checklists completed
- [ ] TestFlight/Internal testing set up

## Best Practices

1. **Start early** - App review can take 24-48 hours (Apple) or few hours (Google)
2. **Test thoroughly** on real devices before submission
3. **Follow platform guidelines** strictly to avoid rejection
4. **Optimize screenshots** to showcase key features
5. **Write compelling descriptions** with relevant keywords
6. **Set up beta testing** with TestFlight and Google Play Internal Testing
7. **Prepare for rejection** - have a plan to address feedback quickly
8. **Keep records** of all submissions and communications

## Next Steps
- Submit for internal/beta testing
- Gather feedback from testers
- Make final adjustments
- Submit for production review
- Monitor review status
- Prepare launch marketing materials
