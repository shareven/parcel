package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import com.xxxx.parcel.R
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.MainActivity
import com.xxxx.parcel.util.PermissionUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.xxxx.parcel.util.addCompletedIds
import com.xxxx.parcel.util.removeCompletedId
import com.xxxx.parcel.util.saveIndex
import com.xxxx.parcel.viewmodel.ParcelViewModel
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.os.Build

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    onCallBack: () -> Unit,
    updateAllWidget: () -> Unit
) {


    var hasPermission by remember { mutableStateOf(PermissionUtil.hasSmsPermissions(context)) }
    LaunchedEffect(hasPermission) {
        hasPermission = PermissionUtil.hasSmsPermissions(context)
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(getShowCompleted(context)) }
    var showCodeTime by remember { mutableStateOf(getShowCodeTime(context)) }
    val timeFilterOptions = listOf(
        "全部",
        "今天",
        "近2天",
        "近3天",
        "近4天",
        "近5天",
        "近6天",
        "近7天",
        "近8天",
        "近9天",
        "近10天",
    )

    val selectedTimeFilterIndex by viewModel.timeFilterIndex.collectAsState()
    val failedData by viewModel.failedMessages.collectAsState()
    val successData by viewModel.successSmsData.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(
                        onClick = { showBottomSheet = true },

                        ) {
                        Text(text = timeFilterOptions[selectedTimeFilterIndex])
                    }
                },
                actions = {

                    Button(
                        contentPadding = PaddingValues(2.dp),
                        colors = ButtonColors(
                            containerColor = Color(0xFF25AF22),
                            contentColor = Color.White,
                            disabledContentColor = Color.DarkGray,
                            disabledContainerColor = Color.LightGray
                        ),
                        onClick = { navController.navigate("success_sms") },
                    ) {
                        Text(
                            text = successData.size.toString(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        colors = ButtonColors(
                            containerColor = Color(0xFFAB1A65),
                            contentColor = Color.White,
                            disabledContentColor = Color.DarkGray,
                            disabledContainerColor = Color.LightGray
                        ),
                        onClick = { navController.navigate("fail_sms") },
                    ) {
                        Text(
                            text = failedData.size.toString(),
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.width(8.dp))
                    // 顶栏菜单：规则列表 / 监听第三方app通知 / 关于
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "菜单")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(if (showCompleted) "隐藏已取件的码" else "显示已取件的码") },
                                    onClick = {
                                        showMenu = false
                                        val new = !showCompleted
                                        saveShowCompleted(context, new)
                                        showCompleted = new
                                    }
                                )
                            DropdownMenuItem(
                                text = { Text(if (showCodeTime) "隐藏时间" else "显示时间") },
                                onClick = {
                                    showMenu = false
                                    val new = !showCodeTime
                                    saveShowCodeTime(context, new)
                                    showCodeTime = new
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("添加自定义取件短信") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("add_custom_sms/ ")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("规则列表") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("rules")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("查看日志") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("logs")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("监听第三方app通知") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("use_notification")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("淘宝身份码") },
                                onClick = {
                                    showMenu = false
                                    openTaobaoIdentityEntry(context)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("拼多多身份码") },
                                onClick = {
                                    showMenu = false
                                    openPddIdentityEntry(context)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("关于") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("about")
                                }
                            )
                        }
                    }

                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasPermission) List(context, viewModel, navController, updateAllWidget, showCompleted, showCodeTime) else
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { onCallBack() }) {
                        Text("获取短信权限")
                    }
                }
        }
        if (showBottomSheet) ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                timeFilterOptions.forEachIndexed { index, option ->
                    Text(
                        text = option,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable {
                                saveIndex(context, index)
                                viewModel.setTimeFilterIndex(index)
                                // 重新根据过滤时间读取短信
                                (context as MainActivity).readAndParseSms()
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }
                    )
                }
            }
        }
    }

}


