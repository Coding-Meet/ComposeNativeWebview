package dev.nucleusframework.webview.util

import androidx.compose.ui.graphics.Color
import platform.AppKit.NSColor

internal fun Color.toNSColor(): NSColor =
    NSColor.colorWithSRGBRed(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble(),
    )
