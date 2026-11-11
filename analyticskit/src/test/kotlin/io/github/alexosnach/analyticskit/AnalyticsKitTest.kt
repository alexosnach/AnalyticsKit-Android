package io.github.alexosnach.analyticskit

import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsEventTest {
    @Test
    fun hasNoParametersByDefault() {
        val event = AnalyticsEvent(name = "app_opened")

        assertEquals("app_opened", event.name)
        assertTrue(event.parameters.isEmpty())
    }

    @Test
    fun keepsTheGivenParameters() {
        val event = AnalyticsEvent(name = "paywall_shown", parameters = mapOf("source" to "onboarding"))

        assertEquals("onboarding", event.parameters["source"])
    }
}

class CompositeAnalyticsTest {
    private val hasher = SHA256IdentifierHasher()

    @Test
    fun sendsEveryCallToEveryProviderInOrder() {
        val first = SpyAnalyticsProvider()
        val second = SpyAnalyticsProvider()
        val analytics = CompositeAnalytics(listOf(first, second))
        val id = hasher.hash("user")

        analytics.track(AnalyticsEvent(name = "app_opened", parameters = mapOf("n" to 1)))
        analytics.setUserID(id)
        analytics.setUserProperties(mapOf("is_premium" to true))
        analytics.reset()

        val expected = listOf(
            ProviderCall.Track(name = "app_opened", parameters = mapOf("n" to 1)),
            ProviderCall.SetUserID(id.value),
            ProviderCall.SetUserProperties(mapOf("is_premium" to true)),
            ProviderCall.Reset,
        )
        assertEquals(expected, first.calls)
        assertEquals(expected, second.calls)
    }

    @Test
    fun callsProvidersInTheOrderOfTheList() {
        val order = mutableListOf<String>()
        fun named(name: String) = object : AnalyticsProvider by NoOpProvider {
            override fun track(event: AnalyticsEvent) {
                order += name
            }
        }
        val analytics = CompositeAnalytics(listOf(named("first"), named("second"), named("third")))

        analytics.track(AnalyticsEvent(name = "app_opened"))

        assertEquals(listOf("first", "second", "third"), order)
    }

    @Test
    fun worksWithNoProviders() {
        val analytics = CompositeAnalytics(emptyList())

        analytics.track(AnalyticsEvent(name = "app_opened"))
        analytics.reset()
    }

    @Test
    fun eachProviderGetsTheSameUnchangedEvent() {
        val spies = List(3) { SpyAnalyticsProvider() }
        val analytics = CompositeAnalytics(spies)

        analytics.track(AnalyticsEvent(name = "purchase", parameters = mapOf("price" to 9.99)))

        for (spy in spies) {
            assertEquals(
                listOf<ProviderCall>(ProviderCall.Track(name = "purchase", parameters = mapOf("price" to 9.99))),
                spy.calls,
            )
        }
    }

    @Test
    fun aListChangedLaterDoesNotChangeTheProviders() {
        val spy = SpyAnalyticsProvider()
        val providers = mutableListOf<AnalyticsProvider>()
        val analytics = CompositeAnalytics(providers)

        providers += spy
        analytics.track(AnalyticsEvent(name = "app_opened"))

        assertTrue(spy.calls.isEmpty())
    }

    private object NoOpProvider : AnalyticsProvider {
        override fun track(event: AnalyticsEvent) {}
        override fun setUserID(id: HashedUserID) {}
        override fun setUserProperties(properties: Map<String, Any>) {}
        override fun reset() {}
    }
}

class SHA256IdentifierHasherTest {
    private val hasher = SHA256IdentifierHasher()

    @Test
    fun matchesTheKnownSHA256OfABC() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            hasher.hash("abc").value,
        )
    }

    /** Text is hashed as UTF-8, so the same input gives the same ID on iOS and Android. */
    @Test
    fun hashesNonASCIITextAsUTF8() {
        assertEquals(
            "e58f1e8c55fa105bdd3f40e5037eb0b039b5998d52c05e6cd98878dd2da5cab2",
            hasher.hash("привет").value,
        )
    }

    @Test
    fun isLowercaseHexOf64Characters() {
        val value = hasher.hash("8FC10609-B5A6-4C45-A2A0-8B340468E611").value

        assertEquals(64, value.length)
        assertTrue(value.all { it in "0123456789abcdef" })
    }

    @Test
    fun sameInputGivesSameHash() {
        assertEquals(hasher.hash("user"), hasher.hash("user"))
    }

    @Test
    fun differentInputsGiveDifferentHashes() {
        assertNotEquals(hasher.hash("user-1"), hasher.hash("user-2"))
    }

    @Test
    fun neverReturnsTheRawID() {
        assertNotEquals("raw-id", hasher.hash("raw-id").value)
    }
}

class AnonymousUserIDProviderTest {
    private val hasher = SHA256IdentifierHasher()

    @Test
    fun createsAndSavesAnIDOnFirstLaunch() {
        val store = InMemoryUserIDStore()
        val provider = AnonymousUserIDProvider(store, hasher)

        val id = provider.currentUserID()

        val saved = store.saved
        assertNotNull(saved)
        UUID.fromString(saved) // throws if it is not a UUID
        assertEquals(hasher.hash(saved!!), id)
    }

    @Test
    fun reusesTheSavedID() {
        val store = InMemoryUserIDStore(saved = "existing-id")
        val provider = AnonymousUserIDProvider(store, hasher)

        assertEquals(hasher.hash("existing-id"), provider.currentUserID())
        assertEquals(0, store.saveCount)
    }

    @Test
    fun givesTheSameIDOnEveryCall() {
        val store = InMemoryUserIDStore()
        val provider = AnonymousUserIDProvider(store, hasher)

        val first = provider.currentUserID()
        val second = provider.currentUserID()

        assertEquals(first, second)
        assertEquals(1, store.saveCount)
    }

    @Test
    fun stillGivesAnIDWhenSavingFails() {
        val store = InMemoryUserIDStore(keepsSaves = false)
        val provider = AnonymousUserIDProvider(store, hasher)

        val id = provider.currentUserID()

        assertEquals(64, id.value.length)
        assertEquals(1, store.saveCount)
    }

    @Test
    fun sendsOnlyTheHashNotTheRawID() {
        val store = InMemoryUserIDStore(saved = "raw-id")
        val provider = AnonymousUserIDProvider(store, hasher)

        assertNotEquals("raw-id", provider.currentUserID().value)
    }
}

class NoOpAnalyticsTest {
    @Test
    fun acceptsEveryCall() {
        val analytics: AnalyticsService = NoOpAnalytics

        analytics.track(AnalyticsEvent(name = "app_opened"))
        analytics.setUserID(SHA256IdentifierHasher().hash("user"))
        analytics.setUserProperties(mapOf("is_premium" to true))
        analytics.reset()
    }
}
