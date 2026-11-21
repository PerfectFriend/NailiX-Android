package com.example.ui.viewmodel

data class ClientProfile(
    val name: String,
    val phone: String,
    val email: String = "",
    val totalBookings: Int = 0,
    val totalSpent: Double = 0.0,
    val isSubscribed: Boolean = false,
    val rating: Double = 5.0,
    val ratingCount: Int = 1,
    val homeAddress: String = "Calella, Carrer de Jovara 100",
    val homeLat: Double = 41.6140,
    val homeLon: Double = 2.6600,
    val workAddress: String = "Calella, Carrer de Sant Jaume 150",
    val workLat: Double = 41.6160,
    val workLon: Double = 2.6630,
    val referralCode: String = ""
)

data class AdminStats(
    val totalRevenue: Double = 0.0,
    val activeBookings: Int = 0,
    val completedBookings: Int = 0,
    val popularCategory: String = "Нет",
    val topMaster: String = "Нет"
)

data class MasterProfile(
    val id: Int,
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val workingSchedule: String,
    val experienceYears: Int,
    val languages: List<String>,
    val instagramUsername: String,
    val rating: Double = 5.0,
    val ratingCount: Int = 1,
    val isUserCreated: Boolean = false,
    val currency: String = "EUR",
    val ownerUid: String = ""
)

data class AggregatorReview(
    val id: Int,
    val targetId: String,
    val targetType: String,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val dateStr: String
)

data class NailContest(
    val id: String,
    val title: String,
    val desc: String,
    val prize: String,
    val endDate: String,
    val icon: String
)

data class ContestEntry(
    val id: String,
    val contestId: String,
    val title: String,
    val imageUrl: String,
    val authorName: String,
    val votes: Int,
    val isUserSubmitted: Boolean = false
)
