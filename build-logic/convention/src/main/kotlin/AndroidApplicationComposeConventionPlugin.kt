import com.android.build.api.dsl.ApplicationExtension
import com.rfcoding.chirp.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationComposeConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with (target) {
            with (pluginManager) {
                apply("com.rfcoding.convention.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                buildFeatures {
                    compose = true
                }

                dependencies {
                    val bom = libs.findLibrary("androidx.compose.bom").get()
                    "implementation"(platform(bom))
                    "testImplementation"(platform(bom))
                    "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling.preview").get())
                    "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling").get())
                }
            }
        }
    }
}