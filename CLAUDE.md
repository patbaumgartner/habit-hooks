# Habit Hooks

## Agent Integration — copy this into your CLAUDE.md or AGENTS.md

```markdown
## Habit Hooks

habit-hooks wraps Checkstyle and PMD to give Java AI coding agents real-time
coaching on structural code quality.

When `habit-hooks` is available in the project, run it before considering any
coding task complete:

```bash
java -jar habit-hooks.jar
```

### Rules

- **NEVER** ignore habit-hooks output
- **ALWAYS** create a task for each reported violation immediately
- **COMPLETE** all required actions before continuing other work
- **NEVER** snooze or bypass the baseline without explicit user approval
- **NEVER** disable rules in checkstyle.xml / pmd-ruleset.xml to silence a violation

### How to interpret output

Each coaching section starts with ❌ and includes:

1. **The smell** — what structural problem was detected
2. **The coaching** — why it matters and how to fix it
3. **The violations** — exact file, line, and message

A violation in the **Uncoached rules** section means the rule has no coaching
prompt. The rule still fires and must be fixed — add a coaching prompt to
`<prompts-dir>/<tool>-<RuleName>.md` to teach the agent about it.

### On a clean run

```
✅ Habit Hooks: all checks passed.
```

No further action needed unless an architecture review (Taikai) is also
configured, in which case run `mvn test -Dtest=ArchitectureTest`.
```

---

## Build & Development Reference

This file is for AI agents working on the habit-hooks codebase itself.

### Build

```bash
./mvnw clean verify            # compile, test, checkstyle, PMD, SpotBugs
./mvnw package -DskipTests     # build fat JAR only
```

### Run locally

```bash
java -jar target/habit-hooks-*-launcher.jar --all
java -jar target/habit-hooks-*-launcher.jar --help
```

### Code conventions

- Java 21 with records, sealed interfaces, text blocks, pattern matching
- Maximum method length: **25 lines** (enforced by Checkstyle)
- Maximum parameters: **5** (enforced by Checkstyle)
- Maximum cyclomatic complexity: **8** (enforced by Checkstyle)
- No `null` returns — use `Optional` or empty collections
- All public API surface must have Javadoc

### Adding a new coached rule

1. Add the rule to `checkstyle.xml` or `pmd-ruleset.xml`
2. Create `src/main/resources/io/github/patbaumgartner/habithooks/prompts/<tool>-<RuleName>.md`
3. Add the rule title to `RuleTitles.TITLES` in the coaching package
4. Add a test in `CoachingEngineTest`

### Baseline management

The project self-dogfoods — habit-hooks runs against its own source in CI:

```bash
# See what habit-hooks says about itself
java -jar target/habit-hooks-*-launcher.jar --all

# If onboarding pre-existing violations
java -jar target/habit-hooks-*-launcher.jar baseline snooze
```
