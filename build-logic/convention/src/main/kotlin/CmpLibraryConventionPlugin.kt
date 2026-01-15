import com.rfcoding.chirp.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CmpLibraryConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with (target) {
            with (pluginManager) {
                apply("com.rfcoding.convention.kmp.library")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.compose")
            }
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain.dependencies {
                        implementation(libs.findLibrary("jetbrains.compose.ui").get())
                        implementation(libs.findLibrary("jetbrains.compose.foundation").get())
                        implementation(libs.findLibrary("jetbrains.compose.material3").get())
                        implementation(libs.findLibrary("jetbrains.compose.material.icons.core").get())
                    }

                    androidMain.dependencies {
                        implementation(libs.findLibrary("androidx.compose.ui.tooling").get())
                    }
                }
            }
        }
    }
}