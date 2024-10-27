package com.example.stockapp// com.example.stockapp.Products.kt
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Products : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var stockList: ArrayList<StockItems>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        listView = findViewById<ListView>(R.id.product_listview)
        drawerLayout = findViewById(R.id.drawer_layout)

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
                    val stockItem = StockItems(partName, brandName, amount, price)
                    stockList.add(stockItem)
                }
                val adapter = StockAdapters(this@Products, stockList)
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        val navView: NavigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_item1 -> {
                    val home = Intent(this, Home_View_Customer::class.java)
                    startActivity(home)
                }
                R.id.nav_item2 -> {
                    val product = Intent(this, Products::class.java)
                    startActivity(product)
                }
                R.id.nav_item3 -> {
                    val cart = Intent(this, Cart_view::class.java)
                    startActivity(cart)
                }
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

class StockItems(val partName: String, val brandName: String, val amount: String, val price: String)

class StockAdapters(private val context: Products, private val stockList: ArrayList<StockItems>) : BaseAdapter() {

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
        val viewHolder: ViewHolder2
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.stock_item, parent, false)
            viewHolder = ViewHolder2(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder2
        }

        viewHolder.partNameTextView.text = stockList[position].partName
        viewHolder.brandNameTextView.text = stockList[position].brandName
        viewHolder.amountTextView.text = stockList[position].amount
        viewHolder.priceTextView.text = stockList[position].price

        viewHolder.addToCartImageView.setOnClickListener {
            if (context != null) {
                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setTitle("Enter Quantity")

                val editText = EditText(context)
                alertDialogBuilder.setView(editText)

                alertDialogBuilder.setPositiveButton("Add to Cart") { dialog, which ->
                    val quantity = editText.text.toString()
                    if (quantity.isNotEmpty() && quantity.toIntOrNull() != null && quantity.toInt() > 0) {
                        val availableAmount = stockList[position].amount.toInt()
                        if (quantity.toInt() <= availableAmount) {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                val cartDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Cart")
                                val itemId = cartDatabaseReference.push().key.toString()
                                val cartItem = CartItems(stockList[position].partName, stockList[position].brandName, quantity, stockList[position].price)
                                cartDatabaseReference.child(itemId).setValue(cartItem)
                                Toast.makeText(context, "Item added to cart successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "You must be logged in to add items to your cart", Toast.LENGTH_SHORT).show()
                            }
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
            } else {
                Toast.makeText(context, "Context is null", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}

class ViewHolder2(view: View) {
    val partNameTextView: TextView = view.findViewById(R.id.partnameTextView)
    val brandNameTextView: TextView = view.findViewById(R.id.BrandnameText)
    val amountTextView: TextView = view.findViewById(R.id.amountTextView)
    val priceTextView: TextView = view.findViewById(R.id.priceTextView)
    val addToCartImageView: ImageView = view.findViewById(R.id.cart)
}

class CartItems(val partName: String, val brandName: String, var amount: String, val price:String)