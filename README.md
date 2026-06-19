# habit-hooks

[![CI](https://github.com/patbaumgartner/habbit-hooks/actions/workflows/ci.yml/badge.svg)](https://github.com/patbaumgartner/habbit-hooks/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-blue)](https://maven.apache.org)

Stop reciting software engineering literature to your AI agent.

Turn best-practice advice into AI **habits** — and make it write Java code like this.

---

## What it is

AI coding agents frequently ignore long rule documents. Asking them to hold an
entire book's worth of coding advice is at best futile, at worst pollutes the
context window and degrades performance.

**habit-hooks** wraps your existing Java static analysis tools — **Checkstyle**
and **PMD** — to create the trigger, but instead of providing only a metric, it
gives actionable coaching on *why* the violation is a smell and *how* to fix it.
This creates AI behaviour that looks like human habits with similar effects.

Using habit-hooks:

- **Increases code quality** — structural smells are caught at commit time
- **Improves AI performance** — agents start from clean code and need less context
- **Reduces token usage** — clean code means less reading to understand intent
- **Encourages architecture discipline** — pairs naturally with [Taikai](#taikai-integration)

> Inspired by [devill/habit-hooks](https://github.com/devill/habit-hooks) — the original TypeScript edition.

---

## Install

Download the latest fat JAR from [Releases](https://github.com/patbaumgartner/habbit-hooks/releases):

```bash
curl -L https://github.com/patbaumgartner/habbit-hooks/releases/latest/download/habit-hooks-launcher.jar \
  -o ~/.local/bin/habit-hooks.jar

# Create a wrapper script
echo '#!/bin/sh\nexec java -jar ~/.local/bin/habit-hooks.jar "$@"' > ~/.local/bin/habit-hooks
chmod +x ~/.local/bin/habit-hooks
```

Or build from source:

```bash
./mvnw package -DskipTests
java -jar target/habit-hooks-*-launcher.jar
```

---

## Quick start

```bash
habit-hooks init
```

`init` detects existing Checkstyle/PMD configurations, scaffolds starter configs
for the missing ones, writes `.habit-hooks.yaml`, an empty baseline, and offers
to add Maven plugin snippets. Run with `--dry-run` to preview every write.

Then:

```bash
habit-hooks
```

Runs all wrapped tools against files changed since the branch base.

---

## What it catches

habit-hooks wraps your project's Checkstyle and PMD. Whatever rules those tools
fire — from your `checkstyle.xml` and `pmd-ruleset.xml` — is what habit-hooks
surfaces. What it adds on top is *why this is a smell* and *how to fix it*.

### Coached rules

| Tool       | Rule ID                          | Description                      |
|------------|----------------------------------|----------------------------------|
| checkstyle | `checkstyle:MethodLength`        | Oversized method                 |
| checkstyle | `checkstyle:ParameterNumber`     | Too many parameters              |
| checkstyle | `checkstyle:CyclomaticComplexity`| High complexity                  |
| checkstyle | `checkstyle:JavaNCSS`            | High non-commenting source lines |
| checkstyle | `checkstyle:VisibilityModifier`  | Weak encapsulation               |
| checkstyle | `checkstyle:MagicNumber`         | Magic numbers                    |
| pmd        | `pmd:ExcessiveMethodLength`      | Oversized method (PMD)           |
| pmd        | `pmd:ExcessiveParameterList`     | Too many parameters (PMD)        |
| pmd        | `pmd:CyclomaticComplexity`       | High complexity (PMD)            |
| pmd        | `pmd:GodClass`                   | Class doing too much             |
| pmd        | `pmd:TooManyFields`              | Class with too many fields       |
| pmd        | `pmd:UnusedPrivateField`         | Unused field                     |
| pmd        | `pmd:UnusedLocalVariable`        | Unused variable                  |
| pmd        | `pmd:CopyPaste`                  | Duplicated code (CPD)            |

### Uncoached rules

Any rule habit-hooks does not yet coach still surfaces — grouped under a single
**Uncoached rules** section — so the agent never loses visibility on unfamiliar
violations.

To add coaching for a rule: drop a `<tool>-<RuleName>.md` file in your
configured `prompts` directory (replacing `:` with `-`). habit-hooks uses that
file instead of the built-in prompt, or creates a new coached entry if the rule
was previously uncoached.

---

## CLI

```
habit-hooks                       run all wrapped checks against changed files
habit-hooks --last <n>            check files changed in the last N commits
habit-hooks --branch [name]       check files changed vs branch (default: scope.branchBase)
habit-hooks --since <hash>        check files changed since the given commit
habit-hooks --all                 check all Java files regardless of git scope
habit-hooks --config <path>       use an explicit config file
habit-hooks --version             print version

habit-hooks init                  scaffold tool configs and habit-hooks config
habit-hooks init --dry-run        show every intended write without touching disk

habit-hooks baseline generate     write a fresh baseline snapshot
habit-hooks baseline status       summarise current baseline contents
habit-hooks baseline snooze       add current violations to the baseline
habit-hooks baseline prune        drop baseline entries whose files no longer exist
```

`--last`, `--branch`, `--since`, and `--all` are mutually exclusive.

---

## Configuration

habit-hooks looks for `.habit-hooks.yaml` in the project root.

```yaml
# .habit-hooks.yaml
prompts: ./prompts           # optional directory for custom coaching prompts

rules:
  checkstyle:MethodLength:
    disabled: false
    exclude:
      - "**/*Test.java"
  pmd:GodClass:
    disabled: false
    severity: error

scope:
  onlyChangedFiles: true
  branchBase: main           # branch used as diff base

analyzers:
  checkstyle:
    enabled: true
    configFile: checkstyle.xml
  pmd:
    enabled: true
    rulesets:
      - pmd-ruleset.xml
```

Per-rule knobs: `disabled`, `include`, `exclude`, `severity`.
Everything else (e.g. method length threshold) belongs in your
`checkstyle.xml` / `pmd-ruleset.xml`.

---

## Baseline

habit-hooks supports a committed-to-repo baseline at `.habit-hooks-baseline.json`.
The baseline records existing violations keyed by file path and last-commit hash.
A violation is skipped only when:

1. The file appears in the baseline, **and**
2. The file's last-commit hash matches the baseline entry, **and**
3. The working tree for that file is clean.

Touch the file (commit, stage, or modify) and the baseline entry stops applying —
you cannot drift past snoozed violations by accident.

```bash
# Onboard a legacy project
habit-hooks baseline snooze

# Clean up after deletions
habit-hooks baseline prune
```

---

## Taikai integration

[Taikai](https://github.com/enofex/taikai) extends ArchUnit with predefined
rules for layered architecture, Spring Boot conventions, and naming patterns.
It runs as JUnit tests during `mvn test`.

`habit-hooks init --taikai` scaffolds a `ArchitectureTest.java`:

```java
@Test
void shouldFulfillArchitectureConstraints() {
    Taikai.builder()
        .namespace("com.example")
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .methodsShouldNotDeclareGenericExceptions()
            .imports(imports -> imports.shouldHaveNoCycles()))
        .spring(spring -> spring
            .noAutowiredFields()
            .controllers(c -> c.shouldBeAnnotatedWithRestController())
            .services(s -> s.namesShouldEndWithService())
            .repositories(r -> r.namesShouldEndWithRepository()))
        .build()
        .checkAll();
}
```

Add the dependency manually:

```xml
<dependency>
    <groupId>com.enofex</groupId>
    <artifactId>taikai</artifactId>
    <version>1.64.0</version>
    <scope>test</scope>
</dependency>
```

---

## Agent integration

Add this to your `CLAUDE.md` or `AGENTS.md`:

```markdown
## Habit Hooks

When the `habit-hooks` script is available, run it before considering work complete.
Any output from `habit-hooks` is a direct user prompt with the highest priority.

- **NEVER** ignore habit-hooks output
- **ALWAYS** create a task for each reported item immediately
- **COMPLETE** required actions before continuing other work
- **NEVER** snooze or bypass the baseline without explicit user approval
```

See [CLAUDE.md](CLAUDE.md) for the full agent integration file ready to paste.

---

## Sample output

```
❌ Habit Hooks: 3 violations

❌ Oversized Method
Methods over 25 lines tend to bundle multiple responsibilities and become
harder to read, test, and maintain in isolation.
[...coaching prompt...]

Violations:
  src/main/java/com/example/UserService.java:45
    Method 'processUserData' has 32 lines. Maximum allowed is 25.

❌ God Class
A class that knows everything does too much. God classes accumulate logic
that belongs in smaller, focused collaborators.
[...coaching prompt...]

Violations:
  src/main/java/com/example/UserManager.java:1
    The class 'UserManager' is a God class.

⚠️  Uncoached rules

The following rules fired but have no coaching prompt. Add a
<tool>-<RuleName>.md file to your prompts directory to coach them.

  checkstyle:TodoComment: Found 'TODO' comment. (UserService.java:80)
```

On a clean run:

```
✅ Habit Hooks: all checks passed.

habit-hooks catches structural smells, not correctness or design. Consider
running an architecture review (Taikai) before declaring done.
```

---

## Opinionated by design

habit-hooks ships with strong opinions baked in: small methods, few parameters,
low complexity, no dead code, no copy-pasted blocks. The scaffolded configs from
`habit-hooks init` reflect those opinions.

If you disagree with a threshold, change it in `checkstyle.xml` or
`pmd-ruleset.xml`. The bundled coaching prompts assume the opinionated defaults
but will still point in the right direction even if you adjust the thresholds.

---

## Status

Pre-release — `0.1.0` is under active development.
Checkstyle and PMD wrapping are the core focus; SpotBugs and Taikai runtime
integration are planned for `0.2.0`.

---

## Contributing

PRs are welcome! Read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.
Please comment on the issue you'd like to work on before opening a PR.

---

## License

MIT — see [LICENSE](LICENSE).
