plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.aiwolfie.tiktokautoscroll"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.aiwolfie.tiktokautoscroll"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    kotlinOptions { jvmTarget = "17" }
}
