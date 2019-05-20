package com.msahil432.sms.feature.gallery

import com.moez.QKSMS.common.base.QkView
import com.msahil432.sms.model.MmsPart
import io.reactivex.Observable

interface GalleryView : QkView<GalleryState> {

    fun optionsItemSelected(): Observable<Int>
    fun screenTouched(): Observable<*>
    fun pageChanged(): Observable<MmsPart>

}