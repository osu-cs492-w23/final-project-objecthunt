package com.example.chatting.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatting.R
import com.example.chatting.SocketHandler
import com.example.chatting.data.Message
import com.example.chatting.ui.ChatAdapter
import com.example.chatting.ui.ChatViewModel
import io.socket.client.Ack
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.*


class ChatActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // connect to the node server
        val mSocket = SocketHandler.getSocket()

        val ready = Color.parseColor("#303131")
        val unready = Color.parseColor("#f08a0c")

        val intentGame = Intent(this, GameActivity::class.java)

        val shared = getSharedPreferences("settings", MODE_PRIVATE)
        val editor = shared.edit()
        val roomID = shared.getString("roomID", "").toString()
        val host = shared.getString("host", "").toString()
        val guest = shared.getString("guest", "").toString()
        val chatHistory = shared.getString("chatHistory", "Error").toString()

        val viewModel : ChatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        Log.d("CHAT Activity: ", chatHistory)

        var isReady = false
        val messageList: MutableList<Message> = getChatList(chatHistory)

        val readyBtn = findViewById<Button>(R.id.buttonReady)
        val sendBtn = findViewById<Button>(R.id.send_button)
        val chatEntry = findViewById<EditText>(R.id.chatbox)

        val chatListRV: RecyclerView = findViewById(R.id.chatRecyclerView)
        chatListRV.layoutManager = LinearLayoutManager(this)
        chatListRV.setHasFixedSize(true)

        val adapter = ChatAdapter(host)
        chatListRV.adapter = adapter
        adapter.messageList = messageList
        adapter.notifyDataSetChanged()

        readyBtn.setOnClickListener {
            isReady = !isReady

            if (isReady) {
                readyBtn.text = "Unready"
                readyBtn.setBackgroundColor(ready)
                mSocket.emit("ready", Ack { args ->
                    Log.d("Status", "${((args.get(0) as JSONObject))}")
                })
                mSocket.emit("getChatHistory", Ack { args ->
                    editor.putString("chatHistory", "${((args.get(0) as JSONObject).get("chatHistory"))}")
                    editor.commit()
                    Log.d("AGAIN", "${getChatList(shared.getString("chatHistory", "Error").toString())}")
                })

                adapter.updateChatList(getChatList(shared.getString("chatHistory", "Error").toString()))
                adapter.notifyDataSetChanged()

                mSocket.on("roomReadied",
                    Emitter.Listener { startGame(intentGame) })
            } else {
                readyBtn.text = "Ready"
                readyBtn.setBackgroundColor(unready)
                mSocket.emit("unready", Ack { args ->
                    Log.d("Status", "${((args.get(0) as JSONObject))}")
                })

                mSocket.emit("getChatHistory", Ack { args ->
                    editor.putString("chatHistory", "${((args.get(0) as JSONObject).get("chatHistory"))}")
                    editor.commit()
                })

                adapter.updateChatList(getChatList(shared.getString("chatHistory", "Error").toString()))
                adapter.notifyDataSetChanged()
            }

            adapter.updateChatList(getChatList(shared.getString("chatHistory", "Error").toString()))
            adapter.notifyDataSetChanged()

        }

        sendBtn.setOnClickListener {
            val message = chatEntry.text.toString()
            if (!TextUtils.isEmpty(message)) {

                mSocket.emit("sendChat", message, Ack { args ->
                    Log.d("SENDCHAT: ", "${((args[0] as JSONObject))}")
                })

                chatListRV.scrollToPosition(adapter.itemCount - 1)
                chatEntry.setText("")
                mSocket.emit("getChatHistory", Ack { args ->
                    Log.d("ChatHistory", "${((args.get(0) as JSONObject))}")
                })
            }
            hideSoftKeyboard()
        }

    }

    // The function for hiding the user's keyboard automatically
    fun Activity.hideSoftKeyboard() {
        val inputMethodManager = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun startGame(intent: Intent) {
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getChatList(data: String): MutableList<Message> {
        Log.d("FUnction: ", "$data")
        val chatHistory = JSONArray(data)
        val messageList = mutableListOf<Message>()

        for (i in 0 until chatHistory.length()) {
            val chat = chatHistory.getJSONObject(i)
            Log.d("HERE","$chat")
            val message = Message(sender = "${chat.get("sender")}", body = "${chat.get("body")}", timeStamp = Date.from(Instant.now()))
            messageList.apply { add(message) }
        }
        return messageList
    }
}