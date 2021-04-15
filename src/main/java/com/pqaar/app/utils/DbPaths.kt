package com.pqaar.app.utils

object DbPaths {
    /**
     * Firestore paths
     */
    const val USER_DATA = "user-data"
    const val LIVE_ROUTES_LIST = "LiveRoutesList"
    const val ROUTES_LIST_DATA = "routes-list-data"
    const val AUCTION_LIST_DATA = "auction-lists"
    const val MANDI_ROUTES_LIST = "MandiRoutesList"
    const val AUCTIONS_INFO = "AuctionsInfo"
    const val SCHEDULED_AUCTIONS = "ScheduledAuctions"
    const val LIVE_AUCTION_LIST = "LiveAuctionList"
    const val AUCTION_BONUS_TIME_INFO = "AuctionBonusTimeInfo"


    /**
     * Firebase paths
     */
    const val LIVE_TRUCK_DATA_LIST = "LiveTruckData"
    const val ADD_TRUCKS_REQUESTS = "AddTruckRequests"
    /**
     * Firebase child paths
     */
    const val STATUS = "Status"
    const val DEL_IN_PROG = "DelInProg"
    const val DEL_PASS = "DelPass"


    const val DEL_FAIL = "DelFail"
}