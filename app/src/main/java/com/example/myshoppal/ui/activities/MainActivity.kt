package com.example.myshoppal.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myshoppal.databinding.ActivityMainBinding
import com.example.myshoppal.utils.Constants

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, MODE_PRIVATE)
        val username = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME, "")
        binding.tvMain.text = "Hello $username"
    }
}