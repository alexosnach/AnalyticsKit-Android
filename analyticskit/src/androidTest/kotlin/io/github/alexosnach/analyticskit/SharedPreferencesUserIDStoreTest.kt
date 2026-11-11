package io.github.alexosnach.analyticskit

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Uses the real SharedPreferences of the device, in a file of its own that each test removes. */
@RunWith(AndroidJUnit4::class)
class SharedPreferencesUserIDStoreTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "tests.analyticskit.${UUID.randomUUID()}"

    @After
    fun deleteFile() {
        context.deleteSharedPreferences(name)
    }

    @Test
    fun hasNoIDBeforeTheFirstSave() {
        val failures = mutableListOf<SharedPreferencesUserIDStore.Failure>()
        val store = SharedPreferencesUserIDStore(context, name = name, onError = { failures += it })

        assertNull(store.load())
        assertTrue("A missing ID is not a failure", failures.isEmpty())
    }

    @Test
    fun loadsWhatWasSaved() {
        val store = SharedPreferencesUserIDStore(context, name = name)

        store.save("first-id")

        assertEquals("first-id", store.load())
    }

    @Test
    fun replacesASavedID() {
        val store = SharedPreferencesUserIDStore(context, name = name)

        store.save("first-id")
        store.save("second-id")

        assertEquals("second-id", store.load())
    }

    @Test
    fun anotherStoreWithTheSameNameSeesTheID() {
        SharedPreferencesUserIDStore(context, name = name).save("shared-id")

        assertEquals("shared-id", SharedPreferencesUserIDStore(context, name = name).load())
    }

    @Test
    fun storesWithDifferentKeysDoNotSeeEachOther() {
        SharedPreferencesUserIDStore(context, name = name, key = "a").save("id-a")

        assertNull(SharedPreferencesUserIDStore(context, name = name, key = "b").load())
    }

    @Test
    fun reportsAReadFailureWhenTheKeyHoldsAnotherType() {
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().putInt("analytics.user_id", 42).commit()
        val failures = mutableListOf<SharedPreferencesUserIDStore.Failure>()
        val store = SharedPreferencesUserIDStore(context, name = name, onError = { failures += it })

        assertNull(store.load())
        assertEquals(1, failures.size)
        assertTrue(failures.single() is SharedPreferencesUserIDStore.Failure.Read)
        assertTrue(failures.single().toString().startsWith("SharedPreferences read failed: "))
    }

    @Test
    fun anonymousProviderKeepsItsIDInTheRealStore() {
        val hasher = SHA256IdentifierHasher()

        val first = AnonymousUserIDProvider(SharedPreferencesUserIDStore(context, name = name), hasher).currentUserID()
        val second = AnonymousUserIDProvider(SharedPreferencesUserIDStore(context, name = name), hasher).currentUserID()

        assertEquals(first, second)
        assertEquals(hasher.hash(SharedPreferencesUserIDStore(context, name = name).load()!!), first)
    }
}
