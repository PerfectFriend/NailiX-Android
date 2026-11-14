package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.Booking

object CalendarSyncHelper {
    fun exportBookingToCalendar(
        context: Context,
        booking: Booking,
        locationAddress: String = "Салон NailiX"
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "💅 ${booking.serviceName} • NailiX")
                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "Запись через NailiX:\n• Клиент: ${booking.clientName} (${booking.clientPhone})\n• Мастер: ${booking.masterName}\n• Время: ${booking.date} в ${booking.timeSlot}\n• Статус: ${booking.status}"
                )
                putExtra(CalendarContract.Events.EVENT_LOCATION, locationAddress)
            }
            context.startActivity(intent)
            Toast.makeText(context, "Открытие Google Календаря... 📅", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть приложение календаря", Toast.LENGTH_SHORT).show()
        }
    }
}
