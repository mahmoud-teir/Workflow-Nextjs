---
name: stitch-design
description: Analyze Stitch mobile app designs and synthesize a semantic design system into DESIGN.md files for React Native development
allowed-tools:
  - "stitch*:*"
  - "Read"
  - "Write"
  - "web_fetch"
---

# Stitch DESIGN.md Skill for React Native

You are an expert Design Systems Lead specializing in mobile app design. Your goal is to analyze Stitch mobile app designs and synthesize a "Semantic Design System" into a file named `DESIGN.md` that can be used to prompt Stitch for generating new React Native screens.

## Overview

This skill helps you create `DESIGN.md` files that serve as the "source of truth" for prompting Stitch to generate new React Native screens that align perfectly with existing design language. Stitch interprets design through "Visual Descriptions" supported by specific color values, typography, and mobile-specific patterns.

## Prerequisites

- Access to the Stitch MCP Server
- A Stitch project with at least one mobile app screen designed
- Access to the Stitch Effective Prompting Guide: https://stitch.withgoogle.com/docs/learn/prompting/
- Understanding of React Native design patterns (iOS Human Interface Guidelines, Material Design)

## The Goal

The `DESIGN.md` file will serve as the "source of truth" for prompting Stitch to generate new React Native screens that align perfectly with the existing mobile design language. This includes platform-specific considerations for iOS and Android.

## Retrieval and Networking

To analyze a Stitch mobile app project, you must retrieve screen metadata and design assets using the Stitch MCP Server tools:

1. **Namespace discovery**: Run `list_tools` to find the Stitch MCP prefix. Use this prefix (e.g., `mcp_stitch:`) for all subsequent calls.

2. **Project lookup** (if Project ID is not provided):
   - Call `[prefix]:list_projects` with `filter: "view=owned"` to retrieve all user projects
   - Identify the target mobile app project by title or URL pattern
   - Extract the Project ID from the `name` field (e.g., `projects/13534454087919359824`)

3. **Screen lookup** (if Screen ID is not provided):
   - Call `[prefix]:list_screens` with the `projectId` (just the numeric ID, not the full path)
   - Review screen titles to identify the target mobile screen (e.g., "Home", "Login", "Profile")
   - Extract the Screen ID from the screen's `name` field

4. **Metadata fetch**:
   - Call `[prefix]:get_screen` with both `projectId` and `screenId` (both as numeric IDs only)
   - This returns the complete mobile screen object including:
     - `screenshot.downloadUrl` - Visual reference of the mobile design
     - `htmlCode.downloadUrl` - Full HTML/CSS source code for analysis
     - `width`, `height`, `deviceType` - Screen dimensions (typically MOBILE)
     - Project metadata including `designTheme` with color and style information

5. **Asset download**:
   - Use `web_fetch` or `read_url_content` to download the HTML code from `htmlCode.downloadUrl`
   - Optionally download the screenshot from `screenshot.downloadUrl` for visual reference
   - Parse the HTML to extract Tailwind classes, custom CSS, and mobile component patterns

6. **Project metadata extraction**:
   - Call `[prefix]:get_project` with the project `name` (full path: `projects/{id}`) to get:
     - `designTheme` object with color mode, fonts, roundness, custom colors
     - Project-level design guidelines and descriptions
     - Device type preferences (MOBILE, TABLET) and layout principles

## Analysis & Synthesis Instructions

### 1. Extract Project Identity (JSON)
- Locate the Mobile App Title
- Locate the specific Project ID (e.g., from the `name` field in the JSON)
- Identify the target platform(s): iOS, Android, or cross-platform

### 2. Define the Atmosphere (Image/HTML)
Evaluate the mobile screenshot and HTML structure to capture the overall "vibe." Use evocative adjectives to describe the mood (e.g., "Airy," "Dense," "Minimalist," "Utilitarian"). Consider mobile-specific aspects like touch-friendly spacing and thumb-zone optimization.

