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
            return readSmsByTimeFilter(context, 0)
        }

        fun readSmsByTimeFilter(context: Context, daysFilter: Int): List<SmsModel> {
            val smsList = mutableListOf<SmsModel>()
            val contentResolver: ContentResolver = context.contentResolver
            val uri: Uri = "content://sms/inbox".toUri()

            var selection: String? = null
            var selectionArgs: Array<String>? = null

            if (daysFilter > 0) {
                // 计算从00:00:00开始的时间范围
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)

                // 减去天数
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -(daysFilter - 1))
                val startTime = calendar.timeInMillis
                selection = "date >= ?"
                selectionArgs = arrayOf(startTime.toString())
            }

            try {
                val cursor = contentResolver.query(
                    uri,
                    arrayOf("_id", "body", "date"),
                    selection,
                    selectionArgs,
                    "date DESC"
                )
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                            val messageBody = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"))

                            smsList.add(SmsModel(id.toString(), messageBody, timestamp))
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