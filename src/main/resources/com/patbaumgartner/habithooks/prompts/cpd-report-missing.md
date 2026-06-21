The CPD goal failed and no XML report was produced. That usually points to a build, plugin,
or classpath problem rather than a normal duplication finding.

**How to fix it:**

1. Run the configured CPD goal directly.
2. Fix the first Maven or plugin error that prevents report generation.
3. Re-run habit-hooks so duplication findings can be normalized into actionable feedback.

Do not disable the analyzer just to silence this. Missing feedback is itself feedback.
