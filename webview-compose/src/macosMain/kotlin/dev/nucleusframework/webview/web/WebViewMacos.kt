package dev.nucleusframework.webview.web

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.nucleusframework.webview.jsbridge.WebViewJsBridge
import dev.nucleusframework.webview.setting.WebSettings
import dev.nucleusframework.webview.util.toNSColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.Foundation.setValue
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled

/**
 * macOS Native WebView implementation.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: (NativeWebView) -> Unit,
    onDispose: (NativeWebView) -> Unit,
    factory: (WebViewFactoryParam) -> NativeWebView,
    content: @Composable () -> Unit,
) {
    val observer = remember { WKWebViewObserver(state, navigator) }
    val navigationDelegate = remember { WKNavigationDelegate(state, navigator) }
    val scope = rememberCoroutineScope()

    Box(modifier) {
        remember(state) {
            val config =
                WKWebViewConfiguration().apply {
                    defaultWebpagePreferences.allowsContentJavaScript = state.webSettings.isJavaScriptEnabled
                    preferences.apply {
                        setValue(
                            state.webSettings.allowFileAccessFromFileURLs,
                            forKey = "allowFileAccessFromFileURLs",
                        )
                        javaScriptEnabled = state.webSettings.isJavaScriptEnabled
                    }
                    setValue(
                        value = state.webSettings.allowUniversalAccessFromFileURLs,
                        forKey = "allowUniversalAccessFromFileURLs",
                    )
                }

            factory(WebViewFactoryParam(config)).apply {
                onCreated(this)

                customUserAgent = state.webSettings.customUserAgentString

                addProgressObservers(observer)
                this.navigationDelegate = navigationDelegate

                applyMacosSettings(this, state.webSettings)

                val app = platform.AppKit.NSApplication.sharedApplication
                val window = app.keyWindow ?: (app.windows.firstOrNull() as? platform.AppKit.NSWindow)
                window?.contentView?.addSubview(this)
            }.also { wkWebView ->
                val macosWebView = MacosWebView(
                    nativeWebView = wkWebView,
                    scope = scope,
                    webViewJsBridge = webViewJsBridge,
                    observer = observer,
                    navigationDelegate = navigationDelegate,
                )
                state.webView = macosWebView
                webViewJsBridge?.webView = macosWebView
            }
        }
        content()
    }

    DisposableEffect(state) {
        onDispose {
            state.webView?.let { wv ->
                val wk = wv.nativeWebView
                wk.removeFromSuperview()
                wk.removeProgressObservers(observer)
                wk.configuration.userContentController.removeScriptMessageHandlerForName(
                    MACOS_JS_BRIDGE_HANDLER_NAME
                )
                wk.navigationDelegate = null
                onDispose(wk)
            }
            state.webView = null
            webViewJsBridge?.webView = null
        }
    }
}

actual data class WebViewFactoryParam(
    val config: WKWebViewConfiguration,
)

@OptIn(ExperimentalForeignApi::class)
actual fun defaultWebViewFactory(param: WebViewFactoryParam): NativeWebView =
    WKWebView(
        frame = CGRectMake(0.0, 0.0, 1024.0, 768.0),
        configuration = param.config,
    )

@OptIn(ExperimentalForeignApi::class)
private fun applyMacosSettings(webView: WKWebView, settings: WebSettings) {
    val iOSSettings = settings.iOSWebSettings
    val backgroundColor = (iOSSettings.backgroundColor ?: settings.backgroundColor).toNSColor()

    webView.setValue(value = iOSSettings.opaque, forKey = "drawsBackground")
    if (!iOSSettings.opaque) {
        webView.setValue(value = backgroundColor, forKey = "backgroundColor")
    }

    val minSetInspectableVersion =
        cValue<NSOperatingSystemVersion> {
            majorVersion = 13
            minorVersion = 3
            patchVersion = 0
        }
    if (NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(minSetInspectableVersion)) {
        webView.setInspectable(iOSSettings.isInspectable)
    }
}
