# 生日提醒 Android 应用

一个专为记录生日并提供智能提醒的Android应用，支持公历和农历生日。

## 功能特点

### 🎂 核心功能
- **生日记录管理**: 添加、编辑、删除生日信息
- **双历支持**: 支持公历和农历生日记录
- **智能提醒**: 可设置提前1-7天的生日提醒
- **关系标记**: 可标记家庭成员关系（父亲、母亲、兄弟姐妹等）
- **年龄计算**: 自动计算年龄和距离下次生日天数

### 📱 用户体验
- **Material Design**: 现代化的界面设计
- **直观操作**: 简单易用的添加和编辑界面
- **状态提示**: 清晰的生日倒计时显示
- **通知提醒**: 准时的推送通知

### 🔧 技术特性
- **数据持久化**: 使用Room数据库存储生日信息
- **后台任务**: WorkManager管理定时提醒任务
- **通知管理**: 智能通知系统
- **权限管理**: 处理Android 13+的通知权限

## 技术架构

### 开发框架
- **语言**: Kotlin
- **架构**: MVVM (Model-View-ViewModel)
- **UI框架**: Android Jetpack + Material Design 3
- **数据库**: Room Database
- **异步处理**: Kotlin Coroutines
- **后台任务**: WorkManager

### 主要组件
- `MainActivity`: 主界面，显示生日列表
- `AddEditBirthdayDialog`: 添加/编辑生日对话框
- `BirthdayAdapter`: RecyclerView适配器
- `BirthdayViewModel`: 数据和业务逻辑管理
- `BirthdayDatabase`: 数据库访问层
- `BirthdayReminderManager`: 提醒任务管理
- `NotificationHelper`: 通知管理

## 项目结构

```
app/
├── src/main/
│   ├── java/com/birthday/reminder/
│   │   ├── MainActivity.kt                    # 主活动
│   │   ├── AddEditBirthdayDialog.kt          # 添加编辑对话框
│   │   ├── BirthdayAdapter.kt                # 列表适配器
│   │   ├── Birthday.kt                       # 数据实体
│   │   ├── BirthdayDao.kt                    # 数据访问对象
│   │   ├── BirthdayDatabase.kt               # 数据库类
│   │   ├── BirthdayRepository.kt             # 数据仓库
│   │   ├── BirthdayViewModel.kt              # 视图模型
│   │   ├── BirthdayReminderManager.kt        # 提醒管理
│   │   ├── NotificationHelper.kt             # 通知助手
│   │   ├── BirthdayReminderReceiver.kt       # 广播接收器
│   │   ├── BirthdayReminderService.kt        # 后台服务
│   │   └── App.kt                            # 应用程序类
│   ├── res/
│   │   ├── layout/                           # 布局文件
│   │   ├── drawable/                         # 图标资源
│   │   ├── values/                           # 字符串、颜色、主题
│   │   └── ...
│   └── AndroidManifest.xml                   # 应用清单
├── build.gradle                              # 模块构建配置
└── ...
```

## 开发环境

### 系统要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- Android SDK API Level 24+ (Android 7.0)
- 目标SDK: API Level 34 (Android 14)
- Kotlin 1.9.10+

### 依赖库
- AndroidX Core
- Material Design Components
- Room Database
- WorkManager
- Lifecycle Components
- Navigation Components

## 安装和运行

### 开发环境设置
1. 克隆项目到本地
2. 用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击"Run"按钮构建并安装应用

### 构建命令
```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease

# 运行测试
./gradlew test
```

## 使用说明

### 添加生日
1. 点击右下角的"+"按钮
2. 填写姓名、关系等信息
3. 选择生日日期
4. 设置是否为农历生日
5. 选择提前提醒天数
6. 点击"保存"

### 管理生日
- **编辑**: 点击生日条目进行编辑
- **删除**: 长按生日条目选择删除
- **开关提醒**: 使用右侧开关启用/关闭提醒

### 查看信息
- 应用会显示每个人的年龄
- 显示距离下次生日的天数
- 近期生日会特别标注

## 权限说明

应用需要以下权限：
- `POST_NOTIFICATIONS`: 发送生日提醒通知
- `SCHEDULE_EXACT_ALARM`: 精确定时提醒
- `WAKE_LOCK`: 后台唤醒设备

## 农历支持

当前版本对农历的支持是基础实现，在实际使用中建议：
1. 集成专业的农历转换库
2. 考虑闰月等特殊情况
3. 提供农历日期选择器

## 未来计划

- [ ] 完善农历转换功能
- [ ] 添加生日祝福语模板
- [ ] 支持导入/导出生日数据
- [ ] 添加生日历史记录
- [ ] 支持多种提醒方式（短信、邮件等）
- [ ] 添加生日统计功能

## 贡献

欢迎提交问题报告和功能建议！

## 许可证

本项目采用MIT许可证，详见LICENSE文件。
