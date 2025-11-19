package com.rfcoding.chirp

import android.app.Application
import com.rfcoding.chirp.di.initKoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class ChirpApplication: Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@ChirpApplication)
            androidLogger()
        }
    }
}