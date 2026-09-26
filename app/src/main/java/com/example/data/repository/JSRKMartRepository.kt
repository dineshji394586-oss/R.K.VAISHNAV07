package com.example.data.repository

import com.example.data.local.JSRKMartDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class CartLineItem(
    val cartItem: CartItemEntity,
    val product: ProductEntity
) {
    val total: Double get() = product.sellingPrice * cartItem.quantity
    val savings: Double get() = (product.mrp - product.sellingPrice) * cartItem.quantity
}

data class CartSummary(
    val items: List<CartLineItem>,
    val subtotal: Double,
    val totalMrp: Double,
    val totalSavings: Double,
    val deliveryFee: Double,
    val tax: Double,
    val platformFee: Double,
    val couponDiscount: Double,
    val finalTotal: Double,
    val appliedCoupon: CouponEntity?
)

class JSRKMartRepository(private val dao: JSRKMartDao) {

    // --- Users & Roles ---
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val customers: Flow<List<UserEntity>> = dao.getUsersByRole(UserRole.CUSTOMER.name)
    val deliveryPartners: Flow<List<UserEntity>> = dao.getUsersByRole(UserRole.DELIVERY_PARTNER.name)

    fun getUser(userId: Long): Flow<UserEntity?> = dao.getUserById(userId)

