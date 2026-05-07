package br.com.fiap.wtcconnect.data

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class FakeChatRepositoryTest {

    @Test
    fun `addUserToGroupByEmail creates user when not exists`() = runBlocking {
        val repo = FakeChatRepository(currentUserId = "me-test", currentUserEmail = "me@test.com")
        val groupId = "g0"
        val email = "newuser@example.com"

        // ensure user doesn't exist initially
        val initial = repo.searchUsers("newuser").first()
        assertTrue(initial.none { it.email == email })

        val res = repo.addUserToGroupByEmail(groupId, email)
        assertTrue(res.isSuccess)

        // now search
        val after = repo.searchUsers("newuser").first()
        assertTrue("User should be found after creation", after.any { it.email == email })
    }

    @Test
    fun `removeUserFromGroup fails if not member`() = runBlocking {
        val repo = FakeChatRepository()
        // try removing nonexistent user
        val fakeUserId = "not-existent"
        val result = repo.removeUserFromGroup("g0", fakeUserId)
        assertTrue(result.isFailure)
    }
}
