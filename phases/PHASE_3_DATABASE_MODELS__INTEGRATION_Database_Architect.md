<a name="phase-3"></a>
# 📌 PHASE 3: DATABASE MODELS & INTEGRATION (Database Architect)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 3.1: Design Database Schema

```text
You are a Staff Data Architect. Your expertise lies in designing robust, normalized, and highly performant database schemas for modern edge/serverless web environments. Design the complete database schema for [ProjectName].

Database: [PostgreSQL (Neon Serverless / Supabase) / MySQL (PlanetScale) / MongoDB / Convex]
ORM: [Prisma / Drizzle ORM / Convex schema]

Constraints:
- Use UUIDs or CUIDs for primary keys. Explain why auto-incrementing integers are bad in distributed systems.
- Every model MUST have `createdAt` and `updatedAt`.
- Include soft delete capabilities (`deletedAt`) where appropriate to meet data retention requirements without losing historical audit trails.
- Enforce strict referential integrity.
- Account for Edge Runtime if the DB driver needs it.

Required Output Format: Provide complete code and documentation:
1. Complete Schema Definition file (e.g., `schema.prisma` or `schema.ts` for Drizzle).
2. Data Modeling Summary: Explain the 3 most complex table relationships and why you modeled them this way.
3. Indexes: Highlight every explicit index/composite index mapped out and the query pattern it speeds up.
4. Schema Visualization format (e.g., provide a Mermaid.js Entity-Relationship diagram).

⚠️ Common Pitfalls:
- **Pitfall:** Using large integers for IDs when running serverless ORMs, creating migration sequence headaches.
- **Solution:** Rely exclusively on `uuid` or `cuid`.
```

✅ **Verification Checklist:**
- [ ] Schema successfully parses in the ORM without circular dependency errors.
- [ ] Run schema visualization (like Prisma Studio) and verify relationship cardinalities (1:1, 1:N, N:M).

---

### Prompt 3.2: Create Service Layer (Repository Pattern)

```text
You are a Backend Architect designing the abstraction layer between the database and application logic.

Constraints:
- Do NOT leak ORM-specific (Prisma/Drizzle) typings into the frontend UI layer. Extract data into raw TS types.
- Ensure all business logic flows through a central repository to guarantee consistent access rules.
- Caching logic must wrap these service functions cleanly using Next.js caching primitives (`cache()`, `unstable_cache`, or Next 15 `cacheLife`).

Required Output Format:
1. `lib/repositories/base.ts`: A generic base repository class/functions (find, create, update, softDelete).
2. `lib/services/user-service.ts`: Highly specific implementations using Next.js deduplication caches.
3. Type-safe Pagination logic: Document exactly how offset or cursor-based pagination is implemented in this service.
4. Error handling patterns per repository method (do not just bubble up raw DB connection errors).

⚠️ Common Pitfalls:
- **Pitfall:** Frontend components receiving a Prisma Object and throwing serialization errors across the client boundary.
- **Solution:** Service layer must always return plain, serializable objects. Remove `Date` instances if passing directly to Server Actions (serialize them to ISO strings).
```

✅ **Verification Checklist:**
- [ ] Write a unit test against the Service Layer that mocks the ORM completely.
- [ ] Ensure that a heavy query called twice in the same render tree only hits the database once (verify React `cache` deduplication).

---

### Prompt 3.3: Data Validation & Transformation

```text
You are a Data Quality Engineer. Construct the ironclad validation schemas that sit between the user layer and the service layer.

Constraints:
- Every data mutation MUST have an associated Zod schema.
- Error messages must be user-friendly and ready for internationalization (i18n).
- Validate strictly; strip extraneous keys automatically.

Required Output Format: Provide `lib/validations/core.ts` with:
1. Reusable primitives (e.g., `emailSchema`, `passwordSchema`).
2. Input sanitization patterns (e.g., trimming strings, coercing numbers).
3. Pagination query schemas that strongly type `page`, `limit`, `sortBy`. 
4. Custom Zod refinements (e.g., password must contain special chars, or end-date > start-date).

⚠️ Common Pitfalls:
- **Pitfall:** Validating a Zod schema but forgetting to `.trim()` or `.toLowerCase()` emails, causing login case-sensitivity issues.
- **Solution:** Bake `.trim().toLowerCase()` directly into the base Zod string schema.
```

✅ **Verification Checklist:**
- [ ] Run schema parsing on corrupted payload; assert it gracefully throws a Zod Error with field-level details.

---

### Prompt 3.4: Database Seeding & Migrations

```text
You are a DevOps Database Admin. Establish the processes for rolling out database changes safely from local dev to production.

Constraints:
- Seeds must be idempotent (safe to run multiple times without duplicating data).
- Migrations must never use destructive commands (like `drop column`) in a single step in production.
- Keep seed scripts modular so tests can request specific seeded state.

Required Output Format: 
1. Database Seed Script (`seed.ts`) built with `faker.js`.
2. Safe Production Migration Checklist.
3. Database Branching methodology (e.g., Neon Branching flow for preview deployments).
4. Outline how to handle a rollback if a migration fails mid-deployment.

⚠️ Common Pitfalls:
- **Pitfall:** `prisma db push` used in production, overriding schemas unsafely.
- **Solution:** Script explicitly separate commands: `migrate dev` for local, `migrate deploy` for CI/CD.
```

✅ **Verification Checklist:**
- [ ] Run `npm run db:seed`.
- [ ] Run `npm run db:seed` a second time — no errors and no duplicated baseline data (idempotency check passed).
- [ ] Verify `package.json` contains `postinstall: prisma generate` and `build: prisma generate && next build`.

---
📎 **Related Phases:**
- Prerequisites: [Phase 2: Backend Setup](./PHASE_2_BACKEND_SETUP_API_Routes__Server_Actions.md)
- Proceeds to: [Phase 4: Authentication](./PHASE_4_AUTHENTICATION__AUTHORIZATION_Security_Expert.md)
