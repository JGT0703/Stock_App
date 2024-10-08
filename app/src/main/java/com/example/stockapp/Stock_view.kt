package com.example.stockapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Stock_view : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var stockList: ArrayList<StockItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_view)

        listView = findViewById<ListView>(R.id.listView)

        databaseReference = FirebaseDatabase.getInstance().getReference("Stock")

        stockList = ArrayList()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stockList.clear()
                for (dataSnapshot in snapshot.children) {
                    val partName = dataSnapshot.child("Part Name").value.toString()
                    val brandName = dataSnapshot.child("Brand Name").value.toString()
                    val amount = dataSnapshot.child("Amount").value.toString()
                    val price = dataSnapshot.child("Price").value.toString()
                    val stockItem = StockItem(partName, brandName, amount, price)
                    stockList.add(stockItem)
                }
                val adapter = StockAdapter(this@Stock_view, stockList)
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        val backClick = findViewById<Button>(R.id.back_button)
        backClick.setOnClickListener {
            val intent = Intent(this, Home_View_Admin::class.java)
            startActivity(intent)
        }
        val addClick = findViewById<Button>(R.id.add_item)
        addClick.setOnClickListener {
            val add = Intent(this, Add_Item_View::class.java)
            startActivity(add)
        }
    }
}

class StockItem(val partName: String, val brandName: String, val amount: String, val price: String)

class StockAdapter(private val context: Stock_view, private val stockList: ArrayList<StockItem>) : BaseAdapter() {

    override fun getCount(): Int {
        return stockList.size
    }

    override fun getItem(position: Int): Any {
        return stockList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.stock_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.partNameTextView.text = stockList[position].partName
        viewHolder.brandNameTextView.text = stockList[position].brandName
        viewHolder.amountTextView.text = stockList[position].amount
        viewHolder.priceTextView.text = stockList[position].price

        viewHolder.addToCartImageView.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle("Enter Quantity")

            val editText = EditText(context)
            alertDialogBuilder.setView(editText)

            alertDialogBuilder.setPositiveButton("Add to Cart") { dialog, which ->
                val quantity = editText.text.toString()
                if (quantity.isNotEmpty() && quantity.toInt() > 0) {
                    val availableAmount = stockList[position].amount.toInt()
                    if (quantity.toInt() <= availableAmount) {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val cartDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Cart")
                        val itemId = cartDatabaseReference.push().key.toString()
                        val cartItem = CartItem(stockList[position].partName, stockList[position].brandName, quantity, stockList[position].price)
                        cartDatabaseReference.child(itemId).setValue(cartItem)
                        Toast.makeText(context, "Item added to cart successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Not Enough Stock to Add to cart", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                }
            }

            alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            alertDialogBuilder.show()
        }

        return view
    }
}

class ViewHolder(view: View) {
    val partNameTextView: TextView = view.findViewById(R.id.partnameTextView)
    val brandNameTextView: TextView = view.findViewById(R.id.BrandnameText)
    val amountTextView: TextView = view.findViewById(R.id.amountTextView)
    val priceTextView: TextView = view.findViewById(R.id.priceTextView)
    val addToCartImageView: ImageView = view.findViewById(R.id.cart)
}

class CartItem(val partName: String, val brandName: String, var amount: String, val price:String)