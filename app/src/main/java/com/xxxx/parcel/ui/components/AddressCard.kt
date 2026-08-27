package com.xxxx.parcel.ui.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.model.ParcelData
import com.xxxx.parcel.model.SmsData
import com.xxxx.parcel.util.addCompletedIds
import com.xxxx.parcel.util.formatPickupCode
import com.xxxx.parcel.util.removeCompletedId
import com.xxxx.parcel.viewmodel.ParcelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddressCard(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    updateAllWidget: () -> Unit,
    showCompleted: Boolean,
    showCodeTime: Boolean,
    showCompartment: Boolean = true,
    parcelData: ParcelData,
    expandedStates: androidx.compose.runtime.MutableState<MutableMap<String, Boolean>>,
    isExpanded: Boolean,
    preferLockerAddress: Boolean,
    isSeniorMode: Boolean,
    isTimeSort: Boolean = false,
    codeNotes: Map<String, String> = emptyMap(),
    onLongPressCode: (SmsData) -> Unit = {},
) {
    val isAllCompleted = parcelData.smsDataList.find { !it.isCompleted } == null
    // 时间排序：取件码按短信时间倒序；默认排序：有柜号的靠前、柜号升序、再按取件码
    val displaySmsDataList = if (isTimeSort) {
        parcelData.smsDataList.sortedByDescending { it.sms.timestamp }
    } else {
        parcelData.smsDataList
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 0.dp),
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
                IconButton(
                    modifier = Modifier.size(if (isSeniorMode) 48.dp else 32.dp),
                    onClick = {
                        navController.navigate("add_custom_sms/${parcelData.address}")
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
                    text = "${parcelData.address}（${parcelData.num}）",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            expandedStates.value = expandedStates.value.toMutableMap().apply {
                                put(parcelData.address, !isExpanded)
                            }
                        }
                )
            }

            IconButton(
                modifier = Modifier.size(if (isSeniorMode) 48.dp else 36.dp),
                onClick = {
                    if (parcelData.num > 0) {
                        val smsList = parcelData.smsDataList
                            .filterNot { it.isCompleted }
                            .map { it.sms }
                        addCompletedIds(context, viewModel, smsList)
                        updateAllWidget()
                    }
                },
                enabled = parcelData.num > 0
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "标记取件",
                    tint = if (parcelData.num > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = if (showCompleted) (isExpanded || !isAllCompleted) else (!isAllCompleted),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        displaySmsDataList.forEach { smsData ->
                            if (!(((!isExpanded) && smsData.isCompleted) || ((!showCompleted) && smsData.isCompleted))) {

                                Box(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatPickupCode(smsData.code),
                                            textDecoration = if (smsData.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (smsData.isCompleted) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
                                            style = if (isSeniorMode) MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ) else MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .combinedClickable(
                                                    onClick = {
                                                        if (smsData.isCompleted) {
                                                            removeCompletedId(
                                                                context,
                                                                viewModel,
                                                                smsData.sms
                                                            )
                                                        } else {
                                                            addCompletedIds(
                                                                context,
                                                                viewModel,
                                                                listOf(smsData.sms)
                                                            )
                                                        }
                                                        updateAllWidget()
                                                    },
                                                    onLongClick = { onLongPressCode(smsData) }
                                                )
                                                .padding(0.dp)
                                        )
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (!preferLockerAddress && smsData.lockerNumber.isNotEmpty()) {
                                                Text(
                                                    text = if (showCompartment && smsData.compartmentNumber.isNotEmpty())
                                                        "${smsData.lockerNumber}号柜 ${smsData.compartmentNumber}格口"
                                                    else "${smsData.lockerNumber}号柜",
                                                    style = if (isSeniorMode) MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ) else MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = if (smsData.isCompleted) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else if (showCompartment && smsData.compartmentNumber.isNotEmpty()) {
                                                Text(
                                                    text = "${smsData.compartmentNumber}格口",
                                                    style = if (isSeniorMode) MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ) else MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = if (smsData.isCompleted) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            val codeNote = codeNotes[smsData.id] ?: ""
                                            if (codeNote.isNotEmpty()) {
                                                Text(
                                                    text = codeNote,
                                                    style = if (isSeniorMode) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            if (showCodeTime) {
                                                val sdf = remember(isSeniorMode) {
                                                    SimpleDateFormat(
                                                        if (isSeniorMode) "MM-dd" else "yyyy-MM-dd HH:mm",
                                                        Locale.getDefault()
                                                    )
                                                }
                                                Text(
                                                    text = sdf.format(Date(smsData.sms.timestamp)),
                                                    style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSecondary
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
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
