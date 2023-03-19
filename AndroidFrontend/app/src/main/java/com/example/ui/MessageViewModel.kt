package com.example.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Message
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class ChatViewModel : ViewModel() {
    // private val repository = ChatRepository
    private val _chats = MutableLiveData<List<Message>>(listOf())
    val chats: LiveData<List<Message>> = _chats

    fun newMessageReceived(newMessage: JSONObject) {
        viewModelScope.launch {
            val newMessageObj = Message(
                newMessage.getString("sender"),
                newMessage.getString("body"),
                Date(newMessage.getLong("timeStamp"))
            )
            val chatsCopy = mutableListOf<Message>().apply {
                addAll(_chats.value!!)
                add(newMessageObj)
            }
            _chats.value = chatsCopy
            println("new message added. chats now look like this: ${chats.value}")
        }
    }
}