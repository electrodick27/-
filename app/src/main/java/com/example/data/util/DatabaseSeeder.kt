package com.example.data.util

import com.example.data.dao.CrmDao
import com.example.data.entity.*

object DatabaseSeeder {

    suspend fun seed(dao: CrmDao) {
        // Only seed if users are empty (first launch)
        val testUser = dao.getUserById("admin")
        if (testUser != null) return

        // 1. Seed Users
        val users = listOf(
            User("admin", "Админ Лось", "admin", HashUtils.sha256("admin123")),
            User("operator", "Оператор Иван", "operator", HashUtils.sha256("operator123")),
            User("tech", "Технолог Петр", "technologist", HashUtils.sha256("tech123")),
            User("manager", "Начальник производства", "manager", HashUtils.sha256("manager123")),
            User("director", "Директор Лосев", "director", HashUtils.sha256("director123"))
        )
        users.forEach { dao.insertUser(it) }

        // 2. Seed Products
        val products = listOf(
            Product("p3", "P-3"),
            Product("p3-ultra", "P-3 ULTRA PRO"),
            Product("p3-twl", "P-3 TWL"),
            Product("c1", "C-1"),
            Product("c1-pro", "C-1 PRO BT")
        )
        products.forEach { dao.insertProduct(it) }

        // 3. Seed Operations (based on Section 8 and tool diameters)
        val operations = listOf(
            // P-3
            Operation(productId = "p3", name = "Сверловка Ø2.7", temporaryMeters = 0.18f, requiredToolDiameter = 2.7f),
            Operation(productId = "p3", name = "Ввод Ø4", temporaryMeters = 0.23f, requiredToolDiameter = 4.0f),
            Operation(productId = "p3", name = "Провод Ø4", temporaryMeters = 0.23f, requiredToolDiameter = 4.0f),
            Operation(productId = "p3", name = "Кнопка Ø4", temporaryMeters = 0.14f, requiredToolDiameter = 4.0f),

            // P-3 ULTRA PRO
            Operation(productId = "p3-ultra", name = "Сверловка Ø2.7", temporaryMeters = 0.18f, requiredToolDiameter = 2.7f),
            Operation(productId = "p3-ultra", name = "Ввод Ø4", temporaryMeters = 0.23f, requiredToolDiameter = 4.0f),
            Operation(productId = "p3-ultra", name = "Провод Ø4", temporaryMeters = 0.23f, requiredToolDiameter = 4.0f),
            Operation(productId = "p3-ultra", name = "Индикация Ø3.1", temporaryMeters = 0.16f, requiredToolDiameter = 3.1f),

            // P-3 TWL
            Operation(productId = "p3-twl", name = "Сверловка Ø2.7", temporaryMeters = 0.18f, requiredToolDiameter = 2.7f),
            Operation(productId = "p3-twl", name = "Провод Ø4", temporaryMeters = 0.23f, requiredToolDiameter = 4.0f),
            Operation(productId = "p3-twl", name = "Индикация + зарядка Ø3.1", temporaryMeters = 0.16f, requiredToolDiameter = 3.1f),

            // C-1
            Operation(productId = "c1", name = "Сверловка Ø1.6 / Ø3.2", temporaryMeters = 0.18f, requiredToolDiameter = 3.2f),
            Operation(productId = "c1", name = "Паз под провод Ø4", temporaryMeters = 0.42f, requiredToolDiameter = 4.0f),
            Operation(productId = "c1", name = "Кнопка Ø4", temporaryMeters = 0.14f, requiredToolDiameter = 4.0f),

            // C-1 PRO BT
            Operation(productId = "c1-pro", name = "Сверловка Ø1.6 / Ø3.2", temporaryMeters = 0.18f, requiredToolDiameter = 3.2f),
            Operation(productId = "c1-pro", name = "Паз под провод Ø4", temporaryMeters = 0.42f, requiredToolDiameter = 4.0f),
            Operation(productId = "c1-pro", name = "Кнопка + индикация Ø4", temporaryMeters = 0.14f, requiredToolDiameter = 4.0f)
        )
        operations.forEach { dao.insertOperation(it) }

        // 4. Seed Month Plans (June 2026)
        val plans = listOf(
            PlanItem(productId = "p3", plannedQty = 400, doneQty = 120, priority = 1, deadline = 1782820800000L, monthYear = "06.2026"), // June 30, 2026
            PlanItem(productId = "p3-ultra", plannedQty = 200, doneQty = 86, priority = 2, deadline = 1782648000000L, monthYear = "06.2026"), // June 28, 2026
            PlanItem(productId = "p3-twl", plannedQty = 150, doneQty = 50, priority = 3, deadline = 1782734400000L, monthYear = "06.2026"), // June 29, 2026
            PlanItem(productId = "c1", plannedQty = 350, doneQty = 100, priority = 4, deadline = 1782561600000L, monthYear = "06.2026"), // June 27, 2026
            PlanItem(productId = "c1-pro", plannedQty = 250, doneQty = 50, priority = 5, deadline = 1782475200000L, monthYear = "06.2026") // June 26, 2026
        )
        plans.forEach { dao.insertPlanItem(it) }

        // 5. Seed Warehouse Stocks
        val stocks = listOf(
            WarehouseStock("p3", received = 1000, stock = 500, inWork = 20, defects = 10, minStock = 100, supplierName = "Завод Сталь", batchNumber = "P3-01"),
            WarehouseStock("p3-ultra", received = 500, stock = 250, inWork = 10, defects = 5, minStock = 80, supplierName = "Сибирь-Ресурс", batchNumber = "U-12"),
            WarehouseStock("p3-twl", received = 400, stock = 150, inWork = 15, defects = 8, minStock = 50, supplierName = "Завод Сталь", batchNumber = "TW-9"),
            WarehouseStock("c1", received = 800, stock = 400, inWork = 30, defects = 12, minStock = 100, supplierName = "ПрофильОпт", batchNumber = "C1-B"),
            WarehouseStock("c1-pro", received = 600, stock = 300, inWork = 25, defects = 15, minStock = 70, supplierName = "ПрофильОпт", batchNumber = "CP-7")
        )
        stocks.forEach { dao.insertWarehouseStock(it) }

        // 6. Seed Tools (one is low on resources! e.g., сверло 3.1: 750/800 = 93.75%, remaining 6.25% < 15%)
        val tools = listOf(
            Tool(name = "Сверло Ø2.7", article = "DR-27", diameter = 2.7f, maxLifetimeMeters = 1000f, metersUsed = 250f, status = "В работе"),
            Tool(name = "Фреза Ø4.0", article = "FR-40", diameter = 4.0f, maxLifetimeMeters = 1500f, metersUsed = 600f, status = "В работе"),
            Tool(name = "Сверло Ø3.1", article = "DR-31", diameter = 3.1f, maxLifetimeMeters = 800f, metersUsed = 750f, status = "В работе")
        )
        tools.forEach { dao.insertTool(it) }

        // 7. Seed Invites
        val invites = listOf(
            Invite("OP-START", "operator"),
            Invite("TC-2026", "technologist")
        )
        invites.forEach { dao.insertInvite(it) }

        // 8. Seed Audit Logs
        dao.insertAuditLog(AuditLog(userId = "system", userName = "Система", action = "База данных успешно инициализирована."))
    }
}
