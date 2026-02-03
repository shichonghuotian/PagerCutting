# 图片分割工具 Image Split Tool

一个用于将 A4 试卷截图从中间分割成左右两张图片的 Kotlin Multiplatform Desktop 应用。

## 功能特性

- ✅ **多图片选择**：使用原生系统文件选择器，支持批量选择
- ✅ **拖放支持**：直接拖放图片文件到应用界面即可添加
- ✅ **自定义分割比例**：支持 10%-90% 的灵活分割比例，默认 50:50
- ✅ **批量处理**：自动批量处理所有选中的图片
- ✅ **进度显示**：实时显示处理进度
- ✅ **自动输出管理**：在源文件目录下自动创建 `split_output` 文件夹
- ✅ **现代化 UI**：Material 3 暗色主题界面

## 运行应用

### 开发环境运行

```bash
./gradlew :composeApp:run
```

### 打包应用

macOS DMG 包：
```bash
./gradlew :composeApp:createDistributable
```

输出位置：`composeApp/build/compose/binaries/main/dmg/`

## 使用说明

1. **启动应用**：运行应用后会看到主界面

2. **调整分割比例**（可选）：
   - 使用滑块调整分割比例
   - 默认 50:50，可以根据需要调整到 10:90 之间任意比例

3. **选择图片**：
   - 点击「选择图片」按钮
   - 支持 jpg、jpeg、png 格式
   - 可多选

4. **管理图片列表**：
   - 查看已选图片列表
   - 可删除不需要的图片

5. **开始分割**：
   - 点击「开始分割」按钮
   - 等待处理完成（会显示进度）

6. **查看结果**：
   - 处理完成后会弹出对话框
   - 点击「打开文件夹」可直接查看输出
   - 图片保存在源文件目录下的 `split_output` 文件夹中

## 输出文件命名

- 左半部分：`原文件名_left.jpg`
- 右半部分：`原文件名_right.jpg`

## 技术栈

- **Kotlin Multiplatform**
- **Compose Multiplatform**
- **Material 3 Design**
- **Coroutines**

## 项目结构

```
imageTool/
├── composeApp/
│   └── src/
│       └── jvmMain/
│           └── kotlin/
│               └── com/easy/tool/imagetool/
│                   ├── model/
│                   │   └── ImageItem.kt          # 图片数据模型
│                   ├── processor/
│                   │   └── ImageProcessor.kt     # 图片处理逻辑
│                   ├── viewmodel/
│                   │   └── ImageSplitViewModel.kt # 状态管理
│                   ├── ui/
│                   │   └── MainScreen.kt         # UI 界面
│                   └── main.kt                   # 应用入口
└── build.gradle.kts
```

## 系统要求

- macOS 10.14+
- Java 11+

## 开发

如需修改或扩展功能，可以编辑以下文件：

- `ImageProcessor.kt`：修改图片处理逻辑
- `MainScreen.kt`：修改 UI 界面
- `ImageSplitViewModel.kt`：修改业务逻辑和状态管理

## License

MIT License# PagerCutting
