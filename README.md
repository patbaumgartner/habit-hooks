# habit-hooks

[![CI](https://github.com/patbaumgartner/habit-hooks/actions/workflows/ci.yml/badge.svg)](https://github.com/patbaumgartner/habit-hooks/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-25-orange)](https://adoptium.net)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-blue)](https://maven.apache.org)
[![Coached rules](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/patbaumgartner/habit-hooks/main/docs/coached-rules-badge.json)](#coached-rules)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/patbaumgartner/habit-hooks/badge)](https://securityscorecards.dev/viewer/?uri=github.com/patbaumgartner/habit-hooks)

Stop reciting software engineering literature to your AI agent.

Turn best-practice advice into AI habits, enforced at commit speed.

---

## What it is

AI coding agents frequently ignore long rule documents. Asking them to hold an
entire book's worth of coding advice is at best futile, at worst pollutes the
context window and degrades performance.

**habit-hooks** wraps your existing Java quality tools — starting with
**Checkstyle** and **PMD**, and extending to project-level Maven signals such as
SpotBugs, JaCoCo, CycloneDX, PIT, Spring Java Format, Error Prone, JSpecify, and
Taikai — to create the trigger. Instead of providing only a metric, it gives actionable coaching on
*why* the finding matters and *how* to fix it. This creates AI behaviour that
looks like human habits with similar effects.

Using habit-hooks:

- **Increases code quality** — structural smells are caught at commit time
- **Improves AI performance** — agents start from clean code and need less context
- **Reduces token usage** — clean code means less reading to understand intent
- **Encourages architecture discipline** — pairs naturally with [Taikai](#taikai-integration)
- **Turns quality tools into an AI harness** — agents see focused feedback from the
  build, tests, coverage, formatting, mutation testing, and supply-chain checks

> Inspired by [devill/habit-hooks](https://github.com/devill/habit-hooks) — the original TypeScript edition.

---

## Install

Use the install script. It picks the native binary for your platform when one
is published, and otherwise downloads the fat JAR plus a wrapper script:

```bash
curl -fsSL https://raw.githubusercontent.com/patbaumgartner/habit-hooks/main/scripts/install.sh | sh
```

Defaults install to `~/.local/bin`. Override with environment variables:

```bash
# Pin a version and choose an install location
VERSION=0.1.0 INSTALL_DIR=~/bin sh scripts/install.sh

# Force the JAR path even if a native binary exists
FORCE_JAR=1 sh scripts/install.sh
```

The JAR path requires a Java 25+ runtime; the script checks for it and exits
with guidance if it is missing. Make sure your install dir is on `PATH`.

To upgrade an existing install to the latest release in place, run:

```bash
habit-hooks --update
```

It detects whether you are running the native binary or the JAR, downloads the
matching asset from the latest GitHub release, and atomically replaces the
installed artifact. Re-run `habit-hooks` afterwards to use the new version.

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
for the missing ones, writes `.habit-hooks.yaml`, creates an empty baseline, and
writes `AGENTS.md` so AI coding agents know how to run the tool. Run with
`--dry-run` to preview every write. Add `--maven-snippets` when you want a
reference file with optional Maven plugin/dependency fragments for the
project-scoped analyzers. Add `--taikai` when you want a starter architecture
test in `src/test/java`; copy the Taikai dependency from the Maven snippets
before running Maven test-based analyzers.

For Spring Boot applications, start with the full analyzer scaffold:

```bash
habit-hooks init --spring-boot
```

Spring Boot mode enables the project-scoped analyzer surface in
`.habit-hooks.yaml`, writes the Spring-oriented `AGENTS.md`, scaffolds the Taikai
architecture test when `src/test/java` exists, and writes Maven snippets for the
required plugins, profiles, and dependencies. Copy the relevant snippets into
`pom.xml`, then run `habit-hooks doctor`.

Then:

```bash
habit-hooks
```

Runs all wrapped tools against files changed since the branch base.

For a fuller local quality loop before handing work back:

```bash
habit-hooks doctor
habit-hooks --all
habit-hooks report --format html --no-fail
habit-hooks tasks --format markdown --no-fail
```

`doctor` catches broken analyzer setup early, `--all` gives the quality gate,
`report` creates a readable dashboard, and `tasks` creates focused remediation
work items for a human or AI coding agent.

## Default behavior (sane out of the box)

- Scope defaults to changed files only (`scope.onlyChangedFiles: true`)
- Diff base defaults to `main` (`scope.branchBase: main`)
- Test sources are excluded by default (`scope.excludeTests: true`)
- Build output directories (`target/`, `build/`) are always excluded regardless of scope
- Checkstyle is enabled by default using `checkstyle.xml`
- PMD is enabled by default using `pmd-ruleset.xml`
- Native binaries run PMD through the Maven PMD goal and parse `target/pmd.xml`, so they need `./mvnw` or `mvn` available for PMD coverage
- Missing, null, or blank config values fall back to safe defaults

For full-repo analysis, run `habit-hooks --all`.

---

## Compatibility and stability guarantees

- Runtime/build baseline: **Java 25 only**
- Supported platforms: Linux and macOS (validated in CI matrix)
- JDK distributions validated: BellSoft Liberica and GraalVM (Java 25)
- Public CLI commands and `.habit-hooks.yaml` structure are considered stable
  within the same minor line (`0.x`) unless explicitly documented in release notes

---

## Maintenance scripts

Two repository scripts keep operational tasks repeatable:

```bash
# Refresh dependencies, rewrite code, run verify, reference validation
./scripts/cleanup-code.sh

# Enforce repo settings (rebase-only), optional PR merge with rebase
./scripts/repo-admin.sh --repo patbaumgartner/habit-hooks --merge-open-prs
```

Use `--dry-run` on either script to preview actions.

---

## What it catches

habit-hooks wraps your project's Checkstyle and PMD rule configurations, plus
optional project-scoped signals from Maven-backed analyzers. Whatever those tools
report is what habit-hooks surfaces. What it adds on top is *why this is a smell*
and *how to fix it*.

### Coached rules

| Tool              | Rule ID                                      | Description                      |
|-------------------|----------------------------------------------|----------------------------------|
| checkstyle        | `checkstyle:MethodLength`                    | Oversized method                 |
| checkstyle        | `checkstyle:ParameterNumber`                 | Too many parameters              |
| checkstyle        | `checkstyle:CyclomaticComplexity`            | High complexity                  |
| checkstyle        | `checkstyle:JavaNCSS`                        | High non-commenting source lines |
| checkstyle        | `checkstyle:VisibilityModifier`              | Weak encapsulation               |
| checkstyle        | `checkstyle:MagicNumber`                     | Magic numbers                    |
| checkstyle        | `checkstyle:EmptyLineSeparator`              | Missing separation               |
| checkstyle        | `checkstyle:FileTabCharacter`                | Tab character                    |
| checkstyle        | `checkstyle:NestedIfDepth`                   | Deeply nested conditions         |
| checkstyle        | `checkstyle:NestedTryDepth`                  | Deeply nested try blocks         |
| checkstyle        | `checkstyle:BooleanExpressionComplexity`     | Complex boolean expression       |
| checkstyle        | `checkstyle:HideUtilityClassConstructor`     | Utility class needs private ctor |
| checkstyle        | `checkstyle:ModifiedControlVariable`         | Modified loop control variable   |
| checkstyle        | `checkstyle:EqualsAvoidNull`                 | Literal not first in `equals()`  |
| checkstyle        | `checkstyle:CovariantEquals`                 | Covariant `equals()`             |
| checkstyle        | `checkstyle:DefaultComesLast`                | `default` case not last          |
| checkstyle        | `checkstyle:UnnecessarySemicolonInEnumeration` | Redundant semicolon in enum    |
| checkstyle        | `checkstyle:UnnecessarySemicolonAfterTypeMemberDeclaration` | Redundant semicolon |
| pmd               | `pmd:NcssCount`                              | Oversized method or class (PMD)  |
| pmd               | `pmd:CyclomaticComplexity`                   | High complexity (PMD)            |
| pmd               | `pmd:ExcessiveParameterList`                 | Too many parameters              |
| pmd               | `pmd:TooManyFields`                          | Too many fields                  |
| pmd               | `pmd:TooManyMethods`                         | Too many methods                 |
| pmd               | `pmd:CollapsibleIfStatements`                | Collapsible if statements        |
| pmd               | `pmd:SimplifiedTernary`                      | Simplifiable ternary             |
| pmd               | `pmd:SingularField`                          | Field could be a local           |
| pmd               | `pmd:GodClass`                               | Class doing too much             |
| pmd               | `pmd:UnusedPrivateField`                     | Unused field                     |
| pmd               | `pmd:UnusedLocalVariable`                    | Unused variable                  |
| pmd               | `pmd:UnusedPrivateMethod`                    | Unused private method            |
| pmd               | `pmd:UnusedFormalParameter`                  | Unused private parameter         |
| pmd               | `pmd:UnusedAssignment`                       | Assignment with no effect        |
| pmd               | `pmd:EmptyCatchBlock`                        | Silent exception swallowing      |
| pmd               | `pmd:LiteralsFirstInComparisons`             | Null-unsafe string comparison    |
| pmd               | `pmd:ReturnEmptyCollectionRatherThanNull`    | Null instead of empty collection |
| pmd               | `pmd:UseCollectionIsEmpty`                   | `size() == 0` -> `isEmpty()`     |
| pmd               | `pmd:UseEqualsToCompareStrings`              | String compared with `==`        |
| pmd               | `pmd:OverrideBothEqualsAndHashcode`          | `equals()` without `hashCode()`  |
| pmd               | `pmd:AvoidReassigningParameters`             | Reassigned parameter             |
| pmd               | `pmd:LooseCoupling`                          | Concrete type in API             |
| pmd               | `pmd:ArrayIsStoredDirectly`                  | Array stored without copying     |
| pmd               | `pmd:MethodReturnsInternalArray`             | Internal array exposed           |
| pmd               | `pmd:PreserveStackTrace`                     | Lost stack trace on rethrow      |
| pmd               | `pmd:CompareObjectsWithEquals`               | Reference comparison of objects  |
| pmd               | `pmd:EqualsNull`                             | `equals()` used for null check   |
| pmd               | `pmd:MissingOverride`                        | Missing `@Override`              |
| pmd               | `pmd:PrimitiveWrapperInstantiation`          | Primitive wrapper instantiated   |
| pmd               | `pmd:UselessParentheses`                     | Useless parentheses              |
| pmd               | `pmd:UnnecessaryReturn`                      | Redundant `return`               |
| pmd               | `pmd:UnnecessaryModifier`                    | Redundant modifier               |
| pmd               | `pmd:CopyPaste`                              | Duplicated code (CPD)            |
| pmd               | `pmd:GuardLogStatement`                      | Unguarded log statement (opt-in) |
| cpd               | `cpd:goal-failed`                            | CPD goal failed                  |
| cpd               | `cpd:lifecycle-blocked`                      | CPD lifecycle blocked            |
| cpd               | `cpd:report-missing`                         | CPD report missing               |
| cpd               | `cpd:report-unreadable`                      | CPD report unreadable            |
| spotbugs          | `spotbugs:goal-failed`                       | SpotBugs goal failed             |
| spotbugs          | `spotbugs:lifecycle-blocked`                 | SpotBugs lifecycle blocked       |
| spotbugs          | `spotbugs:report-missing`                    | SpotBugs report missing          |
| spotbugs          | `spotbugs:report-unreadable`                 | SpotBugs report unreadable       |
| jacoco            | `jacoco:LineCoverage`                        | Line coverage gap                |
| jacoco            | `jacoco:goal-failed`                         | JaCoCo goal failed               |
| jacoco            | `jacoco:lifecycle-blocked`                   | JaCoCo lifecycle blocked         |
| jacoco            | `jacoco:report-missing`                      | JaCoCo report missing            |
| jacoco            | `jacoco:report-unreadable`                   | JaCoCo report unreadable         |
| cyclonedx         | `cyclonedx:InvalidBom`                       | Invalid SBOM                     |
| cyclonedx         | `cyclonedx:MissingComponents`                | SBOM missing components          |
| cyclonedx         | `cyclonedx:goal-failed`                      | CycloneDX goal failed            |
| cyclonedx         | `cyclonedx:lifecycle-blocked`                | CycloneDX lifecycle blocked      |
| cyclonedx         | `cyclonedx:report-missing`                   | CycloneDX report missing         |
| cyclonedx         | `cyclonedx:report-unreadable`                | CycloneDX report unreadable      |
| pitest            | `pitest:SURVIVED`                            | Surviving mutation               |
| pitest            | `pitest:NO_COVERAGE`                         | Mutation without coverage        |
| pitest            | `pitest:goal-failed`                         | PIT goal failed                  |
| pitest            | `pitest:lifecycle-blocked`                   | PIT lifecycle blocked            |
| pitest            | `pitest:report-missing`                      | PIT report missing               |
| pitest            | `pitest:report-unreadable`                   | PIT report unreadable            |
| spring-javaformat | `spring-javaformat:Formatting`               | Formatting drift                 |
| spring-javaformat | `spring-javaformat:goal-failed`              | Formatter goal failed            |
| spring-javaformat | `spring-javaformat:lifecycle-blocked`        | Formatter lifecycle blocked      |
| spring-javaformat | `spring-javaformat:report-missing`           | Formatter output missing         |
| spring-javaformat | `spring-javaformat:report-unreadable`        | Formatter output unreadable      |
| errorprone        | `errorprone:goal-failed`                     | Error Prone compile failed       |
| errorprone        | `errorprone:lifecycle-blocked`               | Error Prone lifecycle blocked    |
| errorprone        | `errorprone:report-missing`                  | Error Prone output missing       |
| errorprone        | `errorprone:report-unreadable`               | Error Prone output unreadable    |
| owasp             | `owasp:CveCritical`                          | Critical CVE                     |
| owasp             | `owasp:CveHigh`                              | High CVE                         |
| owasp             | `owasp:CveMedium`                            | Medium CVE                       |
| owasp             | `owasp:CveLow`                               | Low CVE                          |
| owasp             | `owasp:SuppressedVulnerability`              | Suppressed vulnerability         |
| owasp             | `owasp:goal-failed`                          | OWASP scan failed                |
| owasp             | `owasp:lifecycle-blocked`                    | OWASP lifecycle blocked          |
| owasp             | `owasp:report-missing`                       | OWASP report missing             |
| owasp             | `owasp:report-unreadable`                    | OWASP report unreadable          |
| jspecify          | `jspecify:DependencyMissing`                 | JSpecify dependency missing      |
| jspecify          | `jspecify:NotAdopted`                        | JSpecify not adopted             |

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

```text
habit-hooks                       run all wrapped checks against changed files
habit-hooks --last <n>            check files changed in the last N commits
habit-hooks --branch [name]       check files changed vs branch (default: scope.branchBase)
habit-hooks --since <hash>        check files changed since the given commit
habit-hooks --all                 check all Java files regardless of git scope
habit-hooks --config <path>       use an explicit config file
habit-hooks --version             print version
habit-hooks --update              download and install the latest release

habit-hooks init                  scaffold tool configs and habit-hooks config
habit-hooks init --dry-run        show every intended write without touching disk
habit-hooks init --taikai         scaffold a Taikai ArchitectureTest.java
habit-hooks init --maven-snippets scaffold optional Maven plugin snippets
habit-hooks init --spring-boot    enable Spring Boot analyzer defaults and support files

habit-hooks report                write target/habit-hooks/report.md
habit-hooks report --format html  write a static local quality dashboard
habit-hooks report --format sarif write SARIF for code-scanning consumers
habit-hooks report --output <path> write report artifacts to a custom directory or file
habit-hooks report --no-fail      write the report and always exit zero
habit-hooks tasks                 write target/habit-hooks/tasks.md
habit-hooks tasks --format json   export grouped agent task batches as JSON
habit-hooks tasks --output <path> write task artifacts to a custom directory or file
habit-hooks tasks --no-fail       write task export and always exit zero
habit-hooks doctor                check analyzer prerequisites
habit-hooks dependencies          report Maven dependency/plugin updates
habit-hooks dependencies --apply  apply Maven parent/property updates
habit-hooks dependencies --allow-major allow major parent/property updates with --apply
habit-hooks dependencies --output <file> write update report to a custom file

habit-hooks baseline status       summarise current baseline contents
habit-hooks baseline snooze       add current violations to the baseline
habit-hooks baseline prune        drop baseline entries whose files no longer exist
```

`--last`, `--branch`, `--since`, and `--all` are mutually exclusive.
`report --format` accepts `markdown`, `md`, `json`, `html`, or `sarif`.
`tasks --format` accepts `markdown`, `md`, or `json`. Unknown formats fail fast
with a usage error instead of silently writing the wrong artifact.
Relative `--output` paths for `report`, `tasks`, and `dependencies` resolve from
the analyzed project root, which keeps agent runs deterministic even when launched
from another process directory.
For `report` and `tasks`, a path ending in the selected format extension is used
as the exact artifact file; otherwise the path is treated as an output directory.

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
  excludeTests: true         # skip src/test/java by default

analyzers:
  checkstyle:
    enabled: true
    configFile: checkstyle.xml
  pmd:
    enabled: true
    rulesets:
      - pmd-ruleset.xml
  taikai:                      # opt-in: runs ArchitectureTest via Maven
    enabled: false
    testClass: ArchitectureTest
  spotbugs:                    # opt-in: parses target/spotbugsXml.xml
    enabled: false
    goal: -Phabit-hooks-analyzers spotbugs:spotbugs
    reportFile: target/spotbugsXml.xml
  jacoco:                      # opt-in: parses target/site/jacoco/jacoco.xml
    enabled: false
    goal: test jacoco:report
    reportFile: target/site/jacoco/jacoco.xml
  cyclonedx:                   # opt-in: validates target/bom.json
    enabled: false
    goal: -Phabit-hooks-analyzers cyclonedx:makeAggregateBom
    reportFile: target/bom.json
  pitest:                      # opt-in: expensive mutation testing
    enabled: false
    goal: -Pmutation-test test-compile org.pitest:pitest-maven:mutationCoverage
    reportFile: target/pit-reports/mutations.xml
  spring-javaformat:           # opt-in: captures formatter validation output
    enabled: false
    goal: spring-javaformat:validate
    reportFile: target/habit-hooks/spring-javaformat.log
  errorprone:                  # opt-in: captures compiler/Error Prone output
    enabled: false
    goal: -Phabit-hooks-errorprone compile
    reportFile: target/habit-hooks/errorprone.log
  owasp:                       # opt-in: parses target/dependency-check-report.json
    enabled: false
    goal: -Phabit-hooks-analyzers org.owasp:dependency-check-maven:check -Dformat=JSON -DfailBuildOnCVSS=11
    reportFile: target/dependency-check-report.json
  jspecify:                    # opt-in: checks nullness annotation adoption
    enabled: false
```

Per-rule knobs: `disabled`, `include`, `exclude`, `severity`.
`disabled: true` removes that rule's violations from output. `include` and
`exclude` are file globs applied to each violation path. `severity: warning`
keeps the finding visible but exits zero; unset severity or any non-`warning`
value makes the rule fail the run.
Everything else (e.g. method length threshold) belongs in your
`checkstyle.xml` / `pmd-ruleset.xml`.

Maven-backed analyzers are project-scoped. They can run even when the changed-file
scope contains no Java files. This is useful for checks whose feedback is about the
whole build: coverage, formatting, mutation testing, SBOM validity, or architecture
tests. The JSpecify analyzer is also project-scoped; it checks whether the annotation
dependency is present and whether main sources have started using nullness markers.

Keep expensive analyzers disabled for tight local loops and enable them in stricter
contexts such as `--all`, pre-push, CI reference validation, or nightly quality sweeps.
Enable the matching Maven plugin or dependency before turning on a Maven-backed
analyzer. `habit-hooks init --maven-snippets` writes
`habit-hooks-maven-snippets.xml`, a reference file with copyable fragments for
JaCoCo, SpotBugs, CycloneDX, OWASP Dependency Check, PIT, Spring Java Format,
Error Prone, JSpecify, and Taikai. It does not edit `pom.xml` automatically.
The snippets keep expensive checks opt-in, but the generated PIT profile includes
minimum mutation, coverage, and test-strength thresholds, and the Error Prone
compiler fragment enables `-Xlint:all` with `-Werror` for stricter Spring Boot
service builds.

`habit-hooks init --spring-boot` uses the same snippets but enables the full
Spring Boot reference surface immediately: Taikai, SpotBugs, JaCoCo, CycloneDX,
PIT, Spring Java Format, Error Prone, OWASP Dependency Check, and JSpecify. Use
`habit-hooks doctor` after copying Maven snippets to verify the local setup
before relying on the gate.

`habit-hooks report` is the local Sonar-style path: it writes Markdown, JSON,
HTML, or SARIF under `target/habit-hooks` and stores the latest trend snapshot in
`target/habit-hooks/history/latest.json`. Markdown and HTML reports include the
finding delta from the previous local snapshot when one exists. `habit-hooks tasks`
turns findings into prioritized, rule-grouped work items with verification commands
and acceptance criteria for AI agents. `habit-hooks doctor` checks whether enabled
analyzers can run before an agent spends time on a broken local setup.

If you pass `--config <path>`, absolute paths are used as-is and relative paths
are resolved from the working directory.

If a configured analyzer's config file is missing (e.g. `checkstyle.xml` does
not exist), that analyzer is skipped and a `[WARN]` is printed. Run
`habit-hooks init` to scaffold missing files.

Test sources are excluded by default when habit-hooks resolves scope.
Set `scope.excludeTests: false` if you want the CLI to analyze `src/test/java` too.

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
# Onboard a legacy project — must use --all to cover every file
habit-hooks --all baseline snooze

# Clean up after deletions
habit-hooks baseline prune
```

---

## Taikai integration

[Taikai](https://github.com/enofex/taikai) extends ArchUnit with predefined
rules for layered architecture, Spring Boot conventions, and naming patterns.

habit-hooks integrates Taikai as an optional analyzer. Enable it in
`.habit-hooks.yaml`:

```yaml
analyzers:
  taikai:
    enabled: true
    testClass: ArchitectureTest  # defaults to ArchitectureTest
```

When enabled, habit-hooks runs the named test class via `./mvnw test` and
reports each test failure as an uncoached violation with rule ID
`taikai:<methodName>`. The analyzer is skipped automatically when `mvnw` is
absent, the test class does not exist, or a generated Taikai test is present but
the build does not declare the Taikai dependency. Run `habit-hooks doctor` after
copying snippets to confirm the analyzer is ready.

`habit-hooks init --taikai` scaffolds a strict Spring Boot starter
`ArchitectureTest.java`. `habit-hooks init --spring-boot` scaffolds the same test
and enables the analyzer in `.habit-hooks.yaml`. The template follows Taikai's
Java, logging, test, and Spring rule groups and intentionally enables more checks
than every application will want. Remove or relax the generated rules that do not
fit your architecture.

```java
@Test
void shouldFulfillArchitectureConstraints() {
    Taikai.builder()
        .namespace(NAMESPACE)
        .java(javaRules())
        .logging(loggingRules())
        .spring(springBootRules())
        .test(testRules())
        .build()
        .checkAll();
}
```

The generated Spring Boot rules check constructor-injection habits, application
class placement, configuration properties, configuration/controller/service/
repository naming and annotations, dependency direction between web/service/data
layers, transaction boundaries, logger conventions, JUnit conventions, import
cycles, public fields, deprecated APIs, generic exceptions, method parameters,
utility classes, serialVersionUID fields, and naming conventions.

Add the dependency manually, or generate reference snippets with
`habit-hooks init --maven-snippets`:

```xml
<dependency>
    <groupId>com.enofex</groupId>
    <artifactId>taikai</artifactId>
    <version>1.64.0</version>
    <scope>test</scope>
</dependency>
```

---

## Native image (optional)

`habit-hooks` ships a GraalVM native-image Maven profile for fast startup CLI usage.

```bash
# Requires GraalVM JDK with native-image installed
./mvnw -Pnative package
```

Output binary:

```bash
target/habit-hooks
```

Releases publish prebuilt native binaries per platform
(`habit-hooks-<os>-<arch>`), and the install script prefers them automatically.
The Java fat JAR remains the default fallback distribution artifact.

---

## Supply chain and release integrity

Releases include:

- `habit-hooks-launcher.jar` — runnable fat JAR
- `habit-hooks-<os>-<arch>` — native binaries (e.g. `habit-hooks-linux-x64`,
  `habit-hooks-linux-arm64`, `habit-hooks-darwin-arm64`)
- `habit-hooks-<version>-sources.jar` — source archive
- `bom.json` — CycloneDX SBOM

GitHub release builds also generate **SLSA provenance attestations**
(`actions/attest-build-provenance`).

Security automation includes:

- CodeQL (`codeql.yml`)
- OWASP Dependency Check (`security.yml`)
- OpenSSF Scorecard (`scorecard.yml`)
- Dependabot updates + patch auto-merge (`dependabot-automerge.yml`)
- Private vulnerability reports through GitHub Security Advisories

Release integrity also includes:

- Immutable action pinning by commit SHA in all workflows
- SLSA attestation verification (`gh attestation verify`) during release builds
- Keyless Sigstore/Cosign signatures (`.sig` + `.pem`) for released artifacts

### Verify release artifacts locally

1. Download the release files (`habit-hooks-launcher.jar`, `.sig`, `.pem`, `bom.json`, and related signature files).
1. Verify provenance attestation with GitHub CLI:

```bash
gh attestation verify habit-hooks-launcher.jar --repo patbaumgartner/habit-hooks
```

1. Verify keyless Cosign signature:

```bash
cosign verify-blob \
  --certificate habit-hooks-launcher.jar.pem \
  --signature habit-hooks-launcher.jar.sig \
  --certificate-identity-regexp 'https://github.com/patbaumgartner/habit-hooks/.github/workflows/release.yml@refs/tags/v.*' \
  --certificate-oidc-issuer https://token.actions.githubusercontent.com \
  habit-hooks-launcher.jar
```

1. Repeat for `bom.json` and `habit-hooks-<version>-sources.jar` if desired.

---

## Platform compatibility matrix

`compatibility.yml` continuously smoke-tests launcher behavior across:

- Linux + macOS
- BellSoft Liberica JDK 25 + GraalVM JDK 25

It also runs an additional native-image smoke check on Linux + GraalVM,
including executing the native binary.

`startup-benchmark.yml` tracks launcher startup time and enforces a regression
guard (average startup must stay within CI threshold). A dedicated native
startup benchmark job tracks native binary startup as well.

---

## Architecture decisions

Additional documentation lives in [docs](docs/README.md):

- [Agent integration](docs/agent-integration.md)
- [Artifact contracts](docs/artifacts.md)
- [Release smoke checklist](docs/release-smoke.md)

Project-level decisions are documented as ADRs in [docs/adr](docs/adr/README.md):

- Java 25 baseline
- Coaching on top of Checkstyle and PMD
- Rebase-only repository policy
- Native image distribution profile
- Supply-chain integrity controls
- Baseline suppression model
- Quality gate thresholds and enforcement
- Resilient configuration defaults

Architecture flow and package boundaries are documented in
[ARCHITECTURE.md](ARCHITECTURE.md).

---

## Agent integration

`habit-hooks init` writes `AGENTS.md` automatically when one does not already
exist. If you maintain agent instructions by hand, include the same core loop:

```markdown
## Habit Hooks

Before considering work complete, run habit-hooks. If the installed command is
available, use `habit-hooks --all`. If not, install habit-hooks or use
`java -jar target/habit-hooks-*-launcher.jar --all` when the repository carries a
project-local launcher.

For larger changes, run `habit-hooks doctor` before analysis and generate
follow-up artifacts with `habit-hooks report --no-fail` and
`habit-hooks tasks --no-fail`.

Any output from habit-hooks is direct user feedback with the highest priority.

- **NEVER** ignore habit-hooks output
- **ALWAYS** create a task for each reported item immediately
- **COMPLETE** required actions before continuing other work
- **NEVER** snooze or bypass the baseline without explicit user approval
- **ALWAYS** use Conventional Commits: `<type>[optional scope]: <description>`
```

---

## Sample output

```text
❌ Habit Hooks: 3 violations

❌ Oversized Method
Methods over 20 lines tend to bundle multiple responsibilities and become
harder to read, test, and maintain in isolation.
[...coaching prompt...]

Violations:
  src/main/java/com/example/UserService.java:45
    Method 'processUserData' has 32 lines. Maximum allowed is 20.

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

```text
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

Pre-release — active development in the `0.x` line.
Current implementation:

- Checkstyle and PMD analyzer wrapping with coaching
- Maven-backed project analyzers for SpotBugs, JaCoCo, CycloneDX, OWASP Dependency Check, PIT, Spring Java Format, and Error Prone
- JSpecify adoption analyzer for nullness annotation setup
- Built-in coaching prompts for Maven-backed analyzer meta-rules and JSpecify adoption findings
- Local reports, SARIF, trend deltas, doctor, dependency-update reports, and prioritized agent-task exports
- Optional Maven snippet scaffolding via `habit-hooks init --maven-snippets`
- Baseline management (`status`, `snooze`, `prune`)
- CI quality gates (Checkstyle, PMD/CPD, SpotBugs, tests, coverage)
- Spring-oriented PMD defaults for exception wrapping, resource cleanup, logger hygiene, file stream avoidance, and coupling pressure
- Integration test lifecycle via Maven Failsafe (`*IT.java`)
- 75% minimum line coverage gate (JaCoCo)
- Spring Java Format (`spring-javaformat-maven-plugin`) check at validate phase
- Reproducible build timestamp controls for jar and shaded artifacts
- Release pipeline with SBOM and provenance attestation
- CodeQL security scanning workflow
- Optional GraalVM native image profile
- Optional PIT mutation testing profile (`-Pmutation-test`)
- Taikai architecture-test analyzer (opt-in via `analyzers.taikai.enabled: true`)

Planned next:

- Startup benchmark trend visualization in published CI artifacts

---

## Contributing

PRs are welcome! Read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.
Please comment on the issue you'd like to work on before opening a PR.

---

## License

MIT — see [LICENSE](LICENSE).
