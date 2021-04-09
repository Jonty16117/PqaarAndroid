package com.pqaar.app.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.TruckRequest
import com.pqaar.app.repositories.UnionAdminRepository
import com.pqaar.app.utils.TimeConversions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class UnionAdminViewModel : ViewModel() {
    private val TAG = "UnionAdminViewModel"

    /**
     * Schedule a new auction in the following steps:
     * 1) Create new auction list
     *      (i) Fetch last auction document name
     *      (ii) Fetch last auction records using this document name
     *      (iii) Separate closed and open entries from this record list
     *      (iv) Build last missed list that contains the entries which
     *           where no present in the last auction and are also not in
     *           progress of their delivery route
     *      (v) Merge these three lists to obtain the final auction list
     * 2) Set new routes list for this new auction
     * 3) Set auction status to "Scheduled"
     * 4) Set auction timestamp (auction starting time)
     * 5) Upload new auction list after doing the following steps:
     *      (i) Initialize each list entry with the bid time for
     *          the first n (total required trucks in all the routes
     *          combined) entries.
     */
    fun scheduleNewAuctionList(liveRoutesList: ArrayList<LiveRoutesListItem>
                            , startTimestamp: Long, bidTimeInSec: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                val docName = UnionAdminRepository.fetchLastAuctionListDocument()
                UnionAdminRepository.getLastAuctionList(docName)
                UnionAdminRepository.separateOpenCloseLists()
                UnionAdminRepository.getLastMissedList()
                UnionAdminRepository.combineLists()
                UnionAdminRepository.uploadRoutesList(liveRoutesList)
                UnionAdminRepository.setAuctionStatus("Scheduled")
                UnionAdminRepository.setAuctionTimestamp(startTimestamp)
                UnionAdminRepository.initializeAuction(bidTimeInSec, startTimestamp)
            }

            withContext(Dispatchers.Main){
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    /**
     * 1) Change the auction status and timestamp
     * 2) Store the backup of the current routes list in the database
     * 3) Store the backup of the current auction list in the database
     */
    fun closeAuction(closingTime: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                UnionAdminRepository.setAuctionStatus("AuctionClosed")
                UnionAdminRepository.setAuctionTimestamp(closingTime)
                UnionAdminRepository.closeAuction(closingTime)
            }
            withContext(Dispatchers.Main){
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    /**
     * For adding/removing new trucks to users account
     */
    fun getTruckRequests(): MutableLiveData<ArrayList<TruckRequest>> {
        return UnionAdminRepository.TruckRequestsLive
    }

    fun addTruckToUser(truckRequest: TruckRequest) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
               UnionAdminRepository.addTruckToUser(truckRequest)
            }
            withContext(Dispatchers.Main){
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    fun removeTruckFromUser(truckRequest: TruckRequest) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
               UnionAdminRepository.removeTruckFromUser(truckRequest)
            }
            withContext(Dispatchers.Main){
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }
}