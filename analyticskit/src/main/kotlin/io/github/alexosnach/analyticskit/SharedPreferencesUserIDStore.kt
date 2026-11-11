package io.github.alexosnach.analyticskit

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores the user ID in a private `SharedPreferences` file of the app.
 *
 * The ID lives as long as the app's data: clearing the data or removing the
 * app removes it. It comes back after a reinstall only if the app has Auto
 * Backup on and the file is part of the backup (it is by default).
 *
 * A storage failure does not stop the app: the ID still serves this launch.
 * The store reports it to `onError`, so the app can log it with its own logger
 * or send it to crash reporting:
 *
 * ```kotlin
 * SharedPreferencesUserIDStore(context, onError = { Log.e("analytics", it.toString()) })
 * ```
 *
 * `save` writes to disk before it returns, so the ID is not lost if the
 * process dies right after. It is one small write, made once per install.
 */
class SharedPreferencesUserIDStore(
    context: Context,
    name: String = "analyticskit",
    private val key: String = "analytics.user_id",
    private val onError: (Failure) -> Unit = {},
) : UserIDStore {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun load(): String? =
        try {
            preferences.getString(key, null)
        } catch (e: ClassCastException) {
            // The key holds a value of another type.
            onError(Failure.Read(e))
            null
        }

    override fun save(id: String) {
        val saved = preferences.edit().putString(key, id).commit()
        if (!saved) {
            onError(Failure.Write)
        }
    }

    /**
     * A storage call that failed.
     * The store keeps working after it: the ID serves this launch.
     */
    sealed class Failure {
        /** Reading the saved ID failed (a missing ID is not a failure). */
        data class Read(val cause: Throwable) : Failure() {
            override fun toString(): String = "SharedPreferences read failed: ${cause.message ?: cause}"
        }

        /** Writing the ID to disk failed. */
        data object Write : Failure() {
            override fun toString(): String = "SharedPreferences write failed"
        }
    }
}
