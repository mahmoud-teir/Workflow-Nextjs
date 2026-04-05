<a name="phase-17"></a>📌 PHASE 17: MOBILE & PWA (Frontend Engineer)

> **Next.js Version:** This phase uses Next.js (latest stable). See Phase 0, Prompt 0.7 for the version compatibility table.

### Prompt 17.1: Progressive Web App (PWA)
You are a Mobile Web Expert. Turn a Next.js application into an installable PWA.

Library: **Serwist** (Actively maintained Next.js PWA plugin, successor to next-pwa)
Strategy: **Service Worker** + **Web App Manifest** + **Offline Fallback**

Required:
1. Web App Manifest with modern fields (shortcuts, screenshots, share_target)
2. Custom Service Worker with Serwist (precaching + runtime caching)
3. Install Prompt logic (A2HS — Add to Home Screen)
4. Offline support with caching strategies
5. App update detection and prompt

```bash
# Install Serwist
pnpm add @serwist/next
pnpm add -D serwist
```

```typescript
// next.config.ts
// ⚠️ withSerwist MUST be the outermost (last) wrapper — it injects the SW entry point
// into the webpack config. Any wrapper applied after it (e.g. withBundleAnalyzer) will
// overwrite the webpack config without the SW plugin, silently breaking the PWA build.
//
// Correct order with multiple wrappers:
//   withSerwist(serwistConfig)(withBundleAnalyzer(analyzerConfig)(nextConfig))
//                ↑ outermost = runs last = has the final word on webpack config
import withSerwist from '@serwist/next'

const nextConfig = {
  // ... other config (output: 'standalone', experimental flags, etc.)
}

export default withSerwist({
  swSrc: 'app/sw.ts',
  swDest: 'public/sw.js',
  disable: process.env.NODE_ENV === 'development',
})(nextConfig)
```

```typescript
// app/sw.ts
import { defaultCache } from '@serwist/next/worker'
import { type PrecacheEntry, Serwist } from 'serwist'

declare const self: ServiceWorkerGlobalScope & {
  __SW_MANIFEST: (PrecacheEntry | string)[] | undefined
}

const serwist = new Serwist({
  precacheEntries: self.__SW_MANIFEST,
  skipWaiting: true,
  clientsClaim: true,
  navigationPreload: true,
  runtimeCaching: defaultCache,
  fallbacks: {
    entries: [
      {
        url: '/offline',
        matcher({ request }) {
          return request.destination === 'document'
        },
      },
    ],
  },
})

serwist.addEventListeners()
```

```typescript
// Custom caching strategies (add to sw.ts runtimeCaching or replace defaultCache)
import {
  CacheFirst,
  NetworkFirst,
  StaleWhileRevalidate,
} from 'serwist'

const customCache = [
  // Static assets — cache first (long-lived)
  {
    matcher: ({ request }: { request: Request }) =>
      request.destination === 'image' ||
      request.destination === 'font' ||
      request.destination === 'style',
    handler: new CacheFirst({
      cacheName: 'static-assets',
      plugins: [
        { maxEntries: 100, maxAgeSeconds: 30 * 24 * 60 * 60 }, // 30 days
      ],
    }),
  },
  // API calls — network first (fresh data preferred)
  {
    matcher: ({ url }: { url: URL }) => url.pathname.startsWith('/api/'),
    handler: new NetworkFirst({
      cacheName: 'api-cache',
      networkTimeoutSeconds: 5,
      plugins: [
        { maxEntries: 50, maxAgeSeconds: 24 * 60 * 60 }, // 1 day
      ],
    }),
  },
  // Pages — stale while revalidate
  {
    matcher: ({ request }: { request: Request }) =>
      request.destination === 'document',
    handler: new StaleWhileRevalidate({
      cacheName: 'pages-cache',
      plugins: [
        { maxEntries: 30, maxAgeSeconds: 7 * 24 * 60 * 60 }, // 7 days
      ],
    }),
  },
]
```

```json
// public/manifest.webmanifest
{
  "name": "My App — Full Application Name",
  "short_name": "My App",
  "description": "A brief description of your application",
  "start_url": "/dashboard",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#000000",
  "orientation": "portrait-primary",
  "categories": ["productivity"],
  "icons": [
    { "src": "/icons/icon-192.png", "sizes": "192x192", "type": "image/png" },
    { "src": "/icons/icon-512.png", "sizes": "512x512", "type": "image/png" },
    { "src": "/icons/icon-maskable-512.png", "sizes": "512x512", "type": "image/png", "purpose": "maskable" }
  ],
  "screenshots": [
    { "src": "/screenshots/desktop.png", "sizes": "1280x720", "type": "image/png", "form_factor": "wide" },
    { "src": "/screenshots/mobile.png", "sizes": "390x844", "type": "image/png", "form_factor": "narrow" }
  ],
  "shortcuts": [
    {
      "name": "New Item",
      "short_name": "New",
      "url": "/dashboard/new",
      "icons": [{ "src": "/icons/shortcut-new.png", "sizes": "96x96" }]
    }
  ],
  "share_target": {
    "action": "/share",
    "method": "POST",
    "enctype": "multipart/form-data",
    "params": {
      "title": "title",
      "text": "text",
      "url": "url"
    }
  }
}
```

