package com.xxxx.parcel.util


import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.xxxx.parcel.model.SmsModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsUtil {
    companion object {
        fun readAllSms(context: Context): List<SmsModel> {
            val smsList = mutableListOf<SmsModel>()
            val contentResolver: ContentResolver = context.contentResolver
            val uri: Uri = "content://sms/inbox".toUri()

            try {
                val cursor = contentResolver.query(
                    uri,
                    arrayOf("_id", "body", "date"),
                    null,
                    null,
                    "date DESC"
                )
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                            val messageBody = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"))

                            smsList.add(SmsModel(id, messageBody, timestamp))
                        } while (cursor.moveToNext())
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "读取短信失败: ${e.message}", Toast.LENGTH_LONG).show()

                Log.e("SmsUtil", "读取短信失败: ${e.message}")
            }

            return smsList
        }
    }
}

fun dateToString(timestamp: Long): String {
    // 时间戳转可读格式
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(timestamp))

}