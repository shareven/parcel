package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.xxxx.parcel.MainActivity
import com.xxxx.parcel.ui.components.HomeTopBar
import com.xxxx.parcel.ui.components.ParcelList
import com.xxxx.parcel.ui.components.TimeFilterSheet
import com.xxxx.parcel.ui.components.timeFilterOptions
import com.xxxx.parcel.util.getHorizontalLayout
import com.xxxx.parcel.util.getPreferLockerAddress
import com.xxxx.parcel.util.getShowCodeTime
import com.xxxx.parcel.util.getShowCompartment
import com.xxxx.parcel.util.getShowCompleted
import com.xxxx.parcel.util.getTimeSort
import com.xxxx.parcel.util.saveHorizontalLayout
import com.xxxx.parcel.util.saveIndex
import com.xxxx.parcel.util.savePreferLockerAddress
import com.xxxx.parcel.util.saveShowCodeTime
import com.xxxx.parcel.util.saveShowCompartment
import com.xxxx.parcel.util.saveShowCompleted
import com.xxxx.parcel.util.saveTimeSort
import com.xxxx.parcel.viewmodel.ParcelViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    hasPermission: Boolean,
    onCallBack: () -> Unit,
    updateAllWidget: () -> Unit,
    isSeniorMode: Boolean,
    onSeniorModeChanged: (Boolean) -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(getShowCompleted(context)) }
    var showCodeTime by remember { mutableStateOf(getShowCodeTime(context)) }
    var showCompartment by remember { mutableStateOf(getShowCompartment(context)) }
    var isHorizontalLayout by remember { mutableStateOf(getHorizontalLayout(context)) }
    var isTimeSort by remember { mutableStateOf(getTimeSort(context)) }
    var preferLockerAddress by remember { mutableStateOf(getPreferLockerAddress(context)) }

    val selectedTimeFilterIndex by viewModel.timeFilterIndex.collectAsState()
    val failedData by viewModel.failedMessages.collectAsState()
    val successData by viewModel.successSmsData.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HomeTopBar(
                context = context,
                navController = navController,
                isSeniorMode = isSeniorMode,
                isTimeSort = isTimeSort,
                preferLockerAddress = preferLockerAddress,
                isHorizontalLayout = isHorizontalLayout,
                showCompleted = showCompleted,
                showCodeTime = showCodeTime,
                showCompartment = showCompartment,
                currentFilterLabel = timeFilterOptions[selectedTimeFilterIndex],
                successCount = successData.size,
                failedCount = failedData.size,
                onFilterClick = { showBottomSheet = true },
                onSuccessCountClick = { navController.navigate("success_sms") },
                onFailedCountClick = { navController.navigate("fail_sms") },
                onToggleTimeSort = {
                    val new = !isTimeSort
                    saveTimeSort(context, new)
                    isTimeSort = new
                },
                onTogglePreferLockerAddress = {
                    val new = !preferLockerAddress
                    savePreferLockerAddress(context, new)
                    preferLockerAddress = new
                    viewModel.setPreferLockerAddress(new)
                    (context as MainActivity).readAndParseSms()
                },
                onToggleHorizontalLayout = {
                    val new = !isHorizontalLayout
                    saveHorizontalLayout(context, new)
                    isHorizontalLayout = new
                },
                onToggleShowCompleted = {
                    val new = !showCompleted
                    saveShowCompleted(context, new)
                    showCompleted = new
                },
                onToggleShowCodeTime = {
                    val new = !showCodeTime
                    saveShowCodeTime(context, new)
                    showCodeTime = new
                },
                onToggleShowCompartment = {
                    val new = !showCompartment
                    saveShowCompartment(context, new)
                    showCompartment = new
                },
                onSeniorModeChanged = onSeniorModeChanged,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasPermission) ParcelList(
                context = context,
                viewModel = viewModel,
                navController = navController,
                updateAllWidget = updateAllWidget,
                showCompleted = showCompleted,
                showCodeTime = showCodeTime,
                showCompartment = showCompartment,
                isHorizontalLayout = isHorizontalLayout,
                preferLockerAddress = preferLockerAddress,
                isSeniorMode = isSeniorMode,
                isTimeSort = isTimeSort
            ) else
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
        if (showBottomSheet) TimeFilterSheet(
            isSeniorMode = isSeniorMode,
            onOptionSelected = { index ->
                saveIndex(context, index)
                viewModel.setTimeFilterIndex(index)
                // 重新根据过滤时间读取短信
                (context as MainActivity).readAndParseSms()
            },
            onDismiss = { showBottomSheet = false }
        )
    }

}
