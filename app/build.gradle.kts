import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.tayf.photocompressor"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.tayf.photocompressor"
    minSdk = 24
    targetSdk = 36
    versionCode = 4
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH")
        ?: System.getenv("CM_KEYSTORE_PATH")
        ?: "${rootDir}/my-upload-key.jks"
      val keystoreFile = file(keystorePath)
      if (keystoreFile.exists()) {
        storeFile = keystoreFile
        storePassword = System.getenv("STORE_PASSWORD") ?: System.getenv("CM_KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS") ?: System.getenv("CM_KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD") ?: System.getenv("CM_KEY_PASSWORD")
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      val keystorePath = System.getenv("KEYSTORE_PATH")
        ?: System.getenv("CM_KEYSTORE_PATH")
        ?: "${rootDir}/my-upload-key.jks"
      if (file(keystorePath).exists()) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

configurations.all {
  exclude(group = "com.android.billingclient")
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.exifinterface)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  // implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  // implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  // implementation(libs.logging.interceptor)
  // implementation(libs.moshi.kotlin)
  // implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  // implementation(libs.retrofit)

  // Appodeal SDK 4.3.0
  implementation("com.appodeal.ads.sdk:core:4.3.0")

  // BidMachine
  implementation("io.bidmachine:ads.networks.amazon:12.0.0.0")
  implementation("io.bidmachine:ads.networks.meta_audience:6.21.0.1")
  implementation("io.bidmachine:ads.networks.mintegral:17.1.61.1")
  implementation("io.bidmachine:ads.networks.my_target:5.47.1.2") {
    exclude(group = "com.android.billingclient")
  }
  implementation("io.bidmachine:ads.networks.vungle:7.7.4.0")

  // AppLovin MAX
  implementation("com.applovin.mediation:amazon-tam-adapter:11.3.1.0")
  implementation("com.applovin.mediation:bidmachine-adapter:3.7.1.0")
  implementation("com.applovin.mediation:bigoads-adapter:5.9.0.0")
  implementation("com.applovin.mediation:bytedance-adapter:8.1.0.3.0")
  implementation("com.applovin.mediation:chartboost-adapter:9.10.2.0")
  implementation("com.applovin.mediation:facebook-adapter:6.21.0.0")
  implementation("com.applovin.mediation:fyber-adapter:8.4.6.0")
  implementation("com.applovin.mediation:inmobi-adapter:11.3.0.1")
  implementation("com.applovin.mediation:ironsource-adapter:9.4.4.0.0")
  implementation("com.applovin.mediation:mintegral-adapter:17.1.61.0")
  implementation("com.applovin.mediation:mobilefuse-adapter:1.11.0.0")
  implementation("com.applovin.mediation:moloco-adapter:4.3.1.0")
  implementation("com.applovin.mediation:ogury-presage-adapter:6.2.0.0")
  implementation("com.applovin.mediation:pubmatic-adapter:4.10.0.0")
  implementation("com.applovin.mediation:smaato-adapter:22.7.2.3")
  implementation("com.applovin.mediation:unityads-adapter:4.17.0.0")
  implementation("com.applovin.mediation:verve-adapter:3.7.1.0")
  implementation("com.applovin.mediation:vungle-adapter:7.7.4.0")
  implementation("com.applovin.mediation:yandex-adapter:7.17.0.0")

  // Bidon
  implementation("org.bidon:amazon-adapter:12.0.0.0")
  implementation("org.bidon:applovin-adapter:13.6.3.0")
  implementation("org.bidon:bidmachine-adapter:3.7.1.0")
  implementation("org.bidon:bigoads-adapter:5.9.0.0")
  implementation("org.bidon:chartboost-adapter:9.10.2.0")
  implementation("org.bidon:dtexchange-adapter:8.4.6.0")
  implementation("org.bidon:inmobi-adapter:11.3.0.0")
  implementation("org.bidon:ironsource-adapter:9.4.4.0")
  implementation("org.bidon:meta-adapter:6.21.0.0")
  implementation("org.bidon:mintegral-adapter:17.1.61.0")
  implementation("org.bidon:mobilefuse-adapter:1.11.0.0")
  implementation("org.bidon:moloco-adapter:4.3.1.0")
  implementation("org.bidon:startio-adapter:5.2.4.1")
  implementation("org.bidon:taurusx-adapter:1.12.2.0")
  implementation("org.bidon:unityads-adapter:4.17.0.0")
  implementation("org.bidon:vkads-adapter:5.47.1.0") {
    exclude(group = "com.android.billingclient")
  }
  implementation("org.bidon:vungle-adapter:7.7.4.0")
  implementation("org.bidon:yandex-adapter:7.17.0.0")
  implementation("org.bidon:zmaticoo-adapter:2.0.6.0.0")

  // Appodeal adapters
  implementation("com.appodeal.ads.sdk.adapters:amazon:12.0.0.0")
  implementation("com.appodeal.ads.sdk.adapters:applovin:13.6.3.0")
  implementation("com.appodeal.ads.sdk.adapters:applovin_max:13.6.3.0")
  implementation("com.appodeal.ads.sdk.adapters:bidmachine:3.7.1.0")
  implementation("com.appodeal.ads.sdk.adapters:bidon:0.14.0.0")
  implementation("com.appodeal.ads.sdk.adapters:bigo_ads:5.9.0.0")
  implementation("com.appodeal.ads.sdk.adapters:chartboost:9.10.2.0")
  implementation("com.appodeal.ads.sdk.adapters:dt_exchange:8.4.6.0")
  implementation("com.appodeal.ads.sdk.adapters:iab:1.8.1.0")
  implementation("com.appodeal.ads.sdk.adapters:inmobi:11.3.0.0")
  implementation("com.appodeal.ads.sdk.adapters:ironsource:9.4.4.0")
  implementation("com.appodeal.ads.sdk.adapters:meta:6.21.0.0")
  implementation("com.appodeal.ads.sdk.adapters:mintegral:17.1.61.0")
  implementation("com.appodeal.ads.sdk.adapters:mobilefuse:1.11.0.0")
  implementation("com.appodeal.ads.sdk.adapters:moloco:4.3.1.0")
  implementation("com.appodeal.ads.sdk.adapters:my_target:5.47.1.0") {
    exclude(group = "com.android.billingclient")
  }
  implementation("com.appodeal.ads.sdk.adapters:ogury:6.2.0.0")
  implementation("com.appodeal.ads.sdk.adapters:pubmatic:4.10.0.0")
  implementation("com.appodeal.ads.sdk.adapters:sentry_analytics:8.44.1.0")
  implementation("com.appodeal.ads.sdk.adapters:smaato:22.7.2.0")
  implementation("com.appodeal.ads.sdk.adapters:startio:5.2.4.0")
  implementation("com.appodeal.ads.sdk.adapters:taurusx:1.12.2.0")
  implementation("com.appodeal.ads.sdk.adapters:unity_ads:4.17.0.0")
  implementation("com.appodeal.ads.sdk.adapters:verve:3.7.1.0")
  implementation("com.appodeal.ads.sdk.adapters:vungle:7.7.4.0")
  implementation("com.appodeal.ads.sdk.adapters:yandex:7.17.0.0")
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  // "ksp"(libs.moshi.kotlin.codegen)
}
