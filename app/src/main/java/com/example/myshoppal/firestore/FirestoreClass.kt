package com.example.myshoppal.firestore
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.myshoppal.models.*
import com.example.myshoppal.*

import com.example.myshoppal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

    private val mFirestore = FirebaseFirestore.getInstance()

    //register the user to the cloud
    fun registerUser(activity: RegisterActivity, userInfo:User) {
        //the "users" is collection name. If the collection is already created then it will not create the same one again
        mFirestore.collection(Constants.USERS)
            //define the name of the document to be the User ID
            .document(userInfo.id)
            //define the origin of the fields and how new data behave.
            //fields come from User object, and data get merged
            .set(userInfo, SetOptions.merge())
            //Call userRegistrationSuccess from Register Activity
            .addOnSuccessListener {
                activity.userRegistrationSuccess()
            }
            //if error, hide progress dialog, and log an error
            .addOnFailureListener { e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while registering the user",e)
            }
    }

    fun getCurrentUserID(): String{
        //create instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        //this val will stock currentUser uid if it's not null
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getUserDetails(activity: Activity){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.USERS)
            //we go to the documents of currentuserID
            .document(getCurrentUserID())
            //get try to get something (the doc)
            .get()
            //once we get it, we do something
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                //once we retrieved the document and its field, we convert it into a User model
                val user = document.toObject(User::class.java)!!

                val sharedPreferences = activity.getSharedPreferences(
                    Constants.MYSHOPPAL_PREFERENCES,
                    Context.MODE_PRIVATE
                )

                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    "${user.firstName} ${user.lastName}"
                )
                editor.apply()

                //here we define what to do with the val user depending on the activity we are in
                when (activity){
                    is LoginActivity -> {
                        //activity.hideProgressDialog()
                        activity.userLoggedInSuccess(user)
                    }

                }
            }
            .addOnFailureListener { e->
                //hide progress dialog if there is error and print inn log
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }

                }
                Log.e(activity.javaClass.simpleName, "Error while getting user details",e)
            }
    }



    //get list of product sold of specific user

}