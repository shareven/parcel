package com.xxxx.parcel.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
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
import com.xxxx.parcel.model.SmsModel
import com.xxxx.parcel.util.addCustomSms
import com.xxxx.parcel.viewmodel.ParcelViewModel
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomSmsScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    address: String,
    onCallback: () -> Unit
) {
    var pickupCode by remember { mutableStateOf("") }
    var addressSet by remember { mutableStateOf(address) }
    var generatedSmsContent by remember { mutableStateOf("") }
    var isPickupCodeValid by remember { mutableStateOf(true) }
    var isSms by remember { mutableStateOf(false) }
    var sms by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf("") }

    // 取件码校验正则表达式
    val pickupCodePattern = Pattern.compile("[A-Za-z0-9\\s-]{2,}(?:[，,、][A-Za-z0-9\\s-]{2,})*")

    // 校验取件码函数
    fun validatePickupCode(code: String): Boolean {
        return if (code.isEmpty()) {
            validationMessage = "取件码不能为空"
            false
        } else if (code.length < 2) {
            validationMessage = "最小长度为2"
            false
        } else if (!pickupCodePattern.matcher(code).matches()) {
            validationMessage = "取件码格式不正确，应包含字母、数字、空格或短横线，长度至少2位"

            false
        } else {
            validationMessage = ""
            true
        }
    }

    // 校验是否为短信
    fun validateIsSms(code: String): Boolean {
        return if (code.isEmpty()) {
            false
        } else if (code.length < 2) {
            false
        } else if (pickupCodePattern.matcher(code).matches()) {
            false
        } else {
            true
        }
    }


    // 自动粘贴剪贴板内容到取件码输入框
    LaunchedEffect(Unit) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val clipText = clipData.getItemAt(0).text?.toString() ?: ""

            isSms = validateIsSms(clipText)
            if (isSms) {
                sms = clipText
            } else {
                pickupCode = clipText
            }
        }
    }

    // 根据取件码或短信自动生成自定义短信内容
    LaunchedEffect(pickupCode, addressSet, sms, isSms) {
        if (isSms) {
            if (sms.isNotEmpty()) {
                generatedSmsContent = "【自定义取件短信】${sms}"
            } else {
                generatedSmsContent = ""
            }
        } else if (addressSet.isNotEmpty() && pickupCode.isNotEmpty()) {
            generatedSmsContent = "【自定义取件短信】取件码${pickupCode}，包裹已到${addressSet}"
        } else {
            generatedSmsContent = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增自定义取件短信") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "自动识别复制内容是取件码还是短信",
                style = MaterialTheme.typography.bodyMedium
            )
            Card(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                ) {

                    if (isSms) {

                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                        ) {


                            // 取件码输入框
                            Column {
                                Text(
                                    text = "短信（可自动粘贴短信）",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedTextField(
                                    value = sms,
                                    onValueChange = {
                                        sms = it
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("请输入短信") },

                                    )
                            }

                            Spacer(modifier = Modifier.height(16.dp))


                        }
                    } else {

                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                        ) {
                            // 显示地址信息
                            Column {
                                Text(
                                    text = "取件地址：",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedTextField(
                                    value = addressSet,
                                    onValueChange = { addressSet = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 取件码输入框
                            Column {
                                Text(
                                    text = "取件码（可自动粘贴取件码）",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedTextField(
                                    value = pickupCode,
                                    onValueChange = {
                                        pickupCode = it
                                        isPickupCodeValid = validatePickupCode(it)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("请输入取件码") },
                                    isError = !isPickupCodeValid,
                                    supportingText = {
                                        if (!isPickupCodeValid && validationMessage.isNotEmpty()) {
                                            Text(
                                                text = validationMessage,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))


                        }
                    }
                    // 显示自动生成的短信内容
                    if (generatedSmsContent.isNotEmpty()) {
                        Column {
                            Text(
                                text = "生成的短信内容：",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            SelectionContainer {
                                Text(
                                    text = generatedSmsContent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    color = Color(0xFF25AF22),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        isSms = !isSms
                    }
                ) {
                    if (isSms) Text(text = "输入取件码") else Text(text = "输入短信")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    enabled = generatedSmsContent.isNotEmpty(),
                    onClick = {
                        val currentTime = System.currentTimeMillis()
                        val smsModel = SmsModel(
                            id = currentTime.toString(),
                            body = generatedSmsContent,
                            timestamp = currentTime
                        )
                        addCustomSms(context, smsModel)
                        onCallback()
                        navController.navigate("home")

                    }
                ) {
                    Text(text = "点击保存")
                }
            }
        }
    }
}