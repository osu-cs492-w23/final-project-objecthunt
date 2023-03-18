let socket = io()
let roomID = ""
let currentRoom = {}
let currentItem = ""

let itemsList = []

let elements = {}

let timeLeft = 0

let sampleMaps = []

window.onload = () => {
    elements = {
        "createRoomDiv": document.getElementById("createRoomDiv"),
        "createRoomBtn": document.getElementById("createRoomBtn"),
        "joinRoomBtn": document.getElementById("joinRoomBtn"),
        "roomIDInput": document.getElementById("roomIDInput"),
        "itemListInput": document.getElementById("itemListInput"),
        "searchItemList": document.getElementById("searchItemList"),
        "itemListRemoveInput": document.getElementById("itemListRemoveInput"),
        "timeLimitMinutesInput": document.getElementById("timeLimitMinutesInput"),
        "timeLimitSecondsInput": document.getElementById("timeLimitSecondsInput"),
        "nicknameInput": document.getElementById("nicknameInput"),
        "presetMaps": document.getElementById("presetMaps"),
        "chooseMapInput": document.getElementById("chooseMapInput"),
        "submitMapChoice": document.getElementById("submitMapChoice"),

        "chatRoomDiv": document.getElementById("chatRoomDiv"),
        "roomCode": document.getElementById("roomCode"),
        "chatWindow": document.getElementById("chatWindow"),
        "chatBoxInput": document.getElementById("chatBoxInput"),
        "roomUserNickname": document.getElementById("roomUserNickname"),
        "roomObjectList": document.getElementById("roomObjectList"),
        "roomTimeLimit": document.getElementById("roomTimeLimit"),
        "readyTxt": document.getElementById("readyTxt"),
        "readyBtn": document.getElementById("readyBtn"),
        "unreadyBtn": document.getElementById("unreadyBtn"),

        "gameRoomDiv": document.getElementById("gameRoomDiv"),
        "timeLeft": document.getElementById("timeLeft"),
        "submitImage": document.getElementById("submitImage"),
        "imageInput": document.getElementById("imageInput"),
        "gameCurrentItem": document.getElementById("gameCurrentItem"),
        "latitudeInput": document.getElementById("latitudeInput"),
        "longtitudeInput": document.getElementById("longtitudeInput"),

        "gameResultDiv": document.getElementById("gameResultDiv"),
        "winnerText": document.getElementById("winnerText"),
        "submittedPictures": document.getElementById("submittedPictures")
    }
    socket.emit("getMaps", (data) => {
        if(data["status"] === "ok") {
            sampleMaps = data["maps"]
            console.log("got sample maps:", data["maps"])
            console.log(data["maps"].map(map=> {
                return JSON.stringify(map)
            }).join(""))
            elements.presetMaps.textContent = data["maps"].map(map=> JSON.stringify(map)).join("")
        }
    })


    elements.itemListRemoveInput.min = 0
    elements.timeLimitMinutesInput.min = 0
    elements.timeLimitSecondsInput.min = 0

    elements.chatRoomDiv.style.display = "none"

    elements.readyTxt.style.display = "none"
    elements.unreadyBtn.style.display = "none"
    elements.gameRoomDiv.style.display = "none"

    elements.gameResultDiv.style.display = "none"
}



function prepChatPage() {
    elements.createRoomDiv.style.display = "none"
    elements.chatRoomDiv.style.display = "block"
    elements.roomCode.textContent = "Room code: " + roomID
    elements.roomUserNickname.textContent = "Nickname: " + currentRoom["players"][socket.id]["nickname"]
    elements.roomObjectList.textContent = "Possible objects: " + currentRoom["items"].map(item=>item["name"])
    elements.roomTimeLimit.textContent = "Time limit (seconds): " + currentRoom["timeLimit"]
    socketUpdateChat()
}

function prepGamePage() {
    elements.chatRoomDiv.style.display = "none"
    elements.gameRoomDiv.style.display = "block"
}

