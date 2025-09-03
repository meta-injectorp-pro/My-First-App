package com.metainjector.licensemanager

import androidx.appcompat.app.AppCompatDelegate
import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000 // 2 seconds

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied. You won't receive real-time updates.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        val btnHamburgerMenu = findViewById<ImageButton>(R.id.btn_hamburger_menu)
        btnHamburgerMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        navigationView.setNavigationItemSelectedListener(this)

        val logoutButton = navigationView.findViewById<Button>(R.id.btn_nav_logout)
        logoutButton.setOnClickListener { showLogoutDialog() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_dashboard)
        }

        askNotificationPermission()
    }

    override fun onStart() {
        super.onStart()
        // --- নতুন কোড: সার্ভিসটি চালু আছে কিনা তা পরীক্ষা করার জন্য ---
        checkAndStartService()
    }

    private fun checkAndStartService() {
        if (!isServiceRunning(FirestoreListenerService::class.java)) {
            val serviceIntent = Intent(this, FirestoreListenerService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    // Helper function to check if the service is already running
    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to logout and exit?")
            .setPositiveButton("Logout") { _, _ ->
                val serviceIntent = Intent(this, FirestoreListenerService::class.java)
                stopService(serviceIntent)

                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_dashboard -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, DashboardFragment()).commit()
            R.id.nav_licenses -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, LicenseListFragment()).commit()
            R.id.nav_key_generator -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, KeyGeneratorFragment()).commit()
            R.id.nav_form_responses -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FormResponsesFragment()).commit()
            R.id.nav_bug_reports_icon -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, BugReportsFragment()).commit()
            R.id.nav_security_logs_icon -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SecurityLogsFragment()).commit()
            R.id.nav_settings -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment()).commit()
        }
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (System.currentTimeMillis() - backPressedTime > BACK_PRESS_INTERVAL) {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            } else {
                super.onBackPressed()
            }
        }
    }
}