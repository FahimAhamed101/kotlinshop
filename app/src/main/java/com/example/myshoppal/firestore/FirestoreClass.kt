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
    //upload the product object to the cloud
    fun uploadProductDetails(activity: AddProductActivity, productInfo: Product){
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
            .addOnFailureListener { e->
                Log.e(activity.javaClass.simpleName,
                    "error while uploading product details",
                    e
                )
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

                when(activity){
                    is CartListActivity ->{
                        activity.successProductsListFromFirestore(productsList)
                    }

                }

            }
            .addOnFailureListener {
                when(activity){
                    is CartListActivity ->{
                        activity.hideProgressDialog()
                        Log.e("Get product list", "Error while getting all product list")
                    }

                }

            }
    }

    //get product details from id
    fun getProductDetails(activity: ProductDetailsActivity, productId: String){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.PRODUCTS)
            //access the document with the same productId we are passing
            .document(productId)
            //get it
            .get()
            //create a Product object from the document we got and,
            //send it to the function that will display it in the layout
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                val product = document.toObject(Product::class.java)
                if (product != null) {
                    activity.productDetailsSuccess(product)
                }
            }
            //log the error
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while getting the product details", e)
            }
    }

    //delete product from user's product list
    fun deleteProduct (fragment: ProductsFragment, productId: String){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.PRODUCTS)
            //access the document with the same productId we are passing
            .document(productId)
            //delete that document
            .delete()
            //call productDeleteSuccess if successful
            .addOnSuccessListener {
                fragment.productDeleteSuccess()
            }
            //log the error
            .addOnFailureListener {
                fragment.hideProgressDialog()
                Log.e(
                    fragment.requireActivity().javaClass.simpleName,
                    "error while deleting the product"
                )
            }
    }

    //delete product from user cart
    fun removeItemFromCart(context: Context, cart_id:String){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.CART_ITEMS)
            //access the document with the same cartId we are passing
            .document(cart_id)
            //delete that document
            .delete()
            //call itemRemovedSuccess if successful
            .addOnSuccessListener {
                when (context){
                    is CartListActivity ->{
                        context.itemRemovedSuccess()
                    }

                }
            }
            //log the error
            .addOnFailureListener {
                when(context){
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }

                }
                Log.e(
                    context.javaClass.simpleName,
                    "error while deleting the product"
                )
            }
    }

    //update quantity of cart item
    fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.CART_ITEMS)
            //access the document with the same productId we are passing
            .document(cart_id)
            //update document with prepared hashmap (key+value)
            .update(itemHashMap)
            //call itemUpdateSuccess if successful
            .addOnSuccessListener {
                when(context){
                    is CartListActivity -> {
                        context.itemUpdateSuccess()
                    }

                }
            }
            //log the error
            .addOnFailureListener {
                when(context){
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }

                }
                Log.e(
                    context.javaClass.simpleName,
                    "error while deleting the product"
                )
            }
    }

    //get all product from all users
    fun getDashboardItemsList (fragment: DashboardFragment){
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

    //get cart items from specific user
    fun getCartList(activity: Activity){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.CART_ITEMS)
            //access the document which has the field USER_ID with the corresponding user ID
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            //get the fields of the doc
            .get()
            .addOnSuccessListener { document ->
                //create log with the list of product
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                //will store the cartItems in a array list
                val cartItemlist: ArrayList<CartItem> = ArrayList()

                //loop through all the fields while assigning them in a proper CartItem object,
                //add id of document to cartItem id, and add the cartItem to the array list
                for (i in document.documents){
                    val cartItem = i.toObject(CartItem::class.java)!!
                    cartItem.id = i.id
                    cartItemlist.add(cartItem)
                }

                //pass the list to specific function of specific activity
                when(activity){
                    is CartListActivity -> {
                        //when activity = CartListActivity, pass the arrayList to function successCart...
                        activity.successCartItemsList(cartItemlist)
                    }

                }
            }
            //if error, hide progress dialog, and log an error
            .addOnFailureListener {
                    e->
                when(activity){
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "error while getting the cart list item", e)
                    }

                }

            }
    }

    //add a cart object to the CART_ITEMS collection
    fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.CART_ITEMS)
            //Define name for the document.  empty argument = random generated by google
            .document()
            //define the origin of the fields and how new data behave.
            //fields come from CartItem object, and data get merged
            .set(addToCart, SetOptions.merge())
            //call the function that display a toast
            .addOnSuccessListener {
                activity.addToCartSuccess()
            }
            //log the error
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "error while creating document for cart item"
                )
            }
    }
    //check if item is already in cart
    fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String){
        //we pass name of collection we want data from
        mFirestore.collection(Constants.CART_ITEMS)
            //access the document which has the field USER_ID with the corresponding user ID
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            //access the document which has the field PRODUCT_ID with the corresponding user ID
            .whereEqualTo(Constants.PRODUCT_ID,productId)
            //get the fields of the doc
            .get()
            //log and change buttons displayed
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                //if product is already in cart, change addToCart btn to goToCart
                if (document.documents.size > 0){
                    activity.productExistsInCart()
                    //else leave btn as is
                }else{
                    activity.hideProgressDialog()
                }
            }
            //log the error
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "error while checking existing cart list"
                )
            }
    }
}











    //get list of product sold of specific user

