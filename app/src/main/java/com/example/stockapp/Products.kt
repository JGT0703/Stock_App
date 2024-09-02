import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.stockapp.Cart_view
import com.example.stockapp.MainActivity
import com.example.stockapp.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Products : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var profileEmail: TextView
    private lateinit var profileName: TextView
    private lateinit var logoutTextView: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var stockRef: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var stockAdapter: StockAdapter
    private val stockList = ArrayList<StockItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        listView = findViewById(R.id.product_listview)

        // Get the header view and the email text view
        val headerView = navView.getHeaderView(0)
        if (headerView != null) {
            profileEmail = headerView.findViewById(R.id.profile_email)
            profileName = headerView.findViewById(R.id.profile_name)
        } else {
            Log.e("Products", "Header view is null")
        }

        // Get the logout text view
        val logoutView = navView.findViewById<TextView>(R.id.logout)

        // Check if Firebase user is not null
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val userEmail = firebaseUser.email
            if (!userEmail.isNullOrEmpty()) {
                profileEmail.text = userEmail
            } else {
                Log.e("Products", "User email is null or empty")
            }
        } else {
            Log.e("Products", "Firebase user is null")
        }

        // Initialize ActionBarDrawerToggle
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        } else {
            Log.e("Products", "ActionBar is null")
        }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set navigation item selected listener
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_item1 -> {
                    val add = Intent(this, Products::class.java)
                    startActivity(add)
                }
                R.id.nav_item2 -> {
                    val cart = Intent(this, Cart_view::class.java)
                    startActivity(cart)
                }
                // Handle other items here
            }
            true
        }

        // Set logout text view click listener
        logoutView.setOnClickListener {
            logout()
        }

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance()
        stockRef = database.getReference("Stock")

        // Read data from Firebase Realtime Database
        stockRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    stockList.clear()
                    for (childSnapshot in dataSnapshot.children) {
                        val stockItem = childSnapshot.getValue(StockItem::class.java)
                        if (stockItem != null) {
                            stockList.add(stockItem)
                        } else {
                            Log.e("Products", "Stock item is null")
                        }
                    }
                    stockAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("Products", "Error reading data from Firebase Realtime Database: $e")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Products", "Error reading data from Firebase Realtime Database: $error")
            }
        })

        // Create an Adapter to bind the data to the ListView
        stockAdapter = StockAdapter(this, stockList)
        listView.adapter = stockAdapter
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

// Data class for stock item
data class StockItem(val name: String = "", val brandName: String = "", val amount: Int = 0, val price: Double = 0.0)
// Custom Adapter to bind the data to the ListView
class StockAdapter(private val context: Context, private val stockList: ArrayList<StockItem>) : BaseAdapter() {
    override fun getCount(): Int {
        return stockList.size
    }

    override fun getItem(position: Int): Any {
        return stockList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.stock_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val stockItem = stockList[position]

        viewHolder.partNameTextView.text = stockItem.name
        viewHolder.brandNameTextView.text = stockItem.brandName
        viewHolder.amountTextView.text = "Amount: ${stockItem.amount}"
        viewHolder.priceTextView.text = "Price (ZAR): ${stockItem.price}"

        return view
    }

    private class ViewHolder(view: View) {
        val partNameTextView: TextView = view.findViewById(R.id.partnameTextView)
        val brandNameTextView: TextView = view.findViewById(R.id.BrandnameText)
        val amountTextView: TextView = view.findViewById(R.id.amountTextView)
        val priceTextView: TextView = view.findViewById(R.id.priceTextView)
        val cartImageView: ImageView = view.findViewById(R.id.cart)
    }
}