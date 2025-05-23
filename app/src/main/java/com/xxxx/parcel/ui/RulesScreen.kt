package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.util.clearAllCustomPatternsa
import com.xxxx.parcel.util.getCustomPatterns
import com.xxxx.parcel.viewmodel.ParcelViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    onCallback: () -> Unit
) {
    val listAddr = getCustomPatterns(context, "address").toMutableList()
    val listCode = getCustomPatterns(context, "code").toMutableList()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("规则列表") },
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
                            clearAllCustomPatternsa(context, viewModel)
                            onCallback()
                            navController.navigate("home")
                        }
                    ) {
                        Text("清除所有自定规则")
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
            item {
                Text(
                    text = "自定义地址规则",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            items(listAddr) { address ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                ) {

                    SelectionContainer(Modifier.padding(16.dp)) {
                        Text(
                            text = address,
                            modifier = Modifier
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


                }
            }
            item {
                Spacer(Modifier.height(16.dp))
            }
            item {
                Text(
                    text = "自定义取件码规则",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            items(listCode) { code ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                ) {

                    SelectionContainer(Modifier.padding(16.dp)) {
                        Text(
                            text = code,
                            modifier = Modifier
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium
                        )


                    }
                }
            }

        }
    }
}