#!/bin/sh
# habit-hooks installer
#
# Installs habit-hooks into a per-user bin directory. Prefers a native
# binary for the current platform when one is published in the release;
# otherwise falls back to the fat JAR plus a small wrapper script.
#
# Usage (local):
#   ./scripts/install.sh
#
# Usage (curl-pipe):
#   curl -fsSL https://raw.githubusercontent.com/patbaumgartner/habit-hooks/main/scripts/install.sh | sh
#
# Environment overrides:
#   VERSION       Release tag/version to install (default: latest)
#                 Accepts "0.1.0" or "v0.1.0".
#   INSTALL_DIR   Install location (default: $HOME/.local/bin)
#   FORCE_JAR     Set to 1 to skip native binary and install the JAR
#   GITHUB_TOKEN  Optional token to raise GitHub API rate limits
#
# Exit codes:
#   0 success, 1 generic failure, 2 missing prerequisite

set -eu

REPO="patbaumgartner/habit-hooks"
INSTALL_DIR="${INSTALL_DIR:-$HOME/.local/bin}"
VERSION="${VERSION:-latest}"
FORCE_JAR="${FORCE_JAR:-0}"
MIN_JAVA="25"

# --- output helpers ---------------------------------------------------------

# Only colorize when stdout is a terminal.
if [ -t 1 ]; then
	C_RESET="$(printf '\033[0m')"
	C_BOLD="$(printf '\033[1m')"
	C_RED="$(printf '\033[31m')"
	C_GREEN="$(printf '\033[32m')"
	C_YELLOW="$(printf '\033[33m')"
else
	C_RESET=""; C_BOLD=""; C_RED=""; C_GREEN=""; C_YELLOW=""
fi

info()  { printf '%s\n' "${C_BOLD}==>${C_RESET} $*"; }
warn()  { printf '%s\n' "${C_YELLOW}warning:${C_RESET} $*" >&2; }
ok()    { printf '%s\n' "${C_GREEN}✓${C_RESET} $*"; }
die()   { printf '%s\n' "${C_RED}error:${C_RESET} $*" >&2; exit "${2:-1}"; }

# --- prerequisite detection -------------------------------------------------

have() { command -v "$1" >/dev/null 2>&1; }

# Pick a downloader. curl preferred, wget as fallback.
DOWNLOADER=""
if have curl; then
	DOWNLOADER="curl"
elif have wget; then
	DOWNLOADER="wget"
else
	die "need either curl or wget to download files" 2
fi

# fetch <url> <output-file>  (-> 0 ok, non-zero on HTTP error / failure)
fetch() {
	url="$1"; out="$2"
	if [ "$DOWNLOADER" = "curl" ]; then
		curl -fsSL ${GITHUB_TOKEN:+-H "Authorization: Bearer $GITHUB_TOKEN"} \
			-o "$out" "$url"
	else
		# wget: --header only added when token is set
		if [ -n "${GITHUB_TOKEN:-}" ]; then
			wget -q --header="Authorization: Bearer $GITHUB_TOKEN" -O "$out" "$url"
		else
			wget -q -O "$out" "$url"
		fi
	fi
}

# fetch_stdout <url>  (-> prints body to stdout, non-zero on failure)
fetch_stdout() {
	url="$1"
	if [ "$DOWNLOADER" = "curl" ]; then
		curl -fsSL ${GITHUB_TOKEN:+-H "Authorization: Bearer $GITHUB_TOKEN"} "$url"
	else
		if [ -n "${GITHUB_TOKEN:-}" ]; then
			wget -qO- --header="Authorization: Bearer $GITHUB_TOKEN" "$url"
		else
			wget -qO- "$url"
		fi
	fi
}

# remote_exists <url>  (-> 0 if a HEAD/range request succeeds)
remote_exists() {
	url="$1"
	if [ "$DOWNLOADER" = "curl" ]; then
		curl -fsSL ${GITHUB_TOKEN:+-H "Authorization: Bearer $GITHUB_TOKEN"} \
			-o /dev/null -r 0-0 "$url" 2>/dev/null
	else
		wget -q --spider "$url" 2>/dev/null
	fi
}

# --- platform detection -----------------------------------------------------

detect_os() {
	case "$(uname -s)" in
		Linux)  echo "linux" ;;
		Darwin) echo "darwin" ;;
		*)      echo "unsupported" ;;
	esac
}

detect_arch() {
	case "$(uname -m)" in
		x86_64|amd64)  echo "x64" ;;
		aarch64|arm64) echo "arm64" ;;
		*)             echo "unsupported" ;;
	esac
}

# --- version resolution -----------------------------------------------------

