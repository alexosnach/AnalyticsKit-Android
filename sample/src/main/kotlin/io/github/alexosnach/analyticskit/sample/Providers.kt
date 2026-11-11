package io.github.alexosnach.analyticskit.sample

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import io.github.alexosnach.analyticskit.AnalyticsEvent
import io.github.alexosnach.analyticskit.AnalyticsProvider
import io.github.alexosnach.analyticskit.HashedUserID

// Providers belong to the app, not to AnalyticsKit. A real app would have
// an Amplitude or a Firebase provider here, written the same way.

/** Prints every call to Logcat under the `Analytics` tag. */
class LogcatAnalyticsProvider : AnalyticsProvider {
    override fun track(event: AnalyticsEvent) {
        Log.d(TAG, "track ${event.name} ${event.parameters}")
    }

    override fun setUserID(id: HashedUserID) {
        Log.d(TAG, "setUserID ${id.value}")
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        Log.d(TAG, "setUserProperties $properties")
    }

    override fun reset() {
        Log.d(TAG, "reset")
    }

    private companion object {
        const val TAG = "Analytics"
    }
}

/** Keeps the events it received, so the screen can show them. */
class RecordingAnalyticsProvider : AnalyticsProvider {
    val events = mutableStateListOf<AnalyticsEvent>()
    var userID: HashedUserID? = null
        private set

    override fun track(event: AnalyticsEvent) {
        events += event
    }

    override fun setUserID(id: HashedUserID) {
        userID = id
    }

    override fun setUserProperties(properties: Map<String, Any>) {}

    override fun reset() {
        events.clear()
        userID = null
    }
}
