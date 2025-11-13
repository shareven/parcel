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

class ParcelWidgetXL : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {

        if ("miui.appwidget.action.APPWIDGET_UPDATE".equals(intent.getAction()) ||
            "com.xxxx.parcel.CUSTOM_SMS_ADDED".equals(intent.getAction())) {

            // 获取 ParcelViewModel 实例
            val viewModel = (context.applicationContext as? ViewModelStoreOwner)?.let {
                ViewModelProvider(it)[ParcelViewModel::class.java]
            }
            ParcelWidget.updateAllByProvider(context, ParcelWidgetXL::class.java, viewModel)

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
                    android.content.ComponentName(context, ParcelWidgetXL::class.java)
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
            ParcelWidget.updateSingleAppWidget(context, appWidgetManager, appWidgetId, viewModel)
        }
        }
}