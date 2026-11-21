package com.example.util

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationGeocoderHelper {

    suspend fun geocodeAddress(
        context: Context,
        addressQuery: String
    ): Result<Pair<Double, Double>> = withContext(Dispatchers.IO) {
        val cleanQuery = addressQuery.trim()
        if (cleanQuery.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Введите более подробный адрес"))
        }
        if (!Geocoder.isPresent()) {
            return@withContext Result.failure(IllegalStateException("Сервис геокодирования недоступен на этом устройстве"))
        }

        return@withContext try {
            val geocoder = Geocoder(context.applicationContext, Locale.getDefault())
            val results: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocationName(cleanQuery, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (cont.isActive) cont.resume(addresses)
                        }

                        override fun onError(errorMessage: String?) {
                            if (cont.isActive) cont.resume(emptyList())
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(cleanQuery, 1).orEmpty()
            }

            val first = results.firstOrNull()
            if (first != null) {
                Result.success(first.latitude to first.longitude)
            } else {
                Result.failure(IllegalStateException("Адрес не найден на карте: $cleanQuery"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Ошибка определения координат"))
        }
    }

    fun openMapsDirections(
        context: Context,
        label: String,
        address: String,
        lat: Double,
        lon: Double
    ) {
        val queryText = if (address.isNotBlank()) address else label
        val encodedLabel = Uri.encode(queryText)
        val geoUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($encodedLabel)")
        val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webUri = Uri.parse("https://maps.google.com/?q=$lat,$lon")
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }.onFailure {
                Toast.makeText(context, "Приложение карт не найдено", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
