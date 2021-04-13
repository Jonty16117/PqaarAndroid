package com.pqaar.app.viewmodels

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.LiveTruckDataListItem
import com.pqaar.app.repositories.CommonRepo.LiveAuctionList
import com.pqaar.app.repositories.CommonRepo.LiveRoutesList
import com.pqaar.app.repositories.CommonRepo.fetchLiveAuctionList
import com.pqaar.app.repositories.CommonRepo.fetchLiveRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.LiveAuctionStatus
import com.pqaar.app.repositories.UnionAdminRepo.LiveAuctionTimestamp
import com.pqaar.app.repositories.UnionAdminRepo.LiveTruckDataList
import com.pqaar.app.repositories.UnionAdminRepo.fetchLiveTruckDataList
import com.pqaar.app.repositories.UnionAdminRepo.PropRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.combineLists
import com.pqaar.app.repositories.UnionAdminRepo.fetchAuctionInfo
import com.pqaar.app.repositories.UnionAdminRepo.fetchLastAuctionListDocument
import com.pqaar.app.repositories.UnionAdminRepo.fetchLivePropRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.getLastAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.getLastMissedList
import com.pqaar.app.repositories.UnionAdminRepo.separateOpenCloseLists
import com.pqaar.app.repositories.UnionAdminRepo.setEndTimeInLiveAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.uploadAuctionInfo
import com.pqaar.app.repositories.UnionAdminRepo.uploadLiveAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.uploadLiveMandiRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis


/**
 * Flow of operations for the union admin:
 * 1) The union admin will receive a list of proposed routes (PropRoutesList) from
 * each mandi admin.
 *
 */
class UnionAdminViewModel : ViewModel() {
    private val TAG = "UnionAdminViewModel"

    val Timers = HashMap<String, CountDownTimer>()
    val BID_TIME_PER_TRUCK: Long = 30000 /* Time to be alloted per truck entry in millis */

    init {
        fetchLivePropRoutesList()
        fetchLiveTruckDataList()
        fetchAuctionInfo()
        fetchLiveRoutesList()
    }


    /**
     * Step 1) Update status and timestamp for next auction
     * Step 2) Upload live routes for the next auction
     */
    fun setAuctionInfo(status: String, timestamp: Long) {
        uploadAuctionInfo(status, timestamp)
    }

