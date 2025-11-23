package com.xxxx.parcel.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.content.ComponentName
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.xxxx.parcel.model.SmsModel
import com.xxxx.parcel.util.addCustomSms
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.getCustomList
import com.xxxx.parcel.util.isMainSwitchEnabled
import com.xxxx.parcel.util.isAppSwitchEnabled
import com.xxxx.parcel.util.getTitleForPackage
import com.xxxx.parcel.util.getTitlesForPackage
import com.xxxx.parcel.util.ThirdPartyDefaults
import com.xxxx.parcel.util.addLog
import com.xxxx.parcel.util.SmsUtil
import com.xxxx.parcel.util.getSystemSmsPackages
import com.xxxx.parcel.util.getSystemSmsNotifySwitch
import com.xxxx.parcel.util.getSystemSmsPackages
import com.xxxx.parcel.util.getSystemSmsNotifySwitch

class ParcelNotificationListenerService : NotificationListenerService() {

    companion object {
        @Volatile private var lastContent: String? = null
        @Volatile private var lastTs: Long = 0L
    }

    private val pddPackage = ThirdPartyDefaults.PDD_PACKAGE
    private val douyinPackage = ThirdPartyDefaults.DOUYIN_PACKAGE
    private val xhsPackage = ThirdPartyDefaults.XHS_PACKAGE
    private val wechatPackage = ThirdPartyDefaults.WECHAT_PACKAGE

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        val enabled = NotificationManagerCompat.getEnabledListenerPackages(applicationContext).contains(applicationContext.packageName)
        if (enabled) {
            val componentName = ComponentName(this, ParcelNotificationListenerService::class.java)
            requestRebind(componentName)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            val channelId = "parcel_notify_channel"
            if (Build.VERSION.SDK_INT >= 26) {
                val mgr = getSystemService(NotificationManager::class.java)
                if (mgr?.getNotificationChannel(channelId) == null) {
                    val ch = NotificationChannel(
                        channelId,
                        "监听状态",
                        NotificationManager.IMPORTANCE_MIN
                    )
                    ch.setShowBadge(false)
                    ch.enableLights(false)
                    ch.enableVibration(false)
                    mgr?.createNotificationChannel(ch)
                }
            }
            val notif = NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .setSmallIcon(com.xxxx.parcel.R.drawable.ic_notification)
                .setContentTitle("取件通知监听")
                .setContentText("监听已开启")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
            startForeground(1001, notif)
        } catch (_: Exception) { }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val context = applicationContext
        try {
            if (!isMainSwitchEnabled(context)) return

            val pkg = sbn.packageName ?: return
            val extras = sbn.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val conversationTitle = extras.getString(Notification.EXTRA_CONVERSATION_TITLE) ?: ""
            val subText = extras.getString(Notification.EXTRA_SUB_TEXT) ?: ""
            // 更稳健地提取通知正文，避免因为空文本而提前返回
            val text = extractNotificationText(extras)
            when (pkg) {
                pddPackage -> {
                    if (isAppSwitchEnabled(context, pddPackage) && title == getTitleForPackage(context, pddPackage, defaultTitle = ThirdPartyDefaults.defaultTitleFor(pddPackage))) {
                        addLog(context, "PDD通知: ${text}")
                        addNotificationAsCustomSmsIfNotInInboxDelayed(context, text)
                    }
                }
                douyinPackage -> {
                    if (isAppSwitchEnabled(context, douyinPackage) && title == getTitleForPackage(context, douyinPackage, defaultTitle = ThirdPartyDefaults.defaultTitleFor(douyinPackage))) {
                        addLog(context, "抖音通知: ${text}")
                        addNotificationAsCustomSmsIfNotInInboxDelayed(context, text)
                    }
                }
                xhsPackage -> {
                    if (isAppSwitchEnabled(context, xhsPackage) && title == getTitleForPackage(context, xhsPackage, defaultTitle = ThirdPartyDefaults.defaultTitleFor(xhsPackage))) {
                        addLog(context, "小红书通知: ${text}")
                        addNotificationAsCustomSmsIfNotInInboxDelayed(context, text)
                    }
                }
                wechatPackage -> {
                    // 微信通知标题通常为会话名；
                    if (isAppSwitchEnabled(context, wechatPackage)) {
                        val titles = getTitlesForPackage(context, wechatPackage, count = 5, defaultFirst = ThirdPartyDefaults.WECHAT_DEFAULT_FIRST)
                        val normalizedSaved = titles.filter { it.isNotBlank() }.map { it.trim() }
                        val candidates = listOf(title, conversationTitle, subText, sbn.notification.tickerText?.toString() ?: "")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        if (normalizedSaved.isNotEmpty() && candidates.any { cand -> normalizedSaved.any { it == cand } }) {
                            // 文本可能在 MessagingStyle 或 textLines 中，已在 extractNotificationText 处理
                            if (text.isNotBlank()) {
                                // 仅当解析成功时才保存
                                if (shouldSaveBasedOnParse(context, text)) {
                                    addLog(context, "微信通知: ${text}")
                                    addNotificationAsCustomSmsIfNotInInboxDelayed(context, text)
                                } else {
                                    Log.d(
                                        "ParcelNotifyService",
                                        "WeChat matched titles but parse failed; content not saved"
                                    )
                                    
                                }
                            } else {
                                Log.d(
                                    "ParcelNotifyService",
                                    "WeChat matched titles but text empty; extras may be MessagingStyle"
                                )
                                addLog(context, "微信通知文本为空")
                            }
                        }
                        // 调试日志（帮助定位为何未匹配）
                        else {
                            Log.d(
                                "ParcelNotifyService",
                                "WeChat unmatched. candidates=${candidates} saved=${normalizedSaved}"
                            )
                        }
                    }
                }
                else -> {
                    val systemPkgs = getSystemSmsPackages(context)
                    val systemEnabled = getSystemSmsNotifySwitch(context)
                    if (systemEnabled && systemPkgs.contains(pkg)) {
                        if (text.isNotBlank()) {
                            addLog(context, "短信通知: ${text}")
                            addNotificationAsCustomSmsIfNotInInboxDelayed(context, text)

                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ParcelNotifyService", "通知处理出错: ${e.message}")
            addLog(context,"ParcelNotifyService, 通知处理出错: ${e.message}")
        }
    }

    private fun addNotificationAsCustomSms(context: Context, content: String) {
        val now = System.currentTimeMillis()
        val sms = SmsModel(
            id = now.toString(),
            body = "【自定义取件短信】" + content,
            timestamp = now
        )
        addCustomSms(context, sms)
        // 广播通知 UI：已添加自定义短信
        try {
            val intent = Intent("com.xxxx.parcel.CUSTOM_SMS_ADDED")
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e("ParcelNotifyService", "广播失败: ${e.message}")
            addLog(context, "ParcelNotifyService, 广播失败: ${e.message}")
        }
    }

    private fun addNotificationAsCustomSmsIfNotInInboxDelayed(context: Context, content: String) {
        Thread {
            val nowStart = System.currentTimeMillis()
            val prev = lastContent
            val withinWindow = prev != null && prev == content && (nowStart - lastTs) < 2000L
            if (withinWindow) return@Thread
            try {
                Thread.sleep(1000L)
            } catch (_: Exception) {}
            val exists = try {
                SmsUtil.inboxContainsBodyRecent(context, content, 5 * 60 * 1000L)
            } catch (_: Exception) { false }
            if (!exists) {
                addNotificationAsCustomSms(context, content)
                addLog(context, "通知保存: ${content}")
                lastContent = content
                lastTs = nowStart
            } else {
                addLog(context, "通知文本已在短信箱，跳过保存: ${content}")
            }
        }.start()
    }

    // 仅保存解析成功的内容（加载自定义规则）
    private fun shouldSaveBasedOnParse(context: Context, content: String): Boolean {
        if (content.isBlank()) return false
        return try {
            val parser = SmsParser()
            // 加载自定义地址/取件码/忽略关键词规则
            getCustomList(context, "address").forEach { rule ->
                if (rule.isNotBlank()) parser.addCustomAddressPattern(rule)
            }
            getCustomList(context, "code").forEach { pattern ->
                if (pattern.isNotBlank()) parser.addCustomCodePattern(pattern)
            }
            getCustomList(context, "ignoreKeywords").forEach { kw ->
                if (kw.isNotBlank()) parser.addIgnoreKeyword(kw)
            }
            val result = parser.parseSms(content)
            result.success
        } catch (e: Exception) {
            Log.e("ParcelNotifyService", "解析出错: ${e.message}")
            addLog(context, "ParcelNotifyService 解析出错: ${e.message}")
            false
        }
    }


    // 更稳健地从通知 extras 中提取文本，兼容 MessagingStyle 与 textLines
    private fun extractNotificationText(extras: Bundle): String {
        val main = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence("android.text"))
            ?.toString()
        if (!main.isNullOrBlank()) return main

        // 尝试 textLines（有些应用把多行文本放这里）
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?: extras.getCharSequenceArray("android.textLines")
        val fromLines = lines?.mapNotNull { it?.toString() }?.lastOrNull { it.isNotBlank() }
        if (!fromLines.isNullOrBlank()) return fromLines

        // 尝试 MessagingStyle 的 android.messages（Bundle 中 text 字段）
        val messages = extras.getParcelableArray("android.messages")
        val lastMsgText = messages?.lastOrNull()?.let { it as? Bundle }?.getCharSequence("text")?.toString()
        if (!lastMsgText.isNullOrBlank()) return lastMsgText

        return ""
    }

    override fun onDestroy() {
        try { stopForeground(true) } catch (_: Exception) { }
        super.onDestroy()
    }
}
