plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pls"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pls"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // หมายเหตุ: CardView น่าจะรวมอยู่ใน material แล้ว
    // แต่หากต้องการระบุชัดเจน สามารถเพิ่มได้
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Retrofit & OkHttp สำหรับการเชื่อมต่อ API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Gson สำหรับแปลง JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines สำหรับการทำงานแบบ async
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    // Add Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Add Cloud Messaging (FCM) สำหรับการแจ้งเตือน
    implementation("com.google.firebase:firebase-messaging")
}