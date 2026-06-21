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
installed command is unavailable, use the launcher JAR from the repository root:

```bash
java -jar target/habit-hooks-*-launcher.jar --all
```

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
