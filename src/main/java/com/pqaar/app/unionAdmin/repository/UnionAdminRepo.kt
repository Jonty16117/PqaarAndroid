package com.pqaar.app.unionAdmin.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.pqaar.app.model.*
import com.pqaar.app.utils.DbPaths.AUCTIONS_INFO
import com.pqaar.app.utils.DbPaths.AUCTION_BONUS_TIME_INFO
import com.pqaar.app.utils.DbPaths.AUCTION_LIST_DATA
import com.pqaar.app.utils.DbPaths.LIVE_AUCTION_LIST
import com.pqaar.app.utils.DbPaths.LIVE_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.MANDI_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.ROUTES_LIST_DATA
import com.pqaar.app.utils.DbPaths.SCHEDULED_AUCTIONS
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.tasks.await
import java.lang.Double.POSITIVE_INFINITY
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis

@SuppressLint("StaticFieldLeak")
object UnionAdminRepo {
    private const val TAG = "UnionAdminRepository"
    private const val CHUNKED_LIST_SIZE = 350

    private var firestoreDb = FirebaseFirestore.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()

//    private var propRoutesList = HashMap<String, MutableMap<String, Any>>()
    private var propRoutesList = ArrayList<LivePropRoutesListItem>()
    private val truckRequestsLive = HashMap<String, AddTruckRequest>()
    private var liveAuctionList = HashMap<String, LiveAuctionListItem>()
    private val liveTruckDataList = HashMap<String, LiveTruckDataListItemDTO>()
    private var liveRoutesList = HashMap<String, LiveRoutesListItemDTO>()
    private var lastAuctionListDTO: ArrayList<Pair<String, HistoryAuctionListItemDTO>> = ArrayList()
    private lateinit var lastOpenLiveList: ArrayList<LiveAuctionListItem>
    private lateinit var lastClosedLiveList: ArrayList<LiveAuctionListItem>
    private lateinit var lastMissedList: ArrayList<LiveAuctionListItem>
    private lateinit var truckCheckArray: HashMap<String, Boolean>
    lateinit var liveCombinedAuctionList: ArrayList<LiveAuctionListItem>
//    val PropRoutesList = MutableLiveData<HashMap<String, MutableMap<String, Any>>>()
    val PropRoutesList = MutableLiveData<ArrayList<LivePropRoutesListItem>>()
    val TruckRequestsLive = MutableLiveData<HashMap<String, AddTruckRequest>>()
    val LiveAuctionList = MutableLiveData<HashMap<String, LiveAuctionListItem>>()
    val LiveTruckDataList = MutableLiveData<HashMap<String, LiveTruckDataListItemDTO>>()
    val LiveRoutesList = MutableLiveData<HashMap<String, LiveRoutesListItemDTO>>()
    val LiveAuctionStatus = MutableLiveData<String>()
    val LiveAuctionStartTime = MutableLiveData<Long>()
    val LiveAuctionEndTime = MutableLiveData<Long>()
    val LiveBonusTimeInfo = MutableLiveData<BonusTime>()


