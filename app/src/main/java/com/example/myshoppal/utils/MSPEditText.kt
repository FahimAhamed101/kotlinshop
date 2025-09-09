package com.example.myshoppal.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class MSPEditText(context: Context, attrs:AttributeSet): AppCompatEditText(context, attrs) {
    init {
        //call function to apply the font to the components
        applyFont()
    }

    private fun applyFont() {
        //this is used to get the file from assets folder and set it to the title textview
        val typeface: Typeface =
            Typeface.createFromAsset(context.assets,"Montserrat-Regular.ttf")
        setTypeface(typeface)
    }
}