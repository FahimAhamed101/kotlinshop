package com.example.myshoppal.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ItemCartLayoutBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.CartItem
import com.example.myshoppal.ui.activities.CartListActivity
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader

open class CartItemsListAdapter(
    private val context: Context,
    private val list: ArrayList<CartItem>,
    private val updateCartItems: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //inflate individual product layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemCartLayoutBinding = ItemCartLayoutBinding.inflate(
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

        //check if holder is our personal viewHolder
        if (holder is MyViewHolder) {
            //load image (with glider), name and price in appropriate view
            GlideLoader(context).loadProductPicture(model.image, holder.binding.ivCartItemImage)
            holder.binding.tvCartItemTitle.text = model.title
            holder.binding.tvCartItemPrice.text = "$${model.price}"
            holder.binding.tvCartQuantity.text = model.cart_quantity

            //check if quantity is 0
            if (model.cart_quantity == "0") {
                //remove + and - button
                holder.binding.ibRemoveCartItem.visibility = View.GONE
                holder.binding.ibAddCartItem.visibility = View.GONE

                if(updateCartItems){
                    holder.binding.ibDeleteCartItem.visibility = View.VISIBLE
                }else{
                    holder.binding.ibDeleteCartItem.visibility = View.GONE
                }

                //display message out of stock in red
                holder.binding.tvCartQuantity.text =
                    context.resources.getString(R.string.lbl_out_of_stock)
                holder.binding.tvCartQuantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorsnackBarError
                    )
                )
                //else if not out of stock
            } else {
                if (updateCartItems){
                    //display + and - button
                    holder.binding.ibRemoveCartItem.visibility = View.VISIBLE
                    holder.binding.ibAddCartItem.visibility = View.VISIBLE
                    holder.binding.ibDeleteCartItem.visibility = View.VISIBLE
                }else{
                    //hide + and - button
                    holder.binding.ibRemoveCartItem.visibility = View.GONE
                    holder.binding.ibAddCartItem.visibility = View.GONE
                    holder.binding.ibDeleteCartItem.visibility = View.GONE
                }

                //display quantity of stock
                holder.binding.tvCartQuantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSecondaryText
                    )
                )
            }

            //add delete functionality to delete button
            holder.binding.ibDeleteCartItem.setOnClickListener {
                when (context) {
                    is CartListActivity -> {
                        context.showProgressDialog(context.resources.getString((R.string.please_wait)))
                    }

                }
                FirestoreClass().removeItemFromCart(context, model.id)
            }

            //add add functionality to add button
            holder.binding.ibAddCartItem.setOnClickListener {
                //store cart quantity
                val cartQuantity: Int = model.cart_quantity.toInt()

                //check if there is enough stock to add to cart
                if (cartQuantity < model.stock_quantity.toInt()){
                    //store the hashmap for the cart quantity
                    val itemHashMap = HashMap<String, Any>()

                    //new hashmap has key cart_quantity and value is cartquantity + 1
                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

                    //show progress dialog
                    if(context is CartListActivity) {
                        context.showProgressDialog(context.resources.getString((R.string.please_wait)))
                    }

                    //send hashmap to Firestore function
                    FirestoreClass().updateMyCart(context, model.id, itemHashMap)
                    //else show msg not enough stock
                }else{
                    if (context is CartListActivity){
                        context.showErrorSnackBar(
                            context.resources.getString(
                                R.string.msg_for_available_stock,model.stock_quantity), true)
                    }
                }
            }

            //add remove functionality to remove button
            holder.binding.ibRemoveCartItem.setOnClickListener {
                //if u remove last item, product is deleted from cart
                if (model.cart_quantity == "1"){
                    FirestoreClass().removeItemFromCart(context,model.id)
                    //else prepare hashmap with updated value
                }else{
                    //store cart quantity to int
                    val cartQuantity: Int = model.cart_quantity.toInt()
                    //store the hashmap for the cart quantity
                    val itemHashMap = HashMap<String, Any>()

                    //new hashmap has key cart_quantity and value is cartquantity - 1
                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity - 1).toString()

                    //show progress dialog
                    if(context is CartListActivity) {
                        context.showProgressDialog(context.resources.getString((R.string.please_wait)))
                    }

                    //send hashmap to Firestore function
                    FirestoreClass().updateMyCart(context, model.id, itemHashMap)
                }
            }
        }
    }

    //companion class for recyclerview magic
    class MyViewHolder(val binding: ItemCartLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}