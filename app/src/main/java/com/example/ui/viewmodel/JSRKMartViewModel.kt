package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseInitializer
import com.example.data.local.JSRKMartDatabase
import com.example.data.model.*
import com.example.data.repository.CartLineItem
import com.example.data.repository.CartSummary
import com.example.data.repository.JSRKMartRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CustomerTab {
    HOME,
    CATEGORIES,
    SEARCH,
    CART,
    ORDERS,
    PROFILE
}

enum class DeliveryTab {
    HOME,
    ORDERS,
    EARNINGS,
    PROFILE
}

enum class AdminTab {
    DASHBOARD,
    ORDERS,
    PRODUCTS,
    CATEGORIES,
    INVENTORY,
    CUSTOMERS,
    DELIVERY_PARTNERS,
    STORES,
    COUPONS,
    BANNERS,
    PAYMENTS,
    REPORTS,
    SETTINGS
}

enum class ProductSortOrder {
    POPULARITY,
    PRICE_LOW_HIGH,
    PRICE_HIGH_LOW,
    DISCOUNT
}

data class UiNotificationMessage(
    val title: String,
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class JSRKMartViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JSRKMartDatabase.getInstance(application)
    private val dao = db.dao()
    val repository = JSRKMartRepository(dao)

    // Current Active Role & User
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentCustomerId = MutableStateFlow<Long>(2) // Aarav Sharma by default
    val currentCustomerId: StateFlow<Long> = _currentCustomerId.asStateFlow()

    private val _currentPartnerId = MutableStateFlow<Long>(7) // Sunil Gowda
    val currentPartnerId: StateFlow<Long> = _currentPartnerId.asStateFlow()

    // Navigation Tabs per role
    private val _customerTab = MutableStateFlow(CustomerTab.HOME)
    val customerTab: StateFlow<CustomerTab> = _customerTab.asStateFlow()

    private val _deliveryTab = MutableStateFlow(DeliveryTab.HOME)
    val deliveryTab: StateFlow<DeliveryTab> = _deliveryTab.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    // Transient UI toast / notification
    private val _uiMessage = MutableStateFlow<UiNotificationMessage?>(null)
    val uiMessage: StateFlow<UiNotificationMessage?> = _uiMessage.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _sortOrder = MutableStateFlow(ProductSortOrder.POPULARITY)
    val sortOrder: StateFlow<ProductSortOrder> = _sortOrder.asStateFlow()

    // Tracking & Detail Modals
    private val _trackingOrderId = MutableStateFlow<Long?>(null)
    val trackingOrderId: StateFlow<Long?> = _trackingOrderId.asStateFlow()

    private val _checkoutCouponCode = MutableStateFlow("")
    val checkoutCouponCode: StateFlow<String> = _checkoutCouponCode.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.UPI)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _deliveryInstructions = MutableStateFlow("")
    val deliveryInstructions: StateFlow<String> = _deliveryInstructions.asStateFlow()

    // Observables from Repository
    val allProducts = repository.allProducts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val banners = repository.activeBanners.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val coupons = repository.allCoupons.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allStores = repository.allStores.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allOrders = repository.allOrders.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val customersList = repository.customers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val deliveryPartnersList = repository.deliveryPartners.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val auditLogs = repository.auditLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val appSettings = repository.allSettings.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Wishlist for active customer
    val customerWishlist = _currentCustomerId.flatMapLatest { id ->
        repository.getWishlistProductIds(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Cart for active customer
    val cartSummary: StateFlow<CartSummary> = combine(
        _currentCustomerId.flatMapLatest { repository.getCartItems(it) },
        allProducts,
        _checkoutCouponCode,
        coupons
    ) { cartItems, prods, couponCode, allCouponsList ->
        val prodMap = prods.associateBy { it.id }
        val lineItems = cartItems.mapNotNull { item ->
            val p = prodMap[item.productId]
            if (p != null) CartLineItem(item, p) else null
        }

        var subtotal = 0.0
        var totalMrp = 0.0
        var totalSavings = 0.0
        for (line in lineItems) {
            subtotal += line.total
            totalMrp += (line.product.mrp * line.cartItem.quantity)
            totalSavings += line.savings
        }

        val deliveryFee = if (subtotal >= 499.0 || subtotal == 0.0) 0.0 else 25.0
        val tax = (subtotal * 0.05).coerceAtLeast(0.0)
        val platformFee = if (subtotal > 0) 4.0 else 0.0

        var couponDiscount = 0.0
        var matchedCoupon: CouponEntity? = null
        if (couponCode.isNotBlank() && subtotal > 0) {
            matchedCoupon = allCouponsList.firstOrNull { it.code.equals(couponCode.trim(), ignoreCase = true) && it.isActive }
            if (matchedCoupon != null && subtotal >= matchedCoupon.minOrderValue) {
                couponDiscount = if (matchedCoupon.discountType == DiscountType.PERCENTAGE.name) {
                    (subtotal * (matchedCoupon.discountValue / 100.0)).coerceAtMost(matchedCoupon.maxDiscount)
                } else {
                    matchedCoupon.discountValue.coerceAtMost(matchedCoupon.maxDiscount)
                }
            }
        }

        val finalTotal = (subtotal + deliveryFee + tax + platformFee - couponDiscount).coerceAtLeast(0.0)

        CartSummary(
            items = lineItems,
            subtotal = subtotal,
            totalMrp = totalMrp,
            totalSavings = totalSavings,
            deliveryFee = deliveryFee,
            tax = tax,
            platformFee = platformFee,
            couponDiscount = couponDiscount,
            finalTotal = finalTotal,
            appliedCoupon = matchedCoupon
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        CartSummary(emptyList(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null)
    )

    // Customer Addresses
    val customerAddresses = _currentCustomerId.flatMapLatest { id ->
        repository.getCustomerAddresses(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active Customer Orders
    val customerOrders = _currentCustomerId.flatMapLatest { id ->
        repository.getOrdersByCustomer(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active Partner Orders & Earnings
    val partnerOrders = _currentPartnerId.flatMapLatest { id ->
        repository.getOrdersByDeliveryPartner(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val partnerEarnings = _currentPartnerId.flatMapLatest { id ->
        repository.getPartnerEarnings(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Notifications for current role
    val currentNotifications = combine(_currentRole, _currentCustomerId, _currentPartnerId) { role, custId, partId ->
        val userId = when (role) {
            UserRole.CUSTOMER -> custId
            UserRole.DELIVERY_PARTNER -> partId
            UserRole.ADMIN -> 1L
        }
        repository.getNotifications(userId, role.name)
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            DatabaseInitializer.seedDatabaseIfEmpty(dao)
            // Simulated live tracking background updater for active orders
            startLiveLocationSimulator()
        }
    }

    private fun startLiveLocationSimulator() {
        viewModelScope.launch {
            while (true) {
                delay(4000)
                val outOrders = dao.getOrdersByStatus(OrderStatus.OUT_FOR_DELIVERY.name).first()
                for (o in outOrders) {
                    val remainingMins = (o.estimatedMinutes - 1).coerceAtLeast(1)
                    val newLat = o.liveLat + 0.0006
                    val newLng = o.liveLng + 0.0004
                    dao.updateOrderLiveLocation(o.id, newLat, newLng, remainingMins)
                }
            }
        }
    }

    // Role & Navigation Setters
    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setCustomerTab(tab: CustomerTab) {
        _customerTab.value = tab
    }

    fun setDeliveryTab(tab: DeliveryTab) {
        _deliveryTab.value = tab
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun selectCustomer(id: Long) {
        _currentCustomerId.value = id
    }

    fun selectDeliveryPartner(id: Long) {
        _currentPartnerId.value = id
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun selectCategory(catId: Long?) {
        _selectedCategoryId.value = catId
    }

    fun setSortOrder(order: ProductSortOrder) {
        _sortOrder.value = order
    }

    fun openOrderTracking(orderId: Long?) {
        _trackingOrderId.value = orderId
    }

    fun setCouponCode(code: String) {
        _checkoutCouponCode.value = code
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun setDeliveryInstructions(inst: String) {
        _deliveryInstructions.value = inst
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showToast(title: String, message: String, isError: Boolean = false) {
        _uiMessage.value = UiNotificationMessage(title, message, isError)
    }

    // Customer Actions
    fun addToCart(productId: Long, qty: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(_currentCustomerId.value, productId, qty)
            showToast("Added to Cart", "Item added to your JSR KMart basket 🛒")
        }
    }

    fun updateCartQty(productId: Long, qty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(_currentCustomerId.value, productId, qty)
        }
    }

    fun toggleWishlist(productId: Long) {
        viewModelScope.launch {
            repository.toggleWishlist(_currentCustomerId.value, productId)
        }
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val subtotal = cartSummary.value.subtotal
            val (coupon, error) = repository.validateCoupon(code, subtotal)
            if (coupon != null) {
                _checkoutCouponCode.value = coupon.code
                showToast("Coupon Applied! 🏷️", "You saved with code ${coupon.code}")
            } else {
                showToast("Invalid Coupon", error ?: "Coupon cannot be applied.", isError = true)
            }
        }
    }

    fun removeCoupon() {
        _checkoutCouponCode.value = ""
        showToast("Coupon Removed", "Discount has been removed.")
    }

    fun placeOrder(address: AddressEntity) {
        viewModelScope.launch {
            val store = allStores.value.firstOrNull()
            val storeId = store?.id ?: 1L

            val result = repository.placeOrder(
                customerId = _currentCustomerId.value,
                storeId = storeId,
                address = address,
                paymentMethod = _selectedPaymentMethod.value,
                couponCode = _checkoutCouponCode.value.takeIf { it.isNotBlank() },
                deliveryInstructions = _deliveryInstructions.value
            )

            result.onSuccess { order ->
                _checkoutCouponCode.value = ""
                _trackingOrderId.value = order.id
                _customerTab.value = CustomerTab.ORDERS
                showToast("Order Placed! ⚡", "Order ${order.orderNumber} placed! Delivery in ~10 mins.")
            }.onFailure { err ->
                showToast("Checkout Failed", err.message ?: "Unable to place order.", isError = true)
            }
        }
    }

    // Delivery Partner Actions
    fun togglePartnerOnline(isOnline: Boolean) {
        viewModelScope.launch {
            repository.setPartnerOnlineStatus(_currentPartnerId.value, isOnline)
            showToast("Status Updated", if (isOnline) "You are now ONLINE and ready for deliveries!" else "You are OFFLINE.")
        }
    }

    fun partnerAcceptOrder(orderId: Long) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.CONFIRMED)
            showToast("Order Accepted 🛵", "Proceed to darkstore to pickup groceries.")
        }
    }

    fun partnerMarkPickedUp(orderId: Long) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY)
            showToast("Picked Up! 🚀", "Groceries collected. Heading to customer location.")
        }
    }

    fun partnerVerifyOtpAndDeliver(orderId: Long, enteredOtp: String) {
        viewModelScope.launch {
            val success = repository.completeDeliveryWithOtp(orderId, enteredOtp, _currentPartnerId.value)
            if (success) {
                showToast("Delivered! 🎉", "OTP verified! Earning credited to your wallet.")
            } else {
                showToast("Invalid OTP", "The customer delivery OTP is incorrect. Please recheck.", isError = true)
            }
        }
    }

    // Admin Actions
    fun adminUpdateOrderStatus(orderId: Long, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            showToast("Order Updated", "Order #$orderId status changed to ${status.name}")
        }
    }

    fun adminAssignPartner(orderId: Long, partnerId: Long) {
        viewModelScope.launch {
            repository.assignDeliveryPartner(orderId, partnerId)
            showToast("Partner Assigned", "Delivery partner #$partnerId assigned to order #$orderId")
        }
    }

    fun adminCancelOrder(orderId: Long, reason: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId, reason)
            showToast("Order Cancelled", "Order #$orderId has been cancelled.")
        }
    }

    fun adminProcessRefund(orderId: Long) {
        viewModelScope.launch {
            repository.processRefund(orderId)
            showToast("Refund Processed", "Payment refunded for order #$orderId.")
        }
    }

    fun adminSaveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            showToast("Product Saved", "${product.name} saved successfully.")
        }
    }

    fun adminDeleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showToast("Product Deleted", "${product.name} removed from inventory.")
        }
    }

    fun adminUpdateProductStock(productId: Long, stock: Int) {
        viewModelScope.launch {
            repository.updateProductStock(productId, stock)
            showToast("Stock Adjusted", "Product #$productId stock set to $stock.")
        }
    }

    fun adminSaveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.saveCategory(category)
            showToast("Category Saved", "${category.name} saved.")
        }
    }

    fun adminDeleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            showToast("Category Deleted", "${category.name} deleted.")
        }
    }

    fun adminSaveStore(store: StoreEntity) {
        viewModelScope.launch {
            repository.saveStore(store)
            showToast("Store Saved", "${store.name} updated.")
        }
    }

    fun adminToggleUserBlocked(userId: Long, currentBlocked: Boolean) {
        viewModelScope.launch {
            repository.setUserBlockedStatus(userId, !currentBlocked)
            showToast("User Status Updated", "User #$userId is now ${if (!currentBlocked) "BLOCKED" else "ACTIVE"}.")
        }
    }

    fun adminUpdatePartnerKyc(partnerId: Long, status: String) {
        viewModelScope.launch {
            repository.updateKycStatus(partnerId, status)
            showToast("KYC Status", "Partner #$partnerId KYC is now $status.")
        }
    }

    fun adminSaveCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.saveCoupon(coupon)
            showToast("Coupon Saved", "Coupon ${coupon.code} updated.")
        }
    }

    fun adminDeleteCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.deleteCoupon(coupon)
            showToast("Coupon Deleted", "Coupon ${coupon.code} deleted.")
        }
    }

    fun adminSaveBanner(banner: BannerEntity) {
        viewModelScope.launch {
            repository.saveBanner(banner)
            showToast("Banner Saved", "Banner updated.")
        }
    }

    fun adminDeleteBanner(banner: BannerEntity) {
        viewModelScope.launch {
            repository.deleteBanner(banner)
            showToast("Banner Deleted", "Banner removed.")
        }
    }

    fun adminSaveSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
            showToast("Setting Saved", "$key updated to $value.")
        }
    }
}
