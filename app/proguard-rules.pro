# 为此项目添加特定的ProGuard规则
# 默认情况下，此文件中的标志会添加到proguard-android-optimize.txt中指定的标志
# 您可以通过在proguard-project.txt中定义规则来编辑包含路径和顺序
# 有关更多详细信息，请参阅http://developer.android.com/guide/developing/tools/proguard.html

# 保留行号信息以便调试堆栈跟踪
-keepattributes SourceFile,LineNumberTable

# 如果保留行号信息，则隐藏原始源文件名
-renamesourcefileattribute SourceFile
