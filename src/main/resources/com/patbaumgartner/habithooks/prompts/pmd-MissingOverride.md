A method that overrides one from a supertype should carry the `@Override` annotation. The
annotation lets the compiler verify that you really are overriding something, so a typo in
the name or a changed signature becomes a compile error instead of a silent bug.

**How to fix it:**

1. **Add `@Override`** to every method that implements an interface method or overrides a
   superclass method.
2. **Let the compiler check intent** — if it complains, the method does not actually
   override anything and the mismatch is now visible.
3. **Configure your IDE** to add the annotation automatically when generating overrides.

```java
// Smell: looks like an override but isn't verified
public String toString() { ... }

// Better
@Override
public String toString() { ... }
```

`@Override` documents intent and protects against refactoring mistakes at no runtime cost.
