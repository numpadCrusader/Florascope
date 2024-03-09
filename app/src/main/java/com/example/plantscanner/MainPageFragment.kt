package com.example.plantscanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.plantscanner.databinding.FragmentMainPageBinding

class MainPageFragment : Fragment() {
    private lateinit var binding: FragmentMainPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainPageBinding.inflate(inflater)

        binding.settingsFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_mainPageFragment_to_settingsFragment)
        }

        binding.cameraFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_mainPageFragment_to_cameraFragment)
        }

        return binding.root
    }
}