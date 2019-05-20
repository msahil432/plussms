package com.msahil432.sms.feature.blocked

import com.moez.QKSMS.common.base.QkView
import io.reactivex.Observable

interface BlockedView : QkView<BlockedState> {

    val siaClickedIntent: Observable<*>
    val unblockIntent: Observable<Long>
    val confirmUnblockIntent: Observable<Long>

    fun showUnblockDialog(threadId: Long)

}