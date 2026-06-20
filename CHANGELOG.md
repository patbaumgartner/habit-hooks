# Changelog

All notable changes to this project will be documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-06-20

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

[Unreleased]: https://github.com/patbaumgartner/habbit-hooks/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/patbaumgartner/habbit-hooks/releases/tag/v0.1.0
