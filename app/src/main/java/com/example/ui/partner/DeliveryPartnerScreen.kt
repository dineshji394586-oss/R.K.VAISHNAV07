package com.example.ui.partner

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.JSRKMartViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryPartnerScreen(
    viewModel: JSRKMartViewModel,
    modifier: Modifier = Modifier
) {
    val inr = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val partners by viewModel.deliveryPartnersList.collectAsStateWithLifecycle()
    val currentPartnerId by viewModel.currentPartnerId.collectAsStateWithLifecycle()
    val assignedOrders by viewModel.partnerOrders.collectAsStateWithLifecycle()
    val earningsList by viewModel.partnerEarnings.collectAsStateWithLifecycle()

    val partner = remember(partners, currentPartnerId) {
        partners.find { it.id == currentPartnerId } ?: partners.firstOrNull()
    }

    var partnerTab by remember { mutableStateOf("DASHBOARD") } // DASHBOARD, ORDERS, EARNINGS, PROFILE
    var orderForOtpModal by remember { mutableStateOf<OrderEntity?>(null) }
    var otpInput by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val activeTrip = remember(assignedOrders) {
        assignedOrders.firstOrNull {
            it.status == OrderStatus.ASSIGNED.name ||
            it.status == OrderStatus.PICKED_UP.name ||
            it.status == OrderStatus.OUT_FOR_DELIVERY.name
        }
    }

    val completedTrips = remember(assignedOrders) {
        assignedOrders.filter { it.status == OrderStatus.DELIVERED.name }
    }

    val totalEarnings: Double = remember(earningsList) {
        earningsList.sumOf { it.totalEarning }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Partner Top Header with Online/Offline Switch
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = KMartPrimaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ElectricMoped, contentDescription = null, tint = KMartPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = partner?.name ?: "Delivery Partner",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = KMartPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = partner?.kycStatus ?: "KYC VERIFIED",
                                        color = KMartPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${partner?.vehicleType?.uppercase()} • ${partner?.vehicleNumber}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Online / Offline Switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (partner?.isOnline == true) "ONLINE" else "OFFLINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (partner?.isOnline == true) KMartPrimary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = partner?.isOnline == true,
                            onCheckedChange = { viewModel.togglePartnerOnline(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KMartPrimary
                            ),
                            modifier = Modifier.testTag("partner_online_switch")
                        )
                    }
                }
            }
        }

        // Navigation Sub-tabs for Partner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            TabRow(
                selectedTabIndex = when (partnerTab) {
                    "DASHBOARD" -> 0
                    "ORDERS" -> 1
                    "EARNINGS" -> 2
                    else -> 3
                },
                contentColor = KMartPrimary
            ) {
                Tab(
                    selected = partnerTab == "DASHBOARD",
                    onClick = { partnerTab = "DASHBOARD" },
                    text = { Text("Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = partnerTab == "ORDERS",
                    onClick = { partnerTab = "ORDERS" },
                    text = { Text("Trips (${assignedOrders.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Moped, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = partnerTab == "EARNINGS",
                    onClick = { partnerTab = "EARNINGS" },
                    text = { Text("Earnings", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = partnerTab == "PROFILE",
                    onClick = { partnerTab = "PROFILE" },
                    text = { Text("Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // Body content
        when (partnerTab) {
            "DASHBOARD" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Stats Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PartnerMetricCard(
                                title = "Today's Earnings",
                                value = inr.format(if (totalEarnings > 0) totalEarnings else 450.0),
                                icon = Icons.Default.CurrencyRupee,
                                color = KMartPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            PartnerMetricCard(
                                title = "Deliveries Done",
                                value = "${completedTrips.size.coerceAtLeast(3)}",
                                icon = Icons.Default.CheckCircle,
                                color = KMartAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Active Trip Card
                    if (activeTrip != null) {
                        item {
                            Text(
                                text = "Current Active Delivery",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            ActiveDeliveryCard(
                                order = activeTrip,
                                onUpdateStatus = {
                                    if (activeTrip.status == OrderStatus.ASSIGNED.name) {
                                        viewModel.partnerAcceptOrder(activeTrip.id)
                                    } else if (activeTrip.status == OrderStatus.CONFIRMED.name || activeTrip.status == OrderStatus.PICKED_UP.name) {
                                        viewModel.partnerMarkPickedUp(activeTrip.id)
                                    }
                                },
                                onOpenOtpModal = {
                                    orderForOtpModal = activeTrip
                                    otpInput = ""
                                    otpError = null
                                }
                            )
                        }
                    } else {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = KMartPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (partner?.isOnline == true) "You are Online & Ready" else "You are Currently Offline",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (partner?.isOnline == true)
                                            "Waiting for new grocery orders in your service radius..."
                                        else
                                            "Switch toggle above to ONLINE to receive instant delivery alerts.",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Recent Completed Deliveries list preview
                    item {
                        Text(
                            text = "Recent Deliveries Today",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (completedTrips.isEmpty()) {
                        item {
                            Text("No deliveries completed today yet.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        items(completedTrips.take(4)) { trip ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(trip.orderNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(trip.deliveryAddress, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("+₹55", fontWeight = FontWeight.ExtraBold, color = KMartPrimary, fontSize = 14.sp)
                                        Text("Completed", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "ORDERS" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(assignedOrders) { order ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(order.orderNumber, fontWeight = FontWeight.Bold)
                                    StatusChip(status = order.status)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Drop: ${order.deliveryAddress}", fontSize = 12.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Earning: ₹55.00", fontWeight = FontWeight.Bold, color = KMartPrimary)
                                    if (order.status != OrderStatus.DELIVERED.name && order.status != OrderStatus.CANCELLED.name) {
                                        Button(
                                            onClick = {
                                                if (order.status == OrderStatus.ASSIGNED.name) {
                                                    viewModel.partnerAcceptOrder(order.id)
                                                } else if (order.status == OrderStatus.CONFIRMED.name || order.status == OrderStatus.PICKED_UP.name) {
                                                    viewModel.partnerMarkPickedUp(order.id)
                                                } else if (order.status == OrderStatus.OUT_FOR_DELIVERY.name) {
                                                    orderForOtpModal = order
                                                    otpInput = ""
                                                    otpError = null
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = when (order.status) {
                                                    OrderStatus.ASSIGNED.name -> "Accept Order"
                                                    OrderStatus.CONFIRMED.name -> "Mark Picked Up"
                                                    OrderStatus.PICKED_UP.name -> "Start Delivery"
                                                    OrderStatus.OUT_FOR_DELIVERY.name -> "Verify OTP & Deliver"
                                                    else -> "Update"
                                                },
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "EARNINGS" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = KMartPrimary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Withdrawable Balance", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                                Text(
                                    text = inr.format(if (totalEarnings > 0) totalEarnings + 2400.0 else 2850.0),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        viewModel.showToast(
                                            title = "Payout Requested",
                                            message = "Instant transfer of earnings initiated to your linked Bank Account."
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KMartAccent),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Withdraw to Bank / UPI", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Earnings Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                EarningsRow("Base Trip Earnings", inr.format(if (totalEarnings > 0) totalEarnings else 450.0))
                                EarningsRow("Peak Hour Incentive", "₹120.00")
                                EarningsRow("Customer Tips", "₹60.00")
                                EarningsRow("Weekly Target Bonus", "₹250.00")
                                Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 4.dp))
                                EarningsRow("Total Earnings This Week", inr.format((if (totalEarnings > 0) totalEarnings else 450.0) + 430.0), isBold = true)
                            }
                        }
                    }
                }
            }

            "PROFILE" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Partner Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Name: ${partner?.name}", fontSize = 13.sp)
                            Text("Phone: ${partner?.phone}", fontSize = 13.sp)
                            Text("Vehicle: ${partner?.vehicleType} (${partner?.vehicleNumber})", fontSize = 13.sp)
                            Text("Rating: ⭐ 4.8 / 5.0", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KMartPrimary)
                            Text("Store Hub: JSR KMart Central Hub Bistupur", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.setRole(UserRole.CUSTOMER) },
                        colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Switch to Customer Shopping App", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.setRole(UserRole.ADMIN) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Switch to Admin Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Customer OTP Verification Dialog Modal
    orderForOtpModal?.let { order ->
        AlertDialog(
            onDismissRequest = { orderForOtpModal = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = KMartPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Customer Delivery OTP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ask customer for the 4-digit delivery OTP shown on their screen.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            if (it.length <= 4) otpInput = it
                            otpError = null
                        },
                        label = { Text("Enter 4-Digit OTP") },
                        placeholder = { Text("e.g. ${order.deliveryOtp}") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_otp_input"),
                        isError = otpError != null,
                        supportingText = {
                            if (otpError != null) {
                                Text(otpError!!, color = Color.Red)
                            } else {
                                Text("Customer OTP is ${order.deliveryOtp}", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpInput.trim() == order.deliveryOtp) {
                            viewModel.partnerVerifyOtpAndDeliver(order.id, otpInput.trim())
                            orderForOtpModal = null
                        } else {
                            otpError = "Incorrect OTP. Please enter ${order.deliveryOtp}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                    modifier = Modifier.testTag("partner_verify_otp_btn")
                ) {
                    Text("Verify & Complete Delivery")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderForOtpModal = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActiveDeliveryCard(
    order: OrderEntity,
    onUpdateStatus: () -> Unit,
    onOpenOtpModal: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Active Trip: ${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Estimated Pay: ₹55.00", fontSize = 12.sp, color = KMartPrimary, fontWeight = FontWeight.SemiBold)
                }
                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 1: Warehouse Pickup details
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF3F4F6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = KMartPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pickup: JSR KMart Hub Bistupur", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text("Plot 12, Main Road, Bistupur • Order packed and ready", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step 2: Customer drop location
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF3F4F6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Drop Destination", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Text(order.deliveryAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (order.deliveryInstructions.isNotBlank()) {
                        Text("Note: \"${order.deliveryInstructions}\"", fontSize = 11.sp, color = KMartPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons based on delivery stage
            when (order.status) {
                OrderStatus.ASSIGNED.name -> {
                    Button(
                        onClick = onUpdateStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("partner_accept_order_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Accept Delivery Order", fontWeight = FontWeight.Bold)
                    }
                }

                OrderStatus.CONFIRMED.name, OrderStatus.PICKED_UP.name -> {
                    Button(
                        onClick = onUpdateStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("partner_mark_picked_up_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Pickup & Start Delivery", fontWeight = FontWeight.Bold)
                    }
                }

                OrderStatus.OUT_FOR_DELIVERY.name -> {
                    Button(
                        onClick = onOpenOtpModal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("partner_enter_otp_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Arrived • Enter Customer OTP", fontWeight = FontWeight.Bold)
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun PartnerMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun EarningsRow(label: String, amount: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = if (isBold) MaterialTheme.colorScheme.onSurface else Color.DarkGray, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = amount, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold, color = if (isBold) KMartPrimary else MaterialTheme.colorScheme.onSurface)
    }
}
