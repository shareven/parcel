package com.xxxx.parcel.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable

private fun scaledStyle(base: TextStyle, scale: Float = 1.5f): TextStyle = base.copy(
    fontSize = (base.fontSize.value * scale).sp,
    lineHeight = (base.lineHeight.value * scale).sp
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )
)

val SeniorTypography = Typography(
    displayLarge = scaledStyle(Typography.displayLarge),
    displayMedium = scaledStyle(Typography.displayMedium),
    displaySmall = scaledStyle(Typography.displaySmall),
    headlineLarge = scaledStyle(Typography.headlineLarge),
    headlineMedium = scaledStyle(Typography.headlineMedium),
    headlineSmall = scaledStyle(Typography.headlineSmall),
    titleLarge = scaledStyle(Typography.titleLarge),
    titleMedium = scaledStyle(Typography.titleMedium),
    titleSmall = scaledStyle(Typography.titleSmall),
    bodyLarge = scaledStyle(Typography.bodyLarge),
    bodyMedium = scaledStyle(Typography.bodyMedium),
    bodySmall = scaledStyle(Typography.bodySmall),
    labelLarge = scaledStyle(Typography.labelLarge),
    labelMedium = scaledStyle(Typography.labelMedium),
    labelSmall = scaledStyle(Typography.labelSmall)
)

@Composable
fun getTypography(isSeniorMode: Boolean): Typography {
    return if (isSeniorMode) SeniorTypography else Typography
}
