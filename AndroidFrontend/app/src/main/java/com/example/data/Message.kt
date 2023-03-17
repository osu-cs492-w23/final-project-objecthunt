package com.example.chatting.data
import java.io.Serializable
import java.util.Date

data class Message(
    val sender: String,
    val body: String,
    val timeStamp: Date
) : Serializable