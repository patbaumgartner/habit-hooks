The configured Maven run stopped before the CPD analyzer goal started. This usually means an
earlier lifecycle phase failed, so a duplication report would be misleading or stale.

**How to fix it:**

1. Inspect the Maven output referenced by habit-hooks.
2. Fix the first lifecycle failure, such as formatting, compilation, or tests.
3. Re-run habit-hooks so CPD can generate fresh evidence.

Treat the first Maven failure as the blocker; do not chase downstream analyzer symptoms
until it is gone.
