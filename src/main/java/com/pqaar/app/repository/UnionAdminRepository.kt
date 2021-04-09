package com.pqaar.app.repository

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.pqaar.app.model.HistoryAuctionListItemDTO
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.LiveTruckDataListItem
import kotlinx.coroutines.tasks.await
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@SuppressLint("StaticFieldLeak")
object UnionAdminRepository {
    private const val TAG = "UnionAdminRepository"
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var firebaseDb: FirebaseDatabase
    private var totalTrucksRequired = 0

    private var lastAuctionListDTO: ArrayList<Pair<String, HistoryAuctionListItemDTO>> = ArrayList()
    private var truckCheckArray: HashMap<String, Boolean> = HashMap()
    private var liveTruckDataList: ArrayList<LiveTruckDataListItem> = ArrayList()
    private var lastOpenLiveList: ArrayList<LiveAuctionListItem> = ArrayList()
    private var lastClosedLiveList: ArrayList<LiveAuctionListItem> = ArrayList()
    private var lastMissedList: ArrayList<LiveAuctionListItem> = ArrayList()
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
        liveTruckDataList = ArrayList()

        firebaseDb.reference.child("LiveTruckData").get().addOnSuccessListener {
            Log.i("firebase", "Got data ${it.value}")
            (it.value as HashMap<*, *>).forEach { entry ->
                val tempHashMap = HashMap<String, String>()
                (entry.value as HashMap<*, *>).forEach { innerEntry ->
                    tempHashMap[innerEntry.key.toString()] = innerEntry.value.toString()
                }
                liveTruckDataList.add(
                    LiveTruckDataListItem(
                        entry.key.toString(),
                        tempHashMap
                    )
                )
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
    for (liveTruckDataItem in liveTruckDataList) {
            /**
             * if there is a truck which was not in the last auction
             * and whose status is "not in progress", initialize it in the
             * last missed list.
             *
             * Note: The timestamp added in the missed list here, is just to do sorting
             * which is done in the next, step. But this timestamp's original motive
             * is to store the timestamp information during the live auction.
             */
            if (!truckCheckArray.containsKey(liveTruckDataItem.truckNo) &&
                !truckInProgress(liveTruckDataItem.truckNo)
            ) {
                lastMissedList.add(
                    LiveAuctionListItem(
                        "", liveTruckDataItem.data["CurrentListNo"]!!,
                        liveTruckDataItem.truckNo,
                        "true", "", "", liveTruckDataItem.data["Timestamp"]!!.toInt()
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
        liveCombinedAuctionList = (lastMissedList + lastOpenLiveList + lastClosedLiveList) as ArrayList<LiveAuctionListItem>
    }

    suspend  fun uploadRoutesList(liveRoutesList: ArrayList<LiveRoutesListItem>) {
        liveRoutesList.forEach{
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
    suspend fun setAuctionStatus(status: String, timestamp: String) {
        /*if (status == "") {
            firebaseDb
                .reference
                .child("LiveRoutesList")
                .setValue(liveRoutesList)
            Log.d(TAG, "Live routes list uploaded successfully!")
        } else {
            Log.e(TAG, "Live routes list is empty!")
        }*/
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun timestampToMillis(timestamp: String): Long {
        val sdf = SimpleDateFormat("dd-M-yyyy hh:mm:ss")
        val date = sdf.parse(timestamp)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        return calendar.timeInMillis
    }

    @SuppressLint("SimpleDateFormat")
    fun millisToTimestamp(millis: Long): String {
        val simple: DateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
        val result = Date(millis)
        return (simple.format(result)).toString()
    }

    //checks if the truck status is in progress
    private fun truckInProgress(truckNo: String): Boolean {
        var truckInProgress = false
        for (liveTruckDataItem in liveTruckDataList) {
            if (liveTruckDataItem.truckNo == truckNo) {
                if (liveTruckDataItem.data["Status"] == "DelInProg") {
                    return true
                }
            }
        }
        return truckInProgress
    }
}



