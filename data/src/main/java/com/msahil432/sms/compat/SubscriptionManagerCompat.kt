package com.msahil432.sms.compat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import com.msahil432.sms.manager.PermissionManager
import javax.inject.Inject

class SubscriptionManagerCompat @Inject constructor(context: Context, private val permissions: PermissionManager) {

    private val subscriptionManager: SubscriptionManager?
        get() = field?.takeIf { permissions.hasPhone() }

    val activeSubscriptionInfoList: List<SubscriptionInfoCompat>
        @SuppressLint("MissingPermission")
        get() {
            return if (Build.VERSION.SDK_INT >= 22) {
                subscriptionManager?.activeSubscriptionInfoList?.map { SubscriptionInfoCompat(it) } ?: listOf()
            } else listOf()
        }

    init {
        subscriptionManager = if (Build.VERSION.SDK_INT >= 22) SubscriptionManager.from(context) else null
    }

    fun addOnSubscriptionsChangedListener(listener: OnSubscriptionsChangedListener) {
        if (Build.VERSION.SDK_INT >= 22) {
            subscriptionManager?.addOnSubscriptionsChangedListener(listener.listener)
        }
    }

    fun removeOnSubscriptionsChangedListener(listener: OnSubscriptionsChangedListener) {
        if (Build.VERSION.SDK_INT >= 22) {
            subscriptionManager?.removeOnSubscriptionsChangedListener(listener.listener)
        }
    }

    abstract class OnSubscriptionsChangedListener {

        val listener: SubscriptionManager.OnSubscriptionsChangedListener? = if (Build.VERSION.SDK_INT >= 22) {
            object : SubscriptionManager.OnSubscriptionsChangedListener() {
                override fun onSubscriptionsChanged() {
                    this@OnSubscriptionsChangedListener.onSubscriptionsChanged()
                }
            }
        } else null

        abstract fun onSubscriptionsChanged()

    }

}