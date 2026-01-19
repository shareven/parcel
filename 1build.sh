#!/bin/bash


VERSION=$(grep 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
APK_NAME="parcel-v${VERSION}.apk"

# 更新 README.md 中的版本号
sed -i '' "s/最新版本：v[0-9.]*/最新版本：v${VERSION}/" README.md
echo "README.md 版本号已更新: v${VERSION}"

# 构建Release版本
echo "开始构建Parcel Release版本..."
./gradlew assembleRelease

# 检查构建是否成功
if [ $? -eq 0 ]; then
    echo "构建成功！"
    
    # 复制APK到桌面
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        cp app/build/outputs/apk/release/app-release.apk ~/Desktop/${APK_NAME}
        echo "APK已复制到桌面: ~/Desktop/${APK_NAME}"
        
        
    else
        echo "错误：找不到Release APK文件"
        exit 1
    fi
else
    echo "构建失败！"
    exit 1
fi

echo "构建完成！"