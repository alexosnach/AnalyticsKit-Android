package io.github.alexosnach.analyticskit

/**
 * One analytics system plugged into the app: Amplitude, Firebase, Logcat.
 *
 * Only [CompositeAnalytics] calls it; the rest of the app uses
 * [AnalyticsService]. The two interfaces have the same methods today, but
 * they serve different callers, so each can grow without touching the other.
 *
 * An implementation adapts these calls to its SDK: names, value types and
 * limits of the SDK stay inside that one file.
 */
interface AnalyticsProvider {
    fun track(event: AnalyticsEvent)
    fun setUserID(id: HashedUserID)
    fun setUserProperties(properties: Map<String, Any>)
    fun reset()
}
