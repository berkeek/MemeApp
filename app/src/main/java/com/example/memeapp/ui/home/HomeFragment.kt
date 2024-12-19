package com.example.memeapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.memeapp.databinding.FragmentHomeBinding
import com.example.memeapp.App
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.memeapp.R
import java.io.ByteArrayOutputStream
import java.io.IOException

//the main part of the home page, includes
//upload open windows, the UI components
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Activity-Scoped ViewModel
    private lateinit var homeViewModel: HomeViewModel

    // Activity Result Launchers for permissions and image picking
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var memeImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, proceed to pick image
                pickImageFromGallery()
            } else {
                // Permission denied, show a message
                Toast.makeText(requireContext(), "Permission denied. Cannot open gallery.", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize the image picker launcher
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                // Handle the image URI
                handleImageUri(uri)
            } else {
                Toast.makeText(requireContext(), "No image selected.", Toast.LENGTH_SHORT).show()
            }
        }

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        memeImageView = root.findViewById(R.id.meme_image)
        return root
    }


    //here is the UI of home fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access views from binding now
        val voteSlider = binding.voteSlider
        val sliderText = binding.sliderValue

        // Set up a listener to update the selected vote dynamically
        voteSlider.addOnChangeListener { _, value, _ ->
            sliderText.text = "Selected vote: ${value.toInt()}"
        }

        val uploadButton = binding.buttonUpload
        val sendVoteButton = binding.buttonSendVote

        sendVoteButton.setOnClickListener {
            val vote = voteSlider.value.toInt()
            App.Repository.sendVote(vote)
        }

        uploadButton.setOnClickListener {
            uploadImage()
        }

        homeViewModel.memeImage.observe(viewLifecycleOwner) { bitmap ->
            setMemeImage(bitmap)
        }

        homeViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImage() {
        // Determine the permission to request based on Android version
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permissionToRequest
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                pickImageFromGallery()
            }
            shouldShowRequestPermissionRationale(permissionToRequest) -> {
                // Show an explanation to the user asynchronously
                Toast.makeText(requireContext(), "Permission needed to access images.", Toast.LENGTH_SHORT).show()
                // Optionally, show a dialog and then request permission
                requestPermissionLauncher.launch(permissionToRequest)
            }
            else -> {
                // Directly request the permission
                requestPermissionLauncher.launch(permissionToRequest)
            }
        }
    }

    private fun pickImageFromGallery() {
        // Launch the image picker
        pickImageLauncher.launch("image/*")
    }

    private fun handleImageUri(uri: Uri) {
        // Convert the image URI to a byte array and send it to the server
        try {
            // Get the bitmap from the URI
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

            // Convert the bitmap to a byte array (JPEG format)
            val byteArray = bitmapToByteArray(bitmap)

            // Send the byte array via TCP connection
            App.Repository.sendImage(byteArray)

            Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }

    private fun setMemeImage(bitmap: Bitmap) {
        memeImageView.setImageBitmap(bitmap)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}