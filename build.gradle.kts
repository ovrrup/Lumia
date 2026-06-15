// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

tasks.register("debugWorkspace") {
    doLast {
        println("=== FULL CODEBASE NAMESPACE SANITIZATION SWEEP ===")
        val srcDir = java.io.File(project.projectDir, "app/src/main/java/ovrrup/lumia")
        if (srcDir.exists()) {
            srcDir.walkTopDown().filter { it.isFile && it.name.endsWith(".kt") }.forEach { file ->
                var content = file.readText()
                if (content.contains("com.example")) {
                    content = content.replace("package com.example", "package ovrrup.lumia")
                    content = content.replace("com.example", "ovrrup.lumia")
                    file.writeText(content)
                    println("Sanitized import/package namespaces in: ${file.name}")
                }
            }
        } else {
            println("Source directory not found: ${srcDir.absolutePath}")
        }
    }
}







