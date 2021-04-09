package com.pqaar.app.model

data class TruckHistory(
    val truckNumber: String,
    val deliveryStatus: Char,
    val pickUpLocation: String,
    val pickUpDate: String,
    val pickUpTime: String,
    val dropLocation: String,
    val dropDate: String,
    val dropTime: String,
    val listNumber: Int
)
