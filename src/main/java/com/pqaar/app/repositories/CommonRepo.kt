package com.pqaar.app.repositories

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.LiveTruckDataListItem
import com.pqaar.app.utils.DbPaths.LIVE_AUCTION_LIST
import com.pqaar.app.utils.DbPaths.LIVE_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.LIVE_TRUCK_DATA_LIST

@SuppressLint("StaticFieldLeak")
object CommonRepo {
    private var TAG = "CommonRepo"
    private var firestoreDb = FirebaseFirestore.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()
    private val liveAuctionList = HashMap<String, LiveAuctionListItem>()
    private val liveTruckDataList = HashMap<String, LiveTruckDataListItem>()
    private val liveRoutesList = HashMap<String, LiveRoutesListItem>()
    val LiveAuctionList = MutableLiveData<HashMap<String, LiveAuctionListItem>>()
    val LiveTruckDataList = MutableLiveData<HashMap<String, LiveTruckDataListItem>>()
    val LiveRoutesList = MutableLiveData<HashMap<String, LiveRoutesListItem>>()

    fun fetchLiveAuctionList() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot,
                                      previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                // A new comment has been added, add it to the displayed list
                val changedEntry = dataSnapshot.getValue<LiveAuctionListItem>()
                liveAuctionList[changedEntry!!.currentNo] = changedEntry
                LiveAuctionList.value = liveAuctionList
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                val changedEntry = dataSnapshot.getValue<LiveAuctionListItem>()
                liveAuctionList[changedEntry!!.currentNo] = changedEntry
                LiveAuctionList.value =
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                val removedEntry = dataSnapshot.getValue<LiveAuctionListItem>()
                liveAuctionList.remove(removedEntry!!.currentNo)
                LiveAuctionList.value = liveAuctionList
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                //no action
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "Retrieving LiveAuctionList Failed:onCancelled", databaseError.toException())
            }
        }
        val ref = firebaseDb.reference.child(LIVE_AUCTION_LIST)
        ref.addChildEventListener(childEventListener)
    }

    fun fetchLiveTruckDataList() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot,
                                      previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                Log.d(TAG, "onChildAdded:" + dataSnapshot.value)

                val changedEntry = dataSnapshot.value as HashMap<*, *>
                changedEntry.forEach{
                    liveTruckDataList[dataSnapshot.key!!] = LiveTruckDataListItem(
                        dataSnapshot.key!!,
                        hashMapOf(
                            it.key.toString() to it.value.toString()
                        )
                    )
                }
                LiveTruckDataList.value = liveTruckDataList
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")
                Log.d(TAG, "onChildChanged: ${dataSnapshot.value}")

                val changedEntry = dataSnapshot.value as HashMap<*, *>
                changedEntry.forEach{
                    liveTruckDataList[dataSnapshot.key!!] = LiveTruckDataListItem(
                        dataSnapshot.key!!,
                        hashMapOf(
                            it.key.toString() to it.value.toString()
                        )
                    )
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
                Log.w(TAG, "Retrieving LiveTruckData Failed:onCancelled", databaseError.toException())
            }
        }
        val ref = firebaseDb.reference.child(LIVE_TRUCK_DATA_LIST)
        ref.addChildEventListener(childEventListener)
    }

    fun fetchLiveRoutesList() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot,
                                      previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                // A new comment has been added, add it to the displayed list
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
                Log.w(TAG, "Retrieving LiveTruckData Failed:onCancelled", databaseError.toException())
            }
        }
        val ref = firebaseDb.reference.child(LIVE_ROUTES_LIST)
        ref.addChildEventListener(childEventListener)
    }
}