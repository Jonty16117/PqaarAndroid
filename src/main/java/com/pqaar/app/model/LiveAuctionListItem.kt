package com.pqaar.app.model

data class LiveAuctionListItem(var currentNo: String = "",
                               var prevNo: String = "",
                               var truckNo: String = "",
                               var locked: String = "true",
                               var closed: String = "false",
                               var routeId: String? = "",
                               var timestamp: String = "")
