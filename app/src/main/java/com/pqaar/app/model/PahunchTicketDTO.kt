package com.pqaar.app.model

data class PahunchTicketDTO(
    var AuctionId: Long = 0L,
    var TruckNo: String = "",
    var Source: String = "",
    var Destination: String = "",
    var Status: String = "",
    var Timestamp: Long = 0L,
)
