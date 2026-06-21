When comparing a variable against a string (or other) literal, put the literal on the left
side of `equals()`. If the variable is `null`, the comparison then safely returns `false`
instead of throwing a `NullPointerException`.

**How to fix it:**

1. **Flip the comparison** so the constant calls `equals()`.
2. **Apply it to `equalsIgnoreCase` and `contentEquals`** too.
3. **Or use `Objects.equals(a, b)`** when both sides are variables.

```java
// Smell: throws NPE if name is null
if (name.equals("admin")) { ... }

// Better: null-safe
if ("admin".equals(name)) { ... }
```

A literal is never null, so leading with it removes a whole class of null-pointer bugs.
