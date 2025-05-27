package com.xxxx.parcel.util

import android.content.Context
import android.content.SharedPreferences
import com.xxxx.parcel.viewmodel.ParcelViewModel


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
    return sharedPreferences.getInt("timeFilterIndex",0)
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

fun removeCompletedId(context: Context, viewModel: ParcelViewModel,id:String){
    val completedIds = getCustomList(context,"completedIds")
    completedIds.remove(id)
    saveCustomList(context,"completedIds",completedIds)
    viewModel.removeCompletedId(id)
}

fun addCompletedIds(context: Context, viewModel: ParcelViewModel,ids:List<String>){
    val completedIds = getCustomList(context,"completedIds")
    completedIds.addAll(ids)
    saveCustomList(context,"completedIds",completedIds)
    viewModel.addCompletedIds(ids)
}

fun getAllSaveData(context: Context, viewModel: ParcelViewModel) {
    val listAddr = getCustomList(context, "address").toMutableList()
    val listCode = getCustomList(context, "code").toMutableList()
    val completedIds = getCustomList(context,"completedIds").toMutableList()
    val timeFilterIndex = getIndex(context)

    listAddr.forEach {
        viewModel.addCustomAddressPattern(it)
    }
    listCode.forEach {
        viewModel.addCustomCodePattern(it)
    }
    viewModel.setTimeFilterIndex(timeFilterIndex)
    viewModel.setAllCompletedIds(completedIds)
}


fun clearAllCustomPatternsa(context: Context, viewModel: ParcelViewModel) {
    // 获取 SharedPreferences 实例
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    // 获取 Editor 对象
    val editor = sharedPreferences.edit()
   saveCustomList(context,"address",mutableSetOf())
    saveCustomList(context,"code",mutableSetOf())
    // 异步提交更改
    editor.apply()
    viewModel.clearAllCustomPatterns()
}