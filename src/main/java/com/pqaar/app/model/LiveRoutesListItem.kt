package com.pqaar.app.model

/**
 * Mandi:
 *     <LiveRoutesListItem>
 *     Destination:
 *         Req: <Trucks required>
 *         Got: <Trucks filled>
 *         Rate: <Rate of this route>
 */

data class LiveRoutesListItem(
    var desData: HashMap<String, HashMap<String, String>> = HashMap()
)

/*data class LiveRoutesListItem(
    var des: String = "",
    var rate: Int = -1,
    var req: Int = -1,
    var got: Int = -1,
    )*/