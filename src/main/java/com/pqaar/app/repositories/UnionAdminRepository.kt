package com.pqaar.app.repositories

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pqaar.app.model.*
import com.pqaar.app.repositories.CommonRepo.getLiveTruckDataList
import com.pqaar.app.utils.RepositoryPaths.ADD_TRUCKS_REQUESTS
import com.pqaar.app.utils.RepositoryPaths.AUCTION_LIST_DATA
import com.pqaar.app.utils.RepositoryPaths.LIVE_ROUTES_LIST
import com.pqaar.app.utils.RepositoryPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.RepositoryPaths.ROUTES_LIST_DATA
import com.pqaar.app.utils.RepositoryPaths.USER_DATA
import com.pqaar.app.utils.TimeConversions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@SuppressLint("StaticFieldLeak")
object UnionAdminRepository {
    private const val TAG = "UnionAdminRepository"

    private var firestoreDb = FirebaseFirestore.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()
    private var currAuctionListSize = -1
    var TotalTrucksRequired = 0
    var TotalTrucksClosed = 0
    var nextOpenAt = -1 /* Based on 1 based indexing */
    var initOpenSize = -1


    private val truckRequestsLive = HashMap<String, AddTruckRequest>()
    val TruckRequestsLive = MutableLiveData<HashMap<String, AddTruckRequest>>()

    private var truckCheckArray: HashMap<String, Boolean> = HashMap()
    private var liveRoutesList = MutableLiveData<ArrayList<LiveRoutesListItem>>()
    private var lastAuctionListDTO: ArrayList<Pair<String, HistoryAuctionListItemDTO>> = ArrayList()
    private var lastOpenLiveList: ArrayList<LiveAuctionListItem> = ArrayList()
    private var lastClosedLiveList: ArrayList<LiveAuctionListItem> = ArrayList()
    private var lastMissedList: ArrayList<LiveAuctionListItem> = ArrayList()
    var liveCombinedAuctionList: ArrayList<LiveAuctionListItem> = ArrayList()


