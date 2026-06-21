The configured Maven run stopped before OWASP Dependency Check started. Vulnerability reports from previous runs may still exist, but they are not fresh evidence for the current dependency graph.

**How to fix it:**

1. Inspect the Maven output referenced by habit-hooks.
2. Fix the first lifecycle failure, such as formatting, compilation, or tests.
3. Re-run habit-hooks so OWASP Dependency Check can generate fresh vulnerability evidence.

Treat the first Maven failure as the blocker; do not chase downstream analyzer symptoms until it is gone.