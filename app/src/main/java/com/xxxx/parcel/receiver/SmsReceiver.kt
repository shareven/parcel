package com.xxxx.parcel.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.xxxx.parcel.service.SmsSyncService
import com.xxxx.parcel.util.addLog

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            try {
                val i = Intent(context, SmsSyncService::class.java)
                if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i) else context.startService(i)
                addLog(context, "短信接收广播触发,启动前台服务")
            } catch (_: Throwable) {}
        }
    }
}
