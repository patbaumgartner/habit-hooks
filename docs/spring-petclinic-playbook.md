# Spring Petclinic analyzer playbook

This playbook is the reference validation workflow for running `habit-hooks` against
Spring Petclinic, collecting a before report, letting `habit-hooks` tell you what
to fix, fixing those findings in Petclinic, and collecting an after report.

The reference workspace layout used while validating this playbook was:

```text
/home/patbaumgartner/GitHub/habbit-hooks
/home/patbaumgartner/GitHub/spring-petclinic
```

Adjust those paths when your checkout lives somewhere else. Commands below assume
Linux/macOS shell syntax and a Maven-based Spring Petclinic checkout.

## 1. Upgrade habit-hooks first

Always start by upgrading the installed `habit-hooks` binary, then record the
version used for the reference validation run.

```bash
cd /home/patbaumgartner/GitHub/habbit-hooks

habit-hooks --version
habit-hooks --update
habit-hooks --version
```

If the GitHub `latest` release API is temporarily unavailable, install a known
published release tag explicitly. Set `HH_VERSION` to the release you intend to
validate.

```bash
cd /home/patbaumgartner/GitHub/habbit-hooks

HH_VERSION=v0.1.10
VERSION="$HH_VERSION" scripts/install.sh
habit-hooks --version
```

The validation run for this playbook used release `v0.1.10`. The exact release is
not part of the workflow contract; the important rule is to record it and use it
in the evidence directory name.

```bash
HH_VERSION="$(habit-hooks --version | awk '{print $2}')"
REFERENCE_VALIDATION_ROOT="target/habit-hooks-reference-validation-v${HH_VERSION#v}"
```

## 2. Analyzer inventory

For Spring Petclinic, the example reference validation run enables every analyzer surface that
can produce useful project feedback.

| Analyzer | Purpose | Primary artifact |
| --- | --- | --- |
| `checkstyle` | file-scoped structural checks and coaching | native console/report output |
| `pmd` | source code design and best-practice findings | `target/pmd.xml` companion report |
| `taikai` | architecture-test failures from `ArchitectureTest` | `target/surefire-reports/TEST-*ArchitectureTest*.xml` |
| `spotbugs` | bytecode bug patterns | `target/spotbugsXml.xml` |
| `jacoco` | line coverage gaps | `target/site/jacoco/jacoco.xml` |
| `cyclonedx` | SBOM validity and components | `target/bom.json` |
| `pitest` | surviving and uncovered mutations | `target/pit-reports/mutations.xml` |
| `spring-javaformat` | formatter validation output | `target/habit-hooks/spring-javaformat.log` |
| `errorprone` | compiler/Error Prone diagnostics | `target/habit-hooks/errorprone.log` |
| `owasp` | dependency vulnerability findings | `target/dependency-check-report.json` |
| `jspecify` | nullness dependency/adoption checks | native console/report output |

Native builds may intentionally skip in-process PMD when PMD internals are not
native-image safe. Do not treat that as permission to skip PMD. Generate
`target/pmd.xml` directly with Maven as a companion artifact, and use the JVM
launcher for strict PMD coaching parity when needed.

## 3. Scaffold Petclinic

Run the real initializer from the upgraded binary. The `--taikai` test is a useful
starting point, but Spring Petclinic needs the generated test moved into the
application package and the namespace set to Petclinic's root package.

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic

habit-hooks init --maven-snippets --taikai
habit-hooks init --help
```

Update `.habit-hooks.yaml` so reference validation checks the full main source tree and
enables all analyzers:

```yaml
scope:
  onlyChangedFiles: false
  branchBase: main
  excludeTests: true