# Resolve the version into a concrete tag like "v0.1.0".
resolve_tag() {
	if [ "$VERSION" = "latest" ]; then
		api="https://api.github.com/repos/$REPO/releases/latest"
		body="$(fetch_stdout "$api")" \
			|| die "could not query latest release from GitHub API"
		tag="$(printf '%s\n' "$body" \
			| grep -m1 '"tag_name"' \
			| sed -E 's/.*"tag_name"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/')"
		[ -n "$tag" ] || die "no published release found for $REPO"
		echo "$tag"
	else
		# Normalize: ensure a single leading "v".
		case "$VERSION" in
			v*) echo "$VERSION" ;;
			*)  echo "v$VERSION" ;;
		esac
	fi
}

# --- java check (JAR path only) ---------------------------------------------

# Returns 0 when a Java >= MIN_JAVA runtime is available.
java_ok() {
	have java || return 1
	# `java -version` prints to stderr; formats vary across vendors.
	ver="$(java -version 2>&1 | head -n1 \
		| sed -E 's/.*version "([0-9]+).*/\1/')"
	case "$ver" in
		''|*[!0-9]*) return 1 ;;
	esac
	[ "$ver" -ge "$MIN_JAVA" ]
}

# --- install routines -------------------------------------------------------

ensure_install_dir() {
	mkdir -p "$INSTALL_DIR" || die "cannot create install dir: $INSTALL_DIR"
}

install_native() {
	tag="$1"; os="$2"; arch="$3"
	asset="habit-hooks-${os}-${arch}"
	url="https://github.com/$REPO/releases/download/$tag/$asset"

	remote_exists "$url" || return 1

	info "Installing native binary ($asset) ..."
	tmp="$(mktemp)" || die "cannot create temp file"
	# shellcheck disable=SC2064
	trap "rm -f '$tmp'" EXIT INT TERM
	fetch "$url" "$tmp" || die "download failed: $url"

	target="$INSTALL_DIR/habit-hooks"
	chmod +x "$tmp"
	mv "$tmp" "$target"
	trap - EXIT INT TERM
	ok "Installed native binary: $target"
	return 0
}

install_jar() {
	tag="$1"
	asset="habit-hooks-launcher.jar"
	url="https://github.com/$REPO/releases/download/$tag/$asset"

	if ! java_ok; then
		warn "Java $MIN_JAVA+ is required to run the JAR build but was not found."
		warn "Install a Java $MIN_JAVA runtime (e.g. BellSoft Liberica or GraalVM),"
		warn "or set FORCE_JAR=0 once native binaries are available for your platform."
		die "missing Java $MIN_JAVA runtime" 2
	fi

	info "Installing fat JAR ($asset) ..."
	jar_target="$INSTALL_DIR/habit-hooks.jar"
	tmp="$(mktemp)" || die "cannot create temp file"
	# shellcheck disable=SC2064
	trap "rm -f '$tmp'" EXIT INT TERM
	fetch "$url" "$tmp" || die "download failed: $url"
	mv "$tmp" "$jar_target"
	trap - EXIT INT TERM

	wrapper="$INSTALL_DIR/habit-hooks"
	cat > "$wrapper" <<EOF
#!/bin/sh
# Wrapper generated by habit-hooks install.sh
exec java -jar "$jar_target" "\$@"
EOF
	chmod +x "$wrapper"
	ok "Installed JAR:     $jar_target"
	ok "Installed wrapper: $wrapper"
}

# --- PATH advice ------------------------------------------------------------

path_hint() {
	case ":$PATH:" in
		*":$INSTALL_DIR:"*) : ;;
		*)
			warn "$INSTALL_DIR is not on your PATH."
			printf '%s\n' "  Add this to your shell profile:"
			printf '%s\n' "    export PATH=\"$INSTALL_DIR:\$PATH\""
			;;
	esac
}

# --- main -------------------------------------------------------------------

main() {
	ensure_install_dir

	tag="$(resolve_tag)"
	info "Target version: $tag"
	info "Install dir:    $INSTALL_DIR"

	os="$(detect_os)"
	arch="$(detect_arch)"

	installed_native=0
	if [ "$FORCE_JAR" != "1" ] && [ "$os" != "unsupported" ] && [ "$arch" != "unsupported" ]; then
		if install_native "$tag" "$os" "$arch"; then
			installed_native=1
		else
			info "No native binary for ${os}-${arch}; falling back to JAR."
		fi
	elif [ "$FORCE_JAR" = "1" ]; then
		info "FORCE_JAR=1 set; skipping native binary."
	else
		info "Unsupported platform for native binary; using JAR."
	fi

	if [ "$installed_native" -eq 0 ]; then
		install_jar "$tag"
	fi

	path_hint
	printf '\n'
	ok "Done. Run: ${C_BOLD}habit-hooks --version${C_RESET}"
}

main "$@"