```tsx
// app/layout.tsx — Add manifest link
export const metadata = {
  manifest: '/manifest.webmanifest',
  appleWebApp: {
    capable: true,
    statusBarStyle: 'default',
    title: 'My App',
  },
}
```

```tsx
// app/offline/page.tsx — Offline fallback page
export default function OfflinePage() {
  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <div className="text-center">
        <h1 className="text-2xl font-bold">You're offline</h1>
        <p className="mt-2 text-muted-foreground">
          Check your internet connection and try again.
        </p>
        <button
          onClick={() => window.location.reload()}
          className="mt-4 rounded-md bg-primary px-4 py-2 text-primary-foreground"
        >
          Retry
        </button>
      </div>
    </div>
  )
}
```

```tsx
// components/install-prompt.tsx
'use client'

import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'

interface BeforeInstallPromptEvent extends Event {
  prompt(): Promise<void>
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>
}

export function InstallPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null)
  const [isInstalled, setIsInstalled] = useState(false)

  useEffect(() => {
    // Check if already installed
    if (window.matchMedia('(display-mode: standalone)').matches) {
      setIsInstalled(true)
      return
    }

    const handler = (e: Event) => {
      e.preventDefault()
      setDeferredPrompt(e as BeforeInstallPromptEvent)
    }

    window.addEventListener('beforeinstallprompt', handler)
    window.addEventListener('appinstalled', () => setIsInstalled(true))

    return () => window.removeEventListener('beforeinstallprompt', handler)
  }, [])

  if (isInstalled || !deferredPrompt) return null

  return (
    <Button
      variant="outline"
      onClick={async () => {
        await deferredPrompt.prompt()
        const { outcome } = await deferredPrompt.userChoice
        if (outcome === 'accepted') setDeferredPrompt(null)
      }}
    >
      Install App
    </Button>
  )
}
```

```tsx
// components/update-prompt.tsx — Detect and prompt for app updates
'use client'

import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'

export function UpdatePrompt() {
  const [waitingWorker, setWaitingWorker] = useState<ServiceWorker | null>(null)
  const [showUpdate, setShowUpdate] = useState(false)

  useEffect(() => {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.ready.then((registration) => {
        registration.addEventListener('updatefound', () => {
          const newWorker = registration.installing
          newWorker?.addEventListener('statechange', () => {
            if (
              newWorker.state === 'installed' &&
              navigator.serviceWorker.controller
            ) {
              setWaitingWorker(newWorker)
              setShowUpdate(true)
            }
          })
        })
      })
    }
  }, [])

  if (!showUpdate) return null

  return (
    <div className="fixed bottom-4 left-4 right-4 z-50 flex items-center justify-between rounded-lg bg-primary p-4 text-primary-foreground shadow-lg sm:left-auto sm:w-96">
      <p className="text-sm font-medium">A new version is available!</p>
      <Button
        size="sm"
        variant="secondary"
        onClick={() => {
          waitingWorker?.postMessage({ type: 'SKIP_WAITING' })
          window.location.reload()
        }}
      >
        Update
      </Button>
    </div>
  )
}
```

### Prompt 17.2: Push Notifications (Web Push API)
Implement push notifications using Web Push API and VAPID keys.

Package: **web-push** (Server)
Browser API: **Service Worker Registration** (Client)

1. Generate VAPID keys
2. Request permission (Client)
3. Subscribe user (Client → Server)
4. Send notification (Server Action)
5. Handle notification click (Service Worker)

```bash
# Generate VAPID keys
pnpm add web-push
npx web-push generate-vapid-keys
# Add to .env: NEXT_PUBLIC_VAPID_PUBLIC_KEY, VAPID_PRIVATE_KEY
```

