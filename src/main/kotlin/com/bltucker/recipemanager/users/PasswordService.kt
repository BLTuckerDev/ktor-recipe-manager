package com.bltucker.recipemanager.users

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordService {
    private val bCryptVerifier = BCrypt.verifyer()

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return bCryptVerifier.verify(password.toCharArray(), hash.toCharArray()).verified
    }
}