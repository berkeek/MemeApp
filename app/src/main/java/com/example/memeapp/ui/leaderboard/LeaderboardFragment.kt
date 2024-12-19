package com.example.memeapp.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.memeapp.LeaderboardEntry
import com.example.memeapp.databinding.FragmentLeaderboardBinding
import com.example.memeapp.ui.home.HomeViewModel

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        homeViewModel.leaderboardData.observe(viewLifecycleOwner) { entries ->
            updateLeaderboardUI(entries)
        }
    }

    private fun updateLeaderboardUI(entries: List<LeaderboardEntry>) {
        // Ensure we only handle up to 3 entries since the layout shows top 3
        val top3 = entries.take(3)

        // 1st place
        if (top3.size >= 1) {
            val first = top3[0]
            binding.firstPlaceVotes.text = "Votes: ${first.averageVotes}"
            if (first.bitmap != null) {
                binding.firstPlaceImage.setImageBitmap(first.bitmap)
            } else {
                binding.firstPlaceImage.setImageResource(com.example.memeapp.R.drawable.rounded_corner)
            }
        } else {
            binding.firstPlaceVotes.text = "Votes: 0"
            binding.firstPlaceImage.setImageResource(com.example.memeapp.R.drawable.rounded_corner)
        }

        // 2nd place
        if (top3.size >= 2) {
            val second = top3[1]
            binding.secondPlaceVotes.text = "Votes: ${second.averageVotes}"
            if (second.bitmap != null) {
                binding.secondPlaceImage.setImageBitmap(second.bitmap)
            } else {
                binding.secondPlaceImage.setImageResource(com.example.memeapp.R.drawable.rounded_corner)
            }
        } else {
            binding.secondPlaceVotes.text = "Votes: 0"
            binding.secondPlaceImage.setImageResource(com.example.memeapp.R.drawable.rounded_corner)
        }

        // 3rd place
        if (top3.size >= 3) {
            val third = top3[2]
            binding.thirdPlaceVotes.text = "Votes: ${third.averageVotes}"
            if (third.bitmap != null) {
                binding.thirdPlaceImage.setImageBitmap(third.bitmap)
            } else {
                binding.thirdPlaceImage.setImageResource(com.example.memeapp.R.drawable.rounded_corner)
            }
        } else {
            binding.thirdPlaceVotes.text = "Votes: 0"
            binding.thirdPlaceImage.setImageResource(com.example.memeapp.R.drawable.rounded_corner)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}