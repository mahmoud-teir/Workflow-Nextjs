# Phase 2: Environment Setup & Project Initialization

## Overview
Set up the development environment, initialize the React Native project, and configure essential tooling.

## Key Activities

### 2.1 Development Environment
- Install Node.js (LTS version, typically 18.x or 20.x)
- Install Watchman (macOS): `brew install watchman`
- Install Ruby & CocoaPods (iOS): `sudo gem install cocoapods`
- Install Android Studio with SDK tools
- Install Xcode (iOS, macOS only)
- Configure ANDROID_HOME and other environment variables

### 2.2 Project Initialization

#### Option A: Expo (Recommended for most apps)
```bash
npx create-expo-app@latest my-app --template blank-typescript
cd my-app
```

#### Option B: React Native CLI
```bash
npx react-native@latest init MyApp --template react-native-template-typescript
cd MyApp
```

### 2.3 Essential Dependencies Installation

#### Core Libraries
```bash
# Navigation
npm install @react-navigation/native @react-navigation/stack
npm install react-native-screens react-native-safe-area-context

# State Management
npm install zustand # or @reduxjs/toolkit react-redux

# Styling
npm install nativewind tailwindcss # or tamagui

# HTTP Client
npm install axios

# Async Storage
npm install @react-native-async-storage/async-storage
```

#### Development Dependencies
```bash
npm install -D typescript @types/react @types/node
npm install -D eslint prettier eslint-config-prettier
npm install -D husky lint-staged
```

### 2.4 TypeScript Configuration
```json
{
  "compilerOptions": {
    "target": "esnext",
    "module": "commonjs",
    "lib": ["es2019"],
    "allowJs": true,
    "jsx": "react-native",
    "noEmit": true,
    "isolatedModules": true,
    "strict": true,
    "moduleResolution": "node",
    "baseUrl": "./",
    "paths": {
      "@/*": ["src/*"],
      "@components/*": ["src/components/*"],
      "@screens/*": ["src/screens/*"],
      "@hooks/*": ["src/hooks/*"],
      "@utils/*": ["src/utils/*"]
    },
    "allowSyntheticDefaultImports": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "resolveJsonModule": true
  },
  "exclude": ["node_modules", "babel.config.js", "metro.config.js"]
}
```

### 2.5 Folder Structure Setup
```
src/
├── components/     # Reusable UI components
├── screens/        # Screen components
├── navigation/     # Navigation configuration
├── store/          # State management
├── hooks/          # Custom hooks
├── utils/          # Utility functions
├── services/       # API services
├── constants/      # App constants
├── types/          # TypeScript types
└── assets/         # Images, fonts, etc.
```

### 2.6 Git Configuration
```bash
git init
echo "node_modules/\n.DS_Store\n*.log\n.env" >> .gitignore
git add .
git commit -m "Initial project setup"
```

## Deliverables
- [ ] Initialized React Native/Expo project
- [ ] TypeScript configured
- [ ] Essential dependencies installed
- [ ] Folder structure created
- [ ] Git repository initialized
- [ ] ESLint/Prettier configured

## Verification
```bash
# Run on iOS simulator
npm run ios # or npx expo run:ios

# Run on Android emulator
npm run android # or npx expo run:android

# Run type checking
npx tsc --noEmit
```
