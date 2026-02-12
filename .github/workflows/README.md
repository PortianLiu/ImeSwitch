# GitHub Actions 工作流说明

## Release 自动构建

当推送tag时，会自动触发构建并创建GitHub Release。

### 使用方法

1. 确保代码已提交并推送到GitHub
2. 创建并推送tag：

```bash
# 创建tag
git tag v1.0.0

# 推送tag到远程仓库
git push origin v1.0.0
```

3. GitHub Actions会自动：
   - 检出代码
   - 设置JDK 21环境
   - 构建Release APK
   - 创建GitHub Release
   - 上传APK文件到Release

### Tag命名规范

- 使用语义化版本号：`v主版本号.次版本号.修订号`
- 例如：`v1.0.0`、`v1.1.0`、`v2.0.0`

### 查看构建结果

1. 访问仓库的 Actions 标签页查看构建进度
2. 构建完成后，在 Releases 页面可以下载APK文件

### 注意事项

- 当前构建的是未签名的APK（app-release-unsigned.apk）
- 如需签名APK，需要配置签名密钥并修改工作流
