package com.xxxx.parcel.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.xxxx.parcel.ui.components.ShareCard
import com.xxxx.parcel.util.ShareData
import com.xxxx.parcel.util.ThirdPartyDefaults

fun getAppVersionName(context: Context): String {
    try {
        // 获取 PackageManager 实例
        val packageManager = context.packageManager
        // 获取当前应用的包名
        val packageName = context.packageName
        // 获取应用信息，包含版本号等
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        // 返回版本名称
        return ("版本：" + packageInfo.versionName)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return "未知版本"
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}

@Composable
private fun FeatureTag(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

@Composable
private fun FaqItem(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val url = "https://github.com/shareven/parcel"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于与分享") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
    ) {
        innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShareCard(context)

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureTag("免费开源")
                FeatureTag("无广告")
                FeatureTag("不联网")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "截图上面的卡片发给朋友，或点击按钮分享",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, ShareData.SHARE_TEXT)
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
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, ShareData.SHARE_TEXT)
                        }
                        context.startActivity(Intent.createChooser(intent, "分享 Parcel 取件码"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("系统分享")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("下载与更新")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { openUrl(context, ShareData.GITHUB_RELEASE_URL) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GitHub 下载（推荐）")
                }
                OutlinedButton(
                    onClick = { openUrl(context, ShareData.WEIYUN_URL) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("微云备用下载")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = getAppVersionName(context),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            SectionTitle("开源地址")
            TextButton(
                onClick = { openUrl(context, url) }
            ) {
                Text(url, color = Color(0XFF6200EE))
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("功能特性")
            Column(modifier = Modifier.fillMaxWidth()) {
                FeatureItem("免费开源，无广告，不联网，不收集任何个人信息")
                FeatureItem("自动解析收到的短信，提取地址和取件码")
                FeatureItem("取件码展示到桌面卡片，支持暗色模式")
                FeatureItem("支持添加自定义规则，改进解析效果")
                FeatureItem("长按取件码可添加备注、分享")
                FeatureItem("支持淘宝身份码和拼多多身份码")
                FeatureItem("监听第三方 App 通知，自动保存取件码消息，帮微信朋友取快递更方便")
                FeatureItem("开启通知监听权限，后台进程保活，实时更新桌面卡片")
                FeatureItem("支持地址归类，把多个地址加入同一地址标签，方便查看")
                FeatureItem("支持老人模式，大字体，方便老年人使用")
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("使用问题")
            Column(modifier = Modifier.fillMaxWidth()) {
                FaqItem(
                    title = "桌面卡片添加",
                    body = "一般藏在全部卡片-最底部的插件或者安卓小组件里面"
                )
                FaqItem(
                    title = "下载困难",
                    body = "如果 GitHub 下载不了，请点击上方「微云备用下载」"
                )
                FaqItem(
                    title = "小米手机权限",
                    body = "要打开通知类短信权限【权限管理→其他权限→通知类短信→始终允许】"
                )
                FaqItem(
                    title = "无法识别的短信",
                    body = "没有发送者号码的网络短信目前识别不了：\n· 第一种办法(推荐)：去短信设置里关闭【服务信息】或【5G信息】（通过移动数据或WLAN接收商家信息），设置不接收这种短信\n· 第二种办法(进程被杀时不可用)：开启监听第三方app功能，设置并监听网络短信通知，自动保存通知里的取件码信息到自定义取件短信"
                )
                FaqItem(
                    title = "桌面卡片不更新",
                    body = "可能是后台进程被杀了。尝试打开监听通知权限，有助于实现后台进程保活，实时更新桌面卡片。在系统【自启动】里面添加app，在耗电管理里设置【不限制应用的后台行为】，然后重新添加桌面卡片"
                )
                FaqItem(
                    title = "没有收到短信",
                    body = "不发短信的话，可以复制取件码或短信，点击 +号 自动粘贴导入。或者找快递客服，让他把取件码通知方式改成短信通知"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(modifier = Modifier.width(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "有问题或建议欢迎提 issue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
