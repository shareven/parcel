package com.xxxx.parcel.ui

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.ui.components.TagDialog
import com.xxxx.parcel.util.removeCustomSms
import com.xxxx.parcel.util.getAddressMappings
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
    readAndParseSms: () -> Unit = {},
) {
    val context = LocalContext.current
    val successSmsData by viewModel.successSmsData.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf("") }
    var currentTag by remember { mutableStateOf("") }


    fun getTagForAddress(addr: String): String? {
        val mappings = getAddressMappings(context)
        return mappings[addr]
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "已解析的短信（${successSmsData.size}）"
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
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
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dateToString(data.sms.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "地址: ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                SelectionContainer {
                                    Text(
                                        text = data.address,
                                        color = Color(0xFF25AF22),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "取件码: ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                SelectionContainer {
                                    Text(
                                        text = data.code,
                                        color = Color(0xFF25AF22),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }

//                            Spacer(modifier = Modifier.height(6.dp))

                            if (tag != null) {
                                Surface(
                                    onClick = {
                                        currentAddress = data.address
                                        currentTag = tag
                                        showEditDialog = true
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor =  MaterialTheme.colorScheme.onPrimaryContainer,
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 4.dp
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            } else {
                                Surface(
                                    onClick = {
                                        currentAddress = data.address
                                        currentTag = ""
                                        showEditDialog = true
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(14.dp),
                                        )
                                        Text(
                                            text = "添加地址标签",
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

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
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .size(16.dp)
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

        if (showEditDialog) {
            TagDialog(
                title = "设置地址标签",
                currentAddress = currentAddress,
                currentTag = currentTag,
                onDismiss = { showEditDialog = false },
                onConfirm = { tag ->
                    if (tag.isNotBlank()) {
                        saveAddressMapping(context, currentAddress, tag)
                        readAndParseSms()
                    }
                    showEditDialog = false
                },
                onRemove = {
                    removeAddressMapping(context, currentAddress)
                    readAndParseSms()
                    showEditDialog = false
                }
            )
        }
    }
}
