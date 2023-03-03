let socket = io()
let roomID = ""
let currentRoom = {}
let currentItem = ""

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
let readyTxt
let readyBtn
let unreadyBtn
let gameRoomDiv
let imageInput
let submitImage
let gameCurrentItem

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
    roomUserNickname = document.getElementById("roomUserNickname")
    roomObjectList = document.getElementById("roomObjectList")
    roomTimeLimit = document.getElementById("roomTimeLimit")
    readyTxt = document.getElementById("readyTxt")
    readyTxt.style.display = "none"
    readyBtn = document.getElementById("readyBtn")
    unreadyBtn = document.getElementById("unreadyBtn")
    unreadyBtn.style.display = "none"
    gameRoomDiv = document.getElementById("gameRoomDiv")
    gameRoomDiv.style.display = "none"
    submitImage = document.getElementById("submitImage")
    imageInput = document.getElementById("imageInput")

    gameCurrentItem = document.getElementById("gameCurrentItem")
}

function prepChatPage() {
    createRoomDiv.style.display = "none"
    chatRoomDiv.style.display = "block"
    roomCode.textContent = "Room code: " + roomID
    roomUserNickname.textContent = "Nickname: " + currentRoom["players"][socket.id]["nickname"]
    roomObjectList.textContent = "Possible objects: " + currentRoom["items"]
    roomTimeLimit.textContent = "Time limit (seconds): " + currentRoom["timeLimit"]
    socketUpdateChat()
}

function prepGamePage() {
    chatRoomDiv.style.display = "none"
    gameRoomDiv.style.display = "block"
}

function socketCreateRoom() {
    console.log("attempting to create room")
    if (nicknameInput.value.length > 0)
        socket.emit("createRoom", {
            "itemsList": itemsList,
            "timeLimit": parseInt(timeLimitMinutesInput.value) * 60 + parseInt(timeLimitSecondsInput.value),
            "nickname": nicknameInput.value
        }, (response) => {
            console.log("createRoom Ack: ", response)
            if (response["status"] === "ok") {
                currentRoom = response["room"]
                roomID = currentRoom["roomID"]
                prepChatPage()
            }
        })
    else
        console.log("Username is empty")
}

function socketJoinRoom() {
    if (nicknameInput.value.length > 0)
        socket.emit("joinRoom", {
            "roomID": roomIDInput.value,
            "nickname": nicknameInput.value
        }, (response) => {
            console.log("joinRoom Ack: ", response)
            roomID = response["roomID"]
            if (response["status"] === "ok") {
                prepChatPage()
            }
        })
    else
        console.log("Username is empty")
}

function addSearchItem() {
    if (itemListInput.value.length > 0) {
        itemsList.push(itemListInput.value)
        searchItemList.innerHTML = itemsList.map((item, index) => {
            return "<p>" + index + ". " + item + "</p>"
        }).join("")
        itemListInput.value = ""
    }
    itemListRemoveInput.max = itemsList.length - 1
}

function removeSearchItem() {
    itemsList.splice(itemListRemoveInput.value, 1)
    searchItemList.innerHTML = itemsList.map((item, index) => {
        return "<p>" + index + ". " + item + "</p>"
    }).join("")
    itemListRemoveInput.value = ""
    itemListRemoveInput.max = itemsList.length - 1
}

function socketUpdateChat() {
    socket.emit("getChatHistory", (response) => {
        console.log("socketUpdateChat Ack: ", response)
        if (response["status"] === "ok") {
            chatWindow.innerHTML = response["chatHistory"].map((item) => {
                let senderNickname = item["sender"] === "System" ? "System" : currentRoom["players"][item["sender"]]["nickname"]
                let time = new Date(item["timeStamp"])
                return "<p>" + time.toLocaleTimeString() + " | " + senderNickname + ": " + item["body"] + "</p>"
            }).join("")
        }
    })
}

function socketSendChat() {
    socket.emit("sendChat", chatBoxInput.value, (response) => {
        console.log("socketSendChat Ack: ", response)
        chatBoxInput.value = ""
    })
}

function socketReady() {
    socket.emit("ready", (response) => {
        console.log("socketReady Ack: ", response)
        if (response["status"] === "ok") {
            readyBtn.style.display = "none"
            readyTxt.style.display = "block"
            unreadyBtn.style.display = "block"
            socketUpdateChat()
        }
    })
}

function socketUnReady() {
    socket.emit("unready", (response) => {
        console.log("socketUnready Ack: ", response)
        if (response["status"] === "ok") {
            readyBtn.style.display = "block"
            readyTxt.style.display = "none"
            unreadyBtn.style.display = "none"
            socketUpdateChat()
        }
    })
}

function socketSubmitPicture(){
    let file = imageInput.files[0]
    console.log(file)
    socket.emit("submitAnswer", currentItem, file, (response)=>{
        if(response["status"] === "ok"){
            currentItem = response["newItem"]
        }
    })
}
socket.on("roomFilled", (room) => {
    currentRoom = room
    socketUpdateChat()
})

socket.on("chatUpdated", (newMessage) => {
    let senderNickname = newMessage["senfder"] === "System" ? "System" : currentRoom["players"][newMessage["sender"]]["nickname"]
    let time = new Date(newMessage["timeStamp"])
    chatWindow.innerHTML = chatWindow.innerHTML + "<p>" + time.toLocaleTimeString() + " " + senderNickname + ": " + newMessage["body"] + "</p>"
})

socket.on("readied", (id) => {
    console.log("readied received from ", id)
    socketUpdateChat()
})

socket.on("unreadied", (id) => {
    console.log("unreadied received from", id)
    socketUpdateChat()
})

socket.on("roomReadied", () => {
    prepGamePage()
})

socket.on("itemGenerated", (item) =>{
    gameCurrentItem.textContent = "Current item: " + item
    currentItem = item
})

socket.on("opponent disconnected", ()=>{
    roomID = ""
    currentRoom = {}
    itemsList = []
    readyBtn.style.display = "block"
    readyTxt.style.display = "none"
    unreadyBtn.style.display = "none"

    gameRoomDiv.style.display = "none"
    chatRoomDiv.style.display = "none"
    createRoomDiv.style.display = "block"
})