package com.example.myshoppal.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityLoginBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.User
import com.example.myshoppal.utils.Constants
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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

        binding.tvForgotPassword.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.tvRegister.setOnClickListener(this)
    }

    //Check if email and password are not empty
    private fun validateLoginDetails(): Boolean{
        return when {
            TextUtils.isEmpty(binding.etEmail.text.toString().trim{it <= ' '})-> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(binding.etPassword.text.toString().trim{it <= ' '})-> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                showErrorSnackBar("your details are valid", false)
                true
            }
        }
    }

    //Log in the user
    private fun LogInRegisteredUser() {
        if (validateLoginDetails()) {
            //show progress dialog
            showProgressDialog(resources.getString(R.string.please_wait))

            //get text from edit text and trim the space
            val email = binding.etEmail.text.toString().trim{it <= ' '}
            val password = binding.etPassword.text.toString().trim{it <= ' '}

            //log in using FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        FirestoreClass().getUserDetails(this@LoginActivity)
                    }else {
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(),true)
                    }
                }
        }
    }

    //In login screen the clickable components are login button, forgotPassword text and register text
    override fun onClick(v: View?) {
        //if view is not null, check his id and do as instructed in the lambda
        if (v != null) {
            when (v.id) {
                R.id.tv_forgot_password -> {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_login -> {
                    LogInRegisteredUser()
                }

                R.id.tv_register -> {
                    //launch register screen
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    fun userLoggedInSuccess(user: User){
        hideProgressDialog()

        /*print user details in log
        Log.i("first name: ", user.firstName)
        Log.i("last name: ", user.lastName)
        Log.i("Email: ", user.email)*/

        if (user.profileCompleted ==0){
            //if profile incomplete, launch UserProfileActivity
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            //redirect user to main screen after log in
            startActivity(Intent(this@LoginActivity, AddProductActivity::class.java))
        }
        finish()
    }
}