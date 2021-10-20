package com.whatsapp.chattema.app

import com.onesignal.NotificationExtenderService
import com.onesignal.OSNotificationReceivedResult
import com.whatsapp.chattema.extensions.context.preferences

class NotificationService : NotificationExtenderService() {
    override fun onNotificationProcessing(notification: OSNotificationReceivedResult?): Boolean =
        !preferences.notificationsEnabled
}
