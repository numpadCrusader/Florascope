package com.example.florascope

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.florascope.databinding.ActivityMainBinding

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

        // Add a destination changed listener to update visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavVisibility(destination)
            updateActionBarTitle(destination)
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)

        // Apply theme based on saved preference
        applyTheme()
    }

    private fun updateBottomNavVisibility(destination: NavDestination) {
        // Identify fragments where you want to hide the BottomNavigationView
        val fragmentsWithHiddenBottomNav = listOf(
            R.id.settingsFragment,
            R.id.cameraFragment,
            R.id.feedbackFragment
        )

        // Check if the current destination should hide the BottomNavigationView
        val shouldHideBottomNav = fragmentsWithHiddenBottomNav.contains(destination.id)

        // Update visibility accordingly
        binding.bottomNavigationView.visibility = if (shouldHideBottomNav) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun updateActionBarTitle(destination: NavDestination) {
        val actionBar = supportActionBar

        when (destination.id) {
            R.id.settingsFragment -> {
                actionBar?.title = getString(R.string.settings_screen_title)
                actionBar?.setDisplayHomeAsUpEnabled(true)
            }

            R.id.cameraFragment -> {
                actionBar?.title = getString(R.string.camera_screen_title)
                actionBar?.setDisplayHomeAsUpEnabled(true)
            }

            R.id.homeFragment -> {
                actionBar?.title = getString(R.string.home_screen_title)
                actionBar?.setDisplayHomeAsUpEnabled(false)
            }

            R.id.galleryFragment -> {
                actionBar?.title = getString(R.string.gallery_screen_title)
                actionBar?.setDisplayHomeAsUpEnabled(false)
            }

            R.id.feedbackFragment -> {
                actionBar?.title = getString(R.string.send_feedback)
                actionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun applyTheme() {
        val nightMode = sharedPreferences.getBoolean("night", false)
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}