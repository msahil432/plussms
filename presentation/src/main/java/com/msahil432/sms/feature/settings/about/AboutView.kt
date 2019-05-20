package com.msahil432.sms.feature.settings.about

import com.moez.QKSMS.common.base.QkViewContract
import com.moez.QKSMS.common.widget.PreferenceView
import io.reactivex.Observable

interface AboutView : QkViewContract<Unit> {

    fun preferenceClicks(): Observable<PreferenceView>

}