package com.xxxx.parcel.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val TextColor = Color(0xFF222222)
val TextColorAAA = Color(0xFFAAAAAA)
val TextColorWhite = Color(0xFFFFFFFF)

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Purple80,
    onPrimaryContainer = Color.Black,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    onSecondary = Color.Gray,
    onTertiary = Color.Black,
    background = Color.Transparent,
    onBackground = TextColorAAA,
    surface = Color.Transparent,
    onSurface = TextColorWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Purple80,
    onPrimaryContainer = Color.Black,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    onSecondary = Color.Gray,
    onTertiary = Color.White,
    background = Color.Transparent,
    onBackground = TextColor,
    surface = Color.Transparent,
    onSurface = TextColor

)

@Composable
fun ParcelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    gradientBrush: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFCEB6F6), Color(0xFFF6F3F4), Color(0xFFF6C8D8)) // 定义渐变颜色
    ),
    gradientBrushDark: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFF2C105E), Color(0xFF020202), Color(0xFF590E26)) // 定义渐变颜色
    ),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val backgroundGradient = when {
        darkTheme -> gradientBrushDark
        else -> gradientBrush
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundGradient)) {
                content()
            }
        }
    )
}