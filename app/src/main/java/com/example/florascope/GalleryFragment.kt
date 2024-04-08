package com.example.florascope

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.florascope.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {
    private lateinit var binding: FragmentGalleryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(inflater)

        binding.settingsFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_galleryFragment_to_settingsFragment)
        }

        binding.cameraFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_galleryFragment_to_cameraFragment)
        }

        return binding.root
    }
}