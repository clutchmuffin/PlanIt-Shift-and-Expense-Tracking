plugins {
    id ("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    packagingOptions {
        resources.excludes.add("META-INF/androidx.cardview_cardview.version")
    }
}

dependencies {

    // Main dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.activity)
    implementation(libs.cardview.v7)
    implementation(platform(libs.firebase.bom))                 // Firebase BoM
    implementation(libs.firebase.auth)                          // Firebase Authentication
    implementation(libs.google.firebase.firestore)              // Firestore
    implementation("androidx.core:core-ktx:1.15.0")             // NotificationCompat
    implementation("com.kizitonwose.calendar:view:2.6.2")       // View Calendar library
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")  // Chart library

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.uiautomator)
    androidTestImplementation(libs.espresso.core.v351)
    androidTestImplementation(libs.espresso.contrib) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.espresso.intents)
}