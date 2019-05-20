package com.msahil432.sms.injection

import com.msahil432.sms.common.PlusSmsApp

internal lateinit var appComponent: AppComponent
    private set

internal object AppComponentManager {

    fun init(application: PlusSmsApp) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .build()
    }

}