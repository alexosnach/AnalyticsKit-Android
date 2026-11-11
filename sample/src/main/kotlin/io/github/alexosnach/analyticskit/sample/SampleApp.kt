package io.github.alexosnach.analyticskit.sample

import android.app.Application
import android.util.Log
import io.github.alexosnach.analyticskit.AnalyticsEvent
import io.github.alexosnach.analyticskit.AnalyticsService
import io.github.alexosnach.analyticskit.AnonymousUserIDProvider
import io.github.alexosnach.analyticskit.CompositeAnalytics
import io.github.alexosnach.analyticskit.HashedUserID
import io.github.alexosnach.analyticskit.SHA256IdentifierHasher
import io.github.alexosnach.analyticskit.SharedPreferencesUserIDStore

/** The Composition Root: the one place that knows which providers the app has. */
class SampleApp : Application() {
    val recorder = RecordingAnalyticsProvider()

    lateinit var analytics: AnalyticsService
        private set
    lateinit var userID: HashedUserID
        private set

    override fun onCreate() {
        super.onCreate()

        analytics = CompositeAnalytics(
            listOf(
                LogcatAnalyticsProvider(),
                recorder,
            )
        )

        val userIDProvider = AnonymousUserIDProvider(
            store = SharedPreferencesUserIDStore(this, onError = { Log.e("Analytics", it.toString()) }),
            hasher = SHA256IdentifierHasher(),
        )
        userID = userIDProvider.currentUserID()

        analytics.setUserID(userID)
        analytics.track(AnalyticsEvent(name = "app_opened"))
    }
}
