package io.github.alexosnach.analyticskit

import java.util.UUID

/**
 * Gives the user ID analytics sends, already hashed.
 *
 * Today the ID is anonymous, made on the device. When the app gets accounts,
 * a provider that hashes the backend's ID replaces this one in the
 * Composition Root; the code that sends the ID does not change.
 */
interface UserIDProvider {
    fun currentUserID(): HashedUserID
}

/**
 * A random ID made on first launch and kept in the store from then on.
 *
 * It identifies this app on this device: another device of the same person
 * gets its own.
 */
class AnonymousUserIDProvider(
    private val store: UserIDStore,
    private val hasher: IdentifierHasher,
) : UserIDProvider {

    override fun currentUserID(): HashedUserID = hasher.hash(rawID())

    /**
     * The saved ID, or a new one saved now. If saving fails, the new ID still
     * serves this call and the next one makes another.
     */
    private fun rawID(): String {
        store.load()?.let { return it }
        val new = UUID.randomUUID().toString()
        store.save(new)
        return new
    }
}
