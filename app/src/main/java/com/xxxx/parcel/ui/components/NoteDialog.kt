package com.xxxx.parcel.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xxxx.parcel.util.ThirdPartyDefaults

@Composable
fun NoteDialog(
    context: Context,
    code: String,
    address: String = "",
    compartmentNumber: String = "",
    currentNote: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var noteText by remember { mutableStateOf(currentNote) }
    val clipboard = LocalClipboardManager.current

    // 复制/分享文本：有格口和备注就拼接，没有就不显示
    val copyText = buildString {
        append("取件码$code，包裹已到$address")
        if (compartmentNumber.isNotEmpty()) append("${compartmentNumber}格口")
        if (noteText.isNotBlank()) append("，备注：${noteText.trim()}")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { if (it.length <= 9) noteText = it },
                                label = { Text("备注内容") },
                                placeholder = { Text("输入备注") },
                                supportingText = {
                                    Text(
                                        text = "${noteText.length}/9",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                },
                                trailingIcon = {
                                    if (noteText.isNotEmpty()) {
                                        IconButton(onClick = { noteText = "" }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "清空",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            SelectionContainer {
                                Text(
                                    text = copyText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF25AF22)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(copyText))
                                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("复制")
                                }
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, copyText)
                                            setPackage(ThirdPartyDefaults.WECHAT_PACKAGE)
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "未安装微信", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("微信分享")
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Button(
                                onClick = { onConfirm(noteText.trim()) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("确定")
                            }
                        }
                    }
                }
            }
        }
    }
}
