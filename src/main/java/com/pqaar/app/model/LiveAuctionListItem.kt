package com.pqaar.app.model

data class LiveAuctionListItem(var currentNo: String = "",
                               var prevNo: String = "",
                               var truckNo: String = "",
                               var closed: String = "true",
                               var src: String? = "",
                               var des: String? = "",
                               var timestamp: Int = -1)
