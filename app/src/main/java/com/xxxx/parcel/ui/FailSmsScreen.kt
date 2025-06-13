package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.viewmodel.ParcelViewModel
import java.net.URLEncoder

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailSmsScreen(viewModel: ParcelViewModel, navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("解析失败的短信（${viewModel.failedMessages.value.size}）") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            items(viewModel.failedMessages.value) { message ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        SelectionContainer {
                            Text(
                                text = message.body,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                       
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val encodedMsg = URLEncoder.encode(message.body, "UTF-8")
                                navController.navigate("add_rule?message=${encodedMsg}") 
                            }
                        ) {
                            Text(text = "添加解析规则")
                        }
                    }
                }
            }
        }
    }
}