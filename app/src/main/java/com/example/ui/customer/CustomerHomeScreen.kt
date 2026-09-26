package com.example.ui.customer

import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CustomerTab
import com.example.ui.viewmodel.JSRKMartViewModel

@Composable
fun CustomerHomeScreen(
    viewModel: JSRKMartViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val cartSummary by viewModel.cartSummary.collectAsStateWithLifecycle()
    val wishlist by viewModel.customerWishlist.collectAsStateWithLifecycle()
    val cartItemMap = remember(cartSummary) {
        cartSummary.items.associate { it.cartItem.productId to it.cartItem.quantity }
    }

    val bestSellers = remember(allProducts) {
        allProducts.filter { it.isBestSeller }
    }
    val freshVeggies = remember(allProducts) {
        allProducts.filter { it.categoryId == 1L }
    }
    val dairyAndBakery = remember(allProducts) {
        allProducts.filter { it.categoryId == 2L || it.categoryId == 3L }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Top Header with Location & Delivery ETA
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
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
                            JSRKMartLogo()

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = KMartGreenContainer,
                                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = KMartOnGreenContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "10 MINS",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = KMartOnGreenContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Delivery Address line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.setCustomerTab(CustomerTab.PROFILE) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = KMartGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Deliver to Bellandur, Bengaluru",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar Button (taps go to Search screen)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setCustomerTab(CustomerTab.SEARCH) }
                                .testTag("home_search_bar"),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Search 'tomatoes, milk, atta, chips...'",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Promotional Banners
            item {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(banners) { banner ->
                        BannerCard(
                            banner = banner,
                            onBannerClick = {
                                if (banner.categoryId != null) {
                                    viewModel.selectCategory(banner.categoryId)
                                    viewModel.setCustomerTab(CustomerTab.CATEGORIES)
                                }
                            }
                        )
                    }
                }
            }

            // 3. Quick Categories Strip
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shop by Category",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "See All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KMartGreenPrimary,
                        modifier = Modifier.clickable { viewModel.setCustomerTab(CustomerTab.CATEGORIES) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(categories) { cat ->
                        CategoryCircleItem(
                            category = cat,
                            onClick = {
                                viewModel.selectCategory(cat.id)
                                viewModel.setCustomerTab(CustomerTab.CATEGORIES)
                            }
                        )
                    }
                }
            }

            // 4. Best Sellers Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔥 Best Sellers",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Explore",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KMartGreenPrimary,
                        modifier = Modifier.clickable { viewModel.setCustomerTab(CustomerTab.CATEGORIES) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bestSellers) { product ->
                        ProductItemCard(
                            product = product,
                            cartQuantity = cartItemMap[product.id] ?: 0,
                            isWishlisted = wishlist.contains(product.id),
                            onAddToCart = { viewModel.addToCart(product.id) },
                            onIncreaseQty = { viewModel.updateCartQty(product.id, (cartItemMap[product.id] ?: 0) + 1) },
                            onDecreaseQty = { viewModel.updateCartQty(product.id, (cartItemMap[product.id] ?: 0) - 1) },
                            onToggleWishlist = { viewModel.toggleWishlist(product.id) }
                        )
                    }
                }
            }

            // 5. Fresh Fruits & Veggies Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🥬 Fresh Vegetables & Fruits",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KMartGreenPrimary,
                        modifier = Modifier.clickable {
                            viewModel.selectCategory(1L)
                            viewModel.setCustomerTab(CustomerTab.CATEGORIES)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(freshVeggies) { product ->
                        ProductItemCard(
                            product = product,
                            cartQuantity = cartItemMap[product.id] ?: 0,
                            isWishlisted = wishlist.contains(product.id),
                            onAddToCart = { viewModel.addToCart(product.id) },
                            onIncreaseQty = { viewModel.updateCartQty(product.id, (cartItemMap[product.id] ?: 0) + 1) },
                            onDecreaseQty = { viewModel.updateCartQty(product.id, (cartItemMap[product.id] ?: 0) - 1) },
                            onToggleWishlist = { viewModel.toggleWishlist(product.id) }
                        )
                    }
                }
            }

            // 6. Dairy & Bakery
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🥛 Dairy, Bread & Eggs",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dairyAndBakery) { product ->
                        ProductItemCard(
                            product = product,
                            cartQuantity = cartItemMap[product.id] ?: 0,
                            isWishlisted = wishlist.contains(product.id),
                            onAddToCart = { viewModel.addToCart(product.id) },
                            onIncreaseQty = { viewModel.updateCartQty(product.id, (cartItemMap[product.id] ?: 0) + 1) },
                            onDecreaseQty = { viewModel.updateCartQty(product.id, (cartItemMap[product.id] ?: 0) - 1) },
                            onToggleWishlist = { viewModel.toggleWishlist(product.id) }
                        )
                    }
                }
            }
        }

        // Floating Cart Bar at bottom when cart has items
        if (cartSummary.items.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.setCustomerTab(CustomerTab.CART) }
                    .testTag("floating_cart_bar"),
                color = KMartGreenPrimary,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${cartSummary.items.sumOf { it.cartItem.quantity }} ITEMS • ₹${cartSummary.finalTotal.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            if (cartSummary.totalSavings > 0) {
                                Text(
                                    text = "Saved ₹${cartSummary.totalSavings.toInt()} on this order",
                                    fontSize = 11.sp,
                                    color = KMartYellowFlash,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Cart",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BannerCard(
    banner: com.example.data.model.BannerEntity,
    onBannerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(130.dp)
            .clickable { onBannerClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val resId = remember(banner.drawableName) {
                if (banner.drawableName.isNotBlank()) {
                    context.resources.getIdentifier(banner.drawableName, "drawable", context.packageName)
                } else 0
            }

            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark overlay gradient for readable text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Black.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.2f))
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(KMartGreenDark, KMartGreenPrimary))
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (banner.badgeText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KMartYellowFlash)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = banner.badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                Column {
                    Text(
                        text = banner.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = banner.subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCircleItem(
    category: CategoryEntity,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
    ) {
        val icon = when (category.iconKey) {
            "veg" -> Icons.Default.Eco
            "dairy" -> Icons.Default.Egg
            "bakery" -> Icons.Default.BakeryDining
            "snacks" -> Icons.Default.Fastfood
            "beverages" -> Icons.Default.LocalCafe
            "grocery", "staples" -> Icons.Default.RiceBowl
            "household" -> Icons.Default.CleanHands
            "personal" -> Icons.Default.Spa
            "baby" -> Icons.Default.ChildCare
            "pet" -> Icons.Default.Pets
            "frozen" -> Icons.Default.AcUnit
            else -> Icons.Default.ShoppingBag
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(KMartGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category.name,
                tint = KMartGreenPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            lineHeight = 13.sp,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
