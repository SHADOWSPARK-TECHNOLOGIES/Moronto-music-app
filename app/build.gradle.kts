plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.shadowspark.morontomornica"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.shadowspark.morontomornica"
    minSdk = 26
    targetSdk = 36
    versionCode = 2
    versionName = "1.0.1-musicolet"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("debugConfig") {
      val ks = file("${rootDir}/debug.keystore")
      if (ks.exists()) {
        storeFile = ks
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
    debug {
      val debugCfg = signingConfigs.findByName("debugConfig")
      if (debugCfg?.storeFile?.exists() == true) {
        signingConfig = debugCfg
      }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    compose = true
  }

  lint {
    disable += "UnsafeOptInUsageError"
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.media3.common)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.session)
  implementation(libs.kotlinx.coroutines.android)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
