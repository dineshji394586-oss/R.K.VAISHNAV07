package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.ProductEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun RoleSwitchingBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserRole.values().forEach { role ->
                val isSelected = currentRole == role
                val label = when (role) {
                    UserRole.CUSTOMER -> "🛍️ Customer"
                    UserRole.DELIVERY_PARTNER -> "🛵 Delivery Partner"
                    UserRole.ADMIN -> "⚙️ Admin Hub"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { onRoleSelected(role) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun JSRKMartLogo(
    modifier: Modifier = Modifier,
    showTagline: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom dynamic logo mark
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(KMartGreenPrimary, Color(0xFF024626))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "JSR KMart Logo",
                tint = KMartYellowFlash,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "JSR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KMartGreenPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "KMart",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = KMartOrangeAccent,
                    letterSpacing = 0.5.sp
                )
            }
            if (showTagline) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚡ 10 MIN DELIVERY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = KMartGreenDark
                    )
                }
            }
        }
    }
}

@Composable
fun PriceTag(
    sellingPrice: Double,
    mrp: Double,
    discountPercent: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "₹${sellingPrice.toInt()}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (mrp > sellingPrice) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "₹${mrp.toInt()}",
                fontSize = 12.sp,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            if (discountPercent > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(KMartGreenContainer)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$discountPercent% OFF",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KMartOnGreenContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    cartQuantity: Int,
    isWishlisted: Boolean,
    onAddToCart: () -> Unit,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(168.dp)
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Top Badge and Wishlist
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                // Category/Product Icon Art
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when {
                        product.name.contains("Tomato", ignoreCase = true) || product.name.contains("Palak", ignoreCase = true) -> Icons.Default.Eco
                        product.name.contains("Milk", ignoreCase = true) || product.name.contains("Ghee", ignoreCase = true) || product.name.contains("Paneer", ignoreCase = true) -> Icons.Default.Egg
                        product.name.contains("Bread", ignoreCase = true) || product.name.contains("Bun", ignoreCase = true) -> Icons.Default.BakeryDining
                        product.name.contains("Tea", ignoreCase = true) || product.name.contains("Coffee", ignoreCase = true) || product.name.contains("Juice", ignoreCase = true) -> Icons.Default.LocalCafe
                        product.name.contains("Atta", ignoreCase = true) || product.name.contains("Rice", ignoreCase = true) || product.name.contains("Dal", ignoreCase = true) -> Icons.Default.RiceBowl
                        product.name.contains("Wash", ignoreCase = true) || product.name.contains("Detergent", ignoreCase = true) -> Icons.Default.CleanHands
                        product.name.contains("Dog", ignoreCase = true) || product.name.contains("Cat", ignoreCase = true) -> Icons.Default.Pets
                        else -> Icons.Default.ShoppingBag
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = product.name,
                        modifier = Modifier.size(46.dp),
                        tint = KMartGreenPrimary.copy(alpha = 0.85f)
                    )
                }

                // Discount pill on top-left
                if (product.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(bottomEnd = 6.dp))
                            .background(KMartDiscountBadge)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Wishlist button on top-right
                IconButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Unit/Weight
            Text(
                text = product.unit,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            // Product Name
            Text(
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = KMartStarYellow,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${product.rating} (${product.reviewCount})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price and Add/Qty Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${product.sellingPrice.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.mrp > product.sellingPrice) {
                        Text(
                            text = "₹${product.mrp.toInt()}",
                            fontSize = 10.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                if (cartQuantity == 0) {
                    OutlinedButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("add_to_cart_${product.id}"),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KMartGreenPrimary)
                    ) {
                        Text(
                            text = "ADD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KMartGreenPrimary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KMartGreenPrimary),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onDecreaseQty() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Text(
                            text = "$cartQuantity",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onIncreaseQty() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        OrderStatus.PLACED.name -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "PLACED")
        OrderStatus.CONFIRMED.name -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "CONFIRMED")
        OrderStatus.PACKING.name -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "PACKING")
        OrderStatus.READY_FOR_PICKUP.name -> Triple(Color(0xFFEDE7F6), Color(0xFF512DA8), "READY")
        OrderStatus.ASSIGNED.name -> Triple(Color(0xFFE1F5FE), Color(0xFF0277BD), "ASSIGNED")
        OrderStatus.PICKED_UP.name -> Triple(Color(0xFFE0F2F1), Color(0xFF00695C), "PICKED UP")
        OrderStatus.OUT_FOR_DELIVERY.name -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "OUT FOR DELIVERY ⚡")
        OrderStatus.DELIVERED.name -> Triple(KMartGreenContainer, KMartOnGreenContainer, "DELIVERED ✓")
        OrderStatus.CANCELLED.name -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "CANCELLED")
        OrderStatus.REFUNDED.name -> Triple(Color(0xFFECEFF1), Color(0xFF37474F), "REFUNDED")
        else -> Triple(Color.LightGray, Color.DarkGray, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun LiveMapCanvasView(
    liveLat: Double,
    liveLng: Double,
    estimatedMinutes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE5EDE8))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Draw map grid roads
            val roadColor = Color(0xFFFFFFFF)
            val stroke = Stroke(width = 8.dp.toPx())
            val thinStroke = Stroke(width = 3.dp.toPx())

            // Main avenues
            drawLine(roadColor, Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 12.dp.toPx())
            drawLine(roadColor, Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 10.dp.toPx())
            drawLine(roadColor, Offset(w * 0.3f, 0f), Offset(w * 0.3f, h), strokeWidth = 10.dp.toPx())
            drawLine(roadColor, Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 10.dp.toPx())

            // Park / Green zones
            drawRoundRect(
                color = Color(0xFFCEE5D6),
                topLeft = Offset(w * 0.05f, h * 0.05f),
                size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.25f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )

            // Route path from Store to Rider to Customer
            val storePos = Offset(w * 0.15f, h * 0.35f)
            val customerPos = Offset(w * 0.85f, h * 0.7f)
            val riderPos = Offset(w * 0.55f, h * 0.52f)

            val routePath = Path().apply {
                moveTo(storePos.x, storePos.y)
                lineTo(w * 0.3f, storePos.y)
                lineTo(w * 0.3f, h * 0.7f)
                lineTo(customerPos.x, customerPos.y)
            }

            drawPath(
                path = routePath,
                color = KMartGreenPrimary.copy(alpha = 0.5f),
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            )

            // Draw Store Pin
            drawCircle(color = Color(0xFF1976D2), radius = 9.dp.toPx(), center = storePos)
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = storePos)

            // Draw Customer Pin
            drawCircle(color = KMartOrangeAccent, radius = 10.dp.toPx(), center = customerPos)
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = customerPos)

            // Draw Delivery Rider Pulse & Pin
            drawCircle(color = KMartGreenPrimary.copy(alpha = 0.3f), radius = 18.dp.toPx(), center = riderPos)
            drawCircle(color = KMartGreenPrimary, radius = 11.dp.toPx(), center = riderPos)
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = riderPos)
        }

        // Overlay Map Labels & ETA Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Darkstore", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = KMartOrangeAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Your Doorstep", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Floating ETA Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            shape = RoundedCornerShape(20.dp),
            color = KMartGreenPrimary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricScooter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Arriving in $estimatedMinutes mins",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    OrderStatusBadge(status = status, modifier = modifier)
}

