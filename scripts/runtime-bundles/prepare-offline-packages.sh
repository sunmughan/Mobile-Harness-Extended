#!/usr/bin/env bash
set -euo pipefail

# Build architecture-correct Debian package bundles for the offline APK.
# This runs on the CI build host; packages are downloaded at build time and
# installed later without network access inside the ARM64 Ubuntu rootfs.
OUT_DIR="${1:-dist/runtime-bundles}"
WORK="${RUNNER_TEMP:-/tmp}/mobile-harness-offline-packages"
mkdir -p "$OUT_DIR"
rm -rf "$WORK" && mkdir -p "$WORK"

export DEBIAN_FRONTEND=noninteractive
dpkg --add-architecture arm64 || true

cat >/etc/apt/sources.list.d/mobile-harness-arm64.list <<'EOF'
deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports focal main universe
deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports focal-updates main universe
deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports focal-security main universe
EOF

apt-get update

download_group() {
  local group="$1"
  shift
  local dir="$WORK/$group"
  mkdir -p "$dir"
  pushd "$dir" >/dev/null
  apt-get -y --download-only \
    -o APT::Architecture=arm64 \
    -o APT::Get::Download-Only=true \
    -o APT::Install-Recommends=false \
    install "$@"
  popd >/dev/null
}

# Keep the package set explicit. Dependencies are resolved by APT and every
# resulting .deb is carried into the APK.
download_group cpp build-essential:arm64 cmake:arm64 gdb:arm64
download_group php php-cli:arm64 php-mbstring:arm64 php-xml:arm64 php-curl:arm64 php-zip:arm64 composer

make_bundle() {
  local group="$1"
  local output="$2"
  local staging="$WORK/staging-$group"
  rm -rf "$staging"
  mkdir -p "$staging/var/cache/pocketdev-offline/$group"
  find "$WORK/$group" -maxdepth 1 -type f -name '*.deb' -exec cp -f {} "$staging/var/cache/pocketdev-offline/$group/" \;
  test -n "$(find "$staging/var/cache/pocketdev-offline/$group" -name '*.deb' -print -quit)"
  tar --sort=name --mtime='2026-10-03T00:00:00Z' --owner=0 --group=0 --numeric-owner \
    -C "$staging" -cf - . | zstd -19 -T0 -o "$OUT_DIR/$output"
}

make_bundle cpp pocketdev-cpp-packages-arm64-ubuntu20.04.tar.zst
make_bundle php pocketdev-php-packages-arm64-ubuntu20.04.tar.zst

CPP_SHA=$(sha256sum "$OUT_DIR/pocketdev-cpp-packages-arm64-ubuntu20.04.tar.zst" | awk '{print $1}')
PHP_SHA=$(sha256sum "$OUT_DIR/pocketdev-php-packages-arm64-ubuntu20.04.tar.zst" | awk '{print $1}')
CPP_SIZE=$(stat -c '%s' "$OUT_DIR/pocketdev-cpp-packages-arm64-ubuntu20.04.tar.zst")
PHP_SIZE=$(stat -c '%s' "$OUT_DIR/pocketdev-php-packages-arm64-ubuntu20.04.tar.zst")

cat >"$OUT_DIR/offline-toolchains-manifest.json" <<EOF
{
  "cpp": {
    "file": "pocketdev-cpp-packages-arm64-ubuntu20.04.tar.zst",
    "sha256": "$CPP_SHA",
    "compressedBytes": $CPP_SIZE
  },
  "php": {
    "file": "pocketdev-php-packages-arm64-ubuntu20.04.tar.zst",
    "sha256": "$PHP_SHA",
    "compressedBytes": $PHP_SIZE
  }
}
EOF

rm -f /etc/apt/sources.list.d/mobile-harness-arm64.list
apt-get clean
