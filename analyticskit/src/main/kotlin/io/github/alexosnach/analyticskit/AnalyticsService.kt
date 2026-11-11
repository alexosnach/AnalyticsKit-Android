package io.github.alexosnach.analyticskit

/** What screens and features get: they can send events and nothing else. */
interface EventTracking {
    fun track(event: AnalyticsEvent)
}

/** Says who the user is. Used in the Composition Root, not in the UI. */
interface UserIdentification {
    fun setUserID(id: HashedUserID)
    fun setUserProperties(properties: Map<String, Any>)
    fun reset()
}

/** The analytics the app uses: [EventTracking] and [UserIdentification] together. */
interface AnalyticsService : EventTracking, UserIdentification
