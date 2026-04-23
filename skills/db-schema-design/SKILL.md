---
name: db-schema-design
description: Database schema design patterns and validation checklist for Prisma ORM with PostgreSQL. Ensures indexing, relations, RLS, and migration safety.
---

# Database Schema Design Skill

Reusable knowledge for designing production-grade database schemas with Prisma ORM and PostgreSQL.

## Schema Design Checklist

Before finalizing any schema, verify every item:

### Models
- [ ] Every model has `id` (cuid/uuid), `createdAt`, `updatedAt`
- [ ] User-owned models have `userId` with `@relation` and `onDelete`
- [ ] Soft-delete (`deletedAt DateTime?`) added where business logic requires it
- [ ] Model names are PascalCase singular (`User`, not `Users`)

### Fields
- [ ] No `String` used where `enum` is appropriate
- [ ] `@unique` on natural keys (email, slug, username)
- [ ] `@default` values set for status fields and timestamps
- [ ] Optional fields (`?`) only when truly nullable

### Indexes
- [ ] `@@index` on every foreign key field
- [ ] `@@index` on fields used in `WHERE` clauses
- [ ] `@@index` on fields used in `ORDER BY` clauses
- [ ] Composite `@@index([fieldA, fieldB])` for multi-column queries
- [ ] `@@unique` constraints for business-rule uniqueness (e.g., one vote per user per post)

### Relations
- [ ] All relations use explicit `fields` and `references`
- [ ] Many-to-many uses explicit join table (not implicit)
- [ ] Join tables include metadata (`createdAt`, `role`, etc.)
- [ ] `onDelete` behavior explicitly set (`Cascade`, `Restrict`, `SetNull`)

### Security
- [ ] RLS policies documented for multi-tenant access
- [ ] Sensitive fields excluded from default query selects
- [ ] Admin-only models flagged with comments

## Common Patterns

### Soft Delete
```prisma
model Post {
  id        String    @id @default(cuid())
  deletedAt DateTime?
  // Query filter: where: { deletedAt: null }
}
```

### Polymorphic Relations (via Enum)
```prisma
enum NotificationType {
  COMMENT
  LIKE
  FOLLOW
}

model Notification {
  id       String           @id @default(cuid())
  type     NotificationType
  targetId String           // Points to Comment, Like, or User depending on type
  @@index([targetId])
}
```

### Audit Trail
```prisma
model AuditLog {
  id        String   @id @default(cuid())
  userId    String
  action    String   // "CREATE", "UPDATE", "DELETE"
  model     String   // "User", "Post", etc.
  recordId  String
  changes   Json?    // { field: { old, new } }
  createdAt DateTime @default(now())
  user      User     @relation(fields: [userId], references: [id])
  @@index([userId])
  @@index([model, recordId])
}
```

## Anti-Patterns to Reject

| Anti-Pattern | Why It's Bad | Fix |
|---|---|---|
| `@@map("users")` without reason | Hides intent | Only use for legacy DB mapping |
| Implicit many-to-many | No metadata, no control | Explicit join table |
| `String` for status/role | Typos, no validation | Use `enum` |
| Missing `@@index` on FK | Slow JOINs at scale | Always index FKs |
| `onDelete: Cascade` everywhere | Accidental data loss | Default to `Restrict` |
| >15 fields on one model | God object | Normalize into related models |
