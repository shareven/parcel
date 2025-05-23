package com.xxxx.parcel.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.util.PermissionUtil
import com.xxxx.parcel.viewmodel.ParcelViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(context:Context,viewModel: ParcelViewModel, navController: NavController,onCallBack:()->Unit) {
   val hasPermission= PermissionUtil.hasSmsPermissions(context)
    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    Button(
                        contentPadding = PaddingValues(2.dp),
                        colors = ButtonColors(
                            containerColor = Color(0xFF25AF22),
                            contentColor = Color.White,
                            disabledContentColor = Color.DarkGray,
                            disabledContainerColor = Color.LightGray
                        ),
                        onClick = { navController.navigate("success_sms") },
                    ) {
                        Text(
                            text = viewModel.successSmsData.value.size.toString(),
                            fontWeight = FontWeight.Bold
                        )

                    }
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        colors = ButtonColors(
                            containerColor = Color(0xFFAB1A65),
                            contentColor = Color.White,
                            disabledContentColor = Color.DarkGray,
                            disabledContainerColor = Color.LightGray
                        ),

                        onClick = { navController.navigate("fail_sms") },
                    ) {
                        Text(
                            text = viewModel.failedMessages.value.size.toString(),
                            color = Color.White,

                            )
                    }
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        onClick = { navController.navigate("about") },
                    ) {
                        Text(text = "关于")
                    }
                }

            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if(hasPermission) List(viewModel) else
            Column(modifier = Modifier.fillMaxSize(),verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {onCallBack()}) {
                    Text("获取短信权限")
                }
            }
        }
    }
}


@Composable
fun List(viewModel: ParcelViewModel) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
         items(viewModel.parcels.value) { result ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    text = result.address,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        result.codes.forEach { code ->
                            Box(modifier = Modifier.padding(6.dp)) {
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)

                                )
                            }
                        }


                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

        }
    }
}

