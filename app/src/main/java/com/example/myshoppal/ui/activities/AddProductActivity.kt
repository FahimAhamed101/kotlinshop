package com.example.myshoppal.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
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
import com.example.myshoppal.databinding.ActivityAddProductBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.Product
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.GlideLoader
import java.io.IOException


class AddProductActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddProductBinding
    private var mSelectedImageUri: Uri? = null //uri is address on local device
    private var mUserProductImageUrl: String ="" //url is address in cloud storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        binding.ivAddUpdateProduct.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }

    //can go back by pressing black arrow
    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarAddProductActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
        }

        binding.toolbarAddProductActivity.setNavigationOnClickListener { onBackPressed() }
    }

    //make our view clickable, and do stuff when clicked
    override fun onClick(v: View?) {
        //check if view not null
        if (v != null) {
            when (v.id) {
                //if we click the small photo icon, check if it has permission to read storage,
                //and call the function that lets you choose the image in gallery
                R.id.iv_add_update_product -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        Constants.showImageChooser(pickImage)
                        //else, ask for permission
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }

                //if none of the fields are empty, show snackbar and send to cloud
                R.id.btn_submit ->{
                    if (validateProductDetails()){
                        uploadProductImage()
                        showErrorSnackBar("your product exist", false)
                    }
                }
            }
        }
    }

    //is called when a request fpr permission is asked, will execute code depending on the permission asked
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


    //this val is used for the onRequestPermissionsResult, it pick image from gallery and set it as thumbnail
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //if data not null, change the icon of photo to edit, and load the new image
                if (result.data != null) {
                    binding.ivAddUpdateProduct.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_vector_edit
                        )
                    )
                    //load image with glider
                    try {
                        //we save uri of selected image
                        mSelectedImageUri = result.data!!.data!!
                        //we use Uri to replace image
                        GlideLoader(this).loadUserPicture(
                            mSelectedImageUri!!,
                            binding.ivProductImage
                        )
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


    //check that none of the fields are empty
    private fun validateProductDetails(): Boolean{
        return when {
            mSelectedImageUri== null -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }

            TextUtils.isEmpty(binding.etProductTitle.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
                false
            }

            TextUtils.isEmpty(binding.etProductPrice.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }

            TextUtils.isEmpty(binding.etProductDescription.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_description), true)
                false
            }

            TextUtils.isEmpty(binding.etProductQuantity.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_quantity), true)
                false
            }
            else -> {
                true
            }
        }

    }

    private fun uploadProductImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().uploadImageToCloudStorage(this, mSelectedImageUri, Constants.PRODUCT_IMAGE)
    }

    //Once image URL is obtained, pass it to this function, store it as global var,
    //and launch the uploadProductDetails
    fun imageUploadSuccess(imageUrl:String) {
        //hideProgressDialog()
        //showErrorSnackBar("product is uploaded successfully image URL $imageUrl", false)
        mUserProductImageUrl = imageUrl

        uploadProductDetails()
    }

    //create a Product object and pass it to Firestore's uploadProductDetails for upload
    private fun uploadProductDetails() {
        //get username from constant MYSHOPPAL_PREFERENCES
        val username = this.getSharedPreferences(
            Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_USERNAME,"")!!

        val product = Product(
            FirestoreClass().getCurrentUserID(),
            username,
            binding.etProductTitle.text.toString().trim { it <= ' ' },
            binding.etProductPrice.text.toString().trim { it <= ' ' },
            binding.etProductDescription.text.toString().trim { it <= ' ' },
            binding.etProductQuantity.text.toString().trim { it <= ' ' },
            mUserProductImageUrl
        )

        FirestoreClass().uploadProductDetails(this, product)
    }

    //hide progress dialog, show a toast, and get back to product screen
    fun productUploadSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this@AddProductActivity, "product uploaded",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }
}