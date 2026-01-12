package com.rfcoding.chat.presentation.model

import com.rfcoding.chat.domain.models.Media

sealed interface MediaUi {
    data class Images(val images: List<Media>): MediaUi
    data class Audio(val audio: Media): MediaUi
    data object NoMedia: MediaUi
}