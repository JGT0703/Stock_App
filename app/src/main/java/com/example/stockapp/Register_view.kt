package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class Register_view : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_view)

        emailEditText = findViewById(R.id.Email_EditText)
        passwordEditText = findViewById(R.id.Password_EditText)
        confirmPasswordEditText = findViewById(R.id.Password2_EditText)
        registerButton = findViewById(R.id.registrationBtn)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password == confirmPassword) {
                val database = FirebaseDatabase.getInstance()
                val usersRef = database.getReference("Users")

                val user = User(email, password)
                usersRef.child(email).setValue(user)

                val intent = Intent(this@Register_view, Home_view::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class User(val email: String, val password: String)
}