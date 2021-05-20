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
    const val LIVE_TRUCK_DATA_LIST = "LiveTruckData"
    const val PAHUNCH_ADMIN_RECORDS = "PahunchAdminRecords"
    const val TRUCK_REQUESTS = "AddTrucksRequests"
    const val ADD_REQUESTS = "AddRequests"
    const val DEL_REQUESTS = "DelRequests"


    /**
     * Firestore field keys
     */
    //DUMMY_DOC
    const val DUMMY_DOC = "DummyDoc"

    //USER_DATA Document
    const val TRUCKS = "Trucks"
    const val FIRST_NAME = "first-name"
    const val LAST_NAME = "last-name"
    const val PHONE_NO = "phone"
    const val USER_TYPE = "user-type"
    const val EMAIL = "email"
    const val USER_MANDI = "mandi"
    const val MANDI_ADMIN = "MA"
    const val PAHUNCH_ADMIN = "PA"
    const val TRUCK_OWNER = "TO"

    //LIVE_AUCTION_LIST Document
    const val IS_CLOSED = "Closed"
    const val CURR_NO = "CurrNo"
    const val PREVIOUS_LIST_NO = "PrevNo"
    const val SRC = "Src"
    const val DES = "Des"
    const val START_TIME = "StartTime"
    const val TRUCK_NUMBER = "TruckNo"

    //LIVE_TRUCK_DATA_LIST Document
    const val ACTIVE = "Active"
    const val AUCTION_ID = "AuctionId"
    const val BACK_RC_URL = "BackRCURL"
    const val CURRENT_LIST_NO = "CurrentListNo"
    const val DESTINATION = "Destination"
    const val SOURCE = "Source"
    const val FRONT_RC_URL = "FrontRCURL"
    const val OWNER_FIRST_NAME = "OwnerFirstName"
    const val OWNER_LAST_NAME = "OwnerLastName"
    const val STATUS = "Status"
    const val TIMESTAMP = "Timestamp"
    const val TRUCK_RC = "TruckRC"

    const val END_TIME = "EndTime"

    //ADD_REQUESTS Document
    const val FIRSTNAME ="FirstName"
    const val LASTNAME = "LastName"
    const val REQUEST_TYPE = "RequestType"
    const val OWNER_UID = "OwnerUId"
    const val REQUEST_STATUS = "RequestStatus"
    const val ADD = "Add"
    const val REMOVE = "Remove"

    //LIVE_ROUTES_LIST Document
    const val GOT = "Got"
    const val REQ = "Req"
    const val RATE = "Rate"




    /**
     * Firebase storage paths
     */


    /**
     * Firebase paths
     */
    const val TRUCKS_REQUESTS = "TruckRequests"

    /**
     * Firebase child paths
     */
    const val DEL_IN_PROG = "DelInProg"
    const val DEL_PASS = "DelPass"
    const val DEL_FAIL = "DelFail"

}