package com.hasibzero.bakikhata

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hasibzero.bakikhata.databinding.ActivityMainBinding
import com.hasibzero.bakikhata.ui.security.LockScreenActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if security is enabled
        checkSecurityLock()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        val bottomNav: BottomNavigationView = binding.bottomNavigation
        
        // Map bottom nav items to navigation destinations
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.dashboardFragment)
                    true
                }
                R.id.navigation_customers -> {
                    navController.navigate(R.id.customerListFragment)
                    true
                }
                R.id.navigation_reports -> {
                    navController.navigate(R.id.reportFragment)
                    true
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }
        
        // Highlight the correct bottom nav item based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dashboardFragment -> bottomNav.menu.findItem(R.id.navigation_dashboard)?.isChecked = true
                R.id.customerListFragment -> bottomNav.menu.findItem(R.id.navigation_customers)?.isChecked = true
                R.id.reportFragment -> bottomNav.menu.findItem(R.id.navigation_reports)?.isChecked = true
                R.id.settingsFragment -> bottomNav.menu.findItem(R.id.navigation_settings)?.isChecked = true
            }
        }
    }
    
    private fun checkSecurityLock() {
        val sharedPrefs = getSharedPreferences("security_prefs", MODE_PRIVATE)
        val isPinEnabled = sharedPrefs.getBoolean("pin_enabled", false)
        
        if (isPinEnabled) {
            val intent = Intent(this, LockScreenActivity::class.java)
            startActivity(intent)
        }
    }
}
