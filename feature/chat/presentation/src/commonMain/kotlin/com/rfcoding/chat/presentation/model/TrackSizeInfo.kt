package com.rfcoding.chat.presentation.model

data class TrackSizeInfo(
    val trackWidth: Float,
    val barWidth: Float,
    val spacing: Float
) {
    val isValid: Boolean
        get() = trackWidth > 0f && barWidth > 0f && spacing > 0f
}
