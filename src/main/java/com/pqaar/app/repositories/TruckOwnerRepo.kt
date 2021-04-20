package com.pqaar.app.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.model.TruckOwner
import com.pqaar.app.utils.DbPaths.FIRST_NAME
import com.pqaar.app.utils.DbPaths.LAST_NAME
import com.pqaar.app.utils.DbPaths.PHONE_NO
import com.pqaar.app.utils.DbPaths.TRUCKS
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.DbPaths.USER_TYPE
import kotlinx.coroutines.tasks.await


/**
 * Things that any truck owner can do:
 * 1) View live routes list
 * 2) View live auction list
 * 3) Book route/s
 * 4) View the current live status of all of its trucks
 * 5) View the history of all of its trucks
 * 6) Submit requests for adding/removing truck/s from its account
 */
object TruckOwnerRepo {
    private const val TAG = "UnionAdminRepository"

    private val firestoreDb = FirebaseFirestore.getInstance()
    private val firebaseDb = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var truckOwnerLiveData: TruckOwner = TruckOwner()
    var TruckOwnerLiveData: MutableLiveData<TruckOwner> = MutableLiveData()

    suspend fun fetchTruckOwner() {
        firestoreDb.collection(USER_DATA)
            .document(auth.uid!!).get()
            .addOnSuccessListener {
                truckOwnerLiveData.FirstName = it.get(FIRST_NAME).toString()
                truckOwnerLiveData.LastName = it.get(LAST_NAME).toString()
                truckOwnerLiveData.PhoneNo = it.get(PHONE_NO).toString()
                truckOwnerLiveData.Trucks = it.get(TRUCKS) as ArrayList<String>
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to fetch truck owner user data, please try again!")
            }.await()
    }
}