plugins {
    alias(libs.plugins.convention.android.application.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(projects.composeApp)

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.core.splashscreen)

    // Compose
    implementation(libs.jetbrains.compose.runtime)
    implementation(libs.jetbrains.compose.foundation)
    implementation(libs.jetbrains.compose.material3)
    implementation(libs.jetbrains.compose.ui)
    implementation(libs.jetbrains.compose.resources)
    implementation(libs.jetbrains.compose.viewmodel)
    implementation(libs.jetbrains.compose.runtime)
}