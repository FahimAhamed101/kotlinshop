package com.example.myshoppal.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myshoppal.databinding.ItemAddressLayoutBinding
import com.example.myshoppal.databinding.ItemCartLayoutBinding
import com.example.myshoppal.models.Address
import com.example.myshoppal.ui.activities.AddEditAddressActivity
import com.example.myshoppal.ui.activities.CheckoutActivity
import com.example.myshoppal.utils.Constants

class AddressListAdapter(
    private val context: Context, private var list:ArrayList<Address>, private val selectAddress: Boolean
):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //inflate individual product layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemAddressLayoutBinding = ItemAddressLayoutBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    //count the number of items
    override fun getItemCount(): Int {
        return list.size
    }

    //do stuff on each item position
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            holder.binding.tvAddressFullName.text = model.name
            holder.binding.tvAddressType.text = model.type
            holder.binding.tvAddressDetails.text = "${model.address}, ${model.zipcode}"
            holder.binding.tvAddressMobileNumber.text = model.mobileNumber

            //show address selected in a toast
            if (selectAddress){
                holder.itemView.setOnClickListener{
                    /*Toast.makeText(
                        context, "Selected address: ${model.address}, ${model.zipcode}",
                                Toast.LENGTH_SHORT
                    ).show()*/
                    val intent = Intent(context, CheckoutActivity::class.java)
                    intent.putExtra(Constants.EXTRA_SELECTED_ADDRESS, model)
                    context.startActivity(intent)
                }
            }
        }
    }

    //send user to AddEditAddressActivity when swipe with some extra
    fun notifyEditItem(activity: Activity, position: Int){
        val intent = Intent(context, AddEditAddressActivity::class.java)
        intent.putExtra(Constants.EXTRA_ADDRESS_DETAILS, list[position])
        activity.startActivity(intent)
        notifyItemChanged(position)
    }

    //companion class for recyclerview magic
    class MyViewHolder(val binding: ItemAddressLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}