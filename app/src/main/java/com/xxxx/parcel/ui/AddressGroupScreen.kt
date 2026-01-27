package com.xxxx.parcel.ui

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.util.SmsProcessor
import com.xxxx.parcel.util.SmsUtil
import com.xxxx.parcel.util.getAddressMappings
import com.xxxx.parcel.util.getAllTags
import com.xxxx.parcel.util.removeAddressMapping
import com.xxxx.parcel.util.saveAddressMapping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressGroupScreen(
    context: android.content.Context,
    navController: NavController,
    onCallback: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    
    // 所有识别出的地址（去重）
    var allAddresses by remember { mutableStateOf(listOf<String>()) }
    // 当前地址映射
    var addressMappings by remember { mutableStateOf(mapOf<String, String>()) }
    
    // 多选模式状态
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedAddresses = remember { mutableStateListOf<String>() }
    
    // 添加归类弹窗状态
    var showAddDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val mappings = getAddressMappings(context)
                addressMappings = mappings
                
                // 读取所有短信，提取地址
                val addresses = withContext(Dispatchers.IO) {
                    val allSms = SmsUtil.readAllSms(context)
                    val parser = com.xxxx.parcel.util.SmsParser()
                    allSms.mapNotNull { sms ->
                        val result = parser.parseSms(sms.body)
                        if (result.success) result.address else null
                    }.distinct().sorted()
                }
                allAddresses = addresses
            } catch (e: Exception) {
                com.xxxx.parcel.util.addLog(context, "加载地址列表失败: ${e.message}")
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    fun saveMapping(originalAddress: String, tag: String) {
        saveAddressMapping(context, originalAddress, tag)
        addressMappings = getAddressMappings(context)
        onCallback()
    }

    fun removeMapping(originalAddress: String) {
        removeAddressMapping(context, originalAddress)
        addressMappings = getAddressMappings(context)
        onCallback()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("地址归类") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (isSelectionMode && selectedAddresses.isNotEmpty()) {
                        IconButton(onClick = {
                            showAddDialog = true
                            newTagName = ""
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "合并选中")
                        }
                    }
                    if (!isSelectionMode) {
                        IconButton(onClick = {
                            isSelectionMode = true
                            selectedAddresses.clear()
                        }) {
                            Text("多选")
                        }
                    } else {
                        TextButton(onClick = {
                            isSelectionMode = false
                            selectedAddresses.clear()
                        }) {
                            Text("取消")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("加载中...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 已归类地址
                val mappedAddresses = addressMappings.keys.toList()
                if (mappedAddresses.isNotEmpty()) {
                    item {
                        Text(
                            text = "已归类地址 (${mappedAddresses.size})",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(mappedAddresses.sorted()) { addr ->
                        val tag = addressMappings[addr] ?: ""
                        Card(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = addr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(tag) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                                if (!isSelectionMode) {
                                    IconButton(onClick = { removeMapping(addr) }) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "移除归类",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else {
                                    Checkbox(
                                        checked = selectedAddresses.contains(addr),
                                        onCheckedChange = { checked ->
                                            if (checked) selectedAddresses.add(addr)
                                            else selectedAddresses.remove(addr)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                // 未归类地址
                val unmappedAddresses = allAddresses.filter { it !in addressMappings.keys }
                item {
                    Text(
                        text = "未归类地址 (${unmappedAddresses.size})",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(unmappedAddresses.sorted()) { addr ->
                    Card(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = addr,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelectionMode) {
                                Checkbox(
                                    checked = selectedAddresses.contains(addr),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedAddresses.add(addr)
                                        else selectedAddresses.remove(addr)
                                    }
                                )
                            } else {
                                IconButton(onClick = {
                                    selectedAddresses.clear()
                                    selectedAddresses.add(addr)
                                    showAddDialog = true
                                    newTagName = ""
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "添加归类")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 添加归类弹窗
    if (showAddDialog) {
        val availableTags = getAllTags(context)
        
        // 检查是否有地址已被归类到其他标签
        val conflictingAddresses = selectedAddresses.filter { addr ->
            val existingTag = addressMappings[addr]
            existingTag != null && existingTag != newTagName
        }
        
        val showWarning = conflictingAddresses.isNotEmpty()

        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false 
                selectedAddresses.clear()
                isSelectionMode = false
            },
            title = { Text(if (showWarning) "确认修改标签" else "设置归类标签") },
            text = {
                Column {
                    if (showWarning) {
                        Text(
                            text = "以下地址已被归类到其他标签，确定要修改吗？",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        conflictingAddresses.forEach { addr ->
                            val oldTag = addressMappings[addr] ?: ""
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        text = "$addr → $oldTag",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("标签名称") },
                        placeholder = { Text("输入或选择") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 从地址列表选择作为标签
                    if (allAddresses.isNotEmpty()) {
                        Text("从地址列表选择:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(
                            modifier = Modifier.height(120.dp)
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
                            selectedAddresses.forEach { addr ->
                                saveMapping(addr, newTagName)
                            }
                            showAddDialog = false
                            isSelectionMode = false
                            selectedAddresses.clear()
                            loadData()
                        }
                    },
                    enabled = newTagName.isNotBlank()
                ) {
                    Text(if (showWarning) "确定修改" else "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false 
                    selectedAddresses.clear()
                    isSelectionMode = false
                }) {
                    Text("取消")
                }
            }
        )
    }
}
