# ImeSwitch - 输入法快速切换器

一个轻量级的Android应用，通过快速设置快捷开关提供快速切换输入法的功能。

## 功能特性

### 快捷开关操作
- **短按快捷开关**：在已启用的输入法之间循环切换
- **长按快捷开关**：展开输入法选择列表，点击直接切换到指定输入法

### 输入法显示
- 自动获取并显示输入法的真实名称
- 支持系统输入法和第三方输入法
- 针对Android 11+包可见性限制进行优化

## 系统要求

- Android 7.0 (API 24) 及以上
- 目标SDK：Android 16 (API 36)
- 需要授予 WRITE_SECURE_SETTINGS 权限

## 安装步骤

1. 下载并安装APK文件
2. 通过ADB或Root授予权限（见下方命令）
3. 下拉通知栏，进入快速设置面板
4. 点击编辑按钮，添加"输入法切换"快捷开关

## 权限授予

应用需要`WRITE_SECURE_SETTINGS`权限来修改系统输入法设置。此权限无法通过常规方式获取，需要用户手动授予。

### 方式一：ADB授权（推荐）

1. 在手机设置中启用"开发者选项"和"USB调试"
2. 使用USB线连接手机到电脑
3. 在电脑上执行以下命令：

```bash
adb shell pm grant lpt.imeswitch android.permission.WRITE_SECURE_SETTINGS
```

4. 授权完成，权限永久有效（直到应用卸载）

### 方式二：Root授权

如果您的设备已获取Root权限：

1. 打开应用
2. 切换到"Root授权"标签
3. 点击"一键授权"按钮
4. 授予Root权限请求
5. 授权完成

## 可选配置

如果输入法名称显示不正常，可以在系统设置中为本应用授予"查询所有应用"权限。

## 技术特性

- 使用 Settings.Secure 直接读取和修改输入法配置
- 通过 `<queries>` 声明解决Android 11+包可见性限制
- 透明Activity实现长按选择功能
- 最小化权限请求，仅需必要的系统权限

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
- JDK 21
- Gradle 8.14.3
- Kotlin 2.0.21

## 构建项目

```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd ImeSwitch

# 构建项目
gradle build

# 安装到设备
gradle installDebug
```

## 许可证

本项目采用 [Mozilla Public License 2.0](LICENSE) 开源协议。

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

待定
