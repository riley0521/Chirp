package com.rfcoding.chirp.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.androidLibrary(
    action: KotlinMultiplatformAndroidLibraryExtension.() -> Unit
) {
    (this as ExtensionAware)
        .extensions
        .configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            action()
        }
}