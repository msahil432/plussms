package com.msahil432.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.msahil432.sms.interactor.MarkRead
import com.msahil432.sms.common.JavaHelper
import dagger.android.AndroidInjection
import javax.inject.Inject

class MarkReadReceiver : BroadcastReceiver() {

    @Inject lateinit var markRead: MarkRead

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)
        val otp = intent.getStringExtra("otp")
        if(otp!=null && otp!="")
            JavaHelper.copyToClipboard(context, otp)
        markRead.execute(listOf(threadId)) { pendingResult.finish() }
    }

}