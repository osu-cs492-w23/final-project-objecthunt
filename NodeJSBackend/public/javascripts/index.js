let socket = io()
let roomID = ""
let currentRoom = {}

let itemsList = []

let createRoomBtn
let joinRoomBtn
let roomIDInput
let itemListInput
let searchItemList
let itemListRemoveInput
let timeLimitMinutesInput
let timeLimitSecondsInput
let nicknameInput
let createRoomDiv
let chatRoomDiv
let roomCode
let chatWindow

window.onload = () => {
    createRoomBtn = document.getElementById("createRoomBtn")
    joinRoomBtn = document.getElementById("joinRoomBtn")
    roomIDInput = document.getElementById("roomIDInput")
    itemListInput = document.getElementById("itemListInput")
    searchItemList = document.getElementById("searchItemList")
    itemListRemoveInput = document.getElementById("itemListRemoveInput")
    itemListRemoveInput.min = 0
    timeLimitMinutesInput = document.getElementById("timeLimitMinutesInput")
    timeLimitMinutesInput.min = 0
    timeLimitSecondsInput = document.getElementById("timeLimitSecondsInput")
    timeLimitSecondsInput.min = 0
    nicknameInput = document.getElementById("nicknameInput")
    createRoomDiv = document.getElementById("createRoomDiv")
    chatRoomDiv = document.getElementById("chatRoomDiv")
    chatRoomDiv.style.display = "none"
    roomCode = document.getElementById("roomCode")
    chatWindow = document.getElementById("chatWindow")
}

function socketCreateRoom(){
    console.log("attempting to create room")
    socket.emit("createRoom", {
        "itemsList": itemsList,
        "timeLimit": timeLimitMinutesInput.value * 60 + timeLimitSecondsInput.value,
        "nickname": nicknameInput.value
    }, (response) => {
        console.log("createRoom Ack: ", response)
        roomID = socket.id
        if(response["status"] === "ok"){
            createRoomDiv.style.display = "none"
            chatRoomDiv.style.display = "block"
            roomCode.textContent = "Room code: " + socket.id
            socketUpdateChat()
        }
    })
}

function socketJoinRoom(){
    socket.emit("joinRoom", roomIDInput.value, (response) => {
        console.log("joinRoom Ack: ", response)
        roomID = response["roomID"]
        if(response["status"] === "ok"){
            createRoomDiv.style.display = "none"
            chatRoomDiv.style.display = "block"
            roomCode.textContent = "Room code: " + response["roomID"]
            socketUpdateChat()
        }
    })
}

function addSearchItem(){
    if (itemListInput.value.length > 0) {
        itemsList.push(itemListInput.value)
        searchItemList.innerHTML = itemsList.map((item, index) => {
            return "<p>" + index + ". " + item + "</p>"
        }).join("")
        itemListInput.value = ""
    }
    itemListRemoveInput.max = itemsList.length - 1
}

function removeSearchItem(){
    itemsList.splice(itemListRemoveInput.value, 1)
    searchItemList.innerHTML = itemsList.map((item, index)=>{
        return "<p>" + index + ". " + item + "</p>"
    }).join("")
    itemListRemoveInput.value = ""
    itemListRemoveInput.max = itemsList.length - 1
}

function socketUpdateChat(){
    socket.emit("getChatHistory", roomID, (response)=>{
        console.log("socketUpdateChat Ack: ", response)
        if(response["status"] === "ok"){
            chatWindow.innerHTML = response["chatHistory"].map((item)=>{
                let senderNickname = item["sender"] === "System" ? "System" : currentRoom["players"][item["sender"]]
                return "<p>" + item["timeStamp"] + " " + senderNickname + ": " + item["body"] + "</p>"
            }).join("")
        }
    })
}