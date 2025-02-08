# ObjectHunt

A real-time multiplayer game where players race to find and photograph objects in specific locations. Built with Android, Node.js, and Google's ML technologies.

[Gameplay Demo](https://web.engr.oregonstate.edu/~hessro/teaching/hof/cs492#w23)

![Camera UI](https://web.engr.oregonstate.edu/~hessro/static/media/objectHunt.3031aa87c6ef9eba0d8d.jpg)
---

## Features 🎮
- **2-Player Real-Time Competition** using Socket.IO
- **Object Detection** with Android ML Kit (client) + Google Vision API (server)
- **Location-Based Challenges**
- **Live Score Tracking**
- **Custom Game Rooms** with Join Codes

## Setup 🛠️

### Server (Node.js)
1. Clone repo:
   ```bash
   git clone https://github.com/bazarkua/final-project-objecthunt.git
   cd final-project-objecthunt/server
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Configure `.env`:
   ```env
   GOOGLE_APPLICATION_CREDENTIALS="path/to/service-account.json"
   PORT=3000
   ```
4. Start server:
   ```bash
   npm start
   ```

### Client (Android)
1. Open `client/android-project` in Android Studio
2. Add `google-services.json` to `app/` directory
3. Build and run on device/emulator

## Gameplay 🎯
```java
// Example game flow:
1. Player A creates room → gets code "ABC123"
2. Player B joins with code "ABC123"
3. System generates challenge: "Find a [laptop] in the [library]"
4. First player to snap a valid photo wins the round!
5. Game continues until all objects are found
```

## Tech Stack 💻
| Component       | Technologies                          |
|-----------------|---------------------------------------|
| **Frontend**    | Android (Kotlin), ML Kit, CameraX     |
| **Backend**     | Node.js, Socket.IO, Google Vision API |
| **Database**    | Firebase Realtime Database            |
| **Auth**        | Google Sign-In                        |
