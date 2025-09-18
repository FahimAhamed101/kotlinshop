package com.example.myshoppal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityCartListBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.CartItem
import com.example.myshoppal.models.Product
import com.example.myshoppal.ui.adapters.CartItemsListAdapter
import com.example.myshoppal.utils.Constants

class CartListActivity : BaseActivity() {

    private lateinit var mProductList: ArrayList<Product>
    private lateinit var mCartListItems: ArrayList<CartItem>
    private lateinit var binding: ActivityCartListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        binding.btnCheckout.setOnClickListener {
            val intent = Intent(this@CartListActivity, AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECT_ADDRESS, true)
            startActivity(intent)
        }
    }

    //display back arrow in toolbar
    private fun setupActionBar(){

        setSupportActionBar(binding.toolbarCartListActivity)
        val actionBar = supportActionBar
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
        }

        binding.toolbarCartListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    //call firestore function that get list of all product
    private fun getProductList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAllProductsList(this@CartListActivity)
    }

    //get productlist and store it
    fun successProductsListFromFirestore(productList: ArrayList<Product>){
        hideProgressDialog()
        mProductList = productList
        getCartItemsList()
    }

    //call firestore function that get list of cart item
    private fun getCartItemsList(){
        //showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getCartList(this@CartListActivity)
    }

    //display and populate recyclerView with request from firestore
    fun successCartItemsList(cartList: ArrayList<CartItem>){
        hideProgressDialog()

        //log for now
        /*for (i in cartList){
            Log.i("cart item title", i.title)
        }*/

        //This will loop through each product/cartItem and update the stock of cart with stock of product
        for (product in mProductList){
            for (cartItem in cartList){
                if (product.product_id == cartItem.product_id){
                    cartItem.stock_quantity = product.stock_quantity

                    //if no stock, change cart quantity to 0
                    if (product.stock_quantity.toInt() == 0){
                        cartItem.cart_quantity = product.stock_quantity
                    }
                }
            }
        }

        mCartListItems = cartList

        if (mCartListItems.size > 0){
            //change visibility of layout element
            binding.rvCartItemsList.visibility = View.VISIBLE
            binding.llCheckout.visibility = View.VISIBLE
            binding.tvNoCartItemFound.visibility = View.GONE

            //setup adapter
            binding.rvCartItemsList.layoutManager = LinearLayoutManager(this@CartListActivity)
            binding.rvCartItemsList.setHasFixedSize(true)
            val cartListAdapter = CartItemsListAdapter(this@CartListActivity, mCartListItems, true)
            binding.rvCartItemsList.adapter = cartListAdapter

            //calculate subtotal and display it
            var subTotal:Double = 0.0
            for (item in mCartListItems){
                val availableQuantity = item.stock_quantity.toInt()
                if (availableQuantity > 0) {
                    val price = item.price.toDouble()
                    val quantity = item.cart_quantity.toInt()
                    subTotal += (price * quantity)
                }
            }
            binding.tvSubTotal.text = "$$subTotal"

            binding.tvShippingCharge.text = "$10.0"

            //calculate total and display it
            if (subTotal > 0){
                binding.llCheckout.visibility = View.VISIBLE
                val total = subTotal + 10
                binding.tvTotalAmount.text = "$$total"
            }else{
                binding.llCheckout.visibility = View.GONE
            }
        }else{
            //change visibility of layout element if no item in cart
            binding.rvCartItemsList.visibility = View.GONE
            binding.llCheckout.visibility = View.GONE
            binding.tvNoCartItemFound.visibility = View.VISIBLE
        }
    }

    //display toast if deletion successful
    fun itemRemovedSuccess(){
        hideProgressDialog()
        Toast.makeText(
            this@CartListActivity,
            resources.getString(R.string.msg_item_removed_successfully),
            Toast.LENGTH_SHORT
        ).show()

        //get new cart list
        getCartItemsList()
    }

    //hide progress and get new cart list if update is successful
    fun itemUpdateSuccess(){
        hideProgressDialog()
        getCartItemsList()
    }

    //call the method, that call the method to get the list of cart item from firestore,
    //and the method that get list of all product
    //everytime we reach that screen
    override fun onResume() {
        super.onResume()
        //getCartItemsList()
        getProductList()
    }
}