package com.example.myshoppal.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton

class MSPRadioButton(context: Context, attributeSet: AttributeSet):AppCompatRadioButton(context, attributeSet) {
    init {
        applyFont()
    }

    private fun applyFont(){
        //used to get file from assets folder and set it to the title of button
        val typeface: Typeface =
            Typeface.createFromAsset(context.assets, "Montserrat-Bold.ttf")
        setTypeface(typeface)
    }
}