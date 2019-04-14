package com.moez.QKSMS.interactor

import com.moez.QKSMS.manager.ShortcutManager
import com.moez.QKSMS.manager.WidgetManager
import io.reactivex.Flowable
import javax.inject.Inject

class UpdateBadge @Inject constructor(
    private val shortcutManager: ShortcutManager,
    private val widgetManager: WidgetManager
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit, category: String): Flowable<*> {
        return Flowable.just(params)
                .doOnNext { shortcutManager.updateBadge() }
                .doOnNext { widgetManager.updateUnreadCount() }
    }

}