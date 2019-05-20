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
package com.msahil432.sms.injection.android

import com.msahil432.sms.feature.backup.BackupActivity
import com.msahil432.sms.feature.blocked.BlockedActivity
import com.msahil432.sms.feature.blocked.BlockedActivityModule
import com.msahil432.sms.feature.compose.ComposeActivity
import com.msahil432.sms.feature.compose.ComposeActivityModule
import com.msahil432.sms.feature.conversationinfo.ConversationInfoActivity
import com.msahil432.sms.feature.gallery.GalleryActivity
import com.msahil432.sms.feature.gallery.GalleryActivityModule
import com.msahil432.sms.feature.main.MainActivity
import com.msahil432.sms.feature.main.MainActivityModule
import com.msahil432.sms.feature.notificationprefs.NotificationPrefsActivity
import com.msahil432.sms.feature.notificationprefs.NotificationPrefsActivityModule
import com.msahil432.sms.feature.plus.PlusActivity
import com.msahil432.sms.feature.plus.PlusActivityModule
import com.msahil432.sms.feature.qkreply.QkReplyActivity
import com.msahil432.sms.feature.qkreply.QkReplyActivityModule
import com.msahil432.sms.feature.scheduled.ScheduledActivity
import com.msahil432.sms.feature.scheduled.ScheduledActivityModule
import com.msahil432.sms.feature.settings.SettingsActivity
import com.msahil432.sms.injection.scope.ActivityScope
import com.msahil432.sms.CategorizationActivity
import com.msahil432.sms.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PlusActivityModule::class])
    abstract fun bindPlusActivity(): PlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBackupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ComposeActivityModule::class])
    abstract fun bindComposeActivity(): ComposeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSplashActivity(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindCategorizationActivity(): CategorizationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindConversationInfoActivity(): ConversationInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [GalleryActivityModule::class])
    abstract fun bindGalleryActivity(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationPrefsActivityModule::class])
    abstract fun bindNotificationPrefsActivity(): NotificationPrefsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [QkReplyActivityModule::class])
    abstract fun bindQkReplyActivity(): QkReplyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ScheduledActivityModule::class])
    abstract fun bindScheduledActivity(): ScheduledActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BlockedActivityModule::class])
    abstract fun bindBlockedActivity(): BlockedActivity

}