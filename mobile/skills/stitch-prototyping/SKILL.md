---
name: stitch-prototyping
description: Use this skill when generating UI prototypes using the Google Stitch MCP. Defines best practices for using Stitch in a mobile context.
origin: Mobile ECC
stack: Stitch MCP, React (for prototyping), Material Design 3
---

# Stitch Prototyping Skill

## Overview
Stitch is an MCP tool used to quickly generate, edit, and iterate on high-fidelity, interactive UI layouts before translating them to Jetpack Compose. Stitch generates web/React outputs, but we use it to establish visual layout, color schemes, and component hierarchy for Android.

## Core Stitch Tools
- `mcp_StitchMCP_create_design_system`: Sets up the global theme (colors, fonts).
- `mcp_StitchMCP_update_design_system`: Applies updates to the global theme.
- `mcp_StitchMCP_generate_screen_from_text`: Creates a new screen based on a prompt.
- `mcp_StitchMCP_edit_screens`: Modifies existing screens.
- `mcp_StitchMCP_generate_variants`: Creates variations of a screen (e.g., error state, empty state).

## Best Practices for Mobile Prototyping
1. **Enforce Material Design 3**: Always prompt Stitch to use Material Design 3 components (e.g., Floating Action Buttons, Bottom Navigation Bars, Top App Bars).
2. **Mobile Viewport**: Instruct Stitch to design for a mobile viewport constraint (e.g., `max-width: 400px`).
3. **Semantic Tokens**: Ensure the design system relies on semantic tokens (like `primary`, `onSurface`, `error`) rather than hardcoded hex values, to ensure Dark Mode compatibility.
4. **Interactive States**: Use `edit_screens` to ensure buttons and inputs have clear hover/pressed states, which translates well to Android Ripple effects.

## Example Usage Workflow
1. Run `mcp_StitchMCP_create_design_system` defining brand colors and "Material 3" guidelines.
2. Run `mcp_StitchMCP_update_design_system` to apply it.
3. Run `mcp_StitchMCP_generate_screen_from_text` to generate the Home Screen.
4. Iterate with `mcp_StitchMCP_edit_screens` until the UI matches the PRD.
