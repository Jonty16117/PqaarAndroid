package com.pqaar.app.pahunchAdmin.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.model.LiveTruckDataItem
import com.pqaar.app.model.LiveTruckDataListItemDTO
import com.pqaar.app.model.PahunchTicket
import com.pqaar.app.model.PahunchTicketDTO
import com.pqaar.app.utils.DbPaths.DEL_FAIL
import com.pqaar.app.utils.DbPaths.DEL_PASS
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.PAHUNCH_ADMIN_RECORDS
import com.pqaar.app.utils.DbPaths.STATUS
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.DbPaths.USER_TYPE
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@SuppressLint("StaticFieldLeak")
object PahunchAdminRepo {
    private const val TAG = "PahunchAdminRepo"

    private val demoUserUId = "DemoUserPA"
    private val auth = FirebaseAuth.getInstance()
    private var firestoreDb = FirebaseFirestore.getInstance()

    private var liveTruckDataList = ArrayList<LiveTruckDataItem>()
    private var liveTruckDataListIndexMap = HashMap<String, Int>()
    private var pahunchHistory = ArrayList<PahunchTicket>()

    val LiveTruckDataList = MutableLiveData<ArrayList<LiveTruckDataItem>>()
    val PahunchHistory = MutableLiveData<ArrayList<PahunchTicket>>()
    val UserDestination = MutableLiveData<String>()

    suspend fun acceptDel(truckNo: String, delInfo: String) {
        var noException = true
        var pahunchTicketGenerated = false
        try {
            //add pahunch ticket entry in pahunch admin records
            val pahunchTicket = HashMap<String, Any>()
            Log.d(TAG, "${liveTruckDataListIndexMap}, trucks ${liveTruckDataList}")
            if (liveTruckDataListIndexMap.containsKey(truckNo) &&
                liveTruckDataList[liveTruckDataListIndexMap[truckNo]!!].TruckNo == truckNo) {
                pahunchTicketGenerated = true
                val truck = liveTruckDataList[liveTruckDataListIndexMap[truckNo]!!]
                pahunchTicket["AuctionId"] = truck.AuctionId.toString()
                pahunchTicket["DeliveryInfo"] = delInfo
                pahunchTicket["Destination"] = truck.Route.second
//                pahunchTicket["PahunchAdminUId"] = demoUserUId
                pahunchTicket["PahunchAdminUId"] = auth.uid.toString()
                pahunchTicket["Source"] = truck.Route.first
                pahunchTicket["Status"] = "Accepted"
                pahunchTicket["Timestamp"] = CurrDateTimeInMillis()
                pahunchTicket["TruckNo"] = truck.TruckNo
                firestoreDb.collection(PAHUNCH_ADMIN_RECORDS).add(pahunchTicket)
                    .addOnSuccessListener {
                        Log.d(TAG, "Pahunch tick generated successfully for truck ${truckNo}")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Failed to generate pahunch ticket for truck ${truckNo}")
                    }
                    .await()
            }
        } catch (ex: Exception) {
            noException = false
        }
        if (noException && pahunchTicketGenerated) {
            //update entry in live truck data
            delay(1500)
            firestoreDb.collection(LIVE_TRUCK_DATA_LIST).document(truckNo)
                .update(mapOf("Status" to "DelPass"))
                .addOnSuccessListener {
                    Log.d(TAG, "Accepted status for truck ${truckNo}")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to accept status for truck ${truckNo}")
                }
                .await()
        }
    }

