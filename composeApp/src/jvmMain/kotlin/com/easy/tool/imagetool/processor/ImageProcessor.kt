package com.easy.tool.imagetool.processor

import com.easy.tool.imagetool.model.ImageItem
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageProcessor {
    
    /**
     * 分割图片
     * @param image 要分割的图片
     * @param outputDir 输出目录
     * @param splitRatio 分割比例 (0.0 - 1.0)，默认 0.5 表示从中间分割
     * @return Pair<左图路径, 右图路径>
     */
    suspend fun splitImage(
        image: ImageItem,
        outputDir: String,
        splitRatio: Float = 0.5f
    ): Pair<String, String> {
        val inputFile = File(image.path)
        val originalImage = ImageIO.read(inputFile)
        
        val width = originalImage.width
        val height = originalImage.height
        
        // 计算分割位置
        val splitX = (width * splitRatio).toInt()
        
        // 创建左半部分
        val leftImage = BufferedImage(splitX, height, BufferedImage.TYPE_INT_RGB)
        val leftGraphics = leftImage.createGraphics()
        leftGraphics.drawImage(
            originalImage,
            0, 0, splitX, height,  // 目标区域
            0, 0, splitX, height,  // 源区域
            null
        )
        leftGraphics.dispose()
        
        // 创建右半部分
        val rightWidth = width - splitX
        val rightImage = BufferedImage(rightWidth, height, BufferedImage.TYPE_INT_RGB)
        val rightGraphics = rightImage.createGraphics()
        rightGraphics.drawImage(
            originalImage,
            0, 0, rightWidth, height,  // 目标区域
            splitX, 0, width, height,   // 源区域
            null
        )
        rightGraphics.dispose()
        
        // 保存文件
        val outputDirectory = File(outputDir)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        
        val leftPath = "$outputDir/${image.displayName}_left.jpg"
        val rightPath = "$outputDir/${image.displayName}_right.jpg"
        
        ImageIO.write(leftImage, "jpg", File(leftPath))
        ImageIO.write(rightImage, "jpg", File(rightPath))
        
        return Pair(leftPath, rightPath)
    }
    
    /**
     * 批量处理图片
     */
    suspend fun batchSplit(
        images: List<ImageItem>,
        outputDir: String,
        splitRatio: Float = 0.5f,
        onProgress: (current: Int, total: Int) -> Unit
    ): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        
        images.forEachIndexed { index, image ->
            try {
                val result = splitImage(image, outputDir, splitRatio)
                results.add(result)
                onProgress(index + 1, images.size)
            } catch (e: Exception) {
                e.printStackTrace()
                onProgress(index + 1, images.size)
            }
        }
        
        return results
    }
}
