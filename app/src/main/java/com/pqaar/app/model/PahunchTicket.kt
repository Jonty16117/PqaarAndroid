package com.pqaar.app.model

data class PahunchTicket(
    var PahunchAdmin: Pair<String, String> = Pair("NA", "NA"),
    var Source: String = "",
    var Destination: String = "",
    var DeliveryInfo: String = "",
    var Status: String = "",
    var Timestamp: Long = 0L,
    var TruckNo: String = "",
    var AuctionId: Long = 0L
)
