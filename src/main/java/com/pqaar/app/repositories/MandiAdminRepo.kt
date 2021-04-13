package com.pqaar.app.repositories

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pqaar.app.model.PropRoutesListItem
import com.pqaar.app.utils.DbPaths.MANDI_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import com.pqaar.app.utils.TimeConversions.TimestampToMillis
import kotlinx.coroutines.tasks.await


@SuppressLint("StaticFieldLeak")
object MandiAdminRepo {
    private val TAG = "MandiAdminRepo"


    private var firebaseAuth = FirebaseAuth.getInstance()
    private var firestoreDb = FirebaseFirestore.getInstance()

    /**
     * Get the mandi this user is associated with
     */
    suspend fun getUsersMandi(): String {
        var result = ""
        firestoreDb.collection(USER_DATA)
            .document(firebaseAuth.uid.toString()).get()
            .addOnSuccessListener {
                result = it.data!!["mandi"].toString()
            }.addOnFailureListener {
                Log.d(TAG, "Fetching user's mandi failed!")
            }.await()
        return result
    }

    /**
     * For performance:-
     * Do not call this function more than once per second, keep a delay of at-least
     * 2 seconds before calling this again.
     */
    fun addRoute(mandiSrc: String, propRoutesList: ArrayList<PropRoutesListItem>) {
        val dataToUpload = HashMap<String, String>()
        propRoutesList.forEach{
            dataToUpload[it.des] = it.req
        }
        val currTime = CurrDateTimeInMillis().toString()
        dataToUpload["Timestamp"] = currTime

        val mandiCollec = firestoreDb.collection(MANDI_ROUTES_LIST)
            .document(mandiSrc)

        mandiCollec.set(mapOf(currTime to dataToUpload),
            SetOptions.merge())
    }
}