    /**
     * Fetches the proposed routes list from all the mandi admins
     */
    fun fetchLivePropRoutesList() {
        firestoreDb
            .collection(MANDI_ROUTES_LIST)
            .addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.w(
                    TAG, "Failed to update the latest proposed routes lists from" +
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
                propRoutesList = ArrayList()
                snapshots!!.forEach {
                    if (it.id != "DummyDoc") {
                        val mandiPropList = LivePropRoutesListItem()
                        //get the entry from the mandi which has the status live on it
                        it.data.forEach { mandiRoutesData ->
                            Log.d(TAG,"mandiroutesdata: ${mandiRoutesData}")
                            val mandiRoutesDataValue = mandiRoutesData.value as HashMap<*, *>
                            if(mandiRoutesDataValue["Status"] == "Live") {
                                mandiPropList.Timestamp = mandiRoutesDataValue["Timestamp"].toString().toLong()
                                mandiPropList.Mandi = it.id
                                val routes = ArrayList<Pair<String, Int>>()
                                mandiRoutesDataValue.forEach { route ->
                                    if (route.key.toString() != "Status" && route.key.toString() != "Timestamp") {
                                        routes.add(Pair(route.key.toString(), route.value.toString().toInt()))
                                    }
                                }
                                mandiPropList.Routes = routes
                            }
                        }
                        propRoutesList.add(mandiPropList)
                    }
                }
                Log.w(TAG, "Updated proposed routes list ${propRoutesList}")
                PropRoutesList.postValue(propRoutesList)
            }
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
                    batch.set(doc, hashMapOf("Value" to it.second))
                }
            }.addOnSuccessListener {
                Log.d(TAG, "Batch upload successful for ${index + 1} mandi documents!")
            }.addOnFailureListener {
                Log.d(TAG, "Failed to upload a batch of ${index + 1} mandi documents!")
            }.await()
            delay(2000)
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
        lastAuctionListDTO = ArrayList()
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
                                when (it.key.toString()) {
                                    "truck_no" -> {
                                        historyAuctionListItemDTO.truck_no = it.value.toString()
                                    }
                                    "bid_closed" -> {
                                        historyAuctionListItemDTO.bid_closed = it.value.toString()
                                    }
                                    "src" -> {
                                        historyAuctionListItemDTO.src = it.value.toString()
                                    }
                                    "des" -> {
                                        historyAuctionListItemDTO.des = it.value.toString()
                                    }
                                }
                            }
                            lastAuctionListDTO.add(Pair(seqNo.key, historyAuctionListItemDTO))
                        }
                    }
                }
            }.await()

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
        Log.d(TAG, "LastAuctionListDTO: $lastAuctionListDTO")
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
        Log.d(TAG, "Searching for free trucks in available total trucks:" +
                " ${LiveTruckDataList.value!!.keys}")

        for (truck in LiveTruckDataList.value!!.keys) {
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
                        LiveTruckDataList.value!![truck]!!.data["CurrentListNo"]!!,
                        TruckNo = truck,
                        StartTime = LiveTruckDataList.value!![truck]!!.data["Timestamp"]!!.toLong()
                    )
                )
            }
        }

        /**
         * sort the newly generated missed list according
         * to the timestamp, in the ascending order
         */
        lastMissedList = ArrayList(lastMissedList.sortedWith(compareBy { it.StartTime }))
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

    /**
     * Assign the sequence numbers and set the starting unlock time
     * to each truck in live auction list.
     */
    fun setStartTimeInLiveAuctionList(
        auctionStartTime: Long,
        perUserBidDurationInMillis: Long,
    ) {
        liveCombinedAuctionList.forEachIndexed { index, item ->
            item.StartTime =
                auctionStartTime + (perUserBidDurationInMillis * (index + 1))
            item.CurrNo = (index + 1).toString()
        }
        Log.d(TAG, "After setting CurrNo & StartTime in live auction" +
                "list = $liveCombinedAuctionList")
    }

    suspend fun uploadLiveAuctionList() {
        val col = firestoreDb.collection(LIVE_AUCTION_LIST)
        Log.w(TAG, "Live Auction List to upload: $liveCombinedAuctionList")
        val batchedList = liveCombinedAuctionList.chunked(CHUNKED_LIST_SIZE)
        Log.w(TAG, "Batched Live Auction List: ${batchedList}")
        batchedList.forEachIndexed { index, ls ->
            firestoreDb.runBatch { batch ->
                ls.forEach {
                    var doc = col.document(it.CurrNo!!)
                    batch.set(doc, it)
                }
            }.addOnSuccessListener {
                Log.w(TAG, "Batch ${index + 1} of size ${ls.size} (${ls}) uploaded successfully!")
            }.addOnFailureListener {
                Log.w(TAG, "Batch ${index + 1} of size ${ls.size} (${ls}) failed to upload!")
            }.await()
            delay(2000)
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
     * Finished -> When there is no live auction and none is scheduled
     * (Timestamp -> Timestamp of when the last auction ended)
     */
    suspend fun uploadSchAuctionsInfo(status: String, startTime: Long, endTime: Long) {
        val dataToUpload = hashMapOf(
            "Status" to status,
            "StartTime" to startTime,
            "EndTime" to endTime
        )
        firestoreDb.collection(AUCTIONS_INFO)
            .document(SCHEDULED_AUCTIONS).set(dataToUpload)
            .addOnSuccessListener {
                LiveAuctionStatus.value = status
                LiveAuctionStartTime.value = startTime
                LiveAuctionEndTime.value = endTime

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
                        val liveBonusTimeInfo = BonusTime(
                            StartTime = snapshot.getLong("StartTime")!!,
                            EndTime = snapshot.getLong("EndTime")!!
                        )
                        LiveBonusTimeInfo.value = liveBonusTimeInfo
                    }
                }
            }
    }

    suspend fun closeLiveAuctionListItem(changedEntry: LiveAuctionListItem) {
        val doc = firestoreDb.collection(LIVE_AUCTION_LIST)
            .document(changedEntry.CurrNo!!)
        doc.update("closed", "true").addOnSuccessListener {
            Log.d(TAG, "Bid closed for truck no: ${changedEntry.TruckNo}")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to close bid for truck no: ${changedEntry.TruckNo}")
        }.await()
    }

    suspend fun updateRouteItem(src: String, des: String, newGot: Int) {
        val docName = "${src}-${des}-Got"
        val doc = firestoreDb.collection(LIVE_ROUTES_LIST).document(docName)
        doc.set(hashMapOf("Value" to newGot)).addOnSuccessListener {
            Log.d(TAG, "Route list for ${src} updated, ${newGot} trucks filled")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to update live route list for ${src}")
        }.await()
    }

    /**
     * Check if the user has updated the routes
     */
    suspend fun updateRoutesListOnBidClose(changedEntry: LiveAuctionListItem) {
        val src = changedEntry.Src!!.trim()
        val des = changedEntry.Des!!.trim()
        val closed = changedEntry.Closed!!.trim()
        val startTime =
            if (changedEntry.StartTime == null) POSITIVE_INFINITY.toLong() else changedEntry.StartTime!!

        Log.d(TAG, "Checking live routes for: $changedEntry")
        if ((CurrDateTimeInMillis() > startTime) &&
            (closed == "false") && (src.isNotEmpty()) && (des.isNotEmpty())
        ) {
            Log.d(TAG, "Incoming request to close the bid from mandi: " +
                    "${src} to ${des}")
            Log.d(TAG, "Live mandis: ${liveRoutesList.keys}")

            //check if src(mandi) and des(godown) exists in the live routes list
            if (liveRoutesList.containsKey(src) &&
                (liveRoutesList[src]!!.desData.containsKey(des))
            ) {
                val req = liveRoutesList[src]!!.desData[des]!!["Req"]!!.toInt()
                val got = liveRoutesList[src]!!.desData[des]!!["Got"]!!.toInt()
                Log.d(TAG, "req: ${req}, got: ${got}")

                //check if the bidd route is still available in the live route list
                if ((req - got) > 0) {
                    updateRouteItem(src = src, des = des,
                        newGot = got + 1)
                    val currTime = CurrDateTimeInMillis()

                    //check to see if the current request falls in bonus time
                    if ((currTime >= LiveBonusTimeInfo.value!!.StartTime) &&
                        (currTime < LiveBonusTimeInfo.value!!.EndTime)
                    ) {
                        /**
                         * Set the timer to start at infinity for this user to prevent
                         * this user from requesting again
                         */
                        Log.d(TAG, "Setting the start timer to infinity" +
                                " for: ${changedEntry}")
                        StartTimerForLiveAuctionListItem(changedEntry)
                    } else {
                        //if not, then update live auction list item by closing this entry
                        closeLiveAuctionListItem(changedEntry)
                    }
                }
            }
        } else {
            Log.d(TAG, "Request to accept bid rejected for: ${changedEntry}")
        }
    }

    private suspend fun StartTimerForLiveAuctionListItem(changedEntry: LiveAuctionListItem) {
        val doc = firestoreDb.collection(LIVE_AUCTION_LIST)
            .document(changedEntry.CurrNo!!)
        doc.update("StartTime", POSITIVE_INFINITY.toLong()).addOnSuccessListener {
            Log.d(TAG, "Timer closed for for truck no: ${changedEntry.TruckNo}")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to close the timer for truck no: ${changedEntry.TruckNo}")
        }.await()
    }

    suspend fun fetchLiveAuctionList() {
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
                        when (doc.type) {
                            DocumentChange.Type.ADDED -> {
                                        Log.d(TAG,
                                            "Added new live auction list item: ${doc.document.data}")
                            }
                            DocumentChange.Type.MODIFIED -> {
                                GlobalScope.launch(Dispatchers.IO) {
                                    val executionTime = measureTimeMillis {
                                        val job = async {
                                            Log.d(TAG,
                                                "before update routes: ${liveAuctionList[doc.document.id]!!}")
                                            updateRoutesListOnBidClose(liveAuctionList[doc.document.id]!!)
                                        }
                                        job.await()
                                    }
                                    withContext(Dispatchers.Main) {
                                        Log.d(TAG, "ExecutionTime = $executionTime")
                                        Log.d(TAG,
                                            "Modified live auction list item: ${doc.document.data}")
                                    }
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                liveAuctionList.remove(doc.document.id)
                                Log.d(TAG,
                                    "Removed live auction list item: ${doc.document.data}")
                            }
                        }
                    }
                }
                LiveAuctionList.value = liveAuctionList
            }
            Log.w(
                TAG, "Updated live auction list: ${LiveAuctionList.value}")
        }
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



    /*fun fetchLiveRoutesList() {
        val coll = firestoreDb.collection(LIVE_ROUTES_LIST)
        coll.addSnapshotListener { routes, error ->
            if (error != null) {
                Log.w(
                    TAG, "Failed to update the live routes list"
                )
            } else {
                var src: String *//*Mandi*//*
                var des: String *//*Destination*//*
                var data: String *//*Represents specifier for Rate, Requirement or Got*//*
                var value: String *//*Contains the value of the specified specifier*//*
                var splittedWords: List<String>
                routes!!.forEach {
                    if (it.id != "DummyDoc") {
                        splittedWords = it.id.split("-")
                        src = splittedWords[0]
                        des = splittedWords[1]
                        data = splittedWords[2]
                        value = it.get("Value").toString()
                        when (data) {
                            "Rate" -> {
                                if (liveRoutesList.containsKey(src)) {
                                    liveRoutesList[src]!!.desData[des]!![data] = value
                                } else {
                                    liveRoutesList[src] = LiveRoutesListItemDTO(
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
                                if (liveRoutesList.containsKey(src)) {
                                    liveRoutesList[src]!!.desData[des]!![data] = value
                                } else {
                                    liveRoutesList[src] = LiveRoutesListItemDTO(
                                        desData = hashMapOf(
                                            des to hashMapOf(
                                                "Req" to value,
                                                "Got" to "",
                                                "Rate" to ""
                                            )
                                        )
                                    )
                                }
                            }
                            "Got" -> {
                                if (liveRoutesList.containsKey(src)) {
                                    liveRoutesList[src]!!.desData[des]!![data] = value
                                } else {
                                    liveRoutesList[src] = LiveRoutesListItemDTO(
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
            }
            LiveRoutesList.value = liveRoutesList
        }
    }*/

    suspend fun saveLiveAuctionList() {
        val dataToUpload = HashMap<String, HistoryAuctionListItemDTO>()
        LiveAuctionList.value!!.forEach {
            dataToUpload[it.key] = HistoryAuctionListItemDTO(
                truck_no = it.value.TruckNo!!,
                bid_closed = it.value.Closed!!,
                src = it.value.Src!!,
                des = it.value.Des!!)
        }
        val currTimestamp = CurrDateTimeInMillis().toString()
        firestoreDb.collection(AUCTION_LIST_DATA)
            .document(currTimestamp)
            .set(dataToUpload).addOnSuccessListener {
                Log.d(TAG, "Live auction list saved successfully!")
            }.addOnFailureListener {
                Log.d(TAG, "Failed to save live auction list, please try again!")
            }.await()
        delay(1000)

        //update timestamp
        firestoreDb.collection(AUCTION_LIST_DATA)
            .document(currTimestamp)
            .update("Timestamp", currTimestamp).await()
    }

    suspend fun saveLiveRoutesList() {
        val dataToUpload = HashMap<String, HashMap<String, HashMap<String, String>>>()
        LiveRoutesList.value!!.forEach {
            dataToUpload[it.key] = it.value.desData
        }
        val currTimestamp = CurrDateTimeInMillis().toString()
        firestoreDb.collection(ROUTES_LIST_DATA)
            .document(currTimestamp)
            .set(dataToUpload).addOnSuccessListener {
                Log.d(TAG, "Live routes list saved successfully!")
            }.addOnFailureListener {
                Log.d(TAG, "Failed to save live routes list, please try again!")
            }.await()
        delay(1000)

        //update timestamp
        firestoreDb.collection(ROUTES_LIST_DATA)
            .document(currTimestamp)
            .update("Timestamp", currTimestamp.toLong()).await()
    }

    suspend fun clearLiveAuctionList() {
        val col = firestoreDb.collection(LIVE_AUCTION_LIST)
        val docsToDelete = ArrayList<String>()
        LiveAuctionList.value!!.forEach {
            if (it.key != "DummyDoc") {
                docsToDelete.add(it.key)
            }
        }
        val batchedList = docsToDelete.chunked(CHUNKED_LIST_SIZE)
        var doc: DocumentReference
        Log.w(TAG, "Deleting live auction list batch: ${batchedList}")
        batchedList.forEachIndexed { index, listBatch ->
            firestoreDb.runBatch { batch ->
                listBatch.forEach { seqNo ->
                    doc = col.document(seqNo)
                    batch.delete(doc)
                }
            }.addOnSuccessListener {
                Log.w(TAG, "Batch ${index + 1} of size ${listBatch.size} deleted successfully!")
            }.addOnFailureListener {
                Log.w(TAG, "Batch ${index + 1} of size ${listBatch.size} failed to delete!")
            }
            delay(2000)
        }
        LiveAuctionList.value!!.clear()
    }

    suspend fun clearLiveAuctionListDebug() {
        val col = firestoreDb.collection(LIVE_AUCTION_LIST)

        /**
         * Get all documents name
         */
        val docsToDelete = ArrayList<String>()
        col.get().addOnSuccessListener { docSnapshots ->
            if (docSnapshots != null) {
                docSnapshots.documents.forEach {
                    docsToDelete.add(it.id)
                }
            } else {
                Log.d(TAG, "Error fetching document names")
            }
        }.await()

        /**
         * Delete all documents batch-wise
         */
        val batchedList = docsToDelete.chunked(CHUNKED_LIST_SIZE)
        var doc: DocumentReference
        Log.w(TAG, "Deleting live auction list batch: ${batchedList}")
        batchedList.forEachIndexed { index, listBatch ->
            firestoreDb.runBatch { batch ->
                listBatch.forEach { docName ->
                    doc = col.document(docName)
                    batch.delete(doc)
                }
            }.addOnSuccessListener {
                Log.w(TAG, "Batch ${index + 1} of size ${listBatch.size} deleted successfully!")
            }.addOnFailureListener {
                Log.w(TAG, "Batch ${index + 1} of size ${listBatch.size} failed to delete!")
            }
            delay(2000)
        }
        LiveAuctionList.value!!.clear()
    }

    suspend fun clearLiveRoutesList() {
        val col = firestoreDb.collection(LIVE_ROUTES_LIST)
        val docsToDelete = ArrayList<String>()
        LiveRoutesList.value!!.forEach { mandi ->
            mandi.value.desData.forEach { destination ->
                docsToDelete.add("${mandi.key.trim()}-${destination.key}-Req")
                docsToDelete.add("${mandi.key.trim()}-${destination.key}-Rate")
                docsToDelete.add("${mandi.key.trim()}-${destination.key}-Got")
            }
            LiveRoutesList.value!!.clear()
        }
        Log.d(TAG, "Live routes list to delete: ${docsToDelete}")
        val batchedList = docsToDelete.chunked(CHUNKED_LIST_SIZE)
        var doc: DocumentReference
        Log.w(TAG, "Deleting ${batchedList.size} batch(es) of live routes list")
        batchedList.forEachIndexed { index, listBatch ->
            firestoreDb.runBatch { batch ->
                listBatch.forEach { docName ->
                    doc = col.document(docName)
                    batch.delete(doc)
                }
            }.addOnSuccessListener {
                Log.w(TAG, "Batch ${index + 1} of size ${listBatch.size} deleted successfully!")
            }.addOnFailureListener {
                Log.w(TAG, "Batch ${index + 1} of size ${listBatch.size} failed to delete!")
            }
            delay(2000)
        }
    }

    private fun getTruckOwner(truckNo: String): String {
        return LiveTruckDataList.value!![truckNo]!!.data["Owner"].toString()
    }

    //checks if the truck status is in progress
    private fun truckInProgress(truckNo: String): Boolean {
        Log.d(TAG, "Finding truck status of ${truckNo}")
        Log.d(TAG, "${LiveTruckDataList.value}")
        Log.d(TAG,
            "Finding truck status of ${truckNo}....found: ${LiveTruckDataList.value!![truckNo]!!.data["Status"] == "DelInProg"}")
        val ans = LiveTruckDataList.value!![truckNo]!!.data["Status"] == "DelInProg"
        return ans
    }


    /**
     * Move to firestore
     */
    /*fun fetchAddTruckRequests() {
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
*/
    /**
     * Move to firestore
     */
    /*suspend fun addTruckToUser(truckRequest: AddTruckRequest) {
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
*/
    /**
     * Move to firestore
     */
/*
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
*/
}