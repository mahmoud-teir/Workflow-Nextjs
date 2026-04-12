<a name="phase-12"></a>
# 📌 PHASE 12: OBSERVABILITY & MONITORING (SRE)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 12.1: OpenTelemetry Implementation

```text
You are a Site Reliability Engineer (SRE). Implement a full observability stack using OpenTelemetry (OTel).

Tool: **OpenTelemetry (Auto-instrumentation)**

Constraints:
- You MUST utilize Next.js's native `instrumentation.ts` hook to initialize the SDK.
- Trace propagation must connect Next.js route handlers seamlessly to downstream database queries.
- Disable noisy auto-instrumentation (like frequent `fs` reads).

Decision Guide:
- Use **Vercel Analytics + Sentry** if deploying to Vercel and wanting the simplest integration.
- Use **OpenTelemetry** if bringing your own backend (Grafana/Datadog) or managing complex distributed microservices.

Required Output Format: Provide complete code for:
1. `instrumentation.ts` registration hook.
2. `lib/otel-server.ts` configuring the NodeSDK and OTLP exporter.
3. A Correlation ID middleware utility that attaches unique IDs to incoming requests.

⚠️ Common Pitfalls:
- **Pitfall:** Initializing OpenTelemetry in a standard React file or layout. It must run in the Node runtime before Next.js begins handling requests.
- **Solution:** Always use the experimental `instrumentation.ts` file provided by Next.js for SDK initialization.
```

✅ **Verification Checklist:**
- [ ] Trigger an API route. Verify that the trace correctly captures the HTTP request duration AND the inner database query duration locally.

---

### Prompt 12.2: Error Tracking (Sentry)

```text
You are an Application Monitoring Lead. Set up Sentry for error tracking and performance tracing.

Constraints:
- Sentry must be fully initialized across all Next.js environments (Client, Server, Edge).
- Do NOT upload source maps to Sentry on generic PR preview builds to save CI/CD time, only on production builds.
- Configure Session Replay, but aggressively mask PII (Personally Identifiable Information).

Required Output Format:
1. Provide the three config files (`sentry.client.config.ts`, `server`, `edge`).
2. Provide the `next.config.ts` wrapper (`withSentryConfig`).
3. Explicit instructions on how to trigger a fake exception to test the integration.
```

✅ **Verification Checklist:**
- [ ] Add a `throw new Error('Sentry Test')` to a Server Action. Click a button to invoke it. Verify the error appears in Sentry tagged as a Node/Server error, not a browser error.

---

### Prompt 12.3: Logging Strategy (Structured Logs)

```text
You are a Backend Systems Architect. Implement high-performance structured logging.

Tool: **Pino**

Constraints:
- Logs MUST output in JSON format in production (machine readable), but prettified in development (human readable).
- Sensitive fields (passwords, tokens, credit cards) MUST be dynamically redacted by the logger before outputting.
- Attach the Correlation ID to every child logger.

Required Output Format:
1. `lib/logger.ts`: Pino configuration including the redaction spec.
2. An example of creating a child logger per-request.
3. A list of Alerting Rules (e.g., Error Rate > 5% triggers PagerDuty).
```

✅ **Verification Checklist:**
- [ ] Attempt to log an object containing a `password` key. Verify the console output replaces the string with `[REDACTED]`.

---
📎 **Related Phases:**
- Prerequisites: [Phase 11: DevOps & Infrastructure](./PHASE_11_DEVOPS__INFRASTRUCTURE_DevOps_Engineer.md)
- Proceeds to: [Phase 13: Deployment & CI/CD](./PHASE_13_DEPLOYMENT__CICD_DevOps_Engineer.md)
