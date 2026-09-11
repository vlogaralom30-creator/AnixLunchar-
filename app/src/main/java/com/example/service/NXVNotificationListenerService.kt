package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NXVNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        updateNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) {
            instance = null
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateNotifications()
    }

    fun removeNotificationByKey(key: String) {
        try {
            cancelNotification(key)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateNotifications() {
        try {
            val active = activeNotifications ?: emptyArray()
            _activeSbnList.value = active.toList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var instance: NXVNotificationListenerService? = null
            private set

        private val _activeSbnList = MutableStateFlow<List<StatusBarNotification>>(emptyList())
        val activeSbnList: StateFlow<List<StatusBarNotification>> = _activeSbnList.asStateFlow()
    }
}
