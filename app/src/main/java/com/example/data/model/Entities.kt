package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    DELIVERY_PARTNER,
    ADMIN
}

enum class OrderStatus {
    PLACED,
    CONFIRMED,
    PACKING,
    READY_FOR_PICKUP,
    ASSIGNED,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    REFUNDED
}

enum class PaymentMethod {
    COD,
    UPI,
    CARD,
    NET_BANKING
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

enum class DiscountType {
    PERCENTAGE,
    FIXED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // UserRole name
    val name: String,
    val phone: String,
    val email: String,
    val avatarUrl: String = "",
    val kycStatus: String = "VERIFIED", // PENDING, VERIFIED, REJECTED
    val vehicleType: String = "BIKE", // BIKE, SCOOTER, EV
    val vehicleNumber: String = "KA05 KM 1234",
    val isOnline: Boolean = true,
    val isBlocked: Boolean = false,
    val totalSpent: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val serviceRadiusKm: Double = 7.0,
    val openingHours: String = "6:00 AM - 11:30 PM",
    val manager: String = "Rajesh Verma",
    val isActive: Boolean = true
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String, // e.g. "veg", "dairy", "bakery", etc.
    val imageUrl: String = "",
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val description: String,
    val mrp: Double,
    val sellingPrice: Double,
    val discountPercent: Int,
    val unit: String, // e.g. "500 g", "1 L", "1 kg", "Pack of 4"
    val weight: String = "",
    val stock: Int,
    val lowStockThreshold: Int = 10,
    val sku: String,
    val categoryId: Long,
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val rating: Float = 4.7f,
    val reviewCount: Int = 120,
    val imageUrl: String = "" // drawable name or url
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val productId: Long,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val label: String, // "Home", "Work", "Other"
    val fullAddress: String,
    val landmark: String = "",
    val city: String = "Bengaluru",
    val pincode: String = "560038",
    val lat: Double = 12.9716,
    val lng: Double = 77.5946,
    val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String, // e.g. "JSR-89421"
    val customerId: Long,
    val storeId: Long,
    val deliveryPartnerId: Long? = null,
    val status: String, // OrderStatus name
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val platformFee: Double = 4.0,
    val couponDiscount: Double = 0.0,
    val finalTotal: Double,
    val paymentMethod: String, // COD, UPI, CARD, NET_BANKING
    val paymentStatus: String, // PENDING, PAID, FAILED, REFUNDED
    val deliveryOtp: String = "4826",
    val addressSnapshot: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryInstructions: String = "",
    val liveLat: Double = 12.9720,
    val liveLng: Double = 77.5950,
    val estimatedMinutes: Int = 10,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val unit: String,
    val price: Double,
    val mrp: Double,
    val quantity: Int,
    val total: Double,
    val imageUrl: String = ""
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val description: String,
    val discountType: String, // PERCENTAGE or FIXED
    val discountValue: Double,
    val minOrderValue: Double,
    val maxDiscount: Double = 200.0,
    val expiryDate: String = "31 Dec 2026",
    val usageLimit: Int = 100,
    val usedCount: Int = 0,
    val isActive: Boolean = true
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val badgeText: String = "",
    val categoryId: Long? = null,
    val drawableName: String = "",
    val isActive: Boolean = true,
    val displayOrder: Int = 0
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val role: String, // CUSTOMER, DELIVERY_PARTNER, ADMIN, ALL
    val title: String,
    val message: String,
    val type: String = "ORDER_UPDATE",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlists")
data class WishlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "partner_earnings")
data class PartnerEarningsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partnerId: Long,
    val orderId: Long,
    val baseFee: Double = 45.0,
    val distanceFee: Double = 15.0,
    val incentive: Double = 10.0,
    val tip: Double = 0.0,
    val totalEarning: Double = 70.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

val OrderEntity.finalAmount: Double get() = finalTotal
val OrderEntity.deliveryAddress: String get() = addressSnapshot
val AddressEntity.receiverName: String get() = label
val AddressEntity.receiverPhone: String get() = "+91 98765 43210"
val PartnerEarningsEntity.amount: Double get() = totalEarning
val PartnerEarningsEntity.bonusAmount: Double get() = incentive
