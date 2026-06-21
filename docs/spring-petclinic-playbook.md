# Spring Petclinic analyzer playbook

This playbook describes how to dogfood the installed native `habit-hooks` binary
against Spring Petclinic and collect feedback from every analyzer surface.

The reference workspace layout used by this playbook is:

```text
/home/patbaumgartner/GitHub/habbit-hooks
/home/patbaumgartner/GitHub/spring-petclinic
```

## Analyzer inventory

Native `habit-hooks` 0.1.9 supports these configured analyzers in Spring
Petclinic:

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

The `pmd` analyzer is configured in `.habit-hooks.yaml`, but native
`habit-hooks` 0.1.9 intentionally skips the in-process PMD analyzer because PMD's
Java type-system bootstrap is not currently native-image safe. Do not treat that
as permission to skip PMD. Generate `target/pmd.xml` directly with Maven as a
companion artifact, and use the JVM launcher for PMD coaching when strict PMD
parity is required.

## One-time Petclinic setup

Run these commands from the Spring Petclinic repository:

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic
habit-hooks init
```

Update `.habit-hooks.yaml` so the dogfood run uses all source files and enables
all analyzers:

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

Add the Maven properties, dependencies, and profiles needed by the optional
Maven-backed analyzers:

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

The `habit-hooks-maven-snippets.xml` file produced by
`habit-hooks init --maven-snippets` is the source for the optional plugin
fragments. For Petclinic dogfooding, keep these plugins behind profiles so the
normal application build remains unchanged:

- `habit-hooks-analyzers`: SpotBugs and OWASP Dependency Check
- `habit-hooks-errorprone`: Maven Compiler Plugin with Error Prone
- `mutation-test`: PIT with the JUnit 5 plugin and XML output

Add a Taikai test at
`src/test/java/org/springframework/samples/petclinic/ArchitectureTest.java` with
the namespace set to `org.springframework.samples.petclinic`:

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

Run `./mvnw spring-javaformat:apply` after adding the test; Taikai runs through
Maven, so Spring Java Format validation can block the architecture test before
Surefire writes its XML report.

Run OWASP Dependency Check once without `-DautoUpdate=false` when the local NVD
database is empty or stale. After the first data refresh, keep
`-DautoUpdate=false` for repeated dogfood/report runs so the analyzer reuses the
local cache instead of starting another long online update.

## Run the native analyzer pass

Capture the run in a versioned dogfood directory:

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic
out=target/habit-hooks-dogfood-v0.1.9
mkdir -p "$out/logs"

habit-hooks --version | tee "$out/logs/version.log"
habit-hooks doctor 2>&1 | tee "$out/logs/doctor.log"

set +e
habit-hooks --all > "$out/logs/all-analyzers.log" 2>&1
printf '%s\n' "$?" > "$out/exit-code-all-analyzers.txt"
set -e
```

Expected `doctor` output lists all native-ready analyzers as `OK`: `checkstyle`,
`taikai`, `spotbugs`, `jacoco`, `cyclonedx`, `pitest`, `spring-javaformat`,
`errorprone`, `owasp`, and `jspecify`. Native `doctor` does not list PMD for the
reason described above.

## Generate reports and tasks

Run these commands after the main native analyzer pass. Use `--no-fail` so the
artifact generation continues even when the analyzers found issues.

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic
out=target/habit-hooks-dogfood-v0.1.9

habit-hooks report --format markdown --output "$out/report.md" --no-fail
habit-hooks report --format json --output "$out/report.json" --no-fail
habit-hooks report --format html --output "$out/report.html" --no-fail
habit-hooks report --format sarif --output "$out/report.sarif" --no-fail

habit-hooks tasks --format markdown --output "$out/tasks.md" --no-fail
habit-hooks tasks --format json --output "$out/tasks.json" --no-fail

habit-hooks dependencies --output "$out/dependencies.txt"
./mvnw --batch-mode --no-transfer-progress pmd:pmd -Dpmd.rulesets=pmd-ruleset.xml
cp target/pmd.xml "$out/pmd.xml"
```

The report commands each rerun the configured analyzers. That gives every report
format an independently reproducible native run, but it is expensive because
SpotBugs, JaCoCo, CycloneDX, PIT, Error Prone, and OWASP each invoke Maven.

## Evidence checklist

Before handing off the dogfood result, confirm these files exist:

```bash
cd /home/patbaumgartner/GitHub/spring-petclinic
out=target/habit-hooks-dogfood-v0.1.9

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
test -s target/spotbugsXml.xml
test -s target/site/jacoco/jacoco.xml
test -s target/bom.json
test -s target/pit-reports/mutations.xml
test -s target/habit-hooks/spring-javaformat.log
test -s target/habit-hooks/errorprone.log
test -s target/dependency-check-report.json
```

Also scan the logs for infrastructure failures:

```bash
rg -n "Exception|ERROR|MissingReflection|Cannot|NullPointerException|No Java files|Failed|WARN|aborted|failed|not ready|FAIL|unknown TokenTypes" \
  "$out/logs" "$out/report.md" "$out/tasks.md" "$out/dependencies.txt" || true
```

Analyzer findings are expected in a dogfood run. Infrastructure failures,
missing report files, unreadable reports, native-image reflection errors, and
empty analyzer lists are not expected.
