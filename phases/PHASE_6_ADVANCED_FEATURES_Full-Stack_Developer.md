<a name="phase-6"></a>
# 📌 PHASE 6: ADVANCED FEATURES (Full-Stack Developer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 6.1: Search Functionality (Database & AI)

```text
You are a Principal Backend Engineer building scalable search infrastructure. Implement search functionality in a Next.js application.

Choice:
A) **Database Search** (Prototyping / Small Apps) → Prisma Full-Text Search or Drizzle LIKE/ILIKE
B) **AI-Powered Semantic Search** (Modern / Medium Apps) → pgvector + OpenAI Embeddings
C) **Dedicated Search Engine** (Large Scale Apps) → Algolia or Meilisearch

Decision Guide:
- Use Option A if you have simple text data and want to minimize infrastructure.
- Use Option B if users will search by concepts/meaning rather than exact keywords (e.g., "how to fix car" finding "automotive repair").
- Use Option C if you need typo-tolerance, sub-millisecond response times, and facets across massive datasets.

Constraints:
- Client-side search inputs MUST be debounced to prevent spamming the server/API.
- Support optimistic UI updates during search.
- Do NOT pull all records and filter in JavaScript. Filtering must happen in the database/search engine.

Required Output Format: Provide complete code for:
1. The Server Action / API Route acting as the search resolver.
2. The UI Search Input component with `useTransition` and debounce logic.
3. If Option B: The vector embedding generation utility using Vercel AI SDK.
```

✅ **Verification Checklist:**
- [ ] Type rapidly in the search input; verify in the Network tab that only 1 or 2 requests are sent, not one per keystroke.
- [ ] Verify that empty queries return empty states or default lists without crashing.

---

### Prompt 6.2: File Upload & Management

```text
You are a Cloud Infrastructure Developer. Implement file uploads using modern Serverless edge-compatible solutions.

Recommended: **UploadThing** (Type-safe, easy setup) or **Vercel Blob** (Simple storage).

Constraints:
- You must validate MIME types AND maximum file sizes on the server before accepting the upload.
- Uploads should ideally go direct-to-cloud (via presigned URLs or UploadThing) rather than buffering entirely through your Next.js server, saving memory.
- Provide secure image delivery.

Required Output Format: Provide complete code for:
1. Setup and config logic for the chosen upload provider.
2. An authorized Server Action / API endpoint to generate upload tokens or handle uploads.
3. A drag-and-drop Client Component demonstrating upload progress.
4. Logic to delete orphaned files from storage when database records are deleted.

⚠️ Common Pitfalls:
- **Pitfall:** Passing huge blobs directly through a Next.js API Route, hitting the 4.5MB Vercel Serverless payload limit and crashing.
- **Solution:** Use presigned URLs or a provider like UploadThing to upload directly from the browser to the cloud bucket.
```

✅ **Verification Checklist:**
- [ ] Attempt to upload a 50MB file. It must gracefully reject with a file-size error.
- [ ] Attempt to upload a `.exe`; if restricted to images, it must gracefully reject.

---

### Prompt 6.3: Webhooks & Background Jobs

```text
You are a Backend Systems Architect. Implement robust background processing and webhook handling.

Constraints:
- Webhooks MUST verify cryptographic signatures from the provider (e.g., Stripe, Clerk).
- Background jobs should use Next.js 15 `after()` API for simple asynchronous tasks.
- Provide a BullMQ/Inngest pattern for complex, retriable workflows if requested.
- Handle webhook idempotency (prevent processing the same event twice).

Required Output Format: Provide complete code for:
1. `app/api/webhooks/[provider]/route.ts`: Webhook receiver with signature verification.
2. Database implementation to track processed webhook IDs (Idempotency Key).
3. A Server Action demonstrating the `after()` API.
4. Details on provisioning a local tunnel (like ngrok or Stripe CLI) to test webhooks locally.

⚠️ Common Pitfalls:
- **Pitfall:** `after()` executing, failing silently, and getting lost because logs aren't captured properly outside the request lifecycle.
- **Solution:** Ensure robust `try/catch` and Sentry/console logging directly inside the `after()` callback.
```

✅ **Verification Checklist:**
- [ ] Send a mock webhook payload with an invalid signature; ensure it returns a 400 Bad Request.
- [ ] Send the identical valid webhook twice; ensure the second request detects duplicates and returns a 200 without mutating data.

---

### Prompt 6.4: Multi-Tenant Architecture (Optional)

```text
You are a SaaS Architect. Design a multi-tenant (B2B) data isolation architecture.

Decision Guide:
- **Pool Model**: All tenants share the same tables, restricted by `tenantId` (easiest, cheapest, best for most SaaS).
- **Silo Model**: Separate database or schema per tenant (highest security, compliance heavy).

Constraints:
- Every query MUST explicitly require `tenantId`. Never query raw tables globally unless in an Admin context.
- Leverage Row-Level Security (RLS) in PostgreSQL if supported (e.g., via Supabase/Neon).

Required Output Format: Provide complete code for:
1. Schema modification showing `tenantId` relationships and composite keys.
2. A generic repository wrapper that automatically injects `tenantId` into Prisma or Drizzle queries.
3. Organization switching logic in the UI and session.
```

✅ **Verification Checklist:**
- [ ] Querying a resource without a `tenantId` should trigger a TypeScript error or runtime failure.

---
📎 **Related Phases:**
- Prerequisites: [Phase 5: Frontend Development](./PHASE_5_FRONTEND_DEVELOPMENT_Frontend_Developer.md)
- Proceeds to: [Phase 7: Testing & QA](./PHASE_7_TESTING_QA__Testing_Engineer.md)
