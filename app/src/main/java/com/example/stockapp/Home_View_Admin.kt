package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Home_View_Admin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_view_admin)

        val stockClick = findViewById<Button>(R.id.Stock)
        stockClick.setOnClickListener{
            val stock = Intent(this, Stock_view::class.java)
            startActivity(stock)
        }
        val ordersClick = findViewById<Button>(R.id.Order)
        ordersClick.setOnClickListener{
            val order = Intent(this, Order_View::class.java)
            startActivity(order)
        }
        val userClick = findViewById<Button>(R.id.Users)
        userClick.setOnClickListener{
            val user = Intent(this, Users_View::class.java)
            startActivity(user)
        }
    }
}