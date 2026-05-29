# 自动点击助手 (AutoClickApp)

[English](README.md) | 中文

[![Android CI](https://github.com/YOUR_USERNAME/AutoClickApp/actions/workflows/android.yml/badge.svg)](https://github.com/YOUR_USERNAME/AutoClickApp/actions)
[![Release](https://img.shields.io/github/v/release/YOUR_USERNAME/AutoClickApp)](https://github.com/YOUR_USERNAME/AutoClickApp/releases)
[![License](https://img.shields.io/github/license/YOUR_USERNAME/AutoClickApp)](LICENSE)

一个基于 Android 的自动点击应用，支持准星定位和随机偏移点击。

## ✨ 功能特性

| 功能 | 说明 |
|------|------|
| 🎯 可移动准星 | 拖拽准星到任意目标位置 |
| 🔵 侧边悬浮球 | 可拖拽并自动吸附到屏幕边缘，点击切换启动/暂停 |
| 🖱️ 随机偏移点击 | 基于准星位置，每隔1-2秒执行随机偏移（±5-30像素）的点击 |
| ⚡ 前台服务 | 保持服务在后台稳定运行 |
| 🔄 GitHub Actions | 自动构建 Debug/Release APK |

## 📥 下载

### 直接下载
- [Debug APK (最新)](app/build/outputs/apk/debug/app-debug.apk)

### 从 Release 下载
前往 [Releases](https://github.com/YOUR_USERNAME/AutoClickApp/releases) 页面下载签名后的 APK。

## 🔧 技术要求

| 项目 | 版本 |
|------|------|
| Android Studio | 最新版 |
| Gradle | 8.7 |
| Android Gradle Plugin | 8.5.0 |
| minSdk | 24 (Android 7.0) |
| targetSdk | 35 (Android 15) |

## 📁 项目结构

```
AutoClickApp/
├── .github/
│   └── workflows/
│       └── android.yml          # GitHub Actions CI/CD
├── app/
│   ├── src/main/
│   │   ├── java/com/example/autoclick/
│   │   │   ├── MainActivity.java           # 主界面
│   │   │   ├── FloatService.java          # 悬浮窗服务
│   │   │   ├── AutoClickAccessibilityService.java  # 无障碍服务
│   │   │   ├── AutoClickManager.java      # 自动点击管理器
│   │   │   ├── CrosshairView.java         # 准星视图
│   │   │   └── FloatingBallView.java      # 悬浮球视图
│   │   ├── res/                           # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/wrapper/
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 🚀 本地构建

### Android Studio

1. Clone 项目或下载 ZIP
2. Android Studio → File → Open → 选择 `AutoClickApp` 文件夹
3. 等待 Gradle 同步完成
4. 运行或构建

### 命令行

```bash
# 克隆项目
git clone https://github.com/YOUR_USERNAME/AutoClickApp.git
cd AutoClickApp

# Debug 构建
./gradlew assembleDebug

# Release 构建（需要签名配置）
./gradlew assembleRelease

# 清理
./gradlew clean
```

## 📱 使用方法

### 1. 安装应用

将 APK 文件传输到手机并安装（可能需要开启"安装未知来源应用"）。

### 2. 授权权限

首次运行需要授予以下权限：

| 权限 | 用途 |
|------|------|
| 悬浮窗权限 | 显示准星和悬浮球 |
| 无障碍权限 | 执行自动点击 |

### 3. 使用步骤

1. 点击"启动服务"按钮
2. 拖动红色准星到目标位置
3. 点击侧边蓝色悬浮球开始自动点击（球变红色表示运行中）
4. 再次点击悬浮球暂停
5. 拖动悬浮球可移动位置，松开后自动吸附到屏幕边缘

## ⚙️ 点击参数

| 参数 | 默认值 |
|------|--------|
| 点击间隔 | 1-2 秒随机 |
| 偏移范围 | ±5-30 像素随机 |
| 点击持续时间 | 100 毫秒 |

## 🔐 发布到 GitHub

### 首次推送

```bash
cd AutoClickApp
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/AutoClickApp.git
git push -u origin main
```

### 创建 Release

1. 在 GitHub 仓库页面点击 "Releases"
2. 点击 "Draft a new release"
3. 填写版本号（如 v1.0.0）
4. 点击 "Publish release"

GitHub Actions 会自动构建 Release APK 并附加到 Release 中。

### 配置签名密钥（可选）

如需自动构建签名 APK，请在 GitHub 仓库设置中添加以下 Secrets：

| Secret 名称 | 内容 |
|-------------|------|
| `KEYSTORE_BASE64` | keystore 文件的 Base64 编码 |
| `KEYSTORE_PASSWORD` | keystore 密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 |

## ⚠️ 注意事项

1. 本应用需要无障碍权限来执行点击操作
2. 部分应用可能阻止无障碍服务的点击操作
3. 请合理使用，遵守相关应用的使用条款
4. 本应用仅供学习和测试使用

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

- [Android Developers](https://developer.android.com/) - Android 开发文档
- [Material Design](https://material.io/design) - Material Design 组件
