# Server Actions Security Rules

> **MANDATORY**: These rules apply to ALL Server Actions in every project. Violations are CRITICAL severity and must block code review.

## Rule 1: Authorization First

Every Server Action MUST check user authorization as its FIRST operation, before any business logic.

```typescript
// ✅ CORRECT
'use server';
export async function deletePost(postId: string) {
  const session = await auth();                    // Line 1: Auth check
  if (!session?.user) throw new Error('Unauthorized');
  // ... business logic
}

// ❌ WRONG — Auth check is missing
'use server';
export async function deletePost(postId: string) {
  const post = await db.post.delete({ where: { id: postId } });
  return post;
}
```

## Rule 2: Input Validation with Zod

Every Server Action MUST validate ALL inputs using Zod schemas. Never trust client data.

```typescript
// ✅ CORRECT
'use server';
const UpdateProfileSchema = z.object({
  name: z.string().min(2).max(100),
  bio: z.string().max(500).optional(),
});

export async function updateProfile(rawInput: unknown) {
  const session = await auth();
  if (!session?.user) throw new Error('Unauthorized');
  
  const input = UpdateProfileSchema.safeParse(rawInput);
  if (!input.success) return { success: false, error: input.error.flatten() };
  // ... update logic
}
```

## Rule 3: Ownership Verification

For user-owned resources, ALWAYS verify the authenticated user owns the resource before mutating.

```typescript
// ✅ CORRECT
export async function editComment(commentId: string, content: string) {
  const session = await auth();
  if (!session?.user) throw new Error('Unauthorized');
  
  const comment = await db.comment.findUnique({ where: { id: commentId } });
  if (comment?.userId !== session.user.id) throw new Error('Forbidden');
  // ... update logic
}
```

## Rule 4: Role-Based Access Control (RBAC)

Admin-only actions MUST explicitly check the user's role.

```typescript
// ✅ CORRECT
export async function banUser(userId: string) {
  const session = await auth();
  if (!session?.user) throw new Error('Unauthorized');
  if (session.user.role !== 'ADMIN') throw new Error('Forbidden');
  // ... ban logic
}
```

## Rule 5: No Raw Error Exposure

Server Actions must NEVER expose raw error messages, stack traces, or database errors to the client.

```typescript
// ✅ CORRECT
export async function createOrder(input: OrderInput) {
  try {
    // ... logic
    return { success: true, data: order };
  } catch (error) {
    Sentry.captureException(error);
    return { success: false, error: 'Failed to create order' };
  }
}

// ❌ WRONG — Exposes internal error
export async function createOrder(input: OrderInput) {
  const order = await db.order.create({ data: input }); // Raw Prisma error leaks
  return order;
}
```

## Rule 6: Rate Limiting

Sensitive Server Actions (login, registration, password reset, payment) MUST implement rate limiting.

## Summary Checklist

Before approving any Server Action:

- [ ] `'use server'` directive present
- [ ] Auth check is the FIRST line of logic
- [ ] Inputs validated with Zod `.safeParse()`
- [ ] Ownership verified for user-owned resources
- [ ] Admin actions check `role === 'ADMIN'`
- [ ] Errors caught and sanitized (no raw throws)
- [ ] Rate limiting on sensitive operations
- [ ] `revalidatePath` or `revalidateTag` called after mutations
