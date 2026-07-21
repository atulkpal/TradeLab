package com.ashwathai.tradelab.data

import android.util.Log
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardManagerTest {

    private lateinit var leaderboardManager: LeaderboardManager
    private val db = mockk<FirebaseFirestore>(relaxed = true)
    private val collection = mockk<CollectionReference>(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        
        every { db.collection("leaderboard") } returns collection
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        
        leaderboardManager = LeaderboardManager(db)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `syncUserStats skips if userId is blank`() = runTest {
        leaderboardManager.syncUserStats("", "Name", 100, 1000.0)
        verify(exactly = 0) { collection.document(any()) }
    }

    @Test
    fun `syncUserStats updates firestore when valid`() = runTest {
        val document = mockk<DocumentReference>(relaxed = true)
        val task = mockk<Task<Void>>(relaxed = true)
        
        coEvery { task.await() } returns mockk(relaxed = true)
        every { collection.document("user123") } returns document
        every { document.set(any()) } returns task
        
        leaderboardManager.syncUserStats("user123", "Trader", 500, 5000.0)
        
        verify { collection.document("user123") }
        verify { document.set(any()) }
    }

    @Test
    fun `getTopUsersFlow emits empty list on firestore error`() = runTest {
        val query = mockk<Query>(relaxed = true)
        every { collection.orderBy("xp", Query.Direction.DESCENDING) } returns query
        every { query.limit(20) } returns query
        
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        every { query.addSnapshotListener(capture(listenerSlot)) } returns mockk(relaxed = true)

        leaderboardManager.getTopUsersFlow().test {
            val error = mockk<FirebaseFirestoreException>(relaxed = true)
            
            // Wait for the flow to start and register the listener
            verify(timeout = 2000) { query.addSnapshotListener(any()) }
            
            if (listenerSlot.isCaptured) {
                listenerSlot.captured.onEvent(null, error)
                val result = awaitItem()
                assertEquals(0, result.size)
            } else {
                org.junit.Assert.fail("Listener not captured")
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
