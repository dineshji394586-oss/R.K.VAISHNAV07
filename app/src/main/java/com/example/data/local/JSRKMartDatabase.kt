package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        StoreEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CouponEntity::class,
        BannerEntity::class,
        NotificationEntity::class,
        WishlistEntity::class,
        AuditLogEntity::class,
        PartnerEarningsEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JSRKMartDatabase : RoomDatabase() {

    abstract fun dao(): JSRKMartDao

    companion object {
        @Volatile
        private var INSTANCE: JSRKMartDatabase? = null

        fun getInstance(context: Context): JSRKMartDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JSRKMartDatabase::class.java,
                    "jsr_kmart_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
