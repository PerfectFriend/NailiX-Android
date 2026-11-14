package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceName: String,
    val serviceCategory: String,
    val date: String,
    val timeSlot: String,
    val masterName: String,
    val price: Double,
    val clientName: String,
    val clientPhone: String,
    val status: String = "Подтверждена", // "Подтверждена", "Выполнена", "Отменена"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_loyalty")
data class UserLoyalty(
    @PrimaryKey val id: Int = 1, // Single-row user state
    val points: Int = 150,       // Welcome bonus points
    val level: String = "Silver", // "Silver", "Gold", "Platinum"
    val referralCode: String = "VIC-8842-NIL",
    val friendsReferred: Int = 0,
    val isMailingSubscribed: Boolean = false,
    val subscribedEmail: String? = null,
    val subscribedPhone: String? = null
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingId: Int,
    val title: String,
    val message: String,
    val triggerTimeBefore: String, // e.g., "За 2 часа", "За 24 часа"
    val isSent: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_designs")
data class FavoriteDesign(
    @PrimaryKey val designId: Int
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val durationMin: Int,
    val description: String,
    val imageUrl: String = "" // Custom illustration/image URL
)

@Entity(tableName = "referred_friends")
data class ReferredFriend(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val status: String, // e.g., "Зарегистрировалась", "Завершила визит 🎉", "В процессе..."
    val rewardPoints: Int,
    val date: String
)

@Entity(tableName = "loyalty_transactions")
data class LoyaltyTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val points: Int, // positive or negative
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "localizations", primaryKeys = ["key", "langCode"])
data class LocalizationEntity(
    val key: String,
    val langCode: String,
    val value: String
)

@Entity(tableName = "master_nail_arts")
data class MasterNailArtEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val masterName: String,
    val complexity: String, // "Easy", "Medium", "Hard" / "Простой", "Средний", "Шедевр"
    val extraPrice: Double, // Cost added to base manicure
    val imageUrl: String,
    val likes: Int = 0,
    val views: Int = 0,
    val description: String
)

