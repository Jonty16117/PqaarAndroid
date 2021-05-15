package com.pqaar.app.pahunchAdmin.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.model.LiveTruckDataItem
import com.pqaar.app.model.PahunchTicket
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo.LiveTruckDataList
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo.PahunchHistory
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo.UserDestination
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo.acceptDel
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo.fetchIncomingTrucks
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo.fetchPahunchHistory
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo.fetchUserDestination
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo.rejectDel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Duties of Pahunch Admin are:
 * 1) To change the status of incoming truck drivers to "DelPass" or "DelFail".
 */
class PahunchAdminViewModel: ViewModel() {

    private var firestoreDb = FirebaseFirestore.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()

//    private val updatePahunchHistoryList = MutableLiveData<>

    init {
        GlobalScope.launch(Dispatchers.IO) {
            fetchUserDestination()
            fetchIncomingTrucks()
        }
    }

    suspend fun refreshHistory() {
        GlobalScope.launch(Dispatchers.IO) {
            fetchPahunchHistory()
        }
    }

    suspend fun refreshIncomingTrucks() {
        GlobalScope.launch(Dispatchers.IO) {
            fetchPahunchHistory()
        }
    }

    suspend fun AcceptDel(truckNo: String, delInfo: String) {
        GlobalScope.launch(Dispatchers.IO) {
            acceptDel(truckNo, delInfo)
        }
    }

    suspend fun RejectDel(truckNo: String, delInfo: String) {
        GlobalScope.launch(Dispatchers.IO) {
            rejectDel(truckNo, delInfo)
        }
    }

    /**
     * Getters
     */
    fun getPahunchHistory(): MutableLiveData<ArrayList<PahunchTicket>> {
        return PahunchHistory
    }

    fun getUserDestination(): MutableLiveData<String> {
        return UserDestination
    }

    fun getIncomingTrucks(): MutableLiveData<ArrayList<LiveTruckDataItem>> {
        return LiveTruckDataList
    }
}