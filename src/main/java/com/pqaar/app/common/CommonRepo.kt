package com.pqaar.app.common

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
    /*private var firestoreDb = FirebaseFirestore.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()
    private val liveAuctionList = HashMap<String, LiveAuctionListItem>()
    private val liveRoutesList = HashMap<String, LiveRoutesListItem>()
    */

}