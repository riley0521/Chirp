package com.rfcoding.chirp.di

import com.rfcoding.chirp.ChirpApplication
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

actual val platformAppModule = module {
    single { (androidApplication() as ChirpApplication).applicationScope }
}