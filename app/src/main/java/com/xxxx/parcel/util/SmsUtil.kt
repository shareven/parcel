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
import java.util.Calendar

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
                addLog(context, "读取短信失败: ${e.message}")
            }

            return smsList
        }

        fun inboxContainsBodyRecent(context: Context, body: String, windowMs: Long = 5 * 60 * 1000L): Boolean {
            return try {
                val resolver = context.contentResolver
                val uri: Uri = "content://sms/inbox".toUri()
                val now = System.currentTimeMillis()
                val selection = "date >= ? AND body = ?"
                val args = arrayOf((now - windowMs).toString(), body)
                resolver.query(uri, arrayOf("_id"), selection, args, null)?.use { c ->
                    c.moveToFirst()
                } ?: false
            } catch (e: Exception) {
                false
            }
        }
    }
}

fun dateToString(timestamp: Long): String {
    // 时间戳转可读格式
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(timestamp))

}

// 判断是否为自定义短信
fun isCustomSms(sms: SmsModel): Boolean {
    return sms.body.startsWith("【自定义取件短信】")
}


fun isSameDay(ts1: Long, ts2: Long): Boolean {
    val c1 = Calendar.getInstance()
    c1.timeInMillis = ts1
    val c2 = Calendar.getInstance()
    c2.timeInMillis = ts2
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}