    suspend fun fetchLastAuctionListDocument(): String {
        var lastAuctionListDocument = ""
        firestoreDb
            .collection(AUCTION_LIST_DATA)
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
        lastAuctionListDTO = ArrayList()
        var historyAuctionListItemDTO: HistoryAuctionListItemDTO
        firestoreDb
            .collection(AUCTION_LIST_DATA)
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
                        prevNo = listItem.first,
                        truckNo = listItem.second.truck_no
                    )
                )
            }
            if (!truckInProgress(listItem.second.truck_no)) {
                lastOpenLiveList.add(
                    LiveAuctionListItem(
                        prevNo = listItem.first,
                        truckNo = listItem.second.truck_no
                    )
                )
            }
        }
    }

    fun getLastMissedList() {
        lastMissedList = ArrayList()
        if (getLiveTruckDataList().value != null && getLiveTruckDataList().value!!.isEmpty()) {
            for (truck in getLiveTruckDataList().value!!.keys) {
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
                            prevNo =
                            (getLiveTruckDataList().value!!)[truck]!!.data["CurrentListNo"]!!,
                            truckNo = truck,
                            timestamp = (getLiveTruckDataList().value!!)[truck]!!.data["Timestamp"]!!
                        )
                    )
                }
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

    suspend fun uploadRoutesList(RoutesListToUpload: ArrayList<LiveRoutesListItem>) {
        RoutesListToUpload.forEach {
            TotalTrucksRequired += it.req.toInt()
        }
        if (RoutesListToUpload.isNotEmpty()) {
            firebaseDb
                .reference
                .child("LiveRoutesList")
                .setValue(RoutesListToUpload).addOnSuccessListener {
                    Log.d(TAG, "Live routes list uploaded successfully!")
                }.await()
        } else {
            Log.e(TAG, "Live routes list is empty!")
        }
    }

    fun getLiveRoutesList() {
        firebaseDb
            .reference
            .child(LIVE_ROUTES_LIST)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "Live routes is updated...fetching latest list!")
                        val routeList = ArrayList<LiveRoutesListItem>()
                        snapshot.children.forEach { routeNo ->
                            val routeListItem = LiveRoutesListItem()
                            routeNo.children.forEach { route ->
                                when (route.key) {
                                    "src" -> routeListItem.src = route.value.toString()
                                    "des" -> routeListItem.des = route.value.toString()
                                    "req" -> routeListItem.req = route.value.toString()
                                    "got" -> routeListItem.got = route.value.toString()
                                }
                            }
                            routeList.add(routeListItem)
                        }
                        liveRoutesList.postValue(routeList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Failed to fetch new routes list")
                    }
                }
            )
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
    suspend fun initializeAuction(bidTime: Long, startTime: Long) {
        val initBidEndTime = (startTime + bidTime).toInt()
        initOpenSize = TotalTrucksRequired.coerceAtMost(liveCombinedAuctionList.size)

        nextOpenAt = initOpenSize + 1
        currAuctionListSize = liveCombinedAuctionList.size
        val usersTruckCount = HashMap<String, Int>()

        liveCombinedAuctionList.forEach {
            if (usersTruckCount.containsKey(getTruckOwner(it.truckNo))) {
                usersTruckCount[getTruckOwner(it.truckNo)] =
                    usersTruckCount[getTruckOwner(it.truckNo)]!! + 1
            } else {
                usersTruckCount[getTruckOwner(it.truckNo)] = 1
            }
        }

        //see if a user has its truck listed in the current initialized list
        for (i in 0 until initOpenSize) {
            liveCombinedAuctionList[i]
            if (usersTruckCount.containsKey(getTruckOwner(liveCombinedAuctionList[i].truckNo))) {
                usersTruckCount[getTruckOwner(liveCombinedAuctionList[i].truckNo)] =
                    usersTruckCount[getTruckOwner(liveCombinedAuctionList[i].truckNo)]!! + 1
            } else {
                usersTruckCount[getTruckOwner(liveCombinedAuctionList[i].truckNo)] = 1
            }
        }

        //if yes then increase its bid timestamp accordingly
        //timestamp here contains the total duration a bid is supposed to stay unlocked after
        //the start time
        liveCombinedAuctionList.forEach {
            it.timestamp =
                (initBidEndTime * usersTruckCount[getTruckOwner(it.truckNo)]!!).toString()
        }

        /**
         * Upload the initial live auction list with the timestamp set for first
         * "initOpenSize" items and rest of them has the timestamp set to -1
         */
        val LiveBidList = HashMap<Int, LiveAuctionListItem>()
        liveCombinedAuctionList.forEachIndexed { index, liveAuctionListItem ->
            LiveBidList[index + 1] = liveAuctionListItem
        }
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
     * for bids acceptance.
     *
     * NOTE: "unlockDuration" is to be passed as milliseconds in the form of
     * long data type
     */
    @SuppressLint("SimpleDateFormat")
    fun unlockBid(truckNo: String, unlockDuration: Long): Boolean {
        var accepted = false
        firebaseDb
            .reference
            .child("LiveBidList")
            .child(truckNo)
            .child("locked")
            .setValue("false").addOnSuccessListener {
                Log.d(TAG, "Locked set to false successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to set locked to false!")
            }

        val timeInMilli = TimeConversions.TimestampToMillis(
            SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
                .format(Calendar.getInstance().time)
        ) + (unlockDuration)
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
            }
        return accepted
    }

    fun lockBid(truckNo: String): Boolean {
        var accepted = false
        firebaseDb
            .reference
            .child("LiveBidList")
            .child(truckNo)
            .child("locked")
            .setValue("true").addOnSuccessListener {
                accepted = true
                Log.d(TAG, "Bid Locked successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to locked bid!")
            }
        return accepted
    }

    fun fetchAddTruckRequests() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot,
                                      previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                // A new comment has been added, add it to the displayed list
                val changedEntry = dataSnapshot.getValue<AddTruckRequest>()
                truckRequestsLive[dataSnapshot.key.toString()] = changedEntry!!
                TruckRequestsLive.value =truckRequestsLive
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                val changedEntry = dataSnapshot.getValue<AddTruckRequest>()
                truckRequestsLive[dataSnapshot.key.toString()] = changedEntry!!
                TruckRequestsLive.value = truckRequestsLive
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                truckRequestsLive.remove(dataSnapshot.key.toString())
                TruckRequestsLive.value = truckRequestsLive
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                //no action
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "Retrieving AddTrucksRequests Failed:onCancelled",
                    databaseError.toException())
            }
        }
        val ref = firebaseDb.reference.child(ADD_TRUCKS_REQUESTS)
        ref.addChildEventListener(childEventListener)
    }

    suspend fun addTruckToUser(truckRequest: AddTruckRequest) {
        //add truck to live truck data
        val newEntry = mapOf<String, Any>(
            truckRequest.truckNo.toString() to mapOf(
                "Status" to "UnAssigned",
                "Owner" to truckRequest.uid.toString(),
                "CurrentListNo" to "NA",
                "Timestamp" to "NA"
            )
        )
        firebaseDb
            .reference
            .child(LIVE_TRUCK_DATA_LIST)
            .updateChildren(newEntry).addOnSuccessListener {
                Log.d(TAG, "Truck added to live truck data successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to add truck to live data list!")
            }.await()

        //add truck to user's main database
        firestoreDb
            .collection(USER_DATA)
            .document(truckRequest.uid)
            .update("Trucks", FieldValue.arrayUnion(truckRequest.truckNo)).await()
    }

    suspend fun removeTruckFromUser(truckRequest: AddTruckRequest) {
        //remove truck to live truck data
        firebaseDb
            .reference
            .child(LIVE_TRUCK_DATA_LIST)
            .child(truckRequest.truckNo)
            .setValue(null).addOnSuccessListener {
                Log.d(TAG, "Truck removed from live truck data successfully!")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to remove truck from live data list!")
            }.await()

        //add truck to user's main database
        firestoreDb
            .collection(USER_DATA)
            .document(truckRequest.uid)
            .update("Trucks", FieldValue.arrayRemove(truckRequest.truckNo)).await()
    }

    suspend fun closeAuction(closingTime: Long): Boolean {
        var closed = false

        /**
         * Uploading routes list to firestore database
         */
        val routeListToUpload = HashMap<Int, LiveRoutesListItem>()
        if (liveRoutesList.value!!.isNotEmpty()) {
            liveRoutesList.value!!.forEachIndexed { index, liveRoutesListItem ->
                routeListToUpload[index] = liveRoutesListItem
            }
            firestoreDb
                .collection(ROUTES_LIST_DATA)
                .document(closingTime.toString())
                .set(routeListToUpload).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "routes-list-data recorded = $routeListToUpload")
                    } else {
                        Log.d(TAG, "routes-list-data  upload failed")
                    }
                }.await()
        }


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
            .collection(AUCTION_LIST_DATA)
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

    fun getTrucksLeft(): Int {
        return TotalTrucksRequired - TotalTrucksClosed
    }

    fun nextBidAt(): Int {
        return nextOpenAt
    }

    private inline fun getTruckOwner(truckNo: String): String {
        return (getLiveTruckDataList().value!!)[truckNo]!!.data["Owner"]!!
    }

    //checks if the truck status is in progress
    private inline fun truckInProgress(truckNo: String): Boolean {
        return (getLiveTruckDataList().value!!)[truckNo]!!.data["Status"]!! == "DelInProg"
    }
}