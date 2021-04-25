package com.pqaar.app.model

data class LiveRoutesListItem(val Mandi: String, val Routes: ArrayList<RouteDestination>) {
    data class RouteDestination(var Des: String, var Req: Int, var Got: Int, var Rate: Int)
}

