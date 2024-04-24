package com.example.florascope.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.florascope.R
import com.example.florascope.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = Navigation.findNavController(this, R.id.myNavHostFragment)
        setupWithNavController(binding.bottomNavigationView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavVisibility(destination)
            updateActionBarTitle(destination)
        }

        sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        applyTheme()
    }

    private fun updateBottomNavVisibility(destination: NavDestination) {
        val fragmentsWithHiddenBottomNav = listOf(
            R.id.cameraFragment,
            R.id.feedbackFragment,
            R.id.diseaseFragment
        )
        val shouldHideBottomNav = fragmentsWithHiddenBottomNav.contains(destination.id)
        binding.bottomNavigationView.visibility = if (shouldHideBottomNav) View.GONE else View.VISIBLE
    }

    private fun updateActionBarTitle(destination: NavDestination) {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(destination.id != R.id.homeFragment)
        when (destination.id) {
            R.id.settingsFragment -> actionBar?.title = getString(R.string.settings_screen_title)
            R.id.cameraFragment -> actionBar?.title = getString(R.string.camera_screen_title)
            R.id.homeFragment -> actionBar?.title = getString(R.string.home_screen_title)
            R.id.feedbackFragment -> actionBar?.title = getString(R.string.send_feedback)
            R.id.diseaseFragment -> actionBar?.title = getString(R.string.disease_screen_title)
            R.id.languageSelectionFragment -> actionBar?.title = getString(R.string.language)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applyTheme() {
        val nightMode = sharedPreferences.getBoolean("night", false)
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val lang = getPersistedData(context) // Retrieve your saved language
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun getPersistedData(context: Context): String {
        val preferences = context.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        return preferences.getString("AppLang", Locale.getDefault().language) ?: Locale.getDefault().language
    }
}