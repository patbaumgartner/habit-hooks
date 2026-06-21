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
3. Write tests for every new behaviour. Coverage must not drop below 70 %.
4. Make sure `./mvnw verify` passes — Checkstyle, PMD, SpotBugs, and tests all run.
5. Run `habit-hooks --all` or the launcher JAR equivalent before marking the work done.
6. Keep commits small and focused. Each commit should compile and pass tests.
7. Open the PR using the pull request template.

## Code conventions

This project uses reference validation — habit-hooks runs against its own source code in CI.
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
   `src/main/resources/com/patbaumgartner/habithooks/prompts/<tool>-<RuleName>.md`
   (replace `:` with `-`, keep the original rule name casing)
3. Register the title in `RuleTitles.TITLES`
4. Add the rule ID to `PromptLoaderTest.allCoachingPromptFilesAreLoadable`
5. Add or update behavior tests when the rule requires parser or coaching changes
6. Document it in the README coached-rules table

## Running the tests

```bash
./mvnw test                    # unit tests only
./mvnw verify                  # full quality gate
habit-hooks doctor             # analyzer readiness check
habit-hooks --all              # reference validation before handoff
```

When a change touches CLI output, reports, task exports, or documentation, also
check the generated artifacts locally:

```bash
habit-hooks report --no-fail
habit-hooks tasks --no-fail
```

## Release checklist

Before publishing a release:

1. Confirm `./mvnw verify` passes on `main`.
2. Confirm CI workflows are green, including compatibility matrix.
3. Confirm release workflow still signs artifacts and verifies attestation.
4. Confirm README/CHANGELOG entries reflect user-visible changes.
5. Tag with `vX.Y.Z` and verify release assets include:
   - launcher JAR
   - sources JAR
   - CycloneDX SBOM (`bom.json`)
   - Cosign signatures and certificates (`.sig` + `.pem`)

Hotfixes should follow the same checklist with a patch version bump.

## Commit message convention

Use [Conventional Commits](https://www.conventionalcommits.org):

```text
feat: add SpotBugs analyzer wrapper
fix: handle missing Checkstyle config gracefully
docs: add coaching prompt for NPath complexity
test: cover baseline snooze with dirty working tree
chore: bump checkstyle to 10.21
```

## Repository administration

Use `scripts/repo-admin.sh` for GitHub policy changes. It enforces rebase-only
merges, disables merge commits and squash merges, and can optionally merge open
non-draft PRs with `--merge-open-prs`.

Example:

```bash
./scripts/repo-admin.sh --repo patbaumgartner/habbit-hooks --merge-open-prs
```

### Workflow action pin maintenance

GitHub Actions are pinned to immutable commit SHAs for supply-chain safety.

- Keep pins updated regularly (recommended: weekly Dependabot or manual review)
- Validate updated pins in a pull request before merging
- Never downgrade to tag-only references in workflows

## Code of Conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md).
