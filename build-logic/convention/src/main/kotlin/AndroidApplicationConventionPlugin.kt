import com.android.build.api.dsl.ApplicationExtension
import com.rfcoding.chirp.convention.configureKotlin
import com.rfcoding.chirp.convention.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with (target) {
            with (pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                namespace = "com.rfcoding.androidapp"
                compileSdk = libs.findVersion("projectCompileSdkVersion").get().toString().toInt()

                defaultConfig {
                    applicationId = libs.findVersion("projectApplicationId").get().toString()
                    targetSdk = libs.findVersion("projectTargetSdkVersion").get().toString().toInt()
                    minSdk = libs.findVersion("projectMinSdkVersion").get().toString().toInt()
                    versionCode = libs.findVersion("projectVersionCode").get().toString().toInt()
                    versionName = libs.findVersion("projectVersionName").get().toString()
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
                buildFeatures {
                    buildConfig = true
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                    isCoreLibraryDesugaringEnabled = true
                }

                configureKotlin()

                dependencies {
                    "coreLibraryDesugaring"(libs.findLibrary("android.desugar").get())
                }
            }
        }
    }
}