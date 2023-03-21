package com.example.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.SocketHandler
import com.example.chatting.R
import com.example.ui.SpinnerViewModel
import com.google.android.material.snackbar.Snackbar
import io.socket.client.Ack
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class CreateActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var premadeMaps = mutableListOf<JSONArray>()
    private var mapNames = mutableListOf<String>()
    private var selection = JSONArray()
    private var idx = 0
    private var length = 0
    private val viewModel: SpinnerViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new)

        // connect to the node server
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        val mSocket = SocketHandler.getSocket()

        Log.d("Create Activity Length is ", length.toString())

        mSocket.emit("getMaps", Ack { args ->
            val mapList: JSONArray = (args[0] as JSONObject).get("maps") as JSONArray
            for (i in 0 until mapList.length()) {
                val currentMap = mapList.getJSONObject(i)
                val itemList = currentMap.get("items") as JSONArray
                premadeMaps.add(itemList)
                Log.d("GET MAPS", "HERE")
                mapNames.add("Map " + (i + 1).toString())
            }

            println("premade maps processed: $premadeMaps")
            Log.d("Create Activity", "premade maps processed: $premadeMaps")
            viewModel.updatePremadeMaps(premadeMaps)
        })

        val usernameEntry = findViewById<EditText>(R.id.hostusername)
        val minEntry = findViewById<EditText>(R.id.time_minute)
        val secEntry = findViewById<EditText>(R.id.time_second)
        val CreateBtn = findViewById<Button>(R.id.buttonCreate)

        val intentChat = Intent(this, ChatActivity::class.java)
        val sharedPreference = getSharedPreferences("settings", MODE_PRIVATE)
        val editor = sharedPreference.edit()



        val item1 = JSONObject()
        try {
            item1.put("name", "1")
            item1.put("longtitude", "1")
            item1.put("latitude", "1")
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

//        val item2 = JSONObject()
//        try {
//            item2.put("name", "bottle")
//            item2.put("longtitude", "-123.281039")
//            item2.put("latitude", "44.571431")
//        } catch (e: JSONException) {
//            // TODO Auto-generated catch block
//            e.printStackTrace()
//        }
//
//
        val jsonArray = JSONArray()
//
        jsonArray.put(item1)
//        jsonArray.put(item2)
//
//        val test = jsonArray.toString()
//
//        val stringToJA = JSONArray(test)
//
//        Log.d("JSONArray: ", "$stringToJA")

        viewModel.premadeMaps.observe(this) {
            Log.d("PLZZZZZZ", "updating adapter")
            val spinner = findViewById<Spinner>(R.id.spinner_maps)
            spinner.onItemSelectedListener = this

            val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                mapNames
            )

            arrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            spinner.adapter = arrayAdapter


        }

        CreateBtn.setOnClickListener {
            val nickname = usernameEntry.text.toString()
            val min = minEntry.text.toString()
            val sec = secEntry.text.toString()
            val timelimit = calculateTime(min, sec)

            // Check if both values are empty or not
            if (!TextUtils.isEmpty(nickname) && timelimit > 0) {
                mSocket.emit("echoTest", "CONNECTED")
                mSocket.emit(
                    "createRoom",
                    JSONObject("{'nickname': ${nickname}, 'itemsList': ${selection}, 'timeLimit': ${timelimit}}"),
                    Ack { createRoomArgs ->
                        Log.d(
                            "CreateActivity",
                            "Ack ${((createRoomArgs.get(0) as JSONObject).get("room") as JSONObject)}"
                        )
                        if ("${((createRoomArgs[0] as JSONObject)).get("status")}" == "ok") {
                            editor.putString(
                                "roomID",
                                "${
                                    ((createRoomArgs.get(0) as JSONObject).get("room") as JSONObject).get(
                                        "roomID"
                                    )
                                }"
                            )
                            editor.putString("nickname", nickname)
                            editor.putInt("timelimit", timelimit)

                            mSocket.emit(
                                "sendChat",
                                "Your room ID is: ${
                                    ((createRoomArgs.get(0) as JSONObject).get("room") as JSONObject).get(
                                        "roomID"
                                    )
                                }",
                                Ack { sendChatArgs ->
                                    Log.d("SENDCHAT: ", "${((sendChatArgs[0] as JSONObject))}")
                                })

                            mSocket.emit("getChatHistory", Ack { args ->
                                editor.putString(
                                    "chatHistory",
                                    "${((args.get(0) as JSONObject).get("chatHistory"))}"
                                )
                                editor.apply()
//                          Log.d("CHAT HISTORY UPDATED: ",  sharedPreference.getString("chatHistory", "").toString())
                            })

                            editor.apply()
                            startActivity(intentChat)

                            // Make the user go to the main screen
                            finish()
                        } else {
                            Log.d("Create Activity", "Unable to create a room")
                            val snackbar = Snackbar
                                .make(it, "Something went wrong. Try again.", Snackbar.LENGTH_LONG)
                            snackbar.show()
                        }
                    })

                // Need to block the back button or
                // warn the user that if they press the back button, it will go to the main
            } else {
                val snackbar = Snackbar
                    .make(it, "ERROR: Set your time over 0 sec", Snackbar.LENGTH_LONG)
                snackbar.show()
            }
        }
    }

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View, position: Int,
        id: Long
    ) {
        Log.d("Selection is now", "${premadeMaps[position]}}")
        idx = position
        selection = premadeMaps[idx]

        var text = ""

        for (i in 0 until selection.length()) {
            val currentItem = selection.getJSONObject(i)
            text += currentItem.get("name").toString() + " "
        }

        val itemListText = findViewById<TextView>(R.id.items)
        itemListText.text = text
        Log.d("ITEMS!!!!!!!!!!!!!!!!", text)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    fun calculateTime(min: String, sec: String): Int {
        var result: Int = 0

        try {
            result = min.toInt() * 60 + sec.toInt()

        } catch (e: NumberFormatException) {
            Log.d("Calcuation: ", "Invalid inputs")
            return -1
        }

        return result
    }
}