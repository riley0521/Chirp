package com.rfcoding.chat.data.lifecycle

import kotlinx.coroutines.flow.Flow

expect class AppLifecycleObserver {
    val isInForeground: Flow<Boolean>
}