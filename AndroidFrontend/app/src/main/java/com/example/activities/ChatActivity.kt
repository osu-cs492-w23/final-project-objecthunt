package com.example.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SocketHandler
import com.example.chatting.R
import com.example.ui.ChatAdapter
import com.example.ui.ChatViewModel
import io.socket.client.Ack
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class ChatActivity : AppCompatActivity() {
    private val viewModel: ChatViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // connect to the node server
        val mSocket = SocketHandler.getSocket()

        val ready = Color.parseColor("#303131")
        val unready = Color.parseColor("#7586fd")

        val intentGame = Intent(this, GameActivity::class.java)
//        val intentMain =

        val shared = getSharedPreferences("settings", MODE_PRIVATE)
        val editor = shared.edit()
        val roomID = shared.getString("roomID", "").toString()
        val chatHistory = shared.getString("chatHistory", "Error").toString()

        Log.d("Chat", "${getChatList(chatHistory)}")
        for (i in 0 until getChatList(chatHistory).length()) {
            Log.d("PLEASE", "DANG")
            val chatList = getChatList(chatHistory)
            viewModel.newMessageReceived(chatList.getJSONObject(i))
        }

        mSocket.on("chatUpdated") { message ->
            viewModel.newMessageReceived(message[0] as JSONObject)
        }

        mSocket.on("roomFilled") {
            mSocket.emit("getChatHistory", Ack { args ->
                val historyString = ((args.get(0) as JSONObject)).get("chatHistory").toString()
                val tempHistory = getChatList(historyString)
                viewModel.newMessageReceived(tempHistory.getJSONObject(tempHistory.length() - 1))
            })
        }


        var isReady = false

        val readyBtn = findViewById<Button>(R.id.buttonReady)
        val sendBtn = findViewById<Button>(R.id.send_button)
        val chatEntry = findViewById<EditText>(R.id.chatbox)
        val roomHeader = findViewById<TextView>(R.id.roomID_tv)

        roomHeader.text = "Room ID: $roomID"

        val chatListRV: RecyclerView = findViewById(R.id.chatRecyclerView)
        chatListRV.layoutManager = LinearLayoutManager(this)
        chatListRV.setHasFixedSize(true)

        val adapter = ChatAdapter(roomID)
        chatListRV.adapter = adapter

        viewModel.chats.observe(this) {
            println("updating adapter")
            adapter.messageList = it.toList()
            adapter.notifyDataSetChanged()
        }

        readyBtn.setOnClickListener {
            isReady = !isReady

            if (isReady) {
                readyBtn.text = "Unready"
                readyBtn.setBackgroundColor(ready)
                mSocket.emit("ready", Ack { args ->
                    Log.d("Status", "${((args.get(0) as JSONObject))}")
                })

                mSocket.emit("getChatHistory", Ack { args ->
                    val historyString = ((args.get(0) as JSONObject)).get("chatHistory").toString()
                    val tempHistory = getChatList(historyString)
                    viewModel.newMessageReceived(tempHistory.getJSONObject(tempHistory.length() - 1))
                })


                mSocket.on("roomReadied",
                    Emitter.Listener { startGame(intentGame) })

            } else {
                readyBtn.text = "Ready"
                readyBtn.setBackgroundColor(unready)
                mSocket.emit("unready", Ack { args ->
                    Log.d("Status", "${((args.get(0) as JSONObject))}")
                })

                mSocket.emit("getChatHistory", Ack { args ->
                    val historyString = ((args.get(0) as JSONObject)).get("chatHistory").toString()
                    val tempHistory = getChatList(historyString)
                    viewModel.newMessageReceived(tempHistory.getJSONObject(tempHistory.length() - 1))
                })
            }
        }

        sendBtn.setOnClickListener {
            val message = chatEntry.text.toString()
            if (!TextUtils.isEmpty(message)) {

                mSocket.emit("sendChat", message, Ack { args ->
                    Log.d("SENDCHAT: ", "${((args[0] as JSONObject))}")
                })

                chatListRV.scrollToPosition(adapter.itemCount - 1)
                chatEntry.setText("")
            }
            hideSoftKeyboard()
        }

    }

    // The function for hiding the user's keyboard automatically
    fun Activity.hideSoftKeyboard() {
        val inputMethodManager =
            ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun startGame(intent: Intent) {
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getChatList(data: String): JSONArray {
        val chatHistory = JSONArray(data)
        val messageList = JSONArray()

        for (i in 0 until chatHistory.length()) {
            val chat = chatHistory.getJSONObject(i)
            Log.d("HERE", "$chat")
            messageList.put(chat)
        }
        return messageList
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Leave Chat room")
        builder.setMessage("Are you sure to leave?")

        builder.setPositiveButton("Yes", actionListenerYes)
        builder.setNegativeButton("No", null)

        val dialog = builder.create()
        dialog.show()
    }

    var actionListenerYes =
        DialogInterface.OnClickListener { dialog, which ->
            finish()
        }

    var actionListenerNo =
        DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        }
}