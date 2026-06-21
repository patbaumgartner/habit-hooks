# Security Policy

## Supported Versions

- 0.x: Active development and supported

### Support window

- Latest release in `0.x`: full support (security + bug fixes)
- Previous release in `0.x`: best-effort security fixes only
- Older releases: no guaranteed fixes; upgrade recommended

## Reporting a Vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

Please report security issues through GitHub's private vulnerability reporting:

<https://github.com/patbaumgartner/habit-hooks/security/advisories/new>

Include as much detail as possible:

- The nature of the vulnerability
- Steps to reproduce
- Potential impact
- Any suggested mitigations

You will receive a response within 72 hours.

### Triage and remediation targets

- Initial acknowledgement: within 72 hours
- Triage severity classification: within 7 calendar days
- Critical severity: target fix or mitigation within 14 days
- High severity: target fix within 30 days
- Medium/Low severity: scheduled in normal release cadence

Timelines are targets, not legal guarantees, but we prioritize transparent
status updates throughout the process.

### Coordinated disclosure process

1. Reporter submits details privately.
2. Maintainer validates and assigns severity.
3. Fix is prepared and tested privately when needed.
4. Release is published with remediation details.
5. Public advisory/changelog notes are posted.

Please avoid public disclosure before a fix is available unless active
exploitation makes immediate disclosure necessary.

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

## CVE publication policy

For high-impact and ecosystem-relevant vulnerabilities, we may publish a CVE
and corresponding advisory. For lower-severity issues, remediation details may
be disclosed through release notes and changelog entries without a CVE.
