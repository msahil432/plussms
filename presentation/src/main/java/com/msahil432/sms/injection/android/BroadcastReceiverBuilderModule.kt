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

import com.msahil432.sms.feature.widget.WidgetProvider
import com.msahil432.sms.injection.scope.ActivityScope
import com.msahil432.sms.receiver.BootReceiver
import com.msahil432.sms.receiver.DefaultSmsChangedReceiver
import com.msahil432.sms.receiver.DeleteMessagesReceiver
import com.msahil432.sms.receiver.MarkReadReceiver
import com.msahil432.sms.receiver.MarkSeenReceiver
import com.msahil432.sms.receiver.MmsReceivedReceiver
import com.msahil432.sms.receiver.MmsReceiver
import com.msahil432.sms.receiver.MmsSentReceiver
import com.msahil432.sms.receiver.MmsUpdatedReceiver
import com.msahil432.sms.receiver.NightModeReceiver
import com.msahil432.sms.receiver.RemoteMessagingReceiver
import com.msahil432.sms.receiver.SendScheduledMessageReceiver
import com.msahil432.sms.receiver.SmsDeliveredReceiver
import com.msahil432.sms.receiver.SmsProviderChangedReceiver
import com.msahil432.sms.receiver.SmsReceiver
import com.msahil432.sms.receiver.SmsSentReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindBootReceiver(): BootReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindDefaultSmsChangedReceiver(): DefaultSmsChangedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindDeleteMessagesReceiver(): DeleteMessagesReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMarkReadReceiver(): MarkReadReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMarkSeenReceiver(): MarkSeenReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsReceivedReceiver(): MmsReceivedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsReceiver(): MmsReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsSentReceiver(): MmsSentReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsUpdatedReceiver(): MmsUpdatedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindNightModeReceiver(): NightModeReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindRemoteMessagingReceiver(): RemoteMessagingReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSendScheduledMessageReceiver(): SendScheduledMessageReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsDeliveredReceiver(): SmsDeliveredReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsProviderChangedReceiver(): SmsProviderChangedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsReceiver(): SmsReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsSentReceiver(): SmsSentReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindWidgetProvider(): WidgetProvider

}