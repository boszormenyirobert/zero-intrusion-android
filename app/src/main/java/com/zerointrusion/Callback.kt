package com.zerointrusion

interface Callback {
    fun onResult(response: String?)
    fun onError(error: String)
}