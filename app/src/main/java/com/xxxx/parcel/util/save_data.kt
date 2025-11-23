package com.xxxx.parcel.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.xxxx.parcel.model.SmsModel
import com.xxxx.parcel.viewmodel.ParcelViewModel
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.isSameDay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString


// 保存时间index
fun saveIndex(context: Context, index: Int) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putInt("timeFilterIndex", index)
    editor.apply()
}

// 从 SharedPreferences 读取时间index
fun getIndex(context: Context): Int {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getInt("timeFilterIndex", 0)
}

// 保存字符串列表到 SharedPreferences
fun saveCustomList(context: Context, key: String, stringSet: Set<String>) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putStringSet(key, stringSet)
    editor.apply()
}

// 从 SharedPreferences 读取字符串列表
fun getCustomList(context: Context, key: String): MutableSet<String> {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
}

// 添加单个字符串到存储的字符串列表
fun addCustomList(context: Context, key: String, newString: String) {
    // 读取已有的字符串集合
    val existingSet = getCustomList(context, key)
    // 添加新的字符串
    existingSet.add(newString)
    // 保存更新后的集合
    saveCustomList(context, key, existingSet)
}

fun removeCompletedId(context: Context, viewModel: ParcelViewModel, id: String) {
    val completedIds = getCustomList(context, "completedIds")
    completedIds.remove(id)
    saveCustomList(context, "completedIds", completedIds)
    viewModel.removeCompletedId(id)
}

fun addCompletedIds(context: Context, viewModel: ParcelViewModel, ids: List<String>) {
    val completedIds = getCustomList(context, "completedIds")
    completedIds.addAll(ids)
    saveCustomList(context, "completedIds", completedIds)
    viewModel.addCompletedIds(ids)
}

fun getAllSaveData(context: Context, viewModel: ParcelViewModel) {
    val listAddr = getCustomList(context, "address").toMutableList()
    val listCode = getCustomList(context, "code").toMutableList()
    val completedIds = getCustomList(context, "completedIds").toMutableList()
    val ignoreKeywords = getCustomList(context, "ignoreKeywords").toMutableList()
    val timeFilterIndex = getIndex(context)

    listAddr.forEach {
        viewModel.addCustomAddressPattern(it)
    }
    listCode.forEach {
        viewModel.addCustomCodePattern(it)
    }
    ignoreKeywords.forEach {
        viewModel.addIgnoreKeyword(it)
    }
    viewModel.setTimeFilterIndex(timeFilterIndex)
    viewModel.setAllCompletedIds(completedIds)
}


fun clearCustomPattern(
    context: Context,
    key: String,
    pattern: String,
    viewModel: ParcelViewModel
) {

    val listPatterns = getCustomList(context, key)
    listPatterns.remove(pattern)
    saveCustomList(context, key, listPatterns)

    viewModel.clearAllCustomPatterns()
    getAllSaveData(context,viewModel)
}

fun clearAllCustomPatterns(context: Context, viewModel: ParcelViewModel) {
    saveCustomList(context, "address", mutableSetOf())
    saveCustomList(context, "code", mutableSetOf())
    viewModel.clearAllCustomPatterns()
}

// 保存自定义短信
fun addCustomSms(context: Context, sms: SmsModel) {
    val customSmsList = getCustomSmsList(context).toMutableList()
    customSmsList.add(sms)
    saveCustomSmsList(context, customSmsList)
}

// 获取自定义短信列表
fun getCustomSmsList(context: Context): List<SmsModel> {
    return getCustomSmsByTimeFilter(context, 0)
}

// 获取自定义短信列表（带时间过滤）
fun getCustomSmsByTimeFilter(context: Context, daysFilter: Int): List<SmsModel> {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val jsonString = sharedPreferences.getString("custom_sms_list", "[]") ?: "[]"
    
    val allCustomSms = try {
        Json.decodeFromString<List<SmsModel>>(jsonString)
    } catch (e: Exception) {
        emptyList()
    }
    
    // 如果没有时间过滤，返回所有自定义短信
    if (daysFilter <= 0) {
        return allCustomSms
    }
    
    // 计算从00:00:00开始的时间范围
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    
    // 减去天数
    calendar.add(java.util.Calendar.DAY_OF_YEAR, -(daysFilter - 1))
    val startTime = calendar.timeInMillis
    
    // 过滤自定义短信，只返回在时间范围内的
    return allCustomSms.filter { sms ->
        sms.timestamp >= startTime
    }
}

// 删除自定义短信
fun removeCustomSms(context: Context, smsId: String) {
    val customSmsList = getCustomSmsList(context).toMutableList()
    customSmsList.removeAll { it.id == smsId }
    saveCustomSmsList(context, customSmsList)
}