```typescript
// app/actions/push.ts
'use server'

import webpush from 'web-push'
import { db } from '@/db'
import { pushSubscriptions } from '@/db/schema'
import { requireAuth } from '@/lib/action-utils'

webpush.setVapidDetails(
  'mailto:support@myapp.com',
  process.env.NEXT_PUBLIC_VAPID_PUBLIC_KEY!,
  process.env.VAPID_PRIVATE_KEY!
)

export async function subscribeToPush(subscription: PushSubscriptionJSON) {
  const { user } = await requireAuth()

  await db.insert(pushSubscriptions).values({
    userId: user.id,
    endpoint: subscription.endpoint!,
    keys: subscription.keys as Record<string, string>,
  }).onConflictDoUpdate({
    target: pushSubscriptions.endpoint,
    set: { keys: subscription.keys as Record<string, string> },
  })

  return { success: true }
}

export async function sendPushNotification(
  userId: string,
  payload: { title: string; body: string; url?: string }
) {
  const subscriptions = await db.query.pushSubscriptions.findMany({
    where: (t, { eq }) => eq(t.userId, userId),
  })

  const results = await Promise.allSettled(
    subscriptions.map((sub) =>
      webpush.sendNotification(
        { endpoint: sub.endpoint, keys: sub.keys } as webpush.PushSubscription,
        JSON.stringify(payload)
      )
    )
  )

  // Clean up expired subscriptions (410 Gone)
  const expired = results
    .map((r, i) => (r.status === 'rejected' && r.reason?.statusCode === 410 ? i : -1))
    .filter((i) => i !== -1)

  if (expired.length > 0) {
    const expiredEndpoints = expired.map((i) => subscriptions[i].endpoint)
    await db.delete(pushSubscriptions)
      .where(({ endpoint }, { inArray }) => inArray(endpoint, expiredEndpoints))
  }

  return { sent: results.filter((r) => r.status === 'fulfilled').length }
}
```

```tsx
// components/push-permission.tsx
'use client'

import { useState } from 'react'
import { subscribeToPush } from '@/app/actions/push'
import { Button } from '@/components/ui/button'

export function PushPermission() {
  const [permission, setPermission] = useState<NotificationPermission>(
    typeof Notification !== 'undefined' ? Notification.permission : 'default'
  )

  async function handleSubscribe() {
    const result = await Notification.requestPermission()
    setPermission(result)

    if (result === 'granted') {
      const registration = await navigator.serviceWorker.ready
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: process.env.NEXT_PUBLIC_VAPID_PUBLIC_KEY!,
      })
      await subscribeToPush(subscription.toJSON())
    }
  }

  if (permission === 'granted') return null
  if (permission === 'denied') {
    return <p className="text-sm text-muted-foreground">Notifications blocked. Enable in browser settings.</p>
  }

  return (
    <Button variant="outline" onClick={handleSubscribe}>
      Enable Notifications
    </Button>
  )
}
```

```typescript
// Add to app/sw.ts — Handle notification clicks
self.addEventListener('notificationclick', (event) => {
  event.notification.close()

  const url = event.notification.data?.url || '/'

  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clients) => {
      // Focus existing tab if open
      for (const client of clients) {
        if (client.url === url && 'focus' in client) {
          return client.focus()
        }
      }
      // Otherwise open new tab
      return self.clients.openWindow(url)
    })
  )
})
```

### Prompt 17.3: Background Sync (Offline Form Submission)
Enable offline form submissions that sync when connectivity returns.

```typescript
// lib/background-sync.ts
export async function registerSync(tag: string) {
  if ('serviceWorker' in navigator && 'SyncManager' in window) {
    const registration = await navigator.serviceWorker.ready
    await (registration as any).sync.register(tag)
  }
}

export function queueOfflineAction(action: { url: string; method: string; body: string }) {
  const queue = JSON.parse(localStorage.getItem('offline-queue') || '[]')
  queue.push({ ...action, timestamp: Date.now() })
  localStorage.setItem('offline-queue', JSON.stringify(queue))
}
```

```typescript
// Add to app/sw.ts — Process queued actions on sync
self.addEventListener('sync', (event) => {
  if (event.tag === 'offline-actions') {
    event.waitUntil(processOfflineQueue())
  }
})

async function processOfflineQueue() {
  const queue = JSON.parse(
    (await (await caches.open('offline-data')).match('queue'))?.text() || '[]'
  )

  const remaining = []
  for (const action of queue) {
    try {
      await fetch(action.url, {
        method: action.method,
        headers: { 'Content-Type': 'application/json' },
        body: action.body,
      })
    } catch {
      remaining.push(action) // Retry next sync
    }
  }

  const cache = await caches.open('offline-data')
  await cache.put('queue', new Response(JSON.stringify(remaining)))
}
```

### Prompt 17.4: Native Wrapper (Capacitor)
Convert Next.js app to native iOS/Android app using Capacitor.

Tool: **Ionic Capacitor** (Web to Native bridge)
Requirement: Next.js must use `output: 'export'` for static export

