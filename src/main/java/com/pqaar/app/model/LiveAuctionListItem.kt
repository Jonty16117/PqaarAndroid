package com.pqaar.app.model

data class LiveAuctionListItem(var CurrNo: String?  = " ",
                               var PrevNo: String? = " ",
                               var TruckNo: String? = " ",
                               var Closed: String? = "false",
                               var StartTime: Long? = -1,
                               var Des: String? = " ",
                               var Src: String? = " ")
