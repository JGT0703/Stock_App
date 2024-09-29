package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var logoImageView: ImageView
    private lateinit var title_TextView: TextView
    private lateinit var switch1: Switch
    private lateinit var userTypeSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.editTextText)
        passwordEditText = findViewById(R.id.editTextTextPassword)


        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        logoImageView = findViewById(R.id.imageView2)
        title_TextView = findViewById(R.id.title_TextView)
        switch1 = findViewById(R.id.switch1)
        userTypeSpinner = findViewById(R.id.userTypeSpinner)

        userTypeSpinner.visibility = View.GONE

        switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                title_TextView.animate().alpha(0f).setDuration(0).withEndAction {
                    title_TextView.text = "Sign Up"
                    title_TextView.animate().alpha(1f).setDuration(500).startDelay = 200
                    userTypeSpinner.visibility = View.VISIBLE
                    userTypeSpinner.animate().alpha(1f).setDuration(500).startDelay = 200
                }
            } else {
                title_TextView.animate().alpha(0f).setDuration(0).withEndAction {
                    title_TextView.text = "Sign In"
                    title_TextView.animate().alpha(1f).setDuration(500).startDelay = 200
                    userTypeSpinner.visibility = View.GONE
                    userTypeSpinner.animate().alpha(1f).setDuration(500).startDelay = 200

                }
            }
        }
        val userTypeArray = arrayOf("Customer", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypeArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapter

        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (switch1.isChecked) {
                // Register mode
                if (validateInput(email, password)) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val uid = mAuth.currentUser!!.uid
                                val userType = userTypeSpinner.selectedItem.toString()
                                mDatabase.child("Users").child(uid).child("User Information").child("userType").setValue(userType)
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                getUserType(uid, email, password)
                            } else {
                                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                // Login mode
                if (validateInput(email, password)) {
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val uid = mAuth.currentUser!!.uid
                                getUserType(uid, email, password)
                            } else {
                                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

    }


    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun getUserType(uid: String, email: String, password: String) {
        mDatabase.child("Users").child(uid).child("User Information").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userTypeObject = dataSnapshot.child("userType")
                    if (userTypeObject.exists()) {
                        val userType = userTypeObject.value as String
                        if (userType != null) {
                            if (userType == "Admin") {
                                val intent = Intent(this@MainActivity, Home_View_Admin::class.java)
                                startActivity(intent)
                            } else if (userType == "Customer") {
                                val intent = Intent(this@MainActivity, Home_View_Customer::class.java)
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "User type not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "User type not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error getting user type", Toast.LENGTH_SHORT).show()
            }
        })
    }

}