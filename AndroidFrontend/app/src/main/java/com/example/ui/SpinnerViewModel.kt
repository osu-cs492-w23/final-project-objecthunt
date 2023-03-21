package com.example.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONArray

class SpinnerViewModel : ViewModel() {
    private val _premadeMaps = MutableLiveData<List<JSONArray>>(listOf())
    val premadeMaps: LiveData<List<JSONArray>> = _premadeMaps

    fun updatePremadeMaps(newMaps: List<JSONArray>) {
        viewModelScope.launch {
            _premadeMaps.value = newMaps
            println("new message added. chats now look like this: ${_premadeMaps.value}")
        }
    }
}