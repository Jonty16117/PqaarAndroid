package com.pqaar.app.model

data class TruckHistory(
    var TruckNo: String = "",
    var Status: String = "",
    var CurrentListNo: String = "",
    var Timestamp: String = "",
    var Owner: String = "",
    var Route: Pair<String, String> = Pair("NA", "NA")
)
