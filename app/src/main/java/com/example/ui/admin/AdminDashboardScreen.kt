package com.example.ui.admin

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.JSRKMartViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: JSRKMartViewModel,
    modifier: Modifier = Modifier
) {
    val inr = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allPartners by viewModel.deliveryPartnersList.collectAsStateWithLifecycle()
    val allStores by viewModel.allStores.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var adminTab by remember { mutableStateOf("OVERVIEW") } // OVERVIEW, ORDERS, PRODUCTS, CATEGORIES, INVENTORY, PARTNERS, STORES, SETTINGS
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var orderToAssignPartner by remember { mutableStateOf<OrderEntity?>(null) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    // Dashboard calculations
    val totalRevenue = remember(allOrders) {
        allOrders.filter { it.status == OrderStatus.DELIVERED.name }.map { it.finalTotal }.sum()
    }
    val pendingOrders = remember(allOrders) {
        allOrders.filter { it.status != OrderStatus.DELIVERED.name && it.status != OrderStatus.CANCELLED.name }
    }
    val lowStockProducts = remember(allProducts) {
        allProducts.filter { it.stock in 1..15 }
    }
    val outOfStockProducts = remember(allProducts) {
        allProducts.filter { it.stock <= 0 }
    }
    val onlinePartners = remember(allPartners) {
        allPartners.filter { it.isOnline }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Admin Header
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
                            color = KMartPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("JSR KMart Admin Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Real-Time Operations & Inventory Hub", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    // Role switch buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { viewModel.setRole(UserRole.CUSTOMER) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Customer App", fontSize = 11.sp)
                        }
                        FilledTonalButton(
                            onClick = { viewModel.setRole(UserRole.DELIVERY_PARTNER) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Rider App", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = when (adminTab) {
                        "OVERVIEW" -> 0
                        "ORDERS" -> 1
                        "PRODUCTS" -> 2
                        "CATEGORIES" -> 3
                        "INVENTORY" -> 4
                        "PARTNERS" -> 5
                        "STORES" -> 6
                        else -> 7
                    },
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = KMartPrimary
                ) {
                    Tab(
                        selected = adminTab == "OVERVIEW",
                        onClick = { adminTab = "OVERVIEW" },
                        text = { Text("Overview", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = adminTab == "ORDERS",
                        onClick = { adminTab = "ORDERS" },
                        text = { Text("Orders (${pendingOrders.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = adminTab == "PRODUCTS",
                        onClick = { adminTab = "PRODUCTS" },
                        text = { Text("Products (${allProducts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = adminTab == "CATEGORIES",
                        onClick = { adminTab = "CATEGORIES" },
                        text = { Text("Categories (${categories.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = adminTab == "INVENTORY",
                        onClick = { adminTab = "INVENTORY" },
                        text = { Text("Stock (${lowStockProducts.size} low)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = adminTab == "PARTNERS",
                        onClick = { adminTab = "PARTNERS" },
                        text = { Text("Riders (${onlinePartners.size} on)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = adminTab == "STORES",
                        onClick = { adminTab = "STORES" },
                        text = { Text("Stores (${allStores.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = adminTab == "SETTINGS",
                        onClick = { adminTab = "SETTINGS" },
                        text = { Text("Settings & Reports", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Tab Content
        when (adminTab) {
            "OVERVIEW" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Metrics Grid 1: Business performance
                    item {
                        Text("Business Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminCard(title = "Total Revenue", value = inr.format(totalRevenue), icon = Icons.Default.CurrencyRupee, color = KMartPrimary, modifier = Modifier.weight(1f))
                            AdminCard(title = "Total Orders", value = "${allOrders.size}", icon = Icons.Default.Receipt, color = KMartAccent, modifier = Modifier.weight(1f))
                        }
                    }

                    // Metrics Grid 2: Real-time ops
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminCard(title = "Pending Orders", value = "${pendingOrders.size}", icon = Icons.Default.HourglassTop, color = Color(0xFFE65100), modifier = Modifier.weight(1f))
                            AdminCard(title = "Online Riders", value = "${onlinePartners.size} / ${allPartners.size}", icon = Icons.Default.TwoWheeler, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                        }
                    }

                    // Metrics Grid 3: Inventory warnings
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminCard(title = "Low Stock Alerts", value = "${lowStockProducts.size}", icon = Icons.Default.Warning, color = Color(0xFFF57C00), modifier = Modifier.weight(1f))
                            AdminCard(title = "Out of Stock", value = "${outOfStockProducts.size}", icon = Icons.Default.ErrorOutline, color = Color.Red, modifier = Modifier.weight(1f))
                        }
                    }

                    // Quick Orders board
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Live Orders Board", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = { adminTab = "ORDERS" }) {
                                Text("View All Orders", color = KMartPrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    items(pendingOrders.take(3)) { order ->
                        AdminOrderRow(
                            order = order,
                            partners = allPartners,
                            onUpdateStatus = { viewModel.adminUpdateOrderStatus(order.id, it) },
                            onAssignPartner = { orderToAssignPartner = order }
                        )
                    }
                }
            }

            "ORDERS" -> {
                AdminOrdersScreen(
                    orders = allOrders,
                    partners = allPartners,
                    onUpdateStatus = { id, st -> viewModel.adminUpdateOrderStatus(id, st) },
                    onAssignPartner = { orderToAssignPartner = it }
                )
            }

            "PRODUCTS" -> {
                AdminProductsScreen(
                    products = allProducts,
                    categories = categories,
                    onAddClick = { showAddProductDialog = true },
                    onEditClick = { editingProduct = it },
                    onDelete = { prod -> viewModel.adminDeleteProduct(prod) }
                )
            }

            "CATEGORIES" -> {
                AdminCategoriesScreen(
                    categories = categories,
                    onAddClick = { showAddCategoryDialog = true },
                    onDelete = { cat -> viewModel.adminDeleteCategory(cat) }
                )
            }

            "INVENTORY" -> {
                AdminInventoryScreen(
                    products = allProducts,
                    onUpdateStock = { id, stock -> viewModel.adminUpdateProductStock(id, stock) }
                )
            }

            "PARTNERS" -> {
                AdminPartnersScreen(
                    partners = allPartners,
                    onToggleActive = { partnerId, isActive ->
                        viewModel.togglePartnerOnline(isActive)
                    }
                )
            }

            "STORES" -> {
                AdminStoresScreen(stores = allStores)
            }

            "SETTINGS" -> {
                AdminSettingsScreen(
                    onExportCsv = {
                        val header = "Order ID,Order Number,Status,Payment Method,Final Amount\n"
                        val rows = allOrders.joinToString("\n") { "${it.id},${it.orderNumber},${it.status},${it.paymentMethod},${it.finalAmount}" }
                        val csv = header + rows
                        Toast.makeText(context, "Exported ${allOrders.size} orders to CSV successfully!", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    // Assign Partner Dialog
    orderToAssignPartner?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToAssignPartner = null },
            title = { Text("Assign Delivery Rider", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select an available rider for ${order.orderNumber}:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    allPartners.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.adminAssignPartner(order.id, p.id)
                                    orderToAssignPartner = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${p.vehicleType} • ${p.phone}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Surface(
                                color = if (p.isOnline) KMartPrimaryContainer else Color(0xFFEEEEEE),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (p.isOnline) "ONLINE" else "OFFLINE",
                                    color = if (p.isOnline) KMartPrimary else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Divider(color = Color(0xFFEEEEEE))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { orderToAssignPartner = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddEditProductDialog(
            categories = categories,
            product = null,
            onDismiss = { showAddProductDialog = false },
            onSave = {
                viewModel.adminSaveProduct(it)
                showAddProductDialog = false
            }
        )
    }

    // Edit Product Dialog
    editingProduct?.let { prod ->
        AddEditProductDialog(
            categories = categories,
            product = prod,
            onDismiss = { editingProduct = null },
            onSave = {
                viewModel.adminSaveProduct(it)
                editingProduct = null
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = {
                viewModel.adminSaveCategory(it)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
fun AdminCard(
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
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AdminOrderRow(
    order: OrderEntity,
    partners: List<UserEntity>,
    onUpdateStatus: (OrderStatus) -> Unit,
    onAssignPartner: () -> Unit
) {
    val inr = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val partner = partners.find { it.id == order.deliveryPartnerId }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${inr.format(order.finalAmount)} • ${order.paymentMethod}", fontSize = 12.sp, color = Color.Gray)
                }
                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Address: ${order.deliveryAddress}", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

            if (partner != null) {
                Text("Assigned Rider: ${partner.name} (${partner.phone})", fontSize = 11.sp, color = KMartPrimary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (order.status) {
                    OrderStatus.PLACED.name -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.CONFIRMED) },
                            colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Confirm Order", fontSize = 11.sp)
                        }
                    }
                    OrderStatus.CONFIRMED.name -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PACKING) },
                            colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Start Packing", fontSize = 11.sp)
                        }
                    }
                    OrderStatus.PACKING.name -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.READY_FOR_PICKUP) },
                            colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mark Ready", fontSize = 11.sp)
                        }
                        Button(
                            onClick = onAssignPartner,
                            colors = ButtonDefaults.buttonColors(containerColor = KMartAccent),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Assign Rider", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.READY_FOR_PICKUP.name -> {
                        Button(
                            onClick = onAssignPartner,
                            colors = ButtonDefaults.buttonColors(containerColor = KMartAccent),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Assign Rider", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun AdminOrdersScreen(
    orders: List<OrderEntity>,
    partners: List<UserEntity>,
    onUpdateStatus: (Long, OrderStatus) -> Unit,
    onAssignPartner: (OrderEntity) -> Unit
) {
    var filterStatus by remember { mutableStateOf<String?>(null) }

    val displayed = remember(orders, filterStatus) {
        if (filterStatus == null) orders else orders.filter { it.status == filterStatus }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Status filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { filterStatus = null },
                    label = { Text("All (${orders.size})", fontSize = 11.sp) }
                )
            }
            items(OrderStatus.values()) { st ->
                val count = orders.count { it.status == st.name }
                FilterChip(
                    selected = filterStatus == st.name,
                    onClick = { filterStatus = st.name },
                    label = { Text("${st.name.replace("_", " ")} ($count)", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(displayed, key = { it.id }) { ord ->
                AdminOrderRow(
                    order = ord,
                    partners = partners,
                    onUpdateStatus = { onUpdateStatus(ord.id, it) },
                    onAssignPartner = { onAssignPartner(ord) }
                )
            }
        }
    }
}

@Composable
fun AdminProductsScreen(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    onAddClick: () -> Unit,
    onEditClick: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(products, search) {
        if (search.isBlank()) products else products.filter { it.name.contains(search, ignoreCase = true) || it.brand.contains(search, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search catalog...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("+ Product", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.id }) { prod ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${prod.brand} • ${prod.unit} • Stock: ${prod.stock} units", fontSize = 11.sp, color = Color.Gray)
                            Text("MRP: ₹${prod.mrp.toInt()} | Selling: ₹${prod.sellingPrice.toInt()} (${prod.discountPercent}% off)", fontSize = 11.sp, color = KMartPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        Row {
                            IconButton(onClick = { onEditClick(prod) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = KMartPrimary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDelete(prod) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCategoriesScreen(
    categories: List<CategoryEntity>,
    onAddClick: () -> Unit,
    onDelete: (CategoryEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Grocery Categories", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Category", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it.id }) { cat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛒", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Key: ${cat.iconKey}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = { onDelete(cat) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminInventoryScreen(
    products: List<ProductEntity>,
    onUpdateStock: (Long, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Stock & Inventory Adjustments", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Changes update live across customer catalog and server validation", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(products, key = { it.id }) { prod ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("SKU: ${prod.sku}", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "Current Stock: ${prod.stock} units",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (prod.stock <= 5) Color.Red else KMartPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                onClick = { onUpdateStock(prod.id, (prod.stock - 5).coerceAtLeast(0)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("-5", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            FilledTonalButton(
                                onClick = { onUpdateStock(prod.id, prod.stock + 10) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+10", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPartnersScreen(
    partners: List<UserEntity>,
    onToggleActive: (Long, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Delivery Fleet & KYC Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(partners, key = { it.id }) { p ->
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
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("📞 ${p.phone} • ${p.vehicleType} (${p.vehicleNumber})", fontSize = 11.sp, color = Color.Gray)
                            }
                            Surface(
                                color = if (p.isOnline) KMartPrimaryContainer else Color(0xFFEEEEEE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (p.isOnline) "ONLINE" else "OFFLINE",
                                    color = if (p.isOnline) KMartPrimary else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("KYC: ${p.kycStatus} | Rating: ⭐ 4.8", fontSize = 11.sp)
                            Switch(
                                checked = p.isOnline,
                                onCheckedChange = { onToggleActive(p.id, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = KMartPrimary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStoresScreen(stores: List<StoreEntity>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Multi-Warehouse & Dark Stores", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Autonomous store dispatch based on customer geofence radius", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(stores, key = { it.id }) { st ->
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
                            Text(st.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(color = KMartPrimaryContainer, shape = RoundedCornerShape(4.dp)) {
                                Text("RADIUS: ${st.serviceRadiusKm} KM", color = KMartPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(st.address, fontSize = 12.sp, color = Color.DarkGray)
                        Text("Manager: ${st.manager}", fontSize = 11.sp, color = Color.Gray)
                        Text("Hours: ${st.openingHours}", fontSize = 11.sp, color = KMartPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsScreen(onExportCsv: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Business Settings & Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Operational Rules", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("• Free Delivery Above: ₹499.00", fontSize = 12.sp)
                Text("• Base Delivery Fee: ₹25.00", fontSize = 12.sp)
                Text("• Platform & Handling Fee: ₹4.00", fontSize = 12.sp)
                Text("• GST Rate: 5% on Groceries", fontSize = 12.sp)
                Text("• Delivery Partner Base Pay: ₹55.00 / trip", fontSize = 12.sp)
                Text("• Payment Gateway Mode: Razorpay Secured (Production Architecture)", fontSize = 12.sp)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Export Reports", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Download full sales, orders, and delivery records in CSV format.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onExportCsv,
                    colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export All Orders CSV", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    categories: List<CategoryEntity>,
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "") }
    var mrp by remember { mutableStateOf(product?.mrp?.toString() ?: "100.0") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "85.0") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "50") }
    var weight by remember { mutableStateOf(product?.weight ?: "500g") }
    var unit by remember { mutableStateOf(product?.unit ?: "500 g") }
    var selectedCatId by remember { mutableStateOf(product?.categoryId ?: categories.firstOrNull()?.id ?: 1L) }
    var description by remember { mutableStateOf(product?.description ?: "Fresh high quality product") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = mrp, onValueChange = { mrp = it }, label = { Text("MRP (₹)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = { Text("Selling Price (₹)") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = mrp.toDoubleOrNull() ?: 100.0
                    val s = sellingPrice.toDoubleOrNull() ?: 85.0
                    val disc = if (m > 0) (((m - s) / m) * 100).toInt() else 0
                    val p = ProductEntity(
                        id = product?.id ?: 0L,
                        name = name.ifBlank { "New Grocery Item" },
                        brand = brand.ifBlank { "JSR KMart" },
                        categoryId = selectedCatId,
                        description = description,
                        mrp = m,
                        sellingPrice = s,
                        discountPercent = disc,
                        unit = unit,
                        weight = weight,
                        stock = stock.toIntOrNull() ?: 50,
                        sku = product?.sku ?: "JSR-${(1000..9999).random()}",
                        imageUrl = product?.imageUrl ?: "",
                        isAvailable = true,
                        rating = product?.rating ?: 4.5f
                    )
                    onSave(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf("grocery") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = iconKey, onValueChange = { iconKey = it }, label = { Text("Icon Key (e.g. veg, dairy, snacks)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val c = CategoryEntity(
                            id = 0L,
                            name = name,
                            iconKey = iconKey.ifBlank { "grocery" },
                            displayOrder = 99,
                            isActive = true
                        )
                        onSave(c)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
