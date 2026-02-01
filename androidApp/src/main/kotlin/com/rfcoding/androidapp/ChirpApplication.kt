package com.rfcoding.androidapp

import android.app.Application
import com.rfcoding.chirp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class ChirpApplication: Application() {

    override fun onCreate() {
        super.onCreate()
//        if (BuildConfig.DEBUG) {
//            Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)
//        } else {
//            Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.Auto)
//        }

        initKoin {
            androidContext(this@ChirpApplication)
            androidLogger()
        }
    }
}