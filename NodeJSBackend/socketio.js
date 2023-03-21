const {Server} = require("socket.io");

const {checkImage} = require("./google_vision");

const mapList = require("./sample_maps/mapList.json")
const {shuffleArray, distance} = require("./utility");

let io
const maxPlayerNumberInRoom = 2
let rooms = {}

let error_radius = 0.5

const maps = mapList.map(mapName => {
    return require("./sample_maps/" + mapName)
})

function init(server) {
    console.log("server initializing")
    io = new Server(server)

    io.on('connection', (socket) => {
        console.log("connection initialized by id: ", socket.id)

        socket.on("echoTest", (message, callback) => {
            callback = checkCallback(callback, socket.id, "echoTest")
            console.log("echoTest received:", message)
            callback({
                "message": message
            })
            socket.emit("echoTest2", () => {
                console.log("echoTest2 returned")
            })
        })

        socket.on("getMaps", (callback) => {
            callback = checkCallback(callback, socket.id, "getMaps")
            console.log("getMaps called")
            callback({
                "status": "ok",
                "maps": maps
            })
        })

        socket.on("createRoom", (params, callback) => {
            callback = checkCallback(callback, socket.id, "createRoom")
            if (rooms[socket.id] !== undefined) {
                callback({
                    "status": "error",
                    "errorMessage": "room already existed"
                })
                return
            }
            console.log(socket.id + " created room")
            socket.data.roomID = socket.id
            rooms[socket.id] = getNewRoom(socket.id, params)
            callback({
                "status": "ok",
                "room": rooms[socket.id]
            })
        })

        socket.on("joinRoom", (params, callback) => {
            callback = checkCallback(callback, socket.id, "joinRoom")
            if (rooms[params.roomID] === null || rooms[params.roomID] === undefined) {
                callback({
                    "status": "error",
                    "errorMessage": "room not found"
                })
                return
            }
            if (Object.keys(rooms[params.roomID]["players"]).length >= maxPlayerNumberInRoom) {
                callback({
                    "status": "error",
                    "errorMessage": "room is full"
                })
                return
            }
            console.log("player", socket.id, "successfully joined room", params.roomID)
            socket.data.roomID = params.roomID
            socket.join(params.roomID)
            rooms[params.roomID]["players"][socket.id] = getNewPlayer(params.nickname, false)
            rooms[params.roomID]["chatHistory"].push({
                "body": params.nickname + " joined.",
                "timeStamp": Date.now(),
                "sender": "System"
            })
            rooms[params.roomID]["chatHistory"].push({
                "body": "items: " + rooms[params.roomID]["items"].map((item) => item["name"]).join(","),
                "timeStamp": Date.now(),
                "sender": "System"
            })
            io.to(socket.data.roomID).emit("roomFilled", rooms[params.roomID])
            callback({
                "status": "ok",
                "roomID": params.roomID
            })
        })

        socket.on("getOpponentInfo", (callback) => {
            callback = checkCallback(callback, socket.id, "getOpponentInfo")
            if (!verifyRoomRequest(socket, callback)) return
            console.log("getOpponentInfo called")
            for (let i in rooms[socket.data.roomID]["players"]) {
                if (i["id"] !== socket.id) {
                    callback({
                        "status": "ok",
                        "nickname": i["nickname"]
                    })
                }
            }
            callback({
                "status": "error",
                "errorMessage": "you're alone in this room"
            })
        })

        socket.on("sendChat", (message, callback) => {
            callback = checkCallback(callback, socket.id, "sendChat")
            if (!verifyRoomRequest(socket, callback)) return
            if (message.length < 1) {
                callback({
                    "status": "error",
                    "errorMessage": "message is empty"
                })
                return
            }
            let newMessage = {
                "body": message,
                "timeStamp": Date.now(),
                "sender": socket.id
            }
            rooms[socket.data.roomID]["chatHistory"].push(newMessage)
            console.log("player", socket.id, "sent chat message", message)
            callback({
                "status": "ok"
            })
            io.to(socket.data.roomID).emit("chatUpdated", newMessage)
        })

        socket.on("getChatHistory", (callback) => {
            callback = checkCallback(callback, socket.id, "getChatHistory")
            if (!verifyRoomRequest(socket, callback)) return
            console.log("getChatHistory called")
            callback({
                "status": "ok",
                "chatHistory": rooms[socket.data.roomID]["chatHistory"]
            })
        })
        socket.on("ready", (callback) => {
            callback = checkCallback(callback, socket.id, "ready")
            if (!verifyRoomRequest(socket, callback)) return
            rooms[socket.data.roomID]["players"][socket.id]["ready"] = true
            rooms[socket.data.roomID]["chatHistory"].push({
                "body": rooms[socket.data.roomID]["players"][socket.id]["nickname"] + " has readied.",
                "timeStamp": Date.now(),
                "sender": "System"
            })
            io.to(socket.data.roomID).emit("readied", socket.id)
            callback({
                "status": "ok"
            })
            if (Object.keys(rooms[socket.data.roomID]["players"]).length < 2)
                return
            for (let player in rooms[socket.data.roomID]["players"]) {
                if (!rooms[socket.data.roomID]["players"][player]["ready"])
                    return
            }
            console.log("player", socket.id, "readied")
            io.to(socket.data.roomID).emit("roomReadied")
            startGame(rooms[socket.data.roomID])
        })

        socket.on("unready", (callback) => {
            callback = checkCallback(callback, socket.id, "unready")
            if (!verifyRoomRequest(socket, callback)) return
            rooms[socket.data.roomID]["players"][socket.id]["ready"] = false
            rooms[socket.data.roomID]["chatHistory"].push({
                "body": rooms[socket.data.roomID]["players"][socket.id]["nickname"] + " has unreadied.",
                "timeStamp": Date.now(),
                "sender": "System"
            })
            console.log("player", socket.id, "unreadied")
            io.to(socket.data.roomID).emit("unreadied", socket.id)
            callback({
                "status": "ok"
            })
        })

        socket.on("submitAnswer", (file, coordinate, callback) => {
            callback = checkCallback(callback, socket.id, "submitAnswer")
            if (!verifyRoomRequest(socket, callback)) return
            console.log("player ", socket.id, " submitted an answer...")
            let currentRoom = rooms[socket.data.roomID]
            let currentPlayer = currentRoom["players"][socket.id]
            let currentItem = currentRoom["items"][currentRoom["itemIndex"]]
            const locationMatchCondition = distance(coordinate, currentItem) <= error_radius
            if(!locationMatchCondition){
                callback({
                    "status": "wrong_location"
                })
                return
            }
            checkImage(file).then(detectedTags => {
                const tagMatchCondition = detectedTags.find(tag => currentItem["name"] === tag) !== undefined
                if (tagMatchCondition) {
                    currentPlayer["pictures"].push(file)
                    currentPlayer["score"] += 1
                    currentRoom["itemIndex"] += 1
                    let item = currentRoom["items"][currentRoom["itemIndex"]]
                    callback({
                        "status": "ok"
                    })
                    if (item === undefined) {
                        currentPlayer["score"]++
                        currentRoom["gameEnded"] = true
                        let winner = Object.keys(currentRoom["players"]).reduce((winnerID, currentID) => {
                            let currentScore = currentRoom["players"][currentID]["score"]
                            let winnerScore = currentRoom["players"][winnerID]["score"]
                            if(currentScore > winnerScore){
                                return currentID
                            }
                            return winnerID
                        })
                        console.log("player", winner, "won!")
                        io.to(socket.data.roomID).emit("gameEnded", {
                            "winner": winner,
                            "room": currentRoom
                        })
                        return
                    }
                    io.to(socket.data.roomID).emit("newItem", item, currentRoom)
                    console.log("player ", socket.id, "'s answer was correct! The new item is ", item)
                }
                callback({
                    "status": "wrong_answer"
                })
            })
        })

        socket.on("disconnect", () => {
            socket.to(socket.data.roomID).emit("opponent disconnected")
            console.log("connection dropped by id: ", socket.id)
            delete rooms[socket.data.roomID]
        })
    })
}

