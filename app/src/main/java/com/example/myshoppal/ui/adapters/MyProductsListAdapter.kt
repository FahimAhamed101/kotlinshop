package com.example.myshoppal.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ItemListLayoutBinding
import com.example.myshoppal.models.Product
import com.example.myshoppal.ui.activities.ProductDetailsActivity
import com.example.myshoppal.ui.fragments.ProductsFragment
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader

open class MyProductsListAdapter(
    private val context: Activity,
    private var list: ArrayList<Product>,
    private val fragment: ProductsFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //inflate individual product layout
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
            //load image (with glider), name and price in appropriate view
            GlideLoader(context).loadProductPicture(model.image, holder.binding.ivItemImage)
            holder.binding.tvItemName.text = model.title
            holder.binding.tvItemPrice.text = "$${model.price}"

            //set onclicklistener on delete icon
            holder.binding.ibDeleteProduct.setOnClickListener {
                //call delete function on current product's id
                fragment.deleteProduct(model.product_id)
            }

            //set oncliclistener for each item in the view
            holder.itemView.setOnClickListener{
                //move to ProductDetailsActivity with product id
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, model.product_id)
                intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, model.user_id)
                context.startActivity(intent)
            }
        }

    }

    //companion class for recyclerview magic
    class MyViewHolder(val binding: ItemListLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}