const { Server } = require("socket.io");

let io
const maxPlayerNumberInRoom = 2
let rooms = {}
function init(server) {
    console.log("server initializing")
    io = new Server(server)

    io.on('connection', (socket) => {
        console.log("connection initialized by id: ", socket.id)

        socket.on("createRoom", (params, callback)=>{
            callback = checkCallback(callback, socket.id,  "createRoom")
            console.log(socket.id + " created room")
            socket.data.roomID = socket.id
            rooms[socket.id] = {
                "players": {
                    [socket.id]: {"nickname":params.nickname}
                },
                "items": params.itemsList,
                "timeLimit": params.timeLimit,
                "chatHistory":[{
                    "body": params.nickname + " joined.",
                    "timeStamp": Date.now(),
                    "sender": "System"
                }]
            }
            callback({
                "status": "ok"
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
            if(rooms[params.roomID]["players"].length >= maxPlayerNumberInRoom){
                callback({
                    "status": "error",
                    "errorMessage": "room is full"
                })
                return
            }
            socket.data.params.roomID = params.roomID
            socket.join(params.roomID)
            rooms[params.roomID]["players"][socket.id] = {"nickname": params.nickname}
            rooms[params.roomID]["chatHistory"].push({
                "body": params.nickname + " joined.",
                "timeStamp": Date.now(),
                "sender": "System"
            })
            io.to(socket.data.roomID).emit("roomFilled", rooms[params.roomID])
            callback({
                "status": "ok",
                "params.roomID": params.roomID
            })
        })

        socket.on("getOpponentInfo", (roomID, callback)=>{
            callback = checkCallback(callback, socket.id,  "getOpponentInfo")
            if(!verifyRoomRequest(socket.id, roomID, callback)) return
            for (let i in rooms[roomID]["players"]){
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

        socket.on("sendChat", (message, roomID, callback)=>{
            callback = checkCallback(callback, socket.id,  "sendChat")
            if(!verifyRoomRequest(socket.id, roomID, callback)) return
            let newMessage = {
                "body": message,
                "timeStamp": Date.now(),
                "sender": socket.id
            }
            rooms[roomID]["chatHistory"].push(newMessage)
            callback({
                "status": "ok"
            })
            io.to(roomID).emit("chatUpdated", newMessage)
        })

        socket.on("getChatHistory", (roomID, callback)=>{
            callback = checkCallback(callback, socket.id,  "getChatHistory")
            if(!verifyRoomRequest(socket.id, roomID, callback)) return
            callback({
                "status": "ok",
                "chatHistory": rooms[roomID]["chatHistory"]
            })
        })
    })
}

function checkCallback(callback, socketID, functionName){
    if(typeof callback !== 'function') {
        console.log(socketID + " triggered " + functionName + " without providing a callback")
        return (_) => {
        }
    }
    else return callback
}

function verifyRoomRequest(socketID, roomID, callback){
    if(rooms[roomID] === null || rooms[roomID] === undefined){
        callback({
            "status": "error",
            "errorMessage": "room not found"
        })
        return false
    }
    if(!socketID !== roomID){
        callback({
            "status": "error",
            "errorMessage": "you're not in this room"
        })
        return false
    }
    return true
}

module.exports = {
    init: init
}