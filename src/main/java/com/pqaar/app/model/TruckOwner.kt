package com.pqaar.app.model

data class TruckOwner(
    var UserType: String = "NA",
    var FirstName: String = "NA",
    var LastName: String = "NA",
    var PhoneNo: String = "NA",
    var Trucks: ArrayList<String> = ArrayList(),
)
