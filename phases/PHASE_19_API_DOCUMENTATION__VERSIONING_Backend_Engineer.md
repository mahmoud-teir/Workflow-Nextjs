<a name="phase-19"></a>
# 📌 PHASE 19: API DOCUMENTATION & VERSIONING (Backend Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 19.1: OpenAPI Specification & Documentation UI

```text
You are an API Documentation Engineer. Set up a modern API Reference UI for the backend.

Tool: **Scalar** (Modern successor to Swagger UI)
Spec: **OpenAPI 3.1**

Constraints:
- You must serve the OpenAPI JSON spec via a standard Next.js GET Route Handler (`/api/docs`).
- You must set up Scalar UI via `@scalar/nextjs-api-reference` to consume that spec.
- The spec must include clear response schemas, authorization header definitions, and error (400, 401, 500) payloads.

Required Output Format: Provide complete code for:
1. `lib/openapi.ts`: Generating or exporting the OpenAPI 3.1 Document object.
2. `app/api/docs/route.ts`: Exporting the raw JSON spec.
3. `app/api/docs/ui/route.ts`: Configuring the Scalar UI endpoint.
```

✅ **Verification Checklist:**
- [ ] Navigate to `/api/docs/ui` locally. Verify the UI renders without crashing and your endpoints are visible.

---

### Prompt 19.2: API Versioning Strategy

```text
You are a Backend Systems Architect. Implement a robust API versioning strategy preventing breaking changes.

Strategy: **URL Prefixing (`/api/v1/...`)**

Constraints:
- Outline how to handle a theoretical breaking change (e.g., migrating from offset-based pagination to cursor-based pagination).
- You MUST utilize HTTP headers (`Warning`, `Deprecation`, `Sunset`) when an older version is nearing End-of-Life.

Required Output Format: Provide a code sample of:
1. Directory structure separating `/v1/` and `/v2/` routes.
2. A utility function `withDeprecationHeaders(response, sunsetDate)` to wrap outgoing responses from deprecated endpoints.
3. A written "API Deprecation Policy" document detailing the timeline from Announcement -> Deprecation -> Sunset.

⚠️ Common Pitfalls:
- **Pitfall:** Using Header-based versioning (`Accept: application/vnd.company.v2+json`) in Next.js, which complicates CDNs, Caching, and debugging significantly.
- **Solution:** Stick strictly to URL-based versioning (`/api/v1/resource`) for absolute clarity.
```

✅ **Verification Checklist:**
- [ ] Hit a theoretical `/api/v1/` endpoint that is deprecated. Verify the response headers contain a `Sunset` date.

---

### Prompt 19.3: Type-safe SDK Client Generation

```text
You are an SDK Developer. Given an OpenAPI spec, generate a fully type-safe fetch client for frontend consumption.

Tools: `openapi-typescript` and `openapi-fetch`.

Constraints:
- Do not manually edit the generated TypeScript file. Define it as an NPM script.
- The `openapi-fetch` client must gracefully handle injecting the Authentication token (Bearer).

Required Output Format: Provide:
1. The `package.json` scripts needed to execute the AST generation (`openapi-typescript ...`).
2. `lib/api-client.ts` initializing `createClient<paths>({ ... })` from `openapi-fetch`.
```

✅ **Verification Checklist:**
- [ ] Run the generation script. Verify `types/api.d.ts` is created and contains the accurate schemas mapped from your `openapi.ts`.

---
📎 **Related Phases:**
- Prerequisites: [Phase 2: Backend Setup](./PHASE_2_BACKEND_SETUP_API_Routes__Server_Actions.md)
- Proceeds to: [Phase 20: Error Handling & Resilience](./PHASE_20_ERROR_HANDLING__RESILIENCE_Full-Stack_Engineer.md)
