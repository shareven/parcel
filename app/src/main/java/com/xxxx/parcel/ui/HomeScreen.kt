package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.util.PermissionUtil
import com.xxxx.parcel.viewmodel.ParcelViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.xxxx.parcel.widget.ParcelWidget
import android.appwidget.AppWidgetManager

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    onCallBack: () -> Unit
) {


    var hasPermission by remember { mutableStateOf(PermissionUtil.hasSmsPermissions(context)) }
    LaunchedEffect(hasPermission) {
        hasPermission = PermissionUtil.hasSmsPermissions(context)
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
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
    var selectedTimeFilterIndex by remember { mutableStateOf(0) }

    val succssData by viewModel.successSmsData.collectAsState()
    val failedData by viewModel.failedMessages.collectAsState()
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
                            text = succssData.size.toString(),
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
                    
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        onClick = { navController.navigate("about") },
                    ) {
                        Text(text = "关于")
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
            if (hasPermission) List(viewModel) else
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
                                selectedTimeFilterIndex = index
                                viewModel.setTimeFilter(index)
                                // 刷新 AppWidget（不传递 appWidgetId 以更新所有实例）
        ParcelWidget.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
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
fun List(viewModel: ParcelViewModel) {
    val parcelsData by viewModel.parcelsData.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal =16.dp)
            ,
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
                Text(
                    text = result.address + "（${result.codes.size}）",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        result.codes.forEach { code ->
                            Box(modifier = Modifier.padding(6.dp)) {
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)

                                )
                            }
                        }


                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

        }
    }
}

