package com.xxxx.parcel.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.SmsUtil
import com.xxxx.parcel.util.getAllTags

@Composable
private fun getAllAddresses(): List<String> {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(context) {
        try {
            val allSms = SmsUtil.readAllSms(context)
            val parser = SmsParser()
            allSms.mapNotNull { sms ->
                val result = parser.parseSms(sms.body)
                if (result.success) result.address else null
            }.distinct().sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagDialog(
    title: String = "设置归类标签",
    currentAddress: String? = null,
    currentTag: String = "",
    existingMappings: Map<String, String> = emptyMap(),
    selectedAddresses: List<String> = listOf(currentAddress ?: ""),
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    var tagName by remember { mutableStateOf(currentTag) }
    val availableTags = getAllTags(androidx.compose.ui.platform.LocalContext.current)
    val isTagChanged = currentTag.isNotBlank() && tagName != currentTag
    val showRemoveButton = currentTag.isNotBlank() && onRemove != null

    val conflictingAddresses = selectedAddresses.filter { addr ->
        val existingTag = existingMappings[addr]
        existingTag != null && existingTag != tagName && existingTag != currentTag
    }
    val showWarning = conflictingAddresses.isNotEmpty()

    val allAddresses = getAllAddresses()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),

                    ) {
                    LazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                       item{
                           Row(
                               modifier = Modifier.fillMaxWidth(),
                               horizontalArrangement = Arrangement.SpaceBetween,
                               verticalAlignment = Alignment.CenterVertically
                           ) {
                               Text(
                                   text = when {
                                       showWarning -> "确认修改标签"
                                       isTagChanged -> "确认修改标签"
                                       else -> title
                                   },
                                   style = MaterialTheme.typography.titleLarge,
                                   fontWeight = FontWeight.Bold
                               )
                               IconButton(onClick = onDismiss) {
                                   Icon(Icons.Default.Close, contentDescription = "关闭")
                               }
                           }
                       }

                       item{
                           Spacer(modifier = Modifier.height(16.dp))
                       }

                       item{
                           when {
                               showWarning -> {
                                   WarningCard(
                                       addresses = conflictingAddresses,
                                       existingMappings = existingMappings
                                   )
                                   Spacer(modifier = Modifier.height(16.dp))
                               }

                               isTagChanged -> {
                                   TagChangeWarningCard(
                                       currentTag = currentTag,
                                       newTag = tagName
                                   )
                                   Spacer(modifier = Modifier.height(12.dp))
                               }
                           }
                       }

                        item{
                            currentAddress?.let { addr ->
                                Surface(
                                    onClick = { tagName = addr },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = addr,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                       item{
                           if (selectedAddresses.size > 1) {
                               Text(
                                   text = "已选择 ${selectedAddresses.size} 个地址",
                                   style = MaterialTheme.typography.bodyMedium,
                                   color = MaterialTheme.colorScheme.onSurface
                               )
                               Spacer(modifier = Modifier.height(8.dp))
                               LazyColumn(
                                   modifier = Modifier.height(70.dp)
                               ) {
                                   items(selectedAddresses) { addr ->
                                       Text(
                                           text = addr,
                                           style = MaterialTheme.typography.bodySmall,
                                           color = MaterialTheme.colorScheme.onSurfaceVariant,
                                           maxLines = 1,
                                           overflow = TextOverflow.Ellipsis
                                       )
                                   }
                               }
                               Spacer(modifier = Modifier.height(12.dp))
                           }

                       }
                       item{
                           OutlinedTextField(
                               value = tagName,
                               onValueChange = { tagName = it },
                               label = { Text("标签名称") },
                               placeholder = { Text("输入或选择") },
                               modifier = Modifier.fillMaxWidth(),
                               singleLine = true,
                               colors = OutlinedTextFieldDefaults.colors(
                                   focusedBorderColor = MaterialTheme.colorScheme.primary,
                                   unfocusedBorderColor = MaterialTheme.colorScheme.outline
                               )
                           )

                       }
                       item{
                           Spacer(modifier = Modifier.height(16.dp))
                       }


                       item{
                           if (allAddresses.isNotEmpty()) {
                               Text(
                                   text = "从地址列表选择:",
                                   style = MaterialTheme.typography.bodySmall,
                                   color = MaterialTheme.colorScheme.onSurfaceVariant
                               )
                               Spacer(modifier = Modifier.height(8.dp))
                               LazyColumn(
                                   modifier = Modifier
                                       .height(121.dp)
                                       .fillMaxWidth()
                                       .background(color = MaterialTheme.colorScheme.surfaceVariant),
                                   contentPadding = PaddingValues(8.dp)
                               ) {
                                   item {

                                       FlowRow(
                                           horizontalArrangement = Arrangement.spacedBy(8.dp),
                                           verticalArrangement = Arrangement.spacedBy(4.dp)
                                       ) {

                                           allAddresses.forEach { addr ->
                                               SuggestionChip(
                                                   text = addr,
                                                   onClick = { tagName = addr },
                                                   isSelected = tagName == addr,
                                               )
                                           }
                                       }
                                   }
                               }
                               Spacer(modifier = Modifier.height(16.dp))
                           }

                       }

                        item{
                            if (availableTags.isNotEmpty()) {
                                Text(
                                    text = "选择已有标签:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyColumn(
                                    modifier = Modifier
                                        .height(121.dp)
                                        .fillMaxWidth()
                                        .background(color = MaterialTheme.colorScheme.surfaceVariant),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    item {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            availableTags.forEach { tag ->
                                                SuggestionChip(
                                                    text = tag,
                                                    onClick = { tagName = tag },
                                                    isSelected = tagName == tag,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        }
                      item{
                          Spacer(modifier = Modifier.height(12.dp))
                      }

                        item{
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (showRemoveButton) {
                                    TextButton(onClick = { onRemove?.invoke() }) {
                                        Text("移除标签", color = MaterialTheme.colorScheme.error)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onDismiss,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("取消")
                                    }
                                    Button(
                                        onClick = { onConfirm(tagName) },
                                        enabled = tagName.isNotBlank(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            when {
                                                showWarning -> "确定修改"
                                                isTagChanged -> "确定修改"
                                                else -> "确定"
                                            }
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

@Composable
private fun WarningCard(
    addresses: List<String>,
    existingMappings: Map<String, String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "以下地址已被归类，确定要修改吗？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            addresses.take(3).forEach { addr ->
                val oldTag = existingMappings[addr] ?: ""
                Text(
                    text = "$addr → $oldTag",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (addresses.size > 3) {
                Text(
                    text = "...等 ${addresses.size} 个地址",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TagChangeWarningCard(
    currentTag: String,
    newTag: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "该地址已归类到 \"$currentTag\"，确定改为 \"$newTag\" 吗？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean,
) {


    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceBright
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
