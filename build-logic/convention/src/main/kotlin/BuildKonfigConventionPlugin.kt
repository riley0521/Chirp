import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.rfcoding.chirp.convention.pathToPackageName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.Actions.with
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class BuildKonfigConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with (target) {
            with (pluginManager) {
                apply("com.codingfeline.buildkonfig")
            }

            extensions.configure<BuildKonfigExtension> {
                packageName = target.pathToPackageName()
                defaultConfigs {
                    val apiKey = gradleLocalProperties(rootDir, rootProject.providers)
                        .getProperty("API_KEY")
                        ?: throw IllegalStateException("Missing API_KEY property in local.properties")
                    buildConfigField(FieldSpec.Type.STRING, "API_KEY", apiKey)
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL", "https://rf-chat.com/api")
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_WS", "wss://rf-chat.com/ws")
                }
                targetConfigs("dev") {
                    create("android") {
                        buildConfigField(FieldSpec.Type.STRING, "BASE_URL", "http://10.0.2.2:8080/api")
                        buildConfigField(FieldSpec.Type.STRING, "BASE_URL_WS", "ws://10.0.2.2:8080/ws")
                    }
                    setupIOS { target ->
                        create(target.name) {
                            buildConfigField(FieldSpec.Type.STRING, "BASE_URL", "http://localhost:8080/api")
                            buildConfigField(FieldSpec.Type.STRING, "BASE_URL_WS", "ws://localhost:8080/ws")
                        }
                    }
                }
            }
        }
    }

    private fun Project.setupIOS(onAction: (KotlinNativeTarget) -> Unit) {
        extensions.configure<KotlinMultiplatformExtension> {
            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                onAction(iosTarget)
            }
        }
    }
}