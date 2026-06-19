# Contributing to habit-hooks

Thank you for considering contributing! This project follows software craftsmanship
principles — small methods, high cohesion, meaningful names, and no dead code.

## How to contribute

### Reporting bugs

Open an issue using the **Bug report** template. Include:

- habit-hooks version (`habit-hooks --version`)
- Java version (`java -version`)
- Your `checkstyle.xml` and `pmd-ruleset.xml` (if relevant)
- The full command output

### Requesting features

Open an issue using the **Feature request** template. Explain the use case first,
then the proposed solution. Discuss before implementing large changes.

### Submitting a pull request

1. **Comment on the issue** you'd like to work on so a maintainer can confirm
   the direction before you invest time coding.
2. Fork the repository and create a branch from `main`.
3. Write tests for every new behaviour. Coverage must not drop below 60 %.
4. Make sure `./mvnw verify` passes — Checkstyle, PMD, SpotBugs, and tests all run.
5. Keep commits small and focused. Each commit should compile and pass tests.
6. Open the PR using the pull request template.

## Code conventions

This project self-dogfoods — habit-hooks runs against its own source code in CI.
The conventions are enforced mechanically:

- **Method length ≤ 25 lines** — if you're writing more, extract a helper
- **Parameters ≤ 5** — introduce a parameter object if you need more
- **Cyclomatic complexity ≤ 8** — flatten conditions or extract polymorphism
- **No star imports** — every import must be explicit
- **No magic numbers** — use named constants
- **No public mutable fields** — encapsulate state

Additionally:

- Prefer **records** for immutable data, plain POJOs for Jackson-deserialized config
- Return **`Optional`** instead of `null` for optional single values
- Return **empty collections** instead of `null` for absent lists
- Catch only **specific exceptions** — never `Exception` or `Throwable`
- Write **unit tests** that cover the logic, not the framework

## Adding a coached rule

1. Add the rule to `checkstyle.xml` or `pmd-ruleset.xml`
2. Create the coaching prompt at
   `src/main/resources/io/github/patbaumgartner/habithooks/prompts/<tool>-<RuleName>.md`  
   (replace `:` with `-`, keep the original rule name casing)
3. Register the title in `RuleTitles.TITLES`
4. Add a test in `CoachingEngineTest`
5. Document it in the README table

## Running the tests

```bash
./mvnw test                    # unit tests only
./mvnw verify                  # full quality gate
```

## Commit message convention

Use [Conventional Commits](https://www.conventionalcommits.org):

```
feat: add SpotBugs analyzer wrapper
fix: handle missing Checkstyle config gracefully
docs: add coaching prompt for NPath complexity
test: cover baseline snooze with dirty working tree
chore: bump checkstyle to 10.21
```

## Code of Conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md).
