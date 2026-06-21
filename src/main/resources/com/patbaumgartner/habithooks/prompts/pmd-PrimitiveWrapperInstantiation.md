Constructing primitive wrapper objects with `new` (for example `new Integer(0)` or
`new Boolean(true)`) is deprecated since Java 9. It always allocates a fresh object and
skips the caching that the factory methods provide.

**How to fix it:**

1. **Autobox** — assign the primitive directly (`Integer i = 0;`).
2. **Use `valueOf`** when you need an explicit conversion (`Integer.valueOf(0)`), which may
   reuse cached instances.
3. **Prefer the named constants** `Boolean.TRUE` / `Boolean.FALSE` for booleans.

```java
// Smell
Integer zero = new Integer(0);
Boolean yes = new Boolean(true);

// Better
Integer zero = 0;
Boolean yes = Boolean.TRUE;
```

The factory and autoboxing forms are clearer and avoid needless allocations.
