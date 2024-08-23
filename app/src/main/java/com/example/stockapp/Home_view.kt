package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Home_view : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_view)

        val logoutClick = findViewById<Button>(R.id.button)
        logoutClick.setOnClickListener() {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        val stockClick = findViewById<Button>(R.id.stock_button)
        stockClick.setOnClickListener(){
            val intent=Intent(this,Stock_view::class.java)
            startActivity(intent)
        }
        val carClick = findViewById<Button>(R.id.cart_button)
        carClick.setOnClickListener(){
            val intent = Intent(this, Cart_view::class.java)
            startActivity(intent)
        }
    }
}