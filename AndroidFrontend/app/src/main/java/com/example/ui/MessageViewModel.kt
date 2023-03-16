package com.example.chatting.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatting.data.Message

class ChatViewModel: ViewModel() {
    // private val repository = ChatRepository
    private val _chats = MutableLiveData<List<Message>?>(null)
    val chats: LiveData<List<Message>?> = _chats
}