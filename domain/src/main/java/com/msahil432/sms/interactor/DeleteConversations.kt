package com.msahil432.sms.interactor

import com.msahil432.sms.manager.NotificationManager
import com.msahil432.sms.repository.ConversationRepository
import io.reactivex.Flowable
import javax.inject.Inject

class DeleteConversations @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge
) : Interactor<List<Long>>() {

    override fun buildObservable(params: List<Long>): Flowable<*> {
        return Flowable.just(params.toLongArray())
                .doOnNext { threadIds -> conversationRepo.deleteConversations(*threadIds) }
                .doOnNext { threadIds -> threadIds.forEach{notificationManager.update(it)} }
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

}