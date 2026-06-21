# Release smoke checklist

Run this checklist before tagging a release or after release assets are published.

## Local launcher

```bash
./mvnw -q verify
./mvnw -q package -DskipTests
java -jar target/habit-hooks-*-launcher.jar --version
java -jar target/habit-hooks-*-launcher.jar --help
java -jar target/habit-hooks-*-launcher.jar init --dry-run
java -jar target/habit-hooks-*-launcher.jar doctor
java -jar target/habit-hooks-*-launcher.jar --all
java -jar target/habit-hooks-*-launcher.jar report --format json --no-fail
java -jar target/habit-hooks-*-launcher.jar tasks --format json --no-fail
```

Expected result: commands complete without unexpected stack traces, generated
artifacts land under `target/habit-hooks`, and `habit-hooks --all` is clean for
this repository.

## Install script

Use a temporary install directory:

```bash
INSTALL_DIR="$(mktemp -d)"
INSTALL_DIR="$INSTALL_DIR" sh scripts/install.sh
"$INSTALL_DIR/habit-hooks" --version
"$INSTALL_DIR/habit-hooks" --help
```

For a JAR fallback path:

```bash
INSTALL_DIR="$(mktemp -d)"
INSTALL_DIR="$INSTALL_DIR" FORCE_JAR=1 sh scripts/install.sh
"$INSTALL_DIR/habit-hooks" --version
```

## Native image

When GraalVM native-image is available:

```bash
./mvnw -q -Pnative package -DskipTests
target/habit-hooks --version
target/habit-hooks --help
target/habit-hooks report --format sarif --no-fail
```

Native and JAR output should agree for `--version`, `--help`, `report`, `tasks`,
`doctor`, and the default quality gate.
