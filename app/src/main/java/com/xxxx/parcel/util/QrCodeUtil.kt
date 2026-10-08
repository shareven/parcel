package com.xxxx.parcel.util

import android.graphics.Bitmap
import android.graphics.Color
import io.nayuki.qrcodegen.QrCode

object QrCodeUtil {

    fun generateQrBitmap(content: String, size: Int): Bitmap? = try {
        val qr = QrCode.encodeText(content, QrCode.Ecc.MEDIUM)
        val border = 3
        val dimension = qr.size + border * 2
        val scale = (size + dimension - 1) / dimension
        val width = dimension * scale
        val height = dimension * scale
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val dark = qr.getModule(x / scale - border, y / scale - border)
                pixels[y * width + x] = if (dark) Color.BLACK else Color.WHITE
            }
        }
        Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    } catch (_: Exception) {
        null
    }
}
