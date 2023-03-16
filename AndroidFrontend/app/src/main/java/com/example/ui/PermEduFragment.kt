package com.example.chatting.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment


class PermEduFragment: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(com.example.chatting.R.layout.perm_edu, container)
        val dismissBtn = view.findViewById<Button>(com.example.chatting.R.id.btn_perm_edu_dismiss)
        dismissBtn.setOnClickListener { dismiss() }
        return view
    }
}