---
name: stitch-loop
description: Teaches agents to iteratively build React Native mobile app screens using Stitch with an autonomous baton-passing loop pattern
allowed-tools:
  - "stitch*:*"
  - "chrome*:*"
  - "Read"
  - "Write"
  - "Bash"
---

# Stitch Build Loop for React Native

You are an **autonomous mobile app builder** participating in an iterative app-building loop. Your goal is to generate mobile screens using Stitch, integrate them into the React Native project structure, and prepare instructions for the next iteration.

## Overview

The Build Loop pattern enables continuous, autonomous React Native mobile app development through a "baton" system. Each iteration:
1. Reads the current task from a baton file (`.stitch/next-prompt.md`)
2. Generates a mobile screen using Stitch MCP tools
3. Integrates the screen into the React Native project structure
4. Writes the next task to the baton file for the next iteration

## Prerequisites

**Required:**
- Access to the Stitch MCP Server
- A Stitch project (existing or will be created)
- A `.stitch/DESIGN.md` file (generate one using the `stitch-design` skill if needed)
- A `.stitch/SITE.md` file documenting the app vision and roadmap

**Optional:**
- Chrome DevTools MCP Server — enables visual verification of generated screens
- React Native development environment (Expo CLI or React Native CLI)

## The Baton System

The `.stitch/next-prompt.md` file acts as a relay baton between iterations:

```markdown
---
screen: login
---
A login screen with email/password fields, social auth buttons, and forgot password link.

**DESIGN SYSTEM (REQUIRED):**
[Copy from .stitch/DESIGN.md Section 6]

**Screen Structure:**
1. App logo at top
2. Email input field with validation
3. Password input field with show/hide toggle
4. Login button (primary CTA)
5. Social login buttons (Google, Apple)
6. Forgot password link
7. Sign up link for new users
```

**Critical rules:**
- The `screen` field in YAML frontmatter determines the output filename
- The prompt content must include the design system block from `.stitch/DESIGN.md`
- You MUST update this file before completing your work to continue the loop
- Specify mobile-first constraints (touch targets, safe areas, etc.)

## Execution Protocol

### Step 1: Read the Baton

Parse `.stitch/next-prompt.md` to extract:
- **Screen name** from the `screen` frontmatter field
- **Prompt content** from the markdown body

### Step 2: Consult Context Files

Before generating, read these files:

| File | Purpose |
|------|---------|
| `.stitch/SITE.md` | App vision, **Stitch Project ID**, existing screens (sitemap), roadmap |
| `.stitch/DESIGN.md` | Required visual style for Stitch prompts |
| `.stitch/metadata.json` | Persisted Stitch project and screen IDs |

**Important checks:**
- Section 4 (Sitemap) — Do NOT recreate screens that already exist
- Section 5 (Roadmap) — Pick tasks from here if backlog exists
- Section 6 (Creative Freedom) — Ideas for new screens if roadmap is empty

### Step 3: Generate with Stitch

Use the Stitch MCP tools to generate the mobile screen:

1. **Discover namespace**: Run `list_tools` to find the Stitch MCP prefix
2. **Get or create project**:
   - If `.stitch/metadata.json` exists, use the `projectId` from it
   - Otherwise, call `[prefix]:create_project` with `deviceType: "MOBILE"`, then call `[prefix]:get_project` to retrieve full project details, and save them to `.stitch/metadata.json` (see schema below)
   - After generating each screen, call `[prefix]:get_project` again and update the `screens` map in `.stitch/metadata.json` with each screen's full metadata (id, sourceScreen, dimensions, canvas position)
3. **Generate screen**: Call `[prefix]:generate_screen_from_text` with:
   - `projectId`: The project ID
   - `prompt`: The full prompt from the baton (including design system block)
   - `deviceType`: `MOBILE` (critical for mobile-first layouts)
4. **Retrieve assets**: Before downloading, check if `.stitch/designs/{screen}.html` and `.stitch/designs/{screen}.png` already exist:
   - **If files exist**: Ask the user whether to refresh the designs from the Stitch project or reuse the existing local files. Only re-download if the user confirms.
   - **If files do not exist**: Proceed with download:
     - `htmlCode.downloadUrl` — Download and save as `.stitch/designs/{screen}.html`
     - `screenshot.downloadUrl` — Append `=w{width}` to the URL before downloading, where `{width}` is the `width` value from the screen metadata (Google CDN serves low-res thumbnails by default). Save as `.stitch/designs/{screen}.png`

### Step 4: Integrate into React Native Project

1. **Create component structure**:
   - Convert HTML to React Native components (JSX/TSX)
   - Move from `.stitch/designs/{screen}.html` to `app/(tabs)/{screen}.tsx` or `app/{screen}.tsx`
   - Use NativeWind/Tailwind classes or Tamagui props based on your styling strategy

2. **Fix asset paths**:
   - Convert web image paths to React Native imports
   - Use `require()` or import statements for local assets
   - Handle remote images with proper caching strategies

