#!/usr/bin/env bash
set -euxo pipefail

mkdir -p emulator-logs

for variant in online offline; do
  apk="app/build/outputs/apk/${variant}/release/app-${variant}-release.apk"
  test -f "$apk"

  adb logcat -c
  adb uninstall com.jarves.mh || true
  adb install "$apk"

  adb shell am force-stop com.jarves.mh
  adb shell monkey -p com.jarves.mh 1
  sleep 12

  adb shell dumpsys activity activities > "emulator-logs/${variant}-activities.txt"
  adb shell dumpsys package com.jarves.mh > "emulator-logs/${variant}-package.txt"
  adb shell pidof com.jarves.mh > "emulator-logs/${variant}-pid.txt" || true
  adb logcat -d -v threadtime > "emulator-logs/${variant}-logcat.txt"

  if ! grep -qE '[0-9]+' "emulator-logs/${variant}-pid.txt"; then
    echo "Application process is not alive after launch: ${variant}"
    tail -n 300 "emulator-logs/${variant}-logcat.txt"
    exit 1
  fi

  if grep -qE 'FATAL EXCEPTION|Process: com\.jarves\.mh, PID:|AndroidRuntime.*FATAL' "emulator-logs/${variant}-logcat.txt"; then
    echo "Fatal Android runtime exception detected: ${variant}"
    grep -nE 'FATAL EXCEPTION|Process: com\.jarves\.mh, PID:|AndroidRuntime.*FATAL' "emulator-logs/${variant}-logcat.txt" || true
    tail -n 300 "emulator-logs/${variant}-logcat.txt"
    exit 1
  fi
done
