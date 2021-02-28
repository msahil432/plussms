#!/bin/sh
set -e
set -u

# Prepares APK and AAB files for Publishing

export API_TOKEN_GITHUB=$1
export KEYSTORE_REPO=$2
export KEYSTORE_ZIP_PASSWORD=$3
export KEYSTORE_KEY_VALUE=$4
export KEYSTORE_PASSWORD=$5

export FOLDER_NAME="$(echo "$KEYSTORE_REPO" |  cut -d'/' -f2)"

echo "Getting KeyStore for Signing -----------"
git clone "https://$API_TOKEN_GITHUB:x-oauth-basic@github.com/$KEYSTORE_REPO"
cd $FOLDER_NAME
unzip -P $KEYSTORE_ZIP_PASSWORD temp.zip

echo ""
echo "Signing AAB Bundle ---------------------"
export AAB_FILE="./../presentation/build/outputs/bundle/release/app-release.aab"
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore sahil432.jks $AAB_FILE $KEYSTORE_KEY_VALUE -storepass $KEYSTORE_PASSWORD
rm -rf $FOLDER_NAME
