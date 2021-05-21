package com.pqaar.app.truckOwner.repository

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pqaar.app.model.*
import com.pqaar.app.utils.DbPaths.ADD
import com.pqaar.app.utils.DbPaths.AUCTIONS_INFO
import com.pqaar.app.utils.DbPaths.AUCTION_BONUS_TIME_INFO
import com.pqaar.app.utils.DbPaths.BACK_RC_URL
import com.pqaar.app.utils.DbPaths.CURRENT_LIST_NO
import com.pqaar.app.utils.DbPaths.CURR_NO
import com.pqaar.app.utils.DbPaths.DES
import com.pqaar.app.utils.DbPaths.DESTINATION
import com.pqaar.app.utils.DbPaths.DUMMY_DOC
import com.pqaar.app.utils.DbPaths.END_TIME
import com.pqaar.app.utils.DbPaths.FIRSTNAME
import com.pqaar.app.utils.DbPaths.FIRST_NAME
import com.pqaar.app.utils.DbPaths.FRONT_RC_URL
import com.pqaar.app.utils.DbPaths.GOT
import com.pqaar.app.utils.DbPaths.IS_CLOSED
import com.pqaar.app.utils.DbPaths.LASTNAME
import com.pqaar.app.utils.DbPaths.LAST_NAME
import com.pqaar.app.utils.DbPaths.LIVE_AUCTION_LIST
import com.pqaar.app.utils.DbPaths.LIVE_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST
import com.pqaar.app.utils.DbPaths.OWNER_FIRST_NAME
import com.pqaar.app.utils.DbPaths.OWNER_LAST_NAME
import com.pqaar.app.utils.DbPaths.OWNER_UID
import com.pqaar.app.utils.DbPaths.PHONE_NO
import com.pqaar.app.utils.DbPaths.PREVIOUS_LIST_NO
import com.pqaar.app.utils.DbPaths.RATE
import com.pqaar.app.utils.DbPaths.REMOVE
import com.pqaar.app.utils.DbPaths.REQ
import com.pqaar.app.utils.DbPaths.REQUEST_STATUS
import com.pqaar.app.utils.DbPaths.REQUEST_TYPE
import com.pqaar.app.utils.DbPaths.SCHEDULED_AUCTIONS
import com.pqaar.app.utils.DbPaths.SOURCE
import com.pqaar.app.utils.DbPaths.SRC
import com.pqaar.app.utils.DbPaths.START_TIME
import com.pqaar.app.utils.DbPaths.STATUS
import com.pqaar.app.utils.DbPaths.TIMESTAMP
import com.pqaar.app.utils.DbPaths.TRUCKS
import com.pqaar.app.utils.DbPaths.TRUCK_NUMBER
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
                if (it.get(TRUCKS) != null || (it.get(TRUCKS) as List<*>).size > 0) {
                    val trucks = it.get(TRUCKS) as List<*>
                    trucks.forEach { trucksArrayList.add(it.toString()) }
                } else {
                    trucksArrayList.clear()
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
                    if (it.id != DUMMY_DOC) {
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
            REQ -> {
                desData[REQ] = value
                desData[GOT] = "0"
                desData[RATE] = "0"
            }
            GOT -> {
                desData[REQ] = "0"
                desData[GOT] = value
                desData[RATE] = "0"
            }
            RATE -> {
                desData[REQ] = "0"
                desData[GOT] = "0"
                desData[RATE] = value
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
                    if (doc.document.id != DUMMY_DOC) {
                        val docData = doc.document
                        liveAuctionList[doc.document.id] = LiveAuctionListItem(
                            CurrNo = docData.data[CURR_NO].toString(),
                            PrevNo = docData.data[PREVIOUS_LIST_NO].toString(),
                            TruckNo = docData.data[TRUCK_NUMBER].toString(),
                            Closed = docData.data[IS_CLOSED].toString(),
                            StartTime = docData.getLong(START_TIME),
                            Des = docData.data[DES].toString(),
                            Src = docData.data[SRC].toString()
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
        val dataToUpdate = hashMapOf<String, Any>(SRC to src, DES to des)
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
        liveTruckDataList.clear()
        if (truckOwnerLiveData.Trucks.isNotEmpty()) {
            truckOwnerLiveData.Trucks.forEach { truck ->
                col.document(truck).addSnapshotListener { truckDocument, error ->
                    if (error == null) {
                        Log.d(TAG, "truck: ${truck} doc: ${truckDocument}")
                        val liveTruckDataItem = LiveTruckDataItem()
                        liveTruckDataItem.TruckNo = truckDocument!!.get(TRUCK_NUMBER).toString()
                        liveTruckDataItem.CurrentListNo =
                            truckDocument.get(CURRENT_LIST_NO).toString()
                        liveTruckDataItem.Status = truckDocument.get(STATUS).toString()
                        /*val timestamp =
                            truckDocument.get(TIMESTAMP) as com.google.firebase.Timestamp
                        val millis = (timestamp.seconds * 1000)*/
//                        Log.d(TAG, "truck timestamp in millis: ${millis}")
                        val timestamp = truckDocument.get(TIMESTAMP)
                        liveTruckDataItem.Timestamp = timestamp.toString()
                        val firstName = truckDocument.get(OWNER_FIRST_NAME).toString()
                        val lastName = truckDocument.get(OWNER_LAST_NAME).toString()
                        liveTruckDataItem.Owner = Pair(firstName, lastName)
                        liveTruckDataItem.Route = Pair(
                            truckDocument.get(SOURCE).toString(),
                            truckDocument.get(DESTINATION).toString()
                        )
                        liveTruckDataList[truckDocument.get(TRUCK_NUMBER).toString()] =
                            liveTruckDataItem
                        LiveTruckDataList.postValue(liveTruckDataList)
                        Log.d(TAG, "after each truck update: ${liveTruckDataItem}")
                    } else {
                        Log.d(TAG, "unable to update live truck data, error: ${error}")
                    }
                }
            }
        }
        else {
            LiveTruckDataList.postValue(liveTruckDataList)
        }
    }

    fun fetchSchAuctionsInfo() {
        firestoreDb.collection(AUCTIONS_INFO)
            .document(SCHEDULED_AUCTIONS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(
                        TAG, "Failed to fetch auction info!"
                    )
                } else {
                    val startTime: Long = snapshot!!.getLong(START_TIME)!!
                    val endTime: Long = snapshot.getLong(END_TIME)!!
                    LiveAuctionTimestamps.value = Pair(startTime, endTime)
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
                            "Fetched updated auction bonus time: ${snapshot.get(START_TIME)}" +
                                    ", ${snapshot.get("EndTime")}!"
                        )
                        Log.d(
                            TAG, "Fetched updated auction bonus time: ${snapshot}!"
                        )
                        Log.d(
                            TAG, "Fetched updated auction bonus time: ${snapshot.data}!"
                        )
                        val liveBonusTimeInfo = BonusTime(
                            StartTime = snapshot.getLong(START_TIME)!!,
                            EndTime = snapshot.getLong(END_TIME)!!
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
        //upload rc images
        val rcFrontPath = "${TRUCK_RC}/${userFolder}/${truckNo}/rc_front.jpeg"
        val rcFrontref = firebaseSt.reference.child(rcFrontPath)
        val baos1 = ByteArrayOutputStream()
        rcFront.compress(Bitmap.CompressFormat.JPEG, 50, baos1)
        val rcFrontData = baos1.toByteArray()
        rcFrontref.putBytes(rcFrontData).addOnSuccessListener { uploadFrontRCTask ->
            rcFrontref.downloadUrl.addOnSuccessListener { uri ->
                val frontRCURL = uri.toString()
                Log.d(TAG, "url: ${frontRCURL}")
                var backRCURL = ""
                val rcBackPath = "${TRUCK_RC}/${userFolder}/${truckNo}/rc_back.jpeg"
                val rcBackref = firebaseSt.reference.child(rcBackPath)
                GlobalScope.launch(Dispatchers.IO) {
                    val job1 = async {
                        val baos2 = ByteArrayOutputStream()
                        rcBack.compress(Bitmap.CompressFormat.JPEG, 50, baos2)
                        val rcBackData = baos2.toByteArray()
                        rcBackref.putBytes(rcBackData).addOnSuccessListener {uploadBackRCTask ->
                            rcBackref.downloadUrl.addOnSuccessListener { uri ->
                                backRCURL = uri.toString()
                            }
                        }.await()
                    }
                    job1.await()

                    delay(2000)

                    val job3 = async {
                        //add req doc to TRUCK_REQUESTS collection
                        val dataToUpload = hashMapOf(
                            FIRSTNAME to truckOwnerLiveData.FirstName,
                            LASTNAME to truckOwnerLiveData.LastName,
                            OWNER_UID to auth.uid,
                            TRUCK_NUMBER to truckNo,
                            TRUCK_RC to truckRC,
                            REQUEST_TYPE to ADD,
                            REQUEST_STATUS to null,
                            TIMESTAMP to FieldValue.serverTimestamp(),
                            FRONT_RC_URL to frontRCURL,
                            BACK_RC_URL to backRCURL
                        )
                        firestoreDb
                            .collection(TRUCK_REQUESTS)
                            .document(truckNo)
                            .set(dataToUpload)
                    }
                    job3.await()
                }
            }
//            val frontRCURL = uploadFrontRCTask.task.result.toString()

        }
    }

    suspend fun sendRemoveTruckReq(
        truckNo: String
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            var frontRCURL = ""
            var backRCURL = ""
            var truckRC = ""
            val job1 = async {
                firestoreDb
                    .collection(LIVE_TRUCK_DATA_LIST)
                    .document(truckNo)
                    .get()
                    .addOnSuccessListener {
                        frontRCURL = it.get(FRONT_RC_URL).toString()
                        backRCURL = it.get(BACK_RC_URL).toString()
                        truckRC = it.get(TRUCK_RC).toString()
                    }.await()
            }
            job1.await()
            val job = async {
                val dataToUpload = hashMapOf(
                    FIRSTNAME to truckOwnerLiveData.FirstName,
                    LASTNAME to truckOwnerLiveData.LastName,
                    OWNER_UID to auth.uid,
                    TRUCK_NUMBER to truckNo,
                    TRUCK_RC to truckRC,
                    REQUEST_TYPE to REMOVE,
                    REQUEST_STATUS to null,
                    TIMESTAMP to FieldValue.serverTimestamp(),
                    FRONT_RC_URL to frontRCURL,
                    BACK_RC_URL to backRCURL
                )
                firestoreDb
                    .collection(TRUCK_REQUESTS)
                    .document(truckNo)
                    .set(dataToUpload)
            }
            job.await()
        }
    }

}