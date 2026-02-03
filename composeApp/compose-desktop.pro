# 基础混淆规则
-dontwarn
-noverify

# 1. 保留应用入口和业务代码
-keep class com.easy.tool.imagetool.MainKt { *; }
-keep class com.easy.tool.imagetool.** { *; }

# 2. 关键：不要全局保留 androidx.compose.**，允许移除未使用的图标！
# 只保留必要的运行时入口，其他的让 ProGuard 自动追踪引用
-keep class androidx.compose.runtime.DefaultMonotonicFrameClock { *; }
-keep class androidx.compose.ui.window.** { *; }

# 3. AWT/Swing/ImageIO (必须保留，否则反射调用会崩)
-keep class java.awt.** { *; }
-keep class javax.swing.** { *; }
-keep class javax.imageio.** { *; }
-keep class sun.awt.** { *; }
-keep class sun.java2d.** { *; }

# 4. Compose Desktop 底层引擎 (Skiko) 和 协程支持
-keep class org.jetbrains.skiko.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class kotlinx.coroutines.** { *; }

# 忽略警告
-dontwarn java.lang.invoke.**
-dontwarn androidx.**
-dontwarn org.jetbrains.**
