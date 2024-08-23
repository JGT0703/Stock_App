package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.editTextText)
        passwordEditText = findViewById(R.id.editTextTextPassword)
        loginButton = findViewById(R.id.button_click)
        registerButton = findViewById(R.id.registerBtn)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateInput(email, password)) {
                val database = FirebaseDatabase.getInstance()
                val usersRef = database.getReference("Users")
                val context = this@MainActivity

                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var userFound = false

                        for (userSnapshot in dataSnapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)

                            if (user != null && user.email == email && user.password == password) {
                                userFound = true
                                val intent = Intent(context, Home_view::class.java)
                                context.startActivity(intent)
                                break
                            }
                        }

                        if (!userFound) {
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(context, "Error: $databaseError", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this@MainActivity, Register_view::class.java)
            startActivity(intent)
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
}

data class User(val email: String, val password: String)