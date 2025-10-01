package com.example.myshoppal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.R
import com.example.myshoppal.databinding.FragmentSoldProductsBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.Product
import com.example.myshoppal.models.SoldProduct
import com.example.myshoppal.ui.adapters.MyProductsListAdapter
import com.example.myshoppal.ui.adapters.SoldProductListAdapter


class SoldProductsFragment : BaseFragment() {
    private var _binding: FragmentSoldProductsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentSoldProductsBinding.inflate(inflater, container, false)

        return binding.root
    }

    //call firestore function that get the soldList
    private fun getSoldProductsList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getSoldProductsList(this@SoldProductsFragment)

    }

    //display recyclerView if request from firestore is successful
    fun successSoldProductsList(soldProductsList: ArrayList<SoldProduct>) {
        hideProgressDialog()

        if (soldProductsList.size > 0) {
            //change visibility
            binding.rvSoldOut.visibility = View.VISIBLE
            binding.tvNoSoldProductFound.visibility = View.GONE

            //set up adapter
            binding.rvSoldOut.layoutManager = LinearLayoutManager(activity)
            binding.rvSoldOut.setHasFixedSize(true)
            val adapterProducts =
                SoldProductListAdapter(requireActivity(), soldProductsList)
            binding.rvSoldOut.adapter = adapterProducts
        } else {
            //change visibility
            binding.rvSoldOut.visibility = View.GONE
            binding.tvNoSoldProductFound.visibility = View.VISIBLE
        }
    }

    //call getSoldProductList each time u get here
    override fun onResume() {
        super.onResume()
        getSoldProductsList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}