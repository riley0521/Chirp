package com.rfcoding.core.data.util

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

actual fun currentLocale(): String {
    return NSLocale.currentLocale.localeIdentifier.replace("_", "-")
}