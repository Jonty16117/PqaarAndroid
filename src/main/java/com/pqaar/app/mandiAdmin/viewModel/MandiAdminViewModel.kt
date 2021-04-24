package com.pqaar.app.mandiAdmin.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pqaar.app.model.PropRoutesListItem
import com.pqaar.app.model.MandiRoutesHistoryItem
import com.pqaar.app.mandiAdmin.repository.MandiAdminRepo.MANDI
import com.pqaar.app.mandiAdmin.repository.MandiAdminRepo.MandiRoutesListLiveHistory
import com.pqaar.app.mandiAdmin.repository.MandiAdminRepo.addRoute
import com.pqaar.app.mandiAdmin.repository.MandiAdminRepo.fetchRoutesHistory
import com.pqaar.app.mandiAdmin.repository.MandiAdminRepo.fetchUsersMandi
import com.pqaar.app.mandiAdmin.repository.MandiAdminRepo.initNewRoutesTable
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis


/**
 * Duties of Mandi Admin are:
 * 1) To build and upload proposed routes list.
 */
class MandiAdminViewModel : ViewModel() {
    private val TAG = "MandiAdminViewModel"

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                val job = async { fetchUsersMandi() }
                job.await()
                fetchRoutesHistory()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    fun InitNewRoutesTable() {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                initNewRoutesTable()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    fun AddRoutes(routes: ArrayList<PropRoutesListItem>) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                val job2 = async {
                    addRoute(routes)
                }
                job2.await()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "Adding routes: ${routes}")
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    fun refreshRoutesHistory() {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                fetchRoutesHistory()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    fun refreshMandi() {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                fetchUsersMandi()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    /**
     * Getters
     */
    fun getMandi(): MutableLiveData<String> {
        return MANDI
    }

    fun getRoutesHistory(): MutableLiveData<ArrayList<MandiRoutesHistoryItem>> {
        return MandiRoutesListLiveHistory
    }
}