### 3. Map the Color Palette (Tailwind Config/JSON)
Identify the key colors in the mobile design system. For each color, provide:
- A descriptive, natural language name that conveys its character (e.g., "Deep Muted Teal-Navy")
- The specific hex code in parentheses for precision (e.g., "#294056")
- Its specific functional role in mobile UI (e.g., "Used for primary action buttons", "Background for dark mode")
- Platform-specific variations if applicable (iOS vs Android)

### 4. Translate Geometry & Shape (CSS/Tailwind)
Convert technical `border-radius` and layout values into physical descriptions for mobile components:
- Describe `rounded-full` as "Pill-shaped buttons and avatars"
- Describe `rounded-xl` or `rounded-2xl` as "Generously rounded cards and containers (mobile-friendly)"
- Describe `rounded-lg` as "Moderately rounded corners for inputs and buttons"
- Note any platform-specific differences (iOS typically uses larger radii)

### 5. Describe Depth & Elevation
Explain how the mobile UI handles layers. Describe the presence and quality of shadows:
- "Flat design with no shadows (Material You influence)"
- "Whisper-soft diffused shadows for card elevation"
- "Heavy, high-contrast drop shadows for modal overlays"
- Note iOS-specific translucency effects or Android-specific elevation

### 6. Mobile-Specific Patterns
Analyze and document:
- **Navigation patterns**: Tab bars, bottom navigation, hamburger menus, gesture-based navigation
- **Touch targets**: Minimum sizes (44x44pt iOS, 48dp Android), spacing between interactive elements
- **Safe areas**: Handling of notches, status bars, home indicators
- **Platform adaptations**: iOS-specific vs Android-specific design choices
- **Responsive behavior**: How layouts adapt to different screen sizes and orientations

## Output Guidelines

- **Language:** Use descriptive design terminology and natural language exclusively
- **Format:** Generate a clean Markdown file following the structure below
- **Precision:** Include exact hex codes for colors while using descriptive names
- **Context:** Explain the "why" behind mobile design decisions, not just the "what"
- **Platform awareness:** Note iOS vs Android differences where applicable

## Output Format (DESIGN.md Structure for React Native)

```markdown
# Design System: [Mobile App Title]
**Project ID:** [Insert Project ID Here]
**Target Platforms:** iOS / Android / Cross-platform

## 1. Visual Theme & Atmosphere
(Description of the mood, density, and aesthetic philosophy for mobile experience.)

## 2. Color Palette & Roles
(List colors by Descriptive Name + Hex Code + Functional Role in mobile context.)
- Primary Action Color: #XXXXXX (Used for CTAs, active states)
- Background Color: #XXXXXX (Main app background, light/dark mode variants)
- Surface Color: #XXXXXX (Cards, modals, elevated surfaces)
- Text Colors: #XXXXXX (Primary, secondary, disabled states)
- Semantic Colors: #XXXXXX (Success, warning, error, info)

## 3. Typography Rules
(Description of font family, weight usage for headers vs. body, letter-spacing, and mobile-specific sizing.)
- Font Family: [Name] (System fonts: SF Pro for iOS, Roboto for Android, or custom)
- Heading Scale: H1-H6 with point sizes optimized for mobile readability
- Body Text: Base size (minimum 16pt for accessibility)
- Touch-friendly line heights and spacing

## 4. Component Stylings
* **Buttons:** (Shape description, color assignment, min touch target size, loading states)
* **Cards/Containers:** (Corner roundness, background color, shadow depth, padding)
* **Inputs/Forms:** (Stroke style, background, focus states, keyboard types)
* **Navigation:** (Tab bar style, bottom vs top placement, active indicators)
* **Lists:** (Row height, separator styles, swipe actions)
* **Modals/Sheets:** (Presentation style, backdrop, dismissal gestures)

## 5. Layout Principles
(Description of whitespace strategy, margins, padding, and grid alignment for mobile.)
- Safe Area Insets: Handling of notches, status bars, home indicators
- Thumb Zone Optimization: Placement of primary actions within easy reach
- Responsive Breakpoints: Phone vs tablet adaptations
- Spacing Scale: Consistent 4pt or 8pt grid system

## 6. Platform-Specific Adaptations
* **iOS:** (SF Symbols, iOS-specific gestures, haptic feedback patterns)
* **Android:** (Material Design 3 components, back button behavior, ripple effects)

## 7. Interaction Patterns
* **Gestures:** (Swipe, pull-to-refresh, long press, pinch)
* **Animations:** (Transition styles, micro-interactions, loading animations)
* **Haptics:** (Feedback patterns for different interactions)

## 8. Accessibility Considerations
* Dynamic Type support
* Color contrast ratios (WCAG AA/AAA compliance)
* VoiceOver/TalkBack labels and hints
* Reduced motion preferences
```

