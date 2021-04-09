package com.pqaar.app.model

data class Route(
    val pickUpLocation: String,
    val dropLocation: String,
    val trucksNeeded: Int,
    val trucksProvided: Int
)