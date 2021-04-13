package com.pqaar.app.viewmodels

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pqaar.app.repositories.PahunchAdminRepo.acceptDel
import com.pqaar.app.repositories.PahunchAdminRepo.rejectDel

/**
 * Duties of Pahunch Admin are:
 * 1) To change the status of incoming truck drivers to "DelPass" or "DelFail".
 */
class PahunchAdminViewModel {
    private var firestoreDb = FirebaseFirestore.getInstance()
    private var firebaseDb = FirebaseDatabase.getInstance()

    fun AcceptDel(truckNo: String) {
       acceptDel(truckNo)
    }

    fun RejectDel(truckNo: String) {
        rejectDel(truckNo)
    }
}