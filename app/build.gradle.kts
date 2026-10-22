plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.shadowspark.morontomornica"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.shadowspark.morontomornica"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0-oct19"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildConfigField("String", "YOUTUBE_API_KEY", "\"\"")
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
    create("release") {
      val defaultUploadKeyPath =
        "${System.getProperty("user.home")}/.local/share/moronto-mornica/signing/upload.jks"
      val ksPath =
        providers.environmentVariable("UPLOAD_KEYSTORE_PATH").orNull
          ?: providers.gradleProperty("UPLOAD_KEYSTORE_PATH").orNull
          ?: defaultUploadKeyPath
      val ksFile = file(ksPath)
      val ksPass =
        providers.environmentVariable("UPLOAD_KEYSTORE_PASSWORD").orNull
          ?: providers.gradleProperty("UPLOAD_KEYSTORE_PASSWORD").orNull
      val aliasName =
        providers.environmentVariable("UPLOAD_KEY_ALIAS").orNull
          ?: providers.gradleProperty("UPLOAD_KEY_ALIAS").orNull
          ?: "moronto-upload"
      val aliasPass =
        providers.environmentVariable("UPLOAD_KEY_PASSWORD").orNull
          ?: providers.gradleProperty("UPLOAD_KEY_PASSWORD").orNull
          ?: ksPass
      if (ksFile.exists() && !ksPass.isNullOrBlank() && !aliasPass.isNullOrBlank()) {
        storeFile = ksFile
        storePassword = ksPass
        keyAlias = aliasName
        keyPassword = aliasPass
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      val releaseCfg = signingConfigs.findByName("release")
      if (
        releaseCfg?.storeFile?.exists() == true &&
          !releaseCfg.storePassword.isNullOrBlank() &&
          !releaseCfg.keyPassword.isNullOrBlank()
      ) {
        signingConfig = releaseCfg
      }
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
    buildConfig = true
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      isReturnDefaultValues = true
    }
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
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
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.okhttp)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)

  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
