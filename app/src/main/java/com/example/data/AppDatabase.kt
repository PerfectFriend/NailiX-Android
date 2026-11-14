package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Booking::class,
        UserLoyalty::class,
        Reminder::class,
        FavoriteDesign::class,
        ServiceEntity::class,
        ReferredFriend::class,
        LoyaltyTransaction::class,
        LocalizationEntity::class,
        MasterNailArtEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
    abstract fun loyaltyDao(): LoyaltyDao
    abstract fun reminderDao(): ReminderDao
    abstract fun favoriteDesignDao(): FavoriteDesignDao
    abstract fun serviceDao(): ServiceDao
    abstract fun referredFriendDao(): ReferredFriendDao
    abstract fun loyaltyTransactionDao(): LoyaltyTransactionDao
    abstract fun localizationDao(): LocalizationDao
    abstract fun masterNailArtDao(): MasterNailArtDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "victoria_studio_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
