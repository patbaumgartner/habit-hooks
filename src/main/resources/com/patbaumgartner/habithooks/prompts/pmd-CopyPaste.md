Copy-pasted code blocks duplicate logic across the codebase. When the same block
appears in several places, every future change has to be made — and tested — in each
copy, and it is easy to fix one and forget the others.

**How to fix it:**

1. **Extract the shared block** into a single well-named method.
2. **Promote common behaviour** to a shared class, base type, or utility when the
   duplication spans multiple classes.
3. **Parameterize the differences** so one implementation serves every call site.
4. **Delete the copies** and route all callers through the single source of truth.

```java
// Smell: the same validation copied into two methods
void createUser(String email) {
    if (email == null || !email.contains("@")) throw new IllegalArgumentException();
    // ...
}
void updateUser(String email) {
    if (email == null || !email.contains("@")) throw new IllegalArgumentException();
    // ...
}

// Better: one source of truth
private static void requireEmail(String email) {
    if (email == null || !email.contains("@")) throw new IllegalArgumentException();
}
```

One implementation means one place to fix bugs and one place to evolve behaviour.
