#!/usr/bin/env bash
#
# Publish pdfserver to Maven Central (central.sonatype.com / Central Portal)
#
# Requirements:
#   - Maven 3.9+ and JDK 17+
#   - A GPG key whose public key has been uploaded to a public keyserver
#   - A central.sonatype.com account with the com.github.gzsombor namespace verified
#     and a User Token (token + username) for publishing
#
# Credentials:
#   The script reads the Central Portal credentials from the environment, or finds
#   them in a local file. Provide either:
#     1. Environment variables:  CENTRAL_USERNAME and CENTRAL_PASSWORD
#     2. A properties file at ./maven-central.properties with:
#           central.username=...
#           central.password=...
#
# GPG passphrase:
#   If your key has a passphrase, it will be prompted for (or read from the env
#   var GPG_PASSPHRASE). The public key ID defaults to the key configured in
#   ~/.m2/settings.xml, or use the --key-id option.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
CRED_FILE="$PROJECT_DIR/maven-central.properties"

KEY_ID=""
PASSPHRASE=""
DRY_RUN=""

usage() {
  cat <<'EOF'
Usage: release.sh [options]

  --key-id HEXID     GPG key id to sign with (overrides settings.xml)
  --passphrase XXX   GPG key passphrase (avoid; use env GPG_PASSPHRASE instead)
  --dry-run          Build, sign and package but do NOT publish
  -h, --help         Show this help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --key-id)      KEY_ID="$2"; shift 2 ;;
    --passphrase)  PASSPHRASE="$2"; shift 2 ;;
    --dry-run)     DRY_RUN=1; shift ;;
    -h|--help)     usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

cd "$PROJECT_DIR"

echo "==> Verifying prerequisites"
command -v mvn >/dev/null || { echo "ERROR: mvn not found" >&2; exit 1; }
command -v gpg  >/dev/null || { echo "ERROR: gpg not found" >&2; exit 1; }

# Determine GPG key id
if [[ -z "$KEY_ID" ]]; then
  KEY_ID=$(grep -A6 'gpg' "$HOME/.m2/settings.xml" 2>/dev/null \
    | grep -oP '<keyname>\K[^<]+' | head -1 || true)
fi
if [[ -z "$KEY_ID" ]]; then
  KEY_ID=$(gpg --list-secret-keys --with-colons 2>/dev/null \
    | awk -F: '$1=="sec"{print $5; exit}')
fi
if [[ -z "$KEY_ID" ]]; then
  echo "ERROR: could not determine GPG key id." >&2
  echo "       Specify it with --key-id HEXID" >&2
  exit 1
fi
echo "    using GPG key: $KEY_ID"

# GPG passphrase
if [[ -z "$PASSPHRASE" ]]; then
  PASSPHRASE="${GPG_PASSPHRASE:-}"
fi
if [[ -n "$PASSPHRASE" ]]; then
  echo "${PASSPHRASE}" | gpg --batch --yes --passphrase-fd 0 \
    --local-user "$KEY_ID" --output /dev/null --sign "$0" \
    && echo "    GPG key unlocked" \
    || { echo "ERROR: GPG signing test failed" >&2; exit 1; }
else
  echo "    GPG passphrase will be asked by gpg-agent"
fi

# Central Portal credentials
CENTRAL_USERNAME="${CENTRAL_USERNAME:-}"
CENTRAL_PASSWORD="${CENTRAL_PASSWORD:-}"
if [[ -z "$CENTRAL_USERNAME" && -f "$CRED_FILE" ]]; then
  while IFS= read -r line; do
    [[ -z "$line" || "$line" == \#* ]] && continue
    key="${line%%=*}"
    value="${line#*=}"
    case "$key" in
      central.username) CENTRAL_USERNAME="$value" ;;
      central.password) CENTRAL_PASSWORD="$value" ;;
    esac
  done < "$CRED_FILE"
fi

GPG_ARGS=(-Dgpg.keyname="$KEY_ID")
MAVEN_ARGS=()
if [[ -n "$PASSPHRASE" ]]; then
  GPG_ARGS+=(-Dgpg.passphrase="$PASSPHRASE")
fi

if [[ -n "$DRY_RUN" ]]; then
  echo "==> Dry run: building/signing but NOT publishing"
  mvn -B clean verify "${GPG_ARGS[@]}"
  echo "==> Done (dry run). Artifacts are in target/"
  exit 0
fi

if [[ -z "$CENTRAL_USERNAME" || -z "$CENTRAL_PASSWORD" ]]; then
  echo "ERROR: Central Portal credentials missing." >&2
  echo "       Set CENTRAL_USERNAME and CENTRAL_PASSWORD env vars, or" >&2
  echo "       create $CRED_FILE with central.username/central.password" >&2
  exit 1
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

cat > "$TMP_DIR/settings.xml" <<EOF
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>central</id>
      <username>${CENTRAL_USERNAME}</username>
      <password>${CENTRAL_PASSWORD}</password>
    </server>
  </servers>
</settings>
EOF
chmod 600 "$TMP_DIR/settings.xml"

echo "==> Publishing to Maven Central (auto-publish enabled)"
mvn -B -s "$TMP_DIR/settings.xml" \
    clean deploy \
    "${GPG_ARGS[@]}" \
    -Dcentral-publishing.publish=true \
    -DskipTests=false

echo "==> Release published. Verify at https://central.sonatype.com"
