package com.example

import android.app.Application
import android.graphics.Color
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.Booking
import com.example.data.MasterNailArtEntity
import com.example.data.ServiceItem
import com.example.data.SovereignNodeManager
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.RadioPlayerController
import com.example.util.VisitTicketCryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NailixRobolectricCujTest {

    @Test
    fun visitTicketCryptoManager_roundTripAndTamperRejection() {
        val booking = Booking(
            id = 42,
            serviceName = "Gaudí Mosaic French",
            serviceCategory = "Маникюр",
            date = "24.11 (Вторник)",
            timeSlot = "16:00",
            masterName = "Виктория",
            price = 65.0,
            clientName = "Elena",
            clientPhone = "+34612345678"
        )

        val ticket = VisitTicketCryptoManager.createTicket(booking)
        assertEquals(16, ticket.securityToken.length)
        assertTrue(ticket.rawPayload.startsWith("NAILIX://CHECKIN?data="))

        val (isValid, parsed) = VisitTicketCryptoManager.verifyAndParsePayload(ticket.rawPayload)
        assertTrue(isValid)
        assertNotNull(parsed)
        assertEquals(42, parsed?.getInt("id"))
        assertEquals("Elena", parsed?.getString("n"))

        // Tamper the base64 JSON payload (invalid token/signature check)
        val badPayload = "NAILIX://CHECKIN?data=" + android.util.Base64.encodeToString(
            """{"id":42,"n":"Elena","p":"+34612345678","s":"Hacked","d":"24.11 (Вторник)","t":"16:00","m":"Виктория","pr":0.0,"tok":"${ticket.securityToken}"}"""
                .toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )
        val (tamperedValid, _) = VisitTicketCryptoManager.verifyAndParsePayload(badPayload)
        assertFalse(tamperedValid)
        assertFalse(VisitTicketCryptoManager.verifyAndParsePayload("invalid-qr-payload").first)

        val bitmap = VisitTicketCryptoManager.generateQrBitmap(ticket.rawPayload, size = 128)
        assertEquals(128, bitmap.width)
        assertEquals(128, bitmap.height)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        assertTrue(pixels.any { it == Color.BLACK })
        assertTrue(pixels.any { it == Color.WHITE })
    }

    @Test
    fun sovereignNodeManager_computesDeterministicSnapshotJson() = runBlocking {
        val bookings = listOf(
            Booking(
                id = 7,
                serviceName = "Маникюр + гель",
                serviceCategory = "Маникюр",
                date = "25.11",
                timeSlot = "14:00",
                masterName = "Виктория",
                price = 55.0,
                clientName = "Elena",
                clientPhone = "+34600000007"
            )
        )
        val services = listOf(
            ServiceItem(id = 1, name = "Маникюр + гель", category = "Маникюр", price = 55.0, durationMin = 90, description = "Base")
        )
        val arts = listOf(
            MasterNailArtEntity(id = 1, title = "Gaudí Gold", masterName = "Виктория", complexity = "Medium", extraPrice = 15.0, imageUrl = "", likes = 3, description = "Mosaic")
        )

        val state1 = SovereignNodeManager.computeRoomStateSnapshot(bookings, services, arts)
        val state2 = SovereignNodeManager.computeRoomStateSnapshot(bookings, services, arts)
        assertEquals(64, state1.stateHash.length)
        assertEquals(state1.stateHash, state2.stateHash)
        assertEquals(3, state1.syncedRecordsCount)

        val json = JSONObject(SovereignNodeManager.exportSnapshotJson(bookings, services, arts))
        assertEquals("NailiX-LocalSnapshot-v1", json.getString("protocol"))
        assertEquals(3, json.getInt("records_count"))
        assertEquals(state1.stateHash, json.getString("sha256_digest"))
    }

    @Test
    fun radioPlayerController_idleStopAndPauseDoNotThrowIllegalState() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        try {
            val controller = RadioPlayerController(scope)
            assertEquals(20, controller.topRadioStations.size)
            assertNotNull(controller.selectedStation.value)
            assertFalse(controller.radioIsPlaying.value)
            assertNull(controller.radioError.value)

            // Guard against MediaPlayer stop/pause in invalid state (-38, 0)
            controller.pauseRadio()
            controller.stopRadio()
            controller.stopRadio()

            assertFalse(controller.radioIsPlaying.value)
            assertFalse(controller.radioIsLoading.value)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun mainViewModel_quickStartAndStudioBookingConfirmationCuj() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = MainViewModel(app)

        vm.completeQuickStart(
            role = "client",
            fallbackName = "Elena K.",
            fallbackEmail = "elena.k@example.com"
        )
        assertEquals("client", vm.userRole.value)
        assertTrue(vm.isOnboarded.value)
        assertEquals("Elena K.", vm.clientProfile.value.name)
        assertEquals("elena.k@example.com", vm.clientProfile.value.email)
        assertFalse(vm.clientProfile.value.phone.contains("999 000-00-00"))

        val testService = ServiceItem(
            id = 101,
            name = "Комби маникюр",
            category = "Маникюр",
            price = 45.0,
            durationMin = 75,
            description = "Clean manicure"
        )
        vm.selectService(testService)
        vm.selectMaster("Виктория")
        vm.selectDate("28.11 (Суббота)")
        vm.selectTimeSlot("14:00")
        vm.requestBookingConfirmation()

        val pending = vm.pendingConfirmationBooking.value
        assertNotNull(pending)
        assertEquals("Комби маникюр", pending?.serviceName)

        vm.confirmStudioBooking(paymentMethod = "studio_visit")

        val deadline = System.currentTimeMillis() + 5000L
        while (vm.bookingSuccessDialog.value == null && System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val created = vm.bookingSuccessDialog.value
        assertNotNull(created)
        assertEquals("Комби маникюр", created?.serviceName)
        assertEquals("Подтверждена", created?.status)
        assertNull(vm.pendingConfirmationBooking.value)
        assertTrue(SovereignNodeManager.simplexMessages.value.any { it.type == "BOOKING_EVENT" })
    }

    @Test
    fun mainViewModel_rescheduleRepeatAndQuickSlotCuj() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = MainViewModel(app)

        vm.completeQuickStart(
            role = "client",
            fallbackName = "Elena K.",
            fallbackEmail = "elena.k@example.com"
        )
        vm.registerMasterProfile(
            name = "Виктория",
            address = "Calella, Carrer de l'Església",
            schedule = "09:00 - 19:00",
            experience = 5,
            languages = listOf("Русский", "Español"),
            instagram = "victoria_nails"
        )

        // Wait for Room seed services to emit
        val seedDeadline = System.currentTimeMillis() + 5000L
        while (vm.services.value.isEmpty() && System.currentTimeMillis() < seedDeadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
        }
        assertTrue(vm.services.value.isNotEmpty())

        val firstService = vm.services.value.first()
        vm.selectService(firstService)
        vm.selectMaster("Виктория")
        val initialDate = vm.dates.first()
        val rescheduledDate = vm.dates[1]
        vm.selectDate(initialDate)
        vm.selectTimeSlot("09:00")
        vm.requestBookingConfirmation()
        vm.confirmStudioBooking("studio_visit")

        val deadline1 = System.currentTimeMillis() + 5000L
        while (vm.bookingSuccessDialog.value == null && System.currentTimeMillis() < deadline1) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val created = vm.bookingSuccessDialog.value
        assertNotNull(created)

        // Reschedule the booking
        vm.rescheduleBooking(created!!, rescheduledDate, "15:00")
        val reschedDeadline = System.currentTimeMillis() + 5000L
        while (vm.bookings.value.none { it.id == created.id && it.date == rescheduledDate && it.timeSlot == "15:00" } &&
            System.currentTimeMillis() < reschedDeadline
        ) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        val rescheduledBooking = vm.bookings.value.find { it.id == created.id }
        assertEquals(rescheduledDate, rescheduledBooking?.date)
        assertEquals("15:00", rescheduledBooking?.timeSlot)

        // Verify repeatBooking pre-fills and switches to booking tab
        vm.repeatBooking(rescheduledBooking!!)
        assertEquals("booking", vm.currentTab.value)
        assertEquals("Виктория", vm.selectedMaster.value)
        assertEquals(firstService.name, vm.selectedService.value?.name)

        // Verify findNextAvailableQuickSlot + quickBookNextAvailableSlot
        val quickSlot = vm.findNextAvailableQuickSlot()
        assertNotNull(quickSlot)
        vm.quickBookNextAvailableSlot(quickSlot!!)
        val quickPending = vm.pendingConfirmationBooking.value
        assertNotNull(quickPending)
        assertEquals(quickSlot.master.name, quickPending?.masterName)
        assertEquals(quickSlot.date, quickPending?.date)
        assertEquals(quickSlot.timeSlot, quickPending?.timeSlot)
    }
}
