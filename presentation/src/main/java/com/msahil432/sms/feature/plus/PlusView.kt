package com.msahil432.sms.feature.plus

import com.moez.QKSMS.common.base.QkView
import com.moez.QKSMS.common.util.BillingManager
import io.reactivex.Observable

interface PlusView : QkView<PlusState> {

    val upgradeIntent: Observable<Unit>
    val upgradeDonateIntent: Observable<Unit>
    val donateIntent: Observable<*>

    fun initiatePurchaseFlow(billingManager: BillingManager, sku: String)

}