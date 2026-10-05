package dev.nucleusframework.webview.e2e

import platform.Foundation.NSProcessInfo

internal actual fun platformInfoJson(): String {
    val osVersion = NSProcessInfo.processInfo.operatingSystemVersionString
    return """{"platform":"macos","os":"macOS","osVersion":"$osVersion","device":"Mac"}"""
}
