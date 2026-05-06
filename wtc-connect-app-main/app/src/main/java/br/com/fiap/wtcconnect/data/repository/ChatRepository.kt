package br.com.fiap.wtcconnect.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String? = null,
    val createdAt: Timestamp? = null,
    val type: String = "text",
    val interactive: Map<String, Any>? = null
)

class ChatRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val col = firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val registration = col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                val m = doc.data ?: return@mapNotNull null
                val interactive = m["interactive"] as? Map<*, *>
                ChatMessage(
                    id = doc.id,
                    text = m["text"] as? String ?: "",
                    senderId = m["senderId"] as? String ?: "",
                    senderName = m["senderName"] as? String,
                    createdAt = m["createdAt"] as? Timestamp,
                    type = m["type"] as? String ?: "text",
                    interactive = interactive?.mapKeys { it.key as String }?.mapValues { it.value as Any }
                )
            } ?: emptyList()
            trySend(list).isSuccess
        }

        awaitClose { registration.remove() }
    }

    fun sendMessage(chatId: String, text: String, senderId: String, senderName: String?, interactive: Map<String, Any>? = null) {
        val chatRef = firestore.collection("chats").document(chatId)
        val messagesRef = chatRef.collection("messages")

        val newMsg = hashMapOf(
            "text" to text,
            "senderId" to senderId,
            "senderName" to senderName,
            "createdAt" to FieldValue.serverTimestamp(),
            "type" to "text"
        )
        if (interactive != null) {
            newMsg["interactive"] = interactive
            newMsg["type"] = "interactive"
        }

        val batch = firestore.batch()
        val msgDoc = messagesRef.document()
        batch.set(msgDoc, newMsg)
        batch.set(chatRef, hashMapOf(
            "lastMessage" to text,
            "lastMessageAt" to FieldValue.serverTimestamp()
        ), com.google.firebase.firestore.SetOptions.merge())

        batch.commit()
            .addOnSuccessListener { /* success: no-op */ }
            .addOnFailureListener {
                // Consider bubbling this up via a callback or logging; keeping simple for now
                it.printStackTrace()
            }
    }
}
