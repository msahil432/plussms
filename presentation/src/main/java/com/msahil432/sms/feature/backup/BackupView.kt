package com.msahil432.sms.feature.backup

import com.moez.QKSMS.common.base.QkViewContract
import com.msahil432.sms.model.BackupFile
import io.reactivex.Observable

interface BackupView : QkViewContract<BackupState> {

    fun activityVisible(): Observable<*>
    fun restoreClicks(): Observable<*>
    fun restoreFileSelected(): Observable<BackupFile>
    fun restoreConfirmed(): Observable<*>
    fun stopRestoreClicks(): Observable<*>
    fun stopRestoreConfirmed(): Observable<*>
    fun fabClicks(): Observable<*>

    fun requestStoragePermission()
    fun selectFile()
    fun confirmRestore()
    fun stopRestore()

}