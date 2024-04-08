package com.example.florascope

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.florascope.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        binding.settingsFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        binding.cameraFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homeFragment_to_cameraFragment)
        }

        return binding.root
    }
}