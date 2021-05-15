package com.pqaar.app.truckOwner.viewModel

import android.graphics.Bitmap
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.pqaar.app.model.*
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveAuctionList
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveAuctionTimestamps
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveBonusTimeInfo
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveRoutesList
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveTruckDataList
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.TruckOwnerLiveData
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.closeBid
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.fetchAuctionsBonusTimeInfo
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.fetchLiveAuctionList
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.fetchLiveRoutesList
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.fetchLiveTruckDataList
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.fetchSchAuctionsInfo
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.fetchTruckOwner
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.sendAddTruckReq
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class TruckOwnerViewModel : ViewModel() {
    private val TAG = "TruckOwnerViewModel"

    private val SCHEDULED_AUCTION_STATUS = "Next Auction In"
    private val LIVE_AUCTION_STATUS = "Live Auction Ending In"
    private val NO_AUCTION_STATUS = "No Auctions Available"

    private lateinit var timer: CountDownTimer
    private var timerIsRunning = false

    var dashboardStatus = MutableLiveData<String>()
    var dashboardTimer = MutableLiveData<String>()
    val liveAuctionStatusChanged = Transformations.map(getLiveAuctionTimestamps()) {
        //it.first is starting time of the auction
        //it.ending is ending time of the auction
        val auctionStartTime = it.first
        val auctionEndTime = it.second

        //means that the auction is scheduled
        if (CurrDateTimeInMillis() < it.first) {
            dashboardStatus.value = SCHEDULED_AUCTION_STATUS
            setScheduledTimer(auctionStartTime, auctionEndTime)
        } else if (CurrDateTimeInMillis() >= it.first && CurrDateTimeInMillis() < it.second) {
            dashboardStatus.value = LIVE_AUCTION_STATUS
            setLiveTimer(auctionStartTime, auctionEndTime)
        } else {
            //means that there are no auction scheduled or live
            dashboardStatus.value = NO_AUCTION_STATUS
            dashboardTimer.value = "00:00:00"
        }
    }

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                fetchTruckOwner()
                fetchLiveRoutesList()
                fetchLiveAuctionList()
                fetchLiveTruckDataList()
                fetchSchAuctionsInfo()
                fetchAuctionsBonusTimeInfo()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }


    fun getLiveAuctionTimestamps(): MutableLiveData<Pair<Long, Long>> {
        return LiveAuctionTimestamps
    }

    suspend fun bookRoute(truckNo: String, src: String, des: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val job = async { closeBid(truckNo, src, des) }
            job.await()
        }
    }

    suspend fun addTruck(
        rcFront: Bitmap, rcBack: Bitmap,
        truckNo: String,
        truckRC: String,
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val job = async {
                sendAddTruckReq(
                    rcFront,
                    rcBack,
                    truckNo,
                    truckRC,
                )
            }
            job.await()
        }
    }

    private fun setScheduledTimer(auctionStartTime: Long, auctionEndTime: Long) {
        if (!timerIsRunning) {
            timerIsRunning = true
            var hour: Long
            var min: Long
            var sec: Long
            var text: String
            timer = object : CountDownTimer(
                auctionStartTime.minus(CurrDateTimeInMillis()),
                1000) {
                override fun onTick(timeLeft: Long) {
                    hour = (timeLeft / (1000 * 60 * 60)) % 60
                    min = (timeLeft / (1000 * 60)) % 60
                    sec = (timeLeft / 1000) % 60
                    text = "${hour.toString().padStart(2, '0')}:" +
                            "${min.toString().padStart(2, '0')}:" +
                            sec.toString().padStart(2, '0')
                    dashboardTimer.value = text
                }

                override fun onFinish() {
                    dashboardStatus.value = LIVE_AUCTION_STATUS
                    timerIsRunning = true
                    timer = object : CountDownTimer(
                        auctionEndTime.minus(auctionStartTime),
                        1000) {
                        override fun onTick(timeLeft: Long) {
                            hour = (timeLeft / (1000 * 60 * 60)) % 60
                            min = (timeLeft / (1000 * 60)) % 60
                            sec = (timeLeft / 1000) % 60
                            text = "${hour.toString().padStart(2, '0')}:" +
                                    "${min.toString().padStart(2, '0')}:" +
                                    sec.toString().padStart(2, '0')
                            dashboardTimer.value = text
                        }

                        override fun onFinish() {
                            dashboardStatus.value = NO_AUCTION_STATUS
                            dashboardTimer.value = "00:00:00"
                            timerIsRunning = false
                            timer.cancel()
                        }
                    }
                    timer.start()
                }
            }
            timer.start()
        }
    }

    private fun setLiveTimer(auctionStartTime: Long, auctionEndTime: Long) {
        if (!timerIsRunning) {
            timerIsRunning = true
            var hour: Long
            var min: Long
            var sec: Long
            var text: String
            timer = object : CountDownTimer(
                auctionEndTime.minus(auctionStartTime),
                1000) {
                override fun onTick(timeLeft: Long) {
                    hour = (timeLeft / (1000 * 60 * 60)) % 60
                    min = (timeLeft / (1000 * 60)) % 60
                    sec = (timeLeft / 1000) % 60
                    text = "${hour.toString().padStart(2, '0')}:" +
                            "${min.toString().padStart(2, '0')}:" +
                            sec.toString().padStart(2, '0')
                    dashboardTimer.value = text
                }

                override fun onFinish() {
                    dashboardStatus.value = NO_AUCTION_STATUS
                    timerIsRunning = false
                    timer.cancel()
                }
            }
            timer.start()
        }
    }

    suspend fun refreshTruckOwnerData() {
        fetchTruckOwner()
    }

    fun refreshLiveRoutesList() {
        fetchLiveRoutesList()
    }

    fun refreshLiveAuctionList() {
        fetchLiveAuctionList()
    }

    fun refreshLiveTruckDataList() {
        fetchLiveTruckDataList()
    }

    fun refreshSchAuctionsInfo() {
        fetchSchAuctionsInfo()
    }

    fun refreshAuctionsBonusTimeInfo() {
        fetchAuctionsBonusTimeInfo()
    }


    /**
     * Getters
     */
    fun getTruckOwnerData(): MutableLiveData<TruckOwner> {
        return TruckOwnerLiveData
    }

    fun getLiveTruckDataList(): MutableLiveData<HashMap<String, LiveTruckDataItem>> {
        return LiveTruckDataList
    }

    fun getAuctionsBonusTimeInfo(): MutableLiveData<BonusTime> {
        return LiveBonusTimeInfo
    }

    fun getLiveRoutesList(): MutableLiveData<HashMap<String, LiveRoutesListItemDTO>> {
        return LiveRoutesList
    }

    fun getLiveAuctionList(): MutableLiveData<HashMap<String, LiveAuctionListItem>> {
        return LiveAuctionList
    }

}