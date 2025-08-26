<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# 生日提醒Android应用 - Copilot指令

这是一个用于记录生日并提供定时提醒的Android应用项目。

## 项目特点
- 使用Kotlin开发的原生Android应用
- 采用MVVM架构模式
- 支持公历和农历生日记录
- 使用Room数据库进行数据持久化
- 通过WorkManager管理后台提醒任务
- Material Design 3 现代化界面

## 代码规范
- 使用Kotlin语言特性（数据类、扩展函数、协程等）
- 遵循Android官方代码规范
- 变量命名采用camelCase
- 类名采用PascalCase
- 常量使用UPPER_SNAKE_CASE
- 布局文件使用snake_case

## 架构组件
- **ViewModel**: 管理UI相关数据和业务逻辑
- **Repository**: 数据访问抽象层
- **Room Database**: 本地数据存储
- **LiveData/Flow**: 响应式数据观察
- **WorkManager**: 后台任务调度
- **Material Components**: UI组件库

## 开发建议
- 优先使用Jetpack组件
- 遵循Android生命周期最佳实践
- 使用协程处理异步操作
- 实现适当的错误处理
- 添加适当的日志记录
- 考虑不同屏幕尺寸的适配
- 遵循Material Design设计原则

## 特殊注意事项
- 农历转换功能需要专业的农历库支持
- 通知权限需要在Android 13+上动态申请
- WorkManager任务需要考虑Doze模式影响
- 数据库迁移策略要谨慎处理
- 日期时间处理要考虑时区问题

## 测试建议
- 为ViewModel编写单元测试
- 为Repository和DAO编写集成测试
- 测试不同Android版本的兼容性
- 验证通知和提醒功能
- 测试数据库操作的正确性
