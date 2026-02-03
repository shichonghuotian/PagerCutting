package com.easy.tool.imagetool.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.easy.tool.imagetool.model.ImageItem
import com.easy.tool.imagetool.processor.ImageProcessor
import java.io.File

class ImageSplitViewModel {
    private val processor = ImageProcessor()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    var images by mutableStateOf<List<ImageItem>>(emptyList())
        private set
    
    var isProcessing by mutableStateOf(false)
        private set
    
    var progress by mutableStateOf(0 to 0) // (current, total)
        private set
    
    var splitRatio by mutableStateOf(0.5f)
        private set
    
    var showResult by mutableStateOf(false)
        private set
    
    var outputPath by mutableStateOf("")
        private set
    
    fun selectImages() {
        val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "选择图片文件", java.awt.FileDialog.LOAD)
        fileDialog.isMultipleMode = true
        fileDialog.setFilenameFilter { _, name ->
            val extension = name.substringAfterLast(".", "").lowercase()
            extension in listOf("jpg", "jpeg", "png")
        }
        fileDialog.isVisible = true
        
        val selectedFiles = fileDialog.files
        if (selectedFiles.isNotEmpty()) {
            val newImages = selectedFiles.map { file ->
                ImageItem(
                    path = file.absolutePath,
                    name = file.name
                )
            }
            images = (images + newImages).distinctBy { it.path }
        }
    }
    
    fun addImagesFromFiles(files: List<File>) {
        val newImages = files
            .filter { file ->
                val extension = file.extension.lowercase()
                extension in listOf("jpg", "jpeg", "png")
            }
            .map { file ->
                ImageItem(
                    path = file.absolutePath,
                    name = file.name
                )
            }
        images = (images + newImages).distinctBy { it.path }
    }
    
    fun removeImage(image: ImageItem) {
        images = images.filter { it != image }
    }
    
    fun updateSplitRatio(ratio: Float) {
        splitRatio = ratio.coerceIn(0.1f, 0.9f)
    }
    
    fun startProcessing() {
        if (images.isEmpty()) return
        
        scope.launch {
            isProcessing = true
            progress = 0 to images.size
            
            // 获取第一个图片的目录
            val firstImageDir = File(images.first().path).parentFile
            val outputDir = File(firstImageDir, "split_output").absolutePath
            outputPath = outputDir
            
            processor.batchSplit(
                images = images,
                outputDir = outputDir,
                splitRatio = splitRatio
            ) { current, total ->
                progress = current to total
            }
            
            isProcessing = false
            showResult = true
        }
    }
    
    fun reset() {
        images = emptyList()
        isProcessing = false
        progress = 0 to 0
        showResult = false
        outputPath = ""
    }
    
    fun openOutputFolder() {
        if (outputPath.isNotEmpty()) {
            try {
                if (System.getProperty("os.name").lowercase().contains("mac")) {
                    Runtime.getRuntime().exec(arrayOf("open", outputPath))
                } else if (System.getProperty("os.name").lowercase().contains("win")) {
                    Runtime.getRuntime().exec(arrayOf("explorer.exe", outputPath))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun printResults() {
        if (outputPath.isNotEmpty()) {
            try {
                val dir = File(outputPath)
                if (dir.exists() && dir.isDirectory) {
                    val files = dir.listFiles { _, name ->
                        val ext = name.substringAfterLast(".", "").lowercase()
                        ext in listOf("jpg", "jpeg", "png")
                    }?.sortedBy { it.name } ?: emptyList<File>()

                    if (files.isNotEmpty()) {
                        val osName = System.getProperty("os.name").lowercase()
                        if (osName.contains("mac")) {
                            // Mac: Open in Preview application
                            val command = mutableListOf("open", "-a", "Preview")
                            command.addAll(files.map { it.absolutePath })
                            Runtime.getRuntime().exec(command.toTypedArray())
                        } else {
                            // Windows/Linux: Open using default system application
                            if (java.awt.Desktop.isDesktopSupported()) {
                                val desktop = java.awt.Desktop.getDesktop()
                                // For Windows, opening the files usually uses the default Photo viewer which supports printing
                                files.forEach { file ->
                                    try {
                                        desktop.open(file)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
