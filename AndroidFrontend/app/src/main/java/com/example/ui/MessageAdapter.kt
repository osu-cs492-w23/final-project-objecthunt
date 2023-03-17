package com.example.chatting.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatting.R
import com.example.chatting.data.Message

class ChatAdapter(host: String) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    val host = host
    var messageList: List<Message> = listOf()

    fun updateChatList(newList: List<Message>?) {
        messageList = newList ?: listOf()
        notifyDataSetChanged()
    }

    override fun getItemCount() = messageList.size

    /** Change the view type based on the username.
     * This part of the code can be changed.**/
    override fun getItemViewType(position: Int): Int {
        // if messageList[position].sender == thisUser
        return if (messageList[position].sender == host) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == 1) LayoutInflater.from(parent.context)
            .inflate(R.layout.item_container_sent_message, parent, false)
        else
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_container_received_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val msgTV: TextView = itemView.findViewById(R.id.textMessage)

        private lateinit var currentMessage: Message

        fun bind(message: Message) {
            currentMessage = message
            msgTV.text = message.body
        }
    }
}