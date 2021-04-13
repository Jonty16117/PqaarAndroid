 package com.pqaar.app.model

/**
 * Data is a map type of property and should
 * have the following type key-values:
 *
 * Status: String
 * Owner: String
 * CurrentListNo: String
 * Timestamp: String
 */
data class LiveTruckDataListItem(var truckNo: String = "",
                                 var data: HashMap<String, String> = HashMap())