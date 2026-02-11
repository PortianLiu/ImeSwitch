# ImeSwitch - 输入法快速切换器

一个轻量级的Android应用，通过快速设置瓦片和应用图标提供一键切换输入法的功能。

## 功能特性

- ✅ 快速设置瓦片切换：在控制中心添加瓦片，一键切换输入法
- ✅ 应用图标快速切换：点击应用图标直接切换（权限授予后）
- ✅ 循环切换：自动在已启用的输入法之间循环轮换
- ✅ 状态显示：瓦片实时显示当前输入法名称
- ✅ 多种授权方式：支持ADB有线授权和Root一键授权

## 系统要求

- Android 7.0 (API 24) 及以上
- 目标SDK：Android 16 (API 36)

## 使用方法

### 方式1：通过快速设置瓦片切换

1. 下拉通知栏，进入快速设置面板
2. 点击编辑按钮
3. 找到"ImeSwitch"瓦片并拖拽到面板中
4. 点击瓦片即可切换输入法

### 方式2：通过应用图标切换

1. 完成权限授予后
2. 直接点击应用图标即可切换输入法
3. 无需打开应用界面，零UI干扰

## 权限授予

应用需要`WRITE_SECURE_SETTINGS`权限来修改系统输入法设置。此权限无法通过常规方式获取，需要用户手动授予。

### 方法1：ADB有线授权（推荐）

1. 在手机设置中启用"开发者选项"和"USB调试"
2. 使用USB线连接手机到电脑
3. 在电脑上执行以下ADB命令：

```bash
adb shell pm grant lpt.imeswitch android.permission.WRITE_SECURE_SETTINGS
```

4. 授权完成，权限永久有效（直到应用卸载）

### 方法2：Root授权

如果您的设备已获取Root权限：

1. 打开应用
2. 切换到"Root授权"标签
3. 点击"一键授权"按钮
4. 授予Root权限请求
5. 授权完成

### 待开发功能

- 📋 Shizuku授权方式（用户友好的图形化授权）
- 📋 无线调试授权（Android 11+，无需USB线）
- 📋 输入法切换历史记录
- 📋 自定义切换顺序

## 技术栈

- 开发语言：Kotlin
- 最低API级别：Android 7.0 (API 24)
- 目标API级别：Android 16 (API 36)
- 核心组件：TileService, InputMethodManager, Settings.Secure

## 项目结构

```
app/src/main/java/lpt/imeswitch/
├── service/          # 服务类（TileService）
├── ui/               # UI类（Activity）
└── utils/            # 工具类（权限检查、输入法管理）
```

## 开发环境

- Android Studio Arctic Fox或更高版本
- JDK 11或更高版本
- Gradle 8.2或更高版本
- Kotlin 1.9.20或更高版本

## 构建项目

```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd ImeSwitch

# 构建项目
./gradlew build

# 安装到设备
./gradlew installDebug
```

## 许可证

待定

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

待定
