package com.bltucker.recipemanager.users

import org.junit.jupiter.api.*
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PasswordServiceTest {
    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setup() {
        passwordService = PasswordService()
    }

    @Nested
    @DisplayName("hashPassword")
    inner class HashPassword {

        @Test
        fun `should hash password successfully`() {
            val password = "mySecurePassword123"

            val hash = passwordService.hashPassword(password)

            assertNotNull(hash)
            assertTrue(hash.isNotEmpty())
        }

        @Test
        fun `should generate different hashes for same password`() {
            val password = "mySecurePassword123"

            val hash1 = passwordService.hashPassword(password)
            val hash2 = passwordService.hashPassword(password)

            // BCrypt uses salt, so hashes should be different
            assertNotEquals(hash1, hash2)
        }

        @Test
        fun `should hash empty string`() {
            val password = ""

            val hash = passwordService.hashPassword(password)

            assertNotNull(hash)
            assertTrue(hash.isNotEmpty())
        }

        @Test
        fun `should hash long password`() {
            val password = "a".repeat(70) // BCrypt has max 72 byte limit

            val hash = passwordService.hashPassword(password)

            assertNotNull(hash)
            assertTrue(hash.isNotEmpty())
        }

        @Test
        fun `should hash password with special characters`() {
            val password = "p@ssw0rd!#$%^&*()"

            val hash = passwordService.hashPassword(password)

            assertNotNull(hash)
            assertTrue(hash.isNotEmpty())
        }
    }

    @Nested
    @DisplayName("verifyPassword")
    inner class VerifyPassword {

        @Test
        fun `should verify correct password`() {
            val password = "mySecurePassword123"
            val hash = passwordService.hashPassword(password)

            val result = passwordService.verifyPassword(password, hash)

            assertTrue(result)
        }

        @Test
        fun `should reject incorrect password`() {
            val password = "mySecurePassword123"
            val wrongPassword = "wrongPassword"
            val hash = passwordService.hashPassword(password)

            val result = passwordService.verifyPassword(wrongPassword, hash)

            assertFalse(result)
        }

        @Test
        fun `should be case sensitive`() {
            val password = "MySecurePassword123"
            val hash = passwordService.hashPassword(password)

            val result = passwordService.verifyPassword("mysecurepassword123", hash)

            assertFalse(result)
        }

        @Test
        fun `should verify empty password`() {
            val password = ""
            val hash = passwordService.hashPassword(password)

            val result = passwordService.verifyPassword(password, hash)

            assertTrue(result)
        }

        @Test
        fun `should verify password with special characters`() {
            val password = "p@ssw0rd!#$%^&*()"
            val hash = passwordService.hashPassword(password)

            val result = passwordService.verifyPassword(password, hash)

            assertTrue(result)
        }

        @Test
        fun `should reject password that differs by one character`() {
            val password = "mySecurePassword123"
            val hash = passwordService.hashPassword(password)

            val result = passwordService.verifyPassword("mySecurePassword124", hash)

            assertFalse(result)
        }

        @Test
        fun `should reject empty string against non-empty password hash`() {
            val password = "mySecurePassword123"
            val hash = passwordService.hashPassword(password)

            val result = passwordService.verifyPassword("", hash)

            assertFalse(result)
        }
    }

    @Nested
    @DisplayName("Integration tests")
    inner class IntegrationTests {

        @Test
        fun `should work with typical registration and login flow`() {
            val userPassword = "userPassword123"

            // Registration: hash the password
            val storedHash = passwordService.hashPassword(userPassword)

            // Login: verify the password
            val loginSuccess = passwordService.verifyPassword(userPassword, storedHash)

            assertTrue(loginSuccess)
        }

        @Test
        fun `should handle multiple users with same password`() {
            val password = "commonPassword123"

            val hash1 = passwordService.hashPassword(password)
            val hash2 = passwordService.hashPassword(password)

            // Both should verify successfully
            assertTrue(passwordService.verifyPassword(password, hash1))
            assertTrue(passwordService.verifyPassword(password, hash2))

            // But hashes should be different (due to salt)
            assertNotEquals(hash1, hash2)
        }
    }
}
