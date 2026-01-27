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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        _parcelsData.value = calculateNumAndIsCompleted(_parcelsData.value)
    }

    fun removeCompletedId(key: String) {
        val data = _allCompletedIds.value.toMutableList()
        data.remove(key)
        _allCompletedIds.value = data
        _parcelsData.value = calculateNumAndIsCompleted(_parcelsData.value)
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
            val allMessages = _allMessages.value
            
            val (newSuccessful, newParcels, newFailed) = withContext(Dispatchers.Default) {
                val successful = mutableListOf<SmsData>()
                val parcelsMap = mutableMapOf<String, ParcelData>()
                val failed = mutableListOf<SmsModel>()

                allMessages.forEach { sms ->
                    val result: SmsParser.ParseResult = smsParser.parseSms(sms.body)

                    if (result.success) {
                        Log.d("成功短信", sms.body)
                        Log.d("解析", "addr:${result.address} code:${result.code} ")
                        val combinedKey = "${sms.id}_${sms.timestamp}"
                        
                        successful.add(SmsData(result.address, result.code, sms, combinedKey))

                        // 把同一地址的取件码添加到 parcels 列表中
                        val existingParcel = parcelsMap[result.address]
                        
                        val newItem = SmsData(result.address, result.code, sms, combinedKey)
                        if (existingParcel != null) {
                            val existsSameDaySameAddrCode = existingParcel.smsDataList.any { existing ->
                                existing.address == newItem.address &&
                                        existing.code == newItem.code &&
                                        isSameDay(existing.sms.timestamp, newItem.sms.timestamp)
                            }
                            if (!existsSameDaySameAddrCode) {
                                existingParcel.smsDataList.add(newItem)
                            }
                        } else {
                            parcelsMap[result.address] = ParcelData(
                                result.address,
                                mutableListOf(newItem)
                            )
                        }
                    } else {
                        Log.e("失败短信", sms.body)
                        Log.e("解析", "addr:${result.address} code:${result.code} ")
                        failed.add(sms)
                    }
                }

                // 按时间降序排序
                successful.sortByDescending { it.sms.timestamp }
                failed.sortByDescending { it.timestamp }

                val parcelList = parcelsMap.values.toList()
                // Sort parcel sms lists
                parcelList.forEach { parcel ->
                    parcel.smsDataList.sortBy { x -> x.code }
                }

                Triple(successful, parcelList, failed)
            }

            _successSmsData.value = newSuccessful
            _failedMessages.value = newFailed

            val finalParcels = calculateNumAndIsCompleted(newParcels)
            _parcelsData.value = finalParcels
        }
    }


    //计算包裹数量, 判断是否已取件
    private fun calculateNumAndIsCompleted(parcels: List<ParcelData>): List<ParcelData> {
        val completedIdsSet = HashSet(_allCompletedIds.value)

        return parcels.map { parcel ->
            parcel.copy().apply {
                num = smsDataList.sumOf { smsData ->
                    val combinedKey = "${smsData.sms.id}_${smsData.sms.timestamp}"
                    val simpleKey = smsData.sms.id
                    
                    val isCompleted = completedIdsSet.contains(combinedKey) || completedIdsSet.contains(simpleKey)
                    
                    smsData.isCompleted = isCompleted
                    if (!isCompleted) smsData.code.split(", ").size else 0
                }
            }
        }.sortedByDescending { it.num }
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


