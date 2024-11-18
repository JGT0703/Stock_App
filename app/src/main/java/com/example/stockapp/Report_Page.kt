package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private lateinit var drawerLayout: DrawerLayout
private lateinit var navView: NavigationView
private lateinit var profileEmail: TextView
private lateinit var profileName: TextView
private lateinit var logoutTextView: TextView
private lateinit var viewOrdersButton: Button

class ReportPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_page)

        viewOrdersButton = findViewById(R.id.view_orders_button)
        viewOrdersButton.setOnClickListener {
            val intent = Intent(this, Order_View::class.java)
            startActivity(intent)
        }
        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val headerView = navView.getHeaderView(0)
        profileName = headerView.findViewById(R.id.profile_name)
        profileEmail = headerView.findViewById(R.id.profile_email)

        loadUserInfo()

        // Get the logout text view
        val logoutView = navView.findViewById<TextView>(R.id.logout)

        // Initialize ActionBarDrawerToggle
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        } else {
            Log.w("Home_View_Customer", "SupportActionBar is null")
        }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set navigation item selected listener
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_item1 -> {
                    val home = Intent(this, Home_View_Customer::class.java)
                    startActivity(home)
                }
                R.id.nav_item2 -> {
                    val product = Intent(this, Stock_view::class.java)
                    startActivity(product)
                }
                R.id.nav_item3 -> {
                    val cart = Intent(this, Users_View::class.java)
                    startActivity(cart)
                }
                R.id.nav_item4 -> {
                    val report = Intent(this, ReportPage::class.java)
                    startActivity(report)
                }
                // Handle other items here
                else -> {
                    Log.w("Home_View_Customer", "Unknown menu item ID: ${item.itemId}")
                }
            }
            true
        }

        // Set logout text view click listener
        logoutView.setOnClickListener {
            logout()
        }
    }

    private fun loadUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Get a reference to the Firebase Database
            val database = FirebaseDatabase.getInstance()
            val userId = user.uid

            // Reference to the user's information in the database
            val userRef = database.getReference("Users").child(userId).child("User Information")

            // Fetch user information from the database
            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataSnapshot = task.result
                    if (dataSnapshot.exists()) {
                        // Assuming the user information is stored as a Map
                        val name = dataSnapshot.child("username").getValue(String::class.java)
                        val email = dataSnapshot.child("email").getValue(String::class.java)

                        // Update the UI with the retrieved information
                        profileName.text = name ?: "No Name"
                        profileEmail.text = email ?: "No Email"
                    } else {
                        profileName.text = "No Name"
                        profileEmail.text = "No Email"
                    }
                } else {
                    Log.e("Home_View_Admin", "Error getting user info", task.exception)
                    profileName.text = "No Name"
                    profileEmail.text = "No Email"
                }
            }
        } else {
            profileName.text = "No Name"
            profileEmail.text = "No Email"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (drawerLayout != null && navView != null) {
                    drawerLayout.openDrawer(navView)
                    true
                } else {
                    Log.w("Home_View_Customer", "DrawerLayout or NavigationView is null")
                    false
                }
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