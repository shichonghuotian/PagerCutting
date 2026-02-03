package com.easy.tool.imagetool.model

data class ImageItem(
    val path: String,
    val name: String
) {
    val displayName: String
        get() = name.substringBeforeLast(".")
    
    val extension: String
        get() = name.substringAfterLast(".", "jpg")
}
