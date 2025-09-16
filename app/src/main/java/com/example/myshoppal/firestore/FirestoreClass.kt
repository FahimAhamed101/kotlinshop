package com.example.myshoppal.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.myshoppal.models.*
import com.example.myshoppal.ui.activities.*
import com.example.myshoppal.ui.fragments.DashboardFragment

import com.example.myshoppal.ui.fragments.ProductsFragment
import com.example.myshoppal.ui.fragments.SoldProductsFragment
import com.example.myshoppal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

    private val mFirestore = FirebaseFirestore.getInstance()

    //register the user to the cloud
    fun registerUser(activity: RegisterActivity, userInfo: User) {
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
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while registering the user", e)
            }
    }

    fun getCurrentUserID(): String {
        //create instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        //this val will stock currentUser uid if it's not null
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    //download user info details
    fun getUserDetails(activity: Activity) {
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
                when (activity) {
                    is LoginActivity -> {
                        //activity.hideProgressDialog()
                        activity.userLoggedInSuccess(user)
                    }


                }
            }
            .addOnFailureListener { e ->
                //hide progress dialog if there is error and print inn log
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }


                }
                Log.e(activity.javaClass.simpleName, "Error while getting user details", e)
            }
    }

    //update user profile with new data
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {

        mFirestore.collection(Constants.USERS)
            //access the document with the same userID we are passing
            .document(getCurrentUserID())
            //update document with prepared hashmap (key+value)
            .update(userHashMap)
            //call userProfileUpdateSuccess if successfull (toast, back to dashboard, hide progress)
            .addOnSuccessListener {
                when (activity) {
                    is UserProfileActivity -> {
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            //log error
            .addOnFailureListener { e ->
                when (activity) {
                    is UserProfileActivity -> {
                        //hide progress dialog if there is error and print it in log
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "error while updating details", e)
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileUri: Uri?, imageType: String) {
        //sRef is just the name of the file on the cloud: user image+time+file ext
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(activity, imageFileUri)
        )
        //now we put it online with the put method
        sRef.putFile(imageFileUri!!).addOnSuccessListener { taskSnapshot ->
            //log the URL
            Log.e("Firebase image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            //get the downloadable Uri and pass it to imageUploadSuccess
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.e("Downloadable image URl", uri.toString())
                when (activity) {
                    is UserProfileActivity -> {
                        activity.imageUploadSuccess(uri.toString())
                    }

                    is AddProductActivity -> {
                        activity.imageUploadSuccess(uri.toString())
                    }
                }
            }
        }

            .addOnFailureListener { exception ->
                //hide progressdialog and print in log
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }

                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, exception.message, exception)
            }
    }

    //upload the product object to the cloud
    fun uploadProductDetails(activity: AddProductActivity, productInfo: Product) {
        //define the name of collection based on constant
        mFirestore.collection(Constants.PRODUCTS)
            //Define name for the document.  empty argument = random generated by google
            .document()
            //define the origin of the fields and how new data behave.
            //fields come from Product object, and data get merged
            .set(productInfo, SetOptions.merge())
            //call productUploadSuccess when data is done uploading
            .addOnSuccessListener {
                activity.productUploadSuccess()
            }
            //if error, hide progress dialog, and log an error
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "error while uploading product details",
                    e
                )
            }
    }







    //get all product from all users
    fun getDashboardItemsList(fragment: DashboardFragment) {
        //we pass name of collection we want data from
        mFirestore.collection(Constants.PRODUCTS)
            //get the fields of the doc
            .get()
            //if successful
            .addOnSuccessListener { document ->
                //create log with the list of product
                Log.e("products list", document.documents.toString())
                //will store the products in a array list
                val productsList: ArrayList<Product> = ArrayList()

                //loop through all the fields while assigning them in a proper Product object,
                //add id of document to product_id, and add the product to the array list
                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id
                    productsList.add(product)
                }

                //pass the arraylist we populated to successDashboardItemsList
                fragment.successDashboardItemsList(productsList)
            }
            //if failed,
            .addOnFailureListener {
                //hide progressdialog and log error
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "error while getting dashboard items list")
            }
    }
    //get product list from specific user
    fun getProductsList(fragment: Fragment) {
        //we pass name of collection we want data from
        mFirestore.collection(Constants.PRODUCTS)
            //access the document which has the field USER_ID with the corresponding user ID
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            //get the fields of the doc
            .get()
            .addOnSuccessListener { document ->
                //create log with the list of product
                Log.e("products list", document.documents.toString())

                //will store the products in a array list
                val productsList: ArrayList<Product> = ArrayList()

                //loop through all the fields while assigning them in a proper Product object,
                //add id of document to product_id, and add the product to the array list
                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id
                    productsList.add(product)
                }

                //pass the product list to specific function of specific fragment
                when(fragment){
                    //when fragment = ProductsFragment, pass the arrayList to function successPro...
                    is ProductsFragment ->{
                        fragment.successProductsListFromFirestore(productsList)
                    }

                }
            }
    }

    //get product list from everyone
    fun getAllProductsList(activity: Activity){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.PRODUCTS)
            //get the fields of the doc
            .get()
            .addOnSuccessListener { document ->
                //create log with the list of product
                Log.e("products list", document.documents.toString())
                //will store the products in a array list
                val productsList: ArrayList<Product> = ArrayList()

                //loop through all the fields while assigning them in a proper Product object,
                //add id of document to product_id, and add the product to the array list
                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id
                    productsList.add(product)
                }



            }
            .addOnFailureListener {


            }
    }
}











    //get list of product sold of specific user

