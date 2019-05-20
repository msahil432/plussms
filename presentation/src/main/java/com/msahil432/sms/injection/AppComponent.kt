/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.msahil432.sms.injection

import com.msahil432.sms.common.QKApplication
import com.msahil432.sms.common.QkDialog
import com.msahil432.sms.common.util.QkChooserTargetService
import com.msahil432.sms.common.widget.AvatarView
import com.msahil432.sms.common.widget.PagerTitleView
import com.msahil432.sms.common.widget.PreferenceView
import com.msahil432.sms.common.widget.QkEditText
import com.msahil432.sms.common.widget.QkSwitch
import com.msahil432.sms.common.widget.QkTextView
import com.msahil432.sms.feature.backup.BackupController
import com.msahil432.sms.feature.compose.DetailedChipView
import com.msahil432.sms.feature.conversationinfo.injection.ConversationInfoComponent
import com.msahil432.sms.feature.settings.SettingsController
import com.msahil432.sms.feature.settings.about.AboutController
import com.msahil432.sms.feature.settings.swipe.SwipeActionsController
import com.msahil432.sms.feature.themepicker.injection.ThemePickerComponent
import com.msahil432.sms.feature.widget.WidgetAdapter
import com.msahil432.sms.injection.android.ActivityBuilderModule
import com.msahil432.sms.injection.android.BroadcastReceiverBuilderModule
import com.msahil432.sms.injection.android.ServiceBuilderModule
import com.msahil432.sms.util.ContactImageLoader
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivityBuilderModule::class,
    BroadcastReceiverBuilderModule::class,
    ServiceBuilderModule::class])
interface AppComponent {

    fun conversationInfoBuilder(): ConversationInfoComponent.Builder
    fun themePickerBuilder(): ThemePickerComponent.Builder

    fun inject(application: QKApplication)

    fun inject(controller: AboutController)
    fun inject(controller: BackupController)
    fun inject(controller: SettingsController)
    fun inject(controller: SwipeActionsController)

    fun inject(dialog: QkDialog)

    fun inject(fetcher: ContactImageLoader.ContactImageFetcher)

    fun inject(service: WidgetAdapter)

    /**
     * This can't use AndroidInjection, or else it will crash on pre-marshmallow devices
     */
    fun inject(service: QkChooserTargetService)

    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: PagerTitleView)
    fun inject(view: PreferenceView)
    fun inject(view: QkEditText)
    fun inject(view: QkSwitch)
    fun inject(view: QkTextView)

}