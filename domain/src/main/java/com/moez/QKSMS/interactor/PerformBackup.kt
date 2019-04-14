package com.moez.QKSMS.interactor

import com.moez.QKSMS.repository.BackupRepository
import io.reactivex.Flowable
import javax.inject.Inject

class PerformBackup @Inject constructor(
    private val backupRepo: BackupRepository
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit, DoNotUse: String): Flowable<*> {
        return Flowable.just(params)
                .doOnNext { backupRepo.performBackup() }
    }

}