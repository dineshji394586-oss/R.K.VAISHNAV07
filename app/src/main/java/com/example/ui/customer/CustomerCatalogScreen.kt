package com.example.ui.customer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.model.ProductEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.JSRKMartViewModel
import com.example.ui.viewmodel.ProductSortOrder
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCatalogScreen(
    viewModel: JSRKMartViewModel,
    modifier: Modifier = Modifier
) {
    val inr = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartSummary by viewModel.cartSummary.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.customerWishlist.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    var inStockOnly by remember { mutableStateOf(false) }
    var selectedProductForDetail by remember { mutableStateOf<ProductEntity?>(null) }

    val cartItemMap = remember(cartSummary) {
        cartSummary.items.associate { it.cartItem.productId to it.cartItem.quantity }
    }
    val wishlistSet = remember(wishlistIds) {
        wishlistIds.toSet()
    }

    // Filter and sort products
    val displayedProducts = remember(allProducts, searchQuery, selectedCategoryId, sortOrder, inStockOnly) {
        var list = allProducts

        if (selectedCategoryId != null) {
            list = list.filter { it.categoryId == selectedCategoryId }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.brand.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
        }

        if (inStockOnly) {
            list = list.filter { it.stock > 0 }
        }

        when (sortOrder) {
            ProductSortOrder.PRICE_LOW_HIGH -> list.sortedBy { it.sellingPrice }
            ProductSortOrder.PRICE_HIGH_LOW -> list.sortedByDescending { it.sellingPrice }
            ProductSortOrder.DISCOUNT -> list.sortedByDescending { it.discountPercent }
            else -> list.sortedByDescending { it.rating }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Filter Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_input"),
                    placeholder = {
                        Text(
                            text = "Search 30+ fresh groceries & essentials...",
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = KMartPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KMartPrimary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category selector chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All Items (${allProducts.size})") },
                            leadingIcon = if (selectedCategoryId == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KMartPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }

                    items(categories, key = { it.id }) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectCategory(if (isSelected) null else cat.id)
                            },
                            label = { Text(cat.name) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KMartPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Secondary sort & stock filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${displayedProducts.size} items found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = inStockOnly,
                            onClick = { inStockOnly = !inStockOnly },
                            label = { Text("In Stock", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KMartPrimaryContainer,
                                selectedLabelColor = KMartPrimary
                            )
                        )

                        AssistChip(
                            onClick = {
                                val next = when (sortOrder) {
                                    ProductSortOrder.POPULARITY -> ProductSortOrder.PRICE_LOW_HIGH
                                    ProductSortOrder.PRICE_LOW_HIGH -> ProductSortOrder.PRICE_HIGH_LOW
                                    ProductSortOrder.PRICE_HIGH_LOW -> ProductSortOrder.DISCOUNT
                                    else -> ProductSortOrder.POPULARITY
                                }
                                viewModel.setSortOrder(next)
                            },
                            label = {
                                val sortLabel = when (sortOrder) {
                                    ProductSortOrder.PRICE_LOW_HIGH -> "Price: Low to High"
                                    ProductSortOrder.PRICE_HIGH_LOW -> "Price: High to Low"
                                    ProductSortOrder.DISCOUNT -> "Highest Discount"
                                    else -> "Most Popular"
                                }
                                Text("Sort: $sortLabel", fontSize = 11.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }
            }
        }

        // Product Grid Content
        if (displayedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No groceries found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try searching for milk, atta, apples, paneer or reset filters",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.setSearchQuery("")
                            viewModel.selectCategory(null)
                            inStockOnly = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary)
                    ) {
                        Text("Reset All Filters")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedProducts, key = { it.id }) { product ->
                    val qty = cartItemMap[product.id] ?: 0
                    val isWishlisted = wishlistSet.contains(product.id)

                    ProductCard(
                        product = product,
                        cartQuantity = qty,
                        isWishlisted = isWishlisted,
                        onAddToCart = { viewModel.addToCart(product.id) },
                        onUpdateQuantity = { delta ->
                            viewModel.updateCartQty(product.id, qty + delta)
                        },
                        onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                        onClick = { selectedProductForDetail = product },
                        modifier = Modifier.testTag("product_grid_${product.id}")
                    )
                }
            }
        }
    }

    // Product Detail Bottom Sheet Modal
    selectedProductForDetail?.let { prod ->
        ModalBottomSheet(
            onDismissRequest = { selectedProductForDetail = null },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            val qty = cartItemMap[prod.id] ?: 0
            val isWishlisted = wishlistSet.contains(prod.id)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prod.brand.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KMartPrimary
                        )
                        Text(
                            text = prod.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${prod.weight} • ${prod.unit}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleWishlist(prod.id) }
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) Color.Red else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product image preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    ProductImageWithFallback(
                        imageUrl = prod.imageUrl,
                        productName = prod.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )

                    if (prod.discountPercent > 0) {
                        Surface(
                            color = KMartDiscountRed,
                            shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "${prod.discountPercent}% OFF",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Section
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = inr.format(prod.sellingPrice),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (prod.mrp > prod.sellingPrice) {
                        Text(
                            text = inr.format(prod.mrp),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }

                    Text(
                        text = "You save ${inr.format(prod.mrp - prod.sellingPrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KMartPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stock status badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val inStock = prod.stock > 0
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (inStock) KMartPrimary else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (inStock) "In Stock (${prod.stock} units available)" else "Out of stock",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (inStock) KMartPrimary else Color.Red
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "SKU: ${prod.sku}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Description",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = prod.description,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Add to Cart
                if (qty == 0) {
                    Button(
                        onClick = {
                            viewModel.addToCart(prod.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("detail_add_to_cart_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = KMartPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = prod.stock > 0
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (prod.stock > 0) "Add to Cart" else "Currently Out of Stock",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "In your cart ($qty)",
                            fontWeight = FontWeight.SemiBold,
                            color = KMartPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = { viewModel.updateCartQty(prod.id, qty - 1) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = KMartPrimary.copy(alpha = 0.15f)
                                )
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = KMartPrimary)
                            }
                            Text(
                                text = "$qty",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            FilledTonalIconButton(
                                onClick = { viewModel.updateCartQty(prod.id, qty + 1) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = KMartPrimary.copy(alpha = 0.15f)
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = KMartPrimary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
