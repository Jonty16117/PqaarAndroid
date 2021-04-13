package com.pqaar.app.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.model.PropRoutesListItem
import com.pqaar.app.repositories.MandiAdminRepo.addRoute
import com.pqaar.app.repositories.MandiAdminRepo.getUsersMandi
import com.pqaar.app.repositories.UnionAdminRepo
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis


/**
 * Duties of Mandi Admin are:
 * 1) To build and upload proposed routes list.
 */
class MandiAdminViewModel : ViewModel() {
    private val TAG = "MandiAdminViewModel"

    fun AddRoutes(propRoutesList: ArrayList<PropRoutesListItem>) {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                val userMandiSrc = getUsersMandi()
                Log.d(TAG, "Fetched User's Mandi = $userMandiSrc")
                Log.d(TAG, "Adding Routes")
                addRoute(userMandiSrc, propRoutesList)
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "Routes Added = $propRoutesList")
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }

    }
}