```bash
# Install and initialize
pnpm add @capacitor/core
pnpm add -D @capacitor/cli
npx cap init "My App" com.myapp.app --web-dir=out

# Add platforms
npx cap add android
npx cap add ios

# Add common plugins
pnpm add @capacitor/camera @capacitor/haptics @capacitor/status-bar @capacitor/splash-screen @capacitor/app
```

```typescript
// capacitor.config.ts
import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.myapp.app',
  appName: 'My App',
  webDir: 'out',
  server: {
    // Use local server for development
    ...(process.env.NODE_ENV === 'development' && {
      url: 'http://192.168.1.100:3000', // Your local IP
      cleartext: true,
    }),
  },
  plugins: {
    SplashScreen: {
      launchAutoHide: true,
      launchShowDuration: 2000,
      backgroundColor: '#ffffff',
      showSpinner: false,
    },
    StatusBar: {
      style: 'DARK',
      backgroundColor: '#ffffff',
    },
  },
  android: {
    buildOptions: {
      keystorePath: 'release.keystore',
      keystoreAlias: 'myapp',
    },
  },
  ios: {
    scheme: 'My App',
  },
}

export default config
```

```typescript
// lib/capacitor.ts — Platform detection and native API wrappers
import { Capacitor } from '@capacitor/core'

export const isNative = Capacitor.isNativePlatform()
export const platform = Capacitor.getPlatform() // 'web' | 'ios' | 'android'

export async function takePhoto() {
  if (!isNative) return null

  const { Camera, CameraResultType } = await import('@capacitor/camera')
  const photo = await Camera.getPhoto({
    quality: 80,
    allowEditing: false,
    resultType: CameraResultType.Uri,
  })
  return photo.webPath
}

export async function hapticFeedback(style: 'light' | 'medium' | 'heavy' = 'medium') {
  if (!isNative) return

  const { Haptics, ImpactStyle } = await import('@capacitor/haptics')
  const styleMap = { light: ImpactStyle.Light, medium: ImpactStyle.Medium, heavy: ImpactStyle.Heavy }
  await Haptics.impact({ style: styleMap[style] })
}
```

```bash
# Build and deploy workflow
pnpm build          # Build Next.js with output: 'export'
npx cap sync        # Copy web assets + update native plugins
npx cap open android  # Open in Android Studio
npx cap open ios      # Open in Xcode
```

### Prompt 17.5: iOS PWA Limitations & Workarounds

> ⚠️ **iOS Safari has significant PWA limitations.** Plan for these constraints early.

| Feature | iOS Support | Workaround |
|---------|------------|------------|
| Push Notifications | iOS 16.4+ only (must be added to Home Screen) | Show in-app notification banner; guide users to add to Home Screen |
| Background Sync | Not supported | Queue in IndexedDB, process on next app open |
| Badging API | Not supported | Use in-app badge counters |
| Storage quota | ~50MB evictable | Use IndexedDB sparingly; warn users about storage |
| Splash screen | Limited to `apple-touch-startup-image` | Provide multiple sizes via media queries |
| Orientation lock | Not supported in standalone | Design for both orientations |
| `beforeinstallprompt` | Not fired on iOS | Show custom "Add to Home Screen" instructions |

```tsx
// components/ios-install-instructions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'

function isIOS() {
  return /iPad|iPhone|iPod/.test(navigator.userAgent) && !(window as any).MSStream
}

function isInStandaloneMode() {
  return ('standalone' in window.navigator && (window.navigator as any).standalone) ||
    window.matchMedia('(display-mode: standalone)').matches
}

export function IOSInstallInstructions() {
  const [show, setShow] = useState(false)

  useEffect(() => {
    setShow(isIOS() && !isInStandaloneMode())
  }, [])

  if (!show) return null

  return (
    <div className="rounded-lg border bg-card p-4">
      <p className="text-sm font-medium">Install this app on your iPhone:</p>
      <ol className="mt-2 list-inside list-decimal text-sm text-muted-foreground">
        <li>Tap the Share button <span aria-label="share icon">⬆️</span> in Safari</li>
        <li>Scroll down and tap <strong>"Add to Home Screen"</strong></li>
        <li>Tap <strong>"Add"</strong> to confirm</li>
      </ol>
      <Button variant="ghost" size="sm" className="mt-2" onClick={() => setShow(false)}>
        Dismiss
      </Button>
    </div>
  )
}
```

```html
<!-- Add to app/layout.tsx head for iOS splash screens -->
<!-- Generate these with: https://progressier.com/pwa-icons-and-splash-screen-generator -->
<link rel="apple-touch-icon" href="/icons/apple-touch-icon.png" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
```

Implement fully functional PWA with offline support, push notifications, background sync, and native wrapper capability — with iOS workarounds for cross-platform reliability.
