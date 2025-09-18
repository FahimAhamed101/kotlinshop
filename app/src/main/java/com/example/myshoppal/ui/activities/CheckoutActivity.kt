package com.example.myshoppal.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityCheckoutBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.Address
import com.example.myshoppal.models.CartItem
import com.example.myshoppal.models.Order
import com.example.myshoppal.models.Product
import com.example.myshoppal.ui.adapters.CartItemsListAdapter
import com.example.myshoppal.utils.Constants

class CheckoutActivity : BaseActivity() {
    lateinit var binding: ActivityCheckoutBinding
    private var mAddressDetails: Address? = null
    private lateinit var mProductList: ArrayList<Product>
    private lateinit var mCartListItems: ArrayList<CartItem>
    private var mSubTotal: Double = 0.0
    private var mTotalAmount: Double = 0.0
    private lateinit var mOrderDetails: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        //check if we got some extra info
        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)){
            //if yes put it in global var
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_SELECTED_ADDRESS)
        }

        //check if mAddressDetails has info
        if (mAddressDetails != null){
            //if yes, load those details
            binding.tvCheckoutAddressType.text = mAddressDetails?.type
            binding.tvCheckoutFullName.text = mAddressDetails?.name
            binding.tvCheckoutAddress.text = "${mAddressDetails!!.address}, ${mAddressDetails!!.zipcode}"
            binding.tvCheckoutAdditionalNote.text = mAddressDetails?.additionalNote
            if(mAddressDetails?.otherDetails!!.isNotEmpty()){
                binding.tvCheckoutOtherDetails.text = mAddressDetails?.otherDetails
            }
            binding.tvMobileNumber.text = mAddressDetails?.mobileNumber
        }

        getProductList()
        binding.btnPlaceOrder.setOnClickListener {
            placeAnOrder()
        }
    }

    //can go back by pressing black arrow
    private fun setupActionBar(){

        setSupportActionBar(binding.toolbarCheckoutActivity)
        val actionBar = supportActionBar
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
        }

        binding.toolbarCheckoutActivity.setNavigationOnClickListener { onBackPressed() }
    }

    //call firestore function that get list of all product
    private fun getProductList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAllProductsList(this@CheckoutActivity)
    }

    //get productlist and store it
    fun successProductsListFromFirestore(productList: ArrayList<Product>){
        hideProgressDialog()
        mProductList = productList
        getCartItemsList()
    }

    //call firestore function that get list of cart item
    private fun getCartItemsList(){
        FirestoreClass().getCartList(this@CheckoutActivity)
    }

    //display and populate recyclerView with request from firestore
    fun successCartItemsList(cartList: ArrayList<CartItem>){
        hideProgressDialog()
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

        //setup adapter
        binding.rvCartListItems.layoutManager = LinearLayoutManager(this@CheckoutActivity)
        binding.rvCartListItems.setHasFixedSize(true)
        val cartListAdapter = CartItemsListAdapter(this@CheckoutActivity, mCartListItems, false)
        binding.rvCartListItems.adapter = cartListAdapter


        //calculate subtotal and display it
        for (item in mCartListItems){
            val availableQuantity = item.stock_quantity.toInt()
            if (availableQuantity > 0) {
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()
                mSubTotal += (price * quantity)
            }
        }
        binding.tvSubtotal.text = "$$mSubTotal"
        binding.tvShippinCharge.text = "$10.0"

        //calculate total and display it
        mTotalAmount = mSubTotal + 10
        binding.tvTotal.text = "$$mTotalAmount"


    }

    //hide progress and get new cart list if update is successful
    fun itemUpdateSuccess(){
        hideProgressDialog()
        getCartItemsList()
    }

    //display toast if deletion successful
    fun itemRemovedSuccess(){
        hideProgressDialog()
        Toast.makeText(
            this@CheckoutActivity,
            resources.getString(R.string.msg_item_removed_successfully),
            Toast.LENGTH_SHORT
        ).show()

        //get new cart list
        getCartItemsList()
    }

    //Create an order and send it to Firebase when button is clicked
    private fun placeAnOrder(){
        showProgressDialog(resources.getString(R.string.please_wait))
        //check if address details is not empty
        if(mAddressDetails != null){
            //if ok, create an order object
            mOrderDetails = Order(
                FirestoreClass().getCurrentUserID(),
                mCartListItems,
                mAddressDetails!!,
                "My order ${System.currentTimeMillis()}",
                mCartListItems[0].image,
                mSubTotal.toString(),
                "10.0",
                mTotalAmount.toString(),
                System.currentTimeMillis()
            )
            FirestoreClass().placeOrder(this@CheckoutActivity, mOrderDetails)
        }
    }

    //call the Firestore function that update stock and cart
    fun orderPlacedSuccess(){
        FirestoreClass().updateAllDetails(this, mCartListItems,mOrderDetails)
    }

    //display toast if order placed successful, clear task and get back to dashboard
    fun allDetailsUpdatedSuccessfully(){
        hideProgressDialog()
        Toast.makeText(
            this@CheckoutActivity,
            "Order placed successfully",
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}