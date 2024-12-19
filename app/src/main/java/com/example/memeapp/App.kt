package com.example.memeapp

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.IOException
import java.net.Socket

data class LeaderboardEntry(
    val bitmap: Bitmap,
    val averageVotes: Double
)
//message types
//1 : client to server vote
//2 : client to server image upload
//3 : server to client image broadcast
//4 : server to client leaderboard broadcast


class App : Application() {

    // Singleton socket or repository object
    object Repository {
        private var socket: Socket? = null
        private const val serverIp = "13.60.78.252"
        private const val serverPort = 9922

        //dynamic listeners
        private var memeListener: ((Bitmap) -> Unit)? = null
        private var leaderboardListener: ((List<LeaderboardEntry>) -> Unit)? = null
        private var isListening = false

        fun connectToServer() {
            // Make sure not to run network calls on main thread
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (socket == null || socket!!.isClosed) {
                        socket = Socket(serverIp, serverPort)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun sendVote(vote: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    socket?.let {
                        val output = it.getOutputStream()
                        val message = java.io.ByteArrayOutputStream()
                        val dataOut = java.io.DataOutputStream(message)
                        dataOut.writeByte(1)     // message type
                        dataOut.writeInt(vote)
                        output.write(message.toByteArray())
                        output.flush()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun sendImage(imageBytes: ByteArray) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    socket?.let {
                        val output = it.getOutputStream()
                        val message = java.io.ByteArrayOutputStream()
                        val dataOut = java.io.DataOutputStream(message)

                        dataOut.writeByte(2)        // message type
                        dataOut.writeInt(imageBytes.size)
                        dataOut.write(imageBytes)
                        output.write(message.toByteArray())
                        output.flush()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        fun setMemeListener(listener: (Bitmap) -> Unit) {
            memeListener = listener
            startListeningIfNeeded()
        }

        fun setLeaderboardListener(listener: (List<LeaderboardEntry>) -> Unit) {
            leaderboardListener = listener
            startListeningIfNeeded()
        }

        private fun startListeningIfNeeded() {
            if (!isListening) {
                isListening = true
                listenForData()
            }
        }

        //listens for data and parses it on a different thread than UI
        //3 meaning image received, 4 meaning leaderboard received
        private fun listenForData() {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val input = socket?.getInputStream() ?: return@launch
                    val dataInput = DataInputStream(input)
                    println("Started listening for server messages...")

                    while (socket != null && !socket!!.isClosed) {
                        val messageType = try {
                            dataInput.readByte().toInt()
                        } catch (e: IOException) {
                            // Socket might be closed or connection lost
                            e.printStackTrace()
                            break
                        }

                        when (messageType) {
                            3 -> { // Meme
                                val imageSize = dataInput.readInt()
                                val imageBytes = ByteArray(imageSize)
                                dataInput.readFully(imageBytes)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                println("Received a new meme of size: $imageSize bytes")
                                memeListener?.invoke(bitmap)
                            }

                            4 -> { // Leaderboard
                                val entryCount = dataInput.readInt()
                                val entries = mutableListOf<LeaderboardEntry>()
                                for (i in 0 until entryCount) {
                                    val imageSize = dataInput.readInt()
                                    val imageBytes = ByteArray(imageSize)
                                    dataInput.readFully(imageBytes)
                                    val averageVotes = dataInput.readDouble()

                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    entries.add(LeaderboardEntry(bitmap, averageVotes))
                                }
                                println("Received leaderboard with $entryCount entries")
                                leaderboardListener?.invoke(entries)
                            }

                            else -> {
                                println("Received unknown message type: $messageType")
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun closeConnection() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    //connect to server at start
    override fun onCreate() {
        super.onCreate()
        // Connect to the server when the app starts
        Repository.connectToServer()
    }
    //close connection at end
    override fun onTerminate() {
        super.onTerminate()
        Repository.closeConnection()
    }
}
