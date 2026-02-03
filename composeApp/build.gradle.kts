import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.filekit.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

//// 自定义任务：打包不带 JRE 的纯 Jar 包 (体积极小，需本机 Java 环境)
//val packageFatJar by tasks.registering(Jar::class) {
//    archiveBaseName.set("PaperCutting-NoJRE")
//    archiveVersion.set("1.0.0")
//
//    manifest {
//        attributes["Main-Class"] = "com.easy.tool.imagetool.MainKt"
//    }
//
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//
//    from(sourceSets.main.get().output)
//
//    // 收集运行时依赖，并排除签名文件以避免 SecurityException
//    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
//        exclude("META-INF/*.SF")
//        exclude("META-INF/*.DSA")
//        exclude("META-INF/*.RSA")
//    }
//}

compose.desktop {
    application {
        mainClass = "com.easy.tool.imagetool.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Paper cutting"
            packageVersion = "1.0.0"
            description = "Paper cutting"
            
            // 模块裁剪：极简模式
            modules(
                "java.desktop", 
                "jdk.unsupported"
            )

            macOS {
                iconFile.set(project.file("icons/icon.icns"))
                bundleID = "com.easy.tool.imagetool"
                dockName = "Paper cutting"
            }
        }
        
        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
            obfuscate.set(true)
            optimize.set(true)
        }
    }
}
