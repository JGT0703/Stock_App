package com.example.stockapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Users_View : AppCompatActivity() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_view)

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().reference

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Get the logout text view
        val logoutView = navView.findViewById<TextView>(R.id.logout)

        // Initialize ActionBarDrawerToggle
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        } else {
            Log.w("Home_View_Customer", "SupportActionBar is null")
        }

        val toggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set navigation item selected listener
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_item1 -> {
                    val home = Intent(this, Home_View_Admin::class.java)
                    startActivity(home)
                }

                R.id.nav_item2 -> {
                    val product = Intent(this, Products::class.java)
                    startActivity(product)
                }

                R.id.nav_item3 -> {
                    val user_managment = Intent(this, Users_View::class.java)
                    startActivity(user_managment)
                }

                R.id.nav_item4 -> {
                    val report = Intent(this, ReportPage::class.java)
                    startActivity(report)
                }
                // Handle other items here
                else -> {
                    Log.w("Home_View_Customer", "Unknown menu item ID: ${item.itemId}")
                }
            }
            true
        }

        // Set logout text view click listener
        logoutView.setOnClickListener {
            logout()
        }

        // Set up the ListViews and Adapters
        val adminList = mutableListOf<User>()
        val customerList = mutableListOf<User>()
        val unknownList = mutableListOf<User>()

        val adminAdapter = UserAdapter(this, adminList)
        val customerAdapter = UserAdapter(this, customerList)
        val unknownAdapter = UserAdapter(this, unknownList)

        val adminListView = findViewById<ListView>(R.id.admin_list_view)
        val customerListView = findViewById<ListView>(R.id.customer_list_view)
        val unknownListView = findViewById<ListView>(R.id.unknown_list_view)

        adminListView.adapter = adminAdapter
        customerListView.adapter = customerAdapter
        unknownListView.adapter = unknownAdapter

        val backBtn = findViewById<Button>(R.id.back_button)
        backBtn.setOnClickListener {
            val back = Intent(this, Home_View_Admin::class.java)
            startActivity(back)
        }

        // Load users from Firebase
        mDatabase.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val userTypeObject = userSnapshot.child("User Information").child("userType")
                    val emailObject = userSnapshot.child("User Information").child("email")
                    if (userTypeObject.exists() && emailObject.exists()) {
                        val userType = userTypeObject.value as String
                        val email = emailObject.value as String
                        if (userType != null && email != null) {
                            val user = User(email, userType)
                            when (userType) {
                                "Admin" -> adminList.add(user)
                                "Customer" -> customerList.add(user)
                                else -> unknownList.add(user)
                            }
                        }
                    }
                }

                adminAdapter.notifyDataSetChanged()
                customerAdapter.notifyDataSetChanged()
                unknownAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (drawerLayout != null && navView != null) {
                    drawerLayout.openDrawer(navView)
                    true
                } else {
                    Log.w("Home_View_Customer", "DrawerLayout or NavigationView is null")
                    false
                }
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

    class User(val email: String, var userType: String)

    class UserAdapter internal constructor(
        private val context: Context,
        private val userList: MutableList<User>
    ) : BaseAdapter() {

        override fun getCount(): Int {
            return userList.size
        }

        override fun getItem(position: Int): Any {
            return userList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val viewHolder: ViewHolder

            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            val user = userList[position]
            viewHolder.emailTextView.text = user.email
            viewHolder.userTypeTextView.text = user.userType

            view.setOnClickListener {
                val dialogFragment = UserDialogFragment.newInstance(user)
                dialogFragment.show(
                    (context as AppCompatActivity).supportFragmentManager,
                    "UserDialogFragment"
                )
            }

            return view
        }

        internal class ViewHolder(view: View) {
            val emailTextView: TextView = view.findViewById(R.id.email_textview)
            val userTypeTextView: TextView = view.findViewById(R.id.user_type_textview)
        }
    }

    class UserDialogFragment : DialogFragment() {

        var user: User? = null
        private lateinit var mDatabase: DatabaseReference

        companion object {
            fun newInstance(user: User): UserDialogFragment {
                val fragment = UserDialogFragment()
                fragment.user = user
                return fragment
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mDatabase = FirebaseDatabase.getInstance().reference
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_user, null)

            val emailTextView = view.findViewById<TextView>(R.id.email_textview)
            val userTypeTextView = view.findViewById<TextView>(R.id.user_type_textview)
            val userTypeSpinner = view.findViewById<Spinner>(R.id.user_type_spinner)

            emailTextView.text = user?.email
            userTypeTextView.text = user?.userType

            val userTypeOptions = arrayOf("Admin", "Customer", "Unknown")
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                userTypeOptions
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            userTypeSpinner.adapter = adapter

            userTypeSpinner.setSelection(userTypeOptions.indexOf(user?.userType))

            userTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val selectedUserType = userTypeOptions[position]
                    if (selectedUserType != user?.userType) {
                        // Get the email of the user clicked
                        val userEmail = user?.email

                        // Find the UID of the user based on their email
                        mDatabase.child("Users").orderByChild("User Information/email")
                            .equalTo(userEmail)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (userSnapshot in dataSnapshot.children) {
                                        val uid = userSnapshot.key

                                        // Update the user's userType in the database
                                        mDatabase.child("Users").child(uid!!)
                                            .child("User Information").child("userType")
                                            .setValue(selectedUserType)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    user?.userType = selectedUserType
                                                    userTypeTextView.text = selectedUserType

                                                    val intent =
                                                        Intent(context, Users_View::class.java)
                                                    context!!.startActivity(intent)
                                                } else {
                                                    Toast.makeText(
                                                        requireActivity(),
                                                        "Failed to update user type",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle database error
                                }
                            })
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            val dialog = AlertDialog.Builder(requireActivity())
                .setView(view)
                .setPositiveButton("OK") { _, _ -> }
                .create()

            return dialog
        }
    }
}