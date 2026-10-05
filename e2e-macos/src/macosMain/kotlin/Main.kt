import androidx.compose.ui.window.Window
import dev.nucleusframework.webview.e2e.visualsuite.VisualSuiteApp
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSApplicationDelegateProtocol
import platform.darwin.NSObject

fun main() {
    val nsApplication = NSApplication.sharedApplication()
    nsApplication.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    nsApplication.delegate = object : NSObject(), NSApplicationDelegateProtocol {
        override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean {
            return true
        }
    }
    Window("Compose macOS demo") {
        VisualSuiteApp()
    }
    nsApplication.run()
}
