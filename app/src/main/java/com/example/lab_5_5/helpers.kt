package com.example.lab_5_5

import android.os.Handler
import android.os.Looper

private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

fun postOnUiThread(action: () -> Unit) {
    mainHandler.post(action)
}