# Habit Hooks - Agent Operating Guide

## Required quality step

Before considering any coding task complete, run habit-hooks:

```bash
habit-hooks
```

If `habit-hooks` is not installed globally in this repository, use:

```bash
java -jar target/habit-hooks-*-launcher.jar
```

Use `--all` when you need a full-repository pass.

For larger or cross-cutting changes, run the setup check first:

```bash
habit-hooks doctor
```

After a failing run, generate focused follow-up artifacts when useful:

```bash
habit-hooks report --no-fail
habit-hooks tasks --no-fail
```

## Non-negotiable rules

- Never ignore habit-hooks output.
- Create a task for each reported violation immediately.
- Complete required follow-up actions before unrelated work.
- Never snooze or bypass baseline entries without explicit user approval.
- Never disable rules in `checkstyle.xml` or `pmd-ruleset.xml` just to silence violations.

## Output interpretation

Each coached section starts with `❌` and contains:

1. The smell
2. The coaching
3. Violations (file, line, message)

`Uncoached rules` still require remediation. They are not optional.

`habit-hooks tasks` groups findings by rule and includes acceptance criteria,
locations, and the verification command. Treat each generated task as actionable
work, not as advisory text.

## Clean run

```text
✅ Habit Hooks: all checks passed.
```

If Taikai architecture tests are configured, also run:

```bash
./mvnw -q test -Dtest=ArchitectureTest
```

Or enable the built-in Taikai analyzer in `.habit-hooks.yaml` to have
habit-hooks run it automatically:

```yaml
analyzers:
  taikai:
    enabled: true
    testClass: ArchitectureTest
```

## Maintainer reference

```bash
./mvnw spring-javaformat:apply
./mvnw clean verify
./mvnw package -DskipTests
```

Local launcher runs:

```bash
java -jar target/habit-hooks-*-launcher.jar --all
java -jar target/habit-hooks-*-launcher.jar report --format html --no-fail
java -jar target/habit-hooks-*-launcher.jar tasks --format json --no-fail
java -jar target/habit-hooks-*-launcher.jar --help
```

## Code conventions

- Java 25 baseline
- Max method length: 25
- Max parameters: 5
- Max cyclomatic complexity: 8
- Prefer `Optional` or empty collections over `null` returns
- Public API should have Javadoc
- Formatting enforced by Spring Java Format

## Adding coached rules

1. Add the rule to `checkstyle.xml` or `pmd-ruleset.xml`.
2. Add prompt file at `src/main/resources/com/patbaumgartner/habithooks/prompts/<tool>-<RuleName>.md`.
3. Register the title in `RuleTitles.TITLES`.
4. Add or update tests in `CoachingEngineTest`.

## Baseline operations

```bash
java -jar target/habit-hooks-*-launcher.jar --all
java -jar target/habit-hooks-*-launcher.jar --all baseline snooze
java -jar target/habit-hooks-*-launcher.jar baseline prune
```
