package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    fun generateReferralCodeForUid(uid: String): String {
        val suffix = uid.filter { it.isLetterOrDigit() }.uppercase().take(6).padEnd(6, 'X')
        return "NLX-$suffix"
    }

    private fun <T> Flow<T>.handleFirestoreStreamErrors(
        operationType: OperationType,
        path: String,
        uid: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    fun observeUserProfile(uid: String): Flow<CloudUserProfile?> = flow {
        if (uid.isBlank() || auth.currentUser?.uid != uid) {
            emit(null)
            return@flow
        }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(CloudUserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .handleFirestoreStreamErrors(OperationType.GET, path, uid)
        )
    }

    fun observeClientBookings(uid: String): Flow<List<CloudBooking>> = flow {
        if (uid.isBlank() || auth.currentUser?.uid != uid) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("bookings")
                .whereEqualTo("clientUid", uid)
                .snapshots()
                .map { it.toObjects(CloudBooking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStreamErrors(OperationType.LIST, "bookings", uid)
        )
    }

    fun observeMasterBookings(uid: String): Flow<List<CloudBooking>> = flow {
        if (uid.isBlank() || auth.currentUser?.uid != uid) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("bookings")
                .whereEqualTo("masterUid", uid)
                .snapshots()
                .map { it.toObjects(CloudBooking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStreamErrors(OperationType.LIST, "bookings", uid)
        )
    }

    fun observePublicMasters(): Flow<List<CloudMasterProfile>> = flow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("masters")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(CloudMasterProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStreamErrors(OperationType.LIST, "masters", uid)
        )
    }

    fun observePublicServices(): Flow<List<CloudService>> = flow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("services")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(CloudService::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStreamErrors(OperationType.LIST, "services", uid)
        )
    }

    fun observePublicNailArts(): Flow<List<CloudNailArt>> = flow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("nailArts")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(CloudNailArt::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStreamErrors(OperationType.LIST, "nailArts", uid)
        )
    }

    fun observePublicReviews(): Flow<List<CloudReview>> = flow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("reviews")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(CloudReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStreamErrors(OperationType.LIST, "reviews", uid)
        )
    }

    suspend fun upsertUserProfile(
        displayName: String,
        role: String,
        loyaltyPoints: Int,
        loyaltyLevel: String,
        friendsReferred: Int,
        homeAddress: String? = null,
        workAddress: String? = null,
        phone: String? = null,
        email: String? = null,
        preferredSkin: String = "gaudi",
        preferredCurrency: String = "EUR",
        preferredLanguage: String = "ru",
        fcmToken: String? = null
    ): Result<String> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            val safeName = displayName.trim().ifBlank { "NailiX User" }.take(80)
            val safeRole = if (role == "master") "master" else "client"
            val safeLevel = if (loyaltyLevel in listOf("Silver", "Gold", "Platinum")) loyaltyLevel else "Silver"
            val myCode = existing.getString("referralCode")?.takeIf { it.length >= 6 }
                ?: generateReferralCodeForUid(uid)

            if (!existing.exists()) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "role" to safeRole,
                    "referralCode" to myCode,
                    "loyaltyPoints" to loyaltyPoints.coerceAtLeast(0).toLong(),
                    "loyaltyLevel" to safeLevel,
                    "friendsReferred" to friendsReferred.coerceAtLeast(0).toLong(),
                    "homeAddress" to homeAddress?.take(200),
                    "workAddress" to workAddress?.take(200),
                    "phone" to phone?.take(40),
                    "email" to email?.take(120),
                    "preferredSkin" to preferredSkin.take(30),
                    "preferredCurrency" to preferredCurrency.take(5),
                    "preferredLanguage" to preferredLanguage.take(5),
                    "fcmToken" to fcmToken?.take(512),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(createPayload).await()

                // Register personal referral code for honest cross-account redemption
                val codeRef = db.collection("referralCodes").document(myCode)
                if (!codeRef.get().await().exists()) {
                    codeRef.set(
                        mapOf(
                            "ownerUid" to uid,
                            "ownerName" to safeName,
                            "redemptionCount" to 0L,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            } else {
                val updatePayload = mutableMapOf<String, Any>(
                    "displayName" to safeName,
                    "role" to safeRole,
                    "loyaltyPoints" to loyaltyPoints.coerceAtLeast(0).toLong(),
                    "loyaltyLevel" to safeLevel,
                    "friendsReferred" to friendsReferred.coerceAtLeast(0).toLong(),
                    "preferredSkin" to preferredSkin.take(30),
                    "preferredCurrency" to preferredCurrency.take(5),
                    "preferredLanguage" to preferredLanguage.take(5),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                homeAddress?.take(200)?.let { updatePayload["homeAddress"] = it }
                workAddress?.take(200)?.let { updatePayload["workAddress"] = it }
                phone?.take(40)?.let { updatePayload["phone"] = it }
                email?.take(120)?.let { updatePayload["email"] = it }
                fcmToken?.take(512)?.let { updatePayload["fcmToken"] = it }
                docRef.update(updatePayload).await()
            }
            Result.success(myCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun recordLoyaltyTransaction(title: String, points: Int): Result<Unit> {
        var path = "users/unknown/loyalty_transactions"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("loyalty_transactions").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "title" to title.trim().ifBlank { "Бонусная операция" }.take(140),
                    "points" to points.coerceIn(-50000, 50000).toLong(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun publishMasterProfile(
        masterDocId: String,
        name: String,
        address: String,
        schedule: String,
        experienceYears: Int,
        languages: List<String>,
        instagram: String,
        currency: String,
        lat: Double? = null,
        lon: Double? = null
    ): Result<String> {
        var path = "masters/$masterDocId"
        return try {
            val uid = requireUserId()
            val safeId = masterDocId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "master_$uid" }.take(128)
            val docRef = db.collection("masters").document(safeId)
            path = docRef.path
            val existing = docRef.get().await()
            val safeLangs = languages.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(10)
            if (!existing.exists()) {
                val createMap = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "visibility" to "public",
                    "name" to name.trim().ifBlank { "NailiX Master" }.take(80),
                    "address" to address.trim().ifBlank { "Studio NailiX" }.take(200),
                    "workingSchedule" to schedule.trim().ifBlank { "10:00 - 20:00" }.take(100),
                    "experienceYears" to experienceYears.coerceIn(0, 70).toLong(),
                    "languages" to safeLangs,
                    "instagramUsername" to instagram.trim().removePrefix("@").take(60),
                    "currency" to currency.trim().ifBlank { "EUR" }.take(5),
                    "ratingAvg" to 5.0,
                    "ratingCount" to 1L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (lat != null && lat in -90.0..90.0) createMap["lat"] = lat
                if (lon != null && lon in -180.0..180.0) createMap["lon"] = lon
                docRef.set(createMap).await()
            } else {
                val updateMap = mutableMapOf<String, Any>(
                    "name" to name.trim().ifBlank { "NailiX Master" }.take(80),
                    "address" to address.trim().ifBlank { "Studio NailiX" }.take(200),
                    "workingSchedule" to schedule.trim().ifBlank { "10:00 - 20:00" }.take(100),
                    "experienceYears" to experienceYears.coerceIn(0, 70).toLong(),
                    "languages" to safeLangs,
                    "instagramUsername" to instagram.trim().removePrefix("@").take(60),
                    "currency" to currency.trim().ifBlank { "EUR" }.take(5),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (lat != null && lat in -90.0..90.0) updateMap["lat"] = lat
                if (lon != null && lon in -180.0..180.0) updateMap["lon"] = lon
                docRef.update(updateMap).await()
            }
            Result.success(safeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun createBooking(
        bookingDocId: String,
        booking: Booking,
        ticketToken: String,
        paymentMethod: String = "studio_visit",
        masterUid: String? = null
    ): Result<String> {
        var path = "bookings"
        return try {
            val uid = requireUserId()
            val safeId = bookingDocId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "bk_${System.currentTimeMillis()}" }.take(128)
            val docRef = db.collection("bookings").document(safeId)
            path = docRef.path
            val payload = mapOf(
                "clientUid" to uid,
                "masterUid" to masterUid?.takeIf { it.isNotBlank() }?.take(128),
                "serviceName" to booking.serviceName.trim().ifBlank { "Service" }.take(120),
                "serviceCategory" to booking.serviceCategory.trim().ifBlank { "Manicure" }.take(60),
                "date" to booking.date.trim().ifBlank { "Сегодня" }.take(40),
                "timeSlot" to booking.timeSlot.trim().ifBlank { "12:00" }.take(20),
                "masterName" to booking.masterName.trim().ifBlank { "NailiX Studio" }.take(100),
                "price" to booking.price.coerceAtLeast(0.0),
                "clientName" to booking.clientName.trim().ifBlank { "Client" }.take(100),
                "clientPhone" to booking.clientPhone.trim().ifBlank { "-" }.take(40),
                "paymentMethod" to if (paymentMethod == "studio_transfer") "studio_transfer" else "studio_visit",
                "status" to "Подтверждена",
                "ticketToken" to ticketToken.take(64).padEnd(8, '0'),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(safeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(bookingDocId: String, newStatus: String): Result<Unit> {
        val path = "bookings/$bookingDocId"
        return try {
            requireUserId()
            require(newStatus in listOf("Выполнена", "Отменена")) { "Invalid target status" }
            db.collection("bookings").document(bookingDocId).update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateBookingSchedule(bookingDocId: String, newDate: String, newTimeSlot: String): Result<Unit> {
        val path = "bookings/$bookingDocId"
        return try {
            requireUserId()
            val safeDate = newDate.trim().ifBlank { "Сегодня" }.take(40)
            val safeSlot = newTimeSlot.trim().ifBlank { "12:00" }.take(20)
            db.collection("bookings").document(bookingDocId).update(
                mapOf(
                    "date" to safeDate,
                    "timeSlot" to safeSlot,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun publishService(
        serviceDocId: String,
        name: String,
        category: String,
        price: Double,
        durationMin: Int,
        description: String,
        imageUrl: String
    ): Result<String> {
        var path = "services/$serviceDocId"
        return try {
            val uid = requireUserId()
            val safeId = serviceDocId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128)
            val docRef = db.collection("services").document(safeId)
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "visibility" to "public",
                    "name" to name.trim().ifBlank { "Услуга" }.take(100),
                    "category" to category.trim().ifBlank { "Маникюр" }.take(50),
                    "price" to price.coerceAtLeast(0.0),
                    "durationMin" to durationMin.coerceIn(5, 480).toLong(),
                    "description" to description.trim().take(500),
                    "imageUrl" to imageUrl.trim().take(500),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(safeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun publishNailArt(
        artDocId: String,
        title: String,
        masterName: String,
        complexity: String,
        extraPrice: Double,
        imageUrl: String,
        description: String
    ): Result<String> {
        var path = "nailArts/$artDocId"
        return try {
            val uid = requireUserId()
            val safeId = artDocId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128)
            val docRef = db.collection("nailArts").document(safeId)
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "visibility" to "public",
                    "title" to title.trim().ifBlank { "Nail Art" }.take(100),
                    "masterName" to masterName.trim().ifBlank { "Master" }.take(80),
                    "complexity" to complexity.trim().ifBlank { "Medium" }.take(30),
                    "extraPrice" to extraPrice.coerceAtLeast(0.0),
                    "imageUrl" to imageUrl.trim().take(500),
                    "likes" to 0L,
                    "views" to 0L,
                    "description" to description.trim().take(600),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(safeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun createReview(
        targetId: String,
        targetType: String,
        authorName: String,
        rating: Int,
        comment: String,
        dateStr: String
    ): Result<Unit> {
        var path = "reviews"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("reviews").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "authorUid" to uid,
                    "visibility" to "public",
                    "targetId" to targetId.trim().ifBlank { "studio" }.take(128),
                    "targetType" to if (targetType == "client") "client" else "master",
                    "authorName" to authorName.trim().ifBlank { "Гость" }.take(80),
                    "rating" to rating.coerceIn(1, 5).toLong(),
                    "comment" to comment.trim().ifBlank { "Отличный сервис" }.take(500),
                    "dateStr" to dateStr.trim().ifBlank { "Сегодня" }.take(30),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun redeemReferralCode(rawCode: String, claimerName: String): Result<String> {
        val code = rawCode.trim().uppercase()
        if (!code.matches(Regex("^[A-Z0-9_\\-]{6,24}$"))) {
            return Result.failure(IllegalArgumentException("Неверный формат кода. Пример: NLX-XXXXXX"))
        }
        val path = "referralCodes/$code"
        return try {
            val uid = requireUserId()
            val codeRef = db.collection("referralCodes").document(code)
            val codeSnapshot = codeRef.get().await()
            if (!codeSnapshot.exists()) {
                return Result.failure(IllegalStateException("Реферальный код $code не найден в системе."))
            }
            val ownerUid = codeSnapshot.getString("ownerUid") ?: ""
            if (ownerUid == uid) {
                return Result.failure(IllegalStateException("Нельзя активировать собственный реферальный код."))
            }
            val redemptionRef = codeRef.collection("redemptions").document(uid)
            if (redemptionRef.get().await().exists()) {
                return Result.failure(IllegalStateException("Вы уже активировали этот реферальный код."))
            }
            val ownerName = codeSnapshot.getString("ownerName") ?: "Подруга"

            val batch = db.batch()
            batch.set(
                redemptionRef,
                mapOf(
                    "claimerUid" to uid,
                    "claimerName" to claimerName.trim().ifBlank { "Клиент NailiX" }.take(80),
                    "code" to code,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                codeRef,
                mapOf(
                    "redemptionCount" to FieldValue.increment(1L),
                    "lastRedeemedBy" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(ownerName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
