const { Server } = require("socket.io");

const fs = require("fs")
const {quickstart} = require("./gameLogic");

let io
const maxPlayerNumberInRoom = 2
let rooms = {}
function init(server) {
    console.log("server initializing")
    io = new Server(server)

    io.on('connection', (socket) => {
        console.log("connection initialized by id: ", socket.id)

        socket.on("echoTest", (message, callback) => {
            console.log(message)
            callback({
                "message": message
            })
            socket.emit("echoTest2", ()=>{
                console.log("returned from Android")
            })
        })

        socket.on("createRoom", (params, callback)=>{
            callback = checkCallback(callback, socket.id,  "createRoom")
            if(rooms[socket.id] !== undefined){
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

        socket.on("joinRoom", (params, callback)=>{
            callback = checkCallback(callback, socket.id,  "joinRoom")
            if(rooms[params.roomID] === null || rooms[params.roomID] === undefined){
                callback({
                    "status": "error",
                    "errorMessage": "room not found"
                })
                return
            }
            if(Object.keys(rooms[params.roomID]["players"]).length >= maxPlayerNumberInRoom){
                callback({
                    "status": "error",
                    "errorMessage": "room is full"
                })
                return
            }
            socket.data.roomID = params.roomID
            socket.join(params.roomID)
            rooms[params.roomID]["players"][socket.id] = getNewPlayer(params.nickname, false)
            rooms[params.roomID]["chatHistory"].push({
                "body": params.nickname + " joined.",
                "timeStamp": Date.now(),
                "sender": "System"
            })
            io.to(socket.data.roomID).emit("roomFilled", rooms[params.roomID])
            callback({
                "status": "ok",
                "roomID": params.roomID
            })
        })

        socket.on("getOpponentInfo", (callback)=>{
            callback = checkCallback(callback, socket.id,  "getOpponentInfo")
            if(!verifyRoomRequest(socket, callback)) return
            for (let i in rooms[socket.data.roomID]["players"]){
                if(i["id"] !== socket.id){
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

        socket.on("sendChat", (message, callback)=>{
            callback = checkCallback(callback, socket.id,  "sendChat")
            if(!verifyRoomRequest(socket, callback)) return
            let newMessage = {
                "body": message,
                "timeStamp": Date.now(),
                "sender": socket.id
            }
            rooms[socket.data.roomID]["chatHistory"].push(newMessage)
            callback({
                "status": "ok"
            })
            io.to(socket.data.roomID).emit("chatUpdated", newMessage)
        })

        socket.on("getChatHistory", (callback)=>{
            callback = checkCallback(callback, socket.id,  "getChatHistory")
            if(!verifyRoomRequest(socket, callback)) return
            callback({
                "status": "ok",
                "chatHistory": rooms[socket.data.roomID]["chatHistory"]
            })
        })
        socket.on("ready", (callback)=>{
            callback = checkCallback(callback, socket.id,  "ready")
            if(!verifyRoomRequest(socket, callback)) return
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
            if(Object.keys(rooms[socket.data.roomID]["players"]).length < 2)
                return
            for(let player in rooms[socket.data.roomID]["players"]){
                if(!rooms[socket.data.roomID]["players"][player]["ready"])
                    return
            }
            io.to(socket.data.roomID).emit("roomReadied")
            startGame(rooms[socket.data.roomID])
        })

        socket.on("unready", (callback)=>{
            callback = checkCallback(callback, socket.id,  "unready")
            if(!verifyRoomRequest(socket, callback)) return
            rooms[socket.data.roomID]["players"][socket.id]["ready"] = false
            rooms[socket.data.roomID]["chatHistory"].push({
                "body": rooms[socket.data.roomID]["players"][socket.id]["nickname"] + " has unreadied.",
                "timeStamp": Date.now(),
                "sender": "System"
            })
            io.to(socket.data.roomID).emit("unreadied", socket.id)
            callback({
                "status": "ok"
            })
        })

        socket.on("submitAnswer", (item, file, callback) => {
            callback = checkCallback(callback, socket.id,  "submitAnswer")
            if(!verifyRoomRequest(socket, callback)) return
            console.log("player ", socket.id, " submitted an answer...")
            let currentRoom = rooms[socket.data.roomID]
            let currentPlayer = currentRoom["players"][socket.id]
            quickstart(file).then(detectedTags => {
                let playersCurrentItem = currentRoom["items"][currentPlayer["itemIndex"]]
                if(detectedTags.indexOf(playersCurrentItem) > -1){
                    currentPlayer["itemIndex"]+=1
                    let item = currentRoom["items"][currentPlayer["itemIndex"]]
                    console.log("player ", socket.id, "'s answer was correct! Their new item is ", item)
                    callback({
                        "status": "ok",
                        "newItem": item
                    })
                }
            })
        })

        socket.on("disconnect", ()=>{
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
    sockets.forEach(socket=>{
        let item = room["items"][room["players"][socket.id]["itemIndex"]]
        socket.emit("itemGenerated", item)
    })
}

function getRandom(array){
    return array[Math.floor(Math.random()*array.length)]
}

function shuffleArray(array){
        let m = array.length, t, i;

        // While there remain elements to shuffle…
        while (m) {

            // Pick a remaining element…
            i = Math.floor(Math.random() * m--);

            // And swap it with the current element.
            t = array[m];
            array[m] = array[i];
            array[i] = t;
        }

        return array;
}

function checkCallback(callback, socketID, functionName){
    if(typeof callback !== 'function') {
        console.log(socketID + " triggered " + functionName + " without providing a callback")
        return (_) => {
        }
    }
    else return callback
}

function verifyRoomRequest(socketParam, callback){
    if(socketParam.data === undefined || socketParam.data.roomID === undefined){
        callback({
            "status": "error",
            "errorMessage": "you're not in a room"
        })
        return false
    }
    if(rooms[socketParam.data.roomID] === null || rooms[socketParam.data.roomID] === undefined){
        callback({
            "status": "error",
            "errorMessage": "room not found"
        })
        return false
    }
    if(!socketParam.id in rooms[socketParam.data.roomID]["players"]){
        callback({
            "status": "error",
            "errorMessage": "you're not in the room"
        })
        return false
    }
    return true
}

function getNewPlayer(nickname, ready = false, score = 0){
    return {
        "nickname": nickname,
        "ready": ready,
        "score": score,
        "itemIndex": 0,
        "pictures": []
    }
}

function getNewRoom(roomID, params){
    return {
        "roomID": roomID,
        "players": {
            [roomID]: getNewPlayer(params.nickname)
        },
        "items": params.itemsList.map(item=>item.toLowerCase()),
        "timeLimit": params.timeLimit,
        "chatHistory":[{
            "body": params.nickname + " joined.",
            "timeStamp": Date.now(),
            "sender": "System"
        }],
        "inGame": false
    }
}

module.exports = {
    init: init
}