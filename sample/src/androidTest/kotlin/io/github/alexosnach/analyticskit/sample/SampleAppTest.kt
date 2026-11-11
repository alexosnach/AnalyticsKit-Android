package io.github.alexosnach.analyticskit.sample

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.alexosnach.analyticskit.AnalyticsEvent
import io.github.alexosnach.analyticskit.AnonymousUserIDProvider
import io.github.alexosnach.analyticskit.LocalAnalytics
import io.github.alexosnach.analyticskit.NoOpAnalytics
import io.github.alexosnach.analyticskit.SHA256IdentifierHasher
import io.github.alexosnach.analyticskit.SharedPreferencesUserIDStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The real app on a device: real providers, real storage, real taps. */
@RunWith(AndroidJUnit4::class)
class SampleAppTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val app get() = rule.activity.application as SampleApp

    @Test
    fun appStartIdentifiesTheUserAndTracksAppOpened() {
        assertEquals(app.userID, app.recorder.userID)
        assertEquals("app_opened", app.recorder.events.first().name)
    }

    @Test
    fun userIDOnScreenIsTheHashOfTheStoredID() {
        val raw = SharedPreferencesUserIDStore(app).load()!!

        assertEquals(64, app.userID.value.length)
        assertEquals(SHA256IdentifierHasher().hash(raw), app.userID)
        assertTrue(raw != app.userID.value)
        rule.onNodeWithTag("user_id").assertTextEquals(app.userID.value)
    }

    @Test
    fun userIDStaysTheSameForANewProvider() {
        val again = AnonymousUserIDProvider(SharedPreferencesUserIDStore(app), SHA256IdentifierHasher())

        assertEquals(app.userID, again.currentUserID())
    }

    @Test
    fun tapOnTheButtonSendsTheEventThroughTheComposite() {
        val before = app.recorder.events.size

        rule.onNodeWithText("Track event").performClick()
        rule.onNodeWithText("Track event").performClick()

        rule.onNodeWithTag("events_count").assertTextEquals("Events sent: ${before + 2}")
        rule.onNodeWithTag("last_event").assertTextEquals("Last event: button_tapped")
        assertEquals(
            AnalyticsEvent(name = "button_tapped", parameters = mapOf("screen" to "sample")),
            app.recorder.events.last(),
        )
    }
}

@RunWith(AndroidJUnit4::class)
class LocalAnalyticsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun defaultsToNoOpAnalytics() {
        var current: Any? = null

        rule.setContent { current = LocalAnalytics.current }
        rule.waitForIdle()

        assertSame(NoOpAnalytics, current)
    }
}
