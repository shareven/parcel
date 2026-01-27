package com.xxxx.parcel

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xxxx.parcel.ui.AboutScreen
import com.xxxx.parcel.ui.AddCustomSmsScreen
import com.xxxx.parcel.ui.AddRuleScreen
import com.xxxx.parcel.ui.FailSmsScreen
import com.xxxx.parcel.ui.HomeScreen
import com.xxxx.parcel.ui.RulesScreen
import com.xxxx.parcel.ui.SuccessSmsScreen
import com.xxxx.parcel.ui.UseNotificationScreen
import com.xxxx.parcel.ui.theme.ParcelTheme
import com.xxxx.parcel.util.PermissionUtil
import com.xxxx.parcel.util.PermissionUtil.showMiuiPermissionExplanationDialog
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.SmsProcessor
import com.xxxx.parcel.util.SmsUtil
import com.xxxx.parcel.util.getAllSaveData
import com.xxxx.parcel.util.getMainSwitch
import com.xxxx.parcel.viewmodel.ParcelViewModel
import com.xxxx.parcel.widget.ParcelWidget
import com.xxxx.parcel.widget.ParcelWidgetLarge
import com.xxxx.parcel.widget.ParcelWidgetLargeMiui
import com.xxxx.parcel.widget.ParcelWidgetMiui
import java.net.URLDecoder
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.xxxx.parcel.widget.ParcelWidgetXL
import com.xxxx.parcel.service.ParcelNotificationListenerService
import com.xxxx.parcel.ui.LogScreen
import com.xxxx.parcel.util.addLog


class MainActivity : ComponentActivity() {
    private val hasPermissionState = mutableStateOf(false)
    private lateinit var smsContentObserver: ContentObserver
    private lateinit var appDetailsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
    private lateinit var viewModel: ParcelViewModel
    val context = this
    val smsParser = SmsParser()
    private var customSmsReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasPermissionState.value = PermissionUtil.hasSmsPermissions(this)

        viewModel = ParcelViewModel(smsParser, applicationContext)

