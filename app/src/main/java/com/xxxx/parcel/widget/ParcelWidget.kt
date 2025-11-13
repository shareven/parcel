package com.xxxx.parcel.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.xxxx.parcel.util.getCustomList
import com.xxxx.parcel.util.getCustomSmsList
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.isSameDay
import com.xxxx.parcel.util.SmsUtil
import com.xxxx.parcel.util.getIndex
import com.xxxx.parcel.MainActivity
import com.xxxx.parcel.R
import com.xxxx.parcel.util.getIndex
import com.xxxx.parcel.viewmodel.ParcelViewModel

class ParcelWidget : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {

        if ("miui.appwidget.action.APPWIDGET_UPDATE".equals(intent.getAction()) ||
            "com.xxxx.parcel.CUSTOM_SMS_ADDED".equals(intent.getAction())) {

                // 获取 ParcelViewModel 实例
            val viewModel = (context.applicationContext as? ViewModelStoreOwner)?.let {
                ViewModelProvider(it)[ParcelViewModel::class.java]
            }
            updateAppWidget(
                context,
                AppWidgetManager.getInstance(context),
                null,
                viewModel
            )

        } else {

            super.onReceive(context, intent);

        }

    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 获取 ParcelViewModel 实例
        val viewModel = (context.applicationContext as? ViewModelStoreOwner)?.let {
            ViewModelProvider(it)[ParcelViewModel::class.java]
        }

        // 为每个小部件执行更新
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, viewModel)
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        // 当第一个小部件被添加时调用
        // 获取 ParcelViewModel 实例
        if(context!=null) {
            val viewModel = (context.applicationContext as? ViewModelStoreOwner)?.let {
                ViewModelProvider(it)[ParcelViewModel::class.java]
            }
            updateAppWidget(
                context,
                AppWidgetManager.getInstance(context),
                null,
                viewModel
            )
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        // 当最后一个小部件被移除时调用
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int?,
            viewModel: ParcelViewModel?
        ) {
            // 如果没有提供 appWidgetId，则更新所有实例
            if (appWidgetId == null) {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(
                    android.content.ComponentName(context, ParcelWidget::class.java)
                )
                for (id in ids) {
                    updateSingleAppWidget(context, manager, id, viewModel)
                }
            } else {
                updateSingleAppWidget(context, appWidgetManager, appWidgetId, viewModel)
            }
        }

        fun updateAllByProvider(
            context: Context,
            providerClass: Class<out AppWidgetProvider>,
            viewModel: ParcelViewModel?
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, providerClass)
            )
            for (id in ids) {
                updateSingleAppWidget(context, manager, id, viewModel)
            }
        }

        fun updateSingleAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            viewModel: ParcelViewModel?
        ) {
            var total = 0
            var address1 = ""
            var codeList1 = ""
            var address2 = ""
            var codeList2 = ""
            var address3 = ""
            var codeList3 = ""
            var address4 = ""
            var codeList4 = ""
            var address5 = ""
            var codeList5 = ""
            var address6 = ""
            var codeList6 = ""

            val parcels = viewModel?.parcelsData?.value
            if (parcels != null && parcels.isNotEmpty()) {
                total = parcels.sumOf { it.num }
                fun fill(idx: Int, setAddr: (String)->Unit, setCodes: (String)->Unit) {
                    val item = parcels.getOrNull(idx)
                    if (item != null && item.num > 0) {
                        val codes = item.smsDataList.filter { !it.isCompleted }.map { it.code }.joinToString("\n")
                        setAddr(item.address + "（${item.num}）")
                        setCodes(codes)
                    } else {
                        setAddr("")
                        setCodes("")
                    }
                }
                fill(0, { address1 = it }, { codeList1 = it })
                fill(1, { address2 = it }, { codeList2 = it })
                fill(2, { address3 = it }, { codeList3 = it })
                fill(3, { address4 = it }, { codeList4 = it })
                fill(4, { address5 = it }, { codeList5 = it })
                fill(5, { address6 = it }, { codeList6 = it })
            } else {
                val parser = SmsParser()
                getCustomList(context, "address").forEach { if (it.isNotBlank()) parser.addCustomAddressPattern(it) }
                getCustomList(context, "code").forEach { if (it.isNotBlank()) parser.addCustomCodePattern(it) }
                getCustomList(context, "ignoreKeywords").forEach { if (it.isNotBlank()) parser.addIgnoreKeyword(it) }
                val completedIds = getCustomList(context, "completedIds")
                val daysFilter = getIndex(context)
                val mergedList = (SmsUtil.readSmsByTimeFilter(context, daysFilter) + getCustomSmsList(context))
                val grouped = mutableMapOf<String, MutableList<Triple<String, Long, String>>>()
                mergedList.forEach { sms ->
                    val r = parser.parseSms(sms.body)
                    if (r.success) {
                        val addr = r.address
                        val code = r.code
                        val list = grouped.getOrPut(addr) { mutableListOf() }
                        val sameDaySame = list.any { it.first == code && isSameDay(it.second, sms.timestamp) }
                        if (!sameDaySame) list.add(Triple(code, sms.timestamp, sms.id))
                    }
                }
                val ordered = grouped.map { (addr, codes) ->
                    val effectiveCodes = codes.filterNot { triple -> completedIds.contains(triple.third) }
                    addr to effectiveCodes
                }.sortedByDescending { it.second.size }

                total = ordered.sumOf { it.second.size }
                fun fill2(idx: Int, setAddr: (String)->Unit, setCodes: (String)->Unit) {
                    val item = ordered.getOrNull(idx)
                    if (item != null && item.second.isNotEmpty()) {
                        setAddr(item.first + "（${item.second.size}）")
                        setCodes(item.second.joinToString("\n") { it.first })
                    } else {
                        setAddr("")
                        setCodes("")
                    }
                }
                fill2(0, { address1 = it }, { codeList1 = it })
                fill2(1, { address2 = it }, { codeList2 = it })
                fill2(2, { address3 = it }, { codeList3 = it })
                fill2(3, { address4 = it }, { codeList4 = it })
                fill2(4, { address5 = it }, { codeList5 = it })
                fill2(5, { address6 = it }, { codeList6 = it })
            }


            // 构建 RemoteViews 对象
            val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setTextViewText(R.id.parcel_num, total.toString())
                setTextViewText(R.id.widget_address1, address1)
                setTextViewText(R.id.widget_codes1, codeList1)

                setTextViewText(R.id.widget_address2, address2)
                setTextViewText(R.id.widget_codes2, codeList2)
                setTextViewText(R.id.widget_address3, address3)
                setTextViewText(R.id.widget_codes3, codeList3)

                setTextViewText(R.id.widget_address4, address4)
                setTextViewText(R.id.widget_codes4, codeList4)
                setTextViewText(R.id.widget_address5, address5)
                setTextViewText(R.id.widget_codes5, codeList5)
                setTextViewText(R.id.widget_address6, address6)
                setTextViewText(R.id.widget_codes6, codeList6)

                // 设置点击意图
                val intent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                setOnClickPendingIntent(
                    R.id.widget_container,
                    PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }

            // 更新 App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
