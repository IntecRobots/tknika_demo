buildscript {

    extra.apply {
        set("compose_version", "1.2.0")
        set("kotlin_version", "1.9.0")
        set("camerax_version", "1.3.0-beta01")
    }

    repositories {
        // other repositories...
        mavenCentral()
    }

    dependencies {
        // other plugins...
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.47")
        classpath ("com.android.tools.build:gradle:8.2.2")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
}