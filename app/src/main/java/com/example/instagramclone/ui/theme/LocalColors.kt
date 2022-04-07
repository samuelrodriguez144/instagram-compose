package com.example.instagramclone.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class Colors(
    val Purple200: Color =  Color(0xFFBB86FC),
    val Purple500: Color = Color(0xFF6200EE),
    val Purple700: Color = Color(0xFF3700B3),
    val Teal200: Color = Color(0xFF03DAC5)

)

val LocalColors = compositionLocalOf { Colors() }

val MaterialTheme.localColor: Colors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current