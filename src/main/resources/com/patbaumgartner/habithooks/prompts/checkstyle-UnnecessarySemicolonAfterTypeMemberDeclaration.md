A lone semicolon after a field or method declaration is an empty statement, not part of the
declaration. It compiles, but it is dead punctuation that clutters the type body and can hide
copy-paste mistakes.

**How to fix it:**

1. **Delete the stray semicolon** that follows a complete member declaration.
2. **Watch for leftovers** after removing a field initializer or method body during edits.
3. **Treat the warning as a cleanup cue** rather than suppressing it.

```java
// Smell: empty statement after the field
private final int max = 100;;

// Better
private final int max = 100;
```

Removing empty statements keeps the type body free of meaningless tokens.
