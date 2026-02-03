package com.easy.tool.imagetool

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.easy.tool.imagetool.ui.MainScreen
import com.easy.tool.imagetool.viewmodel.ImageSplitViewModel

fun main() = application {
    val viewModel = ImageSplitViewModel()
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Paper cutting",
        state = rememberWindowState(width = 800.dp, height = 900.dp)
    ) {
        MainScreen(viewModel)
    }
}