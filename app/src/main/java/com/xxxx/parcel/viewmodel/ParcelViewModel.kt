package com.xxxx.parcel.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xxxx.parcel.model.ParcelData
import com.xxxx.parcel.model.SmsData
import com.xxxx.parcel.model.SmsModel
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.getCustomList
import com.xxxx.parcel.util.isSameDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ParcelViewModel(
    private val smsParser: SmsParser = SmsParser(),
    private val context: Context? = null
) : ViewModel() {
    // 所有短信列表
    private val _allMessages = MutableStateFlow<List<SmsModel>>(emptyList())

    // 所有已取件id列表
    private val _allCompletedIds = MutableStateFlow<List<String>>(emptyList())

    init {
        context?.let {
            val completedIds = getCustomList(it, "completedIds").toMutableList()
            _allCompletedIds.value = completedIds
        }
    }

    // 解析成功的短信
    private val _successSmsData = MutableStateFlow<List<SmsData>>(emptyList())
    val successSmsData: StateFlow<List<SmsData>> = _successSmsData

    // 解析失败的短信
    private val _failedMessages = MutableStateFlow<List<SmsModel>>(emptyList())
    val failedMessages: StateFlow<List<SmsModel>> = _failedMessages

    // 同一地址的取件码列表
    private val _parcelsData = MutableStateFlow<List<ParcelData>>(emptyList())
    val parcelsData: StateFlow<List<ParcelData>> = _parcelsData.asStateFlow()

    // 时间过滤器
    private val _timeFilterIndex = MutableStateFlow(0)
    val timeFilterIndex: StateFlow<Int> = _timeFilterIndex.asStateFlow()

    fun setTimeFilterIndex(i: Int) {
        _timeFilterIndex.value = i
    }

    fun setAllCompletedIds(list: List<String>) {
        _allCompletedIds.value = list
    }

    fun addCompletedIds(list: List<String>) {
        val data = _allCompletedIds.value.toMutableList()
        data.addAll(list)
        _allCompletedIds.value = data
        calculateNumAndIsCompleted()
    }

    fun removeCompletedId(key: String) {
        val data = _allCompletedIds.value.toMutableList()
        data.remove(key)
        _allCompletedIds.value = data
        calculateNumAndIsCompleted()
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
    
    fun getAllMessageWithCustom(list: List<SmsModel>, customSmsList: List<SmsModel>) {
        val combinedList = list + customSmsList
        _allMessages.value = combinedList
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


                val result: SmsParser.ParseResult = smsParser.parseSms(sms.body)

                    if (result.success) {
                        Log.d("成功短信", sms.body)
                        Log.d("解析", "addr:${result.address} code:${result.code} ")
                        val combinedKey = "${sms.id}_${sms.timestamp}"
                        currentSuccessful.add(SmsData(result.address, result.code, sms, combinedKey))
                        // 把同一地址的取件码添加到 parcels 列表中
                        currentParcels.find { it.address == result.address }?.let { parcel ->
                            val newItem = SmsData(result.address, result.code, sms, combinedKey)
                            val existsSameDaySameAddrCode = parcel.smsDataList.any { existing ->
                                existing.address == newItem.address &&
                                        existing.code == newItem.code &&
                                        isSameDay(existing.sms.timestamp, newItem.sms.timestamp)
                            }
                            if (!existsSameDaySameAddrCode) {
                                parcel.smsDataList.add(newItem)
                                parcel.smsDataList.sortBy { x -> x.code }
                            }
                        } ?: run {
                            currentParcels.add(
                                ParcelData(
                                    result.address,
                                    mutableListOf(SmsData(result.address, result.code, sms, combinedKey))
                                )
                            )
                        }
                    } else {
                        Log.e("失败短信", sms.body)
                        Log.e("解析", "addr:${result.address} code:${result.code} ")
                        currentFailed.add(sms)
                    }
                    // 按时间降序排序
                    currentSuccessful.sortByDescending { it.sms.timestamp }
                    currentFailed.sortByDescending { it.timestamp }
                    _successSmsData.emit(currentSuccessful)
                    _parcelsData.emit(currentParcels)
                    _failedMessages.emit(currentFailed)

                    calculateNumAndIsCompleted()
            }
        }
    }


    //计算包裹数量, 判断是否已取件
    private     fun calculateNumAndIsCompleted() {

        _parcelsData.value.let { currentList ->
            val newList = currentList.map { parcels ->
                parcels.copy().apply {
                    num = smsDataList.sumOf { smsData ->
                        val isCompleted = _allCompletedIds.value.any { stored ->
                            if (stored.contains("_")) {
                                stored == "${smsData.sms.id}_${smsData.sms.timestamp}"
                            } else {
                                stored == smsData.sms.id
                            }
                        }
                        smsData.isCompleted = isCompleted
                        if (!isCompleted) smsData.code.split(", ").size else 0
                    }
                }
            }.sortedByDescending { it.num }

            _parcelsData.value = newList
        }
    }

    // 将自定义规则添加到 SmsParser
    fun addCustomAddressPattern(pattern: String) {
        smsParser.addCustomAddressPattern(pattern)
    }

    fun addCustomCodePattern(pattern: String) {
        smsParser.addCustomCodePattern(pattern)
    }

    fun addIgnoreKeyword(keyword: String) {
        smsParser.addIgnoreKeyword(keyword)
    }

    fun clearAllCustomPatterns() {
        smsParser.clearAllCustomPatterns()
    }

}


