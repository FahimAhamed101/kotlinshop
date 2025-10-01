package com.example.myshoppal.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivitySoldProductDetailsBinding
import com.example.myshoppal.models.Order
import com.example.myshoppal.models.SoldProduct
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SoldProductDetailsActivity : BaseActivity() {
    lateinit var binding: ActivitySoldProductDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoldProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var productDetails: SoldProduct = SoldProduct()
        //if we got extra, store it in productDetails
        if(intent.hasExtra(Constants.EXTRA_SOLD_DETAILS)){
            productDetails = intent.getParcelableExtra<SoldProduct>(Constants.EXTRA_SOLD_DETAILS)!!
        }
        setupActionBar()
        setupUI(productDetails)
    }

    //can go back by pressing black arrow
    private fun setupActionBar(){

        setSupportActionBar(binding.toolbarSoldProductDetails)
        val actionBar = supportActionBar
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_arrow_back_24)
        }

        binding.toolbarSoldProductDetails.setNavigationOnClickListener { onBackPressed() }
    }

    //Setup UI
    fun setupUI(productDetails:SoldProduct) {
        binding.tvSoldDetailsId.text = productDetails.title

        //prepare val to change time in milisec into readible date, just...do those steps
        val dateFormat = "dd MMM yyyy HH:mm"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = productDetails.order_date
        val orderDateTime = formatter.format(calendar.time)
        binding.tvSoldDetailsDate.text = orderDateTime

        //setup rest of the UI
        GlideLoader(this@SoldProductDetailsActivity).loadProductPicture(
            productDetails.image,
            binding.ivItemImage
        )
        binding.tvItemName.text = productDetails.title
        binding.tvItemPrice.text = "$${productDetails.price}"
        binding.tvItemQuantity.text = productDetails.sold_quantity

        binding.tvSoldDetailsAddressType.text = productDetails.address.type
        binding.tvSoldDetailsFullName.text = productDetails.address.name
        binding.tvSoldDetailsAddress.text = "${productDetails.address.address}, ${productDetails.address.zipcode}"
        binding.tvSoldDetailsAdditionalNote.text = productDetails.address.additionalNote
        if (productDetails.address.otherDetails.isNotEmpty()){
            binding.tvSoldDetailsOtherDetails.visibility = View.VISIBLE
            binding.tvSoldDetailsOtherDetails.text = productDetails.address.otherDetails
        }else{
            binding.tvSoldDetailsOtherDetails.visibility = View.GONE
        }

        binding.tvMobileNumber.text = productDetails.address.mobileNumber

        binding.tvSubtotal.text = productDetails.sub_total_amount
        binding.tvShippingCharge.text = productDetails.shipping_charge
        binding.tvTotal.text = productDetails.total_amount
    }
}