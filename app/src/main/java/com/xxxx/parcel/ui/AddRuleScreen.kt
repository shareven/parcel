package com.xxxx.parcel.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.util.addCustomList
import com.xxxx.parcel.viewmodel.ParcelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    message: String,
    onCallback: () -> Unit
) {

    var addressPattern by remember { mutableStateOf("") }
    var codePattern by remember { mutableStateOf("") }
    var ignoreKeyword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增规则") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            navController.navigate("rules")
                        }
                    ) {
                        Text("规则列表")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Card(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),

                    ) {
                    SelectionContainer {
                        Text(
                            text = message,
                            modifier = Modifier
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Text(
                            text = "复制短信中的 取件码 填入",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = codePattern,
                            placeholder = {Text("选填")},
                            onValueChange = { codePattern = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "复制短信中的 地址 填入",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = addressPattern,
                            placeholder = {Text("选填")},
                            onValueChange = { addressPattern = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "填入关键词，不解析短信",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ignoreKeyword,
                            placeholder = {Text("选填")},
                            onValueChange = { ignoreKeyword = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {


                        Button(
                            enabled = addressPattern.isNotEmpty() && message.contains(addressPattern) || codePattern.isNotEmpty() && message.contains(
                                codePattern
                            ) || ignoreKeyword.isNotEmpty(),
                            onClick = {
                                if (addressPattern.isNotBlank()) {
                                    addCustomList(context, "address", addressPattern)
                                    viewModel.addCustomAddressPattern(addressPattern)
                                    addressPattern = ""
                                }
                                if (codePattern.isNotBlank()) {
                                    // 转义正则表达式中的特殊字符，但保留捕获组
                                    val escapedCodePattern = java.util.regex.Pattern.quote(codePattern)
                                    
                                    // 分别转义取件码前后的部分为字面字符
                                    val parts = message.split(codePattern, limit = 2)
                                    val regexPattern = if (parts.size == 2) {
                                        java.util.regex.Pattern.quote(parts[0]) + """([\s\S]{2,})""" + java.util.regex.Pattern.quote(parts[1])
                                    } else {
                                        // 如果分割失败，使用原来的方法
                                        java.util.regex.Pattern.quote(message).replace(escapedCodePattern, """([\s\S]{2,})""")
                                    }
                                    
                                    addCustomList(
                                        context,
                                        "code",
                                        regexPattern
                                    )
                                    viewModel.addCustomCodePattern(regexPattern)
                                    codePattern = ""
                                }
                                if (ignoreKeyword.isNotBlank()) {
                                    addCustomList(context, "ignoreKeywords", ignoreKeyword)
                                    viewModel.addIgnoreKeyword(ignoreKeyword)
                                    ignoreKeyword = ""
                                }
                                onCallback()
                                navController.navigate("rules")
                            }
                        ) {
                            Text(text = "点击自动添加规则")
                        }
                    }

                }

            }
        }
    }
}