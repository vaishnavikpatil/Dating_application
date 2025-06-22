package com.example.dating_app.utils

import io.socket.client.IO
import io.socket.client.Socket

import java.net.URISyntaxException

object SocketManager {
    private var mSocket: Socket? = null

    fun initSocket() {
        try {
            mSocket = IO.socket("http://url:3000") // replace with your backend IP
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun getSocket(): Socket? = mSocket

    fun connect() {
        mSocket?.connect()
    }

    fun disconnect() {
        mSocket?.disconnect()
    }
}
