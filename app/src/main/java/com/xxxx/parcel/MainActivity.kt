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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
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
import com.xxxx.parcel.ui.theme.ParcelTheme
import com.xxxx.parcel.util.PermissionUtil
import com.xxxx.parcel.util.PermissionUtil.showMiuiPermissionExplanationDialog
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.SmsUtil
import com.xxxx.parcel.util.getAllSaveData
import com.xxxx.parcel.util.getCustomSmsList
import com.xxxx.parcel.viewmodel.ParcelViewModel
import com.xxxx.parcel.widget.ParcelWidget
import com.xxxx.parcel.widget.ParcelWidgetLarge
import com.xxxx.parcel.widget.ParcelWidgetLargeMiui
import com.xxxx.parcel.widget.ParcelWidgetMiui
import java.net.URLDecoder


class MainActivity : ComponentActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var smsContentObserver: ContentObserver
    private lateinit var appDetailsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    val context = this
    val smsParser = SmsParser()
    val viewModel = ParcelViewModel(smsParser)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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
                guideToSettings = { guideToSettings() },
                readAndParseSms = { readAndParseSms() },
                updateAllWidget = { updateAllWidget() },
            )
        }
    }


    private fun startSmsDeletionMonitoring() {
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
        val context = applicationContext
        val daysFilter = viewModel.timeFilterIndex.value
        val smsList = SmsUtil.readSmsByTimeFilter(context, daysFilter)
        val customSmsList = getCustomSmsList(context)

        viewModel.getAllMessageWithCustom(smsList, customSmsList)

        updateAllWidget()
    }


    private fun init() {
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
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                ),
                1
            )
            if (PermissionUtil.isMIUI()) {
                //小米手机 显示引导弹窗后调用 requestMiuiSmsPermission()
                showMiuiPermissionExplanationDialog(context)
            }
            if (PermissionUtil.hasSmsPermissions(this)) {
                readAndParseSms()
                startSmsDeletionMonitoring()
            }

        } else {
            // 权限已授予，读取短信
            readAndParseSms()
            startSmsDeletionMonitoring()
        }
        setContent {
            App(
                context,
                viewModel,
                guideToSettings = { guideToSettings() },
                readAndParseSms = { readAndParseSms() },
                updateAllWidget = { updateAllWidget() },
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            init()

        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
                init()
            } else {
                guideToSettings()
            }
        }

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
        contentResolver.unregisterContentObserver(smsContentObserver)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    context: Context,
    viewModel: ParcelViewModel,
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
            }
        }
    }
}