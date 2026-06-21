The configured Maven run stopped before the Spring Java Format analyzer goal started. That means formatter feedback was blocked by an earlier Maven failure, not by ordinary formatting drift.

**How to fix it:**

1. Inspect the Maven output referenced by habit-hooks.
2. Fix the first lifecycle failure before interpreting formatter output.
3. Re-run habit-hooks so Spring Java Format can validate the current sources.

Treat the first Maven failure as the blocker; do not chase downstream analyzer symptoms until it is gone.