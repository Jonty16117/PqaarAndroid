package com.pqaar.app.repositories

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.pqaar.app.model.*
import com.pqaar.app.utils.DbPaths
import com.pqaar.app.utils.DbPaths.ADD_TRUCKS_REQUESTS
import com.pqaar.app.utils.DbPaths.AUCTIONS_INFO
import com.pqaar.app.utils.DbPaths.AUCTION_BONUS_TIME_INFO
import com.pqaar.app.utils.DbPaths.AUCTION_LIST_DATA
import com.pqaar.app.utils.DbPaths.LIVE_AUCTION_LIST
import com.pqaar.app.utils.DbPaths.LIVE_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.MANDI_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.ROUTES_LIST_DATA
import com.pqaar.app.utils.DbPaths.SCHEDULED_AUCTIONS
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.TimeConversions
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@SuppressLint("StaticFieldLeak")
object UnionAdminRepo {
    private const val TAG = "UnionAdminRepository"
    private const val CHUNKED_LIST_SIZE = 350

    private var firestoreDb = FirebaseFirestore.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()

    private var propRoutesList = HashMap<String, MutableMap<String, Any>>()
    private val truckRequestsLive = HashMap<String, AddTruckRequest>()
    private val liveAuctionList = HashMap<String, LiveAuctionListItem>()
    private val liveTruckDataList = HashMap<String, LiveTruckDataListItem>()
    private var liveRoutesList = HashMap<String, LiveRoutesListItem>()
    private var lastAuctionListDTO: ArrayList<Pair<String, HistoryAuctionListItemDTO>> = ArrayList()
    private lateinit var lastOpenLiveList: ArrayList<LiveAuctionListItem>
    private lateinit var lastClosedLiveList: ArrayList<LiveAuctionListItem>
    private lateinit var lastMissedList: ArrayList<LiveAuctionListItem>
    private lateinit var truckCheckArray: HashMap<String, Boolean>
    private lateinit var liveCombinedAuctionList: ArrayList<LiveAuctionListItem>
    val PropRoutesList = MutableLiveData<HashMap<String, MutableMap<String, Any>>>()
    val TruckRequestsLive = MutableLiveData<HashMap<String, AddTruckRequest>>()
    val LiveAuctionList = MutableLiveData<HashMap<String, LiveAuctionListItem>>()
    val LiveTruckDataList = MutableLiveData<HashMap<String, LiveTruckDataListItem>>()
    val LiveRoutesList = MutableLiveData<HashMap<String, LiveRoutesListItem>>()
    val LiveAuctionStatus = MutableLiveData<String>()
    val LiveAuctionTimestamp = MutableLiveData<String>()
    val LiveBonusTimeInfo = MutableLiveData<BonusTime>()


