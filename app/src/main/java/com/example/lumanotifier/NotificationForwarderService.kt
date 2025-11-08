package com.example.lumanotifier

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

object BluetoothLink {
    var send: ((String) -> Unit)? = null
}

class NotificationForwarderService : NotificationListenerService() {

    override fun onListenerConnected() {
        Log.d("Notifier", "Notification listener connected.")
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        Log.w("Notifier", "Listener disconnected. Requesting rebind.")
        requestRebind(ComponentName(this, NotificationForwarderService::class.java))
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val allowed = prefs.getStringSet("allowed_apps", emptySet()) ?: emptySet()
        if (!allowed.contains(pkg)) return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val lines = extras.getCharSequenceArray("android.textLines")

        val fullText = when {
            lines != null -> lines.joinToString("\n") { it.toString() }
            text.isNotEmpty() -> text
            else -> "(no text)"
        }

        val msg = "$pkg: $title - $fullText"
        Log.d("Notifier", msg)

        BluetoothLink.send?.invoke(msg)
    }
}
