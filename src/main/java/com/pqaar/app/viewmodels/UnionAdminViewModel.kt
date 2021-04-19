package com.pqaar.app.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pqaar.app.model.BonusTime
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.LiveTruckDataListItem
import com.pqaar.app.repositories.UnionAdminRepo.LiveAuctionEndTime
import com.pqaar.app.repositories.UnionAdminRepo.LiveAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.LiveAuctionStartTime
import com.pqaar.app.repositories.UnionAdminRepo.LiveAuctionStatus
import com.pqaar.app.repositories.UnionAdminRepo.LiveBonusTimeInfo
import com.pqaar.app.repositories.UnionAdminRepo.LiveRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.LiveTruckDataList
import com.pqaar.app.repositories.UnionAdminRepo.PropRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.clearLiveAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.clearLiveAuctionListDebug
import com.pqaar.app.repositories.UnionAdminRepo.clearLiveRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.combineLists
import com.pqaar.app.repositories.UnionAdminRepo.fetchAuctionsBonusTimeInfo
import com.pqaar.app.repositories.UnionAdminRepo.fetchLastAuctionListDocument
import com.pqaar.app.repositories.UnionAdminRepo.fetchLiveAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.fetchLivePropRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.fetchLiveRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.fetchLiveTruckDataList
import com.pqaar.app.repositories.UnionAdminRepo.fetchSchAuctionsInfo
import com.pqaar.app.repositories.UnionAdminRepo.getLastAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.getLastMissedList
import com.pqaar.app.repositories.UnionAdminRepo.liveCombinedAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.saveLiveAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.saveLiveRoutesList
import com.pqaar.app.repositories.UnionAdminRepo.separateOpenCloseLists
import com.pqaar.app.repositories.UnionAdminRepo.setStartTimeInLiveAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.uploadAuctionsBonusTimeInfo
import com.pqaar.app.repositories.UnionAdminRepo.uploadLiveAuctionList
import com.pqaar.app.repositories.UnionAdminRepo.uploadLiveMandiRoutes
import com.pqaar.app.repositories.UnionAdminRepo.uploadSchAuctionsInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis


/**
 * Flow of operations for the union admin:
 * 1) The union admin (UA) will receive a list of proposed routes (PropRoutesList) from
 * each mandi admin.
 *
 * 2) The UA will then make its own version of list called live routes list, which he
 * will then upload.
 *
 * 3) UA will then schedule the auction by setting the status and timing of the auction
 *
 * 4) UA will then initialize the auction by uploading the live auction list
 *
 * 5) UA will then monitor the auction, in which he will listen for the bid accept callbacks
 * from the truck owners (TO). After receiving each callback, the UA will then either close
 * bid or ignore it. If it closes the bid, the live routes list is also updated
 *
 * 6) When the auctions finishes, the UA can either start the bonus time or close the
 * auction
 *
 * Other duties of the UA include:
 * 1) Accept the add truck request from truck owner users
 * 2) Remove truck request from truck owner users
 */
class UnionAdminViewModel : ViewModel() {
    private val TAG = "UnionAdminViewModel"

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                fetchLivePropRoutesList()
                fetchLiveTruckDataList()
                fetchSchAuctionsInfo()
                fetchLiveRoutesList()
                fetchLiveAuctionList()
                fetchAuctionsBonusTimeInfo()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    /**
     *Update status and timestamp for next auction
     */
    fun scheduleAuction(startTime: Long) {
        val status = "Scheduled"
//        LiveAuctionStatus.value = status
//        LiveAuctionStartTime.value = startTime

        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                uploadSchAuctionsInfo(status, startTime, 0)
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    /**
     * Upload live routes for the next auction
     */
    fun addMandiRoutes(
        mandiSrc: String,
        routesListToUpload: HashMap<String, HashMap<String, Int>>,
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
    fun initializeAuction(
        status: String, startTime: Long,
        perUserBidDurationInMillis: Long,
    ) {
        var endTime: Long
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                val lastAuctionDoc = fetchLastAuctionListDocument()
                getLastAuctionList(lastAuctionDoc)
                Log.d(TAG, "Fetched last auction list")
                separateOpenCloseLists()
                Log.d(TAG, "Separated open and close lists")
                getLastMissedList()
                Log.d(TAG, "Fetched last missed auction list")
                combineLists()
                Log.d(TAG, "Combined all three lists")
                endTime = startTime + (liveCombinedAuctionList.size * perUserBidDurationInMillis)
                getAuctionEndTime().postValue(endTime)
                setStartTimeInLiveAuctionList(startTime, perUserBidDurationInMillis)
                Log.d(TAG, "Start time is set for each entry in live auction list")
                uploadLiveAuctionList()
                Log.d(TAG, "Uploaded final live auction lists")
                fetchLiveAuctionList()
                Log.d(TAG, "Fetched live auction list")
                uploadSchAuctionsInfo(
                    status = status,
                    startTime = startTime,
                    endTime = endTime
                )
                Log.d(TAG, "Auction timing scheduled at ${startTime}")
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
        //update live routes list when a truck user adds his route
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                uploadSchAuctionsInfo(
                    status = "Live",
                    startTime = LiveAuctionStartTime.value!!,
                    endTime = LiveAuctionEndTime.value!!
                )
                fetchLiveAuctionList()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    fun addBonusTime(startTime: String, endTime: String) {
        uploadAuctionsBonusTimeInfo(startTime, endTime)
    }


    /**
     * Step 1) Update Auction status and timestamp
     * Step 2) Save live routes list
     * Step 3) Save live auction list
     */
    fun closeAuction() {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                uploadSchAuctionsInfo(
                    "Finished",
                    LiveAuctionStartTime.value!!,
                    LiveAuctionEndTime.value!!
                )
                saveLiveAuctionList()
                saveLiveRoutesList()
                clearLiveAuctionList()
                clearLiveRoutesList()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    fun forceClearLiveAuctionList() {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                clearLiveAuctionListDebug()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }

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

    fun getAuctionsBonusTimeInfo(): MutableLiveData<BonusTime> {
        return LiveBonusTimeInfo
    }

    fun getAuctionStartTime(): MutableLiveData<Long> {
        return LiveAuctionStartTime
    }

    fun getAuctionEndTime(): MutableLiveData<Long> {
        return LiveAuctionEndTime
    }

    fun getLiveRoutesList(): MutableLiveData<HashMap<String, LiveRoutesListItem>> {
        return LiveRoutesList
    }

    fun getLiveAuctionList(): MutableLiveData<HashMap<String, LiveAuctionListItem>> {
        return LiveAuctionList
    }

    /**
     * ---------------------------OVER-----------------------------------------------
     */


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

