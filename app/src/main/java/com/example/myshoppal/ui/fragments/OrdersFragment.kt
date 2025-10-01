package com.example.myshoppal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshoppal.R
import com.example.myshoppal.databinding.FragmentOrdersBinding
import com.example.myshoppal.firestore.FirestoreClass
import com.example.myshoppal.models.Order
import com.example.myshoppal.ui.adapters.MyOrdersListAdapter
import com.example.myshoppal.ui.adapters.MyProductsListAdapter

class OrdersFragment : BaseFragment() {

    private var _binding: FragmentOrdersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val notificationsViewModel =
        //  ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
        return root
    }

    override fun onResume(){
        super.onResume()
        getMyOrdersList()
    }

    //get the order list through Firestore and populate recyclerview
    fun populateOrdersListInUI(ordersList:ArrayList<Order>){
        hideProgressDialog()
        if (ordersList.size > 0) {
            //change visibility
            binding.rvMyOrderItems.visibility = View.VISIBLE
            binding.tvNoOrdersFound.visibility = View.GONE

            //set up adapter
            binding.rvMyOrderItems.layoutManager = LinearLayoutManager(activity)
            binding.rvMyOrderItems.setHasFixedSize(true)
            val adapterOrders =
                MyOrdersListAdapter(requireActivity(), ordersList)
            binding.rvMyOrderItems.adapter = adapterOrders
        } else {
            //change visibility
            binding.rvMyOrderItems.visibility = View.GONE
            binding.tvNoOrdersFound.visibility = View.VISIBLE
        }
    }

    //call Firestore function that'll get the orderlist
    fun getMyOrdersList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getMyOrdersList(this@OrdersFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}