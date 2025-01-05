plugins {
    id 'com.android.application'
    id 'kotlin-android'
    // Opcional si usas Annotations (Room, Dagger, etc.)
    id 'kotlin-kapt'
}

android {
    namespace "com.friendlysystemgroup.friendlyschedule" // Ajusta tu namespace
    compileSdk 34

    defaultConfig {
        applicationId "com.friendlysystemgroup.friendlyschedule"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    // Si necesitas habilitar ViewBinding, Jetpack Compose, etc.
    buildFeatures {
        viewBinding true
        // compose true
    }

    // Ajusta tu versión de Java/Kotlin segun necesites
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
                targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = '17'
    }

    // Opcional si usas Compose
    // composeOptions {
    //     kotlinCompilerExtensionVersion "1.5.1"
    // }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    // Otras dependencias...
}
