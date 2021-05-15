package com.pqaar.app.model

data class MandiRoutesHistoryItem(
    var Timestamp: Long = 0L,
    var Status: String = "",
    var Routes: ArrayList<Pair<String, Int>> = ArrayList()
)
