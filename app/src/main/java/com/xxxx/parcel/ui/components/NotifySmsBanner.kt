package com.xxxx.parcel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// HyperOS「通知类短信」权限被拒的提醒横幅，显示在首页 TopAppBar 之下、取件码列表之上
@Composable
fun NotifySmsBanner(
    isSeniorMode: Boolean,
    onGoSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
        ) {
            // 左侧警示色边线
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFEFAA17))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "未检测到通知类短信，取件短信可能无法识别",
                    style = if (isSeniorMode) MaterialTheme.typography.headlineSmall
                    else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "其他权限 → 通知类短信 → 始终允许",
                    style = if (isSeniorMode) MaterialTheme.typography.bodyLarge
                    else MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp, vertical = 4.dp
                        ),
                        colors = ButtonColors(
                            containerColor = Color(0xFF25AF22),
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.DarkGray
                        ),
                        onClick = onGoSettings
                    ) {
                        Text(
                            text = "去开启",
                            style = if (isSeniorMode) MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.bodyLarge
                        )
                    }
                    Box(modifier = Modifier.width(12.dp)) // 按钮间距
                    TextButton(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp, vertical = 4.dp
                        ),
                        onClick = onDismiss) {
                        Text(
                            text = "不再提示",
                            style = if (isSeniorMode) MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }
    }
}
