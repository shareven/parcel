package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.util.removeCustomSms
import com.xxxx.parcel.util.getAddressMappings
import com.xxxx.parcel.util.getAllTags
import com.xxxx.parcel.util.saveAddressMapping
import com.xxxx.parcel.util.removeAddressMapping
import com.xxxx.parcel.viewmodel.ParcelViewModel
import java.net.URLEncoder
import com.xxxx.parcel.util.dateToString
import com.xxxx.parcel.util.isCustomSms

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessSmsScreen(
    viewModel: ParcelViewModel,
    navController: NavController,
    readAndParseSms: () -> Unit = {}
) {
    val context = LocalContext.current
    val successSmsData by viewModel.successSmsData.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf("") }
    var currentTag by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }

    fun getTagForAddress(addr: String): String? {
        val mappings = getAddressMappings(context)
        return mappings[addr]
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("已解析的短信（${successSmsData.size}）") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(successSmsData) { data ->
                val tag = getTagForAddress(data.address)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        SelectionContainer {
                            Text(
                                text = data.sms.body,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${dateToString(data.sms.timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 地址显示
                        SelectionContainer {
                            Text(
                                text = "地址: ${data.address}",
                                color = Color(0xFF25AF22),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        // 标签显示
                        if (tag != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            AssistChip(
                                onClick = {
                                    currentAddress = data.address
                                    currentTag = tag
                                    newTagName = tag
                                    showEditDialog = true
                                },
                                label = { Text(tag) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            AssistChip(
                                onClick = {
                                    currentAddress = data.address
                                    currentTag = ""
                                    newTagName = ""
                                    showEditDialog = true
                                },
                                label = { Text("添加标签") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        SelectionContainer {
                            Text(
                                text = "取件码: ${data.code}",
                                color = Color(0xFF25AF22),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val encodedMsg = URLEncoder.encode(data.sms.body, "UTF-8")
                                    navController.navigate("add_rule?message=${encodedMsg}") 
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "添加解析规则")
                            }
                            
                            // 只有自定义短信才显示删除按钮
                            if (isCustomSms(data.sms)) {
                                OutlinedButton(
                                    onClick = {
                                        removeCustomSms(context, data.sms.id)
                                        readAndParseSms()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除",
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(text = "删除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 编辑标签弹窗
    if (showEditDialog) {
        val availableTags = getAllTags(context)
        
        // 检查是否修改了标签
        val isTagChanged = currentTag.isNotBlank() && currentTag != newTagName
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(if (isTagChanged) "确认修改标签" else "设置地址标签") },
            text = {
                Column {
                    if (isTagChanged) {
                        Text(
                            text = "该地址已被归类到 \"$currentTag\"，确定要修改为 \"$newTagName\" 吗？",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("$currentAddress → $currentTag") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    AssistChip(
                        onClick = { },
                        label = { Text(currentAddress) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("标签名称") },
                        placeholder = { Text("输入或选择") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 从地址列表选择作为标签
                    val allAddresses = remember { 
                        try {
                            val allSms = com.xxxx.parcel.util.SmsUtil.readAllSms(context)
                            val parser = com.xxxx.parcel.util.SmsParser()
                            allSms.mapNotNull { sms ->
                                val result = parser.parseSms(sms.body)
                                if (result.success) result.address else null
                            }.distinct().sorted()
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    
                    if (allAddresses.isNotEmpty()) {
                        Text("从地址列表选择:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(
                            modifier = Modifier.height(100.dp)
                        ) {
                            items(allAddresses.take(10)) { addr ->
                                Text(
                                    text = addr,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { newTagName = addr }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    if (availableTags.isNotEmpty()) {
                        Text("或选择已有标签:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableTags.take(3).forEach { tag ->
                                AssistChip(
                                    onClick = { newTagName = tag },
                                    label = { Text(tag, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            saveAddressMapping(context, currentAddress, newTagName)
                            readAndParseSms()
                        }
                        showEditDialog = false
                    },
                    enabled = newTagName.isNotBlank()
                ) {
                    Text(if (isTagChanged) "确定修改" else "确定")
                }
            },
            dismissButton = {
                Row {
                    if (currentTag.isNotBlank()) {
                        TextButton(
                            onClick = {
                                removeAddressMapping(context, currentAddress)
                                readAndParseSms()
                                showEditDialog = false
                            }
                        ) {
                            Text("移除标签", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("取消")
                    }
                }
            }
        )
    }
}

