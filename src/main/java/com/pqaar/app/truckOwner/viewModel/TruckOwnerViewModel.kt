package com.pqaar.app.truckOwner.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pqaar.app.model.*
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveAuctionEndTime
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveAuctionList
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveAuctionStartTime
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo.LiveAuctionStatus
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class TruckOwnerViewModel : ViewModel() {
    private val TAG = "TruckOwnerViewModel"

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

    suspend fun bookRoute(truckNo: String, src: String, des: String) {
        closeBid(truckNo, src, des)
    }

    suspend fun refreshTruckOwnerData() {
        fetchTruckOwner()
    }

    fun refreshLiveRoutesList(){
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

    fun getLiveTruckDataList(): MutableLiveData<HashMap<String, LiveTruckDataListItem>> {
        return LiveTruckDataList
    }

    fun getAuctionStatus(): MutableLiveData<String> {
        return LiveAuctionStatus
    }

    fun getAuctionStartTime(): MutableLiveData<Long> {
        return LiveAuctionStartTime
    }

    fun getAuctionEndTime(): MutableLiveData<Long> {
        return LiveAuctionEndTime
    }

    fun getAuctionsBonusTimeInfo(): MutableLiveData<BonusTime> {
        return LiveBonusTimeInfo
    }

    fun getLiveRoutesList(): MutableLiveData<HashMap<String, LiveRoutesListItem>> {
        return LiveRoutesList
    }

    fun getLiveAuctionList(): MutableLiveData<HashMap<String, LiveAuctionListItem>> {
        return LiveAuctionList
    }
}