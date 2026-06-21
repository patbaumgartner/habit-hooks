# Changelog

<!-- markdownlint-disable MD024 -->

All notable changes to this project will be documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.6] - 2026-06-21

### Fixed

- Native Checkstyle analysis now registers Apache BeanUtils array converter targets required while configuring checks

## [0.1.5] - 2026-06-21

### Fixed

- Native Checkstyle analysis now bundles Checkstyle DTD resources needed for XML configuration parsing
- Native PMD analysis now registers scaffolded Java rule implementations for reflection

## [0.1.4] - 2026-06-21

### Fixed

- Native binaries now read baseline and report history JSON models without Jackson reflection failures
- Native PMD analysis now includes bundled Java ruleset resources
- Checkstyle config loading now uses a file URI system ID so native XML parsing can resolve DTDs reliably

## [0.1.3] - 2026-06-21

### Fixed

- Native binaries now deserialize generated `.habit-hooks.yaml` config files after `habit-hooks init`
- Config parse failures now log concise warnings instead of stack traces before falling back to defaults

## [0.1.2] - 2026-06-21

### Added

- Maven-backed project analyzers for SpotBugs, JaCoCo, CycloneDX, OWASP Dependency Check, PIT, Spring Java Format, and Error Prone
- JSpecify adoption analyzer for nullness annotation setup
- Built-in coaching prompts for Maven-backed analyzer meta-rules and JSpecify adoption findings
- Local quality reports in Markdown, JSON, HTML, and SARIF via `habit-hooks report`
- Prioritized agent task export with verification commands and acceptance criteria via `habit-hooks tasks`
- Analyzer prerequisite checks via `habit-hooks doctor`
- Maven dependency and plugin update reporting via `habit-hooks dependencies`
- Optional Maven plugin/dependency snippet scaffolding via `habit-hooks init --maven-snippets`
- Agent integration, artifact contract, and release smoke documentation under `docs/`
- Coaching prompts for scaffolded whitespace and PMD design rules surfaced by Spring Petclinic dogfooding

### Changed

- README now documents the expanded analyzer configuration, coached prompt coverage, rule severity semantics, and Maven snippet workflow
- Report and task format flags now fail fast on unknown values and use `.md` for Markdown artifacts
- Report and task output paths now accept either a custom directory or exact artifact file
- Report, task, and dependency output paths now resolve relative to the analyzed project root
- Artifact command help and README examples now document `--output`, `--no-fail`, and dependency update safety flags consistently
- Report summary maps now render in deterministic key order for stable human and agent artifacts
- Markdown/HTML reports now surface local trend deltas when a previous snapshot exists
- Markdown/HTML trend reports now include per-dimension deltas
- `habit-hooks init` now points users to the installed `habit-hooks --all` command after scaffolding

### Fixed

- JaCoCo XML reports with a standard DOCTYPE now parse without allowing external entity resolution
- `habit-hooks doctor` now marks Taikai unavailable when a generated Taikai test exists but the build does not declare the Taikai dependency
- Analyzer skip warnings now direct users to `habit-hooks doctor` instead of assuming a missing config file
- Generated Taikai architecture tests now follow Spring Java Format out of the box
- Generated Checkstyle starter configs no longer enable formatting-only tab and separator rules that conflict with Spring Java Format projects
- SARIF reports now include `tool.driver.rules` metadata for code-scanning consumers

## [0.1.0] - 2026-06-21

### Added

- Initial Java implementation of habit-hooks
- Checkstyle analyzer wrapping with coached violation output
- PMD analyzer wrapping (including CPD duplication detection)
- Baseline management: `status`, `snooze`, `prune`
- `init` command to scaffold `checkstyle.xml`, `pmd-ruleset.xml`, and `.habit-hooks.yaml`
- `--all`, `--last <n>`, `--branch`, `--since <hash>` scope flags
- Coached prompts for 22 rules across Checkstyle and PMD
- Taikai architecture-test scaffolding via `init --taikai`
- Self-dogfooding: habit-hooks runs against its own source in CI
- Architecture reference document (`ARCHITECTURE.md`)
- ADRs for native distribution, supply chain controls, baseline model, and quality-gate policy
- CodeQL workflow for Java security analysis
- Native startup benchmark job in CI
- Integration tests (`*IT.java`) via Maven Failsafe
- Optional PIT mutation testing profile (`-Pmutation-test`)
- ADR-0008 defining resilient configuration defaults and deterministic config-path behavior

### Fixed

- `build/` output directories (Gradle projects) are now excluded from file scope alongside `target/`
- Missing analyzer config files (e.g. `checkstyle.xml`, `pmd-ruleset.xml`) now emit a `[WARN]` with a scaffold hint instead of silently passing
- `--help` now works on all subcommands (`init`, `baseline`, `baseline snooze`, `baseline prune`, `baseline status`)
- Redundant `INFO` log lines from `BaselineManager` no longer appear in CLI output
- Internal `slf4j-simple` log format configured: only `WARN`/`ERROR` shown, without thread or class noise
- `TaikaiAnalyzer` no longer silently passes when Maven exits with errors (e.g. missing Taikai dependency); a `[WARN]` is now emitted with guidance

### Changed

- Raised JaCoCo line coverage gate from 40% to 70%
- Added Spring Java Format (`spring-javaformat-maven-plugin`) check in `validate` phase
- Enabled reproducible build timestamps for jar and shaded artifacts
- `Analyzer` is now a sealed interface with exhaustive analyzer selection in orchestrator
- Config handling now normalizes null/blank values to safe defaults
- `--config <path>` relative paths now resolve from the current working directory
- Documentation refresh across README, ARCHITECTURE, AGENTS, and CLAUDE for concise defaults-first guidance

[Unreleased]: https://github.com/patbaumgartner/habbit-hooks/compare/v0.1.6...HEAD
[0.1.6]: https://github.com/patbaumgartner/habbit-hooks/releases/tag/v0.1.6
[0.1.5]: https://github.com/patbaumgartner/habbit-hooks/releases/tag/v0.1.5
[0.1.4]: https://github.com/patbaumgartner/habbit-hooks/releases/tag/v0.1.4
[0.1.3]: https://github.com/patbaumgartner/habbit-hooks/releases/tag/v0.1.3
[0.1.2]: https://github.com/patbaumgartner/habbit-hooks/releases/tag/v0.1.2
[0.1.0]: https://github.com/patbaumgartner/habbit-hooks/releases/tag/v0.1.0
