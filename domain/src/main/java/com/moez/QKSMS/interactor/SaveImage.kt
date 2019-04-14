package com.moez.QKSMS.interactor

import com.moez.QKSMS.repository.ImageRepository
import com.moez.QKSMS.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SaveImage @Inject constructor(
    private val imageRepository: ImageRepository,
    private val messageRepo: MessageRepository
) : Interactor<Long>() {

    override fun buildObservable(params: Long, DoNotUse: String): Flowable<*> {
        return Flowable.just(params)
                .map { partId -> messageRepo.getPart(partId) }
                .map { part -> part.getUri() }
                .doOnNext { uri -> imageRepository.saveImage(uri) }
    }

}