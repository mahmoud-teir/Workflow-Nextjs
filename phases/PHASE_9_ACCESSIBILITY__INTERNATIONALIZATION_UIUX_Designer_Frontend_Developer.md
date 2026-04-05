<a name="phase-9"></a>
# 📌 PHASE 9: ACCESSIBILITY & INTERNATIONALIZATION (UI/UX, Frontend)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 9.1: Internationalization (next-intl)

```text
You are a Frontend Developer. Set up internationalization (i18n) for a Next.js application using `next-intl`.

Library: **next-intl** (Best support for Server Components)
Strategy: **App Router middleware** + **messages/*.json**

Required:
1. `next-intl` configuration (i18n/request.ts)
2. Middleware to detect language
3. Dynamic routing `[locale]/...`
4. Server Component usage (`useTranslations`) (async in Server Components)
5. Client Component usage (same hook, needs provider)
6. Language Switcher component with dynamic switching (no full page reload)
```

```typescript
// i18n/config.ts — Shared locale configuration
export const locales = ['en', 'de', 'es', 'ar'] as const
export type Locale = (typeof locales)[number]
export const defaultLocale: Locale = 'en'

// RTL locales
export const rtlLocales: Locale[] = ['ar']
export function isRtl(locale: Locale): boolean {
  return rtlLocales.includes(locale)
}
```

```typescript
// i18n/request.ts
import { getRequestConfig } from 'next-intl/server'
import { hasLocale } from 'next-intl'
import { locales } from './config'

export default getRequestConfig(async ({ requestLocale }) => {
  const requested = await requestLocale
  const locale = hasLocale(locales, requested) ? requested : 'en'

  return {
    locale,
    messages: (await import(`../../messages/${locale}.json`)).default,
  }
})
```

```typescript
// middleware.ts — i18n composed with security headers from Phase 8
// ⚠️ Do NOT export createMiddleware directly — it will skip security headers.
// Chain both so every response gets CSP nonce + HSTS + other headers.
import createIntlMiddleware from 'next-intl/middleware'
import { type NextRequest, NextResponse } from 'next/server'
import { locales, defaultLocale } from '@/i18n/config'

const intlMiddleware = createIntlMiddleware({
  locales,
  defaultLocale,
  localeDetection: true,   // Auto-detect from Accept-Language header
  localePrefix: 'as-needed', // Only prefix non-default locales
})

function addSecurityHeaders(response: NextResponse, nonce: string): NextResponse {
  const cspHeader = `
    default-src 'self';
    script-src 'self' 'nonce-${nonce}' 'strict-dynamic' https://js.stripe.com;
    style-src 'self' 'unsafe-inline';
    img-src 'self' blob: data: https:;
    font-src 'self';
    connect-src 'self' https://api.stripe.com https://*.posthog.com https://*.sentry.io;
    frame-ancestors 'none';
    base-uri 'self';
    object-src 'none';
    upgrade-insecure-requests;
  `.replace(/\s{2,}/g, ' ').trim()

  response.headers.set('x-nonce', nonce)
  response.headers.set('Content-Security-Policy', cspHeader)
  response.headers.set('Strict-Transport-Security', 'max-age=63072000; includeSubDomains; preload')
  response.headers.set('X-Content-Type-Options', 'nosniff')
  response.headers.set('X-Frame-Options', 'DENY')
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
  response.headers.set('Permissions-Policy', 'camera=(), microphone=(), geolocation=()')
  response.headers.delete('X-Powered-By')
  return response
}

export function middleware(request: NextRequest) {
  const nonce = Buffer.from(crypto.randomUUID()).toString('base64')
  // Run intl middleware first (handles locale redirect / rewrite)
  const intlResponse = intlMiddleware(request)
  const response = intlResponse ?? NextResponse.next()
  return addSecurityHeaders(response, nonce)
}

export const config = {
  matcher: [
    // Same pattern as Phase 8 — excludes static assets
    {
      source: '/((?!_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt).*)',
      missing: [
        { type: 'header', key: 'next-router-prefetch' },
        { type: 'header', key: 'purpose', value: 'prefetch' },
      ],
    },
  ],
}
```

