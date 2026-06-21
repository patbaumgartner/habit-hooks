Building a log message with string concatenation or eager method calls runs that work even
when the log level is disabled. For hot paths or expensive arguments this wastes CPU
producing text that is immediately discarded.

**How to fix it:**

1. **Use parameterized logging** — pass `{}` placeholders and arguments so the message is
   only assembled when the level is enabled.
2. **Use lambda/supplier forms** for genuinely expensive arguments
   (`log.debug("x: {}", () -> expensive())`).
3. **Guard with `isDebugEnabled()`** only when an argument is costly to compute and no
   supplier form is available.

```java
// Smell: string is built every call, even when debug is off
log.debug("processed " + count + " items for " + user.describe());

// Better: arguments evaluated lazily by the framework
log.debug("processed {} items for {}", count, user);
```

Parameterized logging keeps disabled log statements essentially free without scattering
manual level guards through the code.
