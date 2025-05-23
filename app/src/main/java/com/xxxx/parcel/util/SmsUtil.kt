package com.xxxx.parcel.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.xxxx.parcel.model.SmsModel
import androidx.core.net.toUri

class SmsUtil {
    companion object {
        fun readAllSms(context: Context): List<SmsModel> {
            val smsList = mutableListOf<SmsModel>()
            val contentResolver: ContentResolver = context.contentResolver
            val uri: Uri = "content://sms/inbox".toUri()

            try {
                val cursor = contentResolver.query(uri, arrayOf("_id","body"), null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                            val messageBody = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                            smsList.add(SmsModel(id,messageBody))
                        } while (cursor.moveToNext())
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                Log.e("SmsUtil", "读取短信失败: ${e.message}")
            }

            return smsList
        }
    }
}