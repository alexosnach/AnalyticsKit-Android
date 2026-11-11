# AnalyticsKit for Android

A provider-agnostic analytics layer for Android apps: one `AnalyticsService` that fans out to any number of analytics SDKs, plus a hashed, persistent user ID.

The Kotlin counterpart of [AnalyticsKit for Swift](https://github.com/alexosnach/AnalyticsKit) — same types, same names, same behaviour.

## Requirements

- Android 7.0+ (API 24)
- Built with Kotlin 2.4.20 (brings `kotlin-stdlib` 2.4.20), Java 11 bytecode
- Jetpack Compose runtime 1.7.8+ (only for `LocalAnalytics`)

## Installation

AnalyticsKit is distributed through [JitPack](https://jitpack.io/#alexosnach/AnalyticsKit-Android).

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

`app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.alexosnach:AnalyticsKit-Android:1.0.0")
}
```

Concrete SDK adapters (Amplitude, Firebase, a Logcat provider, …) are **not** part of this library — they belong to your app. See [Writing a provider](#writing-a-provider).

## Quick start

```kotlin
import io.github.alexosnach.analyticskit.*

// Composition Root, for example Application.onCreate()
val analytics: AnalyticsService = CompositeAnalytics(
    listOf(
        AmplitudeAnalyticsProvider(context, apiKey = "…"),   // your app's adapter
        LogcatAnalyticsProvider(),                            // your app's adapter
    )
)

val userIDProvider = AnonymousUserIDProvider(
    store = SharedPreferencesUserIDStore(context, onError = { Log.e("Analytics", it.toString()) }),
    hasher = SHA256IdentifierHasher(),
)
analytics.setUserID(userIDProvider.currentUserID())

// Tracking
analytics.track(AnalyticsEvent(name = "paywall_shown", parameters = mapOf("source" to "onboarding")))
```

## Jetpack Compose

AnalyticsKit adds a `LocalAnalytics` composition local (defaults to `NoOpAnalytics`, so previews stay silent):

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val analytics: AnalyticsService = /* … */
        setContent {
            CompositionLocalProvider(LocalAnalytics provides analytics) {
                RootScreen()
            }
        }
    }
}

@Composable
fun PaywallScreen() {
    val analytics = LocalAnalytics.current
    Button(onClick = { analytics.track(AnalyticsEvent(name = "subscribe_tapped")) }) {
        Text("Subscribe")
    }
}
```

Composables get only `EventTracking` (`track`); identifying the user (`UserIdentification`) stays in the Composition Root.

## Writing a provider

A provider adapts AnalyticsKit calls to one SDK. Keep it in your app module:

```kotlin
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import io.github.alexosnach.analyticskit.AnalyticsEvent
import io.github.alexosnach.analyticskit.AnalyticsProvider
import io.github.alexosnach.analyticskit.HashedUserID

class AmplitudeAnalyticsProvider(context: Context, apiKey: String) : AnalyticsProvider {
    private val amplitude = Amplitude(Configuration(apiKey = apiKey, context = context.applicationContext))

    override fun track(event: AnalyticsEvent) {
        amplitude.track(event.name, event.parameters)
    }

    override fun setUserID(id: HashedUserID) {
        amplitude.setUserId(id.value)
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        amplitude.identify(properties)
    }

    override fun reset() {
        amplitude.reset()
    }
}
```

Adding another analytics system = adding one more provider to the `CompositeAnalytics` list. Nothing else changes.

The [`sample`](sample) app has two small providers (`LogcatAnalyticsProvider`, `RecordingAnalyticsProvider`) and a full Composition Root.

## User ID

- `HashedUserID` can only be created by an `IdentifierHasher` of this library (its constructor is `internal`), so a raw ID can never reach analytics by mistake.
- `SHA256IdentifierHasher` hashes the UTF-8 bytes to lowercase hex — the same input gives the same ID as the Swift library.
- `AnonymousUserIDProvider` generates a random ID on first launch and keeps it in a `UserIDStore`.
- `SharedPreferencesUserIDStore` keeps the raw ID in a private preferences file. Storage failures are reported to `onError` and never crash the app.
- When your app gets accounts, replace the provider with one that hashes the backend ID — calling code stays the same.

### Difference from iOS

On iOS the ID lives in the Keychain and survives a reinstall. Android has no such storage: the ID is removed with the app's data. It comes back after a reinstall only through [Auto Backup](https://developer.android.com/identity/data/autobackup), which includes preferences files by default. If you need another policy, implement `UserIDStore` yourself (two methods).

## Threading

AnalyticsKit has no threads and no locks of its own: every call runs on the caller's thread and goes straight to the providers. Call it from the main thread, as the Swift library does, or make sure your providers are thread-safe.

## API

| Type | Purpose |
|---|---|
| `AnalyticsService` | `EventTracking` + `UserIdentification` — what the app uses |
| `AnalyticsProvider` | What an SDK adapter implements |
| `CompositeAnalytics` | Forwards every call to all providers, in order |
| `NoOpAnalytics` | Does nothing (tests, previews) |
| `AnalyticsEvent` | `name` + `parameters` |
| `HashedUserID`, `IdentifierHasher`, `SHA256IdentifierHasher` | Safe user IDs |
| `UserIDProvider`, `AnonymousUserIDProvider` | Where the user ID comes from |
| `UserIDStore`, `SharedPreferencesUserIDStore` | Where the raw ID is persisted |
| `LocalAnalytics` | `EventTracking` for composables |

## Running tests

```bash
./gradlew :analyticskit:testDebugUnitTest
```

On a device or an emulator (real `SharedPreferences`, the sample app with real taps):

```bash
./gradlew :analyticskit:connectedDebugAndroidTest :sample:connectedDebugAndroidTest
```

## License

MIT — see [LICENSE](LICENSE).
