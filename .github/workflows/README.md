# GitHub Actions 工作流说明

## Release 自动构建

当推送tag时，会自动触发构建并创建GitHub Release。

### 使用方法

1. 确保代码已提交并推送到GitHub
2. 创建带注释的tag并推送：

```bash
# 创建带注释的tag（推荐）
git tag -a v1.0.0 -m "版本 1.0.0

## 新增功能
- 短按快捷开关：轮切输入法
- 长按快捷开关：选择输入法

## 安装说明
1. 下载APK文件并安装
2. 通过ADB或Root授予WRITE_SECURE_SETTINGS权限
3. 在快速设置面板中添加\"输入法切换\"快捷开关

## 权限授予命令
\`\`\`bash
adb shell pm grant lpt.imeswitch android.permission.WRITE_SECURE_SETTINGS
\`\`\`
"

# 推送tag到远程仓库
git push origin v1.0.0
```

或者使用轻量级tag（不推荐，会自动生成Release说明）：

```bash
# 创建轻量级tag
git tag v1.0.0

# 推送tag
git push origin v1.0.0
```

3. GitHub Actions会自动：
   - 检出代码
   - 设置JDK 21环境
   - 构建Release APK
   - 创建GitHub Release（使用tag注释作为说明）
   - 上传APK文件到Release

### Tag命名规范

- 使用语义化版本号：`v主版本号.次版本号.修订号`
- 例如：`v1.0.0`、`v1.1.0`、`v2.0.0`

### Release说明来源

- **带注释的tag**（`git tag -a`）：使用tag的注释消息作为Release说明
- **轻量级tag**（`git tag`）：自动生成Release说明（包含commits列表）

### 查看构建结果

1. 访问仓库的 Actions 标签页查看构建进度
2. 构建完成后，在 Releases 页面可以下载APK文件

### 注意事项

- 当前构建的是未签名的APK（app-release-unsigned.apk）
- 如需签名APK，需要配置签名密钥并修改工作流
- 推荐使用带注释的tag（`-a`参数），可以自定义Release说明
