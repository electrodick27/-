package com.example.data.util

import java.security.MessageDigest

object HashUtils {
    /**
     * Hashes a password using SHA-256 and a standard salt.
     */
    fun sha256(password: String): String {
        val salt = "LOS_CRM_SALT_2026"
        val bytes = (password + salt).toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Checks if a password matches the target hash.
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return sha256(password) == hash
    }
}
