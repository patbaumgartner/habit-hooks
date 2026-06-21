#!/usr/bin/env bash
#
# cleanup-code.sh — Update dependencies, rewrite code, and validate the project.
#
# Usage:
#   ./scripts/cleanup-code.sh [--skip-rewrite] [--skip-format] [--skip-verify] [--dry-run]
#
# What it does (in order):
#   1. Update parent POM and dependency version properties
#   2. Sort and tidy pom.xml
#   3. Run OpenRewrite: code cleanup, SLF4J best practices, Mockito best practices
#   4. Remove unused imports and order remaining imports
#   5. Apply Spring Java Format
#   6. Run the full quality gate (verify)
#   7. Reference validation: run habit-hooks against itself
#
# Requires: Java 25+, Maven 3.9+ on $PATH or present as ./mvnw

set -euo pipefail

# --- Configuration -----------------------------------------------------------

readonly VERSION_IGNORE='.*[-_\.](alpha|Alpha|ALPHA|b|beta|Beta|BETA|rc|RC|M|EA)[-_\.]?[0-9]*'

readonly REWRITE_RECIPES=(
    "org.openrewrite.staticanalysis.CodeCleanup"
    "org.openrewrite.java.logging.slf4j.Slf4jBestPractices"
    "org.openrewrite.java.testing.mockito.MockitoBestPractices"
)

readonly REWRITE_ARTIFACTS=(
    "org.openrewrite.recipe:rewrite-static-analysis:LATEST"
    "org.openrewrite.recipe:rewrite-logging-frameworks:LATEST"
    "org.openrewrite.recipe:rewrite-testing-frameworks:LATEST"
)

SKIP_REWRITE=false
SKIP_FORMAT=false
SKIP_VERIFY=false
DRY_RUN=false

# --- Helpers -----------------------------------------------------------------

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BOLD='\033[1m'
RESET='\033[0m'

log()  { printf "${GREEN}[INFO]${RESET}  %s\n" "$*"; }
warn() { printf "${YELLOW}[WARN]${RESET}  %s\n" "$*" >&2; }
step() { printf "${BOLD}  -> %s${RESET}\n" "$*"; }

join_by() { local IFS="$1"; shift; echo "$*"; }

run() {
    if [[ "$DRY_RUN" == true ]]; then
        printf '[DRY]  '
        printf '%q ' "$@"
        printf '\n'
        return 0
    fi
    "$@"
}

# --- Argument parsing --------------------------------------------------------

while [[ $# -gt 0 ]]; do
    case "$1" in
        --skip-rewrite) SKIP_REWRITE=true; shift ;;
        --skip-format)  SKIP_FORMAT=true;  shift ;;
        --skip-verify)  SKIP_VERIFY=true;  shift ;;
        --dry-run)      DRY_RUN=true;      shift ;;
        -h|--help)
            sed -n '3,12p' "$0"
            exit 0
            ;;
        *)
            printf "${RED}[ERROR]${RESET} Unknown argument: %s\n" "$1" >&2
            exit 1
            ;;
    esac
done

# --- Validation --------------------------------------------------------------

MVN="./mvnw"
if [[ ! -x "$MVN" ]]; then
    MVN="$(command -v mvn 2>/dev/null || true)"
    [[ -n "$MVN" ]] || { printf "${RED}[ERROR]${RESET} No Maven wrapper or 'mvn' found\n" >&2; exit 1; }
fi

# --- Main --------------------------------------------------------------------

log "habit-hooks code cleanup starting"

step "Updating parent POM version"
run "$MVN" -q org.codehaus.mojo:versions-maven-plugin:update-parent \
    -DallowSnapshots=false \
    -DgenerateBackupPoms=false \
    "-Dmaven.version.ignore=$VERSION_IGNORE"

step "Updating dependency version properties"
run "$MVN" -q org.codehaus.mojo:versions-maven-plugin:update-properties \
    -DallowSnapshots=false \
    -DallowMajorUpdates=true \
    -DallowMinorUpdates=true \
    -DallowIncrementalUpdates=true \
    "-Dmaven.version.ignore=$VERSION_IGNORE"

step "Tidying POM"
run "$MVN" -q tidy:pom

step "Sorting POM"
run "$MVN" -q -U com.github.ekryd.sortpom:sortpom-maven-plugin:sort \
    -Dsort.predefinedSortOrder=custom_1

if [[ "$SKIP_REWRITE" == false ]]; then
    step "Running OpenRewrite recipes: ${REWRITE_RECIPES[*]}"
    run "$MVN" -q \
        -Dmaven.gitcommitid.skip=true \
        -Dspring-javaformat.validate.skip=true \
        org.openrewrite.maven:rewrite-maven-plugin:run \
        "-Drewrite.activeRecipes=$(join_by , "${REWRITE_RECIPES[@]}")" \
        "-Drewrite.recipeArtifactCoordinates=$(join_by , "${REWRITE_ARTIFACTS[@]}")"

    step "Removing unused imports and ordering"
    run "$MVN" -q \
        -Dmaven.gitcommitid.skip=true \
        -Dspring-javaformat.validate.skip=true \
        org.openrewrite.maven:rewrite-maven-plugin:run \
        -Drewrite.activeRecipes=org.openrewrite.java.RemoveUnusedImports,org.openrewrite.java.OrderImports
fi

if [[ "$SKIP_FORMAT" == false ]]; then
    step "Applying Spring Java Format"
    run "$MVN" -q spring-javaformat:apply
fi

step "Cleaning up backup files"
if [[ "$DRY_RUN" == true ]]; then
    printf '[DRY]  rm -f pom.xml.versionsBackup pom.xml.bak\n'
else
    rm -f pom.xml.versionsBackup pom.xml.bak
fi

if [[ "$SKIP_VERIFY" == false ]]; then
    step "Running full quality gate (clean verify)"
    run "$MVN" clean verify

    step "Reference validation: running habit-hooks against itself"
    run java -jar target/habit-hooks-*-launcher.jar --all
fi

log "Cleanup complete"
