The configured Maven run stopped before the PIT analyzer goal started. Mutation results from an earlier run may still exist, but they are not fresh evidence for the current project state.

**How to fix it:**

1. Inspect the Maven output referenced by habit-hooks.
2. Fix the first lifecycle failure, such as formatting, compilation, or tests.
3. Re-run habit-hooks so PIT can execute mutation analysis.

Treat the first Maven failure as the blocker; do not chase downstream analyzer symptoms until it is gone.