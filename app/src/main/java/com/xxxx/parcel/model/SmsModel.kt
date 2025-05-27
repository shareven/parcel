package com.xxxx.parcel.model

import java.sql.Timestamp

data class SmsModel(
    val id:String,
    val body:String,
    val timestamp: Long
)
