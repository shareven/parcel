package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xxxx.parcel.ui.components.TagDialog
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.SmsUtil
import com.xxxx.parcel.util.getAddressMappings
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
    onCallback: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var allAddresses by remember { mutableStateOf(listOf<String>()) }
    var addressMappings by remember { mutableStateOf(mapOf<String, String>()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedAddresses = remember { mutableStateListOf<String>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf<String?>(null) }
    var currentTag by remember { mutableStateOf("") }


    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val mappings = getAddressMappings(context)
                addressMappings = mappings
                val addresses = withContext(Dispatchers.IO) {
                    val allSms = SmsUtil.readAllSms(context)
                    val parser = SmsParser()
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

    LaunchedEffect(Unit) { loadData() }

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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("地址归类", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    if (isSelectionMode && selectedAddresses.isNotEmpty()) {
                        IconButton(onClick = {
                            showAddDialog = true
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "合并选中")
                        }
                    }
                    if (!isSelectionMode) {
                        TextButton(onClick = {
                            isSelectionMode = true
                            currentAddress = null
                            currentTag = ""
                            selectedAddresses.clear()
                        }) {
                            Text("多选", color = MaterialTheme.colorScheme.onSurface)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("加载中...", color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val mappedAddresses = addressMappings.keys.toList()
                if (mappedAddresses.isNotEmpty()) {
                    item {
                        SectionHeader(title = "已归类地址 (${mappedAddresses.size})")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(mappedAddresses.sorted()) { addr ->
                        val tag = addressMappings[addr] ?: ""
                        AddressCard(
                            address = addr,
                            tag = tag,
                            isSelected = selectedAddresses.contains(addr),
                            isSelectionMode = isSelectionMode,
                            onItemClick = {
                                if (isSelectionMode) {
                                    if (selectedAddresses.contains(addr)) {
                                        selectedAddresses.remove(addr)
                                    } else {
                                        selectedAddresses.add(addr)
                                    }
                                }
                            },
                            onEditClick = {
                                selectedAddresses.clear()
                                selectedAddresses.add(addr)
                                currentTag = tag
                                currentAddress = addr
                                showAddDialog = true
                            },
                            onDeleteClick = { removeMapping(addr) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                val unmappedAddresses = allAddresses.filter { it !in addressMappings.keys }
                item {
                    SectionHeader(title = "未归类地址 (${unmappedAddresses.size})")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(unmappedAddresses.sorted()) { addr ->
                    AddressCard(
                        address = addr,
                        tag = null,
                        isSelected = selectedAddresses.contains(addr),
                        isSelectionMode = isSelectionMode,
                        onItemClick = {
                            if (isSelectionMode) {
                                if (selectedAddresses.contains(addr)) {
                                    selectedAddresses.remove(addr)
                                } else {
                                    selectedAddresses.add(addr)
                                }
                            }
                        },
                        onEditClick = {
                            selectedAddresses.clear()
                            selectedAddresses.add(addr)
                            currentAddress = addr
                            showAddDialog = true
                        },
                        onDeleteClick = {}
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        TagDialog(
            title = "设置归类标签",
            currentTag = currentTag,
            currentAddress = currentAddress,
            existingMappings = addressMappings,
            selectedAddresses = selectedAddresses.toList(),
            onDismiss = {
                showAddDialog = false
                selectedAddresses.clear()
                isSelectionMode = false
            },
            onConfirm = { tag ->
                if (tag.isNotBlank()) {
                    selectedAddresses.forEach { addr ->
                        saveMapping(addr, tag)
                    }
                    showAddDialog = false
                    isSelectionMode = false
                    selectedAddresses.clear()
                    loadData()
                }
            }
        )
    }

}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AddressCard(
    address: String,
    tag: String?,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val cardBorder = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(cardBorder)
            .clickable { onItemClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (tag != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (!isSelectionMode && tag != null) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "移除归类",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else if (!isSelectionMode && tag == null) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加归类",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
