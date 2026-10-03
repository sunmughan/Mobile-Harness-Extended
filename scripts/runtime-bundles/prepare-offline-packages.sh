#!/usr/bin/env bash
set -euo pipefail

# Build one ARM64 Ubuntu 20.04 overlay containing the complete C/C++ and PHP
# package sets. The build downloads/packages everything in CI; the phone never
# invokes apt, Composer, or any other package download during offline setup.
OUT_DIR="${1:-dist/runtime-bundles}"
WORK="${RUNNER_TEMP:-/tmp}/mobile-harness-offline-packages"
IMAGE="ubuntu:20.04"
BUNDLE="pocketdev-offline-packages-arm64-ubuntu20.04.tar.zst"
mkdir -p "$OUT_DIR"
rm -rf "$WORK"
mkdir -p "$WORK"

CID="$(docker create --platform linux/arm64 "$IMAGE" sleep infinity)"
cleanup() {
  docker rm -f "$CID" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker start "$CID" >/dev/null
docker exec "$CID" bash -lc '
  set -euo pipefail
  export DEBIAN_FRONTEND=noninteractive
  apt-get update
  apt-get install -y --no-install-recommends \
    build-essential cmake gdb \
    php-cli php-mbstring php-xml php-curl php-zip composer
  apt-get clean
  rm -rf /var/lib/apt/lists/* /var/cache/apt/* /tmp/* /var/tmp/*
'

docker export "$CID" | zstd -19 -T0 -o "$OUT_DIR/$BUNDLE"

SHA256="$(sha256sum "$OUT_DIR/$BUNDLE" | awk '{print $1}')"
SIZE="$(stat -c '%s' "$OUT_DIR/$BUNDLE")"

cat >"$OUT_DIR/offline-toolchains-manifest.json" <<EOF
{
  "cpp": {
    "file": "$BUNDLE",
    "sha256": "$SHA256",
    "compressedBytes": $SIZE
  },
  "php": {
    "file": "$BUNDLE",
    "sha256": "$SHA256",
    "compressedBytes": $SIZE
  }
}
EOF

echo "Offline C/C++ + PHP bundle: $OUT_DIR/$BUNDLE"
echo "SHA-256: $SHA256"
echo "Size: $SIZE bytes"
