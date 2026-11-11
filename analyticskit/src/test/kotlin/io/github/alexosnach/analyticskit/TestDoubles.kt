package io.github.alexosnach.analyticskit

/** One call a provider received, in a form tests can compare. */
sealed class ProviderCall {
    data class Track(val name: String, val parameters: Map<String, Any>) : ProviderCall()
    data class SetUserID(val value: String) : ProviderCall()
    data class SetUserProperties(val properties: Map<String, Any>) : ProviderCall()
    data object Reset : ProviderCall()
}

/** Records every call, in order. */
class SpyAnalyticsProvider : AnalyticsProvider {
    val calls = mutableListOf<ProviderCall>()

    override fun track(event: AnalyticsEvent) {
        calls += ProviderCall.Track(event.name, event.parameters)
    }

    override fun setUserID(id: HashedUserID) {
        calls += ProviderCall.SetUserID(id.value)
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        calls += ProviderCall.SetUserProperties(properties)
    }

    override fun reset() {
        calls += ProviderCall.Reset
    }
}

/**
 * Keeps the ID in memory; can be told to forget every save.
 *
 * @param keepsSaves `false` acts like a storage that fails to write.
 */
class InMemoryUserIDStore(
    saved: String? = null,
    private val keepsSaves: Boolean = true,
) : UserIDStore {
    var saved: String? = saved
        private set
    var saveCount = 0
        private set

    override fun load(): String? = saved

    override fun save(id: String) {
        saveCount += 1
        if (keepsSaves) {
            saved = id
        }
    }
}
