package com.example

import com.example.data.Booking
import com.example.data.MasterNailArtEntity
import com.example.data.ServiceItem
import com.example.data.SovereignNodeManager
import com.example.util.SmartSchedulerEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreEnginesUnitTest {

    @Test
    fun smartScheduler_calculatesDurationAndEndTimesAccurately() {
        val breakdown = SmartSchedulerEngine.calculateServiceDuration(
            serviceName = "SMART Педикюр",
            hasDesign = true,
            hasOldCoverRemoval = true,
            hasSpaCare = true
        )
        assertEquals(90, breakdown.baseMinutes)
        assertEquals(30, breakdown.nailDesignMinutes)
        assertEquals(20, breakdown.removalMinutes)
        assertEquals(20, breakdown.spaCareMinutes)
        assertEquals(160, breakdown.totalMinutes)
        assertEquals("2 ч 40 мин", SmartSchedulerEngine.formatDurationString(breakdown.totalMinutes))

        assertEquals("15:20", SmartSchedulerEngine.calculateEndTime("14:00", 80))
        assertEquals("00:30", SmartSchedulerEngine.calculateEndTime("23:30", 60))
    }

    @Test
    fun smartScheduler_detectsOverlapsAndRecommendsAdjacentSlots() {
        assertTrue(
            SmartSchedulerEngine.isSlotOverlappingWithExistingBooking(
                targetSlot = "14:30",
                targetDurationMinutes = 60,
                bookedSlot = "14:00",
                bookedDurationMinutes = 90
            )
        )
        assertFalse(
            SmartSchedulerEngine.isSlotOverlappingWithExistingBooking(
                targetSlot = "15:30",
                targetDurationMinutes = 60,
                bookedSlot = "14:00",
                bookedDurationMinutes = 90
            )
        )

        val recommendations = SmartSchedulerEngine.recommendOptimalSlots(
            bookedSlots = listOf("12:00"),
            availableSlots = listOf("10:00", "13:30", "17:00"),
            durationMinutes = 90
        )
        assertEquals(3, recommendations.size)
        assertTrue(recommendations[0].second.contains("Без окон"))
        assertTrue(recommendations[1].second.contains("Без окон"))
        assertEquals("Свободно", recommendations[2].second)
    }

    @Test
    fun sovereignNodeManager_parsesHostPortAndReportsRealOfflineOnUnreachablePort() = runTest {
        assertEquals("192.168.1.50" to 3000, SovereignNodeManager.parseHostPort("http://192.168.1.50:3000/api"))
        assertEquals("10.0.0.2" to 3000, SovereignNodeManager.parseHostPort("10.0.0.2"))

        // Reserve an ephemeral closed port so the TCP handshake genuinely fails offline
        val closedPort = java.net.ServerSocket(0).use { it.localPort }
        SovereignNodeManager.updateNodeAddress(onion = "", local = "127.0.0.1:$closedPort")
        SovereignNodeManager.toggleTorRouting(false)

        val connected = SovereignNodeManager.pingNode()
        val status = SovereignNodeManager.nodeStatus.value
        assertFalse(connected)
        assertFalse(status.isConnected)
        assertEquals(-1L, status.latencyMs)
        assertTrue(status.diagnosticMessage.contains("Офлайн"))
    }

    @Test
    fun sovereignNodeManager_offlineQueueAndFlushWork() {
        SovereignNodeManager.updateNetworkConnectivity(false)
        SovereignNodeManager.recordLocalBookingEvent(99, "Экспресс", "Sofia", "26.11", "11:00")
        assertTrue(SovereignNodeManager.pendingOfflineQueue.value.any { it.id == "bk_99" })

        SovereignNodeManager.updateNetworkConnectivity(true)
        assertTrue(SovereignNodeManager.pendingOfflineQueue.value.isEmpty())
        assertTrue(SovereignNodeManager.simplexMessages.value.any { it.id == "bk_99" })
    }
}
