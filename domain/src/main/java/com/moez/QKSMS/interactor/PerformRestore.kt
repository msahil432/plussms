package com.moez.QKSMS.interactor

import com.moez.QKSMS.repository.BackupRepository
import io.reactivex.Flowable
import javax.inject.Inject

class PerformRestore @Inject constructor(
    private val backupRepo: BackupRepository
) : Interactor<String>() {

    override fun buildObservable(params: String, DoNotUse: String): Flowable<*> {
        return Flowable.just(params)
                .doOnNext(backupRepo::performRestore)
    }

}