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

    // 取件码校验正则表达式（字符集与 SmsParser.codePattern 保持一致）
    val pickupCodePattern = Pattern.compile("[A-Za-z0-9\\s\\-*—]{2,}(?:[，,、][A-Za-z0-9\\s\\-*—]{2,})*")

    // 校验取件码函数
    fun validatePickupCode(code: String): Boolean {
        return if (code.isEmpty()) {
            validationMessage = "取件码不能为空"
            false
        } else if (code.length < 2) {
            validationMessage = "最小长度为2"
            false
        } else if (!pickupCodePattern.matcher(code).matches()) {
            validationMessage = "取件码格式不正确，应包含字母、数字、空格、横线或星号，长度至少2位"

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

            val structured = parseStructuredContent(clipText)
            if (structured != null) {
                isSms = false
                pickupCode = structured.first
                addressSet = structured.second
                isPickupCodeValid = validatePickupCode(structured.first)
            } else {
                isSms = validateIsSms(clipText)
                if (isSms) {
                    sms = clipText
                } else {
                    pickupCode = clipText
                }
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
                text = "自动识别复制内容是取件码,还是短信",
                style = MaterialTheme.typography.bodyMedium
            )
            if (!isSms) {
                Text(
                    text = "可识别的复制格式：\n" +
                        "· 纯取件码：AB-12\n" +
                        "· 取件码/取货码/提货码 A-683 东山快递（单行或换行，可带冒号）\n" +
                        "· 1号柜，取件码：324324\n" +
                        "· 3424-234 到2号快递柜 / 2号快递柜 3424-234（地址含柜/驿站/路等）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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

// 取件码引导关键词（识别复制内容用）
private val codeKeywordsRegex = """(?:取件码|取货码|提货码|取货号|提货号|签收码)"""

// 地址信号词：无关键词格式识别时校验地址合理性（"路"排除"铁路"误报）
private val addressSignal = Regex("""柜|驿站|快递|超市|便利店|门面|楼|室|(?<!铁)路|街|道|区|站|店|场|苑|府|厦|村|栋|单元|广场|中心""")

// 地址内出现取件码关键词说明码是关键词引导的，不应走无关键词识别
private val addressHasCodeKeyword = Regex("""取件码|取货码|提货码|取货号|提货号|签收码|""")

// 识别复制内容中的 取件码+地址：带关键词（正向/倒序）与无关键词（码在前/码在后），返回 取件码 to 地址
private fun parseStructuredContent(text: String): Pair<String, String>? {
    val trimmed = text.trim()
    // 尝试一：关键词在前，如 "取件码 H-683 东山极兔快递"（单行或换行分隔，可带冒号）
    val forward = Regex("""(?s)^\s*$codeKeywordsRegex\s*[:：]?\s*(.+)$""").find(trimmed)
    if (forward != null) {
        val rest = forward.groupValues[1]
        // 取件码字符集：ASCII字母、数字、空格、- — _ *（注意不能用 isLetterOrDigit，中文也是字母）；首个之外的字符处切分
        val idx = rest.indexOfFirst { c ->
            !(c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == ' ' || c == '-' || c == '—' || c == '_' || c == '*')
        }
        if (idx > 0) {
            val code = rest.take(idx).trim()
            val address = cleanupAddress(rest.substring(idx))
            if (code.length >= 2 && address.isNotEmpty() &&
                !addressHasCodeKeyword.containsMatchIn(address)
            ) return code to address
        }
    }
    // 尝试二：地址在前，如 "1号柜，取件码：324324"；取件码须在末尾（仅允许可选备注尾巴），避免误抢真实短信
    val reverse = Regex("""(?s)^(.+?)\s*[,，]?\s*$codeKeywordsRegex\s*[:：]?\s*([A-Za-z0-9\-*—_]{2,})\s*(?:[,，]\s*备注[：:].*)?$""").find(trimmed)
    if (reverse != null) {
        val code = reverse.groupValues[2].trim()
        val address = cleanupAddress(reverse.groupValues[1])
        // 地址含取件码引导词说明码是关键词引导的（如"...凭取件码 8877"结尾的短信），不是倒序格式
        if (address.isNotEmpty() && !addressHasCodeKeyword.containsMatchIn(address)) {
            return code to address
        }
    }
    // 尝试三/四：无关键词格式，如 "3424-234 到2号快递柜"、"2号快递柜 3424-234"、"H-683\n东山极兔快递 | 苏果…"
    // 防误抢：总长受限 + 地址须像地址（含信号词与中文、不含取件码关键词）
    if (trimmed.length <= 40) {
        val codeFirst = Regex("""(?s)^([A-Za-z0-9\-*—_]{2,})(?:\s*(?:到|至|在)\s*|\s*[,，、]\s*|\s+)(.+)$""").find(trimmed)
        if (codeFirst != null) {
            val address = cleanupAddress(codeFirst.groupValues[2])
            if (looksLikeAddress(address)) return codeFirst.groupValues[1] to address
        }
        val codeLast = Regex("""(?s)^(.+?)\s*[,，、]?\s*([A-Za-z0-9\-*—_]{2,})$""").find(trimmed)
        if (codeLast != null) {
            val address = cleanupAddress(codeLast.groupValues[1])
            if (looksLikeAddress(address)) return codeLast.groupValues[2] to address
        }
    }
    return null
}

// 地址清理：去分隔符与"包裹已到/地址/位置"前缀标签，丢弃"，备注：xxx"尾巴，"站点名 | 详细地址"取后段
private fun cleanupAddress(raw: String): String {
    var address = raw.trim().trim(':', '：', ',', '，').trim().removePrefix("包裹已到")
    address = address.removePrefix("地址").removePrefix("位置").trim(':', '：', ' ').trim()
    address = address.replace(Regex("[,，]备注[：:].*$"), "").trim()
    if (address.contains('|') || address.contains('｜')) {
        val tail = address.substringAfterLast('|').substringAfterLast('｜').trim()
        if (tail.isNotEmpty()) address = tail
    }
    return address
}

// 无关键词格式的地址校验：非空、含中文、含地址信号词、不含取件码引导词
private fun looksLikeAddress(address: String): Boolean {
    return address.isNotEmpty() &&
            address.any { it in '\u4e00'..'\u9fff' } &&
            addressSignal.containsMatchIn(address) &&
            !addressHasCodeKeyword.containsMatchIn(address)
}