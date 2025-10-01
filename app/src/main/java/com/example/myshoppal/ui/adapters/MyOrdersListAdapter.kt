package com.example.myshoppal.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myshoppal.databinding.ItemListLayoutBinding
import com.example.myshoppal.models.Order

import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader

open class MyOrdersListAdapter(
    private val context: Context,
    private var list: ArrayList<Order>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //inflate individual order layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemListLayoutBinding = ItemListLayoutBinding.inflate(
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

        //check if holder is our personal viewHolder and,
        if (holder is MyViewHolder) {
            //load image (with glider), name and totalamount in appropriate view
            GlideLoader(context).loadProductPicture(model.image, holder.binding.ivItemImage)
            holder.binding.tvItemName.text = model.title
            holder.binding.tvItemPrice.text = "$${model.total_amount}"

            //make delete icon invisible
            holder.binding.ibDeleteProduct.visibility = View.GONE



        }

    }

    //companion class for recyclerview magic
    class MyViewHolder(val binding: ItemListLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}