This ternary expression can be simplified. Returning or assigning a boolean through
`condition ? true : false` (or its inverse) adds noise without adding meaning.

**How to fix it:**

1. **Return the condition directly** when the branches are `true`/`false`.
2. **Negate the condition** instead of swapping the branches when they are `false`/`true`.
3. **Drop redundant ternaries** where one branch repeats the condition.

```java
// Before
boolean active = (status == OPEN) ? true : false;
boolean closed = (status == OPEN) ? false : true;

// After
boolean active = status == OPEN;
boolean closed = status != OPEN;
```

Simpler expressions read as plain statements of intent and remove an easy place for bugs
to hide.
