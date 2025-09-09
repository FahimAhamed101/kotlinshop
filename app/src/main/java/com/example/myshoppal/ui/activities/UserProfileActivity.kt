package com.example.myshoppal.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityUserProfileBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.User
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader
import java.io.IOException

class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var mUserDetails: User
    private var mSelectedImageUri: Uri? = null //uri is address on local device
    private var mUserProfileImageUrl: String = "" //url is address in cloud storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)



        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            //get user info from intent as a parcelableExtra
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }

        binding.etFirstName.setText(mUserDetails.firstName)
        binding.etLastName.setText(mUserDetails.lastName)
        binding.etEmail.isEnabled = false
        binding.etEmail.setText(mUserDetails.email)

        if (mUserDetails.profileCompleted == 0) {
            binding.tvTitle.text = resources.getString(R.string.title_complete_profile)
            binding.etFirstName.isEnabled = false

            binding.etLastName.isEnabled = false
        } else {
            setupActionBar()
            GlideLoader(this@UserProfileActivity).loadUserPicture(
                mUserDetails.image,
                binding.ivUserPhoto
            )

            if (mUserDetails.mobile != 0L) {
                binding.etMobileNumber.setText(mUserDetails.mobile.toString())
            }
            if (mUserDetails.gender == Constants.MALE) {
                binding.rbMale.isChecked = true
            } else {
                binding.rbFemale.isChecked = true
            }
        }

        binding.ivUserPhoto.setOnClickListener(this@UserProfileActivity)

        binding.btnSubmit.setOnClickListener(this@UserProfileActivity)
    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarUserProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_arrow_back_24)
        }

        binding.toolbarUserProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_user_photo -> {
                    //check if READ_EXTERNAL_STORAGE permission is allowed or not
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        Constants.showImageChooser(pickImage)
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }

                R.id.btn_submit -> {
                    if (validateUserProfileDetails()) {
                        showProgressDialog(resources.getString(R.string.please_wait))


                        if (mSelectedImageUri != null)
                            FirestoreClass().uploadImageToCloudStorage(
                                this,
                                mSelectedImageUri,
                                Constants.USER_PROFILE_IMAGE
                            )
                        //showErrorSnackBar("Your details are valid. You can update them", false)

                    } else {
                        updateUserProfileDetails()
                    }
                }
            }
        }
    }

    //prepare hashmap, combination of KEY (constant) and value (val) before uploading it to the cloud
    private fun updateUserProfileDetails() {
        //prepare hashmap var
        val userHashMap = HashMap<String, Any>()

        //get value from layout and convert mobile number to string
        val mobileNumber = binding.etMobileNumber.text.toString().trim { it <= ' ' }

        //check value in layout and assign a constant to gender
        val gender = if (binding.rbMale.isChecked) {
            Constants.MALE
        } else {
            Constants.FEMALE
        }
        //here we checked if first name is different from the one we saved in the cloud,
        //and if so we change it by uploading a new hashmap
        val firstName = binding.etFirstName.text.toString().trim { it <= ' ' }
        if (firstName != mUserDetails.firstName) {
            userHashMap[Constants.FIRST_NAME] = firstName
        }

        //here we checked if last name is different from the one we saved in the cloud,
        //and if so we change it by uploading a new hashmap
        val lastName = binding.etLastName.text.toString().trim { it <= ' ' }
        if (lastName != mUserDetails.lastName) {
            userHashMap[Constants.LAST_NAME] = lastName
        }

        //if mobilenumber aint empty and different from previous one, we store it in a hashmap with key MOBILE
        // and value are the numbers converted from string to long
        if (mobileNumber.isNotEmpty() && mobileNumber != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }

        //if gender is not empty and different from previous one,
        // we store gender in hashmap with key GENDER and value is male or female
        if (gender.isNotEmpty() && gender != mUserDetails.gender) {
            userHashMap[Constants.GENDER] = gender
        }

        //if uri is not empty and different from previous one,
        // we store uri in hashmap with key IMAGE and value is image uri
        if (mUserProfileImageUrl.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mUserProfileImageUrl
        }

        //change profile value to 1 (=complete)
        userHashMap[Constants.COMPLETE_PROFILE] = 1

        //showProgressDialog(resources.getString(R.string.please_wait))

        //update user profile data by sending all the hashmap
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //Toast to show its granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //showErrorSnackBar("the storage permission is granted", false)
                Constants.showImageChooser(pickImage)
            } else {
                Toast.makeText(
                    this, resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    //use registerForActivity instead
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == Constants.PICK_IMAGE_REQUEST_CODE){
                if (data != null){
                    try {
                        //we save uri of selected image
                        SelectedImageUri = data.data!!
                        //we use Uri to replace image
                        GlideLoader(this).loadUserPicture(mSelectedImageUri!!, binding.ivUserPhoto)
                    }catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this@UserProfileActivity,
                        resources.getString(R.string.image_selection_failed),
                        Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }*/

    //pick image from gallery and set it as thumbnail
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    try {
                        //we save uri of selected image
                        mSelectedImageUri = result.data!!.data!!
                        //we use Uri to replace image
                        GlideLoader(this).loadUserPicture(mSelectedImageUri!!, binding.ivUserPhoto)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }

    //check if mobile number is empty or not
    private fun validateUserProfileDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etMobileNumber.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            else -> {
                true
            }
        }
    }

    //hide progress dialog, show a toast, and get back to Dashboard
    fun userProfileUpdateSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this@UserProfileActivity, "profile updated",
            Toast.LENGTH_SHORT
        )
            .show()
        startActivity(Intent(this@UserProfileActivity, AddProductActivity::class.java))
        finish()
    }

    //Once image URL is obtained, pass it to this function, store it as global var,
    //and launch the updateUserProfileDetails
    fun imageUploadSuccess(imageUrl: String) {
        //hideProgressDialog()
        mUserProfileImageUrl = imageUrl
        updateUserProfileDetails()
    }
}