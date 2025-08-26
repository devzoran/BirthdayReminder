# Android生日提醒应用 - 编译安装指南

## 🛠️ 环境准备

### 必需软件
1. **Android Studio** (推荐)
   - 下载：https://developer.android.com/studio
   - 包含Android SDK、Gradle、ADB等工具

2. **Java Development Kit (JDK)**
   - 版本：JDK 17 或更高
   - Android Studio会自动配置

### 可选软件
- **Gradle** (如果不使用Android Studio)
- **Android SDK Command Line Tools**

## 📦 编译步骤

### 方法一：使用Android Studio (推荐)

1. **打开项目**
   ```bash
   # 启动Android Studio，选择 "Open an existing project"
   # 选择项目目录：/Users/braveheart/DevZoran/GitHubProjects/calendar
   ```

2. **同步项目**
   - Android Studio会自动同步Gradle依赖
   - 如果提示同步，点击 "Sync Now"

3. **编译APK**
   - 菜单：`Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - 或使用快捷键：`Ctrl+Shift+A` (Windows/Linux) / `Cmd+Shift+A` (Mac)

4. **查找生成的APK**
   - 路径：`app/build/outputs/apk/debug/app-debug.apk`

### 方法二：使用命令行

```bash
# 1. 进入项目目录
cd /Users/braveheart/DevZoran/GitHubProjects/calendar

# 2. 给gradlew添加执行权限 (首次运行)
chmod +x gradlew

# 3. 清理项目 (可选)
./gradlew clean

# 4. 编译调试版APK
./gradlew assembleDebug

# 5. 编译发布版APK (需要签名配置)
./gradlew assembleRelease
```

## 📱 安装到手机

### 准备工作

1. **开启开发者选项**
   - 设置 → 关于手机 → 连续点击版本号7次
   - 返回设置，进入开发者选项

2. **开启USB调试**
   - 开发者选项 → USB调试 → 开启
   - 开发者选项 → USB安装 → 开启 (部分手机)

3. **连接手机**
   - 使用USB数据线连接手机和电脑
   - 手机弹出授权提示时选择"允许"

### 安装方法

**方法一：使用Android Studio**
```bash
# 1. 在Android Studio中
# 2. 确保设备已连接 (底部状态栏显示设备名)
# 3. 点击运行按钮 (绿色三角形) 或按 Shift+F10
# 4. 选择目标设备，点击OK
```

**方法二：使用ADB命令**
```bash
# 1. 检查设备连接
adb devices

# 2. 安装APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 3. 如果需要覆盖安装
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**方法三：直接传输安装**
```bash
# 1. 将APK文件复制到手机
# 2. 在手机文件管理器中找到APK文件
# 3. 点击APK文件进行安装
# 4. 可能需要开启"未知来源应用安装"权限
```

**方法四：一键编译安装**
```bash
# 直接编译并安装到连接的设备
./gradlew installDebug
```

## 🔧 常用命令

```bash
# 查看连接的设备
adb devices

# 卸载应用
adb uninstall com.birthday.reminder

# 启动应用
adb shell am start -n com.birthday.reminder/.MainActivity

# 查看应用日志
adb logcat | grep BirthdayReminder

# 清除应用数据
adb shell pm clear com.birthday.reminder

# 查看应用信息
adb shell dumpsys package com.birthday.reminder
```

## 📁 输出文件位置

```
app/build/outputs/apk/
├── debug/
│   └── app-debug.apk          # 调试版APK (已签名，可直接安装)
└── release/
    └── app-release.apk        # 发布版APK (需要签名配置)
```

## ⚠️ 常见问题

### 1. Gradle同步失败
```bash
# 解决方案：
./gradlew --refresh-dependencies
```

### 2. 设备未识别
```bash
# 检查驱动和USB调试
adb kill-server
adb start-server
adb devices
```

### 3. 安装失败
```bash
# 可能的解决方案：
# - 卸载旧版本
# - 检查包名冲突
# - 确认签名一致
adb uninstall com.birthday.reminder
```

### 4. 权限问题
- 确保开启了"未知来源应用安装"权限
- 检查USB调试是否开启
- 尝试重新连接设备

## 🎯 快速开始

```bash
# 一键编译并安装 (推荐)
cd /Users/braveheart/DevZoran/GitHubProjects/calendar
./gradlew installDebug

# 查看设备上的应用
adb shell pm list packages | grep birthday
```

## 📞 技术支持

如果遇到问题，可以：
1. 检查Android Studio的Build输出窗口
2. 查看Gradle构建日志
3. 确认Android SDK配置正确
4. 验证手机USB调试设置
