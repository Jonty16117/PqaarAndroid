package com.pqaar.app.model

data class LiveTruckDataItem(
    var TruckNo: String = "",
    var CurrentListNo: String = "",
    var Owner: Pair<String, String> = Pair("NA", "NA"),
    var Route: Pair<String, String> = Pair("NA", "NA"),
    var Status: String = "",
    var Timestamp: String = "",
    var Active: Boolean = true
)
