package com.xxxx.parcel.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.appwidget.AppWidgetManager
import androidx.core.app.NotificationManagerCompat
import com.xxxx.parcel.service.ParcelNotificationListenerService
import com.xxxx.parcel.widget.ParcelWidget
import com.xxxx.parcel.widget.ParcelWidgetLarge
import com.xxxx.parcel.widget.ParcelWidgetXL

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_USER_UNLOCKED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val enabled = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
            if (!enabled) return
            try {
                context.startService(Intent(context, ParcelNotificationListenerService::class.java))
            } catch (_: Throwable) {}
            val pm = context.packageManager
            val cn = ComponentName(context, ParcelNotificationListenerService::class.java)
            pm.setComponentEnabledSetting(
                cn,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
            pm.setComponentEnabledSetting(
                cn,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )

            try {
                ParcelWidget.updateAppWidget(
                    context,
                    AppWidgetManager.getInstance(context),
                    null,
                    null
                )
                ParcelWidget.updateAllByProvider(context, ParcelWidgetLarge::class.java, null)
                ParcelWidget.updateAllByProvider(context, ParcelWidgetXL::class.java, null)
            } catch (_: Throwable) {}
        }
    }
}
