package com.rfcoding.chirp.navigation

object ExternalUriHandler {

    private var cached: String? = null

    var listener: ((uri: String) -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                cached?.let {
                    value(it)
                }
                cached = null
            }
        }

    fun onNewUri(uri: String) {
        cached = uri
        listener?.let {
            it.invoke(uri)
            cached = null
        }
    }
}