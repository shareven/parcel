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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class ParcelViewModel(private val smsParser: SmsParser = SmsParser()) : ViewModel() {
    // 所有短信列表
    private val _allMessages = MutableStateFlow<List<SmsModel>>(emptyList())

    // 解析成功的短信
    private val _successSmsData = MutableStateFlow<List<SmsData>>(emptyList())
    val successSmsData: StateFlow<List<SmsData>> = _successSmsData

    // 解析失败的短信
    private val _failedMessages = MutableStateFlow<List<SmsModel>>(emptyList())
    val failedMessages: StateFlow<List<SmsModel>> = _failedMessages

    // 同一地址的取件码列表
    private val _parcelsData = MutableStateFlow<List<ParcelData>>(emptyList())
    val parcelsData: StateFlow<List<ParcelData>> = _parcelsData

    // 时间过滤器
    private val _timeFilterIndex = MutableStateFlow(0)
    val timeFilterIndex: StateFlow<Int> = _timeFilterIndex.asStateFlow()

    fun setTimeFilterIndex(i: Int) {
        _timeFilterIndex.value = i
        handleReceivedSms()
    }

    fun clearData() {
        _successSmsData.value = emptyList()
        _failedMessages.value = emptyList()
        _parcelsData.value = emptyList()
    }

    fun getAllMessage(list: List<SmsModel>) {

        _allMessages.value = list
        handleReceivedSms()
    }

    // 处理接收到的短信
    fun handleReceivedSms() {
        clearData()
        viewModelScope.launch {
            _allMessages.value.forEach { sms ->
                val currentSuccessful = _successSmsData.value.toMutableList()
                val currentParcels = _parcelsData.value.toMutableList()
                val currentFailed = _failedMessages.value.toMutableList()


                val result = smsParser.parseSms(sms.body)


                val currentTime = System.currentTimeMillis()
                val messageTime = sms.timestamp

                var includeMessage = true

                if (_timeFilterIndex.value == 0) {
                    // 不进行时间过滤
                    includeMessage = true
                } else {
                    // 获取当前时间和消息时间的日期部分
                    val currentLocalTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(currentTime),
                        ZoneId.systemDefault()
                    ).toLocalDate()
                    val messageLocalTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(messageTime),
                        ZoneId.systemDefault()
                    ).toLocalDate()

                    // 计算两个日期之间的天数差
                    val daysDifference =
                        ChronoUnit.DAYS.between(messageLocalTime, currentLocalTime)

                    // 根据选择的时间范围判断是否包含该消息
                    includeMessage = daysDifference < _timeFilterIndex.value
                }

                if (includeMessage) {
                    if (result.success) {
                        Log.d("成功短信", sms.body)
                        Log.d("解析", "addr:${result.address} code:${result.code} ")
                        currentSuccessful.add(SmsData(result.address, result.code, sms, sms.id))
                        // 把同一地址的取件码添加到 parcels 列表中
                        currentParcels.find { it.address == result.address }?.let {
                            it.codes.add(result.code)
                            it.codes.sort()
                        } ?: run {
                            currentParcels.add(
                                ParcelData(
                                    result.address,
                                    mutableListOf(result.code)
                                )
                            )
                        }
                    } else {
                        Log.e("失败短信", sms.body)
                        Log.e("解析", "addr:${result.address} code:${result.code} ")
                        currentFailed.add(sms)
                    }


                    currentParcels.sortBy { -it.codes.size }
                    _successSmsData.emit(currentSuccessful)
                    _parcelsData.emit(currentParcels)
                    _failedMessages.emit(currentFailed)

                }


            }
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


