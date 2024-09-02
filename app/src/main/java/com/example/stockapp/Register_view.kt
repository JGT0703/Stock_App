package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register_view : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var userTypeSpinner: Spinner
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_view)

        emailEditText = findViewById(R.id.Email_EditText)
        passwordEditText = findViewById(R.id.Password_EditText)
        confirmPasswordEditText = findViewById(R.id.Password2_EditText)
        registerButton = findViewById(R.id.registrationBtn)
        userTypeSpinner = findViewById(R.id.userTypeSpinner)

        val userTypeArray = arrayOf("Customer", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypeArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapter

        auth = FirebaseAuth.getInstance()

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val selectedUserType = userTypeSpinner.selectedItem.toString()

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.isEmpty() && !password.isEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // User created successfully
                            val user = auth.currentUser
                            val userId = user!!.uid

                            // Save user data to Firebase Realtime Database
                            val db = FirebaseDatabase.getInstance()
                            val usersRef = db.getReference("Users")
                            val userInfoRef = usersRef.child(userId).child("User Information")
                            userInfoRef.setValue(User(email, selectedUserType))

                            // Start Login Page activity
                            val intent = Intent(this@Register_view, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            // Handle error
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class User(val email: String, val userType: String)
}