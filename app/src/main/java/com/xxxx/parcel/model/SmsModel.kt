package com.xxxx.parcel.model

import kotlinx.serialization.Serializable
import java.sql.Timestamp

@Serializable
data class SmsModel(
    val id:String,
    val body:String,
    val timestamp: Long
)
