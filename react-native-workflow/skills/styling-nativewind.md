# Skill: Styling with NativeWind

## Overview
Use Tailwind CSS for React Native with NativeWind for consistent, responsive styling.

## Setup

### Installation
```bash
npm install nativewind tailwindcss
npx tailwindcss init
```

### Configuration
```javascript
// tailwind.config.js
module.exports = {
  content: [
    "./App.{js,jsx,ts,tsx}",
    "./components/**/*.{js,jsx,ts,tsx}",
    "./screens/**/*.{js,jsx,ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        },
      },
    },
  },
  plugins: [],
}
```

## Usage Examples

### Basic Component
```typescript
// components/Button.tsx
import React from 'react';
import { Text, Pressable } from 'react-native';
import { styled } from 'nativewind';

const StyledButton = styled(Pressable);
const StyledText = styled(Text);

interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'outline';
  size?: 'sm' | 'md' | 'lg';
}

export const Button: React.FC<ButtonProps> = ({ 
  title, 
  onPress, 
  variant = 'primary',
  size = 'md'
}) => {
  const baseStyles = "rounded-lg items-center justify-center";
  
  const variantStyles = {
    primary: "bg-primary-600 active:bg-primary-700",
    secondary: "bg-gray-200 active:bg-gray-300",
    outline: "border-2 border-primary-600 bg-transparent"
  };

  const sizeStyles = {
    sm: "px-3 py-2",
    md: "px-4 py-3",
    lg: "px-6 py-4"
  };

  const textStyles = {
    primary: "text-white font-semibold",
    secondary: "text-gray-800 font-medium",
    outline: "text-primary-600 font-semibold"
  };

  const textSize = {
    sm: "text-sm",
    md: "text-base",
    lg: "text-lg"
  };

  return (
    <StyledButton
      className={`${baseStyles} ${variantStyles[variant]} ${sizeStyles[size]}`}
      onPress={onPress}
    >
      <StyledText className={`${textStyles[variant]} ${textSize[size]}`}>
        {title}
      </StyledText>
    </StyledButton>
  );
};
```

### Responsive Design
```typescript
// components/Card.tsx
import React from 'react';
import { View, Text, Image } from 'react-native';
import { styled } from 'nativewind';

const StyledView = styled(View);
const StyledText = styled(Text);
const StyledImage = styled(Image);

export const Card = ({ title, description, image }) => {
  return (
    <StyledView className="
      bg-white 
      rounded-xl 
      shadow-md 
      m-2 
      overflow-hidden
      flex-row
      md:flex-col
      lg:flex-row
    ">
      <StyledImage
        source={{ uri: image }}
        className="
          w-32 
          h-32 
          md:w-full 
          md:h-48 
          lg:w-32 
          lg:h-32
          resize-mode-cover
        "
      />
      <StyledView className="p-4 flex-1">
        <StyledText className="
          text-lg 
          font-bold 
          text-gray-900 
          mb-2
          md:text-xl
        ">
          {title}
        </StyledText>
        <StyledText className="
          text-gray-600 
          text-sm
          leading-relaxed
          md:text-base
        ">
          {description}
        </StyledText>
      </StyledView>
    </StyledView>
  );
};
```

### Dark Mode Support
```typescript
// App.tsx
import { useColorScheme } from 'react-native';
import { StatusBar } from 'expo-status-bar';

export default function App() {
  const colorScheme = useColorScheme();

  return (
    <View className={`flex-1 ${colorScheme === 'dark' ? 'dark bg-gray-900' : 'bg-gray-50'}`}>
      <StatusBar style={colorScheme === 'dark' ? 'light' : 'dark'} />
      
      <Text className="
        text-3xl 
        font-bold 
        text-gray-900 
        dark:text-white
        text-center 
        mt-20
      ">
        Hello World
      </Text>
      
      <View className="
        bg-white 
        dark:bg-gray-800 
        rounded-lg 
        p-6 
        m-4
        shadow-lg
      ">
        <Text className="
          text-gray-700 
          dark:text-gray-300
        ">
          This supports dark mode!
        </Text>
      </View>
    </View>
  );
}
```

## Best Practices
- Use semantic class names for maintainability
- Leverage responsive prefixes (sm:, md:, lg:)
- Implement dark mode from the start
- Create reusable component variants
- Keep custom colors in tailwind.config.js
- Use flexbox for layouts
