package io.github.alexosnach.analyticskit

/** Does nothing: for tests, previews and as the default of [LocalAnalytics]. */
object NoOpAnalytics : AnalyticsService {
    override fun track(event: AnalyticsEvent) {}

    override fun setUserID(id: HashedUserID) {}

    override fun setUserProperties(properties: Map<String, Any>) {}

    override fun reset() {}
}
