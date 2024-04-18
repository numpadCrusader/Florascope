package com.example.florascope

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.florascope.databinding.FragmentLanguageSelectionBinding
import java.util.Locale

class LanguageSelectionFragment : Fragment() {
    private lateinit var binding: FragmentLanguageSelectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLanguageSelectionBinding.inflate(inflater)

        loadLang()

        binding.languageEn.setOnClickListener {
            binding.checkRu.visibility = View.GONE
            binding.checkEng.visibility = View.VISIBLE
        }

        binding.languageRu.setOnClickListener {
            binding.checkRu.visibility = View.VISIBLE
            binding.checkEng.visibility = View.GONE
        }

        return binding.root
    }

    private fun loadLang() {
        if (isAppLanguageEnglish()) {
            binding.checkRu.visibility = View.GONE
            binding.checkEng.visibility = View.VISIBLE
        } else {
            binding.checkRu.visibility = View.VISIBLE
            binding.checkEng.visibility = View.GONE
        }
    }

    private fun isAppLanguageEnglish(): Boolean {
        // Get the current locale of the app
        val currentLocale = Locale.getDefault()

        // Check if the language of the app is English
        return currentLocale.language == "en"
    }
}