// 保存自定义短信列表
private fun saveCustomSmsList(context: Context, smsList: List<SmsModel>) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val jsonString = Json.encodeToString(smsList)
    editor.putString("custom_sms_list", jsonString)
    editor.apply()
}

// ===== 通知监听与应用标题/开关的本地存储封装 =====
fun getMainSwitch(context: Context): Boolean {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sp.getBoolean("notification_listener_enabled", false)
}

fun setMainSwitch(context: Context, value: Boolean) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sp.edit().putBoolean("notification_listener_enabled", value).apply()
}

fun getAppSwitch(context: Context, packageName: String): Boolean {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sp.getBoolean("listen_package_${packageName}", false)
}

fun setAppSwitch(context: Context, packageName: String, value: Boolean) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sp.edit().putBoolean("listen_package_${packageName}", value).apply()
}

fun getAppTitle(context: Context, packageName: String): String {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sp.getString("listen_title_${packageName}", "") ?: ""
}

fun setAppTitle(context: Context, packageName: String, title: String) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sp.edit().putString("listen_title_${packageName}", title).apply()
}

// 读取会话标题列表（优先使用新版 JSON Key，若存在则专用，不回退旧键）
fun getAppTitles(
    context: Context,
    packageName: String,
    count: Int = 5,
    defaultFirst: String = ""
): List<String> {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val jsonKey = "listen_titles_${packageName}"
    // 若 JSON Key 存在，严格仅用该键（即使为空也不回退旧键）
    if (sp.contains(jsonKey)) {
        val jsonStr = sp.getString(jsonKey, null)
        if (!jsonStr.isNullOrBlank()) {
            try {
                val arr = org.json.JSONArray(jsonStr)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    list.add(arr.optString(i))
                }
                val cleaned = list.filter { it.isNotBlank() }
                return if (cleaned.isNotEmpty()) cleaned else if (defaultFirst.isNotBlank()) listOf(defaultFirst) else emptyList()
            } catch (_: Exception) {
                // JSON 解析失败时返回空或默认
                return if (defaultFirst.isNotBlank()) listOf(defaultFirst) else emptyList()
            }
        }
        // JSON 字符串为空，返回空或默认
        return if (defaultFirst.isNotBlank()) listOf(defaultFirst) else emptyList()
    }

    // 旧版本兼容：按多标题旧键读取
    val titles = MutableList(count) { index ->
        val key = "listen_title_${packageName}_${index + 1}"
        sp.getString(key, "") ?: ""
    }

    // 若全部为空，尝试单标题旧键
    if (titles.all { it.isBlank() }) {
        val single = sp.getString("listen_title_${packageName}", "") ?: ""
        if (single.isNotBlank()) titles[0] = single
    }

    if (titles[0].isBlank() && defaultFirst.isNotBlank()) titles[0] = defaultFirst
    return titles.filter { it.isNotBlank() }.ifEmpty { if (defaultFirst.isNotBlank()) listOf(defaultFirst) else emptyList() }
}

fun setAppTitleAt(context: Context, packageName: String, index: Int, title: String) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sp.edit().putString("listen_title_${packageName}_${index}", title).apply()
}

fun setAppTitles(context: Context, packageName: String, titles: List<String>) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val cleaned = titles.map { it.trim() }.filter { it.isNotBlank() }
    val jsonArr = org.json.JSONArray()
    cleaned.forEach { jsonArr.put(it) }
    sp.edit().putString("listen_titles_${packageName}", jsonArr.toString()).apply()
}

// 提供给通知服务使用的别名函数，保持命名一致
fun isMainSwitchEnabled(context: Context): Boolean = getMainSwitch(context)

fun isAppSwitchEnabled(context: Context, packageName: String): Boolean = getAppSwitch(context, packageName)

fun getTitleForPackage(context: Context, packageName: String, defaultTitle: String = ""): String {
    val titles = getAppTitles(context, packageName, defaultFirst = defaultTitle)
    return titles.firstOrNull()?.takeIf { it.isNotBlank() } ?: defaultTitle
}

fun getTitlesForPackage(context: Context, packageName: String, count: Int = 5, defaultFirst: String? = null): List<String> {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val jsonKey = "listen_titles_${packageName}"
    if (sp.contains(jsonKey)) {
        val jsonStr = sp.getString(jsonKey, null)
        if (!jsonStr.isNullOrBlank()) {
            return try {
                val arr = org.json.JSONArray(jsonStr)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) list.add(arr.optString(i))
                list.filter { it.isNotBlank() }
            } catch (_: Exception) {
                emptyList()
            }
        }
        return emptyList()
    }
    // 旧键读取
    val titles = MutableList(count) { index ->
        val key = "listen_title_${packageName}_${index + 1}"
        sp.getString(key, "") ?: ""
    }
    if (titles.all { it.isBlank() }) {
        val single = sp.getString("listen_title_${packageName}", "") ?: ""
        if (single.isNotBlank()) titles[0] = single
    }
    val cleaned = titles.filter { it.isNotBlank() }
    return if (cleaned.isNotEmpty()) cleaned else if (defaultFirst != null && defaultFirst.isNotBlank()) listOf(defaultFirst) else emptyList()
}

