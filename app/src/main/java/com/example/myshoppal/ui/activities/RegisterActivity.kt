package com.example.myshoppal.ui.activities

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityRegisterBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth


class RegisterActivity : BaseActivity() {
    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        /*If you have fullscreen with scrollview, it won't scroll with keyboard displayed
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else{
            @Suppress("DEPRECATION") //find better solution?
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }*/

        binding.tvLogin.setOnClickListener {
            //launch login screen
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRegister.setOnClickListener{
            registerUser()
        }

    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etFirstName.text.toString().trim { it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(binding.etLastName.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(binding.etEmail.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(binding.etPassword.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(binding.etConfirmPassword.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_confirm_password), true)
                false
            }

            binding.etPassword.text.toString().trim{it <= ' '} != binding.etConfirmPassword.text.toString().
            trim { it <=' ' } -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_mismatch), true)
                false
            }

            !binding.cbTermsAndConditions.isChecked -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_conditions), true)
                false
            }
            else -> {
                Log.e("validate","all entry valid")
                true
            }
        }
    }

    //can go back by pressing black arrow
    private fun setupActionBar(){

        setSupportActionBar(binding.toolbarRegisterActivity)
        val actionBar = supportActionBar
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_arrow_back_24)
        }

        binding.toolbarRegisterActivity.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun registerUser () {

        //Check with validateRegisterDetails if the entries are valid or not
        if (validateRegisterDetails()){

            showProgressDialog(resources.getString(R.string.please_wait))

            val email: String = binding.etEmail.text.toString().trim{it <= ' '}
            val password: String = binding.etPassword.text.toString().trim{it <= ' '}

            //create an instance and register user with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->

                        //if registration successful
                        if (task.isSuccessful){
                            //Firebase registers user
                            val firebaseUser: FirebaseUser = task.result!!.user!!

                            //record entered input in user instance
                            val user = User(
                                firebaseUser.uid,
                                binding.etFirstName.text.toString().trim{ it <= ' '},
                                binding.etLastName.text.toString().trim{it <= ' '},
                                binding.etEmail.text.toString().trim{it <= ' '}
                            )

                            FirestoreClass().registerUser(this@RegisterActivity,user)

                            showErrorSnackBar(
                                "You are successfully registered. Your user id is ${firebaseUser.uid}",
                                false
                            )

                            /* //log out and close screen
                             FirebaseAuth.getInstance().signOut()
                             finish()*/

                        }else{
                            hideProgressDialog()
                            // if not successful, show error
                            showErrorSnackBar(task.exception!!.message.toString(),true)
                        }
                    }
                )
        }
    }

    fun userRegistrationSuccess() {
        //hide progress bar
        hideProgressDialog()

        Toast.makeText(
            this@RegisterActivity,
            resources.getString(R.string.register_success),
            Toast.LENGTH_SHORT
        )
            .show()
    }

}