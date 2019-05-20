package com.msahil432.sms.feature.settings.about

import com.msahil432.sms.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkPresenter
import com.uber.autodispose.kotlin.autoDisposable
import javax.inject.Inject

class AboutPresenter @Inject constructor(
    private val navigator: Navigator
) : QkPresenter<AboutView, Unit>(Unit) {

    override fun bindIntents(view: AboutView) {
        super.bindIntents(view)

        view.preferenceClicks()
                .autoDisposable(view.scope())
                .subscribe { preference ->
                    when (preference.id) {
                        R.id.developer -> navigator.showDeveloper()

                        R.id.source -> navigator.showSourceCode()

//                        R.id.changelog -> navigator.showChangelog()

                        R.id.contact -> navigator.showSupport()

//                        R.id.license -> navigator.showLicense()
                    }
                }
    }

}