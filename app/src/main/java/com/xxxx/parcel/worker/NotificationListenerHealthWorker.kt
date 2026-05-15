package com.xxxx.parcel.worker

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.xxxx.parcel.service.ParcelNotificationListenerService
import com.xxxx.parcel.util.addLog
import com.xxxx.parcel.util.getMainSwitch

class NotificationListenerHealthWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val hasAccess = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
        val mainEnabled = getMainSwitch(context)

        if (!hasAccess || !mainEnabled) {
            return Result.success()
        }

        val component = ComponentName(context, ParcelNotificationListenerService::class.java)
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: ""
        val full = component.flattenToString()
        val short = "${context.packageName}/.service.ParcelNotificationListenerService"
        val isEnabled = flat.split(":").any { it == full || it == short }

        if (!isEnabled) {
            addLog(context, "健康检查: 通知监听权限丢失，尝试恢复")
            restartListenerViaComponentToggle(context)
            return Result.success()
        }

        try {
            context.startService(
                android.content.Intent(context, ParcelNotificationListenerService::class.java)
            )
        } catch (_: Throwable) {}

        try {
            NotificationListenerService.requestRebind(component)
        } catch (_: Exception) {}

        return Result.success()
    }

    private fun restartListenerViaComponentToggle(context: Context) {
        val cn = ComponentName(context, ParcelNotificationListenerService::class.java)
        val pm = context.packageManager
        try {
            pm.setComponentEnabledSetting(
                cn,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
            Thread.sleep(500)
            pm.setComponentEnabledSetting(
                cn,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        } catch (_: Exception) {}

        try {
            val full = cn.flattenToString()
            val cr = context.contentResolver
            val flat0 = Settings.Secure.getString(cr, "enabled_notification_listeners") ?: ""
            val list0 = flat0.split(":").filter { it.isNotBlank() }.toMutableList()
            if (list0.contains(full)) {
                list0.remove(full)
                Settings.Secure.putString(cr, "enabled_notification_listeners", list0.joinToString(":"))
                Thread.sleep(1000L)
            }
            val flat1 = Settings.Secure.getString(cr, "enabled_notification_listeners") ?: ""
            val list1 = flat1.split(":").filter { it.isNotBlank() }.toMutableList()
            if (!list1.contains(full)) {
                list1.add(full)
                Settings.Secure.putString(cr, "enabled_notification_listeners", list1.joinToString(":"))
            }
            Thread.sleep(2000L)
            NotificationListenerService.requestRebind(cn)
        } catch (_: Exception) {}
    }

    companion object {
        const val WORK_NAME = "notification_listener_health_check"
    }
}
