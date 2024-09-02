package com.example.stockapp

import Products
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class Home_View_Customer : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var profileEmail: TextView
    private lateinit var profileName: TextView
    private lateinit var logoutTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_view_customer)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Get the header view and the email text view
        val headerView = navView.getHeaderView(0)
        profileEmail = headerView.findViewById(R.id.profile_email)
        profileName = headerView.findViewById(R.id.profile_name)

        // Get the logout text view
        val logoutView = navView.findViewById<TextView>(R.id.logout)

        // Check if Firebase user is not null
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val userEmail = firebaseUser.email
            if (!userEmail.isNullOrEmpty()) {
                profileEmail.text = userEmail
            } else {
            }
        }

        // Initialize ActionBarDrawerToggle
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        } else {
        }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set navigation item selected listener
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_item1 -> {
                    val add = Intent(this, Products::class.java)
                    startActivity(add)
                }
                R.id.nav_item2 -> {
                    val cart = Intent(this, Cart_view::class.java)
                    startActivity(cart)
                }
                // Handle other items here
            }
            true
        }

        // Set logout text view click listener
        logoutView.setOnClickListener {
            logout()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(navView)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}