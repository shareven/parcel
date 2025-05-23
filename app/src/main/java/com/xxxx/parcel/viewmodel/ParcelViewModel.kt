package com.xxxx.parcel.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xxxx.parcel.model.ParcelData
import com.xxxx.parcel.model.SmsData
import com.xxxx.parcel.model.SmsModel
import com.xxxx.parcel.util.SmsParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ParcelViewModel(private val smsParser: SmsParser = SmsParser()) : ViewModel() {
    
    // 解析成功的短信
    private val _successSmsData = MutableStateFlow<List<SmsData>>(emptyList())
    val successSmsData: StateFlow<List<SmsData>> = _successSmsData

    // 解析失败的短信
    private val _failedMessages = MutableStateFlow<List<SmsModel>>(emptyList())
    val failedMessages: StateFlow<List<SmsModel>> = _failedMessages

    // 同一地址的取件码列表
    private val _parcels = MutableStateFlow<List<ParcelData>>(emptyList())
    val parcels: StateFlow<List<ParcelData>> = _parcels

    fun clearData(){
        _successSmsData.value = emptyList()
        _failedMessages.value = emptyList()
        _parcels.value = emptyList()
    }

    // 处理接收到的短信
    fun handleReceivedSms(sms: SmsModel) {
        viewModelScope.launch {
            val currentSuccessful = _successSmsData.value.toMutableList()
            val currentParcels = _parcels.value.toMutableList()
            val currentFailed = _failedMessages.value.toMutableList()

            val result = smsParser.parseSms(sms.body)
            
            if (result.success) {
                Log.d("成功短信", sms.body)
                currentSuccessful.add(SmsData( result.address,result.code,sms))
                // 把同一地址的取件码添加到 parcels 列表中
                currentParcels.find { it.address == result.address }?.let {
                    it.codes.add(result.code)
                    it.codes.sort()
                } ?: run {
                    currentParcels.add(ParcelData(result.address, mutableListOf(result.code)))
                }
            } else {
                Log.e("失败短信", sms.body)
                currentFailed.add(sms)
            }

            currentParcels.sortBy { -it.codes.size }
            _successSmsData.emit(currentSuccessful)
            _parcels.emit(currentParcels)
            _failedMessages.emit(currentFailed)

        }
    }

    // 将自定义规则添加到 SmsParser
    fun addCustomAddressPattern(pattern: String) {
        smsParser.addCustomAddressPattern(pattern)
    }
    fun addCustomCodePattern(pattern: String) {
        smsParser.addCustomCodePattern(pattern)
    }

    fun clearAllCustomPatterns() {
        smsParser.clearAllCustomPatterns()
    }

}