    suspend fun rejectDel(truckNo: String, delInfo: String) {
        var noException = true
        var pahunchTicketGenerated = false
        try {
            //add pahunch ticket entry in pahunch admin records
            val pahunchTicket = HashMap<String, Any>()
            Log.d(TAG, "${liveTruckDataListIndexMap}, trucks ${liveTruckDataList}")
            if (liveTruckDataListIndexMap.containsKey(truckNo) &&
                liveTruckDataList[liveTruckDataListIndexMap[truckNo]!!].TruckNo == truckNo) {
                pahunchTicketGenerated = true
                val truck = liveTruckDataList[liveTruckDataListIndexMap[truckNo]!!]
                pahunchTicket["AuctionId"] = truck.AuctionId.toString()
                pahunchTicket["DeliveryInfo"] = delInfo
                pahunchTicket["Destination"] = truck.Route.second
//                pahunchTicket["PahunchAdminUId"] = demoUserUId
                pahunchTicket["PahunchAdminUId"] = auth.uid.toString()
                pahunchTicket["Source"] = truck.Route.first
                pahunchTicket["Status"] = "Rejected"
                pahunchTicket["Timestamp"] = CurrDateTimeInMillis()
                pahunchTicket["TruckNo"] = truck.TruckNo
                firestoreDb.collection(PAHUNCH_ADMIN_RECORDS).add(pahunchTicket)
                    .addOnSuccessListener {
                        Log.d(TAG, "Pahunch tick generated successfully for truck ${truckNo}")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Failed to generate pahunch ticket for truck ${truckNo}")
                    }
                    .await()
            }
        } catch (ex: Exception) {
            noException = false
        }
        if (noException && pahunchTicketGenerated) {
            //update entry in live truck data
            delay(1500)
            firestoreDb.collection(LIVE_TRUCK_DATA_LIST).document(truckNo)
                .update(mapOf("Status" to "DelFail"))
                .addOnSuccessListener {
                    Log.d(TAG, "Rejected status for truck ${truckNo}")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to reject status for truck ${truckNo}")
                }
                .await()
        }
    }

    suspend fun fetchUserDestination() {
        firestoreDb
            .collection(USER_DATA)
            .document(auth.uid.toString())
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
        val uid = auth.uid

        val ref = firestoreDb.collection(PAHUNCH_ADMIN_RECORDS)
        val query = ref.whereEqualTo("PahunchAdminUId", uid)
        query.get().addOnSuccessListener { documents ->
            pahunchHistory = ArrayList()
            documents!!.forEach { pahunchSnapshot ->
                Log.d(TAG, "user's pahunch records: ${pahunchSnapshot.id}")
                if (pahunchSnapshot.id != "DummyDoc" && !pahunchSnapshot.metadata.hasPendingWrites()) {
                    val src = pahunchSnapshot.get("Source").toString()
                    val des = pahunchSnapshot.get("Destination").toString()
                    val delInfo = pahunchSnapshot.get("DeliveryInfo").toString()
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
                var index = 0
                liveTruckDataListIndexMap.clear()
                liveTruckDataList = ArrayList()
                snapshots!!.forEach { truckDocument ->
                    if (truckDocument.id != "DummyDoc") {
                        val isActive = truckDocument.get("Active").toString() == "true"
                        val delIsInProg = truckDocument.get("Status").toString() == "DelInProg"
                        Log.d(TAG, "truckDocument: ${truckDocument}")
                        if ( isActive && delIsInProg) {
                            val truckNo = truckDocument.get("TruckNo").toString()
                            val route = Pair(
                                truckDocument.get("Source").toString(),
                                truckDocument.get("Destination").toString()
                            )
                            val currListNo = truckDocument.get("CurrentListNo").toString()
                            val status = truckDocument.get("Status").toString()
                            val timestamp = truckDocument.get("Timestamp").toString()
                            val auctionId = truckDocument.get("AuctionId").toString().toLong()
                            val ownerFirstName = truckDocument.get("OwnerFirstName").toString()
                            val ownerLastName = truckDocument.get("OwnerLastName").toString()
                            val owner = Pair(ownerFirstName, ownerLastName)
                            val liveTruckDataItem = LiveTruckDataItem()
                            liveTruckDataItem.TruckNo = truckNo
                            liveTruckDataItem.Active = isActive
                            liveTruckDataItem.CurrentListNo = currListNo
                            liveTruckDataItem.Status = status
                            liveTruckDataItem.Timestamp = timestamp
                            liveTruckDataItem.AuctionId = auctionId
                            liveTruckDataItem.Owner = owner
                            liveTruckDataItem.Route = route

                            liveTruckDataList.add(liveTruckDataItem)
                            liveTruckDataListIndexMap[truckDocument.get("TruckNo").toString()] = index
                            index++
                        }
                    }
                }
                LiveTruckDataList.postValue(liveTruckDataList)
            }
        }
    }
}




