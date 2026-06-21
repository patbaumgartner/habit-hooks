A class with too many methods is often a hidden God Class — it has accumulated behaviour
that belongs in several smaller, more focused types.

**How to fix it:**

1. **Find cohesive method groups** — methods that operate on the same subset of fields
   usually belong together in their own class.
2. **Extract collaborators** — move each group into a focused class and let the original
   class delegate to it.
3. **Separate concerns** — keep parsing, validation, formatting, and persistence in
   distinct types rather than one catch-all class.

```java
// Smell: ReportService has 20+ methods for loading, computing, formatting, exporting

// Better:
class ReportLoader { ... }
class ReportCalculator { ... }
class ReportFormatter { ... }
class ReportService { /* thin orchestration */ }
```

A long method list is a prompt to ask "what are the real responsibilities here?" and to
draw boundaries between them.
