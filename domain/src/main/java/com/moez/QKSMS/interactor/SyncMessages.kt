package com.moez.QKSMS.interactor

import com.moez.QKSMS.repository.SyncRepository
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncMessages @Inject constructor(
    private val syncManager: SyncRepository,
    private val updateBadge: UpdateBadge
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit, DoNotUse: String): Flowable<*> {
        return Flowable.just(System.currentTimeMillis())
                .doOnNext { syncManager.syncMessages() }
                .map { startTime -> System.currentTimeMillis() - startTime }
                .map { elapsed -> TimeUnit.MILLISECONDS.toSeconds(elapsed) }
                .doOnNext { seconds -> Timber.v("Completed sync in $seconds seconds") }
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

}