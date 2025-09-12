package com.example.myshoppal.ui.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myshoppal.databinding.ItemDashboardLayoutBinding
import com.example.myshoppal.databinding.ItemListLayoutBinding
import com.example.myshoppal.models.Product

import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader

open class DashboardItemsListAdapter(
    private val context: Activity,
    private var list: ArrayList<Product>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: View.OnClickListener? = null

    //inflate individual product layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemDashboardLayoutBinding = ItemDashboardLayoutBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    /*fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }*/

    //count the number of items
    override fun getItemCount(): Int {
        return list.size
    }

    //do stuff on each item position
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        //check if holder is our personal viewHolder, and load
        //image (with glider), name and price in appropriate view
        if (holder is MyViewHolder) {
            GlideLoader(context).loadProductPicture(model.image, holder.binding.ivDashboardItemImage)
            holder.binding.tvDashboardItemTitle.text = model.title
            holder.binding.tvDashboardItemPrice.text = "$${model.price}"


            /*holder.itemView.setOnClickListener{
                if (onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }*/
        }
    }

    //companion class for recyclerview magic
    class MyViewHolder(val binding: ItemDashboardLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    /*interface OnClickListener {
        fun onClick(position: Int, product: Product)
    }*/
}