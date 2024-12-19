package com.example.memeapp.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memeapp.App
import com.example.memeapp.LeaderboardEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//start listeners here, even though the leaderboard is in leaderboard fragment
// because otherwise leaderboard is not fetched until that page is opened

class HomeViewModel : ViewModel() {

    private val _memeImage = MutableLiveData<Bitmap>()
    val memeImage: LiveData<Bitmap> = _memeImage

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _leaderboardData = MutableLiveData<List<LeaderboardEntry>>()
    val leaderboardData: LiveData<List<LeaderboardEntry>> = _leaderboardData

    init {
        // Set the meme listener to receive memes from the Repository
       App.Repository.setMemeListener { bitmap ->
            // Update LiveData on the main thread
            CoroutineScope(Dispatchers.Main).launch {
                _memeImage.value = bitmap
            }
        }

        // Set the leaderboard listener
        App.Repository.setLeaderboardListener { entries ->
            // Switch to the main thread to update UI.
            CoroutineScope(Dispatchers.Main).launch {
                _leaderboardData.value = entries
            }
        }
    }
}