## Usage Example

To use this skill for a React Native fitness app project:

1. **Retrieve project information:**
   ```
   Use the Stitch MCP Server to get the Fitness Tracker mobile app project
   ```

2. **Get the Home screen details:**
   ```
   Retrieve the Home screen's code, image, and screen object information
   ```

3. **Reference best practices:**
   ```
   Review the Stitch Effective Prompting Guide at:
   https://stitch.withgoogle.com/docs/learn/prompting/
   Also reference:
   - Apple Human Interface Guidelines: https://developer.apple.com/design/
   - Material Design 3: https://m3.material.io/
   ```

4. **Analyze and synthesize:**
   - Extract all relevant mobile design tokens from the screen
   - Translate technical values into descriptive language
   - Identify platform-specific patterns (iOS vs Android)
   - Organize information according to the DESIGN.md structure

5. **Generate the file:**
   - Create `DESIGN.md` in the project directory (e.g., `.stitch/DESIGN.md`)
   - Follow the prescribed format exactly
   - Ensure all color codes are accurate
   - Use evocative, designer-friendly language
   - Include mobile-specific considerations

## Integration with React Native Workflow

This `DESIGN.md` file integrates with your React Native development workflow:

1. **Stitch Loop Integration**: Use with the `stitch-loop` skill to iteratively generate new screens
2. **Component Library**: Reference when building React Native components with NativeWind, Tamagui, or styled-components
3. **Design Tokens**: Convert to TypeScript constants or theme objects for consistent styling
4. **AI-Assisted Development**: Use as context for AI agents generating React Native code

## Best Practices

- ✅ Always include the Project ID for reference and future iterations
- ✅ Use descriptive color names alongside hex codes for better AI prompting
- ✅ Document both light and dark mode variations
- ✅ Specify minimum touch target sizes for accessibility
- ✅ Note platform-specific behaviors and adaptations
- ✅ Include interaction patterns and animation details
- ✅ Keep the document updated as the design evolves

## Common Pitfalls

- ❌ Focusing only on visual design without considering mobile UX patterns
- ❌ Ignoring platform-specific guidelines (iOS HIG, Material Design)
- ❌ Not documenting touch target sizes and accessibility requirements
- ❌ Missing dark mode specifications
- ❌ Overlooking safe area handling for modern devices
- ❌ Not specifying responsive behavior for tablets and foldables

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Design doesn't match generated screens | Ensure DESIGN.md includes specific hex codes and descriptive language |
| Platform inconsistencies | Document iOS and Android variations separately in Section 6 |
| Accessibility issues | Add explicit touch target sizes and contrast ratios in Section 8 |
| Stitch generates desktop-like layouts | Emphasize mobile-first constraints in the atmosphere description |

## Related Skills

- **stitch-loop**: Use this DESIGN.md with the baton system for iterative screen generation
- **react-components**: Generate React Native components based on this design system
- **shadcn-ui**: Adapt web-based shadcn/ui patterns for React Native using this design language
