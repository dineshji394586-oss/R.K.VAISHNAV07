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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CustomerTab
import com.example.ui.viewmodel.JSRKMartViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCartCheckoutScreen(
    viewModel: JSRKMartViewModel,
    modifier: Modifier = Modifier
) {
    val inr = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val cartSummary by viewModel.cartSummary.collectAsStateWithLifecycle()
    val addresses by viewModel.customerAddresses.collectAsStateWithLifecycle()
    val coupons by viewModel.coupons.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val deliveryInstructions by viewModel.deliveryInstructions.collectAsStateWithLifecycle()

    var showAddressDialog by remember { mutableStateOf(false) }
    var selectedAddressId by remember { mutableStateOf<Long?>(null) }
    var customCouponCode by remember { mutableStateOf("") }
    var isPlacingOrder by remember { mutableStateOf(false) }

    // Sync selected address if not set
    LaunchedEffect(addresses) {
        if (selectedAddressId == null && addresses.isNotEmpty()) {
            selectedAddressId = addresses.firstOrNull { it.isDefault }?.id ?: addresses.first().id
        }
    }

    val selectedAddress = remember(addresses, selectedAddressId) {
        addresses.find { it.id == selectedAddressId } ?: addresses.firstOrNull()
    }

    val totalItemsCount = remember(cartSummary) {
        cartSummary.items.sumOf { it.cartItem.quantity }
    }

    if (cartSummary.items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = KMartPrimaryContainer,
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = KMartPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Your Cart is Empty",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fresh vegetables, dairy, snacks, and daily staples are waiting for you at JSR KMart!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.setCustomerTab(CustomerTab.HOME) },
                    colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cart_start_shopping_btn")
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Shopping", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Header with Delivery ETA
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = KMartAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Delivery in 10-15 mins",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instant dispatch to ${selectedAddress?.label ?: "Home"}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = "$totalItemsCount items",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KMartPrimary
                )
            }
        }

        // Cart items & checkout forms
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Cart Items
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Review Items",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                        cartSummary.items.forEachIndexed { index, itemWithProd ->
                            val prod = itemWithProd.product
                            val qty = itemWithProd.cartItem.quantity

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3F4F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ProductImageWithFallback(
                                        imageUrl = prod.imageUrl,
                                        productName = prod.name,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${prod.weight} • ${prod.unit}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = inr.format(prod.sellingPrice),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (prod.mrp > prod.sellingPrice) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = inr.format(prod.mrp),
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        }
                                    }
                                }

                                // Stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(KMartPrimary.copy(alpha = 0.1f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQty(prod.id, qty - 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            if (qty == 1) Icons.Default.Delete else Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = if (qty == 1) Color.Red else KMartPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "$qty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartQty(prod.id, qty + 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = KMartPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (index < cartSummary.items.size - 1) {
                                Divider(color = Color(0xFFF6F6F6))
                            }
                        }
                    }
                }
            }

            // Section 2: Coupons & Discounts
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = KMartPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coupons & Offers",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (cartSummary.appliedCoupon != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = KMartPrimaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KMartPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "'${cartSummary.appliedCoupon?.code}' applied",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = KMartPrimary
                                            )
                                            Text(
                                                text = "You saved ${inr.format(cartSummary.couponDiscount)}",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                    TextButton(onClick = { viewModel.removeCoupon() }) {
                                        Text("Remove", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customCouponCode,
                                    onValueChange = { customCouponCode = it.uppercase() },
                                    placeholder = { Text("Enter coupon code (e.g. JSR50)", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (customCouponCode.isNotBlank()) {
                                            viewModel.applyCoupon(customCouponCode)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Text("Apply")
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick apply chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                coupons.filter { it.isActive }.take(2).forEach { c ->
                                    AssistChip(
                                        onClick = { viewModel.applyCoupon(c.code) },
                                        label = { Text("${c.code}: ${c.description}", fontSize = 11.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(14.dp), tint = KMartPrimary)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Delivery Address
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = KMartPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delivery Address",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(onClick = { showAddressDialog = true }) {
                                Text("+ Add / Change", fontSize = 12.sp, color = KMartPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedAddress != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF9FAFB),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = KMartPrimary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = selectedAddress.label.uppercase(),
                                                color = KMartPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = selectedAddress.receiverName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${selectedAddress.receiverPhone}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${selectedAddress.fullAddress}, ${selectedAddress.landmark} - ${selectedAddress.pincode}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No address saved. Tap '+ Add / Change' to add delivery address.",
                                fontSize = 12.sp,
                                color = Color.Red
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Delivery instruction chips
                        Text(
                            text = "Delivery Instructions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Leave at door", "Ring bell", "Avoid calling").forEach { instruction ->
                                val isSelected = deliveryInstructions == instruction
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDeliveryInstructions(instruction) },
                                    label = { Text(instruction, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KMartPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Payment Method Selection
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = KMartPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment Method",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        listOf(
                            Triple(PaymentMethod.UPI, "UPI (Google Pay, PhonePe, Paytm)", Icons.Default.QrCodeScanner),
                            Triple(PaymentMethod.COD, "Cash / Pay on Delivery", Icons.Default.Money),
                            Triple(PaymentMethod.CARD, "Credit / Debit Card (Razorpay Gateway)", Icons.Default.CreditCard),
                            Triple(PaymentMethod.NET_BANKING, "Net Banking (SBI, HDFC, ICICI, Axis)", Icons.Default.AccountBalance)
                        ).forEach { (method, label, icon) ->
                            val isSelected = selectedPaymentMethod == method
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setPaymentMethod(method) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setPaymentMethod(method) },
                                    colors = RadioButtonDefaults.colors(selectedColor = KMartPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(icon, contentDescription = null, tint = if (isSelected) KMartPrimary else Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            // Section 5: Bill Breakdown
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bill Details",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        BillRow("Items Total (MRP)", inr.format(cartSummary.totalMrp))
                        BillRow("Product Discount", "-${inr.format(cartSummary.totalSavings)}", isDiscount = true)
                        BillRow(
                            "Delivery Fee",
                            if (cartSummary.deliveryFee == 0.0) "FREE" else inr.format(cartSummary.deliveryFee),
                            isFree = cartSummary.deliveryFee == 0.0
                        )
                        BillRow("Platform & Handling Fee", inr.format(cartSummary.platformFee))
                        BillRow("GST & Government Taxes (5%)", inr.format(cartSummary.tax))

                        if (cartSummary.couponDiscount > 0.0) {
                            BillRow("Coupon Discount (${cartSummary.appliedCoupon?.code})", "-${inr.format(cartSummary.couponDiscount)}", isDiscount = true)
                        }

                        Divider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "To Pay",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Inclusive of all taxes",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = inr.format(cartSummary.finalTotal),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KMartPrimary
                            )
                        }

                        if (cartSummary.totalSavings > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KMartPrimaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Celebration, contentDescription = null, tint = KMartPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Yay! You saved ${inr.format(cartSummary.totalSavings + cartSummary.couponDiscount)} on this order",
                                        color = KMartPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cancellation Policy Note
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Orders can be cancelled before packing begins. Secure payment with Instant Refund guarantee.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Bottom Sticky Action Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = inr.format(cartSummary.finalTotal),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Payment: ${selectedPaymentMethod.name}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        val addr = selectedAddress
                        if (addr != null) {
                            isPlacingOrder = true
                            viewModel.placeOrder(addr)
                        }
                    },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("checkout_place_order_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPlacingOrder && cartSummary.items.isNotEmpty() && selectedAddress != null
                ) {
                    if (isPlacingOrder) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Place Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Address Dialog
    if (showAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddressDialog = false },
            onSave = { newAddr ->
                showAddressDialog = false
            }
        )
    }
}

@Composable
private fun BillRow(label: String, value: String, isDiscount: Boolean = false, isFree: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.DarkGray)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isDiscount || isFree) FontWeight.Bold else FontWeight.Medium,
            color = if (isDiscount || isFree) KMartPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (AddressEntity) -> Unit
) {
    var name by remember { mutableStateOf("Rahul Sharma") }
    var phone by remember { mutableStateOf("+91 98765 43210") }
    var fullAddress by remember { mutableStateOf("Flat 402, Sunshine Heights, Bistupur Main Road") }
    var landmark by remember { mutableStateOf("Near Gopal Maidan") }
    var pincode by remember { mutableStateOf("831001") }
    var label by remember { mutableStateOf("Home") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = KMartPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Delivery Address", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Receiver Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("Full Address / Flat / Building / Street") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Landmark") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it },
                        label = { Text("Pincode") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Home", "Work", "Other").forEach { t ->
                        FilterChip(
                            selected = label == t,
                            onClick = { label = t },
                            label = { Text(t) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entity = AddressEntity(
                        id = 0L,
                        customerId = 2L,
                        label = label,
                        fullAddress = fullAddress,
                        landmark = landmark,
                        pincode = pincode,
                        isDefault = true
                    )
                    onSave(entity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary)
            ) {
                Text("Save Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
