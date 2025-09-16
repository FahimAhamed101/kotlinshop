package com.example.myshoppal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityProductDetailsBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.CartItem
import com.example.myshoppal.models.Product
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityProductDetailsBinding
    private var mProductId: String = ""
    private var mProductOwnerId: String = ""
    private lateinit var mProductDetails: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        //check if we got some extra info
        if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
            //if yes, put it in the global var and log it
            mProductId = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
            Log.i("product id", mProductId)
        }

        //check if we got some extra info
        if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
            //if yes, put it in the global var and log it
            mProductOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
            Log.i("product owner id", mProductOwnerId)
        }

        //compare current id and id received, and display cart icon accordingly
        if (FirestoreClass().getCurrentUserID() == mProductOwnerId) {
            binding.btnAddToCart.visibility = View.GONE
            binding.btnGoToCart.visibility = View.GONE
        } else {
            binding.btnAddToCart.visibility = View.VISIBLE
            binding.btnGoToCart.visibility = View.VISIBLE
        }

        getProductDetails()

        binding.btnAddToCart.setOnClickListener(this)
        binding.btnGoToCart.setOnClickListener(this)

    }

    //display back arrow in toolbar
    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarProductDetailsActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
        }

        binding.toolbarProductDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    //get called when data is recovered from Firestore, and populate layout
    fun productDetailsSuccess(product: Product) {
        mProductDetails = product
        //hideProgressDialog()
        GlideLoader(this@ProductDetailsActivity).loadProductPicture(
            product.image,
            binding.ivProductDetailImage
        )
        binding.tvProductDetailsTitle.text = product.title
        binding.tvProductDetailsPrice.text = "$${product.price}"
        binding.tvProductDetailsDescription.text = product.description
        binding.tvProductDetailsAvailableQuantity.text = product.stock_quantity

        //check if stock = 0
        if (product.stock_quantity.toInt() == 0) {
            //remove btn and add out of stock message
            hideProgressDialog()
            binding.btnAddToCart.visibility = View.GONE
            binding.tvProductDetailsQuantity.text = resources.getString(R.string.lbl_out_of_stock)
            binding.tvProductDetailsQuantity.setTextColor(
                ContextCompat.getColor(
                    this@ProductDetailsActivity,
                    R.color.colorsnackBarError
                )
            )
        } else {
            //if it's user own product, don't do anything
            if (FirestoreClass().getCurrentUserID() == product.user_id) {
                hideProgressDialog()
                //if it's someone else product, change btn from addToCart to goToCart
            } else {
                FirestoreClass().checkIfItemExistInCart(this, mProductId)
            }
        }
    }

    //call firestore function that get product details
    private fun getProductDetails() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getProductDetails(this, mProductId)
    }

    //create a cartItem object to add to collection
    private fun addToCart() {
        val cartItem = CartItem(
            FirestoreClass().getCurrentUserID(),
            mProductOwnerId,
            mProductId,
            mProductDetails.title,
            mProductDetails.price,
            mProductDetails.image,
            Constants.DEFAULT_CART_QUANTITY
        )

        showProgressDialog(resources.getString(R.string.please_wait))

        //add newly created CartItem to collection
        FirestoreClass().addCartItems(this, cartItem)
    }

    //display toast when product added to cart
    fun addToCartSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this@ProductDetailsActivity,
            resources.getString(R.string.success_msg_item_added_to_cart),
            Toast.LENGTH_SHORT
        ).show()

        binding.btnAddToCart.visibility = View.GONE
        binding.btnGoToCart.visibility = View.VISIBLE
    }

    //change button addToCart to goToCart depending if product is in cart or not
    fun productExistsInCart() {
        hideProgressDialog()
        binding.btnAddToCart.visibility = View.GONE
        binding.btnGoToCart.visibility = View.VISIBLE
    }

    //do stuff when click stuff
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.btn_add_to_cart -> {
                    //create cartItem object and add it to collection
                    addToCart()
                }
                R.id.btn_go_to_cart -> {
                    //go to CartListActivity
                    startActivity(Intent(this@ProductDetailsActivity, CartListActivity::class.java))
                }
            }
        }
    }

}