package br.com.fiap.wtcconnect.notifications

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

object FcmTokenManager {
    fun registerTokenForUser(context: Context, uid: String, token: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(uid)
        val map = mapOf("fcmTokens.$token" to true)
        userRef.set(map, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { /* ok */ }
            .addOnFailureListener { it.printStackTrace() }
    }

    fun registerPendingTokenIfAny(context: Context, uid: String) {
        val prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        val pending = prefs.getString("pending_fcm_token", null)
        if (pending != null) {
            registerTokenForUser(context, uid, pending)
            prefs.edit().remove("pending_fcm_token").apply()
        }
    }
}

