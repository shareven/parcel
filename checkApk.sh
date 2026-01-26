#!/bin/bash

echo "检查APK文件..."
VERSION=$(grep 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
APK_NAME="parcel-v${VERSION}.apk"

unzip -p ~/Desktop/${APK_NAME} META-INF/version-control-info.textproto

echo "当前APK的Git提交哈希为："
git rev-parse HEAD