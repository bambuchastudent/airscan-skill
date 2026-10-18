#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
VERSION=$(sed -n 's#^distributionUrl=.*/apache-maven/\([^/]*\)/.*#\1#p' "$ROOT/.mvn/wrapper/maven-wrapper.properties")
[ -n "$VERSION" ] || { echo "Missing Maven distributionUrl version" >&2; exit 1; }
BASE="${HOME}/.m2/wrapper/dists/apache-maven-${VERSION}"
MVN="${BASE}/apache-maven-${VERSION}/bin/mvn"
if [ ! -x "$MVN" ]; then
  mkdir -p "$BASE"
  URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${VERSION}/apache-maven-${VERSION}-bin.tar.gz"
  curl --fail --location --silent --show-error "$URL" -o "$BASE/maven.tar.gz"
  curl --fail --location --silent --show-error "$URL.sha512" -o "$BASE/maven.tar.gz.sha512"
  EXPECTED=$(awk '{print $1}' "$BASE/maven.tar.gz.sha512")
  ACTUAL=$(sha512sum "$BASE/maven.tar.gz" | awk '{print $1}')
  if [ "$EXPECTED" != "$ACTUAL" ]; then
    echo "Maven distribution checksum mismatch" >&2
    exit 1
  fi
  tar -xzf "$BASE/maven.tar.gz" -C "$BASE"
  rm -f "$BASE/maven.tar.gz" "$BASE/maven.tar.gz.sha512"
fi
cd "$ROOT"
exec "$MVN" "$@"
