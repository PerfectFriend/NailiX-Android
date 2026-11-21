package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY date DESC, timeSlot DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: Int)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Int, status: String)

    @Query("UPDATE bookings SET date = :date, timeSlot = :timeSlot WHERE id = :id")
    suspend fun updateBookingSchedule(id: Int, date: String, timeSlot: String)
}

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM user_loyalty WHERE id = 1 LIMIT 1")
    fun getLoyaltyFlow(): Flow<UserLoyalty?>

    @Query("SELECT * FROM user_loyalty WHERE id = 1 LIMIT 1")
    suspend fun getLoyaltyDirect(): UserLoyalty?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLoyalty(loyalty: UserLoyalty)

    @Update
    suspend fun updateLoyalty(loyalty: UserLoyalty)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timestamp DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Query("UPDATE reminders SET isSent = 1 WHERE id = :id")
    suspend fun markAsSent(id: Int)

    @Query("DELETE FROM reminders WHERE bookingId = :bookingId")
    suspend fun deleteRemindersForBooking(bookingId: Int)
}

@Dao
interface FavoriteDesignDao {
    @Query("SELECT * FROM favorite_designs")
    fun getAllFavorites(): Flow<List<FavoriteDesign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteDesign)

    @Query("DELETE FROM favorite_designs WHERE designId = :designId")
    suspend fun removeFavorite(designId: Int)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY category, name")
    fun getAllServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services")
    suspend fun getAllServicesDirect(): List<ServiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Query("UPDATE services SET price = :price WHERE id = :id")
    suspend fun updateServicePrice(id: Int, price: Double)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: Int)
}

@Dao
interface ReferredFriendDao {
    @Query("SELECT * FROM referred_friends ORDER BY id DESC")
    fun getAllReferredFriends(): Flow<List<ReferredFriend>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferredFriend(friend: ReferredFriend)

    @Query("DELETE FROM referred_friends")
    suspend fun deleteAllReferredFriends()
}

@Dao
interface LoyaltyTransactionDao {
    @Query("SELECT * FROM loyalty_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<LoyaltyTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LoyaltyTransaction)

    @Query("DELETE FROM loyalty_transactions")
    suspend fun deleteAllTransactions()
}

@Dao
interface LocalizationDao {
    @Query("SELECT * FROM localizations")
    fun getAllLocalizations(): Flow<List<LocalizationEntity>>

    @Query("SELECT * FROM localizations")
    suspend fun getAllLocalizationsDirect(): List<LocalizationEntity>

    @Query("SELECT * FROM localizations WHERE langCode = :lang")
    fun getLocalizationsForLang(lang: String): Flow<List<LocalizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalizations(localizations: List<LocalizationEntity>)

    @Query("DELETE FROM localizations")
    suspend fun deleteAllLocalizations()
}

@Dao
interface MasterNailArtDao {
    @Query("SELECT * FROM master_nail_arts ORDER BY likes DESC, id DESC")
    fun getAllMasterNailArts(): Flow<List<MasterNailArtEntity>>

    @Query("SELECT * FROM master_nail_arts")
    suspend fun getAllMasterNailArtsDirect(): List<MasterNailArtEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterNailArts(items: List<MasterNailArtEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterNailArt(item: MasterNailArtEntity): Long

    @Query("UPDATE master_nail_arts SET likes = likes + 1 WHERE id = :id")
    suspend fun incrementLikes(id: Int)

    @Query("UPDATE master_nail_arts SET views = views + 1 WHERE id = :id")
    suspend fun incrementViews(id: Int)

    @Query("DELETE FROM master_nail_arts")
    suspend fun deleteAllMasterNailArts()
}

