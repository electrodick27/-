package com.example.data.repository

import com.example.data.dao.CrmDao
import com.example.data.entity.*
import com.example.data.util.DatabaseSeeder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class CrmRepository(private val crmDao: CrmDao) {

    // --- Flows for UI tracking ---
    val allUsers: Flow<List<User>> = crmDao.getAllUsers()
    val allProducts: Flow<List<Product>> = crmDao.getAllProducts()
    val allOperations: Flow<List<Operation>> = crmDao.getAllOperations()
    val allPlanItems: Flow<List<PlanItem>> = crmDao.getAllPlanItems()
    val allOperationLogs: Flow<List<OperationLog>> = crmDao.getAllOperationLogs()
    val allTimeEntries: Flow<List<TimeEntry>> = crmDao.getAllTimeEntries()
    val allWarehouseStocks: Flow<List<WarehouseStock>> = crmDao.getAllWarehouseStocks()
    val allWarehouseMovements: Flow<List<WarehouseMovement>> = crmDao.getAllWarehouseMovements()
    val allDefects: Flow<List<DefectLog>> = crmDao.getAllDefects()
    val allTools: Flow<List<Tool>> = crmDao.getAllTools()
    val allMessages: Flow<List<Message>> = crmDao.getAllMessages()
    val allAuditLogs: Flow<List<AuditLog>> = crmDao.getAllAuditLogs()
    val allInvites: Flow<List<Invite>> = crmDao.getAllInvites()

    suspend fun seedIfNeeded() {
        DatabaseSeeder.seed(crmDao)
    }

    // --- Authentication Actions ---
    suspend fun getUserById(id: String): User? = crmDao.getUserById(id)
    suspend fun insertUser(user: User) = crmDao.insertUser(user)
    suspend fun updateUser(user: User) = crmDao.updateUser(user)
    suspend fun deleteUser(user: User) = crmDao.deleteUser(user)

    // --- Product & Route Actions ---
    suspend fun getProductById(id: String): Product? = crmDao.getProductById(id)
    suspend fun insertProduct(product: Product) = crmDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = crmDao.updateProduct(product)
    fun getOperationsForProduct(productId: String): Flow<List<Operation>> = crmDao.getOperationsForProduct(productId)
    suspend fun insertOperation(operation: Operation) = crmDao.insertOperation(operation)
    suspend fun updateOperation(operation: Operation) = crmDao.updateOperation(operation)
    suspend fun deleteOperationById(id: Int) = crmDao.deleteOperationById(id)

    // --- Plan Month Actions ---
    suspend fun getPlanItemById(id: Int): PlanItem? = crmDao.getPlanItemById(id)
    suspend fun insertPlanItem(planItem: PlanItem) = crmDao.insertPlanItem(planItem)
    suspend fun updatePlanItem(planItem: PlanItem) = crmDao.updatePlanItem(planItem)
    suspend fun deletePlanItemById(id: Int) = crmDao.deletePlanItemById(id)
    fun getPlanHistory(planItemId: Int): Flow<List<PlanHistory>> = crmDao.getPlanHistory(planItemId)
    suspend fun insertPlanHistory(history: PlanHistory) = crmDao.insertPlanHistory(history)

    // --- Tool Actions ---
    suspend fun getToolById(id: Int): Tool? = crmDao.getToolById(id)
    suspend fun insertTool(tool: Tool) = crmDao.insertTool(tool)
    suspend fun updateTool(tool: Tool) = crmDao.updateTool(tool)
    suspend fun deleteToolById(id: Int) = crmDao.deleteToolById(id)
    fun getToolHistory(toolId: Int): Flow<List<ToolHistory>> = crmDao.getToolHistory(toolId)

    // --- Invite Actions ---
    suspend fun getInviteByCode(code: String): Invite? = crmDao.getInviteByCode(code)
    suspend fun insertInvite(invite: Invite) = crmDao.insertInvite(invite)
    suspend fun updateInvite(invite: Invite) = crmDao.updateInvite(invite)

    // --- Defect Actions ---
    suspend fun updateDefect(defectLog: DefectLog) = crmDao.updateDefect(defectLog)

    // --- Messaging ---
    suspend fun insertMessage(message: Message) = crmDao.insertMessage(message)
    suspend fun deleteMessageById(id: Int) = crmDao.deleteMessageById(id)

    // --- Audit Logs ---
    suspend fun logAudit(userId: String, userName: String, action: String) {
        crmDao.insertAuditLog(AuditLog(userId = userId, userName = userName, action = action))
    }

    // --- Warehouse Management ---
    suspend fun insertWarehouseStock(stock: WarehouseStock) = crmDao.insertWarehouseStock(stock)
    suspend fun addWarehouseStock(productId: String, receivedQty: Int, supplierName: String?, batchNumber: String?, userId: String) {
        val current = crmDao.getStockForProduct(productId) ?: WarehouseStock(productId = productId)
        val updated = current.copy(
            received = current.received + receivedQty,
            stock = current.stock + receivedQty,
            supplierName = supplierName ?: current.supplierName,
            batchNumber = batchNumber ?: current.batchNumber,
            lastUpdated = System.currentTimeMillis()
        )
        crmDao.insertWarehouseStock(updated)
        crmDao.insertWarehouseMovement(
            WarehouseMovement(
                productId = productId,
                movementType = "Приход",
                qty = receivedQty,
                comment = "Поставка. Партия: ${batchNumber ?: "N/A"}. Поставщик: ${supplierName ?: "N/A"}",
                userId = userId
            )
        )
    }

    suspend fun adjustStock(productId: String, type: String, qty: Int, comment: String, userId: String) {
        val current = crmDao.getStockForProduct(productId) ?: WarehouseStock(productId = productId)
        val newStock = when (type) {
            "Приход" -> current.stock + qty
            "Передача в работу" -> current.stock - qty
            "Брак" -> current.stock - qty
            else -> qty // Inventory audit or overwrite
        }
        val updated = current.copy(
            stock = if (type == "Инвентаризация" || type == "Корректировка") qty else newStock,
            lastUpdated = System.currentTimeMillis()
        )
        crmDao.insertWarehouseStock(updated)
        crmDao.insertWarehouseMovement(
            WarehouseMovement(
                productId = productId,
                movementType = type,
                qty = qty,
                comment = comment,
                userId = userId
            )
        )
    }

    // --- Active Work Flows (Sections 7.1 & 7.2) ---
    fun getActiveWorkFlow(operatorId: String): Flow<ActiveWork?> = crmDao.getActiveWork(operatorId)

    suspend fun startWork(
        operatorId: String,
        operatorName: String,
        productId: String,
        operationId: Int,
        qtyStarted: Int,
        machineId: String?
    ): Result<Unit> {
        // Rule: Limit of active operations per operator
        val active = crmDao.getActiveWorkSync(operatorId)
        if (active != null) {
            return Result.failure(Exception("У вас уже есть активная операция. Завершите или отмените её перед началом новой."))
        }

        // Rule: Prevent start if tool is written off or worn
        val allOperationsList = crmDao.getAllOperations().first()
        val operation = allOperationsList.find { it.id == operationId }
            ?: return Result.failure(Exception("Операция не найдена."))

        if (operation.requiredToolDiameter != null) {
            val toolsList = crmDao.getAllTools().first()
            val tool = toolsList.find { it.diameter == operation.requiredToolDiameter && it.status != "Списан" }
            if (tool == null) {
                return Result.failure(Exception("Подходящий инструмент диаметром ${operation.requiredToolDiameter} мм не найден."))
            }
            if (tool.status == "Изношен" || tool.metersUsed >= tool.maxLifetimeMeters) {
                return Result.failure(Exception("Инструмент '${tool.name}' изношен (${tool.metersUsed}/${tool.maxLifetimeMeters} м). Начать работу невозможно. Пожалуйста, замените инструмент."))
            }
        }

        // Rule: Prevent start if raw materials are insufficient on warehouse
        val stock = crmDao.getStockForProduct(productId)
            ?: return Result.failure(Exception("Данные о заготовках данного изделия на складе отсутствуют."))
        if (stock.stock < qtyStarted) {
            return Result.failure(Exception("Недостаточно заготовок на складе (в наличии: ${stock.stock} шт., требуется: $qtyStarted шт.). Невозможно начать."))
        }

        // Adjust warehouse: transfer from stock to inWork
        val updatedStock = stock.copy(
            stock = stock.stock - qtyStarted,
            inWork = stock.inWork + qtyStarted,
            lastUpdated = System.currentTimeMillis()
        )
        crmDao.insertWarehouseStock(updatedStock)

        // Log warehouse movement
        crmDao.insertWarehouseMovement(
            WarehouseMovement(
                productId = productId,
                movementType = "Передача в работу",
                qty = qtyStarted,
                comment = "Операция: ${operation.name}. Оператор: $operatorName",
                userId = operatorId
            )
        )

        // Insert Active Work
        val work = ActiveWork(
            operatorId = operatorId,
            productId = productId,
            operationId = operationId,
            qtyStarted = qtyStarted,
            startTime = System.currentTimeMillis(),
            machineId = machineId
        )
        crmDao.insertActiveWork(work)

        // Insert Time Entry for automatic time tracking starting
        val activeTime = crmDao.getActiveTimeEntry(operatorId)
        if (activeTime == null) {
            // Auto start generic "Работа" category time session if none active
            crmDao.insertTimeEntry(
                TimeEntry(
                    userId = operatorId,
                    category = "Работа",
                    startTime = System.currentTimeMillis(),
                    endTime = 0
                )
            )
        }

        logAudit(operatorId, operatorName, "Начал операцию '${operation.name}' по изделию '$productId' (кол-во: $qtyStarted).")
        return Result.success(Unit)
    }

    suspend fun pauseWork(operatorId: String, operatorName: String, reason: String): Result<Unit> {
        val active = crmDao.getActiveWorkSync(operatorId)
            ?: return Result.failure(Exception("Активная операция не найдена."))

        val updated = active.copy(isPaused = true, pauseReason = reason)
        crmDao.insertActiveWork(updated)

        // Close current automatic time entry, open a "Настройка" or "Простой" depending on reason
        val activeTime = crmDao.getActiveTimeEntry(operatorId)
        if (activeTime != null) {
            crmDao.updateTimeEntry(activeTime.copy(endTime = System.currentTimeMillis()))
        }
        val category = when {
            reason.contains("настройк", ignoreCase = true) || reason.contains("переналад", ignoreCase = true) -> "Настройка"
            reason.contains("поломок", ignoreCase = true) || reason.contains("ремонт", ignoreCase = true) -> "Ремонт"
            else -> "Простой"
        }
        crmDao.insertTimeEntry(
            TimeEntry(
                userId = operatorId,
                category = category,
                startTime = System.currentTimeMillis(),
                endTime = 0
            )
        )

        logAudit(operatorId, operatorName, "Приостановил операцию. Причина: $reason.")
        return Result.success(Unit)
    }

    suspend fun resumeWork(operatorId: String, operatorName: String): Result<Unit> {
        val active = crmDao.getActiveWorkSync(operatorId)
            ?: return Result.failure(Exception("Активная операция не найдена."))

        val updated = active.copy(isPaused = false, pauseReason = null)
        crmDao.insertActiveWork(updated)

        // Close pause category time entry, open back generic "Работа"
        val activeTime = crmDao.getActiveTimeEntry(operatorId)
        if (activeTime != null) {
            crmDao.updateTimeEntry(activeTime.copy(endTime = System.currentTimeMillis()))
        }
        crmDao.insertTimeEntry(
            TimeEntry(
                userId = operatorId,
                category = "Работа",
                startTime = System.currentTimeMillis(),
                endTime = 0
            )
        )

        logAudit(operatorId, operatorName, "Возобновил операцию.")
        return Result.success(Unit)
    }

    suspend fun cancelWork(operatorId: String, operatorName: String): Result<Unit> {
        val active = crmDao.getActiveWorkSync(operatorId)
            ?: return Result.failure(Exception("Активная операция не найдена."))

        // Return components back from inWork to stock
        val stock = crmDao.getStockForProduct(active.productId)
        if (stock != null) {
            val updatedStock = stock.copy(
                stock = stock.stock + active.qtyStarted,
                inWork = (stock.inWork - active.qtyStarted).coerceAtLeast(0),
                lastUpdated = System.currentTimeMillis()
            )
            crmDao.insertWarehouseStock(updatedStock)
        }

        // Log warehouse movement return
        crmDao.insertWarehouseMovement(
            WarehouseMovement(
                productId = active.productId,
                movementType = "Корректировка",
                qty = active.qtyStarted,
                comment = "Возврат на склад. Отмена операции оператором $operatorName",
                userId = operatorId
            )
        )

        crmDao.deleteActiveWork(operatorId)

        // Close active time entry
        val activeTime = crmDao.getActiveTimeEntry(operatorId)
        if (activeTime != null) {
            crmDao.updateTimeEntry(activeTime.copy(endTime = System.currentTimeMillis()))
        }

        logAudit(operatorId, operatorName, "Отменил начатую работу по изделию '${active.productId}'.")
        return Result.success(Unit)
    }

    suspend fun finishWork(
        operatorId: String,
        operatorName: String,
        qtyOk: Int,
        qtyDefect: Int,
        defectReason: String?,
        photoUri: String?
    ): Result<Unit> {
        val active = crmDao.getActiveWorkSync(operatorId)
            ?: return Result.failure(Exception("Активная работа не найдена."))

        val totalFinished = qtyOk + qtyDefect
        if (totalFinished > active.qtyStarted) {
            return Result.failure(Exception("Общее количество (Годные + Брак = $totalFinished шт.) превышает взятое в работу количество (${active.qtyStarted} шт.)."))
        }

        val allOperationsList = crmDao.getAllOperations().first()
        val operation = allOperationsList.find { it.id == active.operationId }
            ?: return Result.failure(Exception("Операция не найдена."))

        val allProductsList = crmDao.getAllProducts().first()
        val product = allProductsList.find { it.id == active.productId }
            ?: return Result.failure(Exception("Изделие не найдено."))

        // 1. Calculate meters used
        val ratePerUnit = operation.exactMeters ?: operation.temporaryMeters
        val metersUsed = totalFinished * ratePerUnit

        // 2. Update Tool Wear resource
        if (operation.requiredToolDiameter != null) {
            val toolsList = crmDao.getAllTools().first()
            val tool = toolsList.find { it.diameter == operation.requiredToolDiameter && it.status != "Списан" }
            if (tool != null) {
                val newMetersUsed = tool.metersUsed + metersUsed
                val isWorn = newMetersUsed >= tool.maxLifetimeMeters
                val updatedTool = tool.copy(
                    metersUsed = newMetersUsed,
                    status = if (isWorn) "Изношен" else "В работе"
                )
                crmDao.updateTool(updatedTool)
                crmDao.insertToolHistory(
                    ToolHistory(
                        toolId = tool.id,
                        metersUsed = metersUsed,
                        operationName = "${product.name} - ${operation.name}"
                    )
                )

                // Tool notifications / alerts
                val remainingRatio = (tool.maxLifetimeMeters - newMetersUsed) / tool.maxLifetimeMeters
                if (remainingRatio < 0.15f) {
                    crmDao.insertMessage(
                        Message(
                            userId = "system",
                            userName = "Ворнинг-Бот",
                            text = "⚠ ВНИМАНИЕ: Ресурс инструмента '${tool.name}' (Ø${tool.diameter} мм) на исходе! Осталось менее 15% (${((remainingRatio * 100).coerceAtLeast(0f)).roundToInt()}%). Пожалуйста, подготовьте замену.",
                            productId = product.id,
                            operationId = operation.id
                        )
                    )
                }
            }
        }

        // 3. Update warehouse stock
        val stock = crmDao.getStockForProduct(active.productId)
        if (stock != null) {
            val diffUnfinished = active.qtyStarted - totalFinished
            val updatedStock = stock.copy(
                inWork = (stock.inWork - active.qtyStarted).coerceAtLeast(0),
                defects = stock.defects + qtyDefect,
                // Return any unused pieces back to stock automatically
                stock = stock.stock + diffUnfinished,
                lastUpdated = System.currentTimeMillis()
            )
            crmDao.insertWarehouseStock(updatedStock)

            // Log warehouse movement for defect
            if (qtyDefect > 0) {
                crmDao.insertWarehouseMovement(
                    WarehouseMovement(
                        productId = active.productId,
                        movementType = "Брак",
                        qty = qtyDefect,
                        comment = "Обнаружен брак на операции: ${operation.name}. Причина: ${defectReason ?: "Не указана"}",
                        userId = operatorId
                    )
                )
            }
            if (diffUnfinished > 0) {
                crmDao.insertWarehouseMovement(
                    WarehouseMovement(
                        productId = active.productId,
                        movementType = "Корректировка",
                        qty = diffUnfinished,
                        comment = "Возврат неиспользованного сырья (${diffUnfinished} шт.) с операции: ${operation.name}",
                        userId = operatorId
                    )
                )
            }
        }

        // 4. Update monthly plan
        val plansList = crmDao.getAllPlanItems().first()
        val planItem = plansList.find { it.productId == active.productId }
        if (planItem != null) {
            val updatedPlan = planItem.copy(doneQty = planItem.doneQty + qtyOk)
            crmDao.updatePlanItem(updatedPlan)
            crmDao.insertPlanHistory(
                PlanHistory(
                    planItemId = planItem.id,
                    changerName = operatorName,
                    action = "Выполнено +$qtyOk шт. по операции '${operation.name}'"
                )
            )
        }

        // 5. If defect is logged, insert Defect Log
        if (qtyDefect > 0) {
            crmDao.insertDefect(
                DefectLog(
                    productId = active.productId,
                    productName = product.name,
                    operationName = operation.name,
                    qty = qtyDefect,
                    reason = defectReason ?: "Причина не указана",
                    photoUri = photoUri,
                    supplierName = stock?.supplierName
                )
            )

            // Rule: Alert supervisor if scrap rate > 5% of active work quantity
            val scrapRate = qtyDefect.toFloat() / active.qtyStarted.toFloat()
            if (scrapRate > 0.05f) {
                crmDao.insertMessage(
                    Message(
                        userId = "system",
                        userName = "Брак-Детектор",
                        text = "🚨 КРИТИЧЕСКИЙ БРАК! Оператор $operatorName зафиксировал высокий процент брака (${(scrapRate * 100).roundToInt()}% > 5%) по изделию '${product.name}' на операции '${operation.name}'! Изготовлено годных: $qtyOk, бракованных: $qtyDefect. Причина: ${defectReason ?: "N/A"}.",
                        productId = product.id,
                        operationId = operation.id
                    )
                )
            }
        }

        // 6. Insert Operation Log
        val duration = System.currentTimeMillis() - active.startTime
        crmDao.insertOperationLog(
            OperationLog(
                operatorId = operatorId,
                operatorName = operatorName,
                productId = active.productId,
                productName = product.name,
                operationId = active.operationId,
                operationName = operation.name,
                qtyOk = qtyOk,
                qtyDefect = qtyDefect,
                defectReason = defectReason,
                metersUsed = metersUsed,
                durationMs = duration,
                machineId = active.machineId
            )
        )

        // 7. Delete active work & close active time entries
        crmDao.deleteActiveWork(operatorId)

        val activeTime = crmDao.getActiveTimeEntry(operatorId)
        if (activeTime != null) {
            crmDao.updateTimeEntry(activeTime.copy(endTime = System.currentTimeMillis()))
        }

        logAudit(operatorId, operatorName, "Завершил операцию '${operation.name}' по '${product.name}'. Сдано: $qtyOk годных, $qtyDefect брака за ${(duration / 1000 / 60)} мин.")
        return Result.success(Unit)
    }

    // --- Direct Time entry manipulations (Section 4.6) ---
    suspend fun addTimeEntry(userId: String, category: String, startTime: Long, endTime: Long): Result<Unit> {
        // Prevent overlaps
        val allEntries = crmDao.getAllTimeEntries().first()
        val userEntries = allEntries.filter { it.userId == userId }
        val overlaps = userEntries.any { existing ->
            (startTime >= existing.startTime && startTime < existing.endTime) ||
            (endTime > existing.startTime && endTime <= existing.endTime) ||
            (startTime <= existing.startTime && endTime >= existing.endTime)
        }
        if (overlaps) {
            return Result.failure(Exception("Выбранный интервал времени пересекается с уже существующими записями в карте времени."))
        }

        crmDao.insertTimeEntry(TimeEntry(userId = userId, category = category, startTime = startTime, endTime = endTime))
        return Result.success(Unit)
    }
}
