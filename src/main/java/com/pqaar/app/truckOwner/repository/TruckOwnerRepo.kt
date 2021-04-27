package com.pqaar.app.truckOwner.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.model.*
import com.pqaar.app.unionAdmin.repository.UnionAdminRepo
import com.pqaar.app.utils.DbPaths
import com.pqaar.app.utils.DbPaths.FIRST_NAME
import com.pqaar.app.utils.DbPaths.LAST_NAME
import com.pqaar.app.utils.DbPaths.LIVE_AUCTION_LIST
import com.pqaar.app.utils.DbPaths.LIVE_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.PHONE_NO
import com.pqaar.app.utils.DbPaths.TRUCKS
import com.pqaar.app.utils.DbPaths.USER_DATA
import kotlinx.coroutines.tasks.await


/**
 * Things that any truck owner can do:
 * 1) View live routes list
 * 2) View live auction list
 * 3) Book route/s
 * 4) View the current live status of all of its trucks
 * 5) View auction info (scheduled auction info and auction bonus time)
 * 6) View the history of all of its trucks
 * 7) Submit requests for adding/removing truck/s from its account
 */

@SuppressLint("StaticFieldLeak")
object TruckOwnerRepo {
    private const val TAG = "TruckOwnerRepo"

    private val firestoreDb = FirebaseFirestore.getInstance()
    private val firebaseDb = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var truckOwnerLiveData = TruckOwner()
    private var liveRoutesList = HashMap<String, LiveRoutesListItemDTO>()
    private var liveAuctionList = HashMap<String, LiveAuctionListItem>()
    private val liveTruckDataList = HashMap<String, TruckHistory>()

    val TruckOwnerLiveData = MutableLiveData<TruckOwner>()
    val LiveRoutesList = MutableLiveData<HashMap<String, LiveRoutesListItemDTO>>()
    val LiveAuctionList = MutableLiveData<HashMap<String, LiveAuctionListItem>>()
    val LiveTruckDataList = MutableLiveData<HashMap<String, TruckHistory>>()
    val LiveAuctionStatus = MutableLiveData<String>()
    val LiveAuctionStartTime = MutableLiveData<Long>()
    val LiveAuctionEndTime = MutableLiveData<Long>()
    val LiveBonusTimeInfo = MutableLiveData<BonusTime>()

    suspend fun fetchTruckOwner() {
        val testUid = "DemoUserTO"
        firestoreDb.collection(USER_DATA)
            .document(testUid).get()
            .addOnSuccessListener {
                truckOwnerLiveData.FirstName = it.get(FIRST_NAME).toString()
                truckOwnerLiveData.LastName = it.get(LAST_NAME).toString()
                truckOwnerLiveData.PhoneNo = it.get(PHONE_NO).toString()
                val trucks = it.get(TRUCKS) as List<*>
                val trucksArrayList = ArrayList<String>()
                trucks.forEach { trucksArrayList.add(it.toString()) }
                truckOwnerLiveData.Trucks = trucksArrayList
                Log.w(TAG, "Truck user data fetched successfully!")
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to fetch truck owner user data, please try again!")
            }.await()
        TruckOwnerLiveData.postValue(truckOwnerLiveData)
    }

    fun fetchLiveRoutesList() {
        val coll = firestoreDb.collection(LIVE_ROUTES_LIST)
        coll.addSnapshotListener { routes, error ->
            if (error != null) {
                Log.w(
                    TAG, "Failed to update the live routes list"
                )
            } else {
                var src: String /*Mandi*/
                var des: String /*Destination*/
                var data: String /*Represents specifier for Rate, Requirement or Got*/
                var value: String /*Contains the value of the specified specifier*/
                var splittedWords: List<String>
                routes!!.forEach {
                    if (it.id != "DummyDoc") {
                        splittedWords = it.id.split("-")
                        src = splittedWords[0].replace('_', ' ')
                        des = splittedWords[1].replace('_', ' ')
                        data = splittedWords[2]
                        value = it.get("Value").toString()
                        updateLiveRouteList(src, des, data, value)
                        Log.w(TAG, "Updated list for ${it.id} = ${it.data}")
                    }
                }
                Log.d(TAG, "Fully updated live routes list ${liveRoutesList}")
                LiveRoutesList.value = liveRoutesList
            }
        }
    }

