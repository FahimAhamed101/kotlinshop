package com.example.myshoppal.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.myshoppal.R
import com.example.myshoppal.databinding.DialogProgressBinding


open class BaseFragment : Fragment() {
    private lateinit var mProgressDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_base, container, false)
    }

    //lets you display progress dialog in fragment
    fun showProgressDialog(text: String){
        mProgressDialog = Dialog(requireActivity())
        val dialbinding: DialogProgressBinding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog.setContentView(dialbinding.root)
        dialbinding.tvProgressText.text = text
        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)

        mProgressDialog.show()
    }

    //hide the progressDialog
    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

}