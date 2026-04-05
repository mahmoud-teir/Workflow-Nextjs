<a name="phase-12"></a>
# 📌 PHASE 12: OBSERVABILITY & MONITORING (SRE)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 12.1: OpenTelemetry Implementation

```text
You are an SRE. Implement full observability stack using OpenTelemetry (OTel).

Tools: **OpenTelemetry (Auto-instrumentation)** + **Grafana/Tempo/Loki** or **Honeycomb/Datadog**
Alternative: **Vercel Analytics** (lightweight, zero-config for Vercel deployments)

Required:
1. Auto-instrumentation for Next.js (http, pg, redis)
2. Trace propagation (client → server → db)
3. Structured logging (Pino)
4. Metrics export (Prometheus)
5. Correlation ID for request tracing
```

```typescript
// instrumentation.ts (Next.js built-in instrumentation hook)
// ⚠️ OTel SDK init failures must be caught here — an uncaught error will crash the server
// before it can serve any requests.
export async function register() {
  if (process.env.NEXT_RUNTIME === 'nodejs') {
    try {
      await import('@/lib/otel-server')
    } catch (err) {
      console.error('Failed to initialize OpenTelemetry', err)
    }
  }
}
```

```typescript
// lib/otel-server.ts
import { NodeSDK } from '@opentelemetry/sdk-node'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http'
import { Resource } from '@opentelemetry/resources'
import { ATTR_SERVICE_NAME, ATTR_SERVICE_VERSION } from '@opentelemetry/semantic-conventions'
import { getNodeAutoInstrumentations } from '@opentelemetry/auto-instrumentations-node'

const sdk = new NodeSDK({
  resource: new Resource({
    [ATTR_SERVICE_NAME]: process.env.OTEL_SERVICE_NAME || 'nextjs-app',
    [ATTR_SERVICE_VERSION]: process.env.npm_package_version || '0.0.0',
    'deployment.environment': process.env.NODE_ENV || 'development',
  }),
  traceExporter: new OTLPTraceExporter({
    url: process.env.OTEL_EXPORTER_OTLP_ENDPOINT || 'http://localhost:4318/v1/traces',
  }),
  instrumentations: [
    getNodeAutoInstrumentations({
      // Instrument HTTP, fetch, pg, redis, etc. automatically
      '@opentelemetry/instrumentation-http': { enabled: true },
      '@opentelemetry/instrumentation-fetch': { enabled: true },
      '@opentelemetry/instrumentation-pg': { enabled: true },
      // Disable noisy instrumentations
      '@opentelemetry/instrumentation-fs': { enabled: false },
    }),
  ],
})

sdk.start()

// Graceful shutdown
process.on('SIGTERM', () => {
  sdk.shutdown().then(
    () => console.log('OTel SDK shut down'),
    (err) => console.error('Error shutting down OTel SDK', err)
  )
})
```

```text
Required npm packages:
- @opentelemetry/sdk-node
- @opentelemetry/exporter-trace-otlp-http
- @opentelemetry/resources
- @opentelemetry/semantic-conventions
- @opentelemetry/auto-instrumentations-node
```

#### Correlation ID Middleware:

```typescript
// lib/correlation.ts
import { headers } from 'next/headers'
import { randomUUID } from 'crypto'

export async function getCorrelationId(): Promise<string> {
  const headersList = await headers()
  return headersList.get('x-correlation-id') || randomUUID()
}

// In middleware.ts — add to handleAuth or security middleware:
// const correlationId = request.headers.get('x-correlation-id') || crypto.randomUUID()
// response.headers.set('x-correlation-id', correlationId)
```

#### Vercel Analytics (Lightweight Alternative):

```tsx
// app/layout.tsx — Zero-config for Vercel deployments
import { Analytics } from '@vercel/analytics/react'
import { SpeedInsights } from '@vercel/speed-insights/next'

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        {children}
        <Analytics />
        <SpeedInsights />
      </body>
    </html>
  )
}
```

---

### Prompt 12.2: Error Tracking (Sentry)

```text
Set up Sentry for error monitoring and performance tracing.

Required:
1. Client, server, and edge configuration
2. Source maps upload
3. Session Replay (for debugging UI issues)
4. Performance monitoring
5. Environment-aware sample rates
```

```typescript
// sentry.client.config.ts
import * as Sentry from '@sentry/nextjs'

Sentry.init({
  dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
  environment: process.env.NODE_ENV,

  // Adjust sample rates by environment
  tracesSampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0,
  replaysSessionSampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0,
  replaysOnErrorSampleRate: 1.0, // Always capture replay on error

  integrations: [
    Sentry.replayIntegration({
      maskAllText: true,
      blockAllMedia: true,
    }),
    Sentry.feedbackIntegration({
      colorScheme: 'system',
    }),
  ],
})
```

```typescript
// sentry.server.config.ts
import * as Sentry from '@sentry/nextjs'

Sentry.init({
  dsn: process.env.SENTRY_DSN,
  environment: process.env.NODE_ENV,
  tracesSampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0,
})
```

```typescript
// sentry.edge.config.ts
import * as Sentry from '@sentry/nextjs'

Sentry.init({
  dsn: process.env.SENTRY_DSN,
  environment: process.env.NODE_ENV,
  tracesSampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0,
})
```

```typescript
// next.config.ts — Sentry integration
import { withSentryConfig } from '@sentry/nextjs'

const nextConfig = { /* ... */ }

export default withSentryConfig(nextConfig, {
  org: 'your-org',
  project: 'your-project',
  silent: !process.env.CI,
  widenClientFileUpload: true,
  hideSourceMaps: true,
  disableLogger: true,
})
```

---

### Prompt 12.3: Logging Strategy (Structured Logs)

```text
Implement structured logging with Pino.

Required:
1. JSON format logs (machine readable)
2. Log levels (debug, info, warn, error)
3. Correlation ID integration
4. Redaction of sensitive data (PII)
5. Pretty printing in development only
```

```typescript
// lib/logger.ts
import pino from 'pino'

const isDev = process.env.NODE_ENV === 'development'

export const logger = pino({
  level: process.env.LOG_LEVEL || (isDev ? 'debug' : 'info'),
  redact: {
    paths: [
      'req.headers.authorization',
      'req.headers.cookie',
      'password',
      'email',
      'creditCard',
      '*.password',
      '*.email',
    ],
    censor: '[REDACTED]',
  },
  ...(isDev && {
    transport: {
      target: 'pino-pretty',
      options: { colorize: true },
    },
  }),
})

// Create child logger with correlation ID
export function createRequestLogger(correlationId: string) {
  return logger.child({ correlationId })
}

// Usage in Server Actions / Route Handlers:
// const log = createRequestLogger(await getCorrelationId())
// log.info({ userId: session.user.id }, 'User created a post')
// log.error({ err, postId }, 'Failed to publish post')
```

#### Alerting Rules:

```text
Set up alerting for critical metrics:

1. **Error Rate > 5%** → PagerDuty / Opsgenie P1 alert
2. **P95 Latency > 2s** → Slack notification
3. **Database connection pool exhausted** → P1 alert
4. **Memory usage > 80%** → Warning alert
5. **Deployment failed** → Slack + email notification

Tools:
- Grafana Alerting (if using Grafana stack)
- Sentry Alerts (error rate, issue frequency)
- Vercel Monitoring (if deployed on Vercel)
- PagerDuty / Opsgenie (on-call rotation)
- Uptime monitoring: Checkly, Better Uptime, or UptimeRobot
```

```text
Implement comprehensive observability stack to catch issues early and debug effectively.
```
