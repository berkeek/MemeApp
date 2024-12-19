package com.example.memeapp.ui.leaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memeapp.LeaderboardEntry

//not used
class LeaderboardViewModel : ViewModel() {
    private val _leaderboardData = MutableLiveData<List<LeaderboardEntry>>()
    val leaderboardData: LiveData<List<LeaderboardEntry>> = _leaderboardData

    fun updateLeaderboard(entries: List<LeaderboardEntry>) {
        _leaderboardData.value = entries
    }
}