function prepGameEndPage(winner){
    elements.gameRoomDiv.style.display = "none"
    elements.gameResultDiv.style.display = "block"

    elements.winnerText.textContent = winner === socket.id ? "You won!" : winner === "draw" ? "Game drawn" : "You lost :("
    let maxLen = Object.values(currentRoom["players"]).map(player => player.itemIndex).reduce((a, b) => Math.max(a, b))
    for(let i = 0; i < maxLen; i++){

        const item = currentRoom["items"][i]
        const playerPics = Object.values(currentRoom["players"]).map(player => player["pictures"][i] ? player["pictures"][i] : null)
        const itemHTML = "<p>" + item + "</p>"
        const playerPicsHTML = playerPics.map(picture => {
            if(!picture){
                return "<img alt='no image' class=endingPics style='height: 200px' src=''>"
            }
            const arrayBufferView = new Uint8Array( picture );
            const blob = new Blob( [ arrayBufferView ], { type: "image/jpeg" } );
            const urlCreator = window.URL || window.webkitURL;
            const imageUrl = urlCreator.createObjectURL( blob );
            return "<img class=endingPics alt=supposed" + item + " src=" + imageUrl + ">"
        })
        console.log("HTML: ", playerPicsHTML)
        elements.submittedPictures.innerHTML += "<div>" + itemHTML + playerPicsHTML + "</div>"
    }

    console.log(currentRoom)
}

function socketCreateRoom() {
    console.log("attempting to create room")
    if (elements.nicknameInput.value.length < 1) {
        console.log("Username is empty")
        return
    }
    const timeMinuteString = parseInt(elements.timeLimitMinutesInput.value ? elements.timeLimitMinutesInput.value : "0")
    const timeSecondString = parseInt(elements.timeLimitSecondsInput.value ? elements.timeLimitSecondsInput.value : "0")
    socket.emit("createRoom", {
        "itemsList": itemsList,
        "timeLimit": timeMinuteString * 60 + timeSecondString,
        "nickname": elements.nicknameInput.value
    }, (response) => {
        if (response["status"] === "ok") {
            currentRoom = response["room"]
            roomID = currentRoom["roomID"]
            prepChatPage()
        } else if (response["status"] === "error") {
            console.log("createRoom error: ", response["errorMessage"])
        }
    })
}

function socketJoinRoom() {
    if (elements.nicknameInput.value.length < 1) {
        console.log("Username is empty")
        return
    }
    socket.emit("joinRoom", {
        "roomID": elements.roomIDInput.value,
        "nickname": elements.nicknameInput.value
    }, (response) => {
        if (response["status"] === "ok") {
            roomID = response["roomID"]
            prepChatPage()
        } else if (response["status"] === "error") {
            console.log("joinRoom error: ", response["errorMessage"])
        }
    })
}

function addSearchItem() {
    if (elements.itemListInput.value.length > 0) {
        itemsList.push(elements.itemListInput.value)
        elements.searchItemList.innerHTML = itemsList.map((item, index) => {
            return "<p>" + index + ". " + item["name"] + " at (" + item["latitude"] + "," + item["longtitude"] + ")</p>"
        }).join("")
        elements.itemListInput.value = ""
    }
    elements.itemListRemoveInput.max = itemsList.length - 1
}

function removeSearchItem() {
    itemsList.splice(elements.itemListRemoveInput.value, 1)
    elements.searchItemList.innerHTML = itemsList.map((item, index) => {
        return "<p>" + index + ". " + item["name"] + " at (" + item["latitude"] + "," + item["longtitude"] + ")</p>"
    }).join("")
    elements.itemListRemoveInput.value = ""
    elements.itemListRemoveInput.max = itemsList.length - 1
}

function chooseMap(){
    let mapFound = false
    sampleMaps.forEach(map => {
        if(map["name"] === elements.chooseMapInput.value){
            itemsList = map["items"]
            elements.searchItemList.innerHTML = itemsList.map((item, index) => {
                return "<p>" + index + ". " + item["name"] + " at (" + item["latitude"] + "," + item["longtitude"] + ")</p>"
            }).join("")
            elements.itemListRemoveInput.value = ""
            elements.itemListRemoveInput.max = itemsList.length - 1
            mapFound = true
        }
    })
    if(!mapFound)
        console.log("map not found")
}

function secondToTime(sec){
    const minute = Math.floor(sec / 60)
    const second = sec % 60
    return minute + ":" + second
}

