package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // e.g. "admin", "operator"
    val name: String,
    val role: String, // "admin", "operator", "technologist", "manager", "director"
    val passwordHash: String, // simple bcrypt-like or hashed, or plain for demo (we can do basic hash/check)
    val isBlocked: Boolean = false,
    val sessionActive: Boolean = false,
    val email: String? = null,
    val avatarUrl: String? = null
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String, // e.g. "p3", "c1"
    val name: String,
    val isArchived: Boolean = false
)

@Entity(tableName = "operations")
data class Operation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val name: String,
    val temporaryMeters: Float,
    val exactMeters: Float? = null,
    val requiredToolDiameter: Float? = null
)

@Entity(tableName = "plan_items")
data class PlanItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val plannedQty: Int,
    val doneQty: Int = 0,
    val priority: Int, // lower = higher priority
    val deadline: Long? = null,
    val monthYear: String // e.g., "06.2026"
)

@Entity(tableName = "plan_histories")
data class PlanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planItemId: Int,
    val changerName: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "active_works")
data class ActiveWork(
    @PrimaryKey val operatorId: String, // One active work per operator
    val productId: String,
    val operationId: Int,
    val qtyStarted: Int,
    val startTime: Long,
    val isPaused: Boolean = false,
    val pauseReason: String? = null,
    val machineId: String? = null
)

@Entity(tableName = "operation_logs")
data class OperationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val operatorId: String,
    val operatorName: String,
    val productId: String,
    val productName: String,
    val operationId: Int,
    val operationName: String,
    val qtyOk: Int,
    val qtyDefect: Int,
    val defectReason: String? = null,
    val metersUsed: Float,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val machineId: String? = null
)

@Entity(tableName = "time_entries")
data class TimeEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val category: String, // "Работа", "Перемещение", "Настройка", "ППР", "Ремонт", "Простой", "Другая работа"
    val startTime: Long,
    val endTime: Long // if active, set to 0 or current time
)

@Entity(tableName = "warehouse_stocks")
data class WarehouseStock(
    @PrimaryKey val productId: String,
    val received: Int = 0,
    val stock: Int = 0,
    val inWork: Int = 0,
    val defects: Int = 0,
    val minStock: Int = 0,
    val supplierName: String? = null,
    val batchNumber: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "warehouse_movements")
data class WarehouseMovement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val movementType: String, // "Приход", "Передача в работу", "Брак", "Корректировка", "Инвентаризация"
    val qty: Int,
    val comment: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "defect_logs")
data class DefectLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val operationName: String,
    val qty: Int,
    val reason: String, // Predefined list + other
    val photoUri: String? = null,
    val status: String = "Зафиксирован", // "Зафиксирован", "В переработке", "Списан"
    val supplierName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tools")
data class Tool(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val article: String,
    val diameter: Float,
    val maxLifetimeMeters: Float,
    val metersUsed: Float = 0f,
    val status: String = "Новый" // "Новый", "В работе", "Изношен", "Списан", "В ремонте"
)

@Entity(tableName = "tool_histories")
data class ToolHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val toolId: Int,
    val metersUsed: Float,
    val operationName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val text: String,
    val productId: String? = null,
    val operationId: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "invites")
data class Invite(
    @PrimaryKey val code: String, // OP-START, TC-2026, etc.
    val role: String,
    val usedBy: String? = null, // userId of the user who used it
    val usedByName: String? = null,
    val timestamp: Long? = null
)
