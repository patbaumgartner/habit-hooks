Comparing objects with `==` or `!=` tests reference identity, not value equality. Two
distinct objects that are logically equal will compare as different, which is a classic
source of subtle bugs.

**How to fix it:**

1. **Use `equals()`** for value comparison of objects.
2. **Use `Objects.equals(a, b)`** when either side may be `null`.
3. **Reserve `==`** for primitives, enum constants, and intentional identity checks against
   sentinel constants.

```java
// Smell: reference comparison of two String/object values
if (name == other) { ... }

// Better
if (Objects.equals(name, other)) { ... }
```

Reference comparison is rarely what you mean for ordinary objects — make value comparison
explicit with `equals()`.
