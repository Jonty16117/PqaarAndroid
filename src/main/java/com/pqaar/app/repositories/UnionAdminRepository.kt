package com.pqaar.app.repositories

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pqaar.app.model.*
import com.pqaar.app.utils.TimeConversions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@SuppressLint("StaticFieldLeak")
object UnionAdminRepository {
    var TruckRequestsLive = MutableLiveData<ArrayList<TruckRequest>>()

    private const val TAG = "UnionAdminRepository"
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var firebaseDb: FirebaseDatabase
    private var totalTrucksRequired = 0
    private var nextOpenAt: Int = -1

    private var lastAuctionListDTO: ArrayList<Pair<String, HistoryAuctionListItemDTO>> = ArrayList()
    private var truckCheckArray: HashMap<String, Boolean> = HashMap()
    private var liveTruckDataList: HashMap<String, HashMap<String, String>> = HashMap()
    private var lastOpenLiveList: ArrayList<LiveAuctionListItem> = ArrayList()
    private var lastClosedLiveList: ArrayList<LiveAuctionListItem> = ArrayList()
    private var lastMissedList: ArrayList<LiveAuctionListItem> = ArrayList()
    private var liveRoutesList: ArrayList<LiveRoutesListItem> = ArrayList()
    var liveCombinedAuctionList: ArrayList<LiveAuctionListItem> = ArrayList()


