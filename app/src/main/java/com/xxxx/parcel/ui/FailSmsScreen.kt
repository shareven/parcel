package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.xxxx.parcel.util.removeCustomSms
import com.xxxx.parcel.viewmodel.ParcelViewModel
import java.net.URLEncoder
import com.xxxx.parcel.util.dateToString
import com.xxxx.parcel.util.isCustomSms

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailSmsScreen(viewModel: ParcelViewModel, navController: NavController, isSeniorMode: Boolean = false, readAndParseSms: () -> Unit = {}) {

    val context = LocalContext.current
    val failSmsData by viewModel.failedMessages.collectAsState()
    val textStyle = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium
    val captionStyle = if (isSeniorMode) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("解析失败${failSmsData.size}条短信") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
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
                .padding(horizontal = if (isSeniorMode) 12.dp else 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            items(failSmsData) { message ->
                Card(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                         horizontalAlignment = Alignment.Start
                    ) {
                        SelectionContainer {
                            Text(
                                text = message.body,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = textStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dateToString(message.timestamp),
                            style = captionStyle,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val encodedMsg = URLEncoder.encode(message.body, "UTF-8")
                                    navController.navigate("add_rule?message=${encodedMsg}")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "添加解析规则",
                                    style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelLarge
                                )
                            }

                            if (isCustomSms(message)) {
                                OutlinedButton(
                                    onClick = {
                                        removeCustomSms(context, message.id)
                                        readAndParseSms()
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除",
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .size(18.dp)
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