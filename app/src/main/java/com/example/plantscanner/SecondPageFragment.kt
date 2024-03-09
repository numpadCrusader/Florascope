package com.example.plantscanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.plantscanner.databinding.FragmentMainPageBinding
import com.example.plantscanner.databinding.FragmentSecondPageBinding

class SecondPageFragment : Fragment() {
    private lateinit var binding: FragmentSecondPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecondPageBinding.inflate(inflater)

        binding.settingsFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_secondPageFragment_to_settingsFragment)
        }

        binding.cameraFAB.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_secondPageFragment_to_cameraFragment)
        }

        return binding.root
    }
}