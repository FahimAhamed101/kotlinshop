package com.example.myshoppal.ui.activities


import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowInsets
import android.view.WindowManager

import com.example.myshoppal.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //for the splashscreen to be fullscreen
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else{
            @Suppress("DEPRECATION") //find better solution?
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        //Used to change activity, find better solution
        Handler().postDelayed({//call Handler-Looper to change activity with a 1sec delay
            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            finish()
        },2000)

        //val typeface: Typeface = Typeface.createFromAsset(assets,"Montserrat-Bold.ttf")
        //binding.tvAppName.typeface = typeface
    }
}