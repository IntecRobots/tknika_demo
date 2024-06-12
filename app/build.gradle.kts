plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.intec.template"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.intec.template"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val compose_version = rootProject.extra.get("compose_version") as String
val kotlin_version = rootProject.extra.get("kotlin_version") as String
val camerax_version = rootProject.extra.get("camerax_version") as String

dependencies {

    implementation("io.getstream:stream-video-android-compose:0.4.2")
    implementation("io.getstream:stream-video-android-core:0.5.4")
    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation ("androidx.activity:activity-compose:1.3.1")
    implementation ("androidx.compose.ui:ui:$compose_version")
    implementation ("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation ("androidx.compose.material3:material3:1.0.0-rc01")
    implementation (files("libs\\robotservice.jar"))
    implementation ("androidx.navigation:navigation-compose:2.6.0")
    implementation ("androidx.navigation:navigation-runtime-ktx:2.6.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$compose_version")
    debugImplementation ("androidx.compose.ui:ui-tooling:$compose_version")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$compose_version")
    implementation ("io.coil-kt:coil-compose:1.4.0")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20")
    implementation ("androidx.compose.runtime:runtime-livedata:$compose_version")

    //DAGGER HILT
    implementation ("com.google.dagger:hilt-android:2.47")
    kapt ("com.google.dagger:hilt-compiler:2.47")
    implementation ("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    // For instrumentation tests
    androidTestImplementation  ("com.google.dagger:hilt-android-testing:2.47")
    kaptAndroidTest ("com.google.dagger:hilt-compiler:2.47")

    // For local unit tests
    testImplementation ("com.google.dagger:hilt-android-testing:2.47")
    kaptTest ("com.google.dagger:hilt-compiler:2.47")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    implementation ("androidx.camera:camera-core:${camerax_version}")
    implementation ("androidx.camera:camera-camera2:${camerax_version}")
    implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation ("androidx.camera:camera-video:${camerax_version}")

    implementation ("androidx.camera:camera-view:${camerax_version}")
    implementation ("androidx.camera:camera-extensions:${camerax_version}")
    debugImplementation ("androidx.compose.ui:ui-tooling")
    debugImplementation ("androidx.compose.ui:ui-test-manifest")

    implementation ("androidx.compose.foundation:foundation:$compose_version")

    //MQTT
    implementation ("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation ("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")


    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // GIF
    implementation ("io.coil-kt:coil-compose:2.2.2")
    implementation ("io.coil-kt:coil-gif:2.2.2")

    //VIDEO
    implementation("io.getstream:stream-video-android-compose:0.4.2")

    //WEB RTC
    implementation ("io.getstream:stream-webrtc-android:1.0.0")

    // Añadir la dependencia de Socket.IO para cliente Java
    implementation ("io.socket:socket.io-client:2.0.1")

}

kapt {
    correctErrorTypes = true
}