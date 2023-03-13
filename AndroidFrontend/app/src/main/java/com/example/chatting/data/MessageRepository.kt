package com.example.chatting.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class ChatRepository (private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private var currentMessages: List<Message>? = listOf()
    //private val mSocket = SocketHandler.getSocket()

    /*
    suspend fun loadChats() : Result<List<Chat>?> {

    }*/
}