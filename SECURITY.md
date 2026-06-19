# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.x     | ✅ Active development |

## Reporting a Vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

Please report security issues by emailing the maintainer directly.
Include as much detail as possible:

- The nature of the vulnerability
- Steps to reproduce
- Potential impact
- Any suggested mitigations

You will receive a response within 72 hours.

## Scope

habit-hooks is a build-time / developer tooling CLI. It reads source files
and static analysis configurations from the local filesystem and writes output
to stdout. It does not:

- Open network connections
- Handle user authentication
- Process untrusted remote input at runtime

Security concerns most likely to affect habit-hooks:

- **Dependency vulnerabilities** — we run `./mvnw dependency:check` with OWASP
  in CI. File an issue if a CVE surfaces in our dependency tree.
- **Path traversal in config** — config files are read from the local filesystem
  under the control of the developer running the tool.
