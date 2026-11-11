package io.github.alexosnach.analyticskit

/** One thing that happened in the app: a name and the values that describe it. */
data class AnalyticsEvent(
    val name: String,
    val parameters: Map<String, Any> = emptyMap(),
)