analyzers:
  checkstyle:
    enabled: true
    configFile: checkstyle.xml
  pmd:
    enabled: true
    rulesets:
      - pmd-ruleset.xml
  taikai:
    enabled: true
    testClass: ArchitectureTest
  spotbugs:
    enabled: true
    goal: -Phabit-hooks-analyzers spotbugs:spotbugs
    reportFile: target/spotbugsXml.xml
  jacoco:
    enabled: true
    goal: test jacoco:report
    reportFile: target/site/jacoco/jacoco.xml
  cyclonedx:
    enabled: true
    goal: package cyclonedx:makeAggregateBom
    reportFile: target/bom.json
  pitest:
    enabled: true
    goal: -Pmutation-test test-compile org.pitest:pitest-maven:mutationCoverage -DtargetClasses=org.springframework.samples.petclinic.model.* -DtargetTests=org.springframework.samples.petclinic.model.* -DfailWhenNoMutations=false
    reportFile: target/pit-reports/mutations.xml
  spring-javaformat:
    enabled: true
    goal: spring-javaformat:validate
    reportFile: target/habit-hooks/spring-javaformat.log
  errorprone:
    enabled: true
    goal: -Phabit-hooks-errorprone compile
    reportFile: target/habit-hooks/errorprone.log
  owasp:
    enabled: true
    goal: -Phabit-hooks-analyzers org.owasp:dependency-check-maven:check -Dformat=JSON -DfailBuildOnCVSS=11 -DautoUpdate=false
    reportFile: target/dependency-check-report.json
  jspecify:
    enabled: true
```

Add the Maven properties and dependencies needed by the optional analyzers. Use
the versions from `habit-hooks-maven-snippets.xml` generated by your installed
`habit-hooks`; the values below are the versions used in the validation run.

```xml
<spotbugs-maven-plugin.version>4.10.2.0</spotbugs-maven-plugin.version>
<dependency-check-maven.version>12.1.9</dependency-check-maven.version>
<pitest-maven-plugin.version>1.19.4</pitest-maven-plugin.version>
<pitest-junit5-plugin.version>1.2.2</pitest-junit5-plugin.version>
<maven-compiler-plugin.version>3.15.0</maven-compiler-plugin.version>
<error-prone.version>2.27.1</error-prone.version>
<taikai.version>1.64.0</taikai.version>
```

```xml
<dependency>
  <groupId>org.jspecify</groupId>
  <artifactId>jspecify</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>

<dependency>
  <groupId>com.enofex</groupId>
  <artifactId>taikai</artifactId>
  <version>${taikai.version}</version>
  <scope>test</scope>
</dependency>
```

Keep Maven-backed analyzers behind opt-in profiles so Petclinic's normal build
stays unchanged:

- `habit-hooks-analyzers`: SpotBugs and OWASP Dependency Check
- `habit-hooks-errorprone`: Maven Compiler Plugin with Error Prone
- `mutation-test`: PIT with the JUnit 5 plugin and XML output

Add or verify a package-correct Taikai test at
`src/test/java/org/springframework/samples/petclinic/ArchitectureTest.java`:

```java
package org.springframework.samples.petclinic;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private static final String NAMESPACE = "org.springframework.samples.petclinic";

  @Test
  void shouldFulfillArchitectureConstraints() {
    Taikai.builder()
      .namespace(NAMESPACE)
      .java((java) -> java.noUsageOfDeprecatedAPIs()
        .methodsShouldNotDeclareGenericExceptions()
        .imports((imports) -> imports.shouldHaveNoCycles()))
      .test((test) -> test.junit((junit) -> junit.classesShouldNotBeAnnotatedWithDisabled()
        .methodsShouldNotBeAnnotatedWithDisabled()))
      .build()
      .checkAll();
  }

}
```

Format before running analyzer-backed Maven goals. Otherwise Spring Java Format
can block Taikai, Error Prone, JaCoCo, PIT, and other goals before their own
reports are written.

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic

./mvnw --batch-mode --no-transfer-progress spring-javaformat:apply
./mvnw --batch-mode --no-transfer-progress -Dtest=ArchitectureTest test
habit-hooks doctor
```

Expected `doctor` output lists every native-ready analyzer as `OK`: `checkstyle`,
`taikai`, `spotbugs`, `jacoco`, `cyclonedx`, `pitest`, `spring-javaformat`,
`errorprone`, `owasp`, and `jspecify`. PMD may be absent from native `doctor`;
that is why the companion Maven PMD command is part of the evidence workflow.

