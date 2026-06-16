import java.util.Properties
import java.io.FileInputStream
import java.net.URL
import java.io.File
import java.net.URI
import java.util.regex.Pattern

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

val appConfig = Properties().apply {
    val configFile = project.file("app-config.properties")
    if (configFile.exists()) {
        load(FileInputStream(configFile))
    }
}

android {
  namespace = appConfig.getProperty("NAMESPACE", "com.example")
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = appConfig.getProperty("APPLICATION_ID", "ovrrup.lumia")
    minSdk = 24
    targetSdk = 35
    versionCode = appConfig.getProperty("VERSION_CODE", "1").toInt()
    versionName = appConfig.getProperty("VERSION_NAME", "1.0")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  splits {
    abi {
      isEnable = true
      reset()
      include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
      isUniversalApk = true
    }
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/lumia-key.jks"
      val keystoreFile = file(keystorePath)
      if (keystoreFile.exists()) {
        storeFile = keystoreFile
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      } else {
        // Fallback to debug keystore for AI Studio environment
        storeFile = file("${rootDir}/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      isShrinkResources = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    create("releaseCompressed") {
      initWith(getByName("release"))
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      matchingFallbacks += listOf("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.text.google.fonts)
  implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.work.runtime.ktx)
  implementation("androidx.media:media:1.7.0")
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

abstract class DownloadFontsTask : DefaultTask() {
    @get:OutputDirectory
    abstract val destDir: DirectoryProperty

    @TaskAction
    fun download() {
        val outputDir = destDir.get().asFile
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val fontsToDownload = listOf(
            Triple("nunito_regular.ttf", "Nunito", "400"),
            Triple("nunito_bold.ttf", "Nunito", "700"),
            Triple("lato_regular.ttf", "Lato", "400"),
            Triple("lato_bold.ttf", "Lato", "700"),
            Triple("opensans_regular.ttf", "Open Sans", "400"),
            Triple("opensans_bold.ttf", "Open Sans", "700"),
            Triple("inter_regular.ttf", "Inter", "400"),
            Triple("inter_bold.ttf", "Inter", "700"),
            Triple("tenorsans_regular.ttf", "Tenor Sans", "400"),
            Triple("playfairdisplay_regular.ttf", "Playfair Display", "400"),
            Triple("playfairdisplay_bold.ttf", "Playfair Display", "700"),
            Triple("josefinsans_regular.ttf", "Josefin Sans", "400"),
            Triple("josefinsans_bold.ttf", "Josefin Sans", "700"),
            Triple("archivo_regular.ttf", "Archivo", "400"),
            Triple("archivo_bold.ttf", "Archivo", "700"),
            Triple("syne_regular.ttf", "Syne", "400"),
            Triple("syne_bold.ttf", "Syne", "700"),
            Triple("montserrat_regular.ttf", "Montserrat", "400"),
            Triple("montserrat_bold.ttf", "Montserrat", "700"),
            Triple("yellowtail_regular.ttf", "Yellowtail", "400")
        )
        for ((fileName, family, weight) in fontsToDownload) {
            val file = File(outputDir, fileName)
            if (!file.exists()) {
                println("Fetching URL for $family ($weight)...")
                try {
                    val familyParam = family.replace(" ", "+")
                    val cssUrl = "https://fonts.googleapis.com/css?family=$familyParam:$weight"
                    val conn = URI(cssUrl).toURL().openConnection()
                    conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)")
                    val inputStream = conn.getInputStream()
                    val cssContent = inputStream.bufferedReader().readText()
                    inputStream.close()
                    val pattern = Pattern.compile("url\\((https://fonts.gstatic.com/[^\\)]+)\\)")
                    val matcher = pattern.matcher(cssContent)
                    if (matcher.find()) {
                        val ttfUrl = matcher.group(1)
                        println("Downloading $fileName from $ttfUrl...")
                        val fontStream = URI(ttfUrl).toURL().openConnection()
                        val fontInput = fontStream.getInputStream()
                        val fontOutput = file.outputStream()
                        fontInput.copyTo(fontOutput)
                        fontInput.close()
                        fontOutput.close()
                    } else {
                        println("No TTF URL found in CSS for $family ($weight). Content was: $cssContent")
                    }
                } catch (e: Exception) {
                    println("Failed to download $family ($weight): ${e.message}")
                }
            }
        }
    }
}

tasks.register<DownloadFontsTask>("downloadFonts") {
    destDir.set(layout.projectDirectory.dir("src/main/res/font"))
}

tasks.named("preBuild") {
    dependsOn("downloadFonts")
}
