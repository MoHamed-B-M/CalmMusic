import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Fixed Versioning Protocol: Optimized for GitHub Actions
fun getAutoVersionInfo(): Pair<Int, String> {
    val patchFile = file("version.properties")
    val props = Properties()
    
    if (patchFile.exists()) {
        patchFile.inputStream().use { props.load(it) }
    }
    
    // Start at 12 to recover from your 1.0.11 -> 1.0.7 downgrade
    var patch = props.getProperty("patch", "12").toInt()
    var code = props.getProperty("code", "12").toInt()
    
    // Check if building on GitHub
    val isCI = System.getenv("GITHUB_ACTIONS") == "true"
    val isBuilding = gradle.startParameter.taskNames.any { 
        it.contains("Release", ignoreCase = true) || it.contains("assemble", ignoreCase = true) 
    }

    if (isCI && isBuilding) {
        patch++
        code++
        props.setProperty("patch", patch.toString())
        props.setProperty("code", code.toString())
        patchFile.outputStream().use { props.store(it, "Auto-incremented by CI") }
    }
    
    return code to "1.0.${patch}-beta"
}

android {
    namespace = "com.music.calmplayer"
    compileSdk = 35

    val autoVersion = getAutoVersionInfo()

    defaultConfig {
        applicationId = "com.music.calmplayer"
        minSdk = 24
        targetSdk = 35
        versionCode = autoVersion.first
        versionName = autoVersion.second

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    // Optimization for Celeron N4000
    kotlin { jvmToolchain(17) }
}

dependencies {
    
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("io.coil-kt:coil-compose:2.7.0")
    // 🔥 1. Aligning to the 2025.02 BOM for stability
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.0")
    
    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    
    // 🔥 2. Material Design 3 - Stable 1.3.1 (Fixes 'Internal' Theme errors)
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended")
    
    // 🔥 3. Adaptive Layouts - Stable 1.0.0
    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.0.0")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.0.0")
    
    // Animations (For Shared Transitions)
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.graphics:graphics-shapes:1.0.1")
    
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Media3
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-session:1.5.1")
    
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}