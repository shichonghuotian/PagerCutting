package com.easy.tool.imagetool.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import com.easy.tool.imagetool.model.ImageItem
import com.easy.tool.imagetool.viewmodel.ImageSplitViewModel
import kotlinx.coroutines.launch
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import javax.swing.JFrame
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WindowScope.MainScreen(viewModel: ImageSplitViewModel) {
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    
    // 设置拖放监听
    LaunchedEffect(Unit) {
        val windowFrame = window as? JFrame
        println("DEBUG: Window frame: $windowFrame")
        println("DEBUG: Content pane: ${windowFrame?.contentPane}")
        
        val dropTarget = object : DropTarget() {
            override fun dragEnter(dtde: DropTargetDragEvent) {
                println("DEBUG: Drag Enter")
                dtde.acceptDrag(DnDConstants.ACTION_COPY)
                isDragging = true
            }
            
            override fun dragOver(dtde: DropTargetDragEvent) {
                 // println("DEBUG: Drag Over") // Too noisy
                 dtde.acceptDrag(DnDConstants.ACTION_COPY)
            }

            override fun dragExit(dte: DropTargetEvent) {
                println("DEBUG: Drag Exit")
                isDragging = false
            }
            
            override fun drop(dtde: DropTargetDropEvent) {
                println("DEBUG: Drop event received")
                isDragging = false
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY)
                    val transferable = dtde.transferable
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        println("DEBUG: File list flavor supported")
                        @Suppress("UNCHECKED_CAST")
                        val fileList = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                        println("DEBUG: Dropped files: ${fileList.size}")
                        if (fileList.isNotEmpty()) {
                            viewModel.addImagesFromFiles(fileList)
                            dtde.dropComplete(true)
                            println("DEBUG: Drop complete success")
                        } else {
                            dtde.dropComplete(false)
                        }
                    } else {
                        println("DEBUG: Data flavor not supported")
                        dtde.dropComplete(false)
                    }
                } catch (e: Exception) {
                    println("DEBUG: Drop failed with exception: ${e.message}")
                    e.printStackTrace()
                    dtde.dropComplete(false)
                }
            }
        }
        
        if (windowFrame != null) {
            // 递归 helper 函数
            fun attachDropTargetRecursive(component: java.awt.Component) {
                component.dropTarget = dropTarget
                if (component is java.awt.Container) {
                    for (child in component.components) {
                        attachDropTargetRecursive(child)
                    }
                }
            }
            
            // 初始附加
            attachDropTargetRecursive(windowFrame.contentPane)
            println("DEBUG: Drop target recursively set on content pane and children")
            
            // 可选：监听组件添加，确保动态添加的组件也有 DropTarget (Compose 重绘可能重建组件)
             windowFrame.contentPane.addContainerListener(object : java.awt.event.ContainerAdapter() {
                override fun componentAdded(e: java.awt.event.ContainerEvent) {
                    attachDropTargetRecursive(e.child)
                }
            })
        } else {
            println("DEBUG: Failed to cast window to JFrame")
        }
    }
    
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题
                    Text(
                        text = "图片分割工具",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "将 A4 试卷截图从中间分割成左右两张图片",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    
                    // 分割比例滑块
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("分割比例")
                                Text(
                                    text = "${(viewModel.splitRatio * 100).toInt()}% : ${((1 - viewModel.splitRatio) * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Slider(
                                value = viewModel.splitRatio,
                                onValueChange = { viewModel.updateSplitRatio(it) },
                                valueRange = 0.1f..0.9f,
                                enabled = !viewModel.isProcessing,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    
                    // 拖放区域或选择按钮
                    if (viewModel.images.isEmpty() && !isDragging) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                
                                Text(
                                    text = "拖放图片到此处",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Text(
                                    text = "或点击下方按钮选择文件",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (isDragging) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Text(
                                    text = "松开鼠标以添加文件",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 选择图片按钮
                    Button(
                        onClick = { viewModel.selectImages() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !viewModel.isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("选择图片", fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 图片列表
                    if (viewModel.images.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "已选择 ${viewModel.images.size} 张图片",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(viewModel.images) { image ->
                                        ImageListItem(
                                            image = image,
                                            onRemove = { viewModel.removeImage(image) },
                                            enabled = !viewModel.isProcessing
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 开始处理按钮
                        Button(
                            onClick = { viewModel.startProcessing() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !viewModel.isProcessing && viewModel.images.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("开始分割", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // 处理进度对话框
                if (viewModel.isProcessing) {
                    ProcessingDialog(
                        current = viewModel.progress.first,
                        total = viewModel.progress.second
                    )
                }
                
                // 完成对话框
                if (viewModel.showResult) {
                    ResultDialog(
                        onOpenFolder = { viewModel.openOutputFolder() },
                        onPrint = { viewModel.printResults() },
                        onDismiss = { viewModel.reset() },
                        outputPath = viewModel.outputPath
                    )
                }
            }
        }
    }
}

@Composable
fun ImageListItem(
    image: ImageItem,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    var thumbnail by remember(image.path) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    
    // 异步加载缩略图
    LaunchedEffect(image.path) {
        withContext(Dispatchers.IO) {
            try {
                val file = java.io.File(image.path)
                if (file.exists()) {
                    // 读取图片
                    val originalImage = javax.imageio.ImageIO.read(file)
                    if (originalImage != null) {
                        // 计算缩略图尺寸，保持纵横比，高度固定为 50
                        val targetHeight = 50
                        val ratio = targetHeight.toDouble() / originalImage.height
                        val targetWidth = (originalImage.width * ratio).toInt()
                        
                        // 创建缩略图
                        val resizedImage = java.awt.image.BufferedImage(
                            targetWidth, 
                            targetHeight, 
                            java.awt.image.BufferedImage.TYPE_INT_ARGB
                        )
                        val g = resizedImage.createGraphics()
                        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
                        g.dispose()
                        
                        thumbnail = resizedImage.toComposeImageBitmap()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 缩略图
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    androidx.compose.foundation.Image(
                        bitmap = thumbnail!!,
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FolderOpen, // 占位图
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = image.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = image.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            IconButton(
                onClick = onRemove,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
@Composable
fun ProcessingDialog(
    current: Int,
    total: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "正在处理...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LinearProgressIndicator(
                    progress = { if (total > 0) current.toFloat() / total else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(bottom = 16.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Text(
                    text = "$current / $total",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ResultDialog(
    onOpenFolder: () -> Unit,
    onPrint: () -> Unit,
    onDismiss: () -> Unit,
    outputPath: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(500.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✅ 处理完成！",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "图片已保存到：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = outputPath,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("继续处理")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            onPrint()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("打印")
                    }
                    
                    Button(
                        onClick = {
                            onOpenFolder()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("打开文件夹")
                    }
                }
            }
        }
    }
}
