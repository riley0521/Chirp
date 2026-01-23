import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.rfcoding.chirp.convention.configureKotlin
import com.rfcoding.chirp.convention.configureKotlinMultiplatform
import com.rfcoding.chirp.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with (target) {
            with (pluginManager) {
                apply("com.android.kotlin.multiplatform.library")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureKotlinMultiplatform()
            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware)
                    .extensions
                    .configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
                    compileSdk = libs.findVersion("projectCompileSdkVersion").get().toString().toInt()
                    minSdk = libs.findVersion("projectMinSdkVersion").get().toString().toInt()

                    configureKotlin()

                    // Required to make debug build of app run in iOS simulator
                    experimentalProperties["android.experimental.kmp.enableAndroidResources"] = "true"
                }
            }

            dependencies {
                "commonMainImplementation"(libs.findLibrary("kotlinx.serialization.json").get())
                "commonTestImplementation"(libs.findLibrary("kotlin.test").get())
            }
        }
    }
}