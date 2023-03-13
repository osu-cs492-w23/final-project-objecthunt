package com.example.chatting.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatting.R
import com.example.chatting.SocketHandler
import com.example.chatting.data.Message
import com.example.chatting.ui.ChatAdapter


/******* MEMO
 *
socket.emit("sendChat", chatEntry.text.toString(), roomID, (response)->
socket.data.roomID

 *******/

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //val mSocket = SocketHandler.getSocket()
        //val thisUser = ""

        val ready = Color.parseColor("#303131")
        val unready = Color.parseColor("#f08a0c")

        var isReady = false
        val messageList = mutableListOf<Message>()

        val readyBtn = findViewById<Button>(R.id.buttonReady)
        val sendBtn = findViewById<Button>(R.id.send_button)
        val chatEntry = findViewById<EditText>(R.id.chatbox)

        val chatListRV: RecyclerView = findViewById(R.id.chatRecyclerView)
        chatListRV.layoutManager = LinearLayoutManager(this)
        chatListRV.setHasFixedSize(true)

        val adapter = ChatAdapter()
        chatListRV.adapter = adapter

        // Test the UI
        messageList.apply {
            add(Message(sender = "You", body = "Hello"))
            add(Message(sender = "You", body = "This"))
            add(Message(sender = "You", body = "is"))
            add(Message(sender = "You", body = "Test message from you"))
            add(Message(sender = "User0101", body = "And"))
            add(Message(sender = "User0101", body = "This"))
            add(Message(sender = "User0101", body = "is"))
            add(Message(sender = "User0101", body = "Test message from your opponent"))
            adapter.messageList = messageList
            adapter.notifyDataSetChanged()
        }



        readyBtn.setOnClickListener {
            isReady = !isReady
            if (isReady) {
                readyBtn.text = "Unready"
                readyBtn.setBackgroundColor(ready)

            } else {
                readyBtn.text = "Ready"
                readyBtn.setBackgroundColor(unready)
            }

        }

        sendBtn.setOnClickListener {
            val message = chatEntry.text.toString()
            if (!TextUtils.isEmpty(message)) {
                messageList.apply {
                    add(Message(sender = "You", body = message))}
                // mSocket.emit("sendChat", message, roomID)
                chatListRV.scrollToPosition(adapter.itemCount - 1)
                chatEntry.setText("")
            }
            hideSoftKeyboard()
        }

    }

    // The function for hiding the user's keyboard automatically
    fun Activity.hideSoftKeyboard() {
        val inputMethodManager = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

}