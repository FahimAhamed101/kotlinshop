package com.example.myshoppal.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.SyncStateContract.Constants
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.R
import com.example.myshoppal.databinding.FragmentDashboardBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.Product
import com.example.myshoppal.ui.activities.CartListActivity
import com.example.myshoppal.ui.activities.DashboardActivity
import com.example.myshoppal.ui.activities.ProductDetailsActivity
import com.example.myshoppal.ui.activities.SettingsActivity
import com.example.myshoppal.ui.adapters.DashboardItemsListAdapter
import com.example.myshoppal.ui.adapters.MyProductsListAdapter

class DashboardFragment : BaseFragment(), MenuProvider {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //if we want to use the option menu in fragment we need to add it
        //activity?.addMenuProvider(this)
    }

    //call the method, that call the method to get the product list from firestore
    //everytime we reach that screen
    override fun onResume() {
        super.onResume()
        getDashboardItemsList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val dashboardViewModel =
        //  ViewModelProvider(this).get(DashboardViewModel::class.java)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            this@DashboardFragment,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )


        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
    }

    //launch SettingsActivity when clicked
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            R.id.action_cart -> {
                startActivity(Intent(activity, CartListActivity::class.java))
                return true
            }
        }
        return false
    }

    //display recyclerView if request from firestore is successful
    fun successDashboardItemsList(dashboardItemsList: ArrayList<Product>) {
        hideProgressDialog()
        /*//for now, just log title of each entry
        for (i in dashboardItemsList) {
            Log.i("product name", i.title)
        }*/
        if (dashboardItemsList.size > 0){
            //change visibility
            binding.rvDashboardItems.visibility = View.VISIBLE
            binding.tvNoDashboardItemsFound.visibility = View.GONE

            //set up adapter
            binding.rvDashboardItems.layoutManager = GridLayoutManager(activity, 2)
            binding.rvDashboardItems.setHasFixedSize(true)
            val adapterProducts = DashboardItemsListAdapter(requireActivity(), dashboardItemsList)
            binding.rvDashboardItems.adapter = adapterProducts
            //pass our own onClickListener and send user to product details page
            /*adapterProducts.setOnClickListener(object : DashboardItemsListAdapter.OnClickListener{
                override fun onClick(position: Int, product: Product) {
                    val intent = Intent(context, ProductDetailsActivity::class.java)
                    intent.putExtra(com.example.myshoppal.utils.Constants.EXTRA_PRODUCT_ID, product.product_id)
                    startActivity(intent)
                }
            })*/


        }else{
            //change visibility
            binding.rvDashboardItems.visibility = View.GONE
            binding.tvNoDashboardItemsFound.visibility = View.VISIBLE
        }

    }

    //call the method from Firestore that retrieve product list
    private fun getDashboardItemsList() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardItemsList(this@DashboardFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}