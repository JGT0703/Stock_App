package com.example.stockapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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

class Cart_view : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var cartList: ArrayList<CartItem>
    private lateinit var totalPriceTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_view)

        listView = findViewById(R.id.listViewId)
        totalPriceTextView = findViewById(R.id.totalTextView)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Cart")

        cartList = ArrayList()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartList.clear()
                var totalPrice = 0.0
                for (dataSnapshot in snapshot.children) {
                    val partName = dataSnapshot.child("partName").value.toString()
                    val amount = dataSnapshot.child("amount").value.toString()
                    val price = dataSnapshot.child("price").value.toString()
                    val cartItem = CartItem(partName, "", amount, price)
                    cartList.add(cartItem)
                    totalPrice += cartItem.price.toDouble() * cartItem.amount.toInt()
                }
                val adapter = CartAdapter(this@Cart_view, cartList)
                listView.adapter = adapter
                totalPriceTextView.text = "Total: R " + String.format("%.2f", totalPrice)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Cart_view, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        })

        val backButton = findViewById<Button>(R.id.continue_button)
        backButton.setOnClickListener {
            val intent = Intent(this, Home_View_Customer::class.java)
            startActivity(intent)
        }
    }
}

class CartAdapter(private val context: Cart_view, private val cartList: ArrayList<CartItem>) : ArrayAdapter<CartItem>(context, 0, cartList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: CartViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false)
            viewHolder = CartViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as CartViewHolder
        }

        val cartItem = getItem(position)

        viewHolder.partNameTextView.text = cartItem!!.partName
        viewHolder.amountTextView.text = cartItem!!.amount
        viewHolder.priceTextView.text = cartItem!!.price

        viewHolder.editTextView.setOnClickListener {
            showEditAmountDialog(cartItem!!, viewHolder.amountTextView)
        }

        viewHolder.removeTextView.setOnClickListener {
            removeItemFromCart(cartItem!!)
        }

        return view
    }

    private fun showEditAmountDialog(cartItem: CartItem, amountTextView: TextView) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.edit_amount_dialog, null)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        amountEditText.setText(cartItem.amount)
        builder.setView(dialogView)
        builder.setPositiveButton("Save") { dialog, _ ->
            val newAmount = amountEditText.text.toString()
            cartItem.amount = newAmount
            amountTextView.text = newAmount
            notifyDataSetChanged()

            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser!!.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Cart")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        if (dataSnapshot.child("partName").value.toString() == cartItem.partName) {
                            dataSnapshot.ref.child("amount").setValue(newAmount)
                            break
                        }
                    }
                    val intent = Intent(context, Cart_view::class.java)
                    context.startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            })

            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun removeItemFromCart(cartItem: CartItem) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to remove this item from your cart?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser!!.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Cart")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        if (dataSnapshot.child("partName").value.toString() == cartItem.partName) {
                            dataSnapshot.ref.removeValue()
                            cartList.remove(cartItem)
                            notifyDataSetChanged()
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            })
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
class CartViewHolder(view: View) {
    val partNameTextView: TextView = view.findViewById(R.id.partnameTextView)
    val amountTextView: TextView = view.findViewById(R.id.amountTextView)
    val priceTextView: TextView = view.findViewById(R.id.priceTextView)
    val editTextView: TextView = view.findViewById(R.id.edit)
    val removeTextView: TextView = view.findViewById(R.id.remove)
}
}
