package com.xxxx.parcel.model

import java.sql.Timestamp

data class SmsModel(
    val id:Long,
    val body:String,
    val timestamp: Long
)
