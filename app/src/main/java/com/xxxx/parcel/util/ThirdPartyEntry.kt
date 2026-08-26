package com.xxxx.parcel.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun openTaobaoIdentityEntry(context: Context) {
    val pkg = "com.taobao.taobao"
    val lastmile =
        "https://pages-fast.m.taobao.com/wow/z/uniapp/1100333/last-mile-fe/m-end-school-tab/home"
    val candidates = listOf(
        "tbopen://m.taobao.com/tbopen/index.html?h5Url=" + Uri.encode(lastmile),
    )
    for (u in candidates) {
        try {
            val i = Intent(Intent.ACTION_VIEW, u.toUri())
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        } catch (_: Exception) {
        }
    }
    try {
        val i = Intent(Intent.ACTION_VIEW, lastmile.toUri())
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.setClassName(pkg, "com.taobao.browser.BrowserActivity")
        context.startActivity(i)
        return
    } catch (_: Exception) {
    }
}

fun openPddIdentityEntry(context: Context) {
    val pkg = "com.xunmeng.pinduoduo"
    val schemes = listOf(
        "pinduoduo://com.xunmeng.pinduoduo/mdkd/package",
        "pinduoduo://com.xunmeng.pinduoduo/",
        "pinduoduo://"
    )
    for (u in schemes) {
        try {
            val i = Intent(Intent.ACTION_VIEW, u.toUri())
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        } catch (_: Exception) {
        }
    }
    try {
        val i = context.packageManager.getLaunchIntentForPackage(pkg)
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        }
    } catch (_: Exception) {
    }
}
