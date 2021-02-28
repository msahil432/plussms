#!/bin/sh
set -e
set -u

# Build Script

echo ""
echo "1. Build APK -------------------"
./gradlew assembleDebug
echo "APK built"


echo ""
echo "2. Build Android AAB Bundle ----"
./gradlew bundleRelease
echo "AAB Bundle built"