    private fun getDesDataDTO(data: String, value: String): HashMap<String, String> {
        val desData = HashMap<String, String>()
        when (data) {
            "Req" -> {
                desData["Req"] = value
                desData["Got"] = "0"
                desData["Rate"] = "0"
            }
            "Got" -> {
                desData["Req"] = "0"
                desData["Got"] = value
                desData["Rate"] = "0"
            }
            "Rate" -> {
                desData["Req"] = "0"
                desData["Got"] = "0"
                desData["Rate"] = value
            }
        }
        return desData
    }

    private fun updateLiveRouteList(src: String, des: String, data: String, value: String) {
        if (liveRoutesList.containsKey(src)) {
            //check if this mandi destination has already been added
            if (liveRoutesList[src]!!.desData.containsKey(des)) {
                liveRoutesList[src]!!.desData[des]!![data] = value
            } else {
                liveRoutesList[src]!!.desData[des] = getDesDataDTO(data, value)
            }
        } else {
            liveRoutesList[src] = LiveRoutesListItemDTO(
                desData = hashMapOf(
                    des to getDesDataDTO(data, value)
                )
            )
        }
    }

    fun fetchLiveAuctionList() {
        val coll = firestoreDb.collection(LIVE_AUCTION_LIST)
        coll.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.w(
                    TAG, "Failed to update the latest auction list"
                )
            } else {
                for (doc in snapshots!!.documentChanges) {
                    if (doc.document.id != "DummyDoc") {
                        val docData = doc.document
                        liveAuctionList[doc.document.id] = LiveAuctionListItem(
                            CurrNo = docData.data["currNo"].toString(),
                            PrevNo = docData.data["prevNo"].toString(),
                            TruckNo = docData.data["truckNo"].toString(),
                            Closed = docData.data["closed"].toString(),
                            StartTime = docData.getLong("startTime"),
                            Des = docData.data["des"].toString(),
                            Src = docData.data["src"].toString()
                        )
                    }
                }
                LiveAuctionList.postValue(liveAuctionList)
            }
            Log.w(
                TAG, "Updated live auction list: ${LiveAuctionList.value}")
        }
    }

    suspend fun closeBid(truckNo: String, src: String, des: String) {
        val dataToUpdate = hashMapOf<String, Any>("src" to src, "des" to des)
        Log.d(TAG, "Request to close the bid for truck ${truckNo}")
        Log.d(TAG, "livetruckdatalist in close bid fun: ${liveTruckDataList}")
        val currListNo = liveTruckDataList[truckNo]!!.CurrentListNo
        Log.d(TAG, "Closing the bid for ${truckNo} at list no ${currListNo}")
        firestoreDb.collection(LIVE_AUCTION_LIST)
            .document(currListNo)
            .update(dataToUpdate)
            .addOnSuccessListener {
                Log.d(TAG, "Request to close the bid for truck ${truckNo} " +
                        "sent!")
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to send request to close the " +
                        "bid for truck ${truckNo}!, error: ${it}")
            }.await()
    }

    fun fetchLiveTruckDataList() {
        if (truckOwnerLiveData.Trucks.isNotEmpty()) {
            truckOwnerLiveData.Trucks.forEach{ truck ->

                val childEventListener = object : ChildEventListener {
                    override fun onChildAdded(
                        dataSnapshot: DataSnapshot,
                        previousChildName: String?,
                    ) {
                        Log.d(TAG, "Updated truck ${dataSnapshot.key.toString()} " +
                                "status: ${dataSnapshot}")
                        //if the current list does not have any entry for this truck, then initialize
                        //this truck by making its entry in the list
                        if (!liveTruckDataList.containsKey(truck)) {
                            liveTruckDataList[truck] = TruckHistory(TruckNo = truck)
//                            liveTruckDataList[truck] = LiveTruckDataListItemDTO(truckNo = truck)
                        }

                        when (dataSnapshot.key.toString()) {
                            "CurrentListNo" -> {
                                liveTruckDataList[truck]!!
                                    .CurrentListNo = dataSnapshot.value.toString()
                            }
                            "Owner" -> {
                                liveTruckDataList[truck]!!
                                    .Owner = dataSnapshot.value.toString()
                            }
                            "Status" -> {
                                liveTruckDataList[truck]!!
                                    .Status = dataSnapshot.value.toString()
                            }
                            "Timestamp" -> {
                                liveTruckDataList[truck]!!
                                    .Timestamp = dataSnapshot.value.toString()
                            }
                            "Source" -> {
                                val oldRouteValue = liveTruckDataList[truck]!!.Route
                                val newRouteValue = Pair(dataSnapshot.value.toString(), oldRouteValue.second)
                                liveTruckDataList[truck]!!
                                    .Route = newRouteValue
                            }
                            "Destination" -> {
                                val oldRouteValue = liveTruckDataList[truck]!!.Route
                                val newRouteValue = Pair(oldRouteValue.first, dataSnapshot.value.toString())
                                liveTruckDataList[truck]!!
                                    .Route = newRouteValue
                            }
                        }

                        LiveTruckDataList.value = liveTruckDataList
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                        Log.d(TAG, "Truck Status updated: ${dataSnapshot.value}")

                        if (!liveTruckDataList.containsKey(truck)) {
                            liveTruckDataList[truck] = TruckHistory(TruckNo = truck)
//                            liveTruckDataList[truck] = LiveTruckDataListItemDTO(truckNo = truck)
                        }

                        when (dataSnapshot.key.toString()) {
                            "CurrentListNo" -> {
                                liveTruckDataList[truck]!!
                                    .CurrentListNo = dataSnapshot.value.toString()
                            }
                            "Owner" -> {
                                liveTruckDataList[truck]!!
                                    .Owner = dataSnapshot.value.toString()
                            }
                            "Status" -> {
                                liveTruckDataList[truck]!!
                                    .Status = dataSnapshot.value.toString()
                            }
                            "Timestamp" -> {
                                liveTruckDataList[truck]!!
                                    .Timestamp = dataSnapshot.value.toString()
                            }
                            "Source" -> {
                                val oldRouteValue = liveTruckDataList[truck]!!.Route
                                val newRouteValue = Pair(dataSnapshot.value.toString(), oldRouteValue.second)
                                liveTruckDataList[truck]!!
                                    .Route = newRouteValue
                            }
                            "Destination" -> {
                                val oldRouteValue = liveTruckDataList[truck]!!.Route
                                val newRouteValue = Pair(oldRouteValue.first, dataSnapshot.value.toString())
                                liveTruckDataList[truck]!!
                                    .Route = newRouteValue
                            }
                        }

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
                Log.w(TAG, "Updated live truck data list: ${liveTruckDataList}")
                firebaseDb.reference
                    .child(LIVE_TRUCK_DATA_LIST)
                    .child(truck.trim())
                    .addChildEventListener(childEventListener)
            }
        }
    }


    fun fetchSchAuctionsInfo() {
        firestoreDb.collection(DbPaths.AUCTIONS_INFO)
            .document(DbPaths.SCHEDULED_AUCTIONS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    LiveAuctionStatus.value = "NA"
                    LiveAuctionStartTime.value = 0L
                    LiveAuctionEndTime.value = 0L
                    Log.w(
                        TAG, "Failed to fetch auction info!"
                    )
                } else {
                    LiveAuctionStatus.value = snapshot!!.get("Status").toString()
                    LiveAuctionStartTime.value = snapshot.getLong("StartTime")
                    LiveAuctionEndTime.value = snapshot.getLong("EndTime")
                }
            }
    }

    fun fetchAuctionsBonusTimeInfo() {
        firestoreDb.collection(DbPaths.AUCTIONS_INFO)
            .document(DbPaths.AUCTION_BONUS_TIME_INFO).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(
                        TAG, "Failed to fetch auction info!"
                    )
                } else {
                    if (snapshot != null) {
                        Log.d(
                            TAG,
                            "Fetched updated auction bonus time: ${snapshot.get("StartTime")}" +
                                    ", ${snapshot.get("EndTime")}!"
                        )
                        Log.d(
                            TAG, "Fetched updated auction bonus time: ${snapshot}!"
                        )
                        Log.d(
                            TAG, "Fetched updated auction bonus time: ${snapshot.data}!"
                        )
                        val liveBonusTimeInfo = BonusTime(
                            StartTime = snapshot.getLong("StartTime")!!,
                            EndTime = snapshot.getLong("EndTime")!!
                        )
                        LiveBonusTimeInfo.value = liveBonusTimeInfo
                    }
                }
            }
    }

}