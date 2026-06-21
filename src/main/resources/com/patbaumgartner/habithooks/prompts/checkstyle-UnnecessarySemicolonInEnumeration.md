An enum's constant list does not need a trailing semicolon when no members follow it. The
stray `;` is easy to leave behind after editing and adds punctuation that carries no meaning.

**How to fix it:**

1. **Remove the trailing semicolon** when the enum declares only constants.
2. **Keep the semicolon** only when the enum also declares fields, constructors, or methods.
3. **Re-check after deleting** the last enum member or method during refactors.

```java
// Smell: redundant semicolon after the constants
enum Status {
    ACTIVE, INACTIVE;
}

// Better
enum Status {
    ACTIVE, INACTIVE
}
```

Dropping the redundant separator keeps simple enums clean and signals when a semicolon is
actually doing work.
