package com.rfcoding.core.data.util

import java.util.Locale

actual fun currentLocale(): String {
    return Locale.getDefault().toLanguageTag()
}