function socketUpdateChat() {
    socket.emit("getChatHistory", (response) => {
        if (response["status"] === "ok") {
            elements.chatWindow.innerHTML = response["chatHistory"].map((item) => {
                let senderNickname = item["sender"] === "System" ? "System" : currentRoom["players"][item["sender"]]["nickname"]
                let time = new Date(item["timeStamp"])
                return "<p>" + time.toLocaleTimeString() + " | " + senderNickname + ": " + item["body"] + "</p>"
            }).join("")
        } else if (response["status"] === "error") {
            console.log("getChatHistory error: ", response["errorMessage"])
        }
    })
}

function socketSendChat() {
    socket.emit("sendChat", elements.chatBoxInput.value, (response) => {
        if (response["status"] === "ok")
            elements.chatBoxInput.value = ""
        else if (response["status"] === "error") {
            console.log("sendChat error: ", response["errorMessage"])
        }
    })
}

function socketReady() {
    socket.emit("ready", (response) => {
        if (response["status"] === "ok") {
            elements.readyBtn.style.display = "none"
            elements.readyTxt.style.display = "block"
            elements.unreadyBtn.style.display = "block"
            socketUpdateChat()
        } else if (response["status"] === "error") {
            console.log("ready error: ", response["errorMessage"])
        }
    })
}

function socketUnReady() {
    socket.emit("unready", (response) => {
        if (response["status"] === "ok") {
            elements.readyBtn.style.display = "block"
            elements.readyTxt.style.display = "none"
            elements.unreadyBtn.style.display = "none"
            socketUpdateChat()
        } else if (response["status"] === "error") {
            console.log("unready error: ", response["errorMessage"])
        }
    })
}

function socketSubmitPicture() {
    let file = elements.imageInput.files[0]
    console.log(file)
    const coordinate = {
        "latitude": elements.latitudeInput.value,
        "longtitude": elements.longtitudeInput.value
    }
    socket.emit("submitAnswer", currentItem, file, coordinate, (response) => {
        if (response["status"] === "ok") {
            console.log("answer accepted")
        } else if(response["status"] === "wrong_answer"){
            console.log("wrong answer")
            elements.gameCurrentItem.textContent += " Wrong answer"
        } else if(response["status"] === "wrong_location"){
            console.log("wrong location")
            elements.gameCurrentItem.textContent += " Wrong location"
        } else if (response["status"] === "error") {
            console.log("submitAnswer error: ", response["errorMessage"])
        }
    })
}

socket.on("roomFilled", (room) => {
    currentRoom = room
    socketUpdateChat()
})

socket.on("chatUpdated", (newMessage) => {
    let senderNickname = newMessage["sender"] === "System" ? "System" : currentRoom["players"][newMessage["sender"]]["nickname"]
    let time = new Date(newMessage["timeStamp"])
    elements.chatWindow.innerHTML = elements.chatWindow.innerHTML + "<p>" + time.toLocaleTimeString() + " " + senderNickname + ": " + newMessage["body"] + "</p>"
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

socket.on("itemGenerated", (item) => {
    elements.gameCurrentItem.textContent = "Current item: " + item["name"]
    currentItem = item
    timeLeft = currentRoom["timeLimit"]
    setInterval(()=> {
        timeLeft--
        elements.timeLeft.textContent = "Time left: " + secondToTime(timeLeft)
    }, 1000)
})

socket.on("newItem", (item) => {
    elements.gameCurrentItem.textContent = "Current item: " + item["name"]
    elements.imageInput.value = null
})

socket.on("gameEnded", (data) => {
    currentRoom = data["room"]
    prepGameEndPage(data["winner"])
})

socket.on("opponent disconnected", () => {
    roomID = ""
    currentRoom = {}
    itemsList = []
    elements.readyBtn.style.display = "block"
    elements.readyTxt.style.display = "none"
    elements.unreadyBtn.style.display = "none"

    elements.gameRoomDiv.style.display = "none"
    elements.chatRoomDiv.style.display = "none"
    elements.gameResultDiv.style.display = "none"
    elements.createRoomDiv.style.display = "block"
})