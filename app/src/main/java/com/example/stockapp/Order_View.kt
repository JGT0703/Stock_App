package com.example.stockapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

// Data class to represent an Order
data class Order(
    val orderId: String,
    val productName: String,
    val quantity: Int,
    val price: Double
)

// Adapter class for the RecyclerView
class OrdersAdapter(private val orders: List<Order>) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdTextView: TextView = itemView.findViewById(R.id.orderIdTextView)
        private val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)

        fun bind(order: Order) {
            orderIdTextView.text = order.orderId
            productNameTextView.text = order.productName
            quantityTextView.text = "Quantity: ${order.quantity}"
            priceTextView.text = "Price: $${order.price}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size
}

// Main Activity to display the list of orders
class Order_View : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var orders: MutableList<Order> // Use MutableList to allow modification
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var profileEmail: TextView
    private lateinit var profileName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_view)

        // Initialize the orders list
        orders = mutableListOf()

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        ordersAdapter = OrdersAdapter(orders)
        recyclerView.adapter = ordersAdapter

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val headerView = navView.getHeaderView(0)
        profileName = headerView.findViewById(R.id.profile_name)
        profileEmail = headerView.findViewById(R.id.profile_email)

        loadUserInfo()

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

        // Fetch orders from Firestore
        fetchOrdersFromFirestore()

        // Set navigation item selected listener
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_item1 -> {
                    startActivity(Intent(this, Home_View_Customer::class.java))
                }
                R.id.nav_item2 -> {
                    startActivity(Intent(this, Stock_view::class.java))
                }
                R.id.nav_item3 -> {
                    startActivity(Intent(this, Users_View::class.java))
                }
                R.id.nav_item4 -> {
                    startActivity(Intent(this, ReportPage::class.java))
                }
                R.id.logout -> {
                    logout()
                }
                else -> {
                    Log.w("Order_View", "Unknown menu item ID: ${item.itemId}")
                }
            }
            drawerLayout.closeDrawers() // Close the drawer after selection
            true
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

    private fun fetchOrdersFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db .collection("Orders")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val orderId = document.getString("orderId") ?: ""
                    val productName = document.getString("productName") ?: ""
                    val quantity = document.getLong("quantity")?.toInt() ?: 0
                    val price = document.getDouble("price") ?: 0.0

                    // Create an Order object and add it to the list
                    val order = Order(orderId, productName, quantity, price)
                    orders.add(order)
                }
                // Notify the adapter that the data has changed
                ordersAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("Order_View", "Error getting documents: ", exception)
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(navView)
                true
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