package com.xxxx.parcel.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.xxxx.parcel.MainActivity
import com.xxxx.parcel.R
import com.xxxx.parcel.viewmodel.ParcelViewModel

class ParcelWidgetLarge : AppWidgetProvider() {
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
                    android.content.ComponentName(context, ParcelWidgetLarge::class.java)
                )
                for (id in ids) {
                    updateSingleAppWidget(context, manager, id, viewModel)
                }
            } else {
                updateSingleAppWidget(context, appWidgetManager, appWidgetId, viewModel)
            }
        }

        private fun updateSingleAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            viewModel: ParcelViewModel?
        ) {
            // 从 ViewModel 获取最新的取件码信息
            var total =  0
            viewModel?.parcelsData?.value?.forEach{
                total+= it.num
            }

            val latestMessage = viewModel?.parcelsData?.value?.firstOrNull()
            var address1 = latestMessage?.address ?: ""
            var codeList1 = ""
            if (latestMessage != null&&latestMessage.num>0) {
                codeList1 = latestMessage.smsDataList.filter{!it.isCompleted}.map{it.code}.joinToString(separator = "\n")
            }

            val latestMessage2 = if (viewModel?.parcelsData?.value?.size!! >= 2) viewModel.parcelsData.value!![1] else null
            var address2 = latestMessage2?.address ?: ""
            var codeList2 = ""
            if (latestMessage2 != null&&latestMessage2.num>0) {
                codeList2 = latestMessage2.smsDataList.filter{!it.isCompleted}.map{it.code}.joinToString(separator = "\n")
            }

            val latestMessage3 = if (viewModel?.parcelsData?.value?.size!! >= 3) viewModel.parcelsData.value!![2] else null
            var address3 = latestMessage3?.address ?: ""
            var codeList3 = ""
            if (latestMessage3 != null&&latestMessage3.num>0) {
                codeList3 = latestMessage3.smsDataList.filter{!it.isCompleted}.map{it.code}.joinToString(separator = "\n")
            }

            // 构建 RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // 设置取件数量
            views.setTextViewText(R.id.parcel_num, total.toString())

            // 设置地址和取件码
            views.setTextViewText(R.id.widget_address1, address1)
            views.setTextViewText(R.id.widget_codes1, codeList1)

            views.setTextViewText(R.id.widget_address2, address2)
            views.setTextViewText(R.id.widget_codes2, codeList2)

            views.setTextViewText(R.id.widget_address3, address3)
            views.setTextViewText(R.id.widget_codes3, codeList3)

            // 设置点击事件
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // 通知 AppWidgetManager 执行更新
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}