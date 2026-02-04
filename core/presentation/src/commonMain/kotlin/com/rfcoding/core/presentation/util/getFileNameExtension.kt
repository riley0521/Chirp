package com.rfcoding.core.presentation.util

fun getFileNameExtension(fileName: String): String {
    return fileName.substringAfterLast(".")
}