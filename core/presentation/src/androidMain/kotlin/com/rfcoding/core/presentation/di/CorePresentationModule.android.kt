package com.rfcoding.core.presentation.di

import com.rfcoding.core.presentation.audio.player.AudioPlayer
import com.rfcoding.core.presentation.audio.recorder.AudioRecorder
import com.rfcoding.core.presentation.files.FileManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformCorePresentationModule = module {
    singleOf(::FileManager)
    singleOf(::AudioRecorder)
    singleOf(::AudioPlayer)
}