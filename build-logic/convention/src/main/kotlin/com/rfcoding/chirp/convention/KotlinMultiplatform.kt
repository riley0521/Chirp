package com.rfcoding.chirp.convention

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<LibraryExtension> {
        namespace = this@configureKotlinMultiplatform.pathToPackageName()
    }
    configureAndroidTarget()

    extensions.configure<KotlinMultiplatformExtension> {
//        androidLibrary {
//            namespace = this@configureKotlinMultiplatform.pathToPackageName()
//        }

        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = this@configureKotlinMultiplatform.pathToFrameworkName()
            }
        }

        sourceSets.all {
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
                freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
                freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")

                freeCompilerArgs.add("-Xcontext-sensitive-resolution")
                freeCompilerArgs.add("-Xnested-type-aliases")
                freeCompilerArgs.add("-Xdata-flow-based-exhaustiveness")
            }
            languageSettings.enableLanguageFeature("ExplicitBackingFields")
        }
    }
}