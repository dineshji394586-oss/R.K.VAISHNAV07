package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JSRKMartDao {

    // Users
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role ORDER BY id ASC")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isOnline = :isOnline WHERE id = :partnerId")
    suspend fun setPartnerOnlineStatus(partnerId: Long, isOnline: Boolean)

    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :userId")
    suspend fun setUserBlockedStatus(userId: Long, isBlocked: Boolean)

    @Query("UPDATE users SET kycStatus = :status WHERE id = :partnerId")
    suspend fun updateKycStatus(partnerId: Long, status: String)

    // Stores
    @Query("SELECT * FROM stores ORDER BY id ASC")
    fun getAllStores(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveStores(): Flow<List<StoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreEntity>)

    @Update
    suspend fun updateStore(store: StoreEntity)

    @Delete
    suspend fun deleteStore(store: StoreEntity)

    // Categories
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    // Products
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY isBestSeller DESC, id ASC")
    fun getAvailableProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isAvailable = 1 ORDER BY id ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    fun getProductById(productId: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductByIdSync(productId: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE stock <= lowStockThreshold ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: Long, newStock: Int)

    // Cart
    @Query("SELECT * FROM cart_items WHERE customerId = :customerId ORDER BY addedAt DESC")
    fun getCartItems(customerId: Long): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE customerId = :customerId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(customerId: Long, productId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity): Long

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId AND productId = :productId")
    suspend fun removeCartItem(customerId: Long, productId: Long)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId")
    suspend fun clearCart(customerId: Long)

    // Addresses
    @Query("SELECT * FROM addresses WHERE customerId = :customerId ORDER BY isDefault DESC, id DESC")
    fun getCustomerAddresses(customerId: Long): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<AddressEntity>)

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0 WHERE customerId = :customerId")
    suspend fun clearDefaultAddress(customerId: Long)

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :addressId")
    suspend fun setDefaultAddress(addressId: Long)

    // Orders
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersByCustomer(customerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE deliveryPartnerId = :partnerId ORDER BY createdAt DESC")
    fun getOrdersByDeliveryPartner(partnerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderById(orderId: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderByIdSync(orderId: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET deliveryPartnerId = :partnerId, status = 'ASSIGNED', updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun assignDeliveryPartner(orderId: Long, partnerId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET liveLat = :lat, liveLng = :lng, estimatedMinutes = :minutes WHERE id = :orderId")
    suspend fun updateOrderLiveLocation(orderId: Long, lat: Double, lng: Double, minutes: Int)

    // Order Items
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsSync(orderId: Long): List<OrderItemEntity>

    @Query("SELECT * FROM order_items")
    fun getAllOrderItems(): Flow<List<OrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    // Coupons
    @Query("SELECT * FROM coupons ORDER BY id ASC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE isActive = 1")
    fun getActiveCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Delete
    suspend fun deleteCoupon(coupon: CouponEntity)

    @Query("UPDATE coupons SET usedCount = usedCount + 1 WHERE code = :code")
    suspend fun incrementCouponUsed(code: String)

    // Banners
    @Query("SELECT * FROM banners ORDER BY displayOrder ASC")
    fun getAllBanners(): Flow<List<BannerEntity>>

    @Query("SELECT * FROM banners WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanners(banners: List<BannerEntity>)

    @Update
    suspend fun updateBanner(banner: BannerEntity)

    @Delete
    suspend fun deleteBanner(banner: BannerEntity)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId OR role = :role OR role = 'ALL' ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: Long, role: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR role = :role OR role = 'ALL'")
    suspend fun markAllNotificationsRead(userId: Long, role: String)

    // Wishlist
    @Query("SELECT productId FROM wishlists WHERE customerId = :customerId")
    fun getWishlistProductIds(customerId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistEntity)

    @Query("DELETE FROM wishlists WHERE customerId = :customerId AND productId = :productId")
    suspend fun deleteWishlist(customerId: Long, productId: Long)

    // Partner Earnings
    @Query("SELECT * FROM partner_earnings WHERE partnerId = :partnerId ORDER BY createdAt DESC")
    fun getPartnerEarnings(partnerId: Long): Flow<List<PartnerEarningsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartnerEarning(earning: PartnerEarningsEntity): Long

    // Audit logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Settings
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingEntity>>

    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
