package com.example.florascope.fragments

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
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
            updateLanguage("en")
        }

        binding.languageRu.setOnClickListener {
            updateLanguage("ru")
        }

        return binding.root
    }

    private fun loadLang() {
        when (getPersistedLanguage()) {
            "en" -> {
                binding.checkRu.visibility = View.GONE
                binding.checkEng.visibility = View.VISIBLE
            }
            "ru" -> {
                binding.checkRu.visibility = View.VISIBLE
                binding.checkEng.visibility = View.GONE
            }
        }
    }

    private fun updateLanguage(lang: String) {
        setLocale(lang)
        requireActivity().recreate()
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        val context = requireContext().createConfigurationContext(config)
        context.resources

        saveLanguageSetting(language)
    }

    private fun saveLanguageSetting(lang: String) {
        val sharedPref = activity?.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        sharedPref?.edit()?.putString("AppLang", lang)?.apply()
    }

    private fun getPersistedLanguage(): String {
        val sharedPref = activity?.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("AppLang", Locale.getDefault().language) ?: Locale.getDefault().language
    }
}