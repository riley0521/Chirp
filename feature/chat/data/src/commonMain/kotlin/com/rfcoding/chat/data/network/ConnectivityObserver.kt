package com.rfcoding.chat.data.network

import kotlinx.coroutines.flow.Flow

expect class ConnectivityObserver {
    val isConnected: Flow<Boolean>
}