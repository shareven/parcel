package com.xxxx.parcel.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtil {
    fun hasSmsPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }
}