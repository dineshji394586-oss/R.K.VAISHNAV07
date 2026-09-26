package com.example.ui.customer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CustomerTab
import com.example.ui.viewmodel.JSRKMartViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    viewModel: JSRKMartViewModel,
    modifier: Modifier = Modifier
) {
    val inr = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val customers by viewModel.customersList.collectAsStateWithLifecycle()
    val currentCustomerId by viewModel.currentCustomerId.collectAsStateWithLifecycle()
    val addresses by viewModel.customerAddresses.collectAsStateWithLifecycle()
    val orders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.customerWishlist.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val notifications by viewModel.currentNotifications.collectAsStateWithLifecycle()
    val coupons by viewModel.coupons.collectAsStateWithLifecycle()

    val currentCustomer = remember(customers, currentCustomerId) {
        customers.find { it.id == currentCustomerId } ?: customers.firstOrNull()
    }

    var activeSection by remember { mutableStateOf("MAIN") } // MAIN, ADDRESSES, WISHLIST, NOTIFICATIONS, COUPONS, HELP
    var showAddAddressDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Profile Header
        Surface(
            color = KMartPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentCustomer?.name?.take(2)?.uppercase() ?: "AS",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = KMartPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentCustomer?.name ?: "Aarav Sharma",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = currentCustomer?.phone ?: "+91 98765 43210",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = currentCustomer?.email ?: "aarav.sharma@example.com",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = KMartAccent
                    ) {
                        Text(
                            text = "GOLD",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(title = "Orders", value = "${orders.size}")
                    StatItem(title = "Addresses", value = "${addresses.size}")
                    StatItem(title = "Wishlist", value = "${wishlistIds.size}")
                    StatItem(title = "Coupons", value = "${coupons.size}")
                }
            }
        }

        // Sub-screen navigation or items
        when (activeSection) {
            "ADDRESSES" -> {
                AddressesSubScreen(
                    addresses = addresses,
                    onBack = { activeSection = "MAIN" },
                    onAddNew = { showAddAddressDialog = true }
                )
            }
            "WISHLIST" -> {
                WishlistSubScreen(
                    wishlistIds = wishlistIds,
                    allProducts = allProducts,
                    onAddToCart = { viewModel.addToCart(it) },
                    onRemove = { viewModel.toggleWishlist(it) },
                    onBack = { activeSection = "MAIN" }
                )
            }
            "NOTIFICATIONS" -> {
                NotificationsSubScreen(
                    notifications = notifications,
                    onBack = { activeSection = "MAIN" }
                )
            }
            "COUPONS" -> {
                CouponsSubScreen(
                    coupons = coupons,
                    onBack = { activeSection = "MAIN" },
                    onApply = {
                        viewModel.applyCoupon(it)
                        viewModel.setCustomerTab(CustomerTab.CART)
                    }
                )
            }
            "HELP" -> {
                HelpSupportSubScreen(onBack = { activeSection = "MAIN" })
            }
            else -> {
                // Main options list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Account & Preferences",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        ProfileOptionItem(
                            icon = Icons.Outlined.LocationOn,
                            title = "Saved Delivery Addresses",
                            subtitle = "${addresses.size} addresses saved",
                            onClick = { activeSection = "ADDRESSES" }
                        )
                    }

                    item {
                        ProfileOptionItem(
                            icon = Icons.Outlined.FavoriteBorder,
                            title = "My Wishlist",
                            subtitle = "${wishlistIds.size} saved items",
                            onClick = { activeSection = "WISHLIST" }
                        )
                    }

                    item {
                        ProfileOptionItem(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications & Alerts",
                            subtitle = "${notifications.count { !it.isRead }} unread alerts",
                            onClick = { activeSection = "NOTIFICATIONS" }
                        )
                    }

                    item {
                        ProfileOptionItem(
                            icon = Icons.Outlined.LocalOffer,
                            title = "Coupons & Offers",
                            subtitle = "View active discount codes",
                            onClick = { activeSection = "COUPONS" }
                        )
                    }

                    item {
                        ProfileOptionItem(
                            icon = Icons.Outlined.SupportAgent,
                            title = "Help & Customer Support",
                            subtitle = "FAQs, Contact JSR KMart Hub",
                            onClick = { activeSection = "HELP" }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Platform Role Switching",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Switch Application Interface",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Test all 3 interconnected roles on this JSR KMart single database system.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.setRole(UserRole.DELIVERY_PARTNER) },
                                        colors = ButtonDefaults.buttonColors(containerColor = KMartAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Delivery Partner", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.setRole(UserRole.ADMIN) },
                                        colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Admin Portal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.showToast("Signed Out", "You can sign back in anytime.")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddAddressDialog = false },
            onSave = {
                showAddAddressDialog = false
            }
        )
    }
}

@Composable
private fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = KMartPrimaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = KMartPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AddressesSubScreen(
    addresses: List<AddressEntity>,
    onBack: () -> Unit,
    onAddNew: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Saved Addresses", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Add New", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(addresses) { addr ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = KMartPrimaryContainer, shape = RoundedCornerShape(6.dp)) {
                                Text(addr.label.uppercase(), color = KMartPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(addr.receiverName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(addr.receiverPhone, fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${addr.fullAddress}, ${addr.landmark} - ${addr.pincode}", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistSubScreen(
    wishlistIds: List<Long>,
    allProducts: List<ProductEntity>,
    onAddToCart: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onBack: () -> Unit
) {
    val wishlistProducts = remember(wishlistIds, allProducts) {
        val set = wishlistIds.toSet()
        allProducts.filter { set.contains(it.id) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("My Wishlist (${wishlistProducts.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (wishlistProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your wishlist is empty. Tap ❤️ on any item to save it here.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(wishlistProducts) { prod ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${prod.weight} • ₹${prod.sellingPrice}", fontSize = 12.sp, color = KMartPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = { onAddToCart(prod.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add to Cart", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { onRemove(prod.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsSubScreen(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notifications) { n ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (n.isRead) MaterialTheme.colorScheme.surface else KMartPrimaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = KMartPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(n.message, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun CouponsSubScreen(
    coupons: List<CouponEntity>,
    onBack: () -> Unit,
    onApply: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Available Coupons", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(coupons) { c ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(color = KMartPrimaryContainer, shape = RoundedCornerShape(6.dp)) {
                                Text(c.code, fontWeight = FontWeight.ExtraBold, color = KMartPrimary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(c.description, fontSize = 12.sp, color = Color.DarkGray)
                            Text("Min Order: ₹${c.minOrderValue} • Max Discount: ₹${c.maxDiscount}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = { onApply(c.code) },
                            colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSupportSubScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Help & Customer Support", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("JSR KMart Customer Care", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("We are committed to delivering 100% fresh groceries in 10-15 minutes.", fontSize = 12.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))
                Text("📞 Phone: +91 657 2234567 / 1800-JSR-KMART", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("✉️ Email: support@jsrkmart.in", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("📍 Head Office: JSR KMart Hub, Bistupur, Jamshedpur 831001", fontSize = 13.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Frequently Asked Questions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "How does 10-15 minute delivery work?" to "We dispatch immediately from your nearest neighborhood JSR KMart micro-warehouse.",
            "How do I share the delivery OTP?" to "Check 'My Orders' screen to see your 4-digit OTP. Give it to the rider when they arrive at your door.",
            "Can I cancel my order?" to "Yes, before the order leaves the packing station you can cancel instantly for a full refund."
        ).forEach { (q, a) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(q, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(a, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }
    }
}
