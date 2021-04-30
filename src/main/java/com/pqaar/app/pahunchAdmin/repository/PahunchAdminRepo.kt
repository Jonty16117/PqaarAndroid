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
import com.pqaar.app.model.PahunchTicketDTO
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

    /**
     *Get Issued Pahunch History
     */
    suspend fun fetchPahunchHistory() {
        //val uid = auth.uid

        //For testing purposes
        val uid = "DemoUserPA"

        val ref = firestoreDb.collection(PAHUNCH_ADMIN_RECORDS)
        val query = ref.whereEqualTo("PahunchAdminUId", uid)
        query.get().addOnSuccessListener { documents ->
            pahunchHistory = ArrayList()
            documents!!.forEach { pahunchSnapshot ->
                if (pahunchSnapshot.id != "DummyDoc") {
                    val src = pahunchSnapshot.get("Source").toString()
                    val des = pahunchSnapshot.get("Destination").toString()
                    val delInfo = pahunchSnapshot.get("DelInfo").toString()
                    val truckNo = pahunchSnapshot.get("TruckNo").toString()
                    val status = pahunchSnapshot.get("Status").toString()
                    val timestamp = pahunchSnapshot.get("Timestamp").toString().toLong()
                    val auctionId = pahunchSnapshot.get("AuctionId").toString().toLong()
                    val pahunchTicket = PahunchTicket(
                        Source = src,
                        Destination = des,
                        DeliveryInfo = delInfo,
                        TruckNo = truckNo,
                        Status = status,
                        Timestamp = timestamp,
                        AuctionId = auctionId
                    )
                    pahunchHistory.add(pahunchTicket)
                }
            }
            pahunchHistory = ArrayList(pahunchHistory.sortedWith(compareBy {
                it.Timestamp
            }))
            pahunchHistory.reverse()
            PahunchHistory.postValue(pahunchHistory)
        }.await()
    }

    fun fetchIncomingTrucks() {
        val ref = firestoreDb.collection(LIVE_TRUCK_DATA_LIST)

        ref.addSnapshotListener { snapshots, error ->
            if (error == null) {
                liveTruckDataList = ArrayList()
                snapshots!!.forEach { truckDocument ->
                    if ((truckDocument.get("TruckNo").toString() != "DemoTruckNo") &&
                        truckDocument.get("Active").toString() == "true") {
                        val route = Pair(
                            truckDocument.get("Source").toString(),
                            truckDocument.get("Destination").toString()
                        )
                        val status = truckDocument.get("Status").toString()
                        if (
                            (route.second == UserDestination.value!!) &&
                            (status == "DelInProg")
                        ) {
                            if (truckDocument.id != "DummyDoc") {
                                val liveTruckDataItem = LiveTruckDataItem()
                                liveTruckDataItem.TruckNo = truckDocument.get("TruckNo").toString()
                                liveTruckDataItem.Active = truckDocument.get("Active").toString() == "true"
                                liveTruckDataItem.CurrentListNo =
                                    truckDocument.get("CurrentListNo").toString()
                                liveTruckDataItem.Status = status
                                liveTruckDataItem.Timestamp = truckDocument.get("Timestamp").toString()
                                liveTruckDataItem.AuctionId =
                                    truckDocument.get("AuctionId").toString().toLong()
                                liveTruckDataItem.Owner = (truckDocument.get("Owner") as List<*>)
                                    .zipWithNext { a, b -> Pair(a.toString(), b.toString()) }[0]
                                liveTruckDataItem.Route = route

                                liveTruckDataList.add(liveTruckDataItem)
                            }
                        }
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
