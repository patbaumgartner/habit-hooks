# Artifact contracts

habit-hooks writes local artifacts under `target/habit-hooks` by default. Relative
`--output` paths resolve from the analyzed project root so agents get deterministic
paths even when launched from another process directory.

## Report artifacts

Command:

```bash
habit-hooks report --format markdown --no-fail
```

Formats:

- `markdown` or `md`: `report.md`
- `json`: `report.json`
- `html`: `report.html`
- `sarif`: `report.sarif`

`report.json` has this shape:

```json
{
  "generatedAt": "2026-06-21T00:00:00Z",
  "filesChecked": 1,
  "clean": false,
  "failing": true,
  "totalFindings": 1,
  "byTool": { "pmd": 1 },
  "byRule": { "pmd:GodClass": 1 },
  "byDimension": { "maintainability": 1 },
  "findings": [
    {
      "ruleId": "pmd:GodClass",
      "tool": "pmd",
      "dimension": "maintainability",
      "severity": "low",
      "file": "Big.java",
      "line": 7,
      "message": "Too big"
    }
  ]
}
```

Markdown and HTML reports include local trend deltas when
`target/habit-hooks/history/latest.json` already exists. Trend summaries include
total finding delta and per-dimension deltas.

## Task artifacts

Command:

```bash
habit-hooks tasks --format json --no-fail
```

Formats:

- `markdown` or `md`: `tasks.md`
- `json`: `tasks.json`

`tasks.json` has this shape:

```json
[
  {
    "id": "HH-001",
    "title": "Fix pmd:GodClass",
    "ruleId": "pmd:GodClass",
    "dimension": "maintainability",
    "severity": "low",
    "count": 1,
    "verificationCommand": "habit-hooks --all",
    "acceptanceCriteria": [
      "Resolve all current findings for pmd:GodClass.",
      "Keep the change focused and behavior-preserving unless the finding exposes a real bug.",
      "Re-run habit-hooks --all and confirm the rule no longer appears."
    ],
    "locations": ["Big.java:7"]
  }
]
```

Unknown formats fail with exit code `2`. Use `--no-fail` when generating artifacts
for review while findings are still present.

## Dependency report

Command:

```bash
habit-hooks dependencies --output target/habit-hooks/dependencies.txt
```

The dependency report is plain text output from the Maven Versions Plugin. The
`--apply` mode updates parent/property versions only; `--allow-major` opts into
major updates when applying.
