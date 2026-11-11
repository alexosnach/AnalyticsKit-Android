package io.github.alexosnach.analyticskit

/** Keeps the app's raw user ID between launches. */
interface UserIDStore {
    /** The saved ID, or `null` when none was saved yet. */
    fun load(): String?

    fun save(id: String)
}
