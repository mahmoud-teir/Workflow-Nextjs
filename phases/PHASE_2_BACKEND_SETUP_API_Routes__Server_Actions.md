<a name="phase-2"></a>
# 📌 PHASE 2: BACKEND SETUP — API ROUTES & SERVER ACTIONS (Backend Developer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 2.1: Database & ORM Setup

```text
You are a Senior Backend Developer specializing in scalable serverless and edge database architectures. Set up the database and ORM for my Next.js application.

Database: [PostgreSQL with Neon / Supabase / PlanetScale / MongoDB / Convex]
ORM: [Prisma / Drizzle ORM]

Constraints:
- The database connection must pool efficiently (avoiding max connection limits in serverless).
- Avoid `any` types; everything must be strongly typed.
- Must include a robust connection health-check utility.

Decision Guide:
- **Use Prisma if:** You want speed of development, a robust migration system, and are okay with slightly larger bundle sizes and utilizing Prisma Accelerate for Edge.
- **Use Drizzle if:** You want maximum performance, zero dependencies at the edge, and SQL-like syntax.
- **Use Convex if:** You need built-in real-time capabilities and prefer not managing a separate database/ORM layer.

Required Output Format: Provide complete code for:
1. Connection String Config (`.env` setup)
2. Schema structure (Prisma `.prisma` file or Drizzle schema structure)
3. Database Client instantiation (`lib/db.ts`) with global singleton to prevent exhausting connections during hot-reloads.
4. Database health-check utility (`lib/db-health.ts`).
5. Migration & Setup scripts for `package.json`:
   - `build`: "prisma generate && next build"
   - `postinstall`: "prisma generate"

⚠️ Common Pitfalls:
- **Pitfall:** Creating a new database connection on every HMR reload in development.
- **Solution:** Use the `globalThis` singleton pattern in `lib/db.ts` to cache the connection.
```

✅ **Verification Checklist:**
- [ ] Run the chosen ORM generate command (e.g., `npx prisma generate`).
- [ ] Verify `globalThis` caching is present in `lib/db.ts`.

---

### Prompt 2.2: Route Handlers (API Routes)

```text
You are an API Architect implementing robust Next.js Route Handlers.

Project: [ProjectName]
Database: [your database]

Constraints:
- Route Handlers must strictly type their request params and payloads.
- All incoming request bodies MUST be validated with Zod before processing.
- Do NOT place heavy business logic directly in the route; delegate to a service layer/repository.

Required Output Format:
1. Provide a generic API utility file (`lib/api-utils.ts`) that standardizes error and success responses.
2. Provide an example Health endpoint (`app/api/health/route.ts`).
3. Provide a type-safe higher-order function (middleware wrapper) for catching Zod/App errors automatically.
4. Detail the API versioning strategy (e.g., `/api/v1/resource` vs Header-based).

⚠️ Common Pitfalls:
- **Pitfall:** `searchParams` and `params` being synchronous in Next.js 14, but asynchronous in Next.js 15+.
- **Solution:** Always `await params` and `await searchParams` sequentially at the start of your route handler.
```

✅ **Verification Checklist:**
- [ ] Create a dummy endpoint and hit it with invalid data; ensure it returns a 422 JSON validation error via Zod, not a 500 server crash.

---

### Prompt 2.3: Server Actions (Primary Mutation Pattern)

```text
You are a Senior Full-Stack Developer. Create Server Actions for data mutations in the Next.js application.

Server Actions are the PRIMARY approach for mutations. Prefer them over API routes for form submissions.

Forms Library: [React Hook Form / Conform / native form actions]

Constraints:
- EVERY Server Action must begin with `'use server'`.
- EVERY Server Action must authorize the user session before taking action.
- EVERY Server Action must validate inputs via Zod.
- Action results must return a discriminated union (`{ success: true, data: T } | { success: false, error: string, fieldErrors: ... }`).

Required Output Format: Provide complete code for:
1. A shared utility for type-safe Server Actions (`lib/action-utils.ts`) including auth enforcement.
2. A user creation action (`app/actions/user.ts`) demonstrating validation and path revalidation (`revalidatePath`).
3. A client component form (`components/forms/create-user-form.tsx`) integrating the Server Action using React 19's `useActionState` and `useOptimistic`.
4. Demonstrate background tasks using the Next.js `after()` API (e.g., logging analytics after publishing).
5. **MUST INCLUDE**: The complete set of Server Actions required for the Admin Dashboard (e.g., managing users, toggling settings, deleting content) with explicit Role-Based Access Control (RBAC) ensuring only Admins can execute them.

⚠️ Common Pitfalls:
- **Pitfall:** Throwing raw errors inside a Server Action. They will bubble up to the nearest `error.tsx` barrier, disrupting the UI.
- **Solution:** Catch errors internally and return a structured `{ success: false, error: 'Reason' }` object.
```

✅ **Verification Checklist:**
- [ ] Create a form that invokes the Server Action.
- [ ] Submit invalid data; UI displays field errors.
- [ ] Submit valid data; database is updated and `revalidatePath` successfully refreshes the cached view.

---
📎 **Related Phases:**
- Prerequisites: [Phase 1: Project Structure & Config](./PHASE_1_PROJECT_STRUCTURE__CONFIGURATION_Full-Stack_Developer.md)
- Proceeds to: [Phase 3: Database Models](./PHASE_3_DATABASE_MODELS__INTEGRATION_Database_Architect.md)
