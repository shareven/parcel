package com.xxxx.parcel.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.provider.Telephony
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import java.io.File
import java.io.FileInputStream
import java.util.Properties

object PermissionUtil {
    fun hasSmsPermissions(context: Context): Boolean {
        if (isMIUI()) {
            return isMiuiSmsGranted(context)

        } else {
            // 标准Android动态申请
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 检测是否为MIUI系统
    fun isMIUI(): Boolean {
        return try {
            Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) &&
                    File(Environment.getRootDirectory(), "build.prop").exists()
        } catch (e: Exception) {
            false
        }
    }

    fun showMiuiPermissionExplanationDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("开启取件码短信权限")
        builder.setMessage(
            "小米手机需开启2个权限：\n" +
                    "1. 短信 → 始终允许\n" +
                    "2. 其他权限 → 通知类短信 → 始终允许"
        )
        builder.setPositiveButton("去开启") { dialog, which ->
            // 在这里调用跳转 MIUI 权限设置页的方法
            requestMiuiSmsPermission(context as android.app.Activity)
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // 跳转MIUI权限设置页
    fun requestMiuiSmsPermission(activity: Activity) {
        try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    if (isMiuiV9OrHigher()) "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    else "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
                )
                putExtra("extra_pkgname", activity.packageName)
            }
            activity.startActivityForResult(intent, 1)
        } catch (e: Exception) {
            // 备用方案：跳转系统设置
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivityForResult(intent, 1)
        }
    }

    private fun isMiuiV9OrHigher(): Boolean {
        return try {
            val properties = Properties().apply {
                load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
            }
            properties.getProperty("ro.miui.ui.version.code", "0").toInt() >= 9
        } catch (e: Exception) {
            false
        }
    }

    // 检测 HyperOS「通知类短信」权限是否疑似被拒绝。
    // 该权限被拒时 MIUI 的 SmsProvider 不抛异常，只是静默过滤服务号短信，
    // 因此用启发式探测：最近30天短信 >= 10 条且没有任何服务号（纯数字且非11位手机号）时判定疑似。
    // 需在 IO 线程调用。
    fun isNotificationSmsLikelyFiltered(context: Context): Boolean {
        return try {
            val since = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.ADDRESS),
                "date >= ?",
                arrayOf(since.toString()),
                "date DESC"
            ) ?: return false
            var total = 0
            var serviceCount = 0
            cursor.use { c ->
                while (c.moveToNext() && total < 50) {
                    total++
                    val address = c.getString(0) ?: continue
                    if (address.isNotEmpty() && address.all { it.isDigit() } && address.length != 11) {
                        serviceCount++
                    }
                }
            }
            total >= 10 && serviceCount == 0
        } catch (e: Exception) {
            false
        }
    }

    fun isNotifySmsPromptDismissed(context: Context): Boolean {
        return try {
            context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
                .getBoolean("notify_sms_prompt_dismissed", false)
        } catch (e: Exception) {
            false
        }
    }

    fun setNotifySmsPromptDismissed(context: Context) {
        try {
            context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
                .edit { putBoolean("notify_sms_prompt_dismissed", true) }
        } catch (e: Exception) {
        }
    }

}
// 检测MIUI实际权限状态
private fun isMiuiSmsGranted(context: Context): Boolean {
    return try {
        // 尝试读取短信验证触发权限检查
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY),
            null,
            null,
            null
        )
        cursor?.close()
        true
    } catch (e: SecurityException) {
        false
    }
}