    suspend fun fetchLastAuctionListDocument(): String {
        auth = FirebaseAuth.getInstance()
        firestoreDb = FirebaseFirestore.getInstance()

        var lastAuctionListDocument = ""
        firestoreDb
            .collection("auction-lists")
            .orderBy("Timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.forEach { lastAuctionListDocument = it.id }
                    Log.d(TAG, "lastAuctionListDocument = $lastAuctionListDocument")
                }
            }.await()
        return lastAuctionListDocument
    }

    suspend fun getLastAuctionList(lastAuctionListDocument: String) {
        lastAuctionListDTO = ArrayList<Pair<String, HistoryAuctionListItemDTO>>()
        var historyAuctionListItemDTO: HistoryAuctionListItemDTO
        firestoreDb
            .collection("auction-lists")
            .document(lastAuctionListDocument)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    task.result!!.data!!.forEach { seqNo ->
                        if (seqNo.key != "Timestamp") {
                            historyAuctionListItemDTO = HistoryAuctionListItemDTO()
                            (seqNo.value as HashMap<*, *>).forEach {
                                if (it.key.toString() == "truck-no") {
                                    historyAuctionListItemDTO.truck_no = it.value.toString()
                                } else {
                                    historyAuctionListItemDTO.bid_closed = it.value.toString()
                                }
                            }
                            lastAuctionListDTO.add(Pair(seqNo.key, historyAuctionListItemDTO))
                        }
                    }
                }
            }.await()
        /*
        Format of lastAuctionList = [(0, {truck-no=PB30XXX, bid-closed=false}),
        (Timestamp, Timestamp(seconds=1617736022, nanoseconds=0)),
        (seq-no, {truck-no=PB30XXX, bid-closed=true})]
         */
        /**
         * Although this list is assumed to be sorted when fetched, but since in the
         * firebase documentation, it doesn't mention this sorting assurance explicitly,
         * we therefore sort this list just for safety.
         */
        lastAuctionListDTO = ArrayList(lastAuctionListDTO.sortedWith(compareBy {
            it.first.toInt()
        }))
    }

    suspend fun getLiveTruckDataList(): Boolean {
        firebaseDb = FirebaseDatabase.getInstance()

        firebaseDb.reference.child("LiveTruckData").get().addOnSuccessListener {
            Log.i("firebase", "Got data ${it.value}")
            (it.value as HashMap<*, *>).forEach { entry ->
                val tempHashMap = HashMap<String, String>()
                (entry.value as HashMap<*, *>).forEach { innerEntry ->
                    tempHashMap[innerEntry.key.toString()] = innerEntry.value.toString()
                }
                liveTruckDataList[entry.key.toString()] = tempHashMap
            }
        }.addOnFailureListener {
            Log.e("firebase", "Error getting live truck data list", it)
        }.await()
        Log.i("firebase", "Got data $liveTruckDataList")
        if (liveTruckDataList.isEmpty()) {
            return false
        }
        return true
    }

    fun separateOpenCloseLists() {
        lastOpenLiveList = ArrayList()
        lastClosedLiveList = ArrayList()

        //to track which truck were in the last auction which were not
        truckCheckArray = HashMap<String, Boolean>()

        for (listItem in lastAuctionListDTO) {
            /**
             * if this bid was accepted in last auction, then put it in lastClosedLiveList
             */
            truckCheckArray[listItem.second.truck_no] = true
            if ((listItem.second.bid_closed == "true") &&
                (!truckInProgress(listItem.second.truck_no))
            ) {
                lastClosedLiveList.add(
                    LiveAuctionListItem(
                        "", listItem.first, listItem.second.truck_no,
                        "true", "", ""
                    )
                )
            }
            if (!truckInProgress(listItem.second.truck_no)) {
                lastOpenLiveList.add(
                    LiveAuctionListItem(
                        "", listItem.first, listItem.second.truck_no,
                        "true", "", ""
                    )
                )
            }
        }
    }

    fun getLastMissedList() {
        lastMissedList = ArrayList()
        for (truck in liveTruckDataList.keys) {
            /**
             * if there is a truck which was not in the last auction
             * and whose status is "not in progress", initialize it in the
             * last missed list.
             *
             * Note: The timestamp added in the missed list here, is just to do sorting
             * which is done in the next, step. But this timestamp's original motive
             * is to store the timestamp information during the live auction.
             */
            if (!truckCheckArray.containsKey(truck) &&
                !truckInProgress(truck)
            ) {
                lastMissedList.add(
                    LiveAuctionListItem(
                        "", liveTruckDataList[truck]!!["CurrentListNo"]!!,
                        truck,
                        "true", "", "", liveTruckDataList[truck]!!["Timestamp"]!!.toInt()
                    )
                )
            }
        }

        /**
         * sort the newly generated missed list according
         * to the timestamp, in the ascending order
         */
        lastMissedList = ArrayList(lastMissedList.sortedWith(compareBy { it.timestamp }))
    }

    /**
     * Combine last closed, last open and last missed list in the following order:
     *
     * Last Missed List -> Last Open List -> Last Closed List
     */
    fun combineLists() {
        liveCombinedAuctionList = ArrayList()
        liveCombinedAuctionList =
            (lastMissedList + lastOpenLiveList + lastClosedLiveList) as ArrayList<LiveAuctionListItem>
    }

    suspend fun uploadRoutesList(liveRoutesList: ArrayList<LiveRoutesListItem>) {
        this.liveRoutesList = liveRoutesList
        liveRoutesList.forEach {
            totalTrucksRequired += it.req.toInt()
        }
        if (liveRoutesList.isNotEmpty()) {
            firebaseDb
                .reference
                .child("LiveRoutesList")
                .setValue(liveRoutesList)
            Log.d(TAG, "Live routes list uploaded successfully!")
        } else {
            Log.e(TAG, "Live routes list is empty!")
        }
    }

    /**
     * There can be following type of auction status:
     *
     * Live -> When the auction is in progress
     * (Timestamp -> Timestamp of when the auction will end)
     *
     * Scheduled -> When the auction is scheduled at a future point in date and time
     * (Timestamp -> Timestamp of when the next auction will begin)
     *
     * NA -> When there is no live auction and none is scheduled
     * (Timestamp -> Timestamp of when the last auction ended)
     */
    suspend fun setAuctionStatus(status: String) {
        firebaseDb = FirebaseDatabase.getInstance()
        firebaseDb
            .reference
            .child("AuctionStatus")
            .child("Status")
            .setValue(status).addOnSuccessListener {
                Log.d(TAG, "Auction Status updated successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Auction Status update failed!")
            }.await()
    }

    suspend fun setAuctionTimestamp(timestamp: Long) {
        firebaseDb = FirebaseDatabase.getInstance()
        firebaseDb
            .reference
            .child("AuctionStatus")
            .child("Timestamp")
            .setValue(timestamp.toString()).addOnSuccessListener {
                Log.d(TAG, "Auction Timestamp updated successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Auction Timestamp update failed!")
            }.await()
    }

    /**
     * This is the time allowed for each truck that are included in the initial
     * bid timer. usersTruckCount is to get all the truck owners who have
     * more than one trucks in their initial auction list for the purpose
     * of giving them more time.
     */
    suspend fun initializeAuction(bidTimeInSec: Int, startTime: Long) {
        val initBidEndTime = (startTime + bidTimeInSec).toInt()
        val initSize = totalTrucksRequired.coerceAtMost(liveCombinedAuctionList.size)
        nextOpenAt = initSize + 1
        val usersTruckCount = HashMap<String, Int>()

        liveCombinedAuctionList.forEach {
            if (usersTruckCount.containsKey(getTruckOwner(it.truckNo))) {
                usersTruckCount[getTruckOwner(it.truckNo)] =
                    usersTruckCount[getTruckOwner(it.truckNo)]!! + 1
            } else {
                usersTruckCount[getTruckOwner(it.truckNo)] = 1
            }
        }

        for (i in 0 until initSize) {
            liveCombinedAuctionList[i]
            if (usersTruckCount.containsKey(getTruckOwner(liveCombinedAuctionList[i].truckNo))) {
                usersTruckCount[getTruckOwner(liveCombinedAuctionList[i].truckNo)] =
                    usersTruckCount[getTruckOwner(liveCombinedAuctionList[i].truckNo)]!! + 1
            } else {
                usersTruckCount[getTruckOwner(liveCombinedAuctionList[i].truckNo)] = 1
            }
        }

        liveCombinedAuctionList.forEach {
            it.timestamp = initBidEndTime * usersTruckCount[getTruckOwner(it.truckNo)]!!
        }

        /**
         * Upload the initial live auction list with the timestamp set for first
         * "initSize" items and rest of them has the timestamp set to -1
         */
        val LiveBidList = HashMap<Int, LiveAuctionListItem>()
        liveCombinedAuctionList.forEachIndexed { index, liveAuctionListItem ->
            LiveBidList[index+1] = liveAuctionListItem
        }
        firebaseDb = FirebaseDatabase.getInstance()
        firebaseDb
            .reference
            .child("LiveBidList")
            .setValue(LiveBidList).addOnSuccessListener {
                Log.d(TAG, "Live Bid List uploaded successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Live Bid List upload failed!")
            }.await()
    }

    suspend fun acceptBid(liveAuctionListItem: LiveAuctionListItem): Boolean {
        var accepted = false
        firebaseDb = FirebaseDatabase.getInstance()
        firebaseDb
            .reference
            .child("LiveBidList")
            .child(liveAuctionListItem.currentNo)
            .setValue(liveAuctionListItem).addOnSuccessListener {
                accepted = true
                Log.d(TAG, "Bid Accepted successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to accept bid!")
            }.await()
        return accepted
    }

    /**
     * Essentially set the timestamp for the bid item to allow them to request
     * for bids acceptance
     */
    @SuppressLint("SimpleDateFormat")
    suspend fun unlockBid(truckNo: String, unlockDuration: Int): Boolean {
        var accepted = false
        firebaseDb = FirebaseDatabase.getInstance()
        firebaseDb
            .reference
            .child("LiveBidList")
            .child(truckNo)
            .child("closed")
            .setValue("false").addOnSuccessListener {
                Log.d(TAG, "Locked set to false successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to set locked to false!")
            }.await()

        val timeInMilli = TimeConversions.TimestampToMillis(
            SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
                .format(Calendar.getInstance().time)) + unlockDuration
        firebaseDb
            .reference
            .child("LiveBidList")
            .child(truckNo)
            .child("timestamp")
            .setValue(timeInMilli).addOnSuccessListener {
                accepted = true
                Log.d(TAG, "Unlock timestamp set successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to set unlock timestamp!")
            }.await()
        return accepted
    }

    suspend fun lockBid(truckNo: String): Boolean {
        var accepted = false
        firebaseDb = FirebaseDatabase.getInstance()
        firebaseDb
            .reference
            .child("LiveBidList")
            .child(truckNo)
            .child("closed")
            .setValue("true").addOnSuccessListener {
                accepted = true
                Log.d(TAG, "Bid Locked successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to locked bid!")
            }.await()
        return accepted
    }

    suspend fun fetchTruckRequests() {
        firebaseDb
            .reference
            .child("AddTruckRequests")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val reqList = ArrayList<TruckRequest>()
                        snapshot.children.forEach{
                            reqList.add(
                                TruckRequest(it.key.toString(),it.value.toString())
                            )
                        }
                        TruckRequestsLive.postValue(reqList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Failed to listen to truck add requests")
                    }
                }
            )
    }

    suspend fun addTruckToUser(truckRequest: TruckRequest) {
        //add truck to live truck data
        val newEntry = mapOf<String, Any>(truckRequest.truckNo.toString() to mapOf(
            "Status" to "UnAssigned",
            "Owner" to truckRequest.uid.toString(),
            "CurrentListNo" to "NA",
            "Timestamp" to "NA"
        ))
        firebaseDb
            .reference
            .child("LiveTruckData")
            .updateChildren(newEntry).addOnSuccessListener {
                Log.d(TAG, "Truck added to live truck data successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to add truck to live data list!")
            }.await()

        //add truck to user's main database
        firestoreDb
            .collection("user-data")
            .document(truckRequest.uid)
            .update("Trucks", FieldValue.arrayUnion(truckRequest.truckNo)).await()
    }

    suspend fun removeTruckFromUser(truckRequest: TruckRequest) {
        //remove truck to live truck data
        firebaseDb
            .reference
            .child("LiveTruckData")
            .child(truckRequest.truckNo)
            .setValue(null).addOnSuccessListener {
                Log.d(TAG, "Truck removed from live truck data successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to remove truck from live data list!")
            }.await()

        //add truck to user's main database
        firestoreDb
            .collection("user-data")
            .document(truckRequest.uid)
            .update("Trucks", FieldValue.arrayRemove(truckRequest.truckNo)).await()
    }

    suspend fun closeAuction(closingTime: Long): Boolean {
        var closed = false

        /**
         * Uploading routes list to firestore database
         */
        val routeListToUpload = HashMap<Int, LiveRoutesListItem>()
        liveRoutesList.forEachIndexed { index, liveRoutesListItem ->
            routeListToUpload[index] = liveRoutesListItem
        }
        firestoreDb
            .collection("routes-list-data")
            .document(closingTime.toString())
            .set(routeListToUpload).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "routes-list-data recorded = $routeListToUpload")
                } else {
                    Log.d(TAG, "routes-list-data  upload failed")
                }
            }.await()


        /**
         * Uploading auction list to firestore database
         */
        val auctionListToUpload = HashMap<Int, Map<String, String>>()
        liveCombinedAuctionList.forEachIndexed { index, liveAuctionListItem ->
            auctionListToUpload[index] = mapOf(
                "bid-closed" to liveAuctionListItem.closed,
                "truck-no" to liveAuctionListItem.truckNo
            )
        }
        firestoreDb
            .collection("auction-lists")
            .document(closingTime.toString())
            .set(auctionListToUpload).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "auction-lists recorded = $auctionListToUpload")
                    closed = true
                } else {
                    Log.d(TAG, "auction-lists  upload failed")
                }
            }.await()
        return closed
    }

    private inline fun getTruckOwner(truckNo: String): String {
        return liveTruckDataList[truckNo]!!["Owner"]!!
    }

    //checks if the truck status is in progress
    private inline fun truckInProgress(truckNo: String): Boolean {
        return liveTruckDataList[truckNo]!!["Status"] == "DelInProg"
    }
}



