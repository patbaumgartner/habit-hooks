# Changelog

All notable changes to this project will be documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Initial Java implementation of habit-hooks
- Checkstyle analyzer wrapping with coached violation output
- PMD analyzer wrapping (including CPD duplication detection)
- Baseline management: `generate`, `status`, `snooze`, `prune`
- `init` command to scaffold `checkstyle.xml`, `pmd-ruleset.xml`, and `.habit-hooks.yaml`
- `--all`, `--last <n>`, `--branch`, `--since <hash>` scope flags
- Coached prompts for 14 rules across Checkstyle and PMD
- Taikai architecture-test scaffolding via `init --taikai`
- Self-dogfooding: habit-hooks runs against its own source in CI

[Unreleased]: https://github.com/patbaumgartner/habbit-hooks/compare/HEAD...HEAD
