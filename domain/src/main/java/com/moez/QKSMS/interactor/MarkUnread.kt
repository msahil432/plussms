package com.moez.QKSMS.interactor

import com.moez.QKSMS.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkUnread @Inject constructor(
    private val messageRepo: MessageRepository,
    private val updateBadge: UpdateBadge
) : Interactor<List<Long>>() {

    override fun buildObservable(params: List<Long>, category: String): Flowable<*> {
        return Flowable.just(params.toLongArray())
                .doOnNext { threadId -> messageRepo.markUnread(*threadId, category = category) }
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

}