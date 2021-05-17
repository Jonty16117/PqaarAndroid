package com.pqaar.app.truckOwner.repository

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pqaar.app.model.*
import com.pqaar.app.utils.DbPaths
import com.pqaar.app.utils.DbPaths.FIRST_NAME
import com.pqaar.app.utils.DbPaths.LAST_NAME
import com.pqaar.app.utils.DbPaths.LIVE_AUCTION_LIST
import com.pqaar.app.utils.DbPaths.LIVE_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.PHONE_NO
import com.pqaar.app.utils.DbPaths.TRUCKS
import com.pqaar.app.utils.DbPaths.TRUCK_RC
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.DbPaths.TRUCK_REQUESTS
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream


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

    private val auth = FirebaseAuth.getInstance()
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val firebaseDb = FirebaseDatabase.getInstance()
    private val firebaseSt = FirebaseStorage.getInstance()

    private var truckOwnerLiveData = TruckOwner()
    private var liveRoutesList = HashMap<String, LiveRoutesListItemDTO>()
    private var liveAuctionList = HashMap<String, LiveAuctionListItem>()
    private var liveTruckDataList = HashMap<String, LiveTruckDataItem>()

    val TruckOwnerLiveData = MutableLiveData<TruckOwner>()
    val LiveRoutesList = MutableLiveData<HashMap<String, LiveRoutesListItemDTO>>()
    val LiveAuctionList = MutableLiveData<HashMap<String, LiveAuctionListItem>>()
    val LiveTruckDataList = MutableLiveData<HashMap<String, LiveTruckDataItem>>()
    val LiveBonusTimeInfo = MutableLiveData<BonusTime>()
    val LiveAuctionTimestamps = MutableLiveData<Pair<Long, Long>>()

    suspend fun fetchTruckOwner() {
//        val testUid = "DemoUserTO"
        firestoreDb.collection(USER_DATA)
            .document(auth.uid!!).get()
            .addOnSuccessListener {
                truckOwnerLiveData.FirstName = it.get(FIRST_NAME).toString()
                truckOwnerLiveData.LastName = it.get(LAST_NAME).toString()
                truckOwnerLiveData.PhoneNo = it.get(PHONE_NO).toString()
                val trucksArrayList = ArrayList<String>()
                if (it.get(TRUCKS) != null) {
                    val trucks = it.get(TRUCKS) as List<*>
                    trucks.forEach { trucksArrayList.add(it.toString()) }
                }
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
                TAG, "Updated live auction list: ${LiveAuctionList.value}"
            )
        }
    }

    suspend fun closeBid(truckNo: String, src: String, des: String) {
        val dataToUpdate = hashMapOf<String, Any>("src" to src, "des" to des)
        val currListNo = liveTruckDataList[truckNo]!!.CurrentListNo
        firestoreDb.collection(LIVE_AUCTION_LIST)
            .document(currListNo)
            .update(dataToUpdate)
            .addOnSuccessListener {
                Log.d(
                    TAG, "Request to close the bid for truck ${truckNo} " +
                            "sent!"
                )
            }
            .addOnFailureListener {
                Log.d(
                    TAG, "Failed to send request to close the " +
                            "bid for truck ${truckNo}!, error: ${it}"
                )
            }.await()
    }

    fun fetchLiveTruckDataList() {
        val col = firestoreDb.collection(LIVE_TRUCK_DATA_LIST)

        if (truckOwnerLiveData.Trucks.isNotEmpty()) {
            truckOwnerLiveData.Trucks.forEach { truck ->
                col.document(truck).addSnapshotListener { truckDocument, error ->
                    if (error == null) {
                        Log.d(TAG, "truck: ${truck} doc: ${truckDocument}")
                        val liveTruckDataItem = LiveTruckDataItem()
                        liveTruckDataItem.TruckNo = truckDocument!!.get("TruckNo").toString()
                        liveTruckDataItem.CurrentListNo =
                            truckDocument.get("CurrentListNo").toString()
                        liveTruckDataItem.Status = truckDocument.get("Status").toString()
                        val timestamp =
                            truckDocument.get("Timestamp") as com.google.firebase.Timestamp
                        val millis = timestamp.seconds * 1000 + timestamp.nanoseconds * 1000000
                        liveTruckDataItem.Timestamp = millis.toString()
                        val firstName = truckDocument.get("OwnerFirstName").toString()
                        val lastName = truckDocument.get("OwnerFirstName").toString()
                        liveTruckDataItem.Owner = Pair(firstName, lastName)
//                        liveTruckDataItem.Owner = (truckDocument.get("Owner") as List<*>)
//                                .zipWithNext { a, b -> Pair(a.toString(), b.toString()) }[0]
                        liveTruckDataItem.Route = Pair(
                            truckDocument.get("Source").toString(),
                            truckDocument.get("Destination").toString()
                        )
                        liveTruckDataList[truckDocument.get("TruckNo").toString()] =
                            liveTruckDataItem
                        LiveTruckDataList.postValue(liveTruckDataList)
                        Log.d(TAG, "after each truck update: ${liveTruckDataItem}")
                    } else {
                        Log.d(TAG, "unable to update live truck data, error: ${error}")
                    }
                }
            }
        }
    }

    fun fetchSchAuctionsInfo() {
        firestoreDb.collection(DbPaths.AUCTIONS_INFO)
            .document(DbPaths.SCHEDULED_AUCTIONS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(
                        TAG, "Failed to fetch auction info!"
                    )
                } else {
                    val startTime: Long = snapshot!!.getLong("StartTime")!!
                    val endTime: Long = snapshot.getLong("EndTime")!!
                    LiveAuctionTimestamps.value = Pair(startTime, endTime)
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

    suspend fun sendAddTruckReq(
        rcFront: Bitmap,
        rcBack: Bitmap,
        truckNo: String,
        truckRC: String,
    ) {
        val userFolder = auth.uid.toString()

        //For testing only
//        val userFolder = "DemoUserTO"

        val rcFrontPath = "${TRUCK_RC}/${userFolder}/Front/rc_front.jpeg"
        val rcFrontref = firebaseSt.reference.child(rcFrontPath)
        val baos1 = ByteArrayOutputStream()
        rcFront.compress(Bitmap.CompressFormat.JPEG, 50, baos1)
        val rcFrontData = baos1.toByteArray()
        rcFrontref.putBytes(rcFrontData).addOnSuccessListener {
            val frontRCURL = rcFrontref.downloadUrl.toString()
            var backRCURL = ""
            GlobalScope.launch(Dispatchers.IO) {
                val job1 = async {
                    val rcBackPath = "${TRUCK_RC}/${userFolder}/Back/rc_back.jpeg"
                    val rcBackref = firebaseSt.reference.child(rcBackPath)
                    val baos2 = ByteArrayOutputStream()
                    rcBack.compress(Bitmap.CompressFormat.JPEG, 50, baos2)
                    val rcBackData = baos2.toByteArray()
                    rcBackref.putBytes(rcBackData).addOnSuccessListener {
                        backRCURL = rcBackref.downloadUrl.toString()
                    }.await()
                }
                job1.await()

                delay(2000)

                val job2 = async {
                    //add req doc to TRUCK_REQUESTS collection
                    val dataToUpload = hashMapOf(
                        "FirstName" to truckOwnerLiveData.FirstName,
                        "LastName" to truckOwnerLiveData.LastName,
                        "OwnerUId" to auth.uid,
                        "TruckNo" to truckNo,
                        "TruckRC" to truckRC,
                        "RequestType" to "Add",
                        "RequestStatus" to null,
                        "Timestamp" to FieldValue.serverTimestamp(),
                        "FrontRCURL" to frontRCURL,
                        "BackRCURL" to backRCURL
                    )
                    firestoreDb
                        .collection(TRUCK_REQUESTS)
                        .document(truckNo)
                        .set(dataToUpload)
                }
                job2.await()
            }
        }

        /**
         * truckNo,
         * firstName,
         * lastName,
         * ownerUId,
         * requestType,
         * truckRC,
         * frontRCURL,
         * backRCURL,
         * requestStatus: null,
         * timestamp,
         */

    }

    suspend fun sendRemoveTruckReq(
        truckNo: String
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val job = async {
                //add req doc to TRUCK_REQUESTS collection
                val dataToUpload = hashMapOf(
                    "TruckNo" to truckNo,
                    "RequestType" to "Remove",
                    "RequestStatus" to null,
                    "Timestamp" to FieldValue.serverTimestamp(),
                )
                firestoreDb
                    .collection(TRUCK_REQUESTS)
                    .document(truckNo)
                    .update(dataToUpload)
            }
            job.await()
        }
    }

}