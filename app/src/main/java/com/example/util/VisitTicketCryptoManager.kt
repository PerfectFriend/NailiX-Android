package com.example.util

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.data.Booking
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Криптографический токен визита и генератор QR-кода без внешних тяжелых зависимостей.
 * Генерирует детерминированный QR матрикс (битмап) и подписывает SHA-256 HMAC-подобным дайджестом.
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
        try {
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

            val mockBooking = Booking(
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
            val expectedToken = generateSecurityToken(mockBooking)
            val isValid = token.equals(expectedToken, ignoreCase = true)
            return Pair(isValid, json)
        } catch (e: Exception) {
            return Pair(false, null)
        }
    }

    /**
     * Создает визуальную матрицу QR-кода на чистом Canvas/Bitmap без сторонних библиотек.
     * Использует псевдо-QR сетку 25x25 со стандартными поисковыми маркерами по углам и битами хеша.
     */
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val matrixSize = 25
        val cellSize = size / matrixSize

        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(content.toByteArray(Charsets.UTF_8))

        // Заливаем фон
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, AndroidColor.WHITE)
            }
        }

        fun drawCornerFinder(startX: Int, startY: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    val color = if (isBorder || isCenter) AndroidColor.BLACK else AndroidColor.WHITE
                    val px = (startX + c) * cellSize
                    val py = (startY + r) * cellSize
                    for (ix in 0 until cellSize) {
                        for (iy in 0 until cellSize) {
                            if (px + ix < size && py + iy < size) {
                                bitmap.setPixel(px + ix, py + iy, color)
                            }
                        }
                    }
                }
            }
        }

        // 3 позиционных угловых квадрата
        drawCornerFinder(1, 1)
        drawCornerFinder(matrixSize - 8, 1)
        drawCornerFinder(1, matrixSize - 8)

        // Данные на основе хеша контента
        for (r in 1 until matrixSize - 1) {
            for (c in 1 until matrixSize - 1) {
                val inTopLeft = r in 1..7 && c in 1..7
                val inTopRight = r in 1..7 && c in (matrixSize - 8)..matrixSize
                val inBottomLeft = r in (matrixSize - 8)..matrixSize && c in 1..7
                if (inTopLeft || inTopRight || inBottomLeft) continue

                val hashIndex = ((r * matrixSize + c) % hash.size)
                val bitIndex = (r + c) % 8
                val isBlack = ((hash[hashIndex].toInt() shr bitIndex) and 1) == 1

                val color = if (isBlack) AndroidColor.BLACK else AndroidColor.WHITE
                val px = c * cellSize
                val py = r * cellSize
                for (ix in 0 until cellSize) {
                    for (iy in 0 until cellSize) {
                        if (px + ix < size && py + iy < size) {
                            bitmap.setPixel(px + ix, py + iy, color)
                        }
                    }
                }
            }
        }

        return bitmap
    }
}
