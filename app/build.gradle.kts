plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

//val apiKey: String by project
val apiKey= project.properties["APIKEY"] ?: throw GradleException("APIKEY property is not set in gradle.properties")

android {

    buildFeatures{
        buildConfig = true
    }

    namespace = "com.app.attcompse"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.attcompse"
        minSdk = 24
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
            buildConfigField("String","API_KEY","\"$apiKey\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            buildConfigField("String","API_KEY","\"$apiKey\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    val composeBom = platform("androidx.compose:compose-bom:2023.09.02")
    implementation(composeBom)
    // Choose one of the following:
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    // or Material Design 2
    //implementation("androidx.compose.material:material")
    // or skip Material Design and build directly on top of foundational components
    //implementation("androidx.compose.foundation:foundation")
    // or only import the main APIs for the underlying toolkit systems,
    // such as input and measurement/layout
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Optional - Included automatically by material, only add when you need
    // the icons but not the material library (e.g. when using Material3 or a
    // custom design system based on Foundation)
    implementation("androidx.compose.material:material-icons-core")
    // Optional - Add full set of material icons
    implementation("androidx.compose.material:material-icons-extended")
    // Optional - Add window size utils
    implementation("androidx.compose.material3:material3-window-size-class")

    // Optional - Integration with activities
    implementation("androidx.activity:activity-compose:1.8.2")
    // Optional - Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    // Optional - Integration with LiveData
    implementation("androidx.compose.runtime:runtime-livedata")
    // Optional - Integration with RxJava
    //implementation("androidx.compose.runtime:runtime-rxjava2")

    //location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    val navVersion = "2.7.6"
    implementation("androidx.navigation:navigation-compose:$navVersion")

    //hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    // Splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Retrofit
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")

    // Coroutines
    val coroutinesVersion = "1.7.2"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    //paging 3
    val pagingVersion = "3.3.0-alpha02"
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    // optional - Jetpack Compose integration
    implementation("androidx.paging:paging-compose:3.3.0-alpha02")
    // alternatively - without Android dependencies for tests
    testImplementation("androidx.paging:paging-common-ktx:$pagingVersion")
    // optional - RxJava2 support
    //implementation("androidx.paging:paging-rxjava2-ktx:$pagingVersion")
    // optional - RxJava3 support
    //implementation("androidx.paging:paging-rxjava3:$pagingVersion")
    // optional - Guava ListenableFuture support
    //implementation("androidx.paging:paging-guava:$pagingVersion")

    //coil
    implementation("io.coil-kt:coil:2.4.0")

    //CameraX
    val cameraxVersion = "1.4.0-alpha03"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-video:${cameraxVersion}")

    implementation("androidx.camera:camera-view:${cameraxVersion}")
    implementation("androidx.camera:camera-extensions:${cameraxVersion}")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}