> **Note:** If you are using Phase 8 as the source of truth for security headers, keep a single `middleware.ts` and import both `createIntlMiddleware` and `addSecurityHeaders` there. Do not maintain two separate middleware files.


```tsx
// app/[locale]/layout.tsx
import { NextIntlClientProvider } from 'next-intl'
import { getMessages } from 'next-intl/server'
import { isRtl, type Locale } from '@/i18n/config'

export default async function LocaleLayout({
  children,
  params,
}: {
  children: React.ReactNode
  params: Promise<{ locale: string }>
}) {
  const { locale } = await params
  const messages = await getMessages()
  const dir = isRtl(locale as Locale) ? 'rtl' : 'ltr'

  return (
    <html lang={locale} dir={dir} suppressHydrationWarning>
      <body>
        <NextIntlClientProvider messages={messages}>
          {children}
        </NextIntlClientProvider>
      </body>
    </html>
  )
}
```

#### Language Switcher (Dynamic — no full page reload):

```tsx
// components/locale-switcher.tsx
'use client'

import { useLocale } from 'next-intl'
import { useRouter, usePathname } from 'next/navigation'
import { locales, type Locale } from '@/i18n/config'

const localeNames: Record<Locale, string> = {
  en: 'English',
  de: 'Deutsch',
  es: 'Español',
  ar: 'العربية',
}

export function LocaleSwitcher() {
  const locale = useLocale()
  const router = useRouter()
  const pathname = usePathname()

  function handleChange(newLocale: string) {
    // Replace current locale in pathname
    const segments = pathname.split('/')
    segments[1] = newLocale
    router.replace(segments.join('/'))
  }

  return (
    <select
      value={locale}
      onChange={(e) => handleChange(e.target.value)}
      aria-label="Select language"
    >
      {locales.map((loc) => (
        <option key={loc} value={loc}>
          {localeNames[loc]}
        </option>
      ))}
    </select>
  )
}
```

---

### Prompt 9.2: Accessibility (A11y) Implementation

```text
You are an Accessibility Specialist. Implement accessibility features according to WCAG 2.2 AA standards.

Required:
1. **Semantic HTML**: Use proper tags (main, nav, article, aside, header, footer)
2. **Keyboard Navigation**: Focus management, skip links, focus trapping in modals
3. **ARIA Attributes**: Proper roles and labels where needed
4. **Color Contrast**: Verify all UI elements (4.5:1 ratio for text, 3:1 for large text)
5. **Reduced Motion**: Respect user OS preference
6. **Screen Reader Testing**: NVDA / VoiceOver check
7. **Live Regions**: Announce dynamic content changes
8. **Automated Testing**: axe-core in CI pipeline

Tools:
- **axe-core / @axe-core/playwright**: Automated testing in CI
- **React Aria (Adobe)**: Accessible hooks for complex components
- **Radix UI / Shadcn**: Built-in accessibility
```

```tsx
// components/ui/skip-link.tsx
'use client'

export function SkipLink() {
  return (
    <a
      href="#main-content"
      className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:bg-background focus:text-foreground focus:p-4 focus:rounded-md focus:shadow-lg focus:ring-2 focus:ring-ring"
    >
      Skip to main content
    </a>
  )
}

// Usage in root layout:
// <body>
//   <SkipLink />
//   <Header />
//   <main id="main-content">
//     {children}
//   </main>
// </body>
```

#### ARIA Live Regions for Dynamic Content:

