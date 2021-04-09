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
data class LiveTruckDataListItem(val truckNo: String,
                                 val data: HashMap<String, String>)