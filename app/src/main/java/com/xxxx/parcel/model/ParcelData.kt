package com.xxxx.parcel.model

data class ParcelData(
    val address: String,
    val codes: MutableList<String>,
    var num: Int=0,
//    val parcels: MutableList<SmsData>
)
