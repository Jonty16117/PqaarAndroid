package com.pqaar.app.model

// For Union addmin to get proposed live list recieved from mandi admins
data class LivePropRoutesListItem(
    var Mandi: String = "",
    var Timestamp: Long = 0L,
    var Routes: ArrayList<Pair<String, Int>> = ArrayList()
)
