package com.xxxx.parcel.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xxxx.parcel.util.QrCodeUtil
import com.xxxx.parcel.util.ShareData

@Composable
fun ShareCard(context: Context) {
    val appIcon = remember { loadAppIconBitmap(context) }
    val githubQr = remember { QrCodeUtil.generateQrBitmap(ShareData.GITHUB_RELEASE_URL, 600) }
    val weiyunQr = remember { QrCodeUtil.generateQrBitmap(ShareData.WEIYUN_URL, 600) }

    // 暗色模式下卡片换成深色背景，二维码本身保持黑白不变，不影响扫码
    val darkTheme = isSystemInDarkTheme()
    val bgColor = if (darkTheme) Color(0xFF262626) else Color.White
    val titleColor = if (darkTheme) Color(0xFFEAEAEA) else Color(0xFF212121)
    val subtitleColor = if (darkTheme) Color(0xFFA8A8A8) else Color(0xFF616161)
    val footerColor = if (darkTheme) Color(0xFF8A8A8A) else Color(0xFF9E9E9E)
    val labelColor = if (darkTheme) Color(0xFFD6D6D6) else Color(0xFF424242)
    val borderColor = if (darkTheme) Color(0xFF404040) else Color(0xFFE0E0E0)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                appIcon?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = "Parcel - 取件码",
                    color = titleColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "自动提取快递取件码显示到桌面卡片",
                color = subtitleColor,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QrItem(qr = githubQr, label = "扫码下载（GitHub）", color = labelColor)
                QrItem(qr = weiyunQr, label = "备用渠道（微云）", color = labelColor)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "github.com/shareven/parcel",
                color = footerColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun QrItem(qr: Bitmap?, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        qr?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.size(120.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun loadAppIconBitmap(context: Context): Bitmap? = try {
    val drawable = context.packageManager.getApplicationIcon(context.packageName)
    if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val size = 256
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bmp ->
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
        }
    }
} catch (_: Exception) {
    null
}
