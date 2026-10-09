# QasDM 安卓测试版

当前版本为 QasDM v55。`index.html` 是唯一页面源，构建时复制到 APK，避免网页和 APK 各维护一份代码。

## 下载

在 [Releases](https://github.com/yangdelefangde-cell/QasDM/releases) 下载 APK。首个构建完成后出现下载文件。Android 8.0 及以上可安装。

## 构建

GitHub Actions 在 main 变更后自动运行，也支持手动运行。固定使用 JDK 17、Gradle 8.9、AGP 8.7.3、compileSdk 35、targetSdk 34。

本地安装 Android SDK 后运行 `gradle :app:assembleDebug :app:lintDebug`。工作流生成 APK、可独立打开的 HTML 和 SHA256SUMS。

## 发布下一版

同时更新 `version.json` 的网页版本、安卓 versionCode（必须递增）、versionName、releaseTag 和两个下载地址；`update.js` 的 CURRENT_VERSION；`android-runtime.js` 的版本；HTML 标题。校验脚本会检查版本一致性。

## 数据与权限

聊天与媒体使用 WebView 的 localStorage / IndexedDB，保持稳定本地 HTTPS 来源 `https://appassets.androidplatform.net`。浏览器数据不会自动出现在应用内，请通过备份导入。更新同一签名包会保留数据。应用未上传本机数据到 GitHub。

文件选择使用系统选择器；导出使用系统保存对话框；不索取全部存储权限。麦克风只在本地主页面发起录音时请求。原生消息接口限本地来源与主框架，插件 iframe 无权调用。网页弹窗仍由 QasDM 管理。

`test-signing.p12` 是公开测试签名（密码 qasdm-test），使测试更新可以覆盖安装。不得将此密钥用于正式发布。测试包可被调试，正式版应更换应用 ID、正式密钥和构建类型。

尚未做真机测试。WebView 的后台暂停可能影响主动消息；离线可读取已保存内容，模型 API、在线渲染资源和检查更新需要联网。