async function startGame(room) {

    room["inGame"] = true
    shuffleArray(room["items"])
    let sockets = await io.in(room["roomID"]).fetchSockets()
    setTimeout(
        ()=>{
            sockets.forEach(socket => {
                let item = room["items"][room["itemIndex"]]
                socket.emit("itemGenerated", item)
            })
        }
        , 3000)
    setTimeout(()=> {
        if(room["gameEnded"])
            return
        const winner = Object.entries(room["players"]).reduce(([id1, player1], [id2, player2])=>{
            if(player1["score"] > player2["score"])
                return id1
            if(player1["score"] < player2["score"])
                return id2
            return "draw"
        }
    )
        io.to(room["roomID"]).emit("gameEnded", {
            "winner": winner,
            "room": room
        }
    )}, room["timeLimit"]*1000
    )
}

function checkCallback(callback, socketID, functionName) {
    if (typeof callback !== 'function') {
        console.log(socketID + " triggered " + functionName + " without providing a callback")
        return (_) => {
        }
    } else return callback
}

function verifyRoomRequest(socketParam, callback) {
    if (socketParam.data === undefined || socketParam.data.roomID === undefined) {
        callback({
            "status": "error",
            "errorMessage": "you're not in a room"
        })
        return false
    }
    if (rooms[socketParam.data.roomID] === null || rooms[socketParam.data.roomID] === undefined) {
        callback({
            "status": "error",
            "errorMessage": "room not found"
        })
        return false
    }
    if (!socketParam.id in rooms[socketParam.data.roomID]["players"]) {
        callback({
            "status": "error",
            "errorMessage": "you're not in the room"
        })
        return false
    }
    return true
}

function getNewPlayer(nickname, ready = false) {
    return {
        "nickname": nickname,
        "ready": ready,
        "score": 0,
        "pictures": []
    }
}

function getNewRoom(roomID, params) {
    return {
        "roomID": roomID,
        "players": {
            [roomID]: getNewPlayer(params.nickname)
        },
        "items": params.itemsList.map(item => {
            return {
                ...item,
                "name": item["name"].toLowerCase()
            }
        }),
        "timeLimit": params.timeLimit,
        "chatHistory": [{
            "body": params.nickname + " joined.",
            "timeStamp": Date.now(),
            "sender": "System"
        }],
        "itemIndex": 0,
        "inGame": false,
        "gameEnded": false
    }
}

module.exports = {
    init: init
}