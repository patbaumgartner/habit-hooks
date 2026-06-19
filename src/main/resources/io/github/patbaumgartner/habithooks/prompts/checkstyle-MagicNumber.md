Magic numbers are unnamed numeric literals embedded in logic. The reader cannot tell
what `42`, `86400`, or `0.15` represents without external context. When the number
changes (and it will), finding and updating all occurrences is error-prone.

**How to fix it:**

Replace the literal with a named constant:

```java
// Before
if (retries > 3) { ... }
double fee = amount * 0.15;

// After
private static final int MAX_RETRIES = 3;
private static final double PROCESSING_FEE_RATE = 0.15;

if (retries > MAX_RETRIES) { ... }
double fee = amount * PROCESSING_FEE_RATE;
```

The name documents intent; the constant centralises change; the compiler ensures
consistency. Common exceptions: `-1`, `0`, `1`, `2` in well-understood idioms.
