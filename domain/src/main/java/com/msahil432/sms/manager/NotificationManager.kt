package com.msahil432.sms.manager

import androidx.core.app.NotificationCompat

interface NotificationManager {

    fun update(threadId: Long)

    fun notifyFailed(threadId: Long)

    fun createNotificationChannel(threadId: Long)

    fun buildNotificationChannelId(threadId: Long): String

    fun getNotificationForBackup(): NotificationCompat.Builder

}