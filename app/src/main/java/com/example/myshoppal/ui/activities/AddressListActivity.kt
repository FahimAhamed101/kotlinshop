package com.example.myshoppal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myshoppal.R
import com.example.myshoppal.databinding.ActivityAddressListBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.Address
import com.example.myshoppal.ui.adapters.AddressListAdapter
import com.example.myshoppal.utils.Constants
import com.example.myshoppal.utils.SwipeToDeleteCallback
import com.example.myshoppal.utils.SwipeToEditCallback

class AddressListActivity:BaseActivity() {
    lateinit var binding: ActivityAddressListBinding
    private var mSelectAddress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        binding.tvAddAddress.setOnClickListener{
            val intent= Intent(this@AddressListActivity, AddEditAddressActivity::class.java)
            startActivity(intent)
        }

        //if there is extra info, put it in the global var
        if(intent.hasExtra(Constants.EXTRA_SELECT_ADDRESS)){
            mSelectAddress = intent.getBooleanExtra(Constants.EXTRA_SELECT_ADDRESS, false)
        }

        //if there is extra info, change title name
        if(mSelectAddress){
            binding.tvTitle.text = resources.getString(R.string.title_select_address)
        }
    }

    //call getAddressList when we get (back) on the screen
    override fun onResume() {
        super.onResume()
        getAddressList()
    }

    private fun setupActionBar(){

        setSupportActionBar(binding.toolbarAddressListActivity)
        val actionBar = supportActionBar
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
        }

        binding.toolbarAddressListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    //display recyclerView if request from firestore is successful
    fun successAddressListFromFirestore(addressList: ArrayList<Address>){
        hideProgressDialog()
        /*for now store address in logcat
        for (i in addressList){
            Log.i("name and address", "${i.name}::${i.address}")
        }*/

        //check if there are addresses
        if (addressList.size > 0){
            //change visibility of layout element
            binding.rvAddressList.visibility = View.VISIBLE
            binding.tvNoAddressFound.visibility = View.GONE

            //setup adapter
            binding.rvAddressList.layoutManager = LinearLayoutManager(this@AddressListActivity)
            binding.rvAddressList.setHasFixedSize(true)
            val addressAdapter = AddressListAdapter(this@AddressListActivity, addressList, mSelectAddress)
            binding.rvAddressList.adapter = addressAdapter

            //check if there is info in mSelected, to know if we simply selecting an address, or editing
            if(!mSelectAddress){
                //setup swipe to edit function
                val editSwipeHandler = object: SwipeToEditCallback(this){
                    //here we say what we want to do when swiping
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val adapter = binding.rvAddressList.adapter as AddressListAdapter
                        adapter.notifyEditItem(this@AddressListActivity,viewHolder.adapterPosition)
                    }
                }

                //attach the editSwipeHandler to ItemTouchHelper so it can actually work
                val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(binding.rvAddressList)

                //setup swipe to delete function
                val deleteSwipeHandler = object: SwipeToDeleteCallback(this){
                    //here we say what we want to do when swiping
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        showProgressDialog(resources.getString(R.string.please_wait))
                        FirestoreClass().deleteAddress(this@AddressListActivity,addressList[viewHolder.adapterPosition].id)
                    }
                }

                //attach the deleteSwipeHandler to ItemTouchHelper so it can actually work
                val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(binding.rvAddressList)
            }

            //else hide recycler, show text
        }else{
            binding.rvAddressList.visibility = View.GONE
            binding.tvNoAddressFound.visibility = View.VISIBLE
        }
    }

    //call firestore function that get the addresslist
    private fun getAddressList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAddressesList(this@AddressListActivity)
    }

    //display toast when address is successfully deleted and download latest adresslist
    fun deleteAddressSuccess(){
        hideProgressDialog()

        Toast.makeText(this@AddressListActivity,
            resources.getString(R.string.msg_address_deleted_successfully),
            Toast.LENGTH_SHORT
        ).show()

        getAddressList()
    }

}