Run OWASP Dependency Check once without `-DautoUpdate=false` if the local NVD
database is empty or stale. After the first refresh, keep `-DautoUpdate=false`
for repeated reference validation and report runs so the analyzer reuses the local cache.

## 4. Capture the before evidence

Create a versioned evidence directory and record the exact commands you run.

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic

HH_VERSION="$(habit-hooks --version | awk '{print $2}')"
out="target/habit-hooks-reference-validation-v${HH_VERSION#v}/before"
mkdir -p "$out/logs"

cat > "$out/commands-used.txt" <<'EOF'
habit-hooks --version
habit-hooks doctor
habit-hooks --all
habit-hooks report --format markdown --output "$out/report.md" --no-fail
habit-hooks report --format json --output "$out/report.json" --no-fail
habit-hooks report --format html --output "$out/report.html" --no-fail
habit-hooks report --format sarif --output "$out/report.sarif" --no-fail
habit-hooks tasks --format markdown --output "$out/tasks.md" --no-fail
habit-hooks tasks --format json --output "$out/tasks.json" --no-fail
habit-hooks dependencies --output "$out/dependencies.txt"
./mvnw --batch-mode --no-transfer-progress pmd:pmd -Dpmd.rulesets=pmd-ruleset.xml
cp target/pmd.xml "$out/pmd.xml"
EOF
```

Run each command and capture its exit code. The analyzer pass may fail when it
finds issues; report and task generation should continue with `--no-fail`.

```bash
habit-hooks --version > "$out/logs/version.log" 2>&1
habit-hooks doctor > "$out/logs/doctor.log" 2>&1

habit-hooks --all > "$out/logs/all-analyzers.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-all-analyzers.txt"

habit-hooks report --format markdown --output "$out/report.md" --no-fail > "$out/logs/report-markdown.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-report-markdown.txt"

habit-hooks report --format json --output "$out/report.json" --no-fail > "$out/logs/report-json.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-report-json.txt"

habit-hooks report --format html --output "$out/report.html" --no-fail > "$out/logs/report-html.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-report-html.txt"

habit-hooks report --format sarif --output "$out/report.sarif" --no-fail > "$out/logs/report-sarif.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-report-sarif.txt"

habit-hooks tasks --format markdown --output "$out/tasks.md" --no-fail > "$out/logs/tasks-markdown.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-tasks-markdown.txt"

habit-hooks tasks --format json --output "$out/tasks.json" --no-fail > "$out/logs/tasks-json.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-tasks-json.txt"

habit-hooks dependencies --output "$out/dependencies.txt" > "$out/logs/dependencies.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-dependencies.txt"

./mvnw --batch-mode --no-transfer-progress pmd:pmd -Dpmd.rulesets=pmd-ruleset.xml > "$out/logs/maven-pmd.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-maven-pmd.txt"

