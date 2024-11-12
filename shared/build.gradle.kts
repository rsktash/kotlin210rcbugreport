import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.jetbrains.compose.compiler)
  alias(libs.plugins.sqldelight)
}

kotlin {

  applyDefaultHierarchyTemplate()

  androidTarget {
    compilations.all {
      compileTaskProvider.configure {
        compilerOptions {
          jvmTarget.set(JvmTarget.JVM_1_8)
        }
      }
    }
  }
  js(IR) {
    moduleName = "uz.rsmax.shared"
    useCommonJs()
    browser()
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach {
    it.binaries.framework {
      baseName = "shared"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.base)

      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.coroutines.core)
      implementation(compose.foundation)
      implementation(compose.ui)
      implementation(compose.material3)

      implementation(libs.jetbrains.compose.navigation)
      implementation(libs.jetbrains.compose.window.size)
      implementation(libs.jetbrains.compose.adaptive)
      implementation(libs.jetbrains.compose.adaptive.layout)
      implementation(libs.jetbrains.compose.adaptive.navigation)
      implementation(libs.jetbrains.compose.adaptive.navigation.suite)

      implementation(libs.decompose)
      implementation(libs.decompose.extensions.compose)
      implementation(libs.decompose.extensions.compose.exp)
      implementation(libs.mvikotlin)
      implementation(libs.mvikotlin.logging)
      implementation(libs.mvikotlin.extensions.coroutines)

      implementation(libs.kotlinx.datetime)

      implementation(libs.napier)

      api(libs.sqldelight.coroutines.extensions)
      implementation(libs.sqldelight.primitive.adapters)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }

    val nonJsMain by creating {
      dependsOn(commonMain.get())
      dependencies { implementation(libs.androidx.datastore.preferences) }
    }
    androidMain {
      dependsOn(nonJsMain)
      dependencies {
        implementation(libs.sqldelight.android.driver)
        implementation(libs.requery.sqlite.android)
      }
    }
    iosMain {
      dependsOn(nonJsMain)
      dependencies {
        dependencies {
          implementation(libs.sqldelight.native.driver)
        }
      }
    }
    jsMain {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.sqldelight.web.driver)
      }
    }
  }
}

android {
  namespace = "uz.rsteam.kotlin210rctest"
  compileSdk = 34
  defaultConfig {
    minSdk = 24
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}
sqldelight {
  databases {
    create("AppDatabase") {
      packageName.set("uz.rsmax")
      generateAsync.set(true)
      srcDirs("src/commonMain/database")
      dialect(libs.sqldelight.dialect335)
      module("app.cash.sqldelight:sqlite-json-module:${libs.versions.sqldelight.get()}")
    }
  }
}