        // 注册权限请求 Launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                hasPermissionState.value = true
                readAndParseSms()
                startSmsDeletionMonitoring()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
                    readAndParseSms()
                    startSmsDeletionMonitoring()
                } else {
                    guideToSettings()
                }
            }
        }

        // 注册 ActivityResultLauncher
        appDetailsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                init()

            }

        getAllSaveData(this, viewModel)
        init()

        setContent {
            App(
                context,
                viewModel,
                hasPermissionState.value,
                guideToSettings = { guideToSettings() },
                readAndParseSms = { readAndParseSms() },
                updateAllWidget = { updateAllWidget() },
            )
        }
    }


    private fun startSmsDeletionMonitoring() {
        try {
            smsContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    readAndParseSms()
                }
            }
            contentResolver.registerContentObserver(
                "content://sms".toUri(),
                true,
                smsContentObserver
            )
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Failed to register SMS observer: ${e.message}")
        }
    }

    fun updateAllWidget() {
        // 刷新 AppWidget（不传递 appWidgetId 以更新所有实例）
        ParcelWidget.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
        ParcelWidgetLarge.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
        ParcelWidgetXL.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
        ParcelWidgetMiui.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
        ParcelWidgetLargeMiui.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
    }

    fun readAndParseSms() {
        lifecycleScope.launch {
            try {
                val context = applicationContext
                val daysFilter = viewModel.timeFilterIndex.value
                val (smsList, customSmsList) = SmsProcessor.loadMessages(context, daysFilter)

                viewModel.getAllMessageWithCustom(smsList, customSmsList)

                updateAllWidget()
            } catch (e: SecurityException) {
                Log.e("MainActivity", "Failed to read SMS: ${e.message}")
            }
        }
    }


    private fun init() {
        hasPermissionState.value = PermissionUtil.hasSmsPermissions(this)
        if (PermissionUtil.isMIUI()) {
            //小米手机 MIUI widget启用
            val component = ComponentName(context, ParcelWidgetMiui::class.java)
            context.packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            val componentLarge = ComponentName(context, ParcelWidgetLargeMiui::class.java)
            context.packageManager.setComponentEnabledSetting(
                componentLarge,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        // 检查并请求短信权限
        if (!PermissionUtil.hasSmsPermissions(this)) {
            if (PermissionUtil.isMIUI()) {
                //小米手机 显示引导弹窗后调用权限请求
                showMiuiPermissionExplanationDialog(context)
            }
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                )
            )
        } else {
            // 权限已授予，读取短信
            readAndParseSms()
            startSmsDeletionMonitoring()
        }
        // 注册接收“自定义短信已添加”的广播，统一触发 UI 刷新
        registerCustomSmsAddedReceiver()

        // 应用启动后尝试重新绑定通知监听服务，避免重启后不工作
        rebindNotificationListenerIfNeeded()
    }

    private fun registerCustomSmsAddedReceiver() {
        val action = "com.xxxx.parcel.CUSTOM_SMS_ADDED"
        customSmsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == action) {
                    readAndParseSms()
                }
            }
        }
        val filter = IntentFilter(action)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(customSmsReceiver, filter, RECEIVER_NOT_EXPORTED)
        }
    }

    private fun rebindNotificationListenerIfNeeded() {
        try {
            val hasAccess = hasNotificationAccess(this)
            val mainEnabled = getMainSwitch(this)
            if (hasAccess && mainEnabled) {
                NotificationListenerService.requestRebind(
                    ComponentName(this, ParcelNotificationListenerService::class.java)
                )
                Log.d("MainActivity", "Requested rebind on app start")
            } else {
                Log.d("MainActivity", "Skip rebind: hasAccess=$hasAccess mainEnabled=$mainEnabled")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Request rebind failed: ${e.message}")
            addLog(this, "请求重新绑定通知监听服务失败: ${e.message}")
        }
    }

    private fun hasNotificationAccess(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrBlank()) return false
        val full = ComponentName(context, ParcelNotificationListenerService::class.java).flattenToString()
        val short = "${context.packageName}/.service.ParcelNotificationListenerService"
        return flat.split(":").any { it == full || it == short }
    }

    private fun guideToSettings() {
        val uri = Uri.fromParts("package", "com.xxxx.parcel", null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = uri
        }
        appDetailsLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消短信删除监控
        try {
            contentResolver.unregisterContentObserver(smsContentObserver)
        } catch (_: Exception) { }
        // 取消自定义短信广播接收
        customSmsReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) { }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    context: Context,
    viewModel: ParcelViewModel,
    hasPermission: Boolean,
    guideToSettings: () -> Unit,
    readAndParseSms: () -> Unit,
    updateAllWidget: () -> Unit,
) {
    ParcelTheme {
        val navController = rememberNavController()
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        context,
                        viewModel,
                        navController,
                        hasPermission,
                        onCallBack = { guideToSettings() },
                        updateAllWidget,
                    )
                }
                composable(
                    route = "add_custom_sms/{address}",
                    arguments = listOf(
                        navArgument("address") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val address = backStackEntry.arguments?.getString("address") ?: ""
                    AddCustomSmsScreen(
                        context,
                        viewModel,
                        navController,
                        URLDecoder.decode(address, "UTF-8"),
                        onCallback = { readAndParseSms() }
                    )
                }
                composable(
                    route = "add_rule?message={message}",
                    arguments = listOf(
                        navArgument("message") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val message = backStackEntry.arguments?.getString("message") ?: ""
                    AddRuleScreen(
                        context,
                        viewModel,
                        navController,
                        URLDecoder.decode(message, "UTF-8"),
                        onCallback = { readAndParseSms() })
                }
                composable("rules") {
                    RulesScreen(
                        context,
                        viewModel,
                        navController,
                        onCallback = { readAndParseSms() })
                }
                composable("fail_sms") {
                    FailSmsScreen(viewModel, navController, readAndParseSms)
                }
                composable("success_sms") {
                    SuccessSmsScreen(viewModel, navController, readAndParseSms)
                }
                composable("about") {
                    AboutScreen(navController)
                }
                composable("use_notification") {
                    UseNotificationScreen(navController)
                }
                composable("logs") {
                    LogScreen(navController)
                }
            }
        }
    }
}
