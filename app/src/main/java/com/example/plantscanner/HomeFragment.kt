package com.example.plantscanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.plantscanner.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private lateinit var takePicturePreviewLauncher: ActivityResultLauncher<Void?>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize takePicturePreviewLauncher
        takePicturePreviewLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            // This is where you receive the result (bitmap)
            bitmap?.let {
                binding.clickImage.setImageBitmap(it)
            }
        }

        // Initialize requestPermissionLauncher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, you can launch the camera here
                takePicturePreviewLauncher.launch(null)
            } else {
                // Permission is denied. Handle the error.
                Toast.makeText(requireContext(), "Camera permission is required to take pictures", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        binding.settingsFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        binding.cameraFAB.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted; launch the camera
                    takePicturePreviewLauncher.launch(null)
                }
                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        return binding.root
    }
}