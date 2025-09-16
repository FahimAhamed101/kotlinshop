package com.example.myshoppal.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.R
import com.example.myshoppal.databinding.FragmentProductsBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.Product
import com.example.myshoppal.ui.activities.AddProductActivity
import com.example.myshoppal.ui.adapters.MyProductsListAdapter

class ProductsFragment : BaseFragment(), MenuProvider {

    private var _binding: FragmentProductsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //if we want to use the option menu in fragment we need to add it
        //activity?.addMenuProvider(this@ProductsFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val homeViewModel =
        //  ViewModelProvider(this).get(HomeViewModel::class.java)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this@ProductsFragment, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    //inflate the menu we want to see
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_product_menu, menu)
    }

    //launch AddProductActivity when clicked
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_add_product -> {
                startActivity(Intent(activity, AddProductActivity::class.java))
                return true
            }
        }
        return false
    }

    //show alertDialog for deletion when u click icon
    fun deleteProduct(productID: String) {
        /*Toast.makeText(requireActivity(), "will delete product $productID", Toast.LENGTH_SHORT)
            .show()*/
        showAlertDialogToDeleteProduct(productID)
    }

    //display toast if deletion successful
    fun productDeleteSuccess() {
        hideProgressDialog()
        Toast.makeText(
            requireActivity(),
            resources.getString(R.string.product_delete_success_msg),
            Toast.LENGTH_SHORT
        ).show()

        //show newest list of product
        getProductListFromFirestore()
    }

    //show confirmation dialog dor deletion
    private fun showAlertDialogToDeleteProduct(productID: String){
        val builder = AlertDialog.Builder(requireActivity())
        //set title
        builder.setTitle(resources.getString(R.string.delete_dialog_title))
        //set message
        builder.setMessage(resources.getString(R.string.delete_dialog_message))
        //set icon
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //if yes, show progressDialog, call deleteProduct from Firestore, and dismiss
        builder.setPositiveButton(resources.getString(R.string.yes)){ dialogInterface, _ ->
            showProgressDialog(resources.getString(R.string.please_wait))
           // FirestoreClass().deleteProduct(this, productID)
            dialogInterface.dismiss()
        }

        //if no, dismiss
        builder.setNegativeButton(resources.getString(R.string.no)){ dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        //create alertDialog
        val alertDialog: AlertDialog = builder.create()
        //Set other dialog properties and show it
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    //display recyclerView if request from firestore is successful
    fun successProductsListFromFirestore(productsList: ArrayList<Product>) {
        hideProgressDialog()

        /*for now, just log title of each entry
        for (i in productsList){
            Log.i("product name", i.title)
        }*/

        if (productsList.size > 0) {
            //change visibility
            binding.rvMyProductItems.visibility = View.VISIBLE
            binding.tvNoProductsFound.visibility = View.GONE

            //set up adapter
            binding.rvMyProductItems.layoutManager = LinearLayoutManager(activity)
            binding.rvMyProductItems.setHasFixedSize(true)
            val adapterProducts =
                MyProductsListAdapter(requireActivity(), productsList, this@ProductsFragment)
            binding.rvMyProductItems.adapter = adapterProducts
        } else {
            //change visibility
            binding.rvMyProductItems.visibility = View.GONE
            binding.tvNoProductsFound.visibility = View.VISIBLE
        }
    }

    //call firestore function that get the productlist
    private fun getProductListFromFirestore() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getProductsList(this)
    }

    override fun onResume() {
        super.onResume()
        getProductListFromFirestore()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}