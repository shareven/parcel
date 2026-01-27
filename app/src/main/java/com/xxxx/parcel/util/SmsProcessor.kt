package com.xxxx.parcel.util

import android.content.Context
import com.xxxx.parcel.model.ParcelData
import com.xxxx.parcel.model.SmsData
import com.xxxx.parcel.model.SmsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProcessResult(
    val successful: List<SmsData>,
    val parcels: List<ParcelData>,
    val failed: List<SmsModel>
)

object SmsProcessor {

    suspend fun loadMessages(context: Context, daysFilter: Int): Pair<List<SmsModel>, List<SmsModel>> = withContext(Dispatchers.IO) {
        val systemSms = SmsUtil.readSmsByTimeFilter(context, daysFilter)
        val customSms = getCustomSmsByTimeFilter(context, daysFilter)
        Pair(systemSms, customSms)
    }

    suspend fun loadAndProcess(
        context: Context,
        daysFilter: Int,
        parser: SmsParser,
        completedIds: List<String>
    ): ProcessResult = withContext(Dispatchers.IO) {
        val systemSms = SmsUtil.readSmsByTimeFilter(context, daysFilter)
        val customSms = getCustomSmsByTimeFilter(context, daysFilter)
        val mergedList = systemSms + customSms
        val addressMappings = getAddressMappings(context)

        process(mergedList, parser, completedIds, addressMappings)
    }

    fun process(
        messages: List<SmsModel>,
        parser: SmsParser,
        completedIds: List<String>,
        addressMappings: Map<String, String> = emptyMap()
    ): ProcessResult {
        val successful = mutableListOf<SmsData>()
        val parcelsMap = mutableMapOf<String, ParcelData>()
        val failed = mutableListOf<SmsModel>()

        messages.forEach { sms ->
            val result = parser.parseSms(sms.body)

            if (result.success) {
                val combinedKey = "${sms.id}_${sms.timestamp}"
                val originalAddress = result.address
                // 获取分组用的地址（优先使用 tag）
                val groupAddress = addressMappings[originalAddress] ?: originalAddress
                
                val smsData = SmsData(originalAddress, result.code, sms, combinedKey)
                successful.add(smsData)

                // Grouping logic - use groupAddress for grouping
                val existingParcel = parcelsMap[groupAddress]
                val newItem = SmsData(originalAddress, result.code, sms, combinedKey)

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
                    parcelsMap[groupAddress] = ParcelData(
                        groupAddress,
                        mutableListOf(newItem)
                    )
                }
            } else {
                failed.add(sms)
            }
        }

        // Sorting
        successful.sortByDescending { it.sms.timestamp }
        failed.sortByDescending { it.timestamp }

        val initialParcels = parcelsMap.values.toList()

        // Sort parcel sms lists by code
        initialParcels.forEach { parcel ->
            parcel.smsDataList.sortBy { x -> x.code }
        }

        // Calculate num and isCompleted
        val finalParcels = recalculateParcels(initialParcels, completedIds)

        return ProcessResult(successful, finalParcels, failed)
    }

    fun recalculateParcels(parcels: List<ParcelData>, completedIds: List<String>): List<ParcelData> {
        val completedIdsSet = HashSet(completedIds)

        return parcels.map { parcel ->
            // Deep copy of SmsDataList with updated status
            val newSmsDataList = parcel.smsDataList.map { smsData ->
                val combinedKey = "${smsData.sms.id}_${smsData.sms.timestamp}"
                val simpleKey = smsData.sms.id

                val isCompleted = completedIdsSet.contains(combinedKey) || completedIdsSet.contains(simpleKey)

                smsData.copy(isCompleted = isCompleted)
            }.toMutableList()

            val newNum = newSmsDataList.sumOf { smsData ->
                if (!smsData.isCompleted) smsData.code.split(", ").size else 0
            }

            parcel.copy(smsDataList = newSmsDataList, num = newNum)
        }.sortedByDescending { it.num }
    }
}