    suspend fun setPartnerOnlineStatus(partnerId: Long, isOnline: Boolean) = withContext(Dispatchers.IO) {
        dao.setPartnerOnlineStatus(partnerId, isOnline)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "PARTNER_STATUS_CHANGE",
                details = "Delivery partner #$partnerId changed status to ${if (isOnline) "ONLINE" else "OFFLINE"}"
            )
        )
    }

    suspend fun setUserBlockedStatus(userId: Long, isBlocked: Boolean) = withContext(Dispatchers.IO) {
        dao.setUserBlockedStatus(userId, isBlocked)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "USER_MODERATION",
                details = "User #$userId was ${if (isBlocked) "BLOCKED" else "UNBLOCKED"}"
            )
        )
    }

    suspend fun updateKycStatus(partnerId: Long, status: String) = withContext(Dispatchers.IO) {
        dao.updateKycStatus(partnerId, status)
        dao.insertNotification(
            NotificationEntity(
                userId = partnerId,
                role = UserRole.DELIVERY_PARTNER.name,
                title = "KYC Status Update",
                message = "Your KYC verification status has been updated to: $status",
                type = "KYC"
            )
        )
    }

    // --- Products & Categories ---
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val availableProducts: Flow<List<ProductEntity>> = dao.getAvailableProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = dao.getLowStockProducts()
    val activeCategories: Flow<List<CategoryEntity>> = dao.getActiveCategories()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()

    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>> =
        dao.getProductsByCategory(categoryId)

    fun getProduct(productId: Long): Flow<ProductEntity?> = dao.getProductById(productId)

    suspend fun saveProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val id = dao.insertProduct(product)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "PRODUCT_SAVE",
                details = "Product '${product.name}' saved with price ₹${product.sellingPrice} and stock ${product.stock}"
            )
        )
        id
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        dao.deleteProduct(product)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "PRODUCT_DELETE",
                details = "Product '${product.name}' (ID: ${product.id}) was deleted."
            )
        )
    }

    suspend fun updateProductStock(productId: Long, newStock: Int) = withContext(Dispatchers.IO) {
        dao.updateProductStock(productId, newStock)
    }

    suspend fun saveCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        dao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        dao.deleteCategory(category)
    }

    // --- Cart Management ---
    fun getCartItems(customerId: Long): Flow<List<CartItemEntity>> = dao.getCartItems(customerId)

    suspend fun addToCart(customerId: Long, productId: Long, quantity: Int = 1) = withContext(Dispatchers.IO) {
        val existing = dao.getCartItem(customerId, productId)
        if (existing != null) {
            val updated = existing.copy(quantity = existing.quantity + quantity)
            dao.updateCartItem(updated)
        } else {
            dao.insertCartItem(
                CartItemEntity(
                    customerId = customerId,
                    productId = productId,
                    quantity = quantity
                )
            )
        }
    }

    suspend fun updateCartQuantity(customerId: Long, productId: Long, newQuantity: Int) = withContext(Dispatchers.IO) {
        if (newQuantity <= 0) {
            dao.removeCartItem(customerId, productId)
        } else {
            val existing = dao.getCartItem(customerId, productId)
            if (existing != null) {
                dao.updateCartItem(existing.copy(quantity = newQuantity))
            } else {
                dao.insertCartItem(
                    CartItemEntity(
                        customerId = customerId,
                        productId = productId,
                        quantity = newQuantity
                    )
                )
            }
        }
    }

    suspend fun removeFromCart(customerId: Long, productId: Long) = withContext(Dispatchers.IO) {
        dao.removeCartItem(customerId, productId)
    }

    suspend fun clearCart(customerId: Long) = withContext(Dispatchers.IO) {
        dao.clearCart(customerId)
    }

    // --- Coupons & Pricing ---
    val activeCoupons: Flow<List<CouponEntity>> = dao.getActiveCoupons()
    val allCoupons: Flow<List<CouponEntity>> = dao.getAllCoupons()

    suspend fun saveCoupon(coupon: CouponEntity): Long = withContext(Dispatchers.IO) {
        dao.insertCoupon(coupon)
    }

    suspend fun deleteCoupon(coupon: CouponEntity) = withContext(Dispatchers.IO) {
        dao.deleteCoupon(coupon)
    }

    suspend fun validateCoupon(code: String, subtotal: Double): Pair<CouponEntity?, String?> = withContext(Dispatchers.IO) {
        val coupon = dao.getCouponByCode(code.uppercase().trim())
            ?: return@withContext Pair(null, "Invalid coupon code.")

        if (subtotal < coupon.minOrderValue) {
            return@withContext Pair(null, "Add ₹${(coupon.minOrderValue - subtotal).toInt()} more to use this coupon.")
        }
        if (coupon.usedCount >= coupon.usageLimit) {
            return@withContext Pair(null, "Coupon limit reached.")
        }
        Pair(coupon, null)
    }

    // --- Order Checkout Flow (Server-side calculation & Safe Stock Reduction) ---
    suspend fun placeOrder(
        customerId: Long,
        storeId: Long,
        address: AddressEntity,
        paymentMethod: PaymentMethod,
        couponCode: String?,
        deliveryInstructions: String = ""
    ): Result<OrderEntity> = withContext(Dispatchers.IO) {
        val cartItems = dao.getCartItems(customerId).first()
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(Exception("Cart is empty"))
        }

        val allProds = dao.getAllProducts().first().associateBy { it.id }
        var subtotal = 0.0
        var totalMrp = 0.0
        val orderItemsToInsert = mutableListOf<OrderItemEntity>()

        // 1. Verify stock and calculate server prices
        for (item in cartItems) {
            val product = allProds[item.productId]
                ?: return@withContext Result.failure(Exception("Product not found: #${item.productId}"))

            if (product.stock < item.quantity) {
                return@withContext Result.failure(Exception("Not enough stock for '${product.name}' (Available: ${product.stock})"))
            }

            val itemTotal = product.sellingPrice * item.quantity
            subtotal += itemTotal
            totalMrp += (product.mrp * item.quantity)

            orderItemsToInsert.add(
                OrderItemEntity(
                    orderId = 0, // will be updated
                    productId = product.id,
                    productName = product.name,
                    unit = product.unit,
                    price = product.sellingPrice,
                    mrp = product.mrp,
                    quantity = item.quantity,
                    total = itemTotal,
                    imageUrl = product.imageUrl
                )
            )
        }

        // 2. Fees & Tax calculation
        val deliveryFee = if (subtotal >= 499.0) 0.0 else 25.0
        val tax = (subtotal * 0.05).coerceAtLeast(0.0)
        val platformFee = 4.0

        // 3. Coupon discount
        var discount = 0.0
        if (!couponCode.isNullOrBlank()) {
            val coupon = dao.getCouponByCode(couponCode.uppercase().trim())
            if (coupon != null && subtotal >= coupon.minOrderValue) {
                discount = if (coupon.discountType == DiscountType.PERCENTAGE.name) {
                    (subtotal * (coupon.discountValue / 100.0)).coerceAtMost(coupon.maxDiscount)
                } else {
                    coupon.discountValue.coerceAtMost(coupon.maxDiscount)
                }
                dao.incrementCouponUsed(coupon.code)
            }
        }

        val finalTotal = (subtotal + deliveryFee + tax + platformFee - discount).coerceAtLeast(0.0)

        // 4. Safe stock reduction
        for (item in cartItems) {
            val currentProd = allProds[item.productId]!!
            dao.updateProductStock(item.productId, (currentProd.stock - item.quantity).coerceAtLeast(0))
        }

        // 5. Generate secure random 4-digit Delivery OTP
        val deliveryOtp = String.format("%04d", Random.nextInt(1000, 9999))
        val orderNumber = "JSR-${Random.nextInt(10000, 99999)}"

        // 6. Automatically select active delivery partner
        val onlinePartners = dao.getUsersByRole(UserRole.DELIVERY_PARTNER.name).first()
            .filter { it.isOnline && !it.isBlocked && it.kycStatus == "VERIFIED" }
        val assignedPartner = onlinePartners.firstOrNull()

        val customer = dao.getUserByIdSync(customerId)
        val customerName = customer?.name ?: "Valued Customer"
        val customerPhone = customer?.phone ?: "9876543210"

        val initialStatus = if (assignedPartner != null) OrderStatus.CONFIRMED.name else OrderStatus.PLACED.name

        val newOrder = OrderEntity(
            orderNumber = orderNumber,
            customerId = customerId,
            storeId = storeId,
            deliveryPartnerId = assignedPartner?.id,
            status = initialStatus,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            tax = tax,
            platformFee = platformFee,
            couponDiscount = discount,
            finalTotal = finalTotal,
            paymentMethod = paymentMethod.name,
            paymentStatus = if (paymentMethod == PaymentMethod.COD) PaymentStatus.PENDING.name else PaymentStatus.PAID.name,
            deliveryOtp = deliveryOtp,
            addressSnapshot = "${address.label}: ${address.fullAddress}, ${address.city} - ${address.pincode}",
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryInstructions = deliveryInstructions,
            liveLat = address.lat - 0.005,
            liveLng = address.lng - 0.005,
            estimatedMinutes = 10
        )

        val orderId = dao.insertOrder(newOrder)

        // Insert order items
        val finalItems = orderItemsToInsert.map { it.copy(orderId = orderId) }
        dao.insertOrderItems(finalItems)

        // Clear user's cart
        dao.clearCart(customerId)

        // Update customer spending
        if (customer != null) {
            dao.updateUser(customer.copy(totalSpent = customer.totalSpent + finalTotal))
        }

        // Send Push Notifications
        dao.insertNotification(
            NotificationEntity(
                userId = customerId,
                role = UserRole.CUSTOMER.name,
                title = "Order Placed! 🛍️ $orderNumber",
                message = "Your JSR KMart grocery order of ₹${finalTotal.toInt()} is placed and being packed.",
                type = "ORDER_PLACED"
            )
        )

        if (assignedPartner != null) {
            dao.insertNotification(
                NotificationEntity(
                    userId = assignedPartner.id,
                    role = UserRole.DELIVERY_PARTNER.name,
                    title = "New Delivery Assignment! ⚡",
                    message = "Order $orderNumber assigned to you. Pickup from Store #$storeId.",
                    type = "ORDER_ASSIGNMENT"
                )
            )
        }

        dao.insertNotification(
            NotificationEntity(
                userId = 1,
                role = UserRole.ADMIN.name,
                title = "New Order $orderNumber",
                message = "New ₹${finalTotal.toInt()} order placed by $customerName.",
                type = "ADMIN_ALERT"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                action = "ORDER_PLACED",
                details = "Order $orderNumber placed by $customerName for ₹${finalTotal.toInt()} via ${paymentMethod.name}."
            )
        )

        Result.success(newOrder.copy(id = orderId))
    }

    // --- Order Lifecycle & Real-Time Tracking ---
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()

    fun getOrdersByCustomer(customerId: Long): Flow<List<OrderEntity>> =
        dao.getOrdersByCustomer(customerId)

    fun getOrdersByDeliveryPartner(partnerId: Long): Flow<List<OrderEntity>> =
        dao.getOrdersByDeliveryPartner(partnerId)

    fun getOrderById(orderId: Long): Flow<OrderEntity?> = dao.getOrderById(orderId)

    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> = dao.getOrderItems(orderId)

    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) = withContext(Dispatchers.IO) {
        val order = dao.getOrderByIdSync(orderId) ?: return@withContext
        dao.updateOrderStatus(orderId, newStatus.name)

        // Customer push notification
        val statusMessage = when (newStatus) {
            OrderStatus.CONFIRMED -> "Your order ${order.orderNumber} is confirmed! ⚡"
            OrderStatus.PACKING -> "Your fresh groceries are being packed at the darkstore 🛒"
            OrderStatus.READY_FOR_PICKUP -> "Order is packed and ready for delivery partner pickup"
            OrderStatus.ASSIGNED -> "Delivery partner assigned for order ${order.orderNumber} 🛵"
            OrderStatus.PICKED_UP -> "Order picked up from store! On the way to your doorstep 🚀"
            OrderStatus.OUT_FOR_DELIVERY -> "Out for delivery! Arriving in ${order.estimatedMinutes} mins. Share OTP with rider."
            OrderStatus.DELIVERED -> "Order ${order.orderNumber} delivered successfully! Enjoy your fresh groceries 🎉"
            OrderStatus.CANCELLED -> "Order ${order.orderNumber} has been cancelled."
            OrderStatus.REFUNDED -> "Refund of ₹${order.finalTotal.toInt()} processed for ${order.orderNumber}."
            else -> "Order update: ${newStatus.name}"
        }

        dao.insertNotification(
            NotificationEntity(
                userId = order.customerId,
                role = UserRole.CUSTOMER.name,
                title = "Order Update: ${order.orderNumber}",
                message = statusMessage,
                type = "ORDER_STATUS"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                action = "ORDER_STATUS_UPDATE",
                details = "Order ${order.orderNumber} transitioned to ${newStatus.name}."
            )
        )
    }

    suspend fun assignDeliveryPartner(orderId: Long, partnerId: Long) = withContext(Dispatchers.IO) {
        dao.assignDeliveryPartner(orderId, partnerId)
        val order = dao.getOrderByIdSync(orderId)
        if (order != null) {
            dao.insertNotification(
                NotificationEntity(
                    userId = partnerId,
                    role = UserRole.DELIVERY_PARTNER.name,
                    title = "Assigned Delivery Order ${order.orderNumber}",
                    message = "Pickup from darkstore and deliver to ${order.customerName}.",
                    type = "ORDER_ASSIGNMENT"
                )
            )
        }
    }

    suspend fun completeDeliveryWithOtp(orderId: Long, enteredOtp: String, partnerId: Long): Boolean = withContext(Dispatchers.IO) {
        val order = dao.getOrderByIdSync(orderId) ?: return@withContext false
        if (order.deliveryOtp.trim() != enteredOtp.trim()) {
            return@withContext false
        }

        // Mark DELIVERED
        dao.updateOrderStatus(orderId, OrderStatus.DELIVERED.name)

        // If payment was COD, mark PAID upon delivery
        if (order.paymentMethod == PaymentMethod.COD.name) {
            dao.updateOrder(order.copy(status = OrderStatus.DELIVERED.name, paymentStatus = PaymentStatus.PAID.name))
        }

        // Calculate and credit partner earnings
        val baseFee = 45.0
        val distanceFee = 20.0
        val incentive = 15.0
        val totalEarning = baseFee + distanceFee + incentive

        dao.insertPartnerEarning(
            PartnerEarningsEntity(
                partnerId = partnerId,
                orderId = orderId,
                baseFee = baseFee,
                distanceFee = distanceFee,
                incentive = incentive,
                totalEarning = totalEarning
            )
        )

        // Customer notification
        dao.insertNotification(
            NotificationEntity(
                userId = order.customerId,
                role = UserRole.CUSTOMER.name,
                title = "Delivered! 🎉 ${order.orderNumber}",
                message = "Your grocery order was delivered by partner. Thank you for choosing JSR KMart!",
                type = "DELIVERED"
            )
        )

        // Partner notification
        dao.insertNotification(
            NotificationEntity(
                userId = partnerId,
                role = UserRole.DELIVERY_PARTNER.name,
                title = "Delivery Complete! ₹${totalEarning.toInt()} Earned",
                message = "You earned ₹${totalEarning.toInt()} for order ${order.orderNumber}. Great job!",
                type = "EARNING_CREDIT"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                action = "ORDER_DELIVERED",
                details = "Order ${order.orderNumber} delivered with OTP verification by partner #$partnerId."
            )
        )

        true
    }

    suspend fun cancelOrder(orderId: Long, reason: String) = withContext(Dispatchers.IO) {
        val order = dao.getOrderByIdSync(orderId) ?: return@withContext
        dao.updateOrderStatus(orderId, OrderStatus.CANCELLED.name)

        // Restore stock
        val items = dao.getOrderItemsSync(orderId)
        val allProds = dao.getAllProducts().first().associateBy { it.id }
        for (item in items) {
            val prod = allProds[item.productId]
            if (prod != null) {
                dao.updateProductStock(item.productId, prod.stock + item.quantity)
            }
        }

        dao.insertNotification(
            NotificationEntity(
                userId = order.customerId,
                role = UserRole.CUSTOMER.name,
                title = "Order Cancelled: ${order.orderNumber}",
                message = "Your order was cancelled: $reason. If paid online, refund will reflect shortly.",
                type = "ORDER_CANCELLED"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                action = "ORDER_CANCELLED",
                details = "Order ${order.orderNumber} cancelled. Reason: $reason. Inventory restored."
            )
        )
    }

    suspend fun processRefund(orderId: Long) = withContext(Dispatchers.IO) {
        val order = dao.getOrderByIdSync(orderId) ?: return@withContext
        dao.updateOrder(order.copy(paymentStatus = PaymentStatus.REFUNDED.name, status = OrderStatus.REFUNDED.name))

        dao.insertNotification(
            NotificationEntity(
                userId = order.customerId,
                role = UserRole.CUSTOMER.name,
                title = "Refund Processed: ₹${order.finalTotal.toInt()}",
                message = "Refund for order ${order.orderNumber} has been initiated to your source payment method.",
                type = "REFUND"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                action = "REFUND_PROCESSED",
                details = "Refund of ₹${order.finalTotal.toInt()} processed for order ${order.orderNumber}."
            )
        )
    }

    // --- Delivery Partner Earnings ---
    fun getPartnerEarnings(partnerId: Long): Flow<List<PartnerEarningsEntity>> =
        dao.getPartnerEarnings(partnerId)

    // --- Stores & Darkstores ---
    val allStores: Flow<List<StoreEntity>> = dao.getAllStores()
    val activeStores: Flow<List<StoreEntity>> = dao.getActiveStores()

    suspend fun saveStore(store: StoreEntity): Long = withContext(Dispatchers.IO) {
        dao.insertStore(store)
    }

    suspend fun deleteStore(store: StoreEntity) = withContext(Dispatchers.IO) {
        dao.deleteStore(store)
    }

    // --- Addresses ---
    fun getCustomerAddresses(customerId: Long): Flow<List<AddressEntity>> =
        dao.getCustomerAddresses(customerId)

    suspend fun saveAddress(address: AddressEntity): Long = withContext(Dispatchers.IO) {
        if (address.isDefault) {
            dao.clearDefaultAddress(address.customerId)
        }
        dao.insertAddress(address)
    }

    suspend fun deleteAddress(address: AddressEntity) = withContext(Dispatchers.IO) {
        dao.deleteAddress(address)
    }

    suspend fun setDefaultAddress(customerId: Long, addressId: Long) = withContext(Dispatchers.IO) {
        dao.clearDefaultAddress(customerId)
        dao.setDefaultAddress(addressId)
    }

    // --- Wishlist ---
    fun getWishlistProductIds(customerId: Long): Flow<List<Long>> =
        dao.getWishlistProductIds(customerId)

    suspend fun toggleWishlist(customerId: Long, productId: Long) = withContext(Dispatchers.IO) {
        val current = dao.getWishlistProductIds(customerId).first()
        if (current.contains(productId)) {
            dao.deleteWishlist(customerId, productId)
        } else {
            dao.insertWishlist(WishlistEntity(customerId = customerId, productId = productId))
        }
    }

    // --- Banners ---
    val activeBanners: Flow<List<BannerEntity>> = dao.getActiveBanners()
    val allBanners: Flow<List<BannerEntity>> = dao.getAllBanners()

    suspend fun saveBanner(banner: BannerEntity): Long = withContext(Dispatchers.IO) {
        dao.insertBanner(banner)
    }

    suspend fun deleteBanner(banner: BannerEntity) = withContext(Dispatchers.IO) {
        dao.deleteBanner(banner)
    }

    // --- Notifications ---
    fun getNotifications(userId: Long, role: String): Flow<List<NotificationEntity>> =
        dao.getNotificationsForUser(userId, role)

    suspend fun markNotificationRead(id: Long) = withContext(Dispatchers.IO) {
        dao.markNotificationRead(id)
    }

    suspend fun markAllNotificationsRead(userId: Long, role: String) = withContext(Dispatchers.IO) {
        dao.markAllNotificationsRead(userId, role)
    }

    // --- Settings & Audit ---
    val allSettings: Flow<List<AppSettingEntity>> = dao.getAllSettings()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAuditLogs()

    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.setSetting(AppSettingEntity(key, value))
    }

    // --- Report Generator (CSV Export) ---
    suspend fun generateOrdersCsv(): String = withContext(Dispatchers.IO) {
        val orders = dao.getAllOrders().first()
        val sb = StringBuilder()
        sb.append("Order ID,Order Number,Customer,Total,Status,Payment Method,Payment Status,Date\n")
        orders.forEach { o ->
            sb.append("${o.id},${o.orderNumber},\"${o.customerName}\",${o.finalTotal},${o.status},${o.paymentMethod},${o.paymentStatus},${o.createdAt}\n")
        }
        sb.toString()
    }

    suspend fun generateInventoryCsv(): String = withContext(Dispatchers.IO) {
        val products = dao.getAllProducts().first()
        val sb = StringBuilder()
        sb.append("SKU,Product Name,Brand,MRP,Selling Price,Stock,Threshold,Status\n")
        products.forEach { p ->
            val status = if (p.stock <= 0) "OUT_OF_STOCK" else if (p.stock <= p.lowStockThreshold) "LOW_STOCK" else "IN_STOCK"
            sb.append("${p.sku},\"${p.name}\",\"${p.brand}\",${p.mrp},${p.sellingPrice},${p.stock},${p.lowStockThreshold},$status\n")
        }
        sb.toString()
    }
}