3. **Update navigation**:
   - Add the new screen to your React Navigation or Expo Router configuration
   - Update tab bar or drawer navigation if applicable
   - Ensure type-safe route params are defined

4. **Ensure consistency**:
   - Apply shared header/footer components
   - Use theme colors from your design tokens
   - Implement platform-specific adaptations (iOS/Android)

### Step 4.5: Visual Verification (Optional)

If the **Chrome DevTools MCP Server** is available, verify the generated screen:

1. **Check availability**: Run `list_tools` to see if `chrome*` tools are present
2. **Start dev server**: Use Bash to start Expo dev server (e.g., `npx expo start`)
3. **Navigate to screen**: Call `[chrome_prefix]:navigate` to open the Expo web preview or simulator URL
4. **Capture screenshot**: Call `[chrome_prefix]:screenshot` to capture the rendered screen
5. **Visual comparison**: Compare against the Stitch screenshot (`.stitch/designs/{screen}.png`) for fidelity
6. **Stop server**: Terminate the dev server process

> **Note:** This step is optional. If Chrome DevTools MCP is not installed, skip to Step 5.

### Step 5: Update App Documentation

Modify `.stitch/SITE.md`:
- Add the new screen to Section 4 (Sitemap) with `[x]`
- Remove any idea you consumed from Section 6 (Creative Freedom)
- Update Section 5 (Roadmap) if you completed a backlog item

### Step 6: Prepare the Next Baton (Critical)

**You MUST update `.stitch/next-prompt.md` before completing.** This keeps the loop alive.

1. **Decide the next screen**:
   - Check `.stitch/SITE.md` Section 5 (Roadmap) for pending items
   - If empty, pick from Section 6 (Creative Freedom)
   - Or invent something new that fits the app vision
2. **Write the baton** with proper YAML frontmatter:

```markdown
---
screen: profile
---
User profile screen showing avatar, username, stats, and settings access.

**DESIGN SYSTEM (REQUIRED):**
[Copy the entire design system block from .stitch/DESIGN.md]

**Screen Structure:**
1. Profile header with avatar and cover photo
2. User info section (name, bio, join date)
3. Stats grid (posts, followers, following)
4. Action buttons (Edit Profile, Share)
5. Settings link
6. Recent activity list
```

## File Structure Reference

```
react-native-app/
├── .stitch/
│   ├── metadata.json   # Stitch project & screen IDs (persist this!)
│   ├── DESIGN.md       # Visual design system (from stitch-design skill)
│   ├── SITE.md         # App vision, sitemap, roadmap
│   ├── next-prompt.md  # The baton — current task
│   └── designs/        # Staging area for Stitch output
│       ├── {screen}.html
│       └── {screen}.png
├── app/                # Expo Router app directory
│   ├── (tabs)/
│   │   ├── index.tsx
│   │   ├── explore.tsx
│   │   └── {screen}.tsx
│   └── _layout.tsx
└── components/         # Reusable components
    ├── ui/
    └── features/
```

### `.stitch/metadata.json` Schema

This file persists all Stitch identifiers so future iterations can reference them for edits or variants. Populate it by calling `[prefix]:get_project` after creating a project or generating screens.

```json
{
  "name": "projects/6139132077804554844",
  "projectId": "6139132077804554844",
  "title": "My Mobile App",
  "visibility": "PRIVATE",
  "createTime": "2026-03-04T23:11:25.514932Z",
  "updateTime": "2026-03-04T23:34:40.400007Z",
  "projectType": "PROJECT_DESIGN",
  "origin": "STITCH",
  "deviceType": "MOBILE",
  "designTheme": {
    "colorMode": "DARK",
    "font": "INTER",
    "roundness": "ROUND_EIGHT",
    "customColor": "#40baf7",
    "saturation": 3
  },
  "screens": {
    "index": {
      "id": "d7237c7d78f44befa4f60afb17c818c1",
      "sourceScreen": "projects/6139132077804554844/screens/d7237c7d78f44befa4f60afb17c818c1",
      "x": 0,
      "y": 0,
      "width": 390,
      "height": 844
    },
    "login": {
      "id": "bf6a3fe5c75348e58cf21fc7a9ddeafb",
      "sourceScreen": "projects/6139132077804554844/screens/bf6a3fe5c75348e58cf21fc7a9ddeafb",
      "x": 549,
      "y": 0,
      "width": 390,
      "height": 844
    }
  },
  "metadata": {
    "userRole": "OWNER"
  }
}
```

| Field | Description |
|-------|-------------|
| `name` | Full resource name (`projects/{id}`) |
| `projectId` | Stitch project ID (from `create_project` or `get_project`) |
| `title` | Human-readable app title |
| `designTheme` | Design system tokens: color mode, font, roundness, custom color, saturation |
| `deviceType` | Target device: `MOBILE` (critical for mobile-first generation) |
| `screens` | Map of screen name → screen object. Each screen includes `id`, `sourceScreen` (resource path for MCP calls), canvas position (`x`, `y`), and dimensions (`width`, `height`) |
| `metadata.userRole` | User's role on the project (`OWNER`, `EDITOR`, `VIEWER`) |

