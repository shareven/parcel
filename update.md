## 发布最新版本的步骤
1. 修改/app/build.gradle.kts中的versionName和versionCode，在原来的版本号基础上加1
2. 运行1build.sh,失败则停止，不执行后续步骤
3. 如果看到脚本输出了"APK已复制到桌面",则commit所有代码，然后git push
4. 再次运行1build.sh
5. 如果看到脚本输出了"APK已复制到桌面"，运行checkApk.sh，看生成的2个apk hash是否一致。不一致,则停止，不执行后续步骤
6. hash一致，则发布到GitHub Release,并把桌面上生成的最新的parcel apk上传到GitHub Release