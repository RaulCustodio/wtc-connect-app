package br.com.fiap.wtcconnect.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import br.com.fiap.wtcconnect.viewmodel.UserType
import kotlinx.coroutines.tasks.await

/**
 * UserRepository - Gerencia dados de usuário no Firestore
 *
 * Este é um exemplo de como integrar Firestore com a autenticação Firebase
 * para armazenar informações adicionais de usuário (perfil, preferências, etc)
 *
 * NOTA: Adicione a dependência no build.gradle.kts:
 * implementation("com.google.firebase:firebase-firestore-ktx")
 */

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val userType: UserType = UserType.CLIENT,
    val photoUrl: String = "",
    val phone: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

class UserRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Cria um novo perfil de usuário no Firestore após login bem-sucedido
     */
    suspend fun createUserProfile(
        email: String,
        name: String,
        userType: UserType
    ): Result<Unit> = try {
        val currentUser = firebaseAuth.currentUser
        require(currentUser != null) { "User não autenticado" }

        val userProfile = UserProfile(
            uid = currentUser.uid,
            email = email,
            name = name,
            userType = userType,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        firestore.collection(USERS_COLLECTION)
            .document(currentUser.uid)
            .set(userProfile)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Busca o perfil de usuário do Firestore
     */
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val currentUser = firebaseAuth.currentUser
            require(currentUser != null) { "User não autenticado" }

            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .get()
                .await<com.google.firebase.firestore.DocumentSnapshot>()

            val profile: UserProfile? = snapshot.toObject(UserProfile::class.java)
            if (profile != null) {
                Result.success(profile)
            } else {
                Result.failure(Exception("Perfil não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atualiza o perfil do usuário
     */
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> = try {
        val currentUser = firebaseAuth.currentUser
        require(currentUser != null) { "User não autenticado" }

        firestore.collection(USERS_COLLECTION)
            .document(currentUser.uid)
            .update(
                mapOf(
                    "name" to userProfile.name,
                    "photoUrl" to userProfile.photoUrl,
                    "phone" to userProfile.phone,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await<Void>()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Deleta o perfil do usuário (quando a conta é deletada)
     */
    suspend fun deleteUserProfile(): Result<Unit> = try {
        val currentUser = firebaseAuth.currentUser
        require(currentUser != null) { "User não autenticado" }

        firestore.collection(USERS_COLLECTION)
            .document(currentUser.uid)
            .delete()
            .await<Void>()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

