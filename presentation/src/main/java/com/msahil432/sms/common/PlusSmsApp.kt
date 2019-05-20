package com.msahil432.sms.common

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.akaita.java.rxjava2debug.RxJava2Debug
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.msahil432.sms.BuildConfig
import com.msahil432.sms.R
import com.moez.QKSMS.common.util.BugsnagTree
import com.moez.QKSMS.common.util.FileLoggingTree
import com.msahil432.sms.injection.AppComponentManager
import com.msahil432.sms.injection.appComponent
import com.msahil432.sms.manager.AnalyticsManager
import com.msahil432.sms.migration.QkRealmMigration
import com.msahil432.sms.repository.SyncRepository
import com.msahil432.sms.util.NightModeManager
import com.msahil432.sms.SmsClassifier
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import javax.inject.Inject

class PlusSmsApp : Application(), HasActivityInjector, HasBroadcastReceiverInjector, HasServiceInjector {

    /**
     * Inject this so that it is forced to initialize
     */
    @Suppress("unused")
    @Inject lateinit var analyticsManager: AnalyticsManager

    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var dispatchingBroadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>
    @Inject lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>
    @Inject lateinit var fileLoggingTree: FileLoggingTree
    @Inject lateinit var nightModeManager: NightModeManager

    private var classifier : String? = null

    private val packages = arrayOf("com.msahil432.sms")

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this, Configuration("937e2453db969953603c313cf6553080").apply {
            appVersion = BuildConfig.VERSION_NAME
            projectPackages = packages
        })

        RxJava2Debug.enableRxJava2AssemblyTracking()

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .compactOnLaunch()
                .initialData {
                    syncRepository.addTrainingDataSet(it)
                }
                .schemaVersion(QkRealmMigration.SCHEMA_VERSION)
                .deleteRealmIfMigrationNeeded()
                .build())

        classifier = SmsClassifier.classify("test")

        AppComponentManager.init(this)
        appComponent.inject(this)

        packageManager.getInstallerPackageName(packageName)?.let { installer ->
            analyticsManager.setUserProperty("Installer", installer)
        }

        nightModeManager.updateCurrentTheme()

        val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)

        EmojiCompat.init(FontRequestEmojiCompatConfig(this, fontRequest))

        Timber.plant(Timber.DebugTree(), BugsnagTree(), fileLoggingTree)
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return dispatchingBroadcastReceiverInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

}