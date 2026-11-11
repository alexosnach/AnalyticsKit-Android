package io.github.alexosnach.analyticskit

import java.security.MessageDigest

/**
 * A user ID that has been hashed and is safe to send to analytics.
 *
 * Only a hasher of this library can create it: its constructor is `internal`,
 * so a raw ID can never be passed where a `HashedUserID` is expected.
 */
class HashedUserID internal constructor(val value: String) {
    override fun equals(other: Any?): Boolean = other is HashedUserID && other.value == value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "HashedUserID($value)"
}

/** Turns a raw identifier into the one analytics receives. */
interface IdentifierHasher {
    fun hash(identifier: String): HashedUserID
}

/** SHA-256 as a lowercase hex string of 64 characters. */
class SHA256IdentifierHasher : IdentifierHasher {
    override fun hash(identifier: String): HashedUserID {
        val digest = MessageDigest.getInstance("SHA-256").digest(identifier.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString(separator = "") { "%02x".format(it) }
        return HashedUserID(hex)
    }
}
