package com.example.data.local

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun seedDatabaseIfEmpty(dao: JSRKMartDao) = withContext(Dispatchers.IO) {
        val existingProducts = dao.getProductByIdSync(1)
        if (existingProducts != null) return@withContext

        // 1. Stores
        val stores = listOf(
            StoreEntity(
                id = 1,
                name = "JSR KMart Indiranagar Hub",
                address = "100 Feet Rd, HAL 2nd Stage, Indiranagar, Bengaluru",
                lat = 12.9784,
                lng = 77.6408,
                serviceRadiusKm = 7.5,
                openingHours = "6:00 AM - 11:45 PM",
                manager = "Vikram Aditya",
                isActive = true
            ),
            StoreEntity(
                id = 2,
                name = "JSR KMart Koramangala Express",
                address = "80 Feet Road, 4th Block, Koramangala, Bengaluru",
                lat = 12.9352,
                lng = 77.6245,
                serviceRadiusKm = 8.0,
                openingHours = "6:00 AM - 12:00 AM",
                manager = "Priya Sharma",
                isActive = true
            )
        )
        dao.insertStores(stores)

        // 2. Categories (12 categories)
        val categories = listOf(
            CategoryEntity(id = 1, name = "Fruits & Vegetables", iconKey = "veg", displayOrder = 1),
            CategoryEntity(id = 2, name = "Dairy & Eggs", iconKey = "dairy", displayOrder = 2),
            CategoryEntity(id = 3, name = "Bakery", iconKey = "bakery", displayOrder = 3),
            CategoryEntity(id = 4, name = "Snacks", iconKey = "snacks", displayOrder = 4),
            CategoryEntity(id = 5, name = "Beverages", iconKey = "beverages", displayOrder = 5),
            CategoryEntity(id = 6, name = "Groceries", iconKey = "grocery", displayOrder = 6),
            CategoryEntity(id = 7, name = "Staples", iconKey = "staples", displayOrder = 7),
            CategoryEntity(id = 8, name = "Household", iconKey = "household", displayOrder = 8),
            CategoryEntity(id = 9, name = "Personal Care", iconKey = "personal", displayOrder = 9),
            CategoryEntity(id = 10, name = "Baby Care", iconKey = "baby", displayOrder = 10),
            CategoryEntity(id = 11, name = "Pet Care", iconKey = "pet", displayOrder = 11),
            CategoryEntity(id = 12, name = "Frozen Food", iconKey = "frozen", displayOrder = 12)
        )
        dao.insertCategories(categories)

        // 3. Products (36 realistic grocery items)
        val products = listOf(
            // Fruits & Vegetables
            ProductEntity(1, "Fresh Farm Tomatoes", "JSR Fresh", "Organically harvested juicy ripe hybrid tomatoes.", 45.0, 32.0, 28, "1 kg", "1000g", 85, 10, "SKU-TOM-01", 1, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.8f, reviewCount = 240),
            ProductEntity(2, "Shimla Fresh Red Apples", "Kashmir Valley", "Crisp and sweet premium red royal apples.", 180.0, 149.0, 17, "4 pcs (approx. 600g)", "600g", 42, 10, "SKU-APP-02", 1, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.9f, reviewCount = 310),
            ProductEntity(3, "Fresh Green Palak (Spinach)", "JSR Organic", "Tender leaves, pesticide-free fresh farm harvested spinach.", 30.0, 22.0, 26, "1 bunch (250 g)", "250g", 60, 15, "SKU-SPN-03", 1, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.6f, reviewCount = 88),
            ProductEntity(4, "Nashik Red Onions", "JSR Staples", "Firm and pungent culinary staple red onions.", 50.0, 38.0, 24, "1 kg", "1000g", 120, 20, "SKU-ONI-04", 1, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.7f, reviewCount = 520),
            ProductEntity(5, "Robusta Bananas", "JSR Fresh", "Naturally ripened sweet bananas rich in potassium.", 48.0, 36.0, 25, "1 kg (5-6 pcs)", "1000g", 75, 15, "SKU-BAN-05", 1, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.7f, reviewCount = 190),

            // Dairy & Eggs
            ProductEntity(6, "Amul Taaza Homogenised Milk", "Amul", "UHT treated toned fresh cow & buffalo milk.", 36.0, 34.0, 5, "500 ml", "500ml", 95, 20, "SKU-AMU-06", 2, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.9f, reviewCount = 890),
            ProductEntity(7, "Nandini Pure Cow Ghee", "Nandini", "Traditional golden granulated pure aromatic cow ghee.", 320.0, 295.0, 8, "500 ml Pouch", "500ml", 40, 8, "SKU-GHE-07", 2, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.9f, reviewCount = 412),
            ProductEntity(8, "Farm Fresh Brown Eggs", "Eggoz", "High protein nutrition enriched brown country eggs.", 95.0, 78.0, 18, "Pack of 6", "6 pcs", 55, 10, "SKU-EGG-08", 2, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.8f, reviewCount = 340),
            ProductEntity(9, "Mother Dairy Malai Paneer", "Mother Dairy", "Soft, creamy high protein cottage cheese cubes.", 115.0, 102.0, 11, "200 g", "200g", 30, 8, "SKU-PAN-09", 2, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.7f, reviewCount = 210),

            // Bakery
            ProductEntity(10, "The Baker's Dozen Whole Wheat Bread", "The Baker's Dozen", "100% whole wheat sourdough sandwich bread, zero preservatives.", 65.0, 55.0, 15, "400 g", "400g", 35, 10, "SKU-BRD-10", 3, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.8f, reviewCount = 175),
            ProductEntity(11, "English Oven Multigrain Burger Buns", "English Oven", "Fluffy seeded artisanal burger buns pack.", 45.0, 39.0, 13, "Pack of 2", "200g", 24, 6, "SKU-BUN-11", 3, isAvailable = true, isFeatured = false, isBestSeller = false, rating = 4.5f, reviewCount = 65),

            // Snacks
            ProductEntity(12, "Lay's India's Magic Masala Chips", "Lay's", "Crispy potato chips coated with tangy Indian spices.", 20.0, 18.0, 10, "50 g", "50g", 140, 25, "SKU-LAY-12", 4, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.7f, reviewCount = 620),
            ProductEntity(13, "Haldiram's Bhujia Sev", "Haldiram's", "Crunchy spicy crispy tepary bean and gram flour snack.", 60.0, 52.0, 13, "200 g", "200g", 80, 15, "SKU-BHU-13", 4, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.8f, reviewCount = 380),
            ProductEntity(14, "Cadbury Dairy Milk Silk Chocolate", "Cadbury", "Creamy, smooth melting milk chocolate bar.", 100.0, 92.0, 8, "60 g", "60g", 90, 15, "SKU-CDY-14", 4, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.9f, reviewCount = 540),

            // Beverages
            ProductEntity(15, "Paper Boat Aamras Mango Juice", "Paper Boat", "Authentic Alphonso pulp mango beverage.", 40.0, 35.0, 12, "250 ml", "250ml", 70, 12, "SKU-AAM-15", 5, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.7f, reviewCount = 210),
            ProductEntity(16, "Red Label Strong Tea Leaves", "Brooke Bond", "Rich aromatic blended CTC black tea granules.", 175.0, 155.0, 11, "500 g", "500g", 60, 10, "SKU-TEA-16", 5, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.8f, reviewCount = 390),
            ProductEntity(17, "Nescafe Classic Instant Coffee", "Nescafe", "100% pure roasted coffee beans for rich morning aroma.", 210.0, 189.0, 10, "100 g Glass Jar", "100g", 45, 8, "SKU-COF-17", 5, isAvailable = true, isFeatured = true, isBestSeller = false, rating = 4.8f, reviewCount = 310),

            // Staples & Groceries
            ProductEntity(18, "Fortune Sunlite Refined Sunflower Oil", "Fortune", "Light and fortified healthy cooking oil.", 160.0, 138.0, 14, "1 L Pouch", "1000ml", 90, 15, "SKU-OIL-18", 7, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.7f, reviewCount = 470),
            ProductEntity(19, "Aashirvaad Shudh Chakki Atta", "Aashirvaad", "100% whole wheat flour for fluffy and soft rotis.", 260.0, 225.0, 13, "5 kg", "5000g", 50, 10, "SKU-ATT-19", 7, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.9f, reviewCount = 980),
            ProductEntity(20, "Tata Sampann Unpolished Toor Dal", "Tata Sampann", "High protein unpolished arhar dal with natural taste.", 190.0, 165.0, 13, "1 kg", "1000g", 65, 12, "SKU-DAL-20", 7, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.8f, reviewCount = 310),
            ProductEntity(21, "India Gate Feast Rozzana Basmati Rice", "India Gate", "Fluffy, long grain aromatic aged basmati rice.", 135.0, 110.0, 18, "1 kg", "1000g", 70, 15, "SKU-RIC-21", 7, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.8f, reviewCount = 460),
            ProductEntity(22, "Tata Salt Vacuum Evaporated", "Tata", "Desh Ka Namak with essential iodine fortification.", 28.0, 26.0, 7, "1 kg", "1000g", 150, 25, "SKU-SLT-22", 6, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.9f, reviewCount = 820),
            ProductEntity(23, "Madhur Pure & Hygienic Sugar", "Madhur", "Refined sparkling sulfur-free white crystal sugar.", 65.0, 52.0, 20, "1 kg", "1000g", 85, 15, "SKU-SUG-23", 6, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.7f, reviewCount = 290),

            // Household
            ProductEntity(24, "Vim Lemon Dishwash Gel", "Vim", "Concentrated grease cleaning lemon liquid gel.", 110.0, 89.0, 19, "750 ml Bottle", "750ml", 55, 10, "SKU-VIM-24", 8, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.8f, reviewCount = 370),
            ProductEntity(25, "Surf Excel Matic Front Load Liquid", "Surf Excel", "Tough stain removal detergent liquid with freshness.", 260.0, 219.0, 16, "1 L Bottle", "1000ml", 40, 8, "SKU-SRF-25", 8, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.8f, reviewCount = 510),
            ProductEntity(26, "Origami So Soft 3-Ply Toilet Tissue", "Origami", "Soft absorbent embossed tissue roll pack.", 160.0, 129.0, 19, "Pack of 4", "4 rolls", 30, 6, "SKU-TIS-26", 8, isAvailable = true, isFeatured = false, isBestSeller = false, rating = 4.6f, reviewCount = 110),

            // Personal Care
            ProductEntity(27, "Dettol Original Liquid Handwash Refill", "Dettol", "Antibacterial germ protection hand hygiene refill.", 109.0, 88.0, 19, "675 ml Refill Pouch", "675ml", 65, 12, "SKU-DET-27", 9, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.9f, reviewCount = 680),
            ProductEntity(28, "Colgate MaxFresh Spicy Fresh Toothpaste", "Colgate", "Cooling crystals with invigorating peppermint sensation.", 125.0, 105.0, 16, "150 g Pack", "150g", 80, 15, "SKU-COL-28", 9, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.7f, reviewCount = 440),
            ProductEntity(29, "Dove Deeply Nourishing Body Wash", "Dove", "NutriumMoisture technology for soft, moisturized skin.", 225.0, 185.0, 18, "250 ml", "250ml", 35, 7, "SKU-DOV-29", 9, isAvailable = true, isFeatured = true, isBestSeller = false, rating = 4.8f, reviewCount = 210),

            // Baby Care
            ProductEntity(30, "Pampers All Round Protection Pants (M)", "Pampers", "Anti-rash lotion with magic gel core diaper pants.", 699.0, 549.0, 21, "42 Pants (Medium)", "Pack", 28, 5, "SKU-PAM-30", 10, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.8f, reviewCount = 390),
            ProductEntity(31, "Himalaya Gentle Baby Wipes", "Himalaya", "Aloe vera and Indian lotus soothing wipes.", 190.0, 145.0, 24, "Pack of 72 wipes", "72 pcs", 45, 10, "SKU-HIM-31", 10, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.7f, reviewCount = 260),

            // Pet Care
            ProductEntity(32, "Pedigree Adult Chicken & Vegetables", "Pedigree", "Complete nutrition dry dog food for active adult dogs.", 380.0, 329.0, 13, "1.2 kg Bag", "1200g", 25, 5, "SKU-PED-32", 11, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.8f, reviewCount = 180),
            ProductEntity(33, "Whiskas Wet Cat Food Ocean Fish in Jelly", "Whiskas", "Tasty wet gravy meal for adult feline companions.", 45.0, 39.0, 13, "85 g Pouch", "85g", 60, 12, "SKU-WHI-33", 11, isAvailable = true, isFeatured = false, isBestSeller = false, rating = 4.6f, reviewCount = 95),

            // Frozen Food
            ProductEntity(34, "McCain French Fries Crispy", "McCain", "Golden, crispy potato fries ready in 3 minutes.", 130.0, 109.0, 16, "420 g Pack", "420g", 38, 8, "SKU-MCC-34", 12, isAvailable = true, isFeatured = true, isBestSeller = true, rating = 4.7f, reviewCount = 220),
            ProductEntity(35, "Sumeru Green Peas IQF", "Sumeru", "Individually quick frozen tender sweet garden green peas.", 85.0, 68.0, 20, "500 g", "500g", 44, 10, "SKU-SUM-35", 12, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.6f, reviewCount = 140),
            ProductEntity(36, "Amul Frostik Choco Bar Ice Cream", "Amul", "Crunchy chocolate outer coating with creamy vanilla center.", 30.0, 25.0, 17, "60 ml", "60ml", 50, 10, "SKU-FRO-36", 12, isAvailable = true, isFeatured = false, isBestSeller = true, rating = 4.8f, reviewCount = 310)
        )
        dao.insertProducts(products)

        // 4. Users (5 Customers, 5 Delivery Partners, 1 Admin)
        val users = listOf(
            // Admin
            UserEntity(1, UserRole.ADMIN.name, "Super Admin (JSR KMart)", "9876500001", "admin@jsrkmart.in", "", "VERIFIED", isOnline = true),

            // Customers
            UserEntity(2, UserRole.CUSTOMER.name, "Aarav Sharma", "9876510001", "aarav.sharma@gmail.com", "", "VERIFIED", totalSpent = 4850.0),
            UserEntity(3, UserRole.CUSTOMER.name, "Ananya Iyer", "9876510002", "ananya.iyer@gmail.com", "", "VERIFIED", totalSpent = 3120.0),
            UserEntity(4, UserRole.CUSTOMER.name, "Rohan Deshmukh", "9876510003", "rohan.d@gmail.com", "", "VERIFIED", totalSpent = 6200.0),
            UserEntity(5, UserRole.CUSTOMER.name, "Kavya Patel", "9876510004", "kavya.patel@gmail.com", "", "VERIFIED", totalSpent = 1940.0),
            UserEntity(6, UserRole.CUSTOMER.name, "Nikhil Reddy", "9876510005", "nikhil.reddy@gmail.com", "", "VERIFIED", totalSpent = 5380.0),

            // Delivery Partners
            UserEntity(7, UserRole.DELIVERY_PARTNER.name, "Sunil Gowda", "9876520001", "sunil.delivery@jsrkmart.in", "", "VERIFIED", "BIKE", "KA03 HJ 4421", isOnline = true),
            UserEntity(8, UserRole.DELIVERY_PARTNER.name, "Ramesh Kumar", "9876520002", "ramesh.delivery@jsrkmart.in", "", "VERIFIED", "EV SCOOTER", "KA05 EV 9822", isOnline = true),
            UserEntity(9, UserRole.DELIVERY_PARTNER.name, "Mohammad Zeeshan", "9876520003", "zeeshan.delivery@jsrkmart.in", "", "VERIFIED", "BIKE", "KA01 MK 3319", isOnline = true),
            UserEntity(10, UserRole.DELIVERY_PARTNER.name, "Deepak Joshi", "9876520004", "deepak.delivery@jsrkmart.in", "", "VERIFIED", "SCOOTER", "KA04 AB 6712", isOnline = false),
            UserEntity(11, UserRole.DELIVERY_PARTNER.name, "Kiran Yadav", "9876520005", "kiran.delivery@jsrkmart.in", "", "PENDING", "BIKE", "KA51 CD 8834", isOnline = false)
        )
        dao.insertUsers(users)

        // 5. Addresses for Customers
        val addresses = listOf(
            AddressEntity(1, 2, "Home", "Flat 402, Green Glen Layout, Bellandur", "Near Central Mall", "Bengaluru", "560103", 12.9279, 77.6741, isDefault = true),
            AddressEntity(2, 2, "Office", "Tower 3, Prestige Tech Park, Marathahalli", "Outer Ring Road", "Bengaluru", "560103", 12.9360, 77.6910, isDefault = false),
            AddressEntity(3, 3, "Home", "Villa 12, Palm Meadows, Whitefield", "Near Forum Shantiniketan", "Bengaluru", "560066", 12.9698, 77.7499, isDefault = true),
            AddressEntity(4, 4, "Home", "Apt 201, 14th Main, HSR Layout Sector 4", "Opposite BDA Complex", "Bengaluru", "560102", 12.9116, 77.6389, isDefault = true),
            AddressEntity(5, 5, "Home", "No. 88, 5th Cross, 1st Block Koramangala", "Near Wipro Park", "Bengaluru", "560034", 12.9344, 77.6288, isDefault = true)
        )
        dao.insertAddresses(addresses)

        // 6. Coupons
        val coupons = listOf(
            CouponEntity(1, "FIRSTJSR", "Get 50% discount up to ₹150 on your first grocery order", DiscountType.PERCENTAGE.name, 50.0, 199.0, 150.0, "31 Dec 2026", 500, 42, isActive = true),
            CouponEntity(2, "SUPERMART", "Flat ₹100 OFF on orders above ₹499", DiscountType.FIXED.name, 100.0, 499.0, 100.0, "31 Dec 2026", 1000, 118, isActive = true),
            CouponEntity(3, "FREEDEL", "Free delivery on all grocery essentials above ₹149", DiscountType.FIXED.name, 25.0, 149.0, 25.0, "31 Dec 2026", 2000, 310, isActive = true),
            CouponEntity(4, "WEEKEND20", "20% OFF on weekend fruits, bakery & snacks", DiscountType.PERCENTAGE.name, 20.0, 299.0, 120.0, "31 Dec 2026", 800, 89, isActive = true)
        )
        dao.insertCoupons(coupons)

        // 7. Banners
        val banners = listOf(
            BannerEntity(
                id = 1,
                title = "⚡ Grocery in 10 Minutes",
                subtitle = "Farm fresh veggies, dairy, staples & munchies delivered lightning fast",
                badgeText = "SUPERFAST 10 MIN",
                categoryId = 1,
                drawableName = "ic_hero_grocery_banner",
                isActive = true,
                displayOrder = 1
            ),
            BannerEntity(
                id = 2,
                title = "Mega Indian Grocery Sale",
                subtitle = "Up to 50% OFF on atta, ghee, spices, beverages and daily essentials",
                badgeText = "SAVE BIG TODAY",
                categoryId = 7,
                drawableName = "ic_promo_deals_banner",
                isActive = true,
                displayOrder = 2
            )
        )
        dao.insertBanners(banners)

        // 8. Demo Orders (Real lifecycle state examples)
        val orders = listOf(
            OrderEntity(
                id = 1,
                orderNumber = "JSR-77291",
                customerId = 2,
                storeId = 1,
                deliveryPartnerId = 7,
                status = OrderStatus.OUT_FOR_DELIVERY.name,
                subtotal = 385.0,
                deliveryFee = 25.0,
                tax = 12.0,
                platformFee = 4.0,
                couponDiscount = 100.0,
                finalTotal = 326.0,
                paymentMethod = PaymentMethod.UPI.name,
                paymentStatus = PaymentStatus.PAID.name,
                deliveryOtp = "4826",
                addressSnapshot = "Flat 402, Green Glen Layout, Bellandur, Bengaluru",
                customerName = "Aarav Sharma",
                customerPhone = "9876510001",
                deliveryInstructions = "Please ring doorbell twice and leave at doorstep if unavailable.",
                liveLat = 12.9512,
                liveLng = 77.6520,
                estimatedMinutes = 8,
                createdAt = System.currentTimeMillis() - (15 * 60 * 1000),
                updatedAt = System.currentTimeMillis() - (2 * 60 * 1000)
            ),
            OrderEntity(
                id = 2,
                orderNumber = "JSR-65104",
                customerId = 3,
                storeId = 1,
                deliveryPartnerId = 8,
                status = OrderStatus.DELIVERED.name,
                subtotal = 740.0,
                deliveryFee = 0.0,
                tax = 22.0,
                platformFee = 4.0,
                couponDiscount = 120.0,
                finalTotal = 646.0,
                paymentMethod = PaymentMethod.CARD.name,
                paymentStatus = PaymentStatus.PAID.name,
                deliveryOtp = "1932",
                addressSnapshot = "Villa 12, Palm Meadows, Whitefield, Bengaluru",
                customerName = "Ananya Iyer",
                customerPhone = "9876510002",
                deliveryInstructions = "Leave at gate security if required.",
                createdAt = System.currentTimeMillis() - (2 * 60 * 60 * 1000),
                updatedAt = System.currentTimeMillis() - (1 * 60 * 60 * 1000)
            ),
            OrderEntity(
                id = 3,
                orderNumber = "JSR-91823",
                customerId = 4,
                storeId = 2,
                deliveryPartnerId = 9,
                status = OrderStatus.PACKING.name,
                subtotal = 215.0,
                deliveryFee = 25.0,
                tax = 8.0,
                platformFee = 4.0,
                couponDiscount = 0.0,
                finalTotal = 252.0,
                paymentMethod = PaymentMethod.COD.name,
                paymentStatus = PaymentStatus.PENDING.name,
                deliveryOtp = "5519",
                addressSnapshot = "Apt 201, 14th Main, HSR Layout Sector 4, Bengaluru",
                customerName = "Rohan Deshmukh",
                customerPhone = "9876510003",
                deliveryInstructions = "Deliver before 9 PM",
                createdAt = System.currentTimeMillis() - (10 * 60 * 1000),
                updatedAt = System.currentTimeMillis() - (5 * 60 * 1000)
            )
        )
        dao.insertOrders(orders)

        // Order Items for Order #1
        val orderItems = listOf(
            OrderItemEntity(1, 1, 1, "Fresh Farm Tomatoes", "1 kg", 32.0, 45.0, 2, 64.0),
            OrderItemEntity(2, 1, 6, "Amul Taaza Homogenised Milk", "500 ml", 34.0, 36.0, 3, 102.0),
            OrderItemEntity(3, 1, 10, "The Baker's Dozen Whole Wheat Bread", "400 g", 55.0, 65.0, 1, 55.0),
            OrderItemEntity(4, 1, 14, "Cadbury Dairy Milk Silk Chocolate", "60 g", 92.0, 100.0, 1, 92.0),
            OrderItemEntity(5, 1, 12, "Lay's India's Magic Masala Chips", "50 g", 18.0, 20.0, 4, 72.0),

            // Items for Order #2
            OrderItemEntity(6, 2, 7, "Nandini Pure Cow Ghee", "500 ml Pouch", 295.0, 320.0, 1, 295.0),
            OrderItemEntity(7, 2, 19, "Aashirvaad Shudh Chakki Atta", "5 kg", 225.0, 260.0, 1, 225.0),
            OrderItemEntity(8, 2, 20, "Tata Sampann Unpolished Toor Dal", "1 kg", 165.0, 190.0, 1, 165.0),

            // Items for Order #3
            OrderItemEntity(9, 3, 15, "Paper Boat Aamras Mango Juice", "250 ml", 35.0, 40.0, 2, 70.0),
            OrderItemEntity(10, 3, 8, "Farm Fresh Brown Eggs", "Pack of 6", 78.0, 95.0, 1, 78.0),
            OrderItemEntity(11, 3, 2, "Shimla Fresh Red Apples", "4 pcs (approx. 600g)", 149.0, 180.0, 1, 149.0)
        )
        dao.insertOrderItems(orderItems)

        // Partner Earnings for delivered order #2
        dao.insertPartnerEarning(
            PartnerEarningsEntity(
                id = 1,
                partnerId = 8,
                orderId = 2,
                baseFee = 45.0,
                distanceFee = 20.0,
                incentive = 15.0,
                tip = 20.0,
                totalEarning = 100.0,
                createdAt = System.currentTimeMillis() - (1 * 60 * 60 * 1000)
            )
        )

        // App Settings
        val settings = listOf(
            AppSettingEntity("appName", "JSR KMart"),
            AppSettingEntity("currency", "₹"),
            AppSettingEntity("taxPercent", "5.0"),
            AppSettingEntity("deliveryFee", "25.0"),
            AppSettingEntity("minOrderValue", "99.0"),
            AppSettingEntity("freeDeliveryThreshold", "499.0"),
            AppSettingEntity("platformFee", "4.0"),
            AppSettingEntity("deliveryRadiusKm", "8.0"),
            AppSettingEntity("codEnabled", "true"),
            AppSettingEntity("upiEnabled", "true"),
            AppSettingEntity("partnerCommissionPerOrder", "55.0"),
            AppSettingEntity("customerSupportPhone", "1800-202-JSRK"),
            AppSettingEntity("customerSupportEmail", "support@jsrkmart.in")
        )
        settings.forEach { dao.setSetting(it) }

        // Initial Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                id = 1,
                action = "SYSTEM_INITIALIZATION",
                details = "Initialized JSR KMart quick-commerce store with 36 products, 12 categories, 2 darkstores, 5 delivery partners and coupons."
            )
        )

        // Initial Notification
        dao.insertNotification(
            NotificationEntity(
                id = 1,
                userId = 2,
                role = "CUSTOMER",
                title = "Welcome to JSR KMart! ⚡",
                message = "Enjoy lightning fast 10-minute grocery delivery. Use code FIRSTJSR for 50% OFF.",
                type = "WELCOME"
            )
        )
    }
}
