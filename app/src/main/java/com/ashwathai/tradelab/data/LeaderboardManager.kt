package com.ashwathai.tradelab.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import javax.inject.Inject
import javax.inject.Singleton

data class LeaderboardEntry(
    val userId: String = "",
    val userName: String = "Anonymous",
    val xp: Int = 0,
    val portfolioValue: Double = 0.0,
    val rank: String = ""
)

@Singleton
class LeaderboardManager @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val leaderboardCollection = db.collection("leaderboard")

    suspend fun syncUserStats(userId: String, userName: String, xp: Int, portfolioValue: Double) {
        if (userId.isBlank()) return
        
        val data = mapOf(
            "userId" to userId,
            "userName" to userName,
            "xp" to xp,
            "portfolioValue" to portfolioValue,
            "lastUpdated" to System.currentTimeMillis()
        )
        
        try {
            leaderboardCollection.document(userId).set(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTopUsersFlow(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val listener = leaderboardCollection
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("LeaderboardManager", "Firestore error: ${error.message}", error)
                    trySend(emptyList()) // Emit empty list on error to keep UI alive
                    return@addSnapshotListener
                }
                
                val entries = snapshot?.documents?.mapIndexed { index, doc ->
                    val entry = doc.toObject(LeaderboardEntry::class.java) ?: LeaderboardEntry()
                    entry.copy(rank = "Rank #${index + 1}")
                } ?: emptyList()
                
                trySend(entries)
            }
        
        awaitClose { listener.remove() }
    }
}
