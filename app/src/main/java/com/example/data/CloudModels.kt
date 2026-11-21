package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class CloudUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val role: String = "client",
    val referralCode: String = "",
    val loyaltyPoints: Long = 150L,
    val loyaltyLevel: String = "Silver",
    val friendsReferred: Long = 0L,
    val homeAddress: String = "",
    val workAddress: String = "",
    val phone: String = "",
    val email: String = "",
    val preferredSkin: String = "gaudi",
    val preferredCurrency: String = "EUR",
    val preferredLanguage: String = "ru",
    val fcmToken: String = "",
    val claimedReferralCode: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudLoyaltyTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val points: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudMasterProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val visibility: String = "public",
    val name: String = "",
    val address: String = "",
    val workingSchedule: String = "",
    val experienceYears: Long = 0L,
    val languages: List<String> = emptyList(),
    val instagramUsername: String = "",
    val currency: String = "EUR",
    val ratingAvg: Double = 5.0,
    val ratingCount: Long = 1L,
    val lat: Double? = null,
    val lon: Double? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudService(
    @DocumentId val id: String = "",
    val userId: String = "",
    val visibility: String = "public",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val durationMin: Long = 60L,
    val description: String = "",
    val imageUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudBooking(
    @DocumentId val id: String = "",
    val clientUid: String = "",
    val masterUid: String = "",
    val serviceName: String = "",
    val serviceCategory: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val masterName: String = "",
    val price: Double = 0.0,
    val clientName: String = "",
    val clientPhone: String = "",
    val paymentMethod: String = "studio_visit",
    val status: String = "Подтверждена",
    val ticketToken: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudNailArt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val visibility: String = "public",
    val title: String = "",
    val masterName: String = "",
    val complexity: String = "Medium",
    val extraPrice: Double = 0.0,
    val imageUrl: String = "",
    val likes: Long = 0L,
    val views: Long = 0L,
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudReview(
    @DocumentId val id: String = "",
    val authorUid: String = "",
    val visibility: String = "public",
    val targetId: String = "",
    val targetType: String = "master",
    val authorName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    val dateStr: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