object ThirdPartyDefaults {
    const val PDD_PACKAGE = "com.xunmeng.pinduoduo"
    const val DOUYIN_PACKAGE = "com.ss.android.ugc.aweme"
    const val XHS_PACKAGE = "com.xingin.xhs"
    const val WECHAT_PACKAGE = "com.tencent.mm"

    fun defaultTitleFor(packageName: String): String = when (packageName) {
        PDD_PACKAGE -> "商品待取件提醒"
        DOUYIN_PACKAGE -> "包裹已放至自提柜/代收点"
        XHS_PACKAGE -> "订单待取件"
        else -> ""
    }

    const val WECHAT_DEFAULT_FIRST = "老婆"
}

@kotlinx.serialization.Serializable
data class LogEntry(val timestamp: Long, val text: String, val version: String = "")

fun addLog(context: Context, text: String) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val arrStr = sp.getString("logs_json", null)
    val list = try {
        if (arrStr.isNullOrBlank()) mutableListOf<LogEntry>() else Json.decodeFromString<List<LogEntry>>(arrStr).toMutableList()
    } catch (_: Exception) { mutableListOf() }
    val ver = getAppVersionName(context)
    list.add(LogEntry(System.currentTimeMillis(), text, ver))
    sp.edit().putString("logs_json", Json.encodeToString(list)).apply()
}

fun getAppVersionName(context: Context): String {
    return try {
        val pm = context.packageManager
        val pInfo = pm.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: ""
    } catch (_: Exception) { "" }
}

fun getLogs(context: Context, dayMillis: Long? = null): List<LogEntry> {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val arrStr = sp.getString("logs_json", null) ?: return emptyList()
    val list = try { Json.decodeFromString<List<LogEntry>>(arrStr) } catch (_: Exception) { emptyList() }
    if (dayMillis == null) return list.sortedByDescending { it.timestamp }
    val cal = java.util.Calendar.getInstance().apply {
        timeInMillis = dayMillis
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
    val end = cal.timeInMillis
    return list.filter { it.timestamp in start until end }.sortedByDescending { it.timestamp }
}

fun clearAllLogs(context: Context) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sp.edit().remove("logs_json").apply()
}

fun hasCustomSameDayCode(context: Context, content: String): Boolean {
    val parser = SmsParser()
    getCustomList(context, "address").forEach { if (it.isNotBlank()) parser.addCustomAddressPattern(it) }
    getCustomList(context, "code").forEach { if (it.isNotBlank()) parser.addCustomCodePattern(it) }
    getCustomList(context, "ignoreKeywords").forEach { if (it.isNotBlank()) parser.addIgnoreKeyword(it) }
    val r = parser.parseSms(content)
    if (!r.success) return false
    val recent = getCustomSmsByTimeFilter(context, 1)
    return recent.any { sms ->
        val body = sms.body.removePrefix("【自定义取件短信】")
        val rr = parser.parseSms(body)
        rr.success && rr.address == r.address && rr.code == r.code && isSameDay(sms.timestamp, System.currentTimeMillis())
    }
}

fun hasCustomSameDayBody(context: Context, content: String): Boolean {
    val recent = getCustomSmsByTimeFilter(context, 1)
    return recent.any { sms ->
        val body = sms.body.removePrefix("【自定义取件短信】")
        body == content && isSameDay(sms.timestamp, System.currentTimeMillis())
    }
}

fun getSystemSmsNotifySwitch(context: Context): Boolean {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sp.getBoolean("listen_system_sms_notify", true)
}

fun setSystemSmsNotifySwitch(context: Context, value: Boolean) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sp.edit().putBoolean("listen_system_sms_notify", value).apply()
}

fun getSystemSmsPackages(context: Context): MutableSet<String> {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sp.getStringSet("system_sms_pkgs", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
}

fun setSystemSmsPackages(context: Context, pkgs: Set<String>) {
    val sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sp.edit().putStringSet("system_sms_pkgs", pkgs.toSet()).apply()
}

fun addSystemSmsPackage(context: Context, pkg: String) {
    val set = getSystemSmsPackages(context)
    set.add(pkg)
    setSystemSmsPackages(context, set)
}

fun removeSystemSmsPackage(context: Context, pkg: String) {
    val set = getSystemSmsPackages(context)
    set.remove(pkg)
    setSystemSmsPackages(context, set)
}
