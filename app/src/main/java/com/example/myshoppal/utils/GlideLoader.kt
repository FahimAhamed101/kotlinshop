package com.example.myshoppal.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.myshoppal.R
import java.io.IOException
import java.net.URI

class GlideLoader(val context:Context) {

    fun loadUserPicture(image: Any, imageView: ImageView){
        try{
            Glide
                .with(context)
                .load(image) //uri of image
                .centerCrop() //scale type of image
                .placeholder(R.drawable.ic_user_placeholder) //default placeholder if loading fails
                .into(imageView) //view in which image will be loaded
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    fun loadProductPicture(image: Any, imageView: ImageView){
        try{
            Glide
                .with(context)
                .load(image) //uri of image
                .centerCrop() //scale type of image
                //.placeholder(R.drawable.ic_user_placeholder) //default placeholder if loading fails
                .into(imageView) //view in which image will be loaded
        }catch (e:IOException){
            e.printStackTrace()
        }
    }
}