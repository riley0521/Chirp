import com.rfcoding.chirp.convention.configureAndroidTarget
import com.rfcoding.chirp.convention.configureIosTargets
import com.rfcoding.chirp.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class CmpApplicationConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with (target) {
            with (pluginManager) {
                apply("com.rfcoding.convention.android.application.compose")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            configureAndroidTarget()
            configureIosTargets()

            dependencies {
                "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling").get())
            }
        }
    }
}