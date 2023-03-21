package com.example

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {
    lateinit var mSocket: Socket

    @Synchronized
    fun setSocket() {
        try {

           mSocket = IO.socket("http://192.168.86.35:6571")
           // mSocket = IO.socket("http://192.168.86.21:3005")

        } catch (e: URISyntaxException) {
            Log.d("SocketHandler", "setSocket() throw the error")
        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun getSocketOrNull(): Socket? {
        if (!this::mSocket.isInitialized) {
            return null
        }
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
}