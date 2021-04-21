package com.pqaar.app.utils

object DbPaths {
    /**
     * Firestore paths
     */
    const val USER_DATA = "user-data"
    const val LIVE_ROUTES_LIST = "LiveRoutesList"
    const val ROUTES_LIST_DATA = "LiveRoutesListRecords"
    const val AUCTION_LIST_DATA = "auction-lists"
    const val MANDI_ROUTES_LIST = "MandiRoutesList"
    const val AUCTIONS_INFO = "AuctionsInfo"
    const val SCHEDULED_AUCTIONS = "ScheduledAuctions"
    const val LIVE_AUCTION_LIST = "LiveAuctionList"
    const val AUCTION_BONUS_TIME_INFO = "AuctionBonusTimeInfo"

    /**
     * Firestore field keys
     */
    const val TRUCKS = "Trucks"
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
    const val PHONE_NO = "phoneNo"
    const val USER_TYPE = "userType"

    /**
     * Firebase storage paths
     */
    const val TRUCK_RC = "TruckRC"

    /**
     * Firebase paths
     */
    const val LIVE_TRUCK_DATA_LIST = "LiveTruckData"
    const val TRUCKS_REQUESTS = "TruckRequests"

    /**
     * Firebase child paths
     */
    const val STATUS = "Status"
    const val DEL_IN_PROG = "DelInProg"
    const val DEL_PASS = "DelPass"
    const val ADD_REQUESTS = "AddRequests"
    const val DEL_REQUESTS = "DelRequests"
    const val DEL_FAIL = "DelFail"
}