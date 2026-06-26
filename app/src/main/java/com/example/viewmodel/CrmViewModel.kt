package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.CrmRepository
import com.example.data.util.HashUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CrmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CrmRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CrmRepository(database.crmDao())
        
        // Seed database on startup asynchronously
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    // --- Core UI State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow("login") // e.g., "login", "dashboard", "plan", "products", "work", "timecard", "warehouse", "defects", "tools", "chat", "reports", "admin"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // --- Active Work Flows ---
    val activeWork: StateFlow<ActiveWork?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getActiveWorkFlow(user.id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Data Flows mapped directly from Repository (Auto-updating Room DB) ---
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOperations: StateFlow<List<Operation>> = repository.allOperations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlanItems: StateFlow<List<PlanItem>> = repository.allPlanItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOperationLogs: StateFlow<List<OperationLog>> = repository.allOperationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimeEntries: StateFlow<List<TimeEntry>> = repository.allTimeEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWarehouseStocks: StateFlow<List<WarehouseStock>> = repository.allWarehouseStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWarehouseMovements: StateFlow<List<WarehouseMovement>> = repository.allWarehouseMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDefects: StateFlow<List<DefectLog>> = repository.allDefects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTools: StateFlow<List<Tool>> = repository.allTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<Message>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvites: StateFlow<List<Invite>> = repository.allInvites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Message Badge Counters ---
    val unreadMessageCount: StateFlow<Int> = repository.allMessages
        .map { it.size } // Simulated simple unread check for mock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Message Search & Mentions ---
    private val _messageSearchQuery = MutableStateFlow("")
    val messageSearchQuery: StateFlow<String> = _messageSearchQuery.asStateFlow()

    fun updateMessageSearchQuery(query: String) {
        _messageSearchQuery.value = query
    }

    val filteredMessages: StateFlow<List<Message>> = combine(allMessages, _messageSearchQuery) { msgs, q ->
        if (q.isBlank()) msgs
        else msgs.filter { it.text.contains(q, ignoreCase = true) || it.userName.contains(q, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Action Methods ---

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        clearMessages()
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // 1. Auth Module (Sections 3 & 4.1)
    fun login(userIdInput: String, passwordInput: String) {
        viewModelScope.launch {
            val user = repository.getUserById(userIdInput)
            if (user == null) {
                _errorMessage.value = "Пользователь '$userIdInput' не зарегистрирован."
                return@launch
            }
            if (user.isBlocked) {
                _errorMessage.value = "Пользователь '$userIdInput' заблокирован администратором."
                return@launch
            }
            if (!HashUtils.verifyPassword(passwordInput, user.passwordHash)) {
                _errorMessage.value = "Неверный пароль."
                return@launch
            }

            // Update user to active session
            val updatedUser = user.copy(sessionActive = true)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            _currentScreen.value = "dashboard"
            
            // Auto-log successful login
            repository.logAudit(user.id, user.name, "Вошел в систему (IP: 192.168.1.10).")
            _successMessage.value = "Добро пожаловать, ${user.name}!"
        }
    }

    fun logout() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                repository.updateUser(user.copy(sessionActive = false))
                repository.logAudit(user.id, user.name, "Вышел из системы.")
            }
            _currentUser.value = null
            _currentScreen.value = "login"
            clearMessages()
        }
    }

    fun changePasswordInSettings(oldPass: String, newPass: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (!HashUtils.verifyPassword(oldPass, user.passwordHash)) {
                _errorMessage.value = "Неверный текущий пароль."
                return@launch
            }
            if (newPass.length < 6 || !newPass.any { it.isDigit() }) {
                _errorMessage.value = "Новый пароль должен содержать не менее 6 символов и как минимум одну цифру."
                return@launch
            }

            val updatedUser = user.copy(passwordHash = HashUtils.sha256(newPass))
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            _successMessage.value = "Пароль успешно изменен!"
            repository.logAudit(user.id, user.name, "Сменил пароль в настройках.")
        }
    }

    fun registerWithInvite(code: String, desiredId: String, name: String, passwordInput: String) {
        viewModelScope.launch {
            val invite = repository.getInviteByCode(code)
            if (invite == null) {
                _errorMessage.value = "Неверный инвайт-код."
                return@launch
            }
            if (invite.usedBy != null) {
                _errorMessage.value = "Этот инвайт-код уже был использован пользователем ${invite.usedByName}."
                return@launch
            }

            val existing = repository.getUserById(desiredId)
            if (existing != null) {
                _errorMessage.value = "Логин '$desiredId' уже занят другим сотрудником."
                return@launch
            }

            if (passwordInput.length < 6 || !passwordInput.any { it.isDigit() }) {
                _errorMessage.value = "Пароль должен содержать минимум 6 символов и одну цифру."
                return@launch
            }

            // Create user
            val newUser = User(
                id = desiredId,
                name = name,
                role = invite.role,
                passwordHash = HashUtils.sha256(passwordInput)
            )
            repository.insertUser(newUser)

            // Mark invite used
            val updatedInvite = invite.copy(
                usedBy = desiredId,
                usedByName = name,
                timestamp = System.currentTimeMillis()
            )
            repository.updateInvite(updatedInvite)

            _successMessage.value = "Регистрация прошла успешно! Теперь вы можете войти."
            repository.logAudit(desiredId, name, "Зарегистрировался по инвайту '$code'. Роль: ${invite.role}.")
        }
    }

    // 2. Month Plan Module (Section 4.3)
    fun updatePlanQuantity(itemId: Int, newPlannedQty: Int) {
        viewModelScope.launch {
            val plan = repository.getPlanItemById(itemId) ?: return@launch
            val user = _currentUser.value ?: return@launch
            val oldQty = plan.plannedQty
            val updated = plan.copy(plannedQty = newPlannedQty)
            repository.updatePlanItem(updated)
            repository.insertPlanHistory(
                PlanHistory(
                    planItemId = itemId,
                    changerName = user.name,
                    action = "Изменил план с $oldQty шт. на $newPlannedQty шт."
                )
            )
            repository.logAudit(user.id, user.name, "Изменил объем плана по изделию '${plan.productId}' на $newPlannedQty шт.")
        }
    }

    fun changePlanPriority(itemId: Int, up: Boolean) {
        viewModelScope.launch {
            val planItems = repository.allPlanItems.first()
            val index = planItems.indexOfFirst { it.id == itemId }
            if (index == -1) return@launch
            
            val user = _currentUser.value ?: return@launch

            if (up && index > 0) {
                val previous = planItems[index - 1]
                val current = planItems[index]
                
                val updatedCurrent = current.copy(priority = previous.priority)
                val updatedPrevious = previous.copy(priority = current.priority)
                
                repository.updatePlanItem(updatedCurrent)
                repository.updatePlanItem(updatedPrevious)
                
                repository.insertPlanHistory(PlanHistory(planItemId = current.id, changerName = user.name, action = "Повысил приоритет"))
                repository.insertPlanHistory(PlanHistory(planItemId = previous.id, changerName = user.name, action = "Понизил приоритет"))
            } else if (!up && index < planItems.size - 1) {
                val next = planItems[index + 1]
                val current = planItems[index]

                val updatedCurrent = current.copy(priority = next.priority)
                val updatedNext = next.copy(priority = current.priority)

                repository.updatePlanItem(updatedCurrent)
                repository.updatePlanItem(updatedNext)

                repository.insertPlanHistory(PlanHistory(planItemId = current.id, changerName = user.name, action = "Понизил приоритет"))
                repository.insertPlanHistory(PlanHistory(planItemId = next.id, changerName = user.name, action = "Повысил приоритет"))
            }
        }
    }

    fun deletePlanItemWithLog(itemId: Int) {
        viewModelScope.launch {
            val plan = repository.getPlanItemById(itemId) ?: return@launch
            val user = _currentUser.value ?: return@launch
            repository.deletePlanItemById(itemId)
            repository.logAudit(user.id, user.name, "Удалил позицию '${plan.productId}' из плана производства.")
            _successMessage.value = "Пункт плана успешно удален."
        }
    }

    fun addPlanItem(productId: String, plannedQty: Int, deadline: Long?, monthYear: String) {
        viewModelScope.launch {
            val items = repository.allPlanItems.first()
            val nextPriority = (items.maxOfOrNull { it.priority } ?: 0) + 1
            val plan = PlanItem(
                productId = productId,
                plannedQty = plannedQty,
                priority = nextPriority,
                deadline = deadline,
                monthYear = monthYear
            )
            repository.insertPlanItem(plan)
            val user = _currentUser.value ?: return@launch
            repository.logAudit(user.id, user.name, "Добавил '$productId' в план месяца ($monthYear) объемом $plannedQty шт.")
            _successMessage.value = "Изделие успешно добавлено в план."
        }
    }

    // 3. Products & Route Management (Section 4.4)
    fun addProduct(id: String, name: String) {
        viewModelScope.launch {
            val product = Product(id = id, name = name)
            repository.insertProduct(product)
            // Auto add placeholder stock record
            repository.insertWarehouseStock(WarehouseStock(productId = id, minStock = 50))
            val user = _currentUser.value ?: return@launch
            repository.logAudit(user.id, user.name, "Создал новое изделие '$name' ($id).")
            _successMessage.value = "Изделие '$name' успешно добавлено."
        }
    }

    fun archiveProduct(id: String) {
        viewModelScope.launch {
            val product = repository.getProductById(id) ?: return@launch
            repository.updateProduct(product.copy(isArchived = true))
            val user = _currentUser.value ?: return@launch
            repository.logAudit(user.id, user.name, "Архивировал изделие '${product.name}' ($id).")
            _successMessage.value = "Изделие '${product.name}' архивировано."
        }
    }

    fun addOperationToProduct(productId: String, name: String, tempMeters: Float, requiredTool: Float?) {
        viewModelScope.launch {
            val operation = Operation(productId = productId, name = name, temporaryMeters = tempMeters, requiredToolDiameter = requiredTool)
            repository.insertOperation(operation)
            val user = _currentUser.value ?: return@launch
            repository.logAudit(user.id, user.name, "Добавил операцию '$name' к изделию '$productId'.")
            _successMessage.value = "Операция успешно добавлена."
        }
    }

    fun updateOperationMeters(operationId: Int, exactMeters: Float?, requiredTool: Float?) {
        viewModelScope.launch {
            val operations = repository.allOperations.first()
            val operation = operations.find { it.id == operationId } ?: return@launch
            val updated = operation.copy(exactMeters = exactMeters, requiredToolDiameter = requiredTool)
            repository.updateOperation(updated)
            val user = _currentUser.value ?: return@launch
            repository.logAudit(user.id, user.name, "Обновил параметры нормы расхода для операции '${operation.name}' (Расход: ${exactMeters ?: "замер отсутствует"} м/шт).")
            _successMessage.value = "Параметры операции успешно изменены."
        }
    }

    fun deleteOperation(operationId: Int) {
        viewModelScope.launch {
            repository.deleteOperationById(operationId)
            val user = _currentUser.value ?: return@launch
            repository.logAudit(user.id, user.name, "Удалил операцию ID $operationId.")
            _successMessage.value = "Операция удалена."
        }
    }

    // 4. Operator Working Screens (Section 4.5)
    fun startOperatorWork(productId: String, operationId: Int, qtyStarted: Int, machineId: String?) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val result = repository.startWork(user.id, user.name, productId, operationId, qtyStarted, machineId)
            result.onSuccess {
                _successMessage.value = "Смена запущена! Операция начата."
            }.onFailure {
                _errorMessage.value = it.message ?: "Не удалось начать операцию."
            }
        }
    }

    fun pauseOperatorWork(reason: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val result = repository.pauseWork(user.id, user.name, reason)
            result.onSuccess {
                _successMessage.value = "Работа приостановлена (Причина: $reason)."
            }.onFailure {
                _errorMessage.value = it.message ?: "Ошибка при паузе."
            }
        }
    }

    fun resumeOperatorWork() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val result = repository.resumeWork(user.id, user.name)
            result.onSuccess {
                _successMessage.value = "Работа возобновлена."
            }.onFailure {
                _errorMessage.value = it.message ?: "Ошибка возобновления."
            }
        }
    }

    fun cancelOperatorWork() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val result = repository.cancelWork(user.id, user.name)
            result.onSuccess {
                _successMessage.value = "Активная операция отменена. Ресурс заготовок возвращен на склад."
            }.onFailure {
                _errorMessage.value = it.message ?: "Ошибка отмены."
            }
        }
    }

    fun finishOperatorWork(qtyOk: Int, qtyDefect: Int, defectReason: String?, photoUri: String?) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val result = repository.finishWork(user.id, user.name, qtyOk, qtyDefect, defectReason, photoUri)
            result.onSuccess {
                _successMessage.value = "Операция успешно завершена! Данные сохранены в журнал."
            }.onFailure {
                _errorMessage.value = it.message ?: "Не удалось завершить операцию."
            }
        }
    }

    // SOS Emergency Broadcast Button (Section 4.2)
    fun sendSosAlert(messageText: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val sosMsg = Message(
                userId = user.id,
                userName = "🚨 SOS - ${user.name}",
                text = "🆘 ЭКСТРЕННОЕ СООБЩЕНИЕ: $messageText"
            )
            repository.insertMessage(sosMsg)
            repository.logAudit(user.id, user.name, "Отправил сигнал SOS: '$messageText'")
            _successMessage.value = "Сигнал SOS успешно разослан технологу и начальнику смены!"
        }
    }

    // 5. Time Sheets Module (Section 4.6)
    fun logTimeEntryManually(category: String, startOffsetHours: Float, endOffsetHours: Float) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val now = System.currentTimeMillis()
            val startTime = now - (startOffsetHours * 3600000).toLong()
            val endTime = now - (endOffsetHours * 3600000).toLong()
            if (startTime >= endTime) {
                _errorMessage.value = "Начало времени должно быть раньше окончания."
                return@launch
            }
            
            val result = repository.addTimeEntry(user.id, category, startTime, endTime)
            result.onSuccess {
                _successMessage.value = "Запись карты времени успешно добавлена."
                repository.logAudit(user.id, user.name, "Добавил запись времени '$category' вручную.")
            }.onFailure {
                _errorMessage.value = it.message ?: "Не удалось добавить запись времени."
            }
        }
    }

    // 6. Warehouse Module (Section 4.7)
    fun receiveStockBatch(productId: String, qty: Int, supplier: String?, batchNumber: String?) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.addWarehouseStock(productId, qty, supplier, batchNumber, user.id)
            repository.logAudit(user.id, user.name, "Оприходовал партию '$batchNumber' ($qty шт.) изделия '$productId' от '$supplier'.")
            _successMessage.value = "Заготовки оприходованы на склад."
        }
    }

    fun adjustWarehouseStockManually(productId: String, type: String, qty: Int, comment: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.adjustStock(productId, type, qty, comment, user.id)
            repository.logAudit(user.id, user.name, "Скорректировал склад по '$productId' (Операция: $type, Кол-во: $qty, Коммент: $comment).")
            _successMessage.value = "Баланс склада успешно скорректирован."
        }
    }

    // 7. Defects Status Adjust (Section 4.8)
    fun updateDefectStatus(defectId: Int, newStatus: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val defectLogs = repository.allDefects.first()
            val defect = defectLogs.find { it.id == defectId } ?: return@launch
            val updated = defect.copy(status = newStatus)
            repository.updateDefect(updated)
            repository.logAudit(user.id, user.name, "Изменил статус дефектной партии изделия '${defect.productId}' на '$newStatus'.")
            _successMessage.value = "Статус брака успешно обновлен на '$newStatus'."
        }
    }

    // 8. Tool wear replacement & procurement (Section 4.9)
    fun orderNewToolTask(toolId: Int) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val tool = repository.getToolById(toolId) ?: return@launch
            // Post procurement message
            repository.insertMessage(
                Message(
                    userId = "system",
                    userName = "Закупки",
                    text = "🛒 ЗАПРОС НА ЗАКУПКУ: Требуется новый инструмент взамен изношенного/поврежденного '${tool.name}' (Артикул: ${tool.article}, Ø${tool.diameter} мм). Запросил: ${user.name}.",
                    productId = null,
                    operationId = null
                )
            )
            repository.logAudit(user.id, user.name, "Отправил заявку на закупку инструмента '${tool.name}' (${tool.article}).")
            _successMessage.value = "Заявка на новый инструмент отправлена в службу снабжения."
        }
    }

    fun addNewTool(name: String, article: String, diameter: Float, maxLifetime: Float) {
        viewModelScope.launch {
            val tool = Tool(name = name, article = article, diameter = diameter, maxLifetimeMeters = maxLifetime, status = "Новый")
            repository.insertTool(tool)
            val user = _currentUser.value ?: return@launch
            repository.logAudit(user.id, user.name, "Зарегистрировал новый инструмент '$name' ($article).")
            _successMessage.value = "Инструмент успешно добавлен."
        }
    }

    fun updateToolStatus(toolId: Int, newStatus: String) {
        viewModelScope.launch {
            val tool = repository.getToolById(toolId) ?: return@launch
            val user = _currentUser.value ?: return@launch
            val updated = tool.copy(status = newStatus)
            repository.updateTool(updated)
            repository.logAudit(user.id, user.name, "Изменил статус инструмента '${tool.name}' на '$newStatus'.")
            _successMessage.value = "Статус инструмента изменен на '$newStatus'."
        }
    }

    // 9. Messages feed (Section 4.10)
    fun sendMessage(text: String, productId: String? = null, operationId: Int? = null) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val msg = Message(
                userId = user.id,
                userName = user.name,
                text = text,
                productId = productId,
                operationId = operationId
            )
            repository.insertMessage(msg)
        }
    }

    fun deleteOwnMessage(messageId: Int) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val allMsgs = repository.allMessages.first()
            val msg = allMsgs.find { it.id == messageId } ?: return@launch
            if (msg.userId == user.id || user.role == "admin") {
                repository.deleteMessageById(messageId)
                _successMessage.value = "Сообщение удалено."
            } else {
                _errorMessage.value = "Вы можете удалять только свои сообщения."
            }
        }
    }

    // 10. Admin Control (Section 4.12)
    fun toggleUserBlock(targetUserId: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (user.role != "admin") {
                _errorMessage.value = "Только администратор может блокировать пользователей."
                return@launch
            }
            if (targetUserId == "admin" || targetUserId == user.id) {
                _errorMessage.value = "Нельзя заблокировать себя или корневого администратора."
                return@launch
            }

            val target = repository.getUserById(targetUserId) ?: return@launch
            val isBlocking = !target.isBlocked
            val updated = target.copy(isBlocked = isBlocking, sessionActive = false) // Force close sessions
            repository.updateUser(updated)

            val actionName = if (isBlocking) "Заблокировал" else "Разблокировал"
            repository.logAudit(user.id, user.name, "$actionName пользователя '$targetUserId' (${target.name}).")
            _successMessage.value = "Пользователь успешно ${if (isBlocking) "заблокирован" else "разблокирован"}."
        }
    }

    fun generateInviteCode(code: String, role: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (user.role != "admin") return@launch

            val existing = repository.getInviteByCode(code)
            if (existing != null) {
                _errorMessage.value = "Код '$code' уже существует."
                return@launch
            }

            val invite = Invite(code = code, role = role)
            repository.insertInvite(invite)
            repository.logAudit(user.id, user.name, "Сгенерировал новый инвайт-код '$code' для роли '$role'.")
            _successMessage.value = "Инвайт-код '$code' успешно создан."
        }
    }
}
