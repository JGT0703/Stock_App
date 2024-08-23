package com.example.stockapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Add_Item_View : AppCompatActivity() {

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_view)

        val backClick = findViewById<Button>(R.id.Backbtn)
        backClick.setOnClickListener {
            val back = Intent(this, Stock_view::class.java)
            startActivity(back)
        }

        val addClick = findViewById<Button>(R.id.AddItembtn)
        addClick.setOnClickListener {
            val partNameEditText = findViewById<EditText>(R.id.partnameTextView)
            val brandNameEditText = findViewById<EditText>(R.id.BrandnameText)
            val amountEditText = findViewById<EditText>(R.id.amountTextView)
            val priceEditText = findViewById<EditText>(R.id.priceTextView)

            val partName = partNameEditText.text.toString()
            val brandName = brandNameEditText.text.toString()
            val amount = amountEditText.text.toString()
            val price = priceEditText.text.toString()

            if (!partName.isEmpty() && !brandName.isEmpty() && !amount.isEmpty() && !price.isEmpty()) {
                val db = FirebaseDatabase.getInstance()
                val stockdetails = db.getReference("Stock")

                stockdetails.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val parentId = "Item " + (dataSnapshot.childrenCount + 1).toString()
                        stockdetails.child(parentId).child("Part Name").setValue(partName)
                        stockdetails.child(parentId).child("Brand Name").setValue(brandName)
                        stockdetails.child(parentId).child("Amount").setValue(amount)
                        stockdetails.child(parentId).child("Price").setValue(price)

                        partNameEditText.setText("")
                        brandNameEditText.setText("")
                        amountEditText.setText("")
                        priceEditText.setText("")

                        val add = Intent(this@Add_Item_View, Stock_view::class.java)
                        startActivity(add)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
            }
        }
    }
}