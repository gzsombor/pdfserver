# Release Process

PdfServer is published to Maven Central via the new Central Portal
(<https://central.sonatype.com>). Releases are built and published **locally**;
CI only runs the test suite.

## Prerequisites

- Maven 3.9+ and JDK 21+ (`mise.toml` pins these)
- A GPG key pair. The public key must be uploaded to a public keyserver so Maven
  Central can validate the signatures:
  ```
  gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
  ```
- A central.sonatype.com account with the `com.github.gzsombor` namespace
  verified, and a **User Token** (generated under *Account → User Token*).

## Before the first release

Create `maven-central.properties` in the project root (it is gitignored):

```
central.username=<the-token-username>
central.password=<the-token-password>
```

Alternatively, export `CENTRAL_USERNAME` and `CENTRAL_PASSWORD` environment
variables before publishing.

## Publishing a release

1. Bump the version in `pom.xml` (e.g. `0.2` → `0.3`) and commit.
2. Tag the release and push it:
   ```
   git tag 0.3
   git push origin 0.3
   ```
3. Do a dry run to build, sign, and run the tests without publishing:
   ```
   ./scripts/release.sh --dry-run
   ```
4. Publish:
   ```
   ./scripts/release.sh
   ```

The script resolves your GPG key id automatically (from `~/.m2/settings.xml` or
your default secret key). If your key has a passphrase and `gpg-agent` is not
set up to prompt, pass it via the `GPG_PASSPHRASE` environment variable or the
`--passphrase` option. `autoPublish` is enabled, so artifacts are published
immediately; no manual approval is needed in the Central Portal.

## Notes

- `scripts/release.sh` writes a temporary `settings.xml` with your credentials
  that is removed automatically when the script exits.
- Artifacts are signed as part of the `verify` phase via `maven-gpg-plugin`.