@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    OrderStatusBadge(status = status.name, modifier = modifier)
}

@Composable
fun ProductCard(
    product: ProductEntity,
    cartQuantity: Int,
    isWishlisted: Boolean,
    onAddToCart: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onToggleWishlist: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ProductItemCard(
        product = product,
        cartQuantity = cartQuantity,
        isWishlisted = isWishlisted,
        onAddToCart = onAddToCart,
        onIncreaseQty = { onUpdateQuantity(1) },
        onDecreaseQty = { onUpdateQuantity(-1) },
        onToggleWishlist = onToggleWishlist,
        modifier = modifier.clickable { onClick() }
    )
}

@Composable
fun ProductImageWithFallback(
    imageUrl: String,
    productName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val icon = when {
            productName.contains("Tomato", ignoreCase = true) || productName.contains("Palak", ignoreCase = true) || productName.contains("Apple", ignoreCase = true) -> Icons.Default.Eco
            productName.contains("Milk", ignoreCase = true) || productName.contains("Ghee", ignoreCase = true) || productName.contains("Paneer", ignoreCase = true) -> Icons.Default.Egg
            productName.contains("Bread", ignoreCase = true) || productName.contains("Bun", ignoreCase = true) -> Icons.Default.BakeryDining
            productName.contains("Tea", ignoreCase = true) || productName.contains("Coffee", ignoreCase = true) || productName.contains("Juice", ignoreCase = true) -> Icons.Default.LocalCafe
            productName.contains("Atta", ignoreCase = true) || productName.contains("Rice", ignoreCase = true) || productName.contains("Dal", ignoreCase = true) -> Icons.Default.RiceBowl
            productName.contains("Wash", ignoreCase = true) || productName.contains("Detergent", ignoreCase = true) -> Icons.Default.CleanHands
            productName.contains("Dog", ignoreCase = true) || productName.contains("Cat", ignoreCase = true) -> Icons.Default.Pets
            else -> Icons.Default.ShoppingBag
        }
        Icon(
            imageVector = icon,
            contentDescription = productName,
            modifier = Modifier.fillMaxSize(0.65f),
            tint = KMartGreenPrimary
        )
    }
}
