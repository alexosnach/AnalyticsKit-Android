package io.github.alexosnach.analyticskit

/**
 * The analytics the whole app uses: one [AnalyticsService] that forwards
 * every call to all its providers, in their order.
 *
 * A new analytics system is a new provider in the list the Composition Root
 * builds; this type and the code that calls it do not change.
 *
 * ```kotlin
 * val analytics: AnalyticsService = CompositeAnalytics(
 *     listOf(
 *         AmplitudeAnalyticsProvider(apiKey = "…"),
 *         LogcatAnalyticsProvider(),
 *     )
 * )
 * ```
 */
class CompositeAnalytics(providers: List<AnalyticsProvider>) : AnalyticsService {
    // A copy: a list the caller changes later does not change this one.
    private val providers: List<AnalyticsProvider> = providers.toList()

    override fun track(event: AnalyticsEvent) {
        providers.forEach { it.track(event) }
    }

    override fun setUserID(id: HashedUserID) {
        providers.forEach { it.setUserID(id) }
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        providers.forEach { it.setUserProperties(properties) }
    }

    override fun reset() {
        providers.forEach { it.reset() }
    }
}
