#!/usr/bin/env bash
#
# Regenerates docs/coached-rules-badge.json from the bundled coaching prompts.
#
# A "coached rule" is any prompt file that is not an analyzer meta prompt
# (goal-failed / lifecycle-blocked / report-missing / report-unreadable). The
# count powers the Shields.io endpoint badge shown in the README.
#
# Usage:
#   scripts/coached-rules-count.sh          # rewrite the badge JSON
#   scripts/coached-rules-count.sh --check  # print the count only, no writes
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
prompts_dir="$repo_root/src/main/resources/com/patbaumgartner/habithooks/prompts"
badge_file="$repo_root/docs/coached-rules-badge.json"

meta_pattern='-(goal-failed|lifecycle-blocked|report-missing|report-unreadable)\.md$'

total=$(find "$prompts_dir" -maxdepth 1 -name '*.md' | wc -l | tr -d ' ')
meta=$(find "$prompts_dir" -maxdepth 1 -name '*.md' | grep -E -- "$meta_pattern" | wc -l | tr -d ' ')
coached=$((total - meta))

if [[ "${1:-}" == "--check" ]]; then
  echo "$coached"
  exit 0
fi

cat >"$badge_file" <<JSON
{
  "schemaVersion": 1,
  "label": "coached rules",
  "message": "$coached",
  "color": "blue"
}
JSON

echo "Wrote $badge_file (coached rules: $coached)"
