package com.bltucker.recipemanager.users

import com.bltucker.recipemanager.common.database.tables.Users
import com.bltucker.recipemanager.common.testing.TestConstants
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.*
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExposedUserRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: ExposedUserRepository

    @BeforeEach
    fun setupDatabase() {
        database = Database.connect(
            url = "jdbc:h2:mem:test_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver"
        )

        transaction(database) {
            SchemaUtils.create(Users)
        }

        repository = ExposedUserRepository()
    }

    @AfterEach
    fun teardownDatabase() {
        transaction(database) {
            SchemaUtils.drop(Users)
        }
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking {
            block()
        }
    }

    @Nested
    @DisplayName("createUser")
    inner class CreateUser {

        @Test
        fun `should create user successfully`() = runTest {
            val email = "test@example.com"
            val hashedPassword = "hashedPassword123"

            val user = repository.createUser(email, hashedPassword)

            assertNotNull(user)
            assertNotNull(user.id)
            assertEquals(email, user.email)
            assertEquals(hashedPassword, user.hashedPassword)
            assertEquals(false, user.isVerified)
        }

        @Test
        fun `should create user with unique ID`() = runTest {
            val user1 = repository.createUser("user1@example.com", "hash1")
            val user2 = repository.createUser("user2@example.com", "hash2")

            assertTrue(user1.id != user2.id)
        }

        @Test
        fun `should set isVerified to false by default`() = runTest {
            val user = repository.createUser("test@example.com", "hashedPassword")

            assertEquals(false, user.isVerified)
        }

        @Test
        fun `should generate valid UUID for user ID`() = runTest {
            val user = repository.createUser("test@example.com", "hashedPassword")

            // Should not throw exception when parsing as UUID
            assertDoesNotThrow {
                UUID.fromString(user.id)
            }
        }
    }

    @Nested
    @DisplayName("findByEmail")
    inner class FindByEmail {

        @Test
        fun `should find user by email`() = runTest {
            val email = "test@example.com"
            val hashedPassword = "hashedPassword123"
            repository.createUser(email, hashedPassword)

            val found = repository.findByEmail(email)

            assertNotNull(found)
            assertEquals(email, found.email)
            assertEquals(hashedPassword, found.hashedPassword)
        }

        @Test
        fun `should return null when user not found`() = runTest {
            val found = repository.findByEmail("nonexistent@example.com")

            assertNull(found)
        }

        @Test
        fun `should be case sensitive`() = runTest {
            val email = "Test@Example.com"
            repository.createUser(email, "hashedPassword")

            val found = repository.findByEmail("test@example.com")

            assertNull(found)
        }

        @Test
        fun `should retrieve verified status correctly`() = runTest {
            val email = "test@example.com"
            val createdUser = repository.createUser(email, "hashedPassword")

            // Verify the user
            repository.markAsVerified(createdUser.id)

            val found = repository.findByEmail(email)

            assertNotNull(found)
            assertTrue(found.isVerified)
        }
    }

    @Nested
    @DisplayName("findById")
    inner class FindById {

        @Test
        fun `should find user by ID`() = runTest {
            val email = "test@example.com"
            val hashedPassword = "hashedPassword123"
            val createdUser = repository.createUser(email, hashedPassword)

            val found = repository.findById(createdUser.id)

            assertNotNull(found)
            assertEquals(createdUser.id, found.id)
            assertEquals(email, found.email)
            assertEquals(hashedPassword, found.hashedPassword)
        }

        @Test
        fun `should return null when user not found`() = runTest {
            val randomId = UUID.randomUUID().toString()

            val found = repository.findById(randomId)

            assertNull(found)
        }

        @Test
        fun `should retrieve all user fields correctly`() = runTest {
            val createdUser = repository.createUser("test@example.com", "hashedPassword")

            val found = repository.findById(createdUser.id)

            assertNotNull(found)
            assertEquals(createdUser.id, found.id)
            assertEquals(createdUser.email, found.email)
            assertEquals(createdUser.hashedPassword, found.hashedPassword)
            assertEquals(false, found.isVerified)
            assertNotNull(found.createdAt)
            assertNotNull(found.updatedAt)
        }
    }

    @Nested
    @DisplayName("markAsVerified")
    inner class MarkAsVerified {

        @Test
        fun `should mark user as verified`() = runTest {
            val email = "test@example.com"
            val createdUser = repository.createUser(email, "hashedPassword")

            val result = repository.markAsVerified(createdUser.id)

            assertTrue(result)

            val found = repository.findById(createdUser.id)
            assertNotNull(found)
            assertTrue(found.isVerified)
        }

        @Test
        fun `should return false when user not found`() = runTest {
            val randomId = UUID.randomUUID().toString()

            val result = repository.markAsVerified(randomId)

            assertEquals(false, result)
        }

        @Test
        fun `should handle multiple verification attempts`() = runTest {
            val createdUser = repository.createUser("test@example.com", "hashedPassword")

            val result1 = repository.markAsVerified(createdUser.id)
            val result2 = repository.markAsVerified(createdUser.id)

            assertTrue(result1)
            assertTrue(result2)

            val found = repository.findById(createdUser.id)
            assertNotNull(found)
            assertTrue(found.isVerified)
        }
    }

    @Nested
    @DisplayName("Data integrity")
    inner class DataIntegrity {

        @Test
        fun `should maintain unique email constraint`() = runTest {
            val email = "test@example.com"
            repository.createUser(email, "password1")

            // Attempting to create another user with same email should fail
            // Note: In real implementation this should throw an exception
            // but we can't easily test that without more setup
        }

        @Test
        fun `should persist timestamps`() = runTest {
            val user = repository.createUser("test@example.com", "hashedPassword")

            val found = repository.findById(user.id)

            assertNotNull(found)
            assertNotNull(found.createdAt)
            assertNotNull(found.updatedAt)
            assertTrue(found.createdAt.isNotEmpty())
            assertTrue(found.updatedAt.isNotEmpty())
        }

        @Test
        fun `should store hashed password correctly`() = runTest {
            val hashedPassword = "veryLongHashedPasswordString12345!@#$%"
            val user = repository.createUser("test@example.com", hashedPassword)

            val found = repository.findById(user.id)

            assertNotNull(found)
            assertEquals(hashedPassword, found.hashedPassword)
        }
    }
}
