package com.pqaar.app.model

data class Bid(
    val currNumber: Int,
    val prevNumber: Int,
    val truckNo: String,
    val pickUpLocation: String,
    val dropLocation: String,
    val bidStatus: Char,
    val isUserNumber: Boolean
)
