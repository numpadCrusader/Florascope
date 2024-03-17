package com.example.plantscanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.plantscanner.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)

        binding.languageBlock.setOnClickListener {
            Toast.makeText(context, "Change Language clicked!", Toast.LENGTH_SHORT).show()
        }

        binding.feedbackBlock.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_settingsFragment_to_feedbackFragment)
        }

        return binding.root
    }
}