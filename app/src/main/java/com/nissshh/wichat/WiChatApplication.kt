package com.nissshh.wichat

import android.app.Application
import androidx.room.Room
import com.nissshh.wichat.core.database.WiChatDatabase
import com.nissshh.wichat.core.datastore.PreferencesRepository
import com.nissshh.wichat.core.repository.WiChatRepository
import com.nissshh.wichat.core.repository.WiChatRepositoryImpl

class WiChatApplication : Application() {

    lateinit var database: WiChatDatabase
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var repository: WiChatRepository
        private set

    companion object {
        var isAppInForeground: Boolean = false
            private set
    }

    private var startedActivityCount = 0

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : android.app.Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
            override fun onActivityStarted(activity: android.app.Activity) {
                startedActivityCount++
                isAppInForeground = startedActivityCount > 0
            }
            override fun onActivityResumed(activity: android.app.Activity) {}
            override fun onActivityPaused(activity: android.app.Activity) {}
            override fun onActivityStopped(activity: android.app.Activity) {
                startedActivityCount--
                isAppInForeground = startedActivityCount > 0
            }
            override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: android.app.Activity) {}
        })

        database = Room.databaseBuilder(
            applicationContext,
            WiChatDatabase::class.java,
            "wichat_local_db"
        ).fallbackToDestructiveMigration().build()

        preferencesRepository = PreferencesRepository(applicationContext)
        repository = WiChatRepositoryImpl(applicationContext, database)
        com.nissshh.wichat.core.notification.WiChatNotificationHelper.createNotificationChannels(this)
    }
}

