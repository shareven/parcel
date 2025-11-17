package com.xxxx.parcel.ui

import android.content.Intent
import android.provider.Settings
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.quicksettings.TileService
import com.xxxx.parcel.service.ParcelNotificationListenerService
import com.xxxx.parcel.service.NotificationAccessTileService
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.compose.material3.Switch
import com.xxxx.parcel.util.getMainSwitch
import com.xxxx.parcel.util.setMainSwitch
import com.xxxx.parcel.util.getAppSwitch
import com.xxxx.parcel.util.setAppSwitch
import com.xxxx.parcel.util.getAppTitle
import com.xxxx.parcel.util.setAppTitle
import com.xxxx.parcel.util.getAppTitles
import com.xxxx.parcel.util.setAppTitles
import com.xxxx.parcel.util.ThirdPartyDefaults



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseNotificationScreen(navController: NavController) {
    val context = LocalContext.current

    val pddPackage = ThirdPartyDefaults.PDD_PACKAGE
    val douyinPackage = ThirdPartyDefaults.DOUYIN_PACKAGE
    val xhsPackage = ThirdPartyDefaults.XHS_PACKAGE
    val wechatPackage = ThirdPartyDefaults.WECHAT_PACKAGE

    var pddTitle by remember { mutableStateOf(getAppTitle(context, pddPackage).ifBlank { ThirdPartyDefaults.defaultTitleFor(pddPackage) }) }
    var douyinTitle by remember { mutableStateOf(getAppTitle(context, douyinPackage).ifBlank { ThirdPartyDefaults.defaultTitleFor(douyinPackage) }) }
    var xhsTitle by remember { mutableStateOf(getAppTitle(context, xhsPackage).ifBlank { ThirdPartyDefaults.defaultTitleFor(xhsPackage) }) }
    var wechatTitles by remember { mutableStateOf(getAppTitles(context, wechatPackage, count = 5, defaultFirst = ThirdPartyDefaults.WECHAT_DEFAULT_FIRST)) }

    var mainEnabled by remember { mutableStateOf(getMainSwitch(context)) }
    var hasPermission by remember { mutableStateOf(isNotificationAccessGranted(context)) }
    var batteryUnrestricted by remember { mutableStateOf(isBatteryOptimizationIgnored(context)) }

    var pddEnabled by remember { mutableStateOf(getAppSwitch(context, pddPackage)) }
    var douyinEnabled by remember { mutableStateOf(getAppSwitch(context, douyinPackage)) }
    var xhsEnabled by remember { mutableStateOf(getAppSwitch(context, xhsPackage)) }
    var wechatEnabled by remember { mutableStateOf(getAppSwitch(context, wechatPackage)) }

    LaunchedEffect(Unit) {
        hasPermission = isNotificationAccessGranted(context)
        batteryUnrestricted = isBatteryOptimizationIgnored(context)
    }

    // 页面恢复时重新检测（从系统设置返回后）
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasPermission = isNotificationAccessGranted(context)
                batteryUnrestricted = isBatteryOptimizationIgnored(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 广播接收器已在 MainActivity 全局注册，这里不再重复注册

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("监听第三方app通知") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )

        },
        
    ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                // 主开关与权限引导（简洁原始风格）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "自动从第三方通知获得取件码消息",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = mainEnabled,
                        onCheckedChange = { checked ->
                            mainEnabled = checked
                            setMainSwitch(context, checked)
                            // 切换页面主开关后，立即刷新通知使用权状态与磁贴展示
                            try {
                                // 刷新快速设置磁贴的显示状态
                                TileService.requestListeningState(
                                    context,
                                    ComponentName(context, NotificationAccessTileService::class.java)
                                )
                            } catch (_: Exception) {}

                            if (checked && !hasPermission) {
                                openNotificationAccessSettings(context)
                            } else if (checked && hasPermission) {
                                // 已授权且开启主开关时，主动请求系统重新绑定通知监听服务
                                try {
                                    NotificationListenerService.requestRebind(
                                        ComponentName(context, ParcelNotificationListenerService::class.java)
                                    )
                                } catch (_: Exception) {}
                            }

                            // 重新读取一次授权状态以刷新页面提示
                            hasPermission = isNotificationAccessGranted(context)
                            batteryUnrestricted = isBatteryOptimizationIgnored(context)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "如果还遇到系统杀进程，可以在通知栏添加名为 取件码 的开关，下拉通知便会重启。还可以任务栏锁定app，添加自启动，耗电管理不限制后台；还不行就在收到通知后，手动启动一下app，会自动读取保存取件码通知；还不行就卸载重装app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!hasPermission && mainEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "尚未授予通知访问权限，点击前往设置授权",
                            color = Color(0xFFB00020),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            openNotificationAccessSettings(context)
                        }) {
                            Text("去授权", color = Color(0XFF6200EE))
                        }
                    }
                }

                if (mainEnabled && !batteryUnrestricted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "建议将耗电管理设置为不限制，点击前往设置",
                            color = Color(0xFFB00020),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            openIgnoreBatteryOptimizationSettings(context)
                        }) {
                            Text("去设置", color = Color(0XFF6200EE))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 列表：拼多多、小红书、微信（恢复最初样式）
                val controlsEnabled = mainEnabled && hasPermission
                val sectionAlpha = if (mainEnabled) 1f else 0.5f

                Text(text = "监听应用", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(sectionAlpha)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            AppListenItem(
                                appName = "拼多多",
                                packageName = pddPackage,
                                titleText = pddTitle,
                                checked = pddEnabled,
                                onCheckedChange = { checked ->
                                    if (controlsEnabled) {
                                        pddEnabled = checked
                                        setAppSwitch(context, pddPackage, checked)
                                    }
                                },
                                onTitleChange = { new ->
                                    pddTitle = new
                                    setAppTitle(context, pddPackage, new)
                                },
                                enabled = controlsEnabled
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            AppListenItem(
                                appName = "抖音",
                                packageName = douyinPackage,
                                titleText = douyinTitle,
                                checked = douyinEnabled,
                                onCheckedChange = { checked ->
                                    if (controlsEnabled) {
                                        douyinEnabled = checked
                                        setAppSwitch(context, douyinPackage, checked)
                                    }
                                },
                                onTitleChange = { new ->
                                    douyinTitle = new
                                    setAppTitle(context, douyinPackage, new)
                                },
                                enabled = controlsEnabled
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            AppListenItem(
                                appName = "小红书",
                                packageName = xhsPackage,
                                titleText = xhsTitle,
                                checked = xhsEnabled,
                                onCheckedChange = { checked ->
                                    if (controlsEnabled) {
                                        xhsEnabled = checked
                                        setAppSwitch(context, xhsPackage, checked)
                                    }
                                },
                                onTitleChange = { new ->
                                    xhsTitle = new
                                    setAppTitle(context, xhsPackage, new)
                                },
                                enabled = controlsEnabled
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            AppListenItemMulti(
                                appName = "微信",
                                packageName = wechatPackage,
                                titles = wechatTitles,
                                checked = wechatEnabled,
                                onCheckedChange = { checked ->
                                    if (controlsEnabled) {
                                        wechatEnabled = checked
                                        setAppSwitch(context, wechatPackage, checked)
                                    }
                                },
                                onTitleChangeAt = { index, new ->
                                    val updated = wechatTitles.toMutableList()
                                    if (index in updated.indices) {
                                        updated[index] = new
                                        wechatTitles = updated
                                        setAppTitles(context, wechatPackage, updated)
                                    }
                                },
                                onAddTitle = {
                                    val updated = wechatTitles.toMutableList()
                                    updated.add("")
                                    wechatTitles = updated
                                    setAppTitles(context, wechatPackage, updated)
                                },
                                onRemoveTitleAt = { index ->
                                    val updated = wechatTitles.toMutableList()
                                    if (updated.size > 1 && index in updated.indices) {
                                        updated.removeAt(index)
                                        wechatTitles = updated
                                        setAppTitles(context, wechatPackage, updated)
                                    }
                                },
                                enabled = controlsEnabled
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

        }
    }
}

@Composable
fun AppListenItem(
    appName: String,
    packageName: String,
    titleText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit,
    enabled: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = appName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = titleText,
            onValueChange = { onTitleChange(it) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("监听标题") },
            singleLine = true,
            placeholder = { Text("请输入要匹配的通知标题") }
        )
    }
}

@Composable
fun AppListenItemMulti(
    appName: String,
    packageName: String,
    titles: List<String>,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTitleChangeAt: (Int, String) -> Unit,
    onAddTitle: () -> Unit,
    onRemoveTitleAt: (Int) -> Unit,
    enabled: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = appName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
        Spacer(modifier = Modifier.height(8.dp))
        titles.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { onTitleChangeAt(index, it) },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("会话名 ${index + 1}") },
                singleLine = true,
                placeholder = { Text("会话名，如好友名或群名称") },
                trailingIcon = {
                    IconButton(
                        onClick = { onRemoveTitleAt(index) },
                        enabled = enabled && titles.size > 1
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除会话名")
                    }
                }
            )
            if (index != titles.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onAddTitle, enabled = enabled) {
                Icon(Icons.Filled.Add, contentDescription = "添加")
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加会话名")
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "添加好友名或群名称，只保存解析成功的消息，如无法保存，请先创建自定义规则",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


fun isNotificationAccessGranted(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (flat.isNullOrBlank()) return false
    val full = ComponentName(context, ParcelNotificationListenerService::class.java).flattenToString()
    val short = "${context.packageName}/.service.ParcelNotificationListenerService"
    return flat.split(":").any { it == full || it == short }
}

fun openNotificationAccessSettings(context: Context) {
    val cn = ComponentName(context, ParcelNotificationListenerService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
            putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, cn.flattenToString())
        }
        context.startActivity(intent)
    } else {
        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}

fun isBatteryOptimizationIgnored(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

fun openIgnoreBatteryOptimizationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    context.startActivity(intent)
}
