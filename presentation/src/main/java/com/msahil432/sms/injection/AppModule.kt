package com.msahil432.sms.injection

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProvider
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.moez.QKSMS.common.ViewModelFactory
import com.moez.QKSMS.common.util.NotificationManagerImpl
import com.moez.QKSMS.common.util.ShortcutManagerImpl
import com.msahil432.sms.feature.conversationinfo.injection.ConversationInfoComponent
import com.msahil432.sms.feature.themepicker.injection.ThemePickerComponent
import com.msahil432.sms.listener.ContactAddedListener
import com.msahil432.sms.listener.ContactAddedListenerImpl
import com.msahil432.sms.manager.ActiveConversationManager
import com.msahil432.sms.manager.ActiveConversationManagerImpl
import com.msahil432.sms.manager.AlarmManager
import com.msahil432.sms.manager.AlarmManagerImpl
import com.msahil432.sms.manager.AnalyticsManager
import com.msahil432.sms.manager.AnalyticsManagerImpl
import com.msahil432.sms.manager.ExternalBlockingManager
import com.msahil432.sms.manager.ExternalBlockingManagerImpl
import com.msahil432.sms.manager.KeyManager
import com.msahil432.sms.manager.KeyManagerImpl
import com.msahil432.sms.manager.NotificationManager
import com.msahil432.sms.manager.PermissionManager
import com.msahil432.sms.manager.PermissionManagerImpl
import com.msahil432.sms.manager.RatingManager
import com.msahil432.sms.manager.ShortcutManager
import com.msahil432.sms.manager.WidgetManager
import com.msahil432.sms.manager.WidgetManagerImpl
import com.msahil432.sms.mapper.CursorToContact
import com.msahil432.sms.mapper.CursorToContactImpl
import com.msahil432.sms.mapper.CursorToConversation
import com.msahil432.sms.mapper.CursorToConversationImpl
import com.msahil432.sms.mapper.CursorToMessage
import com.msahil432.sms.mapper.CursorToMessageImpl
import com.msahil432.sms.mapper.CursorToPart
import com.msahil432.sms.mapper.CursorToPartImpl
import com.msahil432.sms.mapper.CursorToRecipient
import com.msahil432.sms.mapper.CursorToRecipientImpl
import com.msahil432.sms.mapper.RatingManagerImpl
import com.msahil432.sms.repository.BackupRepository
import com.msahil432.sms.repository.BackupRepositoryImpl
import com.msahil432.sms.repository.ContactRepository
import com.msahil432.sms.repository.ContactRepositoryImpl
import com.msahil432.sms.repository.ConversationRepository
import com.msahil432.sms.repository.ConversationRepositoryImpl
import com.msahil432.sms.repository.ImageRepository
import com.msahil432.sms.repository.ImageRepostoryImpl
import com.msahil432.sms.repository.MessageRepository
import com.msahil432.sms.repository.MessageRepositoryImpl
import com.msahil432.sms.repository.ScheduledMessageRepository
import com.msahil432.sms.repository.ScheduledMessageRepositoryImpl
import com.msahil432.sms.repository.SyncRepository
import com.msahil432.sms.repository.SyncRepositoryImpl
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(subcomponents = [
    ConversationInfoComponent::class,
    ThemePickerComponent::class])
class AppModule(private var application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideRxPreferences(context: Context): RxSharedPreferences {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return RxSharedPreferences.create(preferences)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    fun provideViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory = factory

    // Listener

    @Provides
    fun provideContactAddedListener(listener: ContactAddedListenerImpl): ContactAddedListener = listener

    // Manager

    @Provides
    fun provideActiveConversationManager(manager: ActiveConversationManagerImpl): ActiveConversationManager = manager

    @Provides
    fun provideAlarmManager(manager: AlarmManagerImpl): AlarmManager = manager

    @Provides
    fun provideAnalyticsManager(manager: AnalyticsManagerImpl): AnalyticsManager = manager

    @Provides
    fun externalBlockingManager(manager: ExternalBlockingManagerImpl): ExternalBlockingManager = manager

    @Provides
    fun provideKeyManager(manager: KeyManagerImpl): KeyManager = manager

    @Provides
    fun provideNotificationsManager(manager: NotificationManagerImpl): NotificationManager = manager

    @Provides
    fun providePermissionsManager(manager: PermissionManagerImpl): PermissionManager = manager

    @Provides
    fun provideRatingManager(manager: RatingManagerImpl): RatingManager = manager

    @Provides
    fun provideShortcutManager(manager: ShortcutManagerImpl): ShortcutManager = manager

    @Provides
    fun provideWidgetManager(manager: WidgetManagerImpl): WidgetManager = manager


    // Mapper

    @Provides
    fun provideCursorToContact(mapper: CursorToContactImpl): CursorToContact = mapper

    @Provides
    fun provideCursorToConversation(mapper: CursorToConversationImpl): CursorToConversation = mapper

    @Provides
    fun provideCursorToMessage(mapper: CursorToMessageImpl): CursorToMessage = mapper

    @Provides
    fun provideCursorToPart(mapper: CursorToPartImpl): CursorToPart = mapper

    @Provides
    fun provideCursorToRecipient(mapper: CursorToRecipientImpl): CursorToRecipient = mapper

    // Repository
    @Provides
    fun provideBackupRepository(repository: BackupRepositoryImpl): BackupRepository = repository

    @Provides
    fun provideContactRepository(repository: ContactRepositoryImpl): ContactRepository = repository

    @Provides
    fun provideConversationRepository(repository: ConversationRepositoryImpl): ConversationRepository = repository

    @Provides
    fun provideImageRepository(repository: ImageRepostoryImpl): ImageRepository = repository

    @Provides
    fun provideMessageRepository(repository: MessageRepositoryImpl): MessageRepository = repository

    @Provides
    fun provideScheduledMessagesRepository(repository: ScheduledMessageRepositoryImpl): ScheduledMessageRepository = repository

    @Provides
    fun provideSyncRepository(repository: SyncRepositoryImpl): SyncRepository = repository

}