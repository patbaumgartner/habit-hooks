The configured Maven run stopped before the CycloneDX analyzer goal started. An SBOM from a previous run may still exist, but it does not prove the current build can produce supply-chain evidence.

**How to fix it:**

1. Inspect the Maven output referenced by habit-hooks.
2. Fix the first lifecycle failure, such as formatting, compilation, or tests.
3. Re-run habit-hooks so CycloneDX can generate a fresh SBOM.

Treat the first Maven failure as the blocker; do not chase downstream analyzer symptoms until it is gone.