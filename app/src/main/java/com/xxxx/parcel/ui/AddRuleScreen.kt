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
import com.xxxx.parcel.util.addCustomPatterns
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
                                .fillMaxWidth()
                                .height(60.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Text(
                            text = "复制短信中的 取件码填入",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = codePattern,
                            onValueChange = { codePattern = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "复制短信中的 地址填入",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = addressPattern,
                            onValueChange = { addressPattern = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {


                        Button(
                            enabled = addressPattern.isNotEmpty() && codePattern.isNotEmpty()&&message.contains(addressPattern)&&message.contains(codePattern),
                            onClick = {
                                if (addressPattern.isNotEmpty() && codePattern.isNotEmpty()) {
                                    addCustomPatterns(context, "address", addressPattern)
                                    addCustomPatterns(
                                        context,
                                        "code",
                                        message.replace(codePattern, """([\s\S]{4,})""")
                                    )
                                    viewModel.addCustomAddressPattern(addressPattern)
                                    viewModel.addCustomCodePattern(
                                        message.replace(
                                            codePattern,
                                            """([\s\S]{4,})"""
                                        )
                                    )
                                    addressPattern = ""
                                    codePattern = ""
                                    onCallback()
                                    navController.navigate("home")
                                }
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