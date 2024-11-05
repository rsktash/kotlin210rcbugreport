import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.jetbrains.compose.compiler)
}

kotlin {
  js(IR) {
    moduleName = "jsApp"
    browser {
      commonWebpackConfig { outputFileName = "jsApp.js" }
      distribution { outputDirectory = File(projectDir, "web-app") }
    }
    useCommonJs()
    nodejs { useEsModules() }
    binaries.executable()
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.base)
        implementation(projects.shared)

        implementation(project.dependencies.enforcedPlatform(libs.jetbrains.kotlin.wrappers.bom))
        implementation(libs.jetbrains.kotlin.wrappers.kotlin.browser)

      }
    }
    val jsMain by getting {
      dependencies {

        implementation(compose.ui)
        implementation(compose.material3)
        implementation(compose.foundation)
      }
    }
  }
}