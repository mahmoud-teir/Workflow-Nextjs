<a name="phase-m0b"></a>
# 📌 MOBILE PHASE M0B: HIFI PROTOTYPE (UI Designer)

> **Context:** This phase leverages the Google Stitch MCP server to generate high-fidelity, interactive prototypes. While Stitch generates React-based components, it is invaluable for quickly iterating on UI layouts, color schemes, and visual flow before committing to writing Jetpack Compose code. We will instruct Stitch to adhere to Material Design 3 principles.

---

### Prompt M0B.1: Establish Material 3 Design System

```text
You are a Lead UI/UX Designer. Create a Material Design 3 design system for the app defined in our PRD.

Use the `mcp_StitchMCP_create_design_system` tool to set up the foundation.

Constraints:
- Use Material 3 principles (vibrant colors, distinct light/dark modes, rounded corners).
- Define Primary, Secondary, Tertiary, Surface, and Error colors.
- Typography: Use standard Android fonts (e.g., Roboto) or a modern Google Font (e.g., Inter, Outfit).
- Shapes: Define corner radius settings (e.g., 16dp for cards, 50% for circular buttons).

Design Markdown (Design MD) Instructions:
Include these rules in the Design MD:
1. "Follow strict Material Design 3 guidelines."
2. "Always include a TopAppBar for main screens."
3. "Use Floating Action Buttons (FAB) for primary actions on lists."
4. "Ensure minimum 48dp touch targets for accessibility."

After creating the design system, immediately call `mcp_StitchMCP_update_design_system` to apply it.
```

---

### Prompt M0B.2: Scaffold Core Screens

```text
You are a Mobile Prototyper. Using the PRD and our Screen Hierarchy (from Phase M0), generate the core screens for our Android app using Google Stitch.

For each screen, call `mcp_StitchMCP_generate_screen_from_text`.

Generate the following screens (one by one):
1. Onboarding / Login Screen
2. Home / Dashboard Screen (with Bottom Navigation Bar)
3. Detail Screen (e.g., viewing an item)
4. Creation / Form Screen (e.g., adding a new item)

Context for Generation:
"Design this screen for a native Android app using Material 3 aesthetics. Ensure standard Android UI patterns are used (BottomNavigationView, TopAppBar, FAB). The design should look extremely premium, modern, and native."
```

---

### Prompt M0B.3: Iterate and Refine Screens

```text
Review the screens generated in the previous step.
Ask the user for feedback on the layout, colors, and components.

Based on user feedback, use `mcp_StitchMCP_edit_screens` to modify the designs.

Examples of edits:
- "Change the list items on the Home screen to use elevated Material Cards."
- "Move the primary action on the Detail screen to a Floating Action Button in the bottom right."
- "Make the form inputs on the Creation screen outlined text fields instead of filled."
- "Adjust the color contrast on the Login screen to be more accessible in dark mode."
```

---

### Prompt M0B.4: Generate Edge Case Variants

```text
You are a UX QA Specialist. We need to design for mobile edge cases.

Select the core Home/Dashboard screen and use `mcp_StitchMCP_generate_variants` to create versions showing different states.

Variants to generate:
1. **Empty State:** What does this screen look like on first launch with no data? (Include a clear illustration and CTA).
2. **Error State:** What does this screen look like if the network request fails? (Include a Material Snackbar or Error Card with a retry button).
3. **Loading State:** What does this screen look like while data is fetching? (Use skeleton loaders instead of simple spinners).
```

---

### Prompt M0B.5: Dark Mode Review

```text
You are a UI Accessibility Expert. Mobile apps must look great in both Light and Dark modes.

1. Ensure the Design System has distinct dark mode background and surface colors (e.g., #121212 for background, slightly lighter for surfaces).
2. Review the generated screens. If any screen has hardcoded colors that break in dark mode, use `mcp_StitchMCP_edit_screens` to fix them.
3. Instruction to Stitch: "Ensure all text and icons use semantic color tokens (like onSurface, primary) so they automatically invert correctly in dark mode."
```

---

### Prompt M0B.6: Export State Matrix to Task Document

```text
You are a Mobile Architect. Review the final high-fidelity prototypes and extract a UI State Matrix to guide the Jetpack Compose implementation.

Create a markdown table summarizing the UI components needed for each screen.

Example output:
| Screen | Compose Components Needed | State Variables Required | Modifiers/Theming Notes |
|--------|---------------------------|--------------------------|-------------------------|
| Home   | TopAppBar, LazyColumn, FloatingActionButton | isLoading, itemsList, errorMessage | Use MaterialTheme.colorScheme.surface |
| Detail | AsyncImage, Text, Button | itemDetails, isFavorite | Circular border radius on image |

Append this table to the `implementation_plan.md` or `task.md` document so the Android engineers have clear component targets.
```

---

✅ **Phase M0B Completion Gate:**
- [ ] Material 3 Design System created and applied via Stitch.
- [ ] Core screens generated and refined.
- [ ] Empty, Error, and Loading states visualized.
- [ ] Dark mode verified.
- [ ] UI State Matrix extracted for Jetpack Compose translation.
