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

            mSocket = IO.socket("http://192.168.8.142:3005")
            //mSocket = IO.socket("http://10.0.2.2:3005")

        } catch (e: URISyntaxException) {
            Log.d("SocketHandler", "setSocket() throw the error")
        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        if (this::mSocket.isInitialized)
            mSocket.disconnect()
    }
}