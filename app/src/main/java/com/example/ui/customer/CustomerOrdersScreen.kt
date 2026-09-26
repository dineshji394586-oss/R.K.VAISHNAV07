package com.example.ui.customer

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.JSRKMartViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerOrdersScreen(
    viewModel: JSRKMartViewModel,
    modifier: Modifier = Modifier
) {
    val inr = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val orders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val deliveryPartners by viewModel.deliveryPartnersList.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, DELIVERED, CANCELLED
    var expandedOrderId by remember { mutableStateOf<Long?>(null) }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "ACTIVE" -> orders.filter { it.status != OrderStatus.DELIVERED.name && it.status != OrderStatus.CANCELLED.name && it.status != OrderStatus.REFUNDED.name }
            "DELIVERED" -> orders.filter { it.status == OrderStatus.DELIVERED.name }
            "CANCELLED" -> orders.filter { it.status == OrderStatus.CANCELLED.name || it.status == OrderStatus.REFUNDED.name }
            else -> orders
        }
    }

    // Default expanded order to the first active order
    LaunchedEffect(orders) {
        if (expandedOrderId == null) {
            expandedOrderId = orders.firstOrNull {
                it.status != OrderStatus.DELIVERED.name && it.status != OrderStatus.CANCELLED.name
            }?.id ?: orders.firstOrNull()?.id
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Screen Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Orders & Live Tracking",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Filter tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val activeCount = orders.count { it.status != OrderStatus.DELIVERED.name && it.status != OrderStatus.CANCELLED.name }
                    val deliveredCount = orders.count { it.status == OrderStatus.DELIVERED.name }
                    val cancelledCount = orders.count { it.status == OrderStatus.CANCELLED.name }

                    listOf(
                        "ALL" to "All (${orders.size})",
                        "ACTIVE" to "Active ($activeCount)",
                        "DELIVERED" to "Delivered ($deliveredCount)",
                        "CANCELLED" to "Cancelled ($cancelledCount)"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KMartPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No orders found in this section",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    val isExpanded = expandedOrderId == order.id
                    val partner = deliveryPartners.find { it.id == order.deliveryPartnerId }

                    CustomerOrderTrackingCard(
                        order = order,
                        partner = partner,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedOrderId = if (isExpanded) null else order.id
                        },
                        onCancelOrder = {
                            viewModel.adminCancelOrder(order.id, "Customer requested cancellation")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerOrderTrackingCard(
    order: OrderEntity,
    partner: UserEntity?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCancelOrder: () -> Unit
) {
    val inr = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val dateStr = remember(order.createdAt) { dateFormat.format(Date(order.createdAt)) }
    val context = LocalContext.current

    val isActive = order.status != OrderStatus.DELIVERED.name &&
                   order.status != OrderStatus.CANCELLED.name &&
                   order.status != OrderStatus.REFUNDED.name

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = order.orderNumber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = KMartAccent,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                    }
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.paymentMethod} • ${inr.format(order.finalAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )

                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Details" else "Track / Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KMartPrimary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = KMartPrimary
                    )
                }
            }

            // Expanded Live Tracking Content
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isActive) {
                        // Live ETA Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KMartPrimaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ElectricMoped,
                                        contentDescription = null,
                                        tint = KMartPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Arriving in ~${order.estimatedMinutes} mins",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = KMartPrimary
                                        )
                                        Text(
                                            text = "On the way to ${order.deliveryAddress}",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = KMartPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Map Canvas View from CommonComponents
                        LiveMapCanvasView(
                            liveLat = order.liveLat,
                            liveLng = order.liveLng,
                            estimatedMinutes = order.estimatedMinutes
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Customer Delivery OTP Card (Requirement: Delivery completed with customer OTP)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "DELIVERY OTP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "Share this 4-digit OTP with delivery partner upon arrival",
                                        fontSize = 11.sp,
                                        color = Color(0xFF78350F)
                                    )
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = order.deliveryOtp,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309),
                                        letterSpacing = 2.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Order Lifecycle Timeline
                    Text(
                        text = "Order Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomerTimelineView(currentStatus = order.status)

                    // Delivery Partner Card
                    if (partner != null && (order.status == OrderStatus.ASSIGNED.name || order.status == OrderStatus.PICKED_UP.name || order.status == OrderStatus.OUT_FOR_DELIVERY.name || order.status == OrderStatus.DELIVERED.name)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = KMartPrimaryContainer,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = KMartPrimary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = partner.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${partner.vehicleType} • ${partner.vehicleNumber}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${partner.phone}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = KMartPrimary)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call Delivery Partner", tint = Color.White)
                                }
                            }
                        }
                    }

                    // Delivery instructions & address
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Delivery To:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = order.deliveryAddress,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (order.deliveryInstructions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instructions: \"${order.deliveryInstructions}\"",
                                fontSize = 11.sp,
                                color = KMartPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Cancel Order Option (Allowed when not out for delivery yet)
                    if (order.status == OrderStatus.PLACED.name || order.status == OrderStatus.CONFIRMED.name || order.status == OrderStatus.PACKING.name) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onCancelOrder,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel Order", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerTimelineView(currentStatus: String) {
    val steps = listOf(
        OrderStatus.PLACED.name to "Order Placed",
        OrderStatus.CONFIRMED.name to "Confirmed",
        OrderStatus.PACKING.name to "Packed",
        OrderStatus.ASSIGNED.name to "Rider Assigned",
        OrderStatus.OUT_FOR_DELIVERY.name to "Out for Delivery",
        OrderStatus.DELIVERED.name to "Delivered"
    )

    val currentOrdinal = try {
        OrderStatus.valueOf(currentStatus).ordinal
    } catch (_: Exception) {
        0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEach { (stepStatus, label) ->
            val stepOrdinal = OrderStatus.valueOf(stepStatus).ordinal
            val isCompleted = currentOrdinal >= stepOrdinal && currentStatus != OrderStatus.CANCELLED.name
            val isCurrent = currentStatus == stepStatus

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                currentStatus == OrderStatus.CANCELLED.name -> Color.Red
                                isCurrent -> KMartAccent
                                isCompleted -> KMartPrimary
                                else -> Color(0xFFE0E0E0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted && !isCurrent) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else if (isCompleted) FontWeight.Medium else FontWeight.Normal,
                    color = if (isCurrent) KMartPrimary else if (isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
            }
        }
    }
}
