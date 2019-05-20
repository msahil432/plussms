package com.msahil432.sms.injection

import com.msahil432.sms.common.PlusSmsApp
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.util.QkChooserTargetService
import com.moez.QKSMS.common.widget.AvatarView
import com.moez.QKSMS.common.widget.PagerTitleView
import com.moez.QKSMS.common.widget.PreferenceView
import com.moez.QKSMS.common.widget.QkEditText
import com.moez.QKSMS.common.widget.QkSwitch
import com.moez.QKSMS.common.widget.QkTextView
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

    fun inject(application: PlusSmsApp)

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