```tsx
// components/ui/announcer.tsx
'use client'

import { useState, useEffect } from 'react'

// Announces messages to screen readers without visual change
export function LiveAnnouncer() {
  const [message, setMessage] = useState('')

  useEffect(() => {
    // Listen for custom events to announce
    function handleAnnounce(e: CustomEvent<string>) {
      setMessage(e.detail)
      // Clear after announcement
      setTimeout(() => setMessage(''), 1000)
    }

    window.addEventListener('announce', handleAnnounce as EventListener)
    return () => window.removeEventListener('announce', handleAnnounce as EventListener)
  }, [])

  return (
    <div
      role="status"
      aria-live="polite"
      aria-atomic="true"
      className="sr-only"
    >
      {message}
    </div>
  )
}

// Usage: announce('3 results found')
export function announce(message: string) {
  window.dispatchEvent(new CustomEvent('announce', { detail: message }))
}
```

#### Automated a11y Testing in CI (Playwright + axe-core):

```typescript
// e2e/accessibility.spec.ts
import { test, expect } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'

test.describe('Accessibility', () => {
  test('home page has no a11y violations', async ({ page }) => {
    await page.goto('/')
    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag22aa'])
      .analyze()

    expect(results.violations).toEqual([])
  })

  test('login page has no a11y violations', async ({ page }) => {
    await page.goto('/login')
    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag22aa'])
      .analyze()

    expect(results.violations).toEqual([])
  })

  test('keyboard navigation works on main nav', async ({ page }) => {
    await page.goto('/')

    // Tab to first nav link
    await page.keyboard.press('Tab') // Skip link
    await page.keyboard.press('Tab') // First nav item

    const focused = await page.evaluate(() => document.activeElement?.tagName)
    expect(focused).toBe('A')
  })
})
```

---

### Prompt 9.3: RTL Support (Right-to-Left)

```text
Implement full RTL support for languages like Arabic and Hebrew.

Required:
1. **Logical Properties**: Use Tailwind logical utilities (ms-, me-, ps-, pe-, start-, end-)
2. **Direction Switching**: Set `dir="rtl"` on `<html>` based on locale (see layout above)
3. **Icon Flipping**: Flip directional icons in RTL mode
4. **Font Pairing**: Arabic + Latin font configuration
```

```css
/* Tailwind CSS v4 handles logical properties automatically via start/end utilities */

/* Example: Using logical properties */
.btn-icon {
  @apply ms-2; /* margin-left in LTR, margin-right in RTL */
}

/* For manual icon flipping */
.arrow-icon {
  @apply transform rtl:rotate-180;
}

/* Arabic font pairing in globals.css */
@theme {
  --font-arabic: 'Noto Sans Arabic Variable', 'Inter Variable', system-ui, sans-serif;
}
```

```tsx
// Example: RTL-aware component
function NavigationArrow({ direction }: { direction: 'next' | 'prev' }) {
  return (
    <ChevronRight
      className={cn(
        'h-4 w-4',
        direction === 'prev' && 'rotate-180',
        // In RTL, arrows should flip
        'rtl:rotate-180',
        direction === 'prev' && 'rtl:rotate-0'
      )}
    />
  )
}
```

#### Content Translation Workflow:

```text
For managing translations at scale:

1. **JSON message files** — One per locale (messages/en.json, messages/de.json)
2. **Nested namespaces** — Organize by feature (auth.login.title, dashboard.stats.label)
3. **ICU Message Syntax** — For plurals, dates, numbers:
   - "items": "{count, plural, =0 {No items} one {1 item} other {# items}}"
4. **AI-assisted translation** — Use Claude/GPT to draft translations, then review with native speakers
5. **Extraction** — Use next-intl's extraction tools to find missing translations
6. **CI check** — Validate all locales have the same keys (custom script or i18n-ally VSCode extension)

Recommended tools:
- **Crowdin / Lokalise** — Translation management platform
- **i18n Ally (VSCode)** — IDE extension for managing translations inline
```

```text
Implement full i18n support with RTL and comprehensive accessibility features using modern libraries (next-intl, Radix UI, axe-core).
```
