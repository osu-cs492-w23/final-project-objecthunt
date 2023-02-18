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
let chatBoxInput
let roomUserNickname
let roomObjectList
let roomTimeLimit

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
    chatBoxInput = document.getElementById("chatBoxInput")
}

function prepChatPage(roomID){
    createRoomDiv.style.display = "none"
    chatRoomDiv.style.display = "block"
    roomCode.textContent = "Room code: " + roomID
    socketUpdateChat()
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
    socket.emit("joinRoom", {
        "roomID": roomIDInput.value,
        "nickname": nicknameInput.value
    }, (response) => {
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
                let senderNickname = item["sender"] === "System" ? "System" : currentRoom["players"][item["sender"]]["nickname"]
                let time = new Date(item["timeStamp"])
                return "<p>" + time.toLocaleTimeString() + " " + senderNickname + ": " + item["body"] + "</p>"
            }).join("")
        }
    })
}

function socketSendChat(){
    socket.emit("sendChat", chatBoxInput.value, roomID, (response)=>{
        console.log("socketSendChat Ack: ", response)
        chatBoxInput.value = ""
    })
}

socket.on("roomFilled", (room)=>{
    currentRoom = room
    socketUpdateChat()
})

socket.on("chatUpdated", (newMessage)=>{
    let senderNickname = newMessage["sender"] === "System" ? "System" : currentRoom["players"][newMessage["sender"]]["nickname"]
    let time = new Date(newMessage["timeStamp"])
    chatWindow.innerHTML = chatWindow.innerHTML + "<p>" + time.toLocaleTimeString() + " " + senderNickname + ": " + newMessage["body"] + "</p>"
})