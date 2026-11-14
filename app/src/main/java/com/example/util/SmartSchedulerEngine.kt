package com.example.util

/**
 * Умный планировщик расчёта длительности комплексных процедур и интеллектуальной группировки слотов.
 */
data class ServiceDurationBreakdown(
    val baseMinutes: Int,
    val nailDesignMinutes: Int = 0,
    val removalMinutes: Int = 0,
    val spaCareMinutes: Int = 0,
    val totalMinutes: Int
)

object SmartSchedulerEngine {

    fun calculateServiceDuration(
        serviceName: String,
        hasDesign: Boolean = false,
        hasOldCoverRemoval: Boolean = false,
        hasSpaCare: Boolean = false
    ): ServiceDurationBreakdown {
        var base = when {
            serviceName.contains("Педикюр", ignoreCase = true) -> 90
            serviceName.contains("Наращивание", ignoreCase = true) -> 120
            serviceName.contains("Укрепление", ignoreCase = true) -> 75
            serviceName.contains("Снятие", ignoreCase = true) -> 30
            serviceName.contains("Экспресс", ignoreCase = true) -> 45
            else -> 60 // Классический/Аппаратный маникюр
        }

        val designMin = if (hasDesign) 30 else 0
        val removalMin = if (hasOldCoverRemoval) 20 else 0
        val spaMin = if (hasSpaCare) 20 else 0

        val total = base + designMin + removalMin + spaMin

        return ServiceDurationBreakdown(
            baseMinutes = base,
            nailDesignMinutes = designMin,
            removalMinutes = removalMin,
            spaCareMinutes = spaMin,
            totalMinutes = total
        )
    }

    fun formatDurationString(minutes: Int): String {
        val hours = minutes / 60
        val remMin = minutes % 60
        return when {
            hours > 0 && remMin > 0 -> "$hours ч $remMin мин"
            hours > 0 -> "$hours ч"
            else -> "$remMin мин"
        }
    }

    /**
     * Вычисляет конечное расчетное время сеанса по начальному слоту (e.g. "14:00" + 80 мин -> "15:20")
     */
    fun calculateEndTime(startSlot: String, durationMinutes: Int): String {
        val parts = startSlot.split(":")
        if (parts.size != 2) return startSlot
        val h = parts[0].trim().toIntOrNull() ?: 10
        val m = parts[1].trim().toIntOrNull() ?: 0

        val totalMinutes = h * 60 + m + durationMinutes
        val endH = (totalMinutes / 60) % 24
        val endM = totalMinutes % 60
        return "%02d:%02d".format(endH, endM)
    }

    /**
     * Рекомендация плотной посадки: минимизирует пустые «окна» между клиентами.
     */
    fun recommendOptimalSlots(
        bookedSlots: List<String>,
        availableSlots: List<String>,
        durationMinutes: Int
    ): List<Pair<String, String>> {
        // Возвращает список слотов с бейджем ("Идеально для мастера", "Обычный слот")
        return availableSlots.map { slot ->
            val isAdjacentToBooked = bookedSlots.any { booked ->
                isAdjacentSlot(booked, slot)
            }
            if (isAdjacentToBooked) {
                Pair(slot, "🔥 Без окон (рекомендовано)")
            } else {
                Pair(slot, "Свободно")
            }
        }
    }

    /**
     * Проверяет, перекрывает ли новый слот существующую бронь мастера с учётом длительности обеих процедур.
     */
    fun isSlotOverlappingWithExistingBooking(
        targetSlot: String,
        targetDurationMinutes: Int,
        bookedSlot: String,
        bookedDurationMinutes: Int = 90
    ): Boolean {
        val targetStart = parseSlotToMinutes(targetSlot) ?: return false
        val targetEnd = targetStart + targetDurationMinutes

        val bookedStart = parseSlotToMinutes(bookedSlot) ?: return false
        val bookedEnd = bookedStart + bookedDurationMinutes

        // Пересечение интервалов: [targetStart, targetEnd) и [bookedStart, bookedEnd)
        return targetStart < bookedEnd && targetEnd > bookedStart
    }

    fun parseSlotToMinutes(slot: String): Int? {
        val parts = slot.split(":")
        if (parts.size != 2) return null
        val h = parts[0].trim().toIntOrNull() ?: return null
        val m = parts[1].trim().toIntOrNull() ?: return null
        return h * 60 + m
    }

    private fun isAdjacentSlot(slot1: String, slot2: String): Boolean {
        val m1 = parseSlotToMinutes(slot1) ?: return false
        val m2 = parseSlotToMinutes(slot2) ?: return false
        val diff = Math.abs(m1 - m2)
        // Если слоты идут с шагом от 60 до 150 минут
        return diff in 60..150
    }
}