cp target/pmd.xml "$out/pmd.xml" 2> "$out/logs/copy-pmd.log"
printf '%s\n' "$?" > "$out/exit-code-copy-pmd.txt"
```

The validation run for this playbook produced this before baseline:

- `12` findings
- failing gate
- `2` `checkstyle:MagicNumber` findings
- `1` `jspecify:NotAdopted` finding
- `1` `owasp:CveCritical` finding on `spring-boot-devtools`
- `8` `pitest:NO_COVERAGE` findings in Petclinic model classes

Your counts may differ as Petclinic and dependency metadata change. Trust the
current report and task file over this historical example.

## 5. Let habit-hooks drive the fixes

Read the generated task file first:

```bash
sed -n '1,220p' "$out/tasks.md"
```

Work in priority order. For each task, make the smallest behavior-preserving fix,
format, run focused tests, then run `habit-hooks --all` again.

```bash
./mvnw --batch-mode --no-transfer-progress spring-javaformat:apply
./mvnw --batch-mode --no-transfer-progress -Dtest=EntityTests,ArchitectureTest test
habit-hooks --all
```

The validation run fixed Petclinic by following the task feed:

- Removed optional `spring-boot-devtools`, which was the direct dependency behind
  the OWASP critical CVE finding in that run.
- Replaced repeated pagination literals with named page-size constants in owner
  and vet controllers.
- Added JSpecify adoption to the model package with `@NullMarked` plus explicit
  `@Nullable` contracts where new JPA entities legitimately start empty.
- Added focused model tests for `BaseEntity`, `NamedEntity`, and `Person` to kill
  the PIT no-coverage mutations.
- Added a Maven version enforcer rule after `habit-hooks dependencies` reported
  that the project did not declare a minimum Maven version.

If `habit-hooks --all` reports Maven-goal failures such as PIT, Error Prone, or
Spring Java Format, inspect `target/habit-hooks/*.log` before changing
application code. In the validation run, stale formatting blocked several
Maven-backed analyzer goals until `spring-javaformat:apply` was rerun.

## 6. Capture the after evidence

Use the same artifact set under `after`.

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic

HH_VERSION="$(habit-hooks --version | awk '{print $2}')"
out="target/habit-hooks-reference-validation-v${HH_VERSION#v}/after"
mkdir -p "$out/logs"
```

Run the same commands from the before capture, writing to the `after` directory.
On a clean run, expect `habit-hooks --all` to exit `0`, `report.json` to contain
`"totalFindings" : 0`, and `tasks.md` to say `No tasks generated.`

The validation run for this playbook produced this after result:

```json
{
  "clean" : true,
  "failing" : false,
  "totalFindings" : 0,
  "byTool" : { },
  "byRule" : { },
  "byDimension" : { },
  "findings" : [ ]
}
```

## 7. Evidence checklist

Before handing off the reference validation result, confirm both the before and after
directories contain the expected evidence.

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic

HH_VERSION="$(habit-hooks --version | awk '{print $2}')"
root="target/habit-hooks-reference-validation-v${HH_VERSION#v}"

for phase in before after; do
  out="$root/$phase"
  test -s "$out/commands-used.txt"
  test -s "$out/logs/version.log"
  test -s "$out/logs/doctor.log"
  test -s "$out/logs/all-analyzers.log"
  test -s "$out/report.md"
  test -s "$out/report.json"
  test -s "$out/report.html"
  test -s "$out/report.sarif"
  test -s "$out/tasks.md"
  test -s "$out/tasks.json"
  test -s "$out/dependencies.txt"
  test -s "$out/pmd.xml"
done

test -s target/spotbugsXml.xml
test -s target/site/jacoco/jacoco.xml
test -s target/bom.json
test -s target/pit-reports/mutations.xml
test -s target/habit-hooks/spring-javaformat.log
test -s target/habit-hooks/errorprone.log
test -s target/dependency-check-report.json
```

Scan for infrastructure failures. Analyzer findings in the before directory are
expected; missing reports, native-image reflection failures, unreadable reports,
and empty analyzer lists are not expected.

```bash
rg -n "Exception|ERROR|MissingReflection|Cannot|NullPointerException|No Java files|Failed|WARN|aborted|failed|not ready|FAIL|unknown TokenTypes" \
  "$root/before/logs" "$root/before/report.md" "$root/before/tasks.md" "$root/before/dependencies.txt" \
  "$root/after/logs" "$root/after/report.md" "$root/after/tasks.md" "$root/after/dependencies.txt" || true
```

PMD may emit Maven site warnings such as missing Source XRef or parent URL. Those
warnings are not reference validation infrastructure failures when `pmd.xml` exists and the
PMD command exits `0`.

## 8. Commands used while validating this playbook

These are the meaningful shell commands used during the validation pass. They are
included so a future maintainer can reproduce the workflow and see where the
playbook came from.

```bash
git -C /home/patbaumgartner/GitHub/habbit-hooks status --short --branch --untracked-files=all
git -C /home/patbaumgartner/GitHub/spring-petclinic status --short --branch --untracked-files=all
command -v habit-hooks || true
habit-hooks --version || true

habit-hooks --update
git -C /home/patbaumgartner/GitHub/habbit-hooks --no-pager log --oneline -5
git -C /home/patbaumgartner/GitHub/habbit-hooks tag --sort=-v:refname | sed -n '1,10p'
VERSION=v0.1.10 scripts/install.sh
habit-hooks --version

cd /home/patbaumgartner/GitHub/spring-petclinic
habit-hooks init --help
habit-hooks init --maven-snippets --taikai
find . -maxdepth 3 \( -name '.habit-hooks.yaml' -o -name 'checkstyle.xml' -o -name 'pmd-ruleset.xml' -o -name 'habit-hooks-maven-snippets.xml' \) -print
find src/test/java -name '*ArchitectureTest*.java' -print

./mvnw --batch-mode --no-transfer-progress spring-javaformat:apply
./mvnw --batch-mode --no-transfer-progress -Dtest=ArchitectureTest test
habit-hooks doctor

habit-hooks --all
habit-hooks report --format markdown --output target/habit-hooks-reference-validation-v0.1.10/before/report.md --no-fail
habit-hooks report --format json --output target/habit-hooks-reference-validation-v0.1.10/before/report.json --no-fail
habit-hooks report --format html --output target/habit-hooks-reference-validation-v0.1.10/before/report.html --no-fail
habit-hooks report --format sarif --output target/habit-hooks-reference-validation-v0.1.10/before/report.sarif --no-fail
habit-hooks tasks --format markdown --output target/habit-hooks-reference-validation-v0.1.10/before/tasks.md --no-fail
habit-hooks tasks --format json --output target/habit-hooks-reference-validation-v0.1.10/before/tasks.json --no-fail
habit-hooks dependencies --output target/habit-hooks-reference-validation-v0.1.10/before/dependencies.txt
./mvnw --batch-mode --no-transfer-progress pmd:pmd -Dpmd.rulesets=pmd-ruleset.xml

sed -n '1,220p' target/habit-hooks-reference-validation-v0.1.10/before/tasks.md
sed -n '1,220p' target/habit-hooks-reference-validation-v0.1.10/before/report.json
rg -n "Exception|ERROR|MissingReflection|Cannot|NullPointerException|No Java files|Failed|WARN|aborted|failed|not ready|FAIL|unknown TokenTypes" target/habit-hooks-reference-validation-v0.1.10/before/logs target/habit-hooks-reference-validation-v0.1.10/before/report.md target/habit-hooks-reference-validation-v0.1.10/before/tasks.md target/habit-hooks-reference-validation-v0.1.10/before/dependencies.txt || true

./mvnw --batch-mode --no-transfer-progress spring-javaformat:apply
./mvnw --batch-mode --no-transfer-progress -Dtest=EntityTests,ArchitectureTest test
habit-hooks --all

habit-hooks report --format markdown --output target/habit-hooks-reference-validation-v0.1.10/after/report.md --no-fail
habit-hooks report --format json --output target/habit-hooks-reference-validation-v0.1.10/after/report.json --no-fail
habit-hooks report --format html --output target/habit-hooks-reference-validation-v0.1.10/after/report.html --no-fail
habit-hooks report --format sarif --output target/habit-hooks-reference-validation-v0.1.10/after/report.sarif --no-fail
habit-hooks tasks --format markdown --output target/habit-hooks-reference-validation-v0.1.10/after/tasks.md --no-fail
habit-hooks tasks --format json --output target/habit-hooks-reference-validation-v0.1.10/after/tasks.json --no-fail
habit-hooks dependencies --output target/habit-hooks-reference-validation-v0.1.10/after/dependencies.txt
./mvnw --batch-mode --no-transfer-progress pmd:pmd -Dpmd.rulesets=pmd-ruleset.xml
cp target/pmd.xml target/habit-hooks-reference-validation-v0.1.10/after/pmd.xml

cat target/habit-hooks-reference-validation-v0.1.10/after/report.json
cat target/habit-hooks-reference-validation-v0.1.10/after/tasks.md
git -C /home/patbaumgartner/GitHub/habbit-hooks --no-pager status --short --branch --untracked-files=all
git -C /home/patbaumgartner/GitHub/spring-petclinic --no-pager status --short --branch --untracked-files=all
```
