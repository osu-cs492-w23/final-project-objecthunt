package com.example.data


import java.io.Serializable

data class ItemToFind(
    val name: String,
    val latitude: Long,
    val longtitude: Long
) : Serializable