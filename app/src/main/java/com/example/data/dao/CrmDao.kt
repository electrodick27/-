package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CrmDao {

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    // --- Products ---
    @Query("SELECT * FROM products WHERE isArchived = 0")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    // --- Operations ---
    @Query("SELECT * FROM operations")
    fun getAllOperations(): Flow<List<Operation>>

    @Query("SELECT * FROM operations WHERE productId = :productId")
    fun getOperationsForProduct(productId: String): Flow<List<Operation>>

    @Query("SELECT * FROM operations WHERE productId = :productId")
    suspend fun getOperationsForProductSync(productId: String): List<Operation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: Operation)

    @Update
    suspend fun updateOperation(operation: Operation)

    @Query("DELETE FROM operations WHERE id = :id")
    suspend fun deleteOperationById(id: Int)

    // --- Plan Items ---
    @Query("SELECT * FROM plan_items ORDER BY priority ASC")
    fun getAllPlanItems(): Flow<List<PlanItem>>

    @Query("SELECT * FROM plan_items WHERE id = :id LIMIT 1")
    suspend fun getPlanItemById(id: Int): PlanItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanItem(planItem: PlanItem)

    @Update
    suspend fun updatePlanItem(planItem: PlanItem)

    @Query("DELETE FROM plan_items WHERE id = :id")
    suspend fun deletePlanItemById(id: Int)

    // --- Plan History ---
    @Query("SELECT * FROM plan_histories WHERE planItemId = :planItemId ORDER BY timestamp DESC")
    fun getPlanHistory(planItemId: Int): Flow<List<PlanHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanHistory(history: PlanHistory)

    // --- Active Work ---
    @Query("SELECT * FROM active_works WHERE operatorId = :operatorId LIMIT 1")
    fun getActiveWork(operatorId: String): Flow<ActiveWork?>

    @Query("SELECT * FROM active_works WHERE operatorId = :operatorId LIMIT 1")
    suspend fun getActiveWorkSync(operatorId: String): ActiveWork?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveWork(activeWork: ActiveWork)

    @Query("DELETE FROM active_works WHERE operatorId = :operatorId")
    suspend fun deleteActiveWork(operatorId: String)

    // --- Operation Logs ---
    @Query("SELECT * FROM operation_logs ORDER BY timestamp DESC")
    fun getAllOperationLogs(): Flow<List<OperationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperationLog(log: OperationLog)

    // --- Time Entries ---
    @Query("SELECT * FROM time_entries ORDER BY startTime DESC")
    fun getAllTimeEntries(): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE userId = :userId ORDER BY startTime DESC")
    fun getTimeEntriesForUser(userId: String): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE userId = :userId AND endTime = 0 LIMIT 1")
    suspend fun getActiveTimeEntry(userId: String): TimeEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntry(timeEntry: TimeEntry)

    @Update
    suspend fun updateTimeEntry(timeEntry: TimeEntry)

    // --- Warehouse Stocks ---
    @Query("SELECT * FROM warehouse_stocks")
    fun getAllWarehouseStocks(): Flow<List<WarehouseStock>>

    @Query("SELECT * FROM warehouse_stocks WHERE productId = :productId LIMIT 1")
    suspend fun getStockForProduct(productId: String): WarehouseStock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouseStock(stock: WarehouseStock)

    @Update
    suspend fun updateWarehouseStock(stock: WarehouseStock)

    // --- Warehouse Movements ---
    @Query("SELECT * FROM warehouse_movements ORDER BY timestamp DESC")
    fun getAllWarehouseMovements(): Flow<List<WarehouseMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouseMovement(movement: WarehouseMovement)

    // --- Defect Logs ---
    @Query("SELECT * FROM defect_logs ORDER BY timestamp DESC")
    fun getAllDefects(): Flow<List<DefectLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefect(defectLog: DefectLog)

    @Update
    suspend fun updateDefect(defectLog: DefectLog)

    // --- Tools ---
    @Query("SELECT * FROM tools")
    fun getAllTools(): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE id = :id LIMIT 1")
    suspend fun getToolById(id: Int): Tool?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: Tool)

    @Update
    suspend fun updateTool(tool: Tool)

    @Query("DELETE FROM tools WHERE id = :id")
    suspend fun deleteToolById(id: Int)

    // --- Tool History ---
    @Query("SELECT * FROM tool_histories WHERE toolId = :toolId ORDER BY timestamp DESC")
    fun getToolHistory(toolId: Int): Flow<List<ToolHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToolHistory(history: ToolHistory)

    // --- Messages ---
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // --- Invites ---
    @Query("SELECT * FROM invites")
    fun getAllInvites(): Flow<List<Invite>>

    @Query("SELECT * FROM invites WHERE code = :code LIMIT 1")
    suspend fun getInviteByCode(code: String): Invite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvite(invite: Invite)

    @Update
    suspend fun updateInvite(invite: Invite)
}
