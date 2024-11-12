import org.jetbrains.compose.internal.de.undercouch.gradle.tasks.download.Download
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


        implementation(libs.mvikotlin)
        implementation(libs.mvikotlin.main)
        implementation(libs.mvikotlin.logging)
        implementation(libs.decompose)

        implementation(libs.essenty.lifecycle)
        implementation(libs.napier)

        implementation(devNpm("copy-webpack-plugin", "12.0.2"))
        implementation(npm("sql.js", libs.versions.sqljs.get()))
      }
      resources.srcDir(layout.buildDirectory.dir("sqlite"))
    }
  }
}
rootProject.plugins.withType(YarnPlugin::class.java) {
  rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
    YarnLockMismatchReport.WARNING // NONE | FAIL
  rootProject.the<YarnRootExtension>().reportNewYarnLock = false // true
  rootProject.the<YarnRootExtension>().yarnLockAutoReplace = false // true
}
val sqliteVersion = "3460000"
val sqliteLink = "https://sqlite.org/2024/sqlite-wasm-${sqliteVersion}.zip"

val sqliteDownload =
  tasks.register("sqliteDownload", Download::class.java) {
    src(sqliteLink)
    dest(layout.buildDirectory.dir("tmp"))
    onlyIfModified(true)
  }

val sqliteUnzip =
  tasks.register("sqliteUnzip", Copy::class.java) {
    dependsOn(sqliteDownload)
    from(zipTree(layout.buildDirectory.dir("tmp/sqlite-wasm-$sqliteVersion.zip"))) {
      include("sqlite-wasm-$sqliteVersion/jswasm/**")
      exclude("**/*worker1*")

      eachFile {
        relativePath = RelativePath(true, *relativePath.segments.drop(2).toTypedArray())
      }
    }
    into(layout.buildDirectory.dir("sqlite"))
    includeEmptyDirs = false
  }

tasks.named<ProcessResources>("jsProcessResources").configure {
  //  dependsOn(copyJsResources)
  dependsOn(sqliteUnzip)
}