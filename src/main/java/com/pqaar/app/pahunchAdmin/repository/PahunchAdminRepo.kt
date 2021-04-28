package com.pqaar.app.pahunchAdmin.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.pqaar.app.model.LiveTruckDataListItemDTO
import com.pqaar.app.unionAdmin.repository.UnionAdminRepo
import com.pqaar.app.utils.DbPaths.DEL_FAIL
import com.pqaar.app.utils.DbPaths.DEL_PASS
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.STATUS

object PahunchAdminRepo {
    private const val TAG = "PahunchAdminRepo"

    private var firebaseDb = FirebaseDatabase.getInstance()

    val LiveTruckDataList = MutableLiveData<HashMap<String, LiveTruckDataListItemDTO>>()
    private val liveTruckDataList = HashMap<String, LiveTruckDataListItemDTO>()

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

    /**
     *Get Live Truck Data
     */
    suspend fun fetchLiveTruckDataList() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(
                dataSnapshot: DataSnapshot,
                previousChildName: String?,
            ) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                Log.d(TAG, "onChildAdded:" + dataSnapshot.value)

                val changedEntry = dataSnapshot.value as HashMap<*, *>
                val data = HashMap<String, String>()
                changedEntry.forEach {
                    data[it.key.toString()] = it.value.toString()
                }
                liveTruckDataList[dataSnapshot.key!!] = LiveTruckDataListItemDTO(
                    dataSnapshot.key!!, data
                )
                LiveTruckDataList.value = liveTruckDataList
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")
                Log.d(TAG, "onChildChanged: ${dataSnapshot.value}")

                val changedEntry = dataSnapshot.value as HashMap<*, *>
                val data = HashMap<String, String>()
                changedEntry.forEach {
                    data[it.key.toString()] = it.value.toString()
                }
                liveTruckDataList[dataSnapshot.key!!] = LiveTruckDataListItemDTO(
                    dataSnapshot.key!!, data
                )
                LiveTruckDataList.value = liveTruckDataList
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                liveTruckDataList.remove(dataSnapshot.key!!)
                LiveTruckDataList.value = liveTruckDataList
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                //no action
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(
                    TAG,
                    "Retrieving LiveTruckData Failed:onCancelled",
                    databaseError.toException()
                )
            }
        }

        val ref = firebaseDb.reference.child(LIVE_TRUCK_DATA_LIST)
        ref.addChildEventListener(childEventListener)
    }
}