package com.pqaar.app.viewmodels

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pqaar.app.model.AddTruckRequest
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.repositories.UnionAdminRepository
import com.pqaar.app.repositories.UnionAdminRepository.TruckRequestsLive
import com.pqaar.app.repositories.UnionAdminRepository.addTruckToUser
import com.pqaar.app.repositories.UnionAdminRepository.bidsClosed
import com.pqaar.app.repositories.UnionAdminRepository.combineLists
import com.pqaar.app.repositories.UnionAdminRepository.fetchAddTruckRequests
import com.pqaar.app.repositories.UnionAdminRepository.getLastAuctionList
import com.pqaar.app.repositories.UnionAdminRepository.getLastMissedList
import com.pqaar.app.repositories.UnionAdminRepository.initOpenSize
import com.pqaar.app.repositories.UnionAdminRepository.initializeAuction
import com.pqaar.app.repositories.UnionAdminRepository.liveCombinedAuctionList
import com.pqaar.app.repositories.UnionAdminRepository.lockBid
import com.pqaar.app.repositories.UnionAdminRepository.nextOpenAt
import com.pqaar.app.repositories.UnionAdminRepository.removeTruckFromUser
import com.pqaar.app.repositories.UnionAdminRepository.separateOpenCloseLists
import com.pqaar.app.repositories.UnionAdminRepository.setAuctionStatus
import com.pqaar.app.repositories.UnionAdminRepository.setAuctionTimestamp
import com.pqaar.app.repositories.UnionAdminRepository.totalTrucksRequired
import com.pqaar.app.repositories.UnionAdminRepository.unlockBid
import com.pqaar.app.repositories.UnionAdminRepository.uploadRoutesList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class UnionAdminViewModel : ViewModel() {
    private val TAG = "UnionAdminViewModel"

    private var currAuctionListSize = -1
    private var TrucksRequired = totalTrucksRequired
    private var BidsClosed = bidsClosed
    private var NextOpenBidAt = nextOpenAt
    private var InitalOpenListSize = initOpenSize
    var Timers = MutableLiveData<HashMap<String, Long>>()

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
     *           they are handled as:
     *           (a) If the timer of an entry finishes: It means that the user didn't
     *               accept the bid and should be considered as open in the next auction
     *               and since the default value of the bid is already set to "closed = true",
     *               we need not to change it. Also in this condition there arises two more
     *               possibilities as:
     *               (i) Requirement for trucks is > 0 and next bid entry exists:
     *                   Stop the timer for the current entry and start the timer for the
     *                   next entry.
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
    fun scheduleNewAuctionList(
        liveRoutesList: ArrayList<LiveRoutesListItem>, startTimestamp: Long, bidTimeInSec: Int
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                uploadRoutesList(liveRoutesList)
                val docName = UnionAdminRepository.fetchLastAuctionListDocument()
                getLastAuctionList(docName)
                separateOpenCloseLists()
                getLastMissedList()
                combineLists()
                initializeAuction(bidTimeInSec, startTimestamp)
                setAuctionTimestamp(startTimestamp)
                setAuctionStatus("Scheduled")
            }

            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    suspend fun startAuction(startTime: Long) {
        //start timers for all the initialized entries in auction list
        GlobalScope.launch(Dispatchers.IO) {
            val Timers = HashMap<String, CountDownTimer>()
            val executionTime = measureTimeMillis {
                for (index in 0 until initOpenSize) {
                    val truckNo = liveCombinedAuctionList[index].truckNo
                    val bidEndTimestamp = liveCombinedAuctionList[index].timestamp.toLong()
                    //set locked = false (or in other words, unlock the bid)
                    unlockBid(
                        truckNo = truckNo,
                        unlockDuration = startTime - bidEndTimestamp
                    )

                    //start timer for this user
                    val timer = object: CountDownTimer(bidEndTimestamp,
                        1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            //wait until user accepts the bid
                            //or until the timer finishes
                        }
                        override fun onFinish() {
                            lockBid(truckNo)
                            bidsClosed += 1
                            if (bidsClosed < totalTrucksRequired) {
                                nextOpenAt += 1
                            }
                        }
                    }
                    Timers[truckNo] = timer
                    Timers[truckNo]!!.start()
                }
            }
            withContext(Dispatchers.Main) {
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
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    /**
     * For adding/removing new trucks to users account
     */
    fun getTruckRequests(): MutableLiveData<HashMap<String, AddTruckRequest>> {
        fetchAddTruckRequests()
        return TruckRequestsLive
    }

    suspend fun AddTruckToUser(truckRequest: AddTruckRequest) {
        addTruckToUser(truckRequest)
    }

    suspend fun RemoveTruckFromUser(truckRequest: AddTruckRequest) {
        removeTruckFromUser(truckRequest)
    }
}