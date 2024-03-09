package com.example.plantscanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.plantscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = Navigation.findNavController(this, R.id.myNavHostFragment)

        setupWithNavController(binding.bottomNavigationView, navController)

        // Add a destination changed listener to update visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavVisibility(destination)
        }
    }

    private fun updateBottomNavVisibility(destination: NavDestination) {
        // Identify fragments where you want to hide the BottomNavigationView
        val fragmentsWithHiddenBottomNav = listOf(
            R.id.settingsFragment
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
}