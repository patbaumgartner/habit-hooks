# Agent integration

habit-hooks is designed to give coding agents short, enforceable feedback loops
instead of long instruction files that are easy to forget.

## Command order

Use this order for substantial changes:

```bash
habit-hooks doctor
habit-hooks --all
habit-hooks report --no-fail
habit-hooks tasks --no-fail
```

For small changes, `habit-hooks --all` is the minimum completion gate. If the
installed command is unavailable, install habit-hooks or use a project-local
launcher JAR when the repository carries one:

```bash
java -jar target/habit-hooks-*-launcher.jar --all
```

`habit-hooks init` writes an `AGENTS.md` file when one does not already exist.
For Spring Boot projects, prefer `habit-hooks init --spring-boot` so the agent
guide, `.habit-hooks.yaml`, Taikai architecture test, and Maven snippets all
describe the same analyzer surface.

## Interpretation rules

- Treat every habit-hooks finding as actionable user feedback.
- Fix findings before moving to unrelated work.
- Use `habit-hooks tasks --format json --no-fail` when another agent needs a
  structured queue.
- Use `habit-hooks report --format html --no-fail` when a human needs a quick
  local dashboard.
- Do not snooze a baseline entry unless the user explicitly asks for that.

## Task JSON workflow

Each task groups current findings by rule and includes:

- `id`: stable task number for this export
- `ruleId`: analyzer rule to fix
- `dimension`: maintainability, correctness, supply-chain, architecture, or test-signal
- `verificationCommand`: command to rerun after fixing
- `acceptanceCriteria`: concise completion checklist
- `locations`: first affected locations, capped for readability

Agents should work tasks in export order, rerun the verification command after
each batch, and regenerate tasks only after the current batch is fixed.

Example task export:

```json
[
  {
    "id": "HH-001",
    "title": "Fix pmd:GodClass",
    "ruleId": "pmd:GodClass",
    "dimension": "maintainability",
    "severity": "low",
    "count": 1,
    "verificationCommand": "habit-hooks --all",
    "acceptanceCriteria": [
      "Resolve all current findings for pmd:GodClass.",
      "Keep the change focused and behavior-preserving unless the finding exposes a real bug.",
      "Re-run habit-hooks --all and confirm the rule no longer appears."
    ],
    "locations": ["src/main/java/com/example/OrderService.java:42"]
  }
]
```

See [Artifact contracts](artifacts.md) for the stable report and task shapes.

## Spring Boot agent loop

Spring Boot projects usually need Maven-backed analyzers ready before the agent
can trust a full run. After copying snippets into `pom.xml`, use this loop:

```bash
./mvnw --batch-mode --no-transfer-progress spring-javaformat:apply
habit-hooks doctor
habit-hooks --all
habit-hooks report --format html --no-fail
habit-hooks tasks --format json --no-fail
```

If `doctor` reports missing Maven profiles or dependencies, fix the build setup
before changing application code.

## End-to-end fix walkthrough

A complete agent loop, from first run to a clean gate:

1. **Run the gate** and capture the coached output.

   ```bash
   habit-hooks --all
   ```

   ```text
   ❌ Habit Hooks: 2 violations

   ❌ Useless Parentheses
   Parentheses that do not change evaluation order add visual noise...

   Violations:
     src/main/java/com/example/Report.java:105
       Useless parentheses around `value * scale`.
   ```

2. **Export a structured queue** so the work is ordered and traceable.

   ```bash
   habit-hooks tasks --format json --no-fail > habit-hooks-tasks.json
   ```

3. **Understand a rule on demand** before touching code.

   ```bash
   habit-hooks explain pmd:UselessParentheses
   ```

   This prints the same coaching shown inline, without needing a fresh
   violation to trigger it.

4. **Fix one task**, keeping the change focused and behavior-preserving.

5. **Re-run the verification command** from the task (`verificationCommand`,
   usually `habit-hooks --all`) and confirm the rule no longer appears.

6. **Regenerate tasks** only after the current batch is clean, then repeat
   until the gate passes:

   ```text
   ✅ Habit Hooks: all checks passed.
   ```

Work tasks in export order and never start unrelated work while a coached
finding is still open.

## Scaffolding the agent guide only

When a repository already has Checkstyle/PMD configuration and you only want the
agent operating guide, scaffold `AGENTS.md` on its own:

```bash
habit-hooks init --agents
```

This writes `AGENTS.md` (Spring Boot variant when combined with
`--spring-boot`) and leaves every other file untouched.
