package io.github.alexosnach.analyticskit

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The analytics composables use. Defaults to [NoOpAnalytics], so previews
 * stay silent.
 *
 * It gives only [EventTracking]: identifying the user stays in the
 * Composition Root.
 *
 * ```kotlin
 * CompositionLocalProvider(LocalAnalytics provides analytics) { App() }
 *
 * val analytics = LocalAnalytics.current
 * Button(onClick = { analytics.track(AnalyticsEvent("subscribe_tapped")) }) { … }
 * ```
 */
val LocalAnalytics = staticCompositionLocalOf<EventTracking> { NoOpAnalytics }
