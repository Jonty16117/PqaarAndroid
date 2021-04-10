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
import com.pqaar.app.utils.RepositoryPaths.LIVE_AUCTION_LIST
import com.pqaar.app.utils.RepositoryPaths.LIVE_ROUTES_LIST
import com.pqaar.app.utils.RepositoryPaths.LIVE_TRUCK_DATA_LIST

@SuppressLint("StaticFieldLeak")
object CommonRepo {
    private var firestoreDb = FirebaseFirestore.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()
    private var TAG = "CommonRepo"

    private val liveAuctionList = HashMap<String, LiveAuctionListItem>()
    private val LiveAuctionList = MutableLiveData<HashMap<String, LiveAuctionListItem>>()

    private val liveTruckDataList = HashMap<String, LiveTruckDataListItem>()
    private val LiveTruckDataList = MutableLiveData<HashMap<String, LiveTruckDataListItem>>()

    private val liveRoutesList = HashMap<String, LiveRoutesListItem>()
    private val LiveRoutesList = MutableLiveData<HashMap<String, LiveRoutesListItem>>()

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
                LiveAuctionList.value = liveAuctionList
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

                // A new comment has been added, add it to the displayed list
                val changedEntry = dataSnapshot.getValue<LiveTruckDataListItem>()
                liveTruckDataList[changedEntry!!.truckNo] = changedEntry
                LiveTruckDataList.value = liveTruckDataList
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                val changedEntry = dataSnapshot.getValue<LiveTruckDataListItem>()
                liveTruckDataList[changedEntry!!.truckNo] = changedEntry
                LiveTruckDataList.value = liveTruckDataList
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                val removedEntry = dataSnapshot.getValue<LiveTruckDataListItem>()
                liveTruckDataList.remove(removedEntry!!.truckNo)
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

    /**
     * Getters that can be used by more than one type of users after authentication
     */
    fun getLiveAuctionList(): MutableLiveData<HashMap<String, LiveAuctionListItem>> {
        return LiveAuctionList
    }

    fun getLiveTruckDataList(): MutableLiveData<HashMap<String, LiveTruckDataListItem>> {
        return LiveTruckDataList
    }

    fun getLiveRoutesList(): MutableLiveData<HashMap<String, LiveRoutesListItem>> {
        return LiveRoutesList
    }
}