@Composable
fun List(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    updateAllWidget: () -> Unit,
    showCompleted: Boolean,
    showCodeTime: Boolean
) {
    val parcelsData by viewModel.parcelsData.collectAsState()
    val expandedStates = remember { mutableStateOf(mutableMapOf<String, Boolean>()) }

    if (parcelsData.isEmpty()) Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 空状态图标
        Icon(
            painter = painterResource(id = R.drawable.ic_empty_package),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 主标题
        Text(
            text = "暂无取件码",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )



        Spacer(modifier = Modifier.height(32.dp))

        // 添加自定义短信按钮
        Button(
            onClick = {
                navController.navigate("add_custom_sms/ ")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加自定义取件短信",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 提示文本
        Text(
            text = "您可以手动添加取件短信或取件码",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
    else
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(parcelsData) { result ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 8.dp),
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // 添加+按钮
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = {
                                    navController.navigate("add_custom_sms/${result.address}")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "添加自定义取件码",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${result.address}（${result.num}）",
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val currentExpanded = expandedStates.value[result.address] ?: true
                                        expandedStates.value = expandedStates.value.toMutableMap().apply {
                                            put(result.address, !currentExpanded)
                                        }
                                    }
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = {
                                if (result.num > 0) {
                                    val ids = result.smsDataList
                                        .filterNot { it.isCompleted }
                                        .map { it.id }
                                    addCompletedIds(context, viewModel, ids)
                                    updateAllWidget()
                                }
                            },
                            enabled = result.num > 0
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "标记取件",
                                tint = if (result.num > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    val isExpanded = expandedStates.value[result.address] ?: true
                    val isAllCompleted = result.smsDataList.find({ !it.isCompleted }) == null
                    androidx.compose.animation.AnimatedVisibility(
                        visible = if (showCompleted) (isExpanded || !isAllCompleted) else (!isAllCompleted),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    result.smsDataList.forEach { smsData ->
                                        if (((!isExpanded) && smsData.isCompleted) || ((!showCompleted) && smsData.isCompleted)) {
                                            // skip displaying this completed smsData based on expand state or global setting
                                        } else {
                                            Box(modifier = Modifier.padding(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = smsData.code,
                                                    textDecoration = if (smsData.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                                    color = if (smsData.isCompleted) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier
                                                        .clickable {
                                                            if (smsData.isCompleted) {
                                                                removeCompletedId(
                                                                    context,
                                                                    viewModel,
                                                                    smsData.id
                                                                )
                                                            } else {
                                                                addCompletedIds(
                                                                    context,
                                                                    viewModel,
                                                                    listOf(smsData.id)
                                                                )
                                                            }
                                                            updateAllWidget()
                                                        }
                                                )
                                                if (showCodeTime) {
                                                    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
                                                    Text(
                                                        text = sdf.format(Date(smsData.sms.timestamp)),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

            }
        }
}

private fun openTaobaoIdentityEntry(context: Context) {
    val pkg = "com.taobao.taobao"
    val lastmile = "https://pages-fast.m.taobao.com/wow/z/uniapp/1100333/last-mile-fe/m-end-school-tab/home"
   val candidates = listOf(
        "tbopen://m.taobao.com/tbopen/index.html?h5Url=" + Uri.encode(lastmile),
    )
    for (u in candidates) {
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(u))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        } catch (_: Exception) {}
    }
    try {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(lastmile))
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.setClassName(pkg, "com.taobao.browser.BrowserActivity")
        context.startActivity(i)
        return
    } catch (_: Exception) {}


}

private fun openPddIdentityEntry(context: Context) {
    val pkg = "com.xunmeng.pinduoduo"
    val schemes = listOf(
        "pinduoduo://com.xunmeng.pinduoduo/mdkd/package",
        "pinduoduo://com.xunmeng.pinduoduo/",
        "pinduoduo://"
    )
    for (u in schemes) {
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(u))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        } catch (_: Exception) {}
    }
    try {
        val i = context.packageManager.getLaunchIntentForPackage(pkg)
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        }
    } catch (_: Exception) {}
}

private fun saveShowCompleted(context: Context, show: Boolean) {
    try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("show_completed_codes", show).apply()
    } catch (_: Exception) {
    }
}

private fun getShowCompleted(context: Context): Boolean {
    return try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.getBoolean("show_completed_codes", true)
    } catch (_: Exception) {
        true
    }
}

private fun saveShowCodeTime(context: Context, show: Boolean) {
    try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("show_code_time", show).apply()
    } catch (_: Exception) {
    }
}

private fun getShowCodeTime(context: Context): Boolean {
    return try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.getBoolean("show_code_time", true)
    } catch (_: Exception) {
        true
    }
}
