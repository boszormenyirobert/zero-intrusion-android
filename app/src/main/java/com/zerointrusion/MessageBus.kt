package com.zerointrusion

import androidx.lifecycle.MutableLiveData

object MessageBus {
    val lastMessage = MutableLiveData<String>()
}