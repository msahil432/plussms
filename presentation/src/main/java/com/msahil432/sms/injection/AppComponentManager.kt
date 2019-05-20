package com.msahil432.sms.injection

import com.msahil432.sms.common.QKApplication

internal lateinit var appComponent: AppComponent
    private set

internal object AppComponentManager {

    fun init(application: QKApplication) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .build()
    }

}