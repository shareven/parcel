package com.xxxx.parcel.model


data class SmsData(val address: String, val code: String, val sms: SmsModel,val id:Long,val isCompleted:Boolean=false)
    