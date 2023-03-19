package com.example.data

import java.io.Serializable
import java.util.*

data class Message(
    val sender: String,
    val body: String,
    val timeStamp: Date
) : Serializable