plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ado81.gdinputbuffer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ado81.gdinputbuffer"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "0.1.0"
    }
}

kotlin {
    jvmToolchain(17)
}
