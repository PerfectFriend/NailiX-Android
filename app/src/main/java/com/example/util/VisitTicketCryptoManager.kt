package com.example.util

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.data.Booking
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Криптографический токен визита и генератор стандартного ISO/IEC 18004 QR-кода (ZXing).
 */
data class VisitQrTicket(
    val bookingId: Int,
    val clientName: String,
    val clientPhone: String,
    val serviceName: String,
    val date: String,
    val timeSlot: String,
    val masterName: String,
    val price: Double,
    val securityToken: String,
    val rawPayload: String
)

object VisitTicketCryptoManager {

    private const val SOVEREIGN_SALT = "NAILIX_SOVEREIGN_VISIT_TOKEN_SALT_2026"

    fun generateSecurityToken(booking: Booking): String {
        val input = "${booking.id}|${booking.clientPhone}|${booking.date}|${booking.timeSlot}|${booking.price}|$SOVEREIGN_SALT"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    fun createTicket(booking: Booking): VisitQrTicket {
        val token = generateSecurityToken(booking)
        val json = JSONObject().apply {
            put("id", booking.id)
            put("n", booking.clientName)
            put("p", booking.clientPhone)
            put("s", booking.serviceName)
            put("d", booking.date)
            put("t", booking.timeSlot)
            put("m", booking.masterName)
            put("pr", booking.price)
            put("tok", token)
            put("iss", System.currentTimeMillis())
        }
        val payload = "NAILIX://CHECKIN?data=" + android.util.Base64.encodeToString(
            json.toString().toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )
        return VisitQrTicket(
            bookingId = booking.id,
            clientName = booking.clientName,
            clientPhone = booking.clientPhone,
            serviceName = booking.serviceName,
            date = booking.date,
            timeSlot = booking.timeSlot,
            masterName = booking.masterName,
            price = booking.price,
            securityToken = token,
            rawPayload = payload
        )
    }

    fun verifyAndParsePayload(payload: String): Pair<Boolean, JSONObject?> {
        return try {
            if (!payload.startsWith("NAILIX://CHECKIN?data=")) return Pair(false, null)
            val base64Data = payload.removePrefix("NAILIX://CHECKIN?data=")
            val jsonStr = String(android.util.Base64.decode(base64Data, android.util.Base64.NO_WRAP), Charsets.UTF_8)
            val json = JSONObject(jsonStr)
            val id = json.optInt("id", -1)
            val phone = json.optString("p", "")
            val date = json.optString("d", "")
            val time = json.optString("t", "")
            val price = json.optDouble("pr", 0.0)
            val token = json.optString("tok", "")

            val candidateBooking = Booking(
                id = id,
                serviceName = json.optString("s", ""),
                serviceCategory = "Manicure",
                date = date,
                timeSlot = time,
                masterName = json.optString("m", ""),
                price = price,
                clientName = json.optString("n", ""),
                clientPhone = phone
            )
            val expectedToken = generateSecurityToken(candidateBooking)
            val isValid = token.equals(expectedToken, ignoreCase = true)
            Pair(isValid, json)
        } catch (_: Exception) {
            Pair(false, null)
        }
    }

    /**
     * Генерирует стандартный сканируемый QR-код ISO/IEC 18004 через ZXing QRCodeWriter.
     */
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bitMatrix = QRCodeWriter().encode(
            content.ifBlank { "NAILIX" },
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