## Orchestration Options

The loop can be driven by different orchestration layers:

| Method | How it works |
|--------|--------------|
| **CI/CD** | GitHub Actions triggers on `.stitch/next-prompt.md` changes |
| **Human-in-loop** | Developer reviews each iteration before continuing |
| **Agent chains** | One agent dispatches to another (e.g., Jules API) |
| **Manual** | Developer runs the agent repeatedly with the same repo |

The skill is orchestration-agnostic — focus on the pattern, not the trigger mechanism.

## Design System Integration

This skill works best with the `stitch-design` skill:

1. **First time setup**: Generate `.stitch/DESIGN.md` using the `stitch-design` skill from an existing Stitch screen
2. **Every iteration**: Copy Section 6 ("Design System Notes for Stitch Generation") into your baton prompt
3. **Consistency**: All generated screens will share the same visual language and mobile patterns

## Mobile-Specific Considerations

When generating and integrating screens, always consider:

### Touch Targets
- Minimum size: 44x44 points (iOS), 48x48 dp (Android)
- Adequate spacing between interactive elements (8pt minimum)
- Clear visual feedback for pressed states

### Safe Areas
- Handle notches, status bars, and home indicators
- Use `SafeAreaView` or equivalent padding
- Test on various device sizes (iPhone SE to iPhone Pro Max, Android phones, tablets)

### Platform Adaptations
- iOS: SF Symbols, native gestures, haptic feedback
- Android: Material Design ripples, back button behavior
- Cross-platform: Consistent experience with platform-appropriate feel

### Performance
- Optimize image loading and caching
- Implement lazy loading for long lists
- Use React.memo and useMemo for expensive computations

### Accessibility
- Proper labels for screen readers (VoiceOver/TalkBack)
- Sufficient color contrast (WCAG AA minimum)
- Support for dynamic type (adjustable text sizes)
- Reduced motion preferences

## Common Pitfalls

- ❌ Forgetting to update `.stitch/next-prompt.md` (breaks the loop)
- ❌ Recreating a screen that already exists in the sitemap
- ❌ Not including the design system block from `.stitch/DESIGN.md` in the prompt
- ❌ Generating desktop layouts instead of mobile-first (forget `deviceType: "MOBILE"`)
- ❌ Leaving placeholder links (`href="#"`) instead of wiring real navigation
- ❌ Forgetting to persist `.stitch/metadata.json` after creating a new project
- ❌ Ignoring touch target sizes and accessibility requirements
- ❌ Not handling safe areas for modern devices

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Stitch generates desktop-like layouts | Ensure `deviceType: "MOBILE"` is set and prompt emphasizes mobile constraints |
| Inconsistent styles across screens | Verify `.stitch/DESIGN.md` is up-to-date and copied correctly into each baton |
| Loop stalls | Check that `.stitch/next-prompt.md` was updated with valid frontmatter and screen name |
| Navigation broken | Ensure Expo Router or React Navigation is properly configured with type-safe routes |
| Touch targets too small | Add explicit minimum size requirements in the prompt (44x44pt / 48x48dp) |
| Safe areas not respected | Include safe area handling instructions in the screen structure section |

## Example Workflow

Here's a complete example of building a fitness tracking app:

### Initial Setup
```bash
# Create .stitch directory structure
mkdir -p .stitch/designs

# Generate DESIGN.md using stitch-design skill
# (Analyze initial home screen design)

# Create SITE.md with app vision
cat > .stitch/SITE.md << 'EOF'
# Fitness Tracker App Vision

## 1. Overview
A mobile app for tracking workouts, nutrition, and progress.

## 4. Sitemap
- [ ] Home (dashboard)
- [ ] Workouts
- [ ] Nutrition
- [ ] Progress
- [ ] Profile
- [ ] Settings

## 5. Roadmap
1. Authentication flow (login, signup, forgot password)
2. Core tracking features
3. Social features

## 6. Creative Freedom
- Achievement badges
- Workout challenges
- Recipe library
EOF

# Create initial baton
cat > .stitch/next-prompt.md << 'EOF'
---
screen: login
---
Login screen for fitness app with email/password and social auth.

**DESIGN SYSTEM (REQUIRED):**
[Copy from .stitch/DESIGN.md]
EOF
```

### Iteration 1: Login Screen
1. Agent reads baton, generates login screen with Stitch
2. Converts HTML to React Native (app/auth/login.tsx)
3. Adds route to Expo Router configuration
4. Updates sitemap in SITE.md
5. Creates next baton for signup screen

### Iteration 2: Signup Screen
...and so on

## Related Skills

- **stitch-design**: Generate the DESIGN.md file required for consistent screen generation
- **react-components**: Convert generated HTML to reusable React Native components
- **typescript-patterns**: Ensure type-safe navigation and props in generated screens
