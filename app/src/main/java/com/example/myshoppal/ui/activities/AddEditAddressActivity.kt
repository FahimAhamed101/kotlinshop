package com.example.myshoppal.ui.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityAddEditAddressBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.Address
import com.example.myshoppal.utils.Constants

class AddEditAddressActivity : BaseActivity() {
    lateinit var binding: ActivityAddEditAddressBinding
    private var mAddressDetails: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        binding.btnSubmitAddress.setOnClickListener { saveAddressToFirestore() }
        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            //check if radio button other is checked, and then change visibility
            if(checkedId == R.id.rb_other){
                binding.tilOtherDetails.visibility = View.VISIBLE
            }else{
                binding.tilOtherDetails.visibility = View.GONE
            }
        }

        //check if we got some extra info
        if(intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS)){
            //if yes put it in global var
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)
        }

        //check if mAddressDetails has info
        if (mAddressDetails != null){
            //if yes, change UI and load those details
            if (mAddressDetails!!.id.isNotEmpty()){
                //change UI
                binding.tvTitle.text = resources.getString(R.string.title_edit_address)
                binding.btnSubmitAddress.text = resources.getString(R.string.btn_lbl_update)

                //load details
                binding.etFullName.setText(mAddressDetails?.name)
                binding.etPhoneNumber.setText(mAddressDetails?.mobileNumber)
                binding.etAddress.setText(mAddressDetails?.address)
                binding.etZipcode.setText(mAddressDetails?.zipcode)
                binding.etAdditionalNote.setText(mAddressDetails?.additionalNote)

                //change which radio button is check
                when(mAddressDetails?.type){
                    Constants.HOME -> {
                        binding.rbHome.isChecked = true
                    }
                    Constants.OFFICE -> {
                        binding.rbOffice.isChecked = true
                    }
                    else -> {
                        binding.rbOther.isChecked = true
                        binding.tilOtherDetails.visibility = View.VISIBLE
                        binding.etOtherDetails.setText(mAddressDetails?.otherDetails)
                    }
                }
            }
        }
    }

    private fun setupActionBar(){

        setSupportActionBar(binding.toolbarAddEditAddressActivity)
        val actionBar = supportActionBar
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
        }

        binding.toolbarAddEditAddressActivity.setNavigationOnClickListener { onBackPressed() }
    }

    //check if user didn't input random crap and trim it
    private fun validateData(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etFullName.text.toString().trim { it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_full_name), true)
                false
            }

            TextUtils.isEmpty(binding.etPhoneNumber.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_phone_number), true)
                false
            }

            TextUtils.isEmpty(binding.etAddress.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_address), true)
                false
            }

            TextUtils.isEmpty(binding.etZipcode.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_zipcode), true)
                false
            }

            binding.rbOther.isChecked && TextUtils.isEmpty(binding.etZipcode.text.toString().trim{it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_confirm_zipcode), true)
                false
            }
            else -> {
                true
            }
        }
    }

    //save data enter by user in val, validate it
    private fun saveAddressToFirestore(){
        val fullName: String = binding.etFullName.text.toString().trim { it <= ' '}
        val phoneNumber: String = binding.etPhoneNumber.text.toString().trim{it <= ' '}
        val address: String = binding.etAddress.text.toString().trim{it <= ' '}
        val zipcode: String = binding.etZipcode.text.toString().trim{it <= ' '}
        val additionalNote: String = binding.etAdditionalNote.text.toString().trim{it <= ' '}
        val otherDetails: String= binding.etOtherDetails.text.toString().trim{it <= ' '}

        //check if data is valid
        if (validateData()){
            showProgressDialog(resources.getString(R.string.please_wait))
            //define type depending on radio button
            val addressType: String = when{
                binding.rbHome.isChecked -> {
                    Constants.HOME
                }
                binding.rbOffice.isChecked -> {
                    Constants.OFFICE
                }
                else -> {
                    Constants.OTHER
                }
            }

            //the final address model that should be send to Firestore
            val addressModel = Address(
                FirestoreClass().getCurrentUserID(),
                fullName,
                phoneNumber,
                address,
                zipcode,
                additionalNote,
                addressType,
                otherDetails
            )

            //check if it's an edit or a new entry, based mAddressDetails's content
            if(mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()){
                FirestoreClass().updateAddress(this, addressModel, mAddressDetails!!.id)
            }else{
                //add newly created addressModel to collection
                FirestoreClass().addAddress(this, addressModel)
            }

        }
    }

    //display toast when address is added
    fun addUpdateAddressSuccess(){
        hideProgressDialog()

        //val that change its string depending on if mAddressDetails is empty or not
        val notifySuccessMessage: String = if(mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()) {
            resources.getString(R.string.msg_address_updated_successfully)
        }else {
            resources.getString(R.string.success_msg_address_added_successfully)
        }
        //we use notifySuccessMessage to display the string
        Toast.makeText(this@AddEditAddressActivity,
            notifySuccessMessage,
            Toast.LENGTH_SHORT
        ).show()

        //close current screen
        finish()
    }

}