    /**
     * Fetches the proposed routes list from all the mandi admins
     */
    fun fetchLivePropRoutesList() {
        val mandiCollec = firestoreDb.collection(MANDI_ROUTES_LIST)
        mandiCollec.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.w(
                    TAG, "Failed to update the lastest proposed routes lists from" +
                            "mandis"
                )
            } else {
                /**
                 * <snapshots>
                 * MandiSrc:
                 *     <snapshot>
                 *     Des:
                 *         Rate: <rate>
                 *         Req: <req>
                 *         Got: <got>
                 * where snapshots represents MandiSrc
                 *
                 */
                propRoutesList = HashMap()
                snapshots!!.forEach {
                    propRoutesList[it.id] = it.data
                    Log.w(TAG, "Updated list for ${it.id} = ${it.data}")
                }
            }
            PropRoutesList.value = propRoutesList
        }
    }

    /**
     * Union admin then uploads its version of live routes list, on which the final
     * auction will take place.
     *
     * Data model for routesListToUpload:
     * Des1:
     *     Rate: <rate>
     *     Req: <requirement>
     *     Got: <got>
     */
    suspend fun uploadLiveMandiRoutes(
        mandiSrc: String,
        routesListToUpload: HashMap<String, HashMap<String, Int>>,
    ) {
        val routes = ArrayList<Pair<String, Int>>()
        routesListToUpload.forEach { route ->
            route.value.forEach { value ->
                routes.add(Pair(
                    "${mandiSrc}-${route.key}-${value.key}", value.value
                    )
                )
            }
        }
        val col = firestoreDb.collection(LIVE_ROUTES_LIST)
        val batchedList = routes.chunked(CHUNKED_LIST_SIZE)
        batchedList.forEachIndexed { index, item ->
            firestoreDb.runBatch { batch ->
                item.forEach {
                    val doc = col.document(it.first)
                    batch.set(doc, it.second)
                }
            }.addOnSuccessListener {
                Log.w(TAG, "Batch upload successful for ${index} mandi documents!")
            }.addOnSuccessListener {
                Log.w(TAG, "Failed to upload a batch of ${index} mandi documents!")
            }
            delay(2000)
        }


        /**
         * Discarded, because it was inefficient
         */
        /*val mandiCollec = firestoreDb.collection(LIVE_ROUTES_LIST)
            .document(mandiSrc)
        Log.d(TAG, "Uploading live routes for ${mandiSrc}...!")
        mandiCollec.set(routesListToUpload).addOnSuccessListener {
            Log.d(TAG, "Live routes for ${mandiSrc} uploaded successfully!")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to upload live routes for ${mandiSrc}!")
        }*/
    }

    /**
     *Get Live Truck Data
     */
    fun fetchLiveTruckDataList() {
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
                liveTruckDataList[dataSnapshot.key!!] = LiveTruckDataListItem(
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
                liveTruckDataList[dataSnapshot.key!!] = LiveTruckDataListItem(
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
        firebaseDb.setPersistenceEnabled(true)
        val ref = firebaseDb.reference.child(LIVE_TRUCK_DATA_LIST)
        ref.addChildEventListener(childEventListener)
    }

    /**
     * Functions for constructing live auction list
     */
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
        var historyAuctionListItemDTO: HistoryAuctionListItemDTO
        firestoreDb
            .collection(AUCTION_LIST_DATA)
            .document(lastAuctionListDocument)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    Log.w(TAG, "Fetched the last auction list named: ${lastAuctionListDocument}")
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
        lastClosedLiveList = ArrayList()
        lastOpenLiveList = ArrayList()
        truckCheckArray = HashMap()
        //to track which truck were in the last auction and which were not
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
                        PrevNo = listItem.first,
                        TruckNo = listItem.second.truck_no
                    )
                )
            }
            if (!truckInProgress(listItem.second.truck_no)) {
                lastOpenLiveList.add(
                    LiveAuctionListItem(
                        PrevNo = listItem.first,
                        TruckNo = listItem.second.truck_no
                    )
                )
            }
        }
    }

    fun getLastMissedList() {
        lastMissedList = ArrayList()
        /**
         * Iterate over all the trucks that are in the live truck data list (because
         * this list contains all the trucks along with their status)
         */
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
                        PrevNo =
                        liveTruckDataList[truck]!!.data["CurrentListNo"]!!,
                        TruckNo = truck,
                        EndTime = liveTruckDataList[truck]!!.data["Timestamp"]!!.toLong()
                    )
                )
            }
        }

        /**
         * sort the newly generated missed list according
         * to the timestamp, in the ascending order
         */
        lastMissedList = ArrayList(lastMissedList.sortedWith(compareBy { it.EndTime }))
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
        Log.d(TAG, "Size of lastMissedList ${lastMissedList.size}, " +
                "lastOpenLiveList: ${lastOpenLiveList.size}, " +
                "lastClosedLiveList: ${lastClosedLiveList.size}")
    }

    fun setEndTimeInLiveAuctionList(perUserBidDurationInMillis: Long) {
        val auctionStartTime = LiveAuctionTimestamp.value!!.toLong()
        liveCombinedAuctionList.forEachIndexed { index, item ->
            item.EndTime = auctionStartTime + (perUserBidDurationInMillis * (index + 1))
            item.CurrNo = (index + 1).toString()
        }
    }

    suspend fun uploadLiveAuctionList() {
        val col = firestoreDb.collection(LIVE_AUCTION_LIST)
        val batchedList = liveCombinedAuctionList.chunked(CHUNKED_LIST_SIZE)
        Log.w(TAG, "Batch to upload: ${batchedList}")
        batchedList.forEachIndexed { index, ls ->
            firestoreDb.runBatch { batch ->
                ls.forEach {
                    var doc = col.document(it.CurrNo)
                    batch.set(doc, it)
                }
            }.addOnSuccessListener {
                Log.w(TAG, "Batch ${index + 1} of size ${ls.size} uploaded successfully!")
            }.addOnFailureListener {
                Log.w(TAG, "Batch ${index + 1} of size ${ls.size} failed to upload!")
            }
            delay(2000)
        }


        /**
         * To upload everything in a single document, but it led to a lot of network calls
         * when any client updated this document. Because the whole document was downloaded
         * again after every updated in any field
         */
        /*val dataToUpload = HashMap<String, LiveAuctionListItem>()
        liveCombinedAuctionList.forEach {
            dataToUpload[it.CurrNo] = it
        }
        val doc = firestoreDb.collection(LIVE_AUCTION_LIST).document(LIVE_AUCTION_LIST)
        doc.set(dataToUpload).addOnSuccessListener {
            Log.d(TAG, "Uploaded live auction list successfully!")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to upload live auction list!")
        }.await()*/
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
    suspend fun uploadSchAuctionsInfo(status: String, timestamp: Long) {
        val dataToUpload = hashMapOf(
            "Status" to status,
            "Timestamp" to timestamp
        )
        firestoreDb.collection(AUCTIONS_INFO)
            .document(SCHEDULED_AUCTIONS).set(dataToUpload)
            .addOnSuccessListener {
                Log.w(
                    TAG, "AuctionInfo updated successfully!"
                )
            }.addOnFailureListener {
                Log.w(
                    TAG, "Failed to update AuctionInfo!"
                )
                println(it)
            }.await()
    }

    fun uploadAuctionsBonusTimeInfo(startTime: String, endTime: String) {
        firestoreDb.collection(AUCTIONS_INFO)
            .document(AUCTION_BONUS_TIME_INFO).set {
                hashMapOf(
                    "StartTime" to startTime.toLong(),
                    "EndTime" to endTime.toLong()
                )
            }.addOnSuccessListener {
                Log.w(
                    TAG, "AuctionsBonusTimeInfo updated successfully!"
                )
            }.addOnFailureListener {
                Log.w(
                    TAG, "Failed to update AuctionsBonusTimeInfo!"
                )
            }
    }

    fun fetchSchAuctionsInfo() {
        firestoreDb.collection(AUCTIONS_INFO)
            .document(SCHEDULED_AUCTIONS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    LiveAuctionStatus.value = ""
                    LiveAuctionTimestamp.value = ""
                    Log.w(
                        TAG, "Failed to fetch auction info!"
                    )
                } else {
                    LiveAuctionStatus.value = snapshot!!.get("Status").toString()
                    LiveAuctionTimestamp.value = snapshot.get("Timestamp").toString()
                }
            }
    }

    fun fetchAuctionsBonusTimeInfo() {
        firestoreDb.collection(AUCTIONS_INFO)
            .document(AUCTION_BONUS_TIME_INFO).addSnapshotListener { snapshot, error ->
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
                        val liveBonusTimeInfo = BonusTime()
                        liveBonusTimeInfo.StartTime = snapshot.getLong("StartTime")!!
                        liveBonusTimeInfo.EndTime = snapshot.getLong("EndTime")!!
                        LiveBonusTimeInfo.value = liveBonusTimeInfo
                    }
                }
            }
    }

    fun closeLiveAuctionListItem(dataSnapshot: DataSnapshot) {
        val doc = firestoreDb.collection(LIVE_AUCTION_LIST).document(dataSnapshot.ref.toString())
        doc.update("Closed", "true").addOnSuccessListener {
            Log.d(TAG, "Bid closed!")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to close bid!")
        }
    }

    fun updateRouteItem(src: String, des: String, newGot: Int) {
//        val dataToUpload = liveRoutesList
//        dataToUpload[src]!!.desData[des]!!["Got"] = newGot.toString()
        val docName = "${src}-${des}-Got"
        val doc = firestoreDb.collection(LIVE_ROUTES_LIST).document(docName)
        doc.set(hashMapOf("Value" to newGot)).addOnSuccessListener {
            Log.d(TAG, "Route list for ${src} updated, ${newGot} trucks filled")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to update live route list for ${src}")
        }
    }

    /**
     * Check if the user has updated the routes
     */
    fun updateRoutesListOnBidClose(dataSnapshot: DataSnapshot) {
        val changedEntry = dataSnapshot.getValue<LiveAuctionListItem>()
        val src = changedEntry!!.Src
        val des = changedEntry.Des
        if ((src != "") && (des != "")) {
            if (liveRoutesList.containsKey(src) &&
                (liveRoutesList[src]!!.desData.containsKey(des))
            ) {
                if ((liveRoutesList[src]!!.desData[des]!!["Req"]!!.toInt() -
                            liveRoutesList[src]!!.desData[des]!!["Got"]!!.toInt()) > 0
                ) {
                    updateRouteItem(src = src, des = des,
                        newGot = liveRoutesList[src]!!.desData[des]!!["Got"]!!.toInt() + 1)
                    val currTime = CurrDateTimeInMillis()
                    if ((currTime >= LiveBonusTimeInfo.value!!.StartTime) &&
                        (currTime < LiveBonusTimeInfo.value!!.EndTime)
                    ) {
                        // do nothing as this is the bonus time
                    } else {
                        //update live auction list item by closing this entry
                        closeLiveAuctionListItem(dataSnapshot)
                    }
                }
            }
        }
    }

    fun fetchLiveAuctionList() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(
                dataSnapshot: DataSnapshot,
                previousChildName: String?,
            ) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                val changedEntry = dataSnapshot.getValue<LiveAuctionListItem>()
                liveAuctionList[changedEntry!!.CurrNo] = changedEntry
                LiveAuctionList.value = liveAuctionList
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                val changedEntry = dataSnapshot.getValue<LiveAuctionListItem>()
                liveAuctionList[changedEntry!!.CurrNo] = changedEntry
                LiveAuctionList.value = liveAuctionList

                updateRoutesListOnBidClose(dataSnapshot)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                val removedEntry = dataSnapshot.getValue<LiveAuctionListItem>()
                liveAuctionList.remove(removedEntry!!.CurrNo)
                LiveAuctionList.value = liveAuctionList
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                //no action
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG,
                    "Retrieving LiveAuctionList Failed:onCancelled",
                    databaseError.toException())
            }
        }
        val ref = firebaseDb.reference.child(LIVE_AUCTION_LIST)
        ref.addChildEventListener(childEventListener)
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
                    splittedWords = it.id.split("-")
                    src = splittedWords[0]
                    des = splittedWords[1]
                    data = splittedWords[2]
                    value = it.get("Value").toString()
                    when (data) {
                        "Rate" -> {
                            if (liveRoutesList.contains(src)) {
                                liveRoutesList[src]!!.desData[des]!![data] = value
                            } else {
                                liveRoutesList[src] = LiveRoutesListItem(
                                    desData = hashMapOf(
                                        des to hashMapOf(
                                            "Req" to "",
                                            "Got" to "",
                                            "Rate" to value
                                        )
                                    )
                                )
                            }

                        }
                        "Req" -> {
                            if (liveRoutesList.contains(src)) {
                                liveRoutesList[src]!!.desData[des]!![data] = value
                            } else {
                                liveRoutesList[src] = LiveRoutesListItem(
                                    desData = hashMapOf(
                                        des to hashMapOf(
                                            "Req" to value,
                                            "Got" to ""                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           ,
                                            "Rate" to ""
                                        )
                                    )
                                )
                            }
                        }
                        "Got" -> {""
                            if (liveRoutesList.contains(src)) {
                                liveRoutesList[src]!!.desData[des]!![data] = value
                            } else {
                                liveRoutesList[src] = LiveRoutesListItem(
                                    desData = hashMapOf(
                                        des to hashMapOf(
                                            "Req" to "",
                                            "Got" to value,
                                            "Rate" to ""
                                        )
                                    )
                                )
                            }
                        }
                    }
                    Log.w(TAG, "Updated list for ${it.id} = ${it.data}")
                }
            }
            LiveRoutesList.value = liveRoutesList
        }

        /*val childEventListener = object : ChildEventListener {
            override fun onChildAdded(
                dataSnapshot: DataSnapshot,
                previousChildName: String?,
            ) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                val changedEntry = dataSnapshot.getValue<LiveRoutesListItem>()
                liveRoutesList[dataSnapshot.key.toString()] = changedEntry!!
                LiveRoutesList.value = liveRoutesList
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                val changedEntry = dataSnapshot.getValue<LiveRoutesListItem>()
                liveRoutesList[dataSnapshot.key.toString()] = changedEntry!!
                LiveRoutesList.value = liveRoutesList
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                //val removedEntry = dataSnapshot.getValue<LiveRoutesListItem>()
                liveRoutesList.remove(dataSnapshot.key.toString())
                LiveRoutesList.value = liveRoutesList
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                //no action
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG,
                    "Retrieving LiveTruckData Failed:onCancelled",
                    databaseError.toException())
            }
        }
        val ref = firebaseDb.reference.child(LIVE_ROUTES_LIST)
        ref.addChildEventListener(childEventListener)*/
    }

    fun saveLiveAuctionList() {
        val dataToUpload = HashMap<String, HistoryAuctionListItemDTO>()
        LiveAuctionList.value!!.forEach {
            dataToUpload[it.key] = HistoryAuctionListItemDTO(truck_no = it.value.TruckNo,
                bid_closed = it.value.Closed)
        }
        firestoreDb.collection(AUCTION_LIST_DATA)
            .document(LiveAuctionTimestamp.value.toString())
            .set(dataToUpload).addOnSuccessListener {
                Log.d(TAG, "Live auction list saved successfully!")
            }.addOnFailureListener {
                Log.d(TAG, "Failed to save live auction list, please try again!")
            }

    }

    suspend fun saveLiveRoutesList() {
        val dataToUpload = HashMap<String, HistoryAuctionListItemDTO>()
        LiveAuctionList.value!!.forEach {
            dataToUpload[it.key] = HistoryAuctionListItemDTO(truck_no = it.value.TruckNo,
                bid_closed = it.value.Closed)
        }
        firestoreDb.collection(AUCTION_LIST_DATA)
            .document(LiveAuctionTimestamp.value.toString())
            .set(dataToUpload).addOnSuccessListener {
                Log.d(TAG, "Live auction list saved successfully!")
            }.addOnFailureListener {
                Log.d(TAG, "Failed to save live auction list, please try again!")
            }
        delay(1000)

        //update timestamp
        firestoreDb.collection(AUCTION_LIST_DATA)
            .document(LiveAuctionTimestamp.value.toString())
            .set(hashMapOf("Timestamp" to CurrDateTimeInMillis()))
    }

    private fun getTruckOwner(truckNo: String): String {
        return liveTruckDataList[truckNo]!!.data["Owner"].toString()
    }

    //checks if the truck status is in progress
    private fun truckInProgress(truckNo: String): Boolean {
        return LiveTruckDataList.value!![truckNo]!!.data["Status"] == "DelInProg"
    }


    /**
     * Move to firestore
     */
    fun fetchAddTruckRequests() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(
                dataSnapshot: DataSnapshot,
                previousChildName: String?,
            ) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                // A new comment has been added, add it to the displayed list
                val changedEntry = dataSnapshot.getValue<AddTruckRequest>()
                truckRequestsLive[dataSnapshot.key.toString()] = changedEntry!!
                TruckRequestsLive.value = truckRequestsLive
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
                Log.w(
                    TAG, "Retrieving AddTrucksRequests Failed:onCancelled",
                    databaseError.toException()
                )
            }
        }
        val ref = firebaseDb.reference.child(ADD_TRUCKS_REQUESTS)
        ref.addChildEventListener(childEventListener)
    }

    /**
     * Move to firestore
     */
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

    /**
     * Move to firestore
     */
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
}