    fun addMandiRoutes(
        mandiSrc: String,
        routesListToUpload: HashMap<String, LiveRoutesListItem>,
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                uploadLiveMandiRoutes(mandiSrc, routesListToUpload)
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    /**
     * Step 1) Fetch last auction document name
     * Step 2) Get last auction list using this document name
     * Step 3) Separate open and closed bid lists from last auction list
     * Step 4) Build last missed list
     * Step 5) Combine all these three lists
     * Step 6) Set bid duration allowed for each truck owner
     * Step 7) Upload the live auction list
     */
    fun initializeAuction(perUserBidDurationInMillis: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                val lastAuctionDoc = fetchLastAuctionListDocument()
                getLastAuctionList(lastAuctionDoc)
                separateOpenCloseLists()
                getLastMissedList()
                combineLists()
                setEndTimeInLiveAuctionList(perUserBidDurationInMillis)
                uploadLiveAuctionList()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }


    /**
     * Close the truck no who accept the bid and update the live routes lise
     * accordingly
     */
    fun monitorAuction() {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                fetchLiveAuctionList()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }


    /**
     * Step 1) Update Auction status and timestamp
     * Step 2) Save live routes list
     * Step 3) Save live auction list
     */
    fun closeAuction() {

    }



    /**
     * Live data getters
     */
    fun getLivePropRoutesList(): MutableLiveData<HashMap<String, MutableMap<String, Any>>> {
        return PropRoutesList
    }

    fun getLiveTruckDataList(): MutableLiveData<HashMap<String, LiveTruckDataListItem>> {
        return LiveTruckDataList
    }

    fun getAuctionStatus(): MutableLiveData<String> {
        return LiveAuctionStatus
    }

    fun getAuctionTimestamp(): MutableLiveData<Long> {
        return LiveAuctionTimestamp
    }

    fun getLiveRoutesList(): MutableLiveData<HashMap<String, LiveRoutesListItem>> {
        return LiveRoutesList
    }

    fun getLiveAuctionList(): MutableLiveData<HashMap<String, LiveAuctionListItem>> {
        return LiveAuctionList
    }


    /**
     * INITIAL CODE, DO NOT DELETE, IT TOOK A LOT OF TIME AND EFFORT.
     */

    /**
     * Schedule a new auction in the following steps:
     * 1) Set new routes list for this new auction
     * 2) Get the live truck data (which is need to build the last missed list in the
     *    next step)
     * 3) Create new auction list
     *      (i) Fetch last auction document name
     *      (ii) Fetch last auction records using this document name
     *      (iii) Separate closed and open entries from this record list
     *      (iv) Build last missed list that contains the entries which
     *           where no present in the last auction and are also not in
     *           progress of their delivery route
     *      (v) Merge these three lists to obtain the final auction list
     * 4) Initialize each list entry with the bid time for the first n (total
     *    required trucks in all the routes combined) entries in this live auction list
     * 5) Upload new live auction list
     * 6) Set auction status to "Scheduled"
     * 7) Set auction timestamp (auction starting time)
     * 8) To Start the realtime auction, following steps are performed in sequence:
     *      (i) Set the countdown timer for each of the initialized entries in live
     *          live auction list.
     *      (ii) After starting the timer, there are 2 possibilities and
     *           they are handled as: (The user is locked if its timer finishes, in all of
     *           the following cases)
     *           (a) If the timer of an entry finishes: It means that the user didn't
     *               accept the bid and should be considered as open in the next auction
     *               and since the default value of the bid is already set to "closed = true",
     *               we need not to change it. Also in this condition there arises two more
     *               possibilities as:
     *               (i) Requirement for trucks is > 0 and next bid entry exists:
     *                   Stop the timer for the current entry and start the timer for the
     *                   next entry. Also unlock the next entry.
     *               (ii) Requirement for trucks is > 0 and the next bid entry do not exist:
     *                    Stop the timer for the current entry.
     *           (b) If a user accepts the bid (or closes the bid, both means the same thing),
     *               then the timer of this user should stop. After stopping the timer, there
     *               are again two possibilities that are:
     *               (i) Requirement for trucks is > 0 and next bid entry exists:
     *                   Handle same as above.
     *               (ii) Requirement for trucks is > 0 and the next bid entry do not exist:
     *                    Handle same as above.
     */
    /*fun scheduleNewAuctionList(
        liveRoutesList: ArrayList<LiveRoutesListItem>,
        startTimestamp: Long,
        bidTime: Long
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                uploadRoutesList(liveRoutesList)
                val docName = UnionAdminRepo.fetchLastAuctionListDocument()
                getLastAuctionList(docName)
                separateOpenCloseLists()
                getLastMissedList()
                combineLists()
                initializeAuction(bidTime, startTimestamp)
                setAuctionTimestamp(startTimestamp)
                setAuctionStatus("Scheduled")
            }

            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }*/

    /*@SuppressLint("SimpleDateFormat")
    suspend fun startAuction(bidTime: Long) {
        //start timers for all the initialized entries in auction list
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                for (index in 0 until initOpenSize) {
                    val truckNo = liveCombinedAuctionList[index].truckNo
                    val bidEndTimestamp = liveCombinedAuctionList[index].timestamp.toLong()
                    val currTime = CurrDateTimeInMillis()
                    //set locked = false (or in other words, unlock the bid)
                    unlockBid(
                        truckNo = truckNo,
                        unlockDuration = currTime - bidEndTimestamp
                    )
                    //start timer for this user
                    Timers[truckNo] = getTimerObject(bidTime, truckNo)
                    Timers[truckNo]!!.start()
                }
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }*/

/*
    private fun getTimerObject(bidTime: Long, truckNo: String): CountDownTimer {
        return object : CountDownTimer(
            bidTime,
            1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                //wait until user accepts the bid
                //or until the timer finishes
            }

            override fun onFinish() {
                checkForNextEntry(bidTime, truckNo)
            }
        }
    }
*/

    /**
     * nextBidAt() returns -1 if there are no more trucks left
     * otherwise returns n where n > 0 and points to the next open
     * entry in the auction list for which the timer needs to started.
     */
    /* private fun checkForNextEntry(bidTime: Long, truckNo: String) {
         lockBid(truckNo)
         if ((getTrucksLeft() > 0) && (nextBidAt() != -1)) {
             val nextTruckNo = liveCombinedAuctionList[nextBidAt()].truckNo
             unlockBid(nextTruckNo, bidTime)
             Timers.remove(nextTruckNo)
             Timers[nextTruckNo] = getTimerObject(bidTime, nextTruckNo)
         }
     }
 */
    /*fun observeAuctionList(bidTime: Long) {
        getLiveAuctionList().observeForever {
            it.forEach { entry ->
                if (entry.value.closed == "true" &&
                    entry.value.locked == "false"
                ) {
                    val selctedRoute = entry.value.routeId
                    if (getLiveRoutesList().value!![selctedRoute]!!.got.toInt() <
                        getLiveRoutesList().value!![selctedRoute]!!.req.toInt()
                    ) {
                        updateRouteItem(selctedRoute!!,
                            (getLiveRoutesList().value!![selctedRoute]!!.got.toInt() + 1).toString())
                        checkForNextEntry(bidTime, entry.value.truckNo)
                    } else {
                        rollBackBid(entry.value.truckNo)
                    }
                }
            }
        }
    }
*/
    /**
     * 1) Change the auction status and timestamp
     * 2) Store the backup of the current routes list in the database
     * 3) Store the backup of the current auction list in the database
     */
    /* fun closeAuction(closingTime: Long) {
         GlobalScope.launch(Dispatchers.IO) {
             val executionTime = measureTimeMillis {
                 setAuctionStatus("AuctionClosed")
                 setAuctionTimestamp(closingTime)
                 closeAuction(closingTime)
             }
             withContext(Dispatchers.Main) {
                 Log.d(TAG, "ExecutionTime = $executionTime")
             }
         }
     }
 */
    /**
     * For adding/removing new trucks to users account
     */
    /*fun getTruckRequests(): MutableLiveData<HashMap<String, AddTruckRequest>> {
        fetchAddTruckRequests()
        return TruckRequestsLive
    }
*/
    /*suspend fun AddTruckToUser(truckRequest: AddTruckRequest) {
        addTruckToUser(truckRequest)
    }*/

    /*suspend fun RemoveTruckFromUser(truckRequest: AddTruckRequest) {
        removeTruckFromUser(truckRequest)
    }*/
}

