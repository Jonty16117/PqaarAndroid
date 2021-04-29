package com.pqaar.app.pahunchAdmin.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.model.LiveTruckDataItem
import com.pqaar.app.model.LiveTruckDataListItemDTO
import com.pqaar.app.model.PahunchTicket
import com.pqaar.app.unionAdmin.repository.UnionAdminRepo
import com.pqaar.app.utils.DbPaths.DEL_FAIL
import com.pqaar.app.utils.DbPaths.DEL_PASS
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.PAHUNCH_ADMIN_RECORDS
import com.pqaar.app.utils.DbPaths.STATUS
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.DbPaths.USER_TYPE
import kotlinx.coroutines.tasks.await

@SuppressLint("StaticFieldLeak")
object PahunchAdminRepo {
    private const val TAG = "PahunchAdminRepo"

    private val auth = FirebaseAuth.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()
    private var firestoreDb = FirebaseFirestore.getInstance()

    private var liveTruckDataList = ArrayList<LiveTruckDataItem>()
    private var pahunchHistory = ArrayList<PahunchTicket>()

    val LiveTruckDataList = MutableLiveData<ArrayList<LiveTruckDataItem>>()
    val PahunchHistory = MutableLiveData<ArrayList<PahunchTicket>>()
    val UserDestination = MutableLiveData<String>()

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
     *Get Issued Pahunch History
     */
    fun fetchPahunchHistory() {
        //val uid = auth.uid

        //For testing purposes
        val uid = "DemoUserPO"

        val ref = firestoreDb.collection(PAHUNCH_ADMIN_RECORDS).document(uid)
        ref.addSnapshotListener { document, error ->
            if (error == null) {
                pahunchHistory = ArrayList()
                document!!.data!!.forEach { pahunchEntry ->
                    val pahunchTicketListItem = PahunchTicket()
                    pahunchTicketListItem.Timestamp = pahunchEntry.key.toLong()
                    val pahunchEntryInfo = pahunchEntry.value as Map<*, *>
                    pahunchEntryInfo.forEach {
                        when (it.key.toString()) {
                            "TruckNo" -> {
                                pahunchTicketListItem.TruckNo = it.value.toString()
                            }
                            "Status" -> {
                                pahunchTicketListItem.Status = it.value.toString()
                            }
                            "DelInfo" -> {
                                pahunchTicketListItem.DeliveryInfo = it.value.toString()
                            }
                            "Source" -> {
                                pahunchTicketListItem.Source = it.value.toString()
                            }
                            "Destination" -> {
                                pahunchTicketListItem.Destination = it.value.toString()
                            }
                        }
                    }
                    pahunchHistory.add(pahunchTicketListItem)
                }
                pahunchHistory = ArrayList(pahunchHistory.sortedWith(compareBy {
                    it.Timestamp
                }))
                pahunchHistory.reverse()
                PahunchHistory.postValue(pahunchHistory)
            }
        }
    }

    suspend fun fetchUserDestination() {
        //val uid = auth.uid

        //For testing purposes
        val uid = "DemoUserPO"

        firestoreDb
            .collection(USER_DATA)
            .document(uid)
            .get()
            .addOnSuccessListener {
                UserDestination.postValue(it.getString(USER_TYPE))
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to fetch the pahunch admin's destination!")
            }
            .await()
    }

    fun fetchIncomingTrucks() {
        val ref = firestoreDb.collection(LIVE_TRUCK_DATA_LIST)

        ref.addSnapshotListener { snapshots, error ->
            if (error == null) {
                liveTruckDataList = ArrayList()
                snapshots!!.forEach { truckDocumentSnapshot ->
                    Log.d(TAG, "Truck fetchted: ${truckDocumentSnapshot}")
                    val route = Pair(
                        truckDocumentSnapshot.get("Source").toString(),
                        truckDocumentSnapshot.get("Destination").toString()
                    )
                    val status = truckDocumentSnapshot.get("Status").toString()
                    if ((truckDocumentSnapshot.id != "DemoTruckNo") &&
                        (route.second == UserDestination.value!!) &&
                        (status == "DelInProg")
                    ) {
                        val liveTruckDataItem = LiveTruckDataItem()
                        liveTruckDataItem.TruckNo = truckDocumentSnapshot.id
                        liveTruckDataItem.CurrentListNo = truckDocumentSnapshot.get("CurrentListNo").toString()
                        liveTruckDataItem.Status = truckDocumentSnapshot.get("Status").toString()
                        liveTruckDataItem.Timestamp = truckDocumentSnapshot.get("Timestamp").toString()
                        liveTruckDataItem.Owner = (truckDocumentSnapshot.get("Owner") as List<*>)
                            .zipWithNext { a, b -> Pair(a.toString(), b.toString()) }[0]
                        liveTruckDataItem.Route = Pair(
                            truckDocumentSnapshot.get("Source").toString(),
                            truckDocumentSnapshot.get("Destination").toString()
                        )
                        liveTruckDataList.add(liveTruckDataItem)
                    }
                }
                LiveTruckDataList.postValue(liveTruckDataList)
            }
        }
    }
}

/*

//Get all the ticket names issued for this truck
val pahunchTickets = ArrayList<Long>()
truckDocument.data.forEach {
    if (it.key != "CurrentListNo" &&
        it.key != "Owner" &&
        it.key != "Route"
    ) {
        pahunchTickets.add(it.key.split("-")[1].toLong())
    }
}

//Get the most recent pahunch ticket
val mostRecentTicket = truckDocument
    .get("Pahunch-${pahunchTickets.sortedDescending()[0]}") as Map<*, *>

var src = ""
var des = ""
var status = ""
var timestamp = ""
mostRecentTicket.forEach {
    when (it.key.toString()) {
        "Source" -> {
            src = it.value.toString()
        }
        "Destination" -> {
            des = it.value.toString()
        }
        "DeliveryStatus" -> {
            status = it.value.toString()
        }
        "Timestamp" -> {
            timestamp = it.value.toString()
        }
    }
}*/
