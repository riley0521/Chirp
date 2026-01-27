package com.rfcoding.chirp.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<KotlinMultiplatformExtension> {
        androidLibrary {
            namespace = this@configureKotlinMultiplatform.pathToPackageName()
        }

        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = this@configureKotlinMultiplatform.pathToFrameworkName()
            }
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")

            freeCompilerArgs.add("-Xcontext-sensitive-resolution")
            freeCompilerArgs.add("-Xreturn-value-checker=check")
            freeCompilerArgs.add("-Xdata-flow-based-exhaustiveness")

            // Don't want to use explicit backing fields for now to stay consistent on this project.
            // freeCompilerArgs.add("-Xexplicit-backing-fields")
        }
    }
}