package com.xxxx.parcel.model

data class ParcelData(
    val address: String,
    val smsDataList: MutableList<SmsData>,
    var num: Int=0,
//    val parcels: MutableList<SmsData>
)
