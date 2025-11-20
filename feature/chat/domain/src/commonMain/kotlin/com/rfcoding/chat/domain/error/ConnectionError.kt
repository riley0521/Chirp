package com.rfcoding.chat.domain.error

import com.rfcoding.core.domain.util.Error

enum class ConnectionError: Error {
    NOT_CONNECTED,
    MESSAGE_SEND_FAILED
}