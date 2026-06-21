Calling `x.equals(null)` to test for null is misleading and wasteful: a correct `equals`
implementation always returns `false` for a `null` argument, so the call can only ever say
"not null" — and it throws `NullPointerException` if `x` itself is null.

**How to fix it:**

1. **Use `==`** to compare against `null`.
2. **Use `Objects.isNull` / `Objects.nonNull`** if you prefer a method form.
3. **Reach for `Optional`** when a value's absence is part of the design.

```java
// Smell
if (value.equals(null)) { ... }

// Better
if (value == null) { ... }
```

The `==` operator is the only reliable, NPE-safe way to test a reference for null.
