package com.example.myshoppal.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityMyOrdersBinding
import com.example.myshoppal.models.Order
import com.example.myshoppal.ui.adapters.CartItemsListAdapter
import com.example.myshoppal.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyOrdersDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        var myOrderDetails: Order = Order()
        //if we got extra, store it in myOrderDetails
        if(intent.hasExtra(Constants.EXTRA_MY_ORDER_DETAILS)){
            myOrderDetails = intent.getParcelableExtra<Order>(Constants.EXTRA_MY_ORDER_DETAILS)!!
        }
        setupUI(myOrderDetails)
    }

    //can go back by pressing black arrow
    private fun setupActionBar(){

        setSupportActionBar(binding.toolbarMyOrderDetailsActivity)
        val actionBar = supportActionBar
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_arrow_back_24)
        }

        binding.toolbarMyOrderDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    //Setup UI
    fun setupUI(orderDetails:Order){
        binding.tvOrderDetailsId.text = orderDetails.title

        //prepare val to change time in milisec into readible date, just...do those steps
        val dateFormat = "dd MMM yyyy HH:mm"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = orderDetails.order_datetime
        val orderDateTime = formatter.format(calendar.time)
        binding.tvOrderDetailsDate.text = orderDateTime

        //prepare val with difference in milli and hours
        val diffInMillisec: Long = System.currentTimeMillis() - orderDetails.order_datetime
        val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
        Log.e("diiference in Hours","$diffInHours")

        //change status according to diff in hours
        when{
            diffInHours < 1 -> {
                binding.tvOrderDetailsStatus.text = resources.getString(R.string.order_status_pending)
                binding.tvOrderDetailsStatus.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrdersDetailsActivity,R.color.colorAccent
                    )
                )
            }
            diffInHours < 2 -> {
                binding.tvOrderDetailsStatus.text = resources.getString(R.string.order_status_in_process)
                binding.tvOrderDetailsStatus.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrdersDetailsActivity,R.color.colorOrderStatusInProcess
                    )
                )
            }
            else ->{
                binding.tvOrderDetailsStatus.text = resources.getString(R.string.order_status_delivered)
                binding.tvOrderDetailsStatus.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrdersDetailsActivity,R.color.colorOrderStatusDelivered
                    )
                )
            }
        }

        //setup adapter
        binding.rvMyOrderDetailsItems.layoutManager = LinearLayoutManager(this@MyOrdersDetailsActivity)
        binding.rvMyOrderDetailsItems.setHasFixedSize(true)
        val cartListAdapter = CartItemsListAdapter(this@MyOrdersDetailsActivity,orderDetails.items, false)
        binding.rvMyOrderDetailsItems.adapter = cartListAdapter

        //setup rest of the UI
        binding.tvMyOrderDetailsAddressType.text = orderDetails.address.type
        binding.tvMyOrderDetailsFullName.text = orderDetails.address.name
        binding.tvMyOrderDetailsAddress.text = "${orderDetails.address.address}, ${orderDetails.address.zipcode}"
        binding.tvMyOrderDetailsAdditionalNote.text = orderDetails.address.additionalNote
        if (orderDetails.address.otherDetails.isNotEmpty()){
            binding.tvMyOrderDetailsOtherDetails.visibility = View.VISIBLE
            binding.tvMyOrderDetailsOtherDetails.text = orderDetails.address.otherDetails
        }else{
            binding.tvMyOrderDetailsOtherDetails.visibility = View.GONE
        }

        binding.tvMobileNumber.text = orderDetails.address.mobileNumber

        binding.tvSubtotal.text = orderDetails.sub_total_amount
        binding.tvShippingCharge.text = orderDetails.shipping_charge
        binding.tvTotal.text = orderDetails.total_amount

    }
}