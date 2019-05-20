package com.msahil432.sms.injection.android

import com.msahil432.sms.feature.backup.RestoreBackupService
import com.msahil432.sms.injection.scope.ActivityScope
import com.msahil432.sms.service.HeadlessSmsSendService
import com.msahil432.sms.service.SendSmsService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindHeadlessSmsSendService(): HeadlessSmsSendService

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindRestoreBackupService(): RestoreBackupService

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSendSmsReceiver(): SendSmsService

}