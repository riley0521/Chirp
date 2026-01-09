package com.rfcoding.core.presentation.util

import platform.Foundation.NSURL

fun formatUrl(url: String): NSURL {
    return when {
        url.startsWith("file://") || url.startsWith("http") -> NSURL(string = url)
        else -> NSURL.fileURLWithPath(url)
    }
}