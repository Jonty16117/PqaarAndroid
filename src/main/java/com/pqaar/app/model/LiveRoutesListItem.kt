package com.pqaar.app.model

/**
 * Mandi:
 *     Destination:
 *         <LiveRoutesListItem>
 *         Requirement: <Trucks required>
 *         Got: <Trucks filled>
 *         Rate: <Rate of this route>
 */
data class LiveRoutesListItem(
    var desData: HashMap<String, HashMap<String, String>>
)
