package com.msahil432.sms.interactor

import com.msahil432.sms.repository.ConversationRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkBlocked @Inject constructor(private val conversationRepo: ConversationRepository) : Interactor<List<Long>>() {

    override fun buildObservable(params: List<Long>): Flowable<*> {
        return Flowable.just(params.toLongArray())
                .doOnNext { threadIds -> conversationRepo.markBlocked(*threadIds) }
    }

}