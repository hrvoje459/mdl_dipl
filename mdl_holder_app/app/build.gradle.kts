plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("plugin.serialization") version "1.9.23"
}

android {
    namespace = "fer.dipl.mdl.mdl_holder_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "fer.dipl.mdl.mdl_holder_app"
        minSdk = 33
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

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("com.android.identity:identity-credential-android:20231002")
    implementation("com.android.identity:identity-credential:20231002")
    implementation("com.github.yuriy-budiyev:code-scanner:2.3.0")
    // https://mvnrepository.com/artifact/com.google.zxing/core
    implementation("com.google.zxing:core:3.3.3")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")


    // WALT ID LIBRARIES
    implementation("id.walt.did:waltid-did:1.0.2403291506-SNAPSHOT"){
        exclude("org.bouncycastle", "bcprov-jdk15on")
        exclude("org.bouncycastle","bcpkix-lts8on")
        exclude("org.bouncycastle","bcprov-lts8on")
    }
    implementation("id.walt:waltid-mdoc-credentials-jvm:1.0.2403291506-SNAPSHOT")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.16.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
    implementation("com.augustcellars.cose:cose-java:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}