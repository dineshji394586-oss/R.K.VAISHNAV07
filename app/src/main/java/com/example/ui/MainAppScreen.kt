package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.customer.*
import com.example.ui.partner.DeliveryPartnerScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.CustomerTab
import com.example.ui.viewmodel.JSRKMartViewModel
import com.example.ui.web.WebContainerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: JSRKMartViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val customerTab by viewModel.customerTab.collectAsStateWithLifecycle()
    val cartSummary by viewModel.cartSummary.collectAsStateWithLifecycle()
    val customerOrders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val addresses by viewModel.customerAddresses.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    var showRoleMenu by remember { mutableStateOf(false) }
    var isWebMode by remember { mutableStateOf(false) }

    val defaultAddress = remember(addresses) {
        addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
    }

    val activeOrdersCount = remember(customerOrders) {
        customerOrders.count {
            it.status != OrderStatus.DELIVERED.name &&
            it.status != OrderStatus.CANCELLED.name &&
            it.status != OrderStatus.REFUNDED.name
        }
    }

    val totalCartItems = remember(cartSummary) {
        cartSummary.items.sumOf { it.cartItem.quantity }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = "${msg.title}: ${msg.message}",
                duration = SnackbarDuration.Short
            )
            viewModel.clearUiMessage()
        }
    }

    if (isWebMode) {
        WebContainerScreen(
            onSwitchToNative = { isWebMode = false },
            modifier = modifier
        )
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedLayout = maxWidth >= 720.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Global Branding & Role Switcher Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo & Location
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = KMartPrimary,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = "JSR KMart",
                                        tint = KMartYellowFlash,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "JSR KMart",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = KMartPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = KMartAccent
                                    ) {
                                        Text(
                                            text = "10 MINS",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = defaultAddress?.let { "${it.label}: ${it.fullAddress}" } ?: "Bistupur, Jamshedpur 831001",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }

                        // Right Action: Web Mode Button + Role Switcher
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Quick Switch to Responsive Web App Mode
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier
                                    .clickable { isWebMode = true }
                                    .testTag("open_web_app_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = "Web App",
                                        tint = KMartAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Web App",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // Role Switcher Dropdown Trigger
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = when (currentRole) {
                                        UserRole.CUSTOMER -> KMartPrimaryContainer
                                        UserRole.DELIVERY_PARTNER -> Color(0xFFFEF3C7)
                                        UserRole.ADMIN -> Color(0xFFE0E7FF)
                                    },
                                    modifier = Modifier
                                        .clickable { showRoleMenu = true }
                                        .testTag("role_switcher_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (currentRole) {
                                                UserRole.CUSTOMER -> Icons.Default.ShoppingBag
                                                UserRole.DELIVERY_PARTNER -> Icons.Default.TwoWheeler
                                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = when (currentRole) {
                                                UserRole.CUSTOMER -> KMartPrimary
                                                UserRole.DELIVERY_PARTNER -> Color(0xFFB45309)
                                                UserRole.ADMIN -> Color(0xFF3730A3)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (currentRole) {
                                                UserRole.CUSTOMER -> "Customer"
                                                UserRole.DELIVERY_PARTNER -> "Rider"
                                                UserRole.ADMIN -> "Admin"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when (currentRole) {
                                                UserRole.CUSTOMER -> KMartPrimary
                                                UserRole.DELIVERY_PARTNER -> Color(0xFFB45309)
                                                UserRole.ADMIN -> Color(0xFF3730A3)
                                            }
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Gray
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showRoleMenu,
                                    onDismissRequest = { showRoleMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🛍️ Customer Shopping App") },
                                        onClick = {
                                            viewModel.setRole(UserRole.CUSTOMER)
                                            showRoleMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = KMartPrimary) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🛵 Delivery Partner App") },
                                        onClick = {
                                            viewModel.setRole(UserRole.DELIVERY_PARTNER)
                                            showRoleMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = KMartAccent) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("⚙️ Admin Operations Dashboard") },
                                        onClick = {
                                            viewModel.setRole(UserRole.ADMIN)
                                            showRoleMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.DarkGray) }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text("🌐 Open Web App (Desktop / Mobile)") },
                                        onClick = {
                                            isWebMode = true
                                            showRoleMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = KMartAccent) }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (currentRole == UserRole.CUSTOMER && !isExpandedLayout) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        NavigationBarItem(
                            selected = customerTab == CustomerTab.HOME,
                            onClick = { viewModel.setCustomerTab(CustomerTab.HOME) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = KMartPrimary,
                                selectedTextColor = KMartPrimary,
                                indicatorColor = KMartPrimaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = customerTab == CustomerTab.CATEGORIES,
                            onClick = { viewModel.setCustomerTab(CustomerTab.CATEGORIES) },
                            icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                            label = { Text("Categories", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = KMartPrimary,
                                selectedTextColor = KMartPrimary,
                                indicatorColor = KMartPrimaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = customerTab == CustomerTab.SEARCH,
                            onClick = { viewModel.setCustomerTab(CustomerTab.SEARCH) },
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = KMartPrimary,
                                selectedTextColor = KMartPrimary,
                                indicatorColor = KMartPrimaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = customerTab == CustomerTab.CART,
                            onClick = { viewModel.setCustomerTab(CustomerTab.CART) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalCartItems > 0) {
                                            Badge(
                                                containerColor = KMartDiscountRed,
                                                contentColor = Color.White
                                            ) {
                                                Text("$totalCartItems")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                                }
                            },
                            label = { Text("Cart", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = KMartPrimary,
                                selectedTextColor = KMartPrimary,
                                indicatorColor = KMartPrimaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = customerTab == CustomerTab.ORDERS,
                            onClick = { viewModel.setCustomerTab(CustomerTab.ORDERS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (activeOrdersCount > 0) {
                                            Badge(
                                                containerColor = KMartAccent,
                                                contentColor = Color.White
                                            ) {
                                                Text("$activeOrdersCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = "Orders")
                                }
                            },
                            label = { Text("Orders", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = KMartPrimary,
                                selectedTextColor = KMartPrimary,
                                indicatorColor = KMartPrimaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = customerTab == CustomerTab.PROFILE,
                            onClick = { viewModel.setCustomerTab(CustomerTab.PROFILE) },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = KMartPrimary,
                                selectedTextColor = KMartPrimary,
                                indicatorColor = KMartPrimaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // If Expanded (Desktop/Tablet landscape layout), display NavigationRail on the left!
                if (currentRole == UserRole.CUSTOMER && isExpandedLayout) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        NavigationRailItem(
                            selected = customerTab == CustomerTab.HOME,
                            onClick = { viewModel.setCustomerTab(CustomerTab.HOME) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 10.sp) }
                        )

                        NavigationRailItem(
                            selected = customerTab == CustomerTab.CATEGORIES,
                            onClick = { viewModel.setCustomerTab(CustomerTab.CATEGORIES) },
                            icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                            label = { Text("Categories", fontSize = 10.sp) }
                        )

                        NavigationRailItem(
                            selected = customerTab == CustomerTab.SEARCH,
                            onClick = { viewModel.setCustomerTab(CustomerTab.SEARCH) },
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search", fontSize = 10.sp) }
                        )

                        NavigationRailItem(
                            selected = customerTab == CustomerTab.CART,
                            onClick = { viewModel.setCustomerTab(CustomerTab.CART) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalCartItems > 0) {
                                            Badge(
                                                containerColor = KMartDiscountRed,
                                                contentColor = Color.White
                                            ) {
                                                Text("$totalCartItems")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                                }
                            },
                            label = { Text("Cart", fontSize = 10.sp) }
                        )

                        NavigationRailItem(
                            selected = customerTab == CustomerTab.ORDERS,
                            onClick = { viewModel.setCustomerTab(CustomerTab.ORDERS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (activeOrdersCount > 0) {
                                            Badge(
                                                containerColor = KMartAccent,
                                                contentColor = Color.White
                                            ) {
                                                Text("$activeOrdersCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = "Orders")
                                }
                            },
                            label = { Text("Orders", fontSize = 10.sp) }
                        )

                        NavigationRailItem(
                            selected = customerTab == CustomerTab.PROFILE,
                            onClick = { viewModel.setCustomerTab(CustomerTab.PROFILE) },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 10.sp) }
                        )
                    }
                }

                // Main Content Pane
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (currentRole) {
                        UserRole.CUSTOMER -> {
                            when (customerTab) {
                                CustomerTab.HOME -> CustomerHomeScreen(viewModel = viewModel)
                                CustomerTab.CATEGORIES -> CustomerCatalogScreen(viewModel = viewModel)
                                CustomerTab.SEARCH -> CustomerCatalogScreen(viewModel = viewModel)
                                CustomerTab.CART -> CustomerCartCheckoutScreen(viewModel = viewModel)
                                CustomerTab.ORDERS -> CustomerOrdersScreen(viewModel = viewModel)
                                CustomerTab.PROFILE -> CustomerProfileScreen(viewModel = viewModel)
                            }
                        }

                        UserRole.DELIVERY_PARTNER -> {
                            DeliveryPartnerScreen(viewModel = viewModel)
                        }

                        UserRole.ADMIN -> {
                            AdminDashboardScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
