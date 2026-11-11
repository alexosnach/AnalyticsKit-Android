package io.github.alexosnach.analyticskit.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.alexosnach.analyticskit.AnalyticsEvent
import io.github.alexosnach.analyticskit.LocalAnalytics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SampleApp

        setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalAnalytics provides app.analytics) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        SampleScreen(userID = app.userID.value, events = app.recorder.events)
                    }
                }
            }
        }
    }
}

@Composable
fun SampleScreen(userID: String, events: List<AnalyticsEvent>) {
    // The screen gets only `EventTracking`: it can send events and nothing else.
    val analytics = LocalAnalytics.current

    Column(
        modifier = Modifier.safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("User ID", style = MaterialTheme.typography.labelLarge)
        Text(userID, modifier = Modifier.testTag("user_id"))

        Button(
            onClick = {
                analytics.track(
                    AnalyticsEvent(name = "button_tapped", parameters = mapOf("screen" to "sample"))
                )
            },
        ) {
            Text("Track event")
        }

        Text("Events sent: ${events.size}", modifier = Modifier.testTag("events_count"))
        Text("Last event: ${events.lastOrNull()?.name ?: "—"}", modifier = Modifier.testTag("last_event"))
    }
}
