package com.pqaar.app.mandiAdmin.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.model.PropRoutesListItem
import com.pqaar.app.model.MandiRoutesHistoryItem
import com.pqaar.app.utils.DbPaths.MANDI_ROUTES_LIST
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis


@SuppressLint("StaticFieldLeak")
object MandiAdminRepo {
    private val TAG = "MandiAdminRepo"

    private var firebaseAuth = FirebaseAuth.getInstance()
    private var firestoreDb = FirebaseFirestore.getInstance()

    private var mandiRoutesListLiveHistory = ArrayList<MandiRoutesHistoryItem>()
    val MandiRoutesListLiveHistory = MutableLiveData<ArrayList<MandiRoutesHistoryItem>>()
    private var _MANDI = ""
    val MANDI = MutableLiveData<String>()


    /**
     * Get the mandi this user is associated with
     */
    suspend fun fetchUsersMandi() {
        var result = ""
        firestoreDb.collection(USER_DATA)
            .document(firebaseAuth.uid.toString()).get()
            .addOnSuccessListener {
                result = it.data!![MANDI].toString()
                Log.d(TAG,"Fetched user's mandi successfully!")
            }.addOnFailureListener {
                Log.d(TAG, "Fetching user's mandi failed!")
            }.await()
        _MANDI = result
        MANDI.postValue(result)

        // For testing only
//        _MANDI = "Abohar"
//        MANDI.postValue("Abohar")
    }

    /**
     * Shows all the routes in all the field entries
     */
    suspend fun fetchRoutesHistory() {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                val job = async {
                    val col = firestoreDb.collection(MANDI_ROUTES_LIST)
                    Log.d(TAG, "Fetching routes for mandi: $_MANDI")
                    col.document(_MANDI).addSnapshotListener { docSnapshot, error ->

                        if (docSnapshot != null && error == null) {
                            if (docSnapshot.data != null) {
                                mandiRoutesListLiveHistory = docToDTO(docSnapshot.data!!)
                                MandiRoutesListLiveHistory
                                    .postValue(mandiRoutesListLiveHistory)
                            } else {
                                MandiRoutesListLiveHistory
                                    .postValue(ArrayList<MandiRoutesHistoryItem>())
                            }
                        }
                    }
                }
                job.await()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
                Log.d(TAG, "New list = $mandiRoutesListLiveHistory")
            }
        }
    }

    private fun docToDTO(docSnapshot: MutableMap<String, Any>): ArrayList<MandiRoutesHistoryItem> {
        var mandiRoutesListLiveHistory = ArrayList<MandiRoutesHistoryItem>()
        docSnapshot.forEach { entry ->
            val fieldValuesMap = entry.value as HashMap<*, *>
            val routesEntry = MandiRoutesHistoryItem()
            val routes = ArrayList<Pair<String, Int>>()
            fieldValuesMap.forEach {
                when (it.key.toString()) {
                    "Status" -> {
                        routesEntry.Status = it.value.toString()
                    }
                    "Timestamp" -> {
                        routesEntry.Timestamp = it.value.toString().toLong()
                    }
                    else -> {
                        routes.add(Pair(
                            it.key.toString(),
                            it.value.toString().toInt()
                        )
                        )
                    }
                }
            }
            routesEntry.Routes = routes
            mandiRoutesListLiveHistory.add(routesEntry)
        }

        mandiRoutesListLiveHistory =
            ArrayList(mandiRoutesListLiveHistory.sortedWith(compareBy { it.Timestamp }))

        mandiRoutesListLiveHistory.reverse()

        return mandiRoutesListLiveHistory
    }

    /**
     * Marks the status of current route field entry to "Past"
     * and makes a new empty field entry with "Live" status
     */
    suspend fun initNewRoutesTable() {
        GlobalScope.launch(Dispatchers.IO) {
            val executionTime = measureTimeMillis {
                val ref = firestoreDb.collection(MANDI_ROUTES_LIST).document(_MANDI)
                val job1 = async {
                    val currRoutesField = mandiRoutesListLiveHistory[0]
                    // If the last live routes entry table was empty, then delete it
                    if (currRoutesField.Routes.size == 0) {
                        Log.d(TAG, "Deleting: ${currRoutesField}!")
                        val dataToDelete = hashMapOf<String, Any>(
                            currRoutesField.Timestamp.toString() to FieldValue.delete()
                        )
                        ref.update(dataToDelete).addOnSuccessListener {
                            Log.d(TAG, "Empty routes list table deleted: ${dataToDelete}!")
                        }.addOnFailureListener {
                            Log.d(TAG, "Failed to delete empty entry!")
                        }.await()
                    } else {
                        val dataToUpdate = HashMap<String, Any>()
                        dataToUpdate["Status"] = "Past"
                        dataToUpdate["Timestamp"] = currRoutesField.Timestamp
                        currRoutesField.Routes.forEach {
                            dataToUpdate[it.first] = it.second
                        }

                        ref.update(currRoutesField.Timestamp.toString(), dataToUpdate)
                            .addOnSuccessListener {
                                Log.d(TAG, "Status set to 'Past' for previous " +
                                        "routes field entry!")
                            }.await()
                    }
                }
                job1.await()
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "ExecutionTime = $executionTime")
            }
        }
    }

    /**
     * For performance:-
     * Do not call this function more than once per second, keep a delay of at-least
     * 2 seconds before calling this again.
     */
    suspend fun addRoute(propRoutesList: ArrayList<PropRoutesListItem>) {
        initNewRoutesTable()
        Log.d(TAG, "Entered in addRoute with route: ${propRoutesList}")
        val ref = firestoreDb.collection(MANDI_ROUTES_LIST).document(_MANDI)
        val currRoutesField = MandiRoutesHistoryItem()
        Log.d(TAG, "Route to update: ${currRoutesField}")
        val dataToUpdate = HashMap<String, Any>()
        dataToUpdate["Status"] = "Live"
        dataToUpdate["Timestamp"] = CurrDateTimeInMillis()
        currRoutesField.Routes.forEach {
            dataToUpdate[it.first] = it.second
        }
        propRoutesList.forEach {
            dataToUpdate[it.des] = it.req.toInt()
        }

        val updates = hashMapOf<String, Any>(
            currRoutesField.Timestamp.toString() to dataToUpdate)
        ref.update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Adding route: ${currRoutesField.Timestamp.toString()}")
                Log.d(TAG, "Route added successfully: ${dataToUpdate}")
            }
            .await()
    }
}