buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Updated AGP to 8.3.2 (supports compileSdk 35)
        classpath("com.android.tools.build:gradle:8.4.0") // Zamiast 8.3.2
        // Updated Kotlin to 1.9.22 (required by Compose 1.5.10)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
        classpath("com.google.gms:google-services:4.4.1")
    }
}