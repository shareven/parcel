package com.xxxx.parcel.service

import android.graphics.drawable.Icon
import android.content.Context
import android.content.ComponentName
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.xxxx.parcel.service.ParcelNotificationListenerService
import com.xxxx.parcel.util.getMainSwitch
import com.xxxx.parcel.util.setMainSwitch
import android.content.Intent
import android.service.notification.NotificationListenerService
import com.xxxx.parcel.R

class NotificationAccessTileService : TileService() {
    override fun onStartListening() {
        val tile = qsTile ?: return
        val mainEnabled = getMainSwitch(this)
        val hasAccess = isNotificationAccessGranted(this)
        tile.state = if (mainEnabled && hasAccess) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_notification)
        tile.updateTile()
        val t = System.currentTimeMillis()
        if (t - lastFixTime > 3000L) {
            lastFixTime = t
            fixRestartNotificationListener()
        }
    }

    override fun onClick() {
        val hasAccess = isNotificationAccessGranted(this)
        val mainEnabled = getMainSwitch(this)
        if (!hasAccess) {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
        } else {
            setMainSwitch(this, !mainEnabled)
            if (!mainEnabled) {
                try {
                    NotificationListenerService.requestRebind(ComponentName(this, ParcelNotificationListenerService::class.java))
                } catch (_: Exception) { }
                fixRestartNotificationListener()
            }
        }
        onStartListening()
    }

    private fun isNotificationAccessGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrBlank()) return false
        val full = ComponentName(context, ParcelNotificationListenerService::class.java).flattenToString()
        val short = "${context.packageName}/.service.ParcelNotificationListenerService"
        return flat.split(":").any { it == full || it == short }
    }

    private fun fixRestartNotificationListener() {
        val ctx = this
        Thread {
            try {
                val full = ComponentName(ctx, ParcelNotificationListenerService::class.java).flattenToString()
                val cr = ctx.contentResolver
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
                try {
                    NotificationListenerService.requestRebind(ComponentName(ctx, ParcelNotificationListenerService::class.java))
                } catch (_: Exception) { }
            } catch (_: Exception) { }
        }.start()
    }

    companion object {
        private var lastFixTime = 0L
    }
}
