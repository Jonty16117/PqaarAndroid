package com.pqaar.app.pahunchAdmin.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.pqaar.app.utils.DbPaths.DEL_FAIL
import com.pqaar.app.utils.DbPaths.DEL_PASS
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.STATUS

object PahunchAdminRepo {
    private const val TAG = "PahunchAdminRepo"

    private var firebaseDb = FirebaseDatabase.getInstance()

    fun acceptDel(truckNo: String) {
        firebaseDb
            .reference
            .child(LIVE_TRUCK_DATA_LIST)
            .child(truckNo)
            .child(STATUS)
            .setValue(DEL_PASS).addOnSuccessListener {
                Log.d(TAG, "Truck($truckNo) delivery passed successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Truck($truckNo) pass delivery update failed!")
            }
    }

    fun rejectDel(truckNo: String) {
        firebaseDb
            .reference
            .child(LIVE_TRUCK_DATA_LIST)
            .child(truckNo)
            .child(STATUS)
            .setValue(DEL_FAIL).addOnSuccessListener {
                Log.d(TAG, "Truck($truckNo) delivery rejected successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Truck($truckNo) reject delivery update failed!")
            }
    }
}