package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.entity.*
import com.example.ui.theme.*
import com.example.viewmodel.CrmViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// --- SHARED UI HELPERS ---

fun <T> mutableStateFlowOf(value: T): MutableState<T> = mutableStateOf(value)

@Composable
fun SectionHeader(title: String, icon: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = LosLimeAccent
        )
    }
}

fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return "Н/Д"
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDuration(ms: Long): String {
    val sec = ms / 1000
    val min = sec / 60
    val hrs = min / 60
    return if (hrs > 0) {
        "${hrs} ч ${min % 60} мин"
    } else {
        "${min} мин ${sec % 60} сек"
    }
}

// --- 1. LOGIN SCREEN ---

@Composable
fun LoginScreen(viewModel: CrmViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LosDarkBg)
    ) {
        // Decorative background glowing brush
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(LosSurfaceLighter.copy(alpha = 0.4f), Color.Transparent),
                        radius = 2000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo Image
            Image(
                painter = painterResource(id = R.drawable.elk_crm_logo),
                contentDescription = "ЛОСЬ CRM Logo",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, LosLimeAccent, RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ЛОСЬ CRM v2.0",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = LosLimeAccent
            )
            Text(
                text = "Производство корпусов светильников · ООО НПП «Лосев»",
                style = MaterialTheme.typography.bodyMedium,
                color = LosTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            AnimatedContent(targetState = isRegisterMode, label = "auth_flip") { isReg ->
                if (isReg) {
                    RegistrationCard(viewModel) { isRegisterMode = false }
                } else {
                    LoginCard(viewModel) { isRegisterMode = true }
                }
            }
        }
    }
}

@Composable
fun LoginCard(viewModel: CrmViewModel, onRegisterSelected: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        colors = CardDefaults.cardColors(containerColor = LosSurface),
        border = BorderStroke(1.dp, LosSurfaceLighter)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Авторизация",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = LosLimeAccent,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Логин") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LosLimeAccent) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LosLimeAccent,
                    unfocusedBorderColor = LosTextMuted,
                    focusedLabelColor = LosLimeAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LosLimeAccent) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Lock else Icons.Default.Check,
                            contentDescription = "Показать пароль",
                            tint = LosTextMuted
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LosLimeAccent,
                    unfocusedBorderColor = LosTextMuted,
                    focusedLabelColor = LosLimeAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(username.trim().lowercase(), password) },
                colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("login_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "ВОЙТИ В СИСТЕМУ",
                    fontWeight = FontWeight.Bold,
                    color = LosOliveText,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onRegisterSelected,
                modifier = Modifier.testTag("register_mode_button")
            ) {
                Text(
                    text = "Зарегистрироваться по инвайт-коду",
                    color = LosLimeAccent,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Quick logins helpful footer
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = LosSurfaceLighter)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "БЫСТРЫЙ ТЕСТОВЫЙ ВХОД (Пароль: 123 в конце):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = LosTextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Flow row for quick accounts
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "admin" to "Админ",
                    "operator" to "Оператор",
                    "tech" to "Технолог",
                    "manager" to "Начальник",
                    "director" to "Директор"
                ).forEach { (id, label) ->
                    AssistChip(
                        onClick = {
                            username = id
                            password = if (id == "tech") "tech123" else "${id}123"
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = LosDarkOlive,
                            labelColor = LosLimeAccent
                        ),
                        border = BorderStroke(1.dp, LosLimeAccent.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
fun RegistrationCard(viewModel: CrmViewModel, onLoginSelected: () -> Unit) {
    var inviteCode by remember { mutableStateOf("") }
    var loginId by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        colors = CardDefaults.cardColors(containerColor = LosSurface),
        border = BorderStroke(1.dp, LosSurfaceLighter)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Регистрация сотрудника",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = LosLimeAccent,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it },
                label = { Text("Инвайт-код (например, OP-START или TC-2026)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LosLimeAccent,
                    unfocusedBorderColor = LosTextMuted,
                    focusedLabelColor = LosLimeAccent
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = loginId,
                onValueChange = { loginId = it },
                label = { Text("Придумайте логин") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LosLimeAccent,
                    unfocusedBorderColor = LosTextMuted,
                    focusedLabelColor = LosLimeAccent
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("ФИО сотрудника") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LosLimeAccent,
                    unfocusedBorderColor = LosTextMuted,
                    focusedLabelColor = LosLimeAccent
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Придумайте пароль") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LosLimeAccent,
                    unfocusedBorderColor = LosTextMuted,
                    focusedLabelColor = LosLimeAccent
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.registerWithInvite(
                        code = inviteCode.trim().uppercase(),
                        desiredId = loginId.trim().lowercase(),
                        name = fullName.trim(),
                        passwordInput = password
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_registration"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "ЗАРЕГИСТРИРОВАТЬСЯ",
                    fontWeight = FontWeight.Bold,
                    color = LosOliveText,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onLoginSelected) {
                Text(
                    text = "Вернуться к экрану входа",
                    color = LosTextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// --- 2. MAIN DASHBOARD ---

@Composable
fun DashboardScreen(viewModel: CrmViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPlanItems by viewModel.allPlanItems.collectAsState()
    val allDefects by viewModel.allDefects.collectAsState()
    val allTools by viewModel.allTools.collectAsState()
    val activeWork by viewModel.activeWork.collectAsState()

    val totalPlanned = allPlanItems.sumOf { it.plannedQty }
    val totalDone = allPlanItems.sumOf { it.doneQty }
    val planPercent = if (totalPlanned > 0) (totalDone.toFloat() / totalPlanned * 100).roundToInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Brief
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LosSurface),
                border = BorderStroke(1.dp, LosSurfaceLighter),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LosLimeAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = LosOliveText,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Приветствуем, ${currentUser?.name ?: "Сотрудник"}!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = LosLimeAccent
                        )
                        Text(
                            text = "Ваша роль в системе: ${roleDescription(currentUser?.role ?: "")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LosTextMuted
                        )
                    }
                }
            }
        }

        // Section: KPIs Summary
        item {
            SectionHeader(title = "Сводные показатели месяца", icon = { Icon(Icons.Default.CheckCircle, null, tint = LosLimeAccent) })
        }

        item {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                maxItemsInEachRow = 4,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // KPI: Plan completion
                KpiCard(
                    title = "Выполнение плана",
                    value = "$planPercent%",
                    subText = "$totalDone из $totalPlanned шт.",
                    color = when {
                        planPercent > 80 -> LosSuccess
                        planPercent > 40 -> LosWarning
                        else -> LosError
                    },
                    modifier = Modifier.weight(1f)
                )

                // KPI: Total Defects
                val totalDefects = allDefects.sumOf { it.qty }
                KpiCard(
                    title = "Обнаружено брака",
                    value = "$totalDefects шт.",
                    subText = "зафиксированные случаи",
                    color = if (totalDefects > 15) LosError else LosSuccess,
                    modifier = Modifier.weight(1f)
                )

                // KPI: Running machine/job
                KpiCard(
                    title = "Текущая работа",
                    value = if (activeWork != null) "В процессе" else "Ожидание",
                    subText = if (activeWork != null) "Операция запущена" else "Станок простаивает",
                    color = if (activeWork != null) LosLimeAccent else LosTextMuted,
                    modifier = Modifier.weight(1f)
                )

                // KPI: Low tool wear list
                val lowToolsCount = allTools.count { (it.maxLifetimeMeters - it.metersUsed) / it.maxLifetimeMeters < 0.15f }
                KpiCard(
                    title = "Инструмент на исходе",
                    value = "$lowToolsCount шт.",
                    subText = "ресурс < 15%",
                    color = if (lowToolsCount > 0) LosWarning else LosSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section: Interactive Quick Actions for OPERATOR (SOS / Active timer)
        if (currentUser?.role == "operator") {
            item {
                SectionHeader(title = "Экстренная связь & SOS", icon = { Icon(Icons.Default.Warning, null, tint = LosError) })
                SosCard(viewModel)
            }
        }

        // Section: Tools close to replacement
        item {
            SectionHeader(title = "Топ-3 изнашиваемых инструментов", icon = { Icon(Icons.Default.Build, null, tint = LosLimeAccent) })
        }

        val nearWearTools = allTools
            .sortedBy { (it.maxLifetimeMeters - it.metersUsed) / it.maxLifetimeMeters }
            .take(3)

        if (nearWearTools.isEmpty()) {
            item {
                Text("Инструменты отсутствуют в базе данных.", color = LosTextMuted, modifier = Modifier.padding(start = 8.dp))
            }
        } else {
            items(nearWearTools) { tool ->
                val remainingRatio = (tool.maxLifetimeMeters - tool.metersUsed) / tool.maxLifetimeMeters
                val remPercent = (remainingRatio * 100).coerceAtLeast(0f).roundToInt()
                val progress = (tool.metersUsed / tool.maxLifetimeMeters).coerceIn(0f, 1f)

                Card(
                    colors = CardDefaults.cardColors(containerColor = LosSurface),
                    border = BorderStroke(1.dp, LosSurfaceLighter),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = tool.name, fontWeight = FontWeight.Bold, color = LosTextOnDark)
                            Text(
                                text = "Осталось $remPercent% ресурса",
                                color = if (remPercent < 15) LosError else LosSuccess,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            color = if (remPercent < 15) LosError else LosLimeAccent,
                            trackColor = LosDarkOlive,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Пробег: ${tool.metersUsed.roundToInt()} м из ${tool.maxLifetimeMeters.roundToInt()} м",
                            style = MaterialTheme.typography.bodySmall,
                            color = LosTextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(title: String, value: String, subText: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LosSurface),
        border = BorderStroke(1.dp, LosSurfaceLighter),
        modifier = modifier.heightIn(min = 120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = LosTextMuted)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(text = subText, style = MaterialTheme.typography.bodySmall, color = LosTextMuted)
        }
    }
}

@Composable
fun SosCard(viewModel: CrmViewModel) {
    var sosReason by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = LosSurface),
        border = BorderStroke(1.dp, LosError),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "КНОПКА «SOS» ЭКСТРЕННОЙ СВЯЗИ",
                fontWeight = FontWeight.Bold,
                color = LosError,
                fontSize = 15.sp
            )
            Text(
                text = "Отправляет немедленный экстренный сигнал с описанием проблемы технологу и начальнику смены.",
                style = MaterialTheme.typography.bodyMedium,
                color = LosTextOnDark,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LosError),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sos_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = LosTextOnDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ОТПРАВИТЬ СИГНАЛ SOS", fontWeight = FontWeight.Bold, color = LosTextOnDark)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = LosSurface,
            title = { Text("Экстренный сигнал SOS", color = LosError, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Опишите характер поломки, инцидента или брака на станочной линии:", color = LosTextOnDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sosReason,
                        onValueChange = { sosReason = it },
                        placeholder = { Text("Сломалась фреза 4мм / зажало заготовку") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LosError,
                            focusedLabelColor = LosError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sosReason.isNotBlank()) {
                            viewModel.sendSosAlert(sosReason)
                            sosReason = ""
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosError)
                ) {
                    Text("ОТПРАВИТЬ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }
}

fun roleDescription(role: String): String = when (role) {
    "admin" -> "Администратор (Полный доступ)"
    "operator" -> "Оператор станочной линии"
    "technologist" -> "Технолог ЧПУ"
    "manager" -> "Начальник производства"
    "director" -> "Генеральный директор"
    else -> "Сотрудник"
}

// --- 3. MONTHLY PLAN SCREEN ---

@Composable
fun PlanScreen(viewModel: CrmViewModel) {
    val allPlanItems by viewModel.allPlanItems.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf("") }
    var inputPlannedQty by remember { mutableStateOf("") }
    var deleteConfirmItemId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SectionHeader(
                title = "План производства · Июнь 2026",
                icon = { Icon(Icons.Default.List, null, tint = LosLimeAccent) },
                modifier = Modifier.weight(1f)
            )

            // Managers and Admins can add items to plan
            if (currentUser?.role in listOf("admin", "manager", "director")) {
                Button(
                    onClick = {
                        if (allProducts.isNotEmpty()) {
                            selectedProductId = allProducts.first().id
                        }
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                    modifier = Modifier.testTag("add_plan_item_button")
                ) {
                    Icon(Icons.Default.Add, null, tint = LosOliveText)
                    Text("Создать пункт", color = LosOliveText, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (allPlanItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("План на данный месяц пуст.", color = LosTextMuted)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allPlanItems) { item ->
                    val prodName = allProducts.find { it.id == item.productId }?.name ?: item.productId
                    val completionPercent = if (item.plannedQty > 0) (item.doneQty.toFloat() / item.plannedQty * 100).roundToInt() else 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = LosSurface),
                        border = BorderStroke(1.dp, LosSurfaceLighter),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "$prodName (Приоритет #${item.priority})",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = LosLimeAccent
                                    )
                                    Text(
                                        text = "Выполнено: ${item.doneQty} шт / План: ${item.plannedQty} шт",
                                        color = LosTextOnDark,
                                        fontSize = 14.sp
                                    )
                                }

                                // Quick sorting & editing for managers
                                if (currentUser?.role in listOf("admin", "manager")) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { viewModel.changePlanPriority(item.id, up = true) }) {
                                            Icon(Icons.Default.KeyboardArrowUp, "Вверх", tint = LosLimeAccent)
                                        }
                                        IconButton(onClick = { viewModel.changePlanPriority(item.id, up = false) }) {
                                            Icon(Icons.Default.KeyboardArrowDown, "Вниз", tint = LosLimeAccent)
                                        }
                                        IconButton(onClick = { deleteConfirmItemId = item.id }) {
                                            Icon(Icons.Default.Delete, "Удалить", tint = LosError)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom colored progress bar
                            LinearProgressIndicator(
                                progress = (item.doneQty.toFloat() / item.plannedQty).coerceIn(0f, 1f),
                                color = when {
                                    completionPercent > 80 -> LosSuccess
                                    completionPercent > 40 -> LosWarning
                                    else -> LosError
                                },
                                trackColor = LosDarkOlive,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Сдано: $completionPercent%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = LosTextMuted
                                )

                                val left = item.plannedQty - item.doneQty
                                Text(
                                    text = if (left <= 0) "Выполнено!" else "Осталось сделать: $left шт.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (left <= 0) LosSuccess else LosLimeAccent
                                )
                            }

                            // Inline editing planned count for managers
                            if (currentUser?.role in listOf("admin", "manager")) {
                                var editQtyValue by remember { mutableStateOf(item.plannedQty.toString()) }
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = LosSurfaceLighter)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = editQtyValue,
                                        onValueChange = { editQtyValue = it },
                                        label = { Text("Изм. кол-во", fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LosLimeAccent,
                                            focusedLabelColor = LosLimeAccent
                                        ),
                                        modifier = Modifier.width(110.dp),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val parsed = editQtyValue.toIntOrNull()
                                            if (parsed != null && parsed > 0) {
                                                viewModel.updatePlanQuantity(item.id, parsed)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LosDarkOlive)
                                    ) {
                                        Text("Сохранить", color = LosLimeAccent, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal adding plan item dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = LosSurface,
            title = { Text("Добавить в план", color = LosLimeAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Выберите изделие из списка:")
                    
                    // Simple select product row/chips
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allProducts.forEach { p ->
                            FilterChip(
                                selected = selectedProductId == p.id,
                                onClick = { selectedProductId = p.id },
                                label = { Text(p.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LosLimeAccent,
                                    selectedLabelColor = LosOliveText
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputPlannedQty,
                        onValueChange = { inputPlannedQty = it },
                        label = { Text("Планируемый объем (шт.)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LosLimeAccent,
                            focusedLabelColor = LosLimeAccent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = inputPlannedQty.toIntOrNull()
                        if (selectedProductId.isNotBlank() && qty != null && qty > 0) {
                            viewModel.addPlanItem(
                                productId = selectedProductId,
                                plannedQty = qty,
                                deadline = System.currentTimeMillis() + 86400000 * 5, // 5 days from now
                                monthYear = "06.2026"
                            )
                            inputPlannedQty = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent)
                ) {
                    Text("ДОБАВИТЬ", color = LosOliveText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }

    // Safe deletion confirmation dialog
    if (deleteConfirmItemId != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmItemId = null },
            containerColor = LosSurface,
            title = { Text("Подтвердите удаление", color = LosError, fontWeight = FontWeight.Bold) },
            text = { Text("Вы действительно хотите удалить этот пункт из производственного плана?") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteConfirmItemId?.let { viewModel.deletePlanItemWithLog(it) }
                        deleteConfirmItemId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosError)
                ) {
                    Text("УДАЛИТЬ")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmItemId = null }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }
}

// --- 4. PRODUCTS & OPERATIONS (КОРПУСА) ---

@Composable
fun ProductsScreen(viewModel: CrmViewModel) {
    val allProducts by viewModel.allProducts.collectAsState()
    val allOperations by viewModel.allOperations.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddOperationDialog by remember { mutableStateOf(false) }

    var newProdId by remember { mutableStateOf("") }
    var newProdName by remember { mutableStateOf("") }

    var targetProductIdForOp by remember { mutableStateOf("") }
    var newOpName by remember { mutableStateOf("") }
    var newOpMeters by remember { mutableStateOf("") }
    var newOpToolSize by remember { mutableStateOf("") }

    var productArchiveConfirm by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SectionHeader(
                title = "Корпуса & Технологические карты",
                icon = { Icon(Icons.Default.Build, null, tint = LosLimeAccent) },
                modifier = Modifier.weight(1f)
            )

            if (currentUser?.role in listOf("admin", "technologist")) {
                Button(
                    onClick = { showAddProductDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent)
                ) {
                    Icon(Icons.Default.Add, null, tint = LosOliveText)
                    Text("Добавить корпус", color = LosOliveText, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allProducts) { product ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = LosSurface),
                    border = BorderStroke(1.dp, LosSurfaceLighter),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LosLimeAccent
                                )
                                Text(text = "Артикул / ID: ${product.id}", style = MaterialTheme.typography.bodySmall, color = LosTextMuted)
                            }

                            if (currentUser?.role in listOf("admin", "technologist")) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = {
                                        targetProductIdForOp = product.id
                                        showAddOperationDialog = true
                                    }) {
                                        Icon(Icons.Default.Add, "Добавить операцию", tint = LosLimeAccent)
                                    }
                                    IconButton(onClick = { productArchiveConfirm = product.id }) {
                                        Icon(Icons.Default.Delete, "Архивировать", tint = LosError)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = LosSurfaceLighter)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Список операций маршрутной карты:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = LosTextMuted
                        )

                        val prodOps = allOperations.filter { it.productId == product.id }

                        if (prodOps.isEmpty()) {
                            Text(
                                text = "Технологические операции не настроены. Добавьте первую операцию.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LosTextMuted,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            prodOps.forEach { op ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "· ${op.name}", style = MaterialTheme.typography.bodyMedium, color = LosTextOnDark)
                                        Text(
                                            text = "Норма расхода: Временная: ${op.temporaryMeters} м/шт | Установленная: ${op.exactMeters ?: "НЕТ ЗАМЕРА"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (op.exactMeters != null) LosSuccess else LosWarning
                                        )
                                        if (op.requiredToolDiameter != null) {
                                            Text(
                                                text = "Требуется фреза/сверло: Ø${op.requiredToolDiameter} мм",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LosTextMuted
                                            )
                                        }
                                    }

                                    // Technologist or admin can update exact meters easily
                                    if (currentUser?.role in listOf("admin", "technologist")) {
                                        var editMetersValue by remember { mutableStateOf(op.exactMeters?.toString() ?: "") }
                                        var editToolValue by remember { mutableStateOf(op.requiredToolDiameter?.toString() ?: "") }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editMetersValue,
                                                onValueChange = { editMetersValue = it },
                                                label = { Text("Замер (м)", fontSize = 9.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = LosLimeAccent,
                                                    focusedLabelColor = LosLimeAccent
                                                ),
                                                modifier = Modifier.width(75.dp),
                                                singleLine = true
                                            )
                                            IconButton(
                                                onClick = {
                                                    val meters = editMetersValue.toFloatOrNull()
                                                    val tool = editToolValue.toFloatOrNull() ?: op.requiredToolDiameter
                                                    viewModel.updateOperationMeters(op.id, meters, tool)
                                                }
                                            ) {
                                                Icon(Icons.Default.Check, "Сохранить", tint = LosLimeAccent)
                                            }
                                            IconButton(onClick = { viewModel.deleteOperation(op.id) }) {
                                                Icon(Icons.Default.Close, "Удалить", tint = LosError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Add Product
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            containerColor = LosSurface,
            title = { Text("Новый корпус ЧПУ", color = LosLimeAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newProdId,
                        onValueChange = { newProdId = it },
                        label = { Text("ID изделия (например: p3-premium)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        label = { Text("Название изделия") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProdId.isNotBlank() && newProdName.isNotBlank()) {
                            viewModel.addProduct(newProdId.trim().lowercase(), newProdName.trim())
                            newProdId = ""
                            newProdName = ""
                            showAddProductDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent)
                ) {
                    Text("СОЗДАТЬ", color = LosOliveText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }

    // Modal Dialog: Add Operation
    if (showAddOperationDialog) {
        AlertDialog(
            onDismissRequest = { showAddOperationDialog = false },
            containerColor = LosSurface,
            title = { Text("Добавить операцию ЧПУ", color = LosLimeAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newOpName,
                        onValueChange = { newOpName = it },
                        label = { Text("Название операции (напр., Сверловка Ø2.7)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newOpMeters,
                        onValueChange = { newOpMeters = it },
                        label = { Text("Временная норма расхода (метров/шт)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newOpToolSize,
                        onValueChange = { newOpToolSize = it },
                        label = { Text("Диаметр фрезы/сверла в мм (опционально)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val meters = newOpMeters.toFloatOrNull() ?: 0.20f
                        val tool = newOpToolSize.toFloatOrNull()
                        if (newOpName.isNotBlank()) {
                            viewModel.addOperationToProduct(
                                productId = targetProductIdForOp,
                                name = newOpName,
                                tempMeters = meters,
                                requiredTool = tool
                            )
                            newOpName = ""
                            newOpMeters = ""
                            newOpToolSize = ""
                            showAddOperationDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent)
                ) {
                    Text("ДОБАВИТЬ", color = LosOliveText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOperationDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }

    // Modal Safe Archive Confirm
    if (productArchiveConfirm != null) {
        AlertDialog(
            onDismissRequest = { productArchiveConfirm = null },
            containerColor = LosSurface,
            title = { Text("Архивировать изделие?", color = LosError, fontWeight = FontWeight.Bold) },
            text = { Text("Изделие будет удалено из основного списка и архивировано. Все старые логи сохранятся (мягкое удаление).") },
            confirmButton = {
                Button(
                    onClick = {
                        productArchiveConfirm?.let { viewModel.archiveProduct(it) }
                        productArchiveConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosError)
                ) {
                    Text("АРХИВИРОВАТЬ")
                }
            },
            dismissButton = {
                TextButton(onClick = { productArchiveConfirm = null }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }
}

// --- 5. OPERATOR ACTIVE WORKSCREEN (РАБОЧИЙ ЭКРАН) ---

@Composable
fun WorkScreen(viewModel: CrmViewModel) {
    val activeWork by viewModel.activeWork.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allOperations by viewModel.allOperations.collectAsState()
    val allWarehouseStocks by viewModel.allWarehouseStocks.collectAsState()

    var showStartDialog by remember { mutableStateOf(false) }
    var selectedProdId by remember { mutableStateOf("") }
    var selectedOpId by remember { mutableStateOf(-1) }
    var inputQty by remember { mutableStateOf("") }
    var machineIdValue by remember { mutableStateOf("") }

    var showPauseDialog by remember { mutableStateOf(false) }
    var pauseReasonText by remember { mutableStateOf("") }

    var showFinishDialog by remember { mutableStateOf(false) }
    var qtyOkText by remember { mutableStateOf("") }
    var qtyScrapText by remember { mutableStateOf("") }
    var scrapReasonText by remember { mutableStateOf("") }

    var cancelConfirm by remember { mutableStateOf(false) }

    // Live clock timer update when operation is active
    var elapsedSeconds by remember { mutableStateOf(0L) }
    LaunchedEffect(key1 = activeWork) {
        if (activeWork != null && !activeWork!!.isPaused) {
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - activeWork!!.startTime) / 1000
                kotlinx.coroutines.delay(1000)
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Панель выполнения работ",
            icon = { Icon(Icons.Default.PlayArrow, null, tint = LosLimeAccent) }
        )

        val work = activeWork

        if (work == null) {
            // Screen state: NO ACTIVE WORK. Render starting card.
            Card(
                colors = CardDefaults.cardColors(containerColor = LosSurface),
                border = BorderStroke(1.dp, LosSurfaceLighter),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = LosLimeAccent,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "СТАНОК ГОТОВ К РАБОТЕ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LosLimeAccent
                    )
                    Text(
                        text = "Выберите изделие, технологическую операцию и укажите количество заготовок для запуска процесса обработки.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LosTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (allProducts.isNotEmpty()) {
                                selectedProdId = allProducts.first().id
                                val initialOps = allOperations.filter { it.productId == selectedProdId }
                                if (initialOps.isNotEmpty()) {
                                    selectedOpId = initialOps.first().id
                                }
                            }
                            showStartDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_operation_nav_button")
                    ) {
                        Text("ЗАПУСТИТЬ ОПЕРАЦИЮ", fontWeight = FontWeight.Bold, color = LosOliveText)
                    }
                }
            }
        } else {
            // Screen state: ACTIVE JOB RUNNING
            val product = allProducts.find { it.id == work.productId }
            val operation = allOperations.find { it.id == work.operationId }

            Card(
                colors = CardDefaults.cardColors(containerColor = if (work.isPaused) LosDarkOlive else LosSurface),
                border = BorderStroke(2.dp, if (work.isPaused) LosWarning else LosLimeAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "АКТИВНАЯ ОПЕРАЦИЯ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (work.isPaused) LosWarning else LosLimeAccent
                            )
                            Text(
                                text = product?.name ?: work.productId,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = LosTextOnDark
                            )
                        }

                        // Digital timer display
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LosDarkOlive),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = if (work.isPaused) "НА ПАУЗЕ" else String.format("%02d:%02d:%02d", elapsedSeconds / 3600, (elapsedSeconds % 3600) / 60, elapsedSeconds % 60),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                color = if (work.isPaused) LosWarning else LosLimeAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Выполняется переход: ${operation?.name ?: "Неизвестно"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Взято заготовок: ${work.qtyStarted} шт. · Станок ID: ${work.machineId ?: "№1"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LosTextMuted
                    )

                    if (work.isPaused) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LosWarning.copy(alpha = 0.15f))
                                .border(1.dp, LosWarning, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Пауза по причине: ${work.pauseReason ?: "не указана"}",
                                color = LosWarning,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = LosSurfaceLighter)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons workflow: Pause/Resume, Cancel, Finish
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (work.isPaused) {
                            Button(
                                onClick = { viewModel.resumeOperatorWork() },
                                colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, tint = LosOliveText)
                                Text("ПРОДОЛЖИТЬ", color = LosOliveText, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { showPauseDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LosWarning),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Build, null, tint = LosOliveText)
                                Text("ПАУЗА", color = LosOliveText, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { cancelConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LosDarkOlive),
                            border = BorderStroke(1.dp, LosError),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, null, tint = LosError)
                            Text("ОТМЕНА", color = LosError, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            qtyOkText = work.qtyStarted.toString()
                            qtyScrapText = "0"
                            scrapReasonText = ""
                            showFinishDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LosSuccess),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("finish_operation_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = LosTextOnDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("СДАТЬ ДЕТАЛИ (ЗАВЕРШИТЬ)", fontWeight = FontWeight.Bold, color = LosTextOnDark)
                    }
                }
            }
        }
    }

    // Modal Dialog: START WORK
    if (showStartDialog) {
        AlertDialog(
            onDismissRequest = { showStartDialog = false },
            containerColor = LosSurface,
            title = { Text("Запуск новой работы", color = LosLimeAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Выберите обрабатываемый корпус:")
                    
                    // Simple product selector
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allProducts.forEach { p ->
                            FilterChip(
                                selected = selectedProdId == p.id,
                                onClick = {
                                    selectedProdId = p.id
                                    val ops = allOperations.filter { it.productId == p.id }
                                    if (ops.isNotEmpty()) {
                                        selectedOpId = ops.first().id
                                    }
                                },
                                label = { Text(p.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Выберите технологический переход:")

                    val productOps = allOperations.filter { it.productId == selectedProdId }
                    if (productOps.isEmpty()) {
                        Text("У данного изделия отсутствуют операции. Настройте карту технолога.", color = LosError, style = MaterialTheme.typography.bodySmall)
                    } else {
                        // Chips for operations
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            productOps.forEach { op ->
                                FilterChip(
                                    selected = selectedOpId == op.id,
                                    onClick = { selectedOpId = op.id },
                                    label = { Text(op.name) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputQty,
                        onValueChange = { inputQty = it },
                        label = { Text("Взято заготовок из склада (шт.)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = machineIdValue,
                        onValueChange = { machineIdValue = it },
                        label = { Text("Номер или ID станка (напр. ЧПУ №1)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = inputQty.toIntOrNull()
                        if (selectedProdId.isNotBlank() && selectedOpId != -1 && qty != null && qty > 0) {
                            viewModel.startOperatorWork(
                                productId = selectedProdId,
                                operationId = selectedOpId,
                                qtyStarted = qty,
                                machineId = if (machineIdValue.isBlank()) "ЧПУ №1" else machineIdValue
                            )
                            inputQty = ""
                            machineIdValue = ""
                            showStartDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                    enabled = selectedOpId != -1
                ) {
                    Text("ПУСК СТАНКА", color = LosOliveText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }

    // Modal Dialog: PAUSE WITH REASONS SELECTOR
    if (showPauseDialog) {
        val presetReasons = listOf("Перерыв", "Переналадка станка", "Поломка фрезы", "Смена задачи", "Профилактика (ППР)")
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            containerColor = LosSurface,
            title = { Text("Приостановить операцию", color = LosWarning, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Укажите официальную причину паузы (для карты времени):")
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        presetReasons.forEach { r ->
                            ElevatedAssistChip(
                                onClick = { pauseReasonText = r },
                                label = { Text(r) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = pauseReasonText,
                        onValueChange = { pauseReasonText = it },
                        label = { Text("Собственная причина") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosWarning),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pauseReasonText.isNotBlank()) {
                            viewModel.pauseOperatorWork(pauseReasonText.trim())
                            pauseReasonText = ""
                            showPauseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosWarning)
                ) {
                    Text("ПРИОСТАНОВИТЬ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPauseDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }

    // Modal Dialog: CANCEL WITH SAFE CONFIRMATION
    if (cancelConfirm) {
        AlertDialog(
            onDismissRequest = { cancelConfirm = false },
            containerColor = LosSurface,
            title = { Text("Отменить начатую работу?", color = LosError, fontWeight = FontWeight.Bold) },
            text = { Text("Внимание! При отмене заготовки в исходном количестве вернутся на склад. Текущее время операции не будет учтено.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelOperatorWork()
                        cancelConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosError)
                ) {
                    Text("ПОДТВЕРЖДАЮ ОТМЕНУ")
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelConfirm = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }

    // Modal Dialog: FINISH (Сдать детали) WITH DEFECTS AND GRAPHICAL CANVAS DRAFT REPRESENTATION
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = LosSurface,
            title = { Text("Завершение операции ЧПУ", color = LosSuccess, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Укажите фактические итоги обработки для отправки в базу:")
                    
                    OutlinedTextField(
                        value = qtyOkText,
                        onValueChange = { qtyOkText = it },
                        label = { Text("Годных деталей сдать (шт.)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosSuccess),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = qtyScrapText,
                        onValueChange = { qtyScrapText = it },
                        label = { Text("Брак обнаруженный (шт.)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosError),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val scrapCount = qtyScrapText.toIntOrNull() ?: 0
                    if (scrapCount > 0) {
                        Text("Укажите классифицированную причину брака (Обязательно):", color = LosError, fontWeight = FontWeight.Bold)
                        val scrapPreset = listOf("Царапина/Скол", "Трещина металла", "Неверный диаметр", "Сбой станка", "Брак сырья")
                        
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            scrapPreset.forEach { p ->
                                FilterChip(
                                    selected = scrapReasonText == p,
                                    onClick = { scrapReasonText = p },
                                    label = { Text(p) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = scrapReasonText,
                            onValueChange = { scrapReasonText = it },
                            label = { Text("Другая причина брака") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosError),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // SIMULATED SCRAP ATTACHMENT CANVAS (Section 4.5 & 4.8)
                        Text("Отрисуйте схематичный дефект пальцем на сенсоре (Приложение фото):", style = MaterialTheme.typography.labelSmall)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LosDarkOlive)
                                .border(1.dp, LosLimeAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Simple interactive drawing simulation or placeholder
                            var linesCount by remember { mutableStateOf(0) }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { linesCount++ }
                            ) {
                                if (linesCount == 0) {
                                    Text("Нажмите сюда пальцем, чтобы отрисовать эскиз дефекта", color = LosTextMuted, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                                } else {
                                    Text("📸 ЭСКИЗ ДЕФЕКТА ЗАГРУЖЕН (${linesCount} штрихов)", color = LosSuccess, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = qtyOkText.toIntOrNull() ?: 0
                        val scrap = qtyScrapText.toIntOrNull() ?: 0
                        if (ok >= 0 && scrap >= 0 && (ok + scrap) > 0) {
                            viewModel.finishOperatorWork(
                                qtyOk = ok,
                                qtyDefect = scrap,
                                defectReason = if (scrap > 0) scrapReasonText.ifBlank { "Причина не уточнена" } else null,
                                photoUri = if (scrap > 0) "mock_photo_defect.jpg" else null
                            )
                            qtyOkText = ""
                            qtyScrapText = ""
                            scrapReasonText = ""
                            showFinishDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosSuccess)
                ) {
                    Text("ПОДТВЕРДИТЬ СДАЧУ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }
}

// --- 6. TIME SHEET CARD (КАРТА ВРЕМЕНИ) ---

@Composable
fun TimeCardScreen(viewModel: CrmViewModel) {
    val allTimeEntries by viewModel.allTimeEntries.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedCategory by remember { mutableStateOf("Работа") }
    var startHoursAgo by remember { mutableStateOf("2") }
    var endHoursAgo by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Карта рабочего времени смены",
            icon = { Icon(Icons.Default.Build, null, tint = LosLimeAccent) }
        )

        // Prevent overlaps card manually logging
        Card(
            colors = CardDefaults.cardColors(containerColor = LosSurface),
            border = BorderStroke(1.dp, LosSurfaceLighter),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "РУЧНОЕ ДОБАВЛЕНИЕ ПЕРИОДА (БЕЗ ПЕРЕКРЫТИЙ)",
                    fontWeight = FontWeight.Bold,
                    color = LosLimeAccent,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val cats = listOf("Работа", "Перемещение", "Настройка", "ППР", "Ремонт", "Простой")
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cats.forEach { c ->
                        FilterChip(
                            selected = selectedCategory == c,
                            onClick = { selectedCategory = c },
                            label = { Text(c) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startHoursAgo,
                        onValueChange = { startHoursAgo = it },
                        label = { Text("Начало (ч назад)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endHoursAgo,
                        onValueChange = { endHoursAgo = it },
                        label = { Text("Конец (ч назад)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val st = startHoursAgo.toFloatOrNull()
                        val en = endHoursAgo.toFloatOrNull()
                        if (st != null && en != null && st > en) {
                            viewModel.logTimeEntryManually(selectedCategory, st, en)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ВНЕСТИ ЗАПИСЬ В ТАБЕЛЬ", color = LosOliveText, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Horizontal hourly timeline visualization
        SectionHeader(
            title = "Визуальный таймлайн смены",
            icon = { Icon(Icons.Default.Menu, null, tint = LosLimeAccent) }
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = LosSurface),
            border = BorderStroke(1.dp, LosSurfaceLighter),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Интервалы категорий:", style = MaterialTheme.typography.bodySmall, color = LosTextMuted)
                Spacer(modifier = Modifier.height(8.dp))

                // Custom timeline row simulation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LosDarkOlive)
                ) {
                    // Render segmented parts representing logged time
                    val userEntries = allTimeEntries.filter { it.userId == (currentUser?.id ?: "") }
                    if (userEntries.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Нет активных записей за сегодня", fontSize = 11.sp, color = LosTextMuted)
                        }
                    } else {
                        // Show colors representing categories
                        userEntries.take(5).forEach { entry ->
                            val color = when (entry.category) {
                                "Работа" -> LosSuccess
                                "Настройка" -> LosWarning
                                "Ремонт" -> LosError
                                else -> LosTextMuted
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(color)
                                    .border(0.5.dp, LosDarkBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = entry.category.take(3),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LosDarkBg,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // flat list grouped
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val myEntries = allTimeEntries.filter { it.userId == (currentUser?.id ?: "") }
            if (myEntries.isEmpty()) {
                item {
                    Text("Записи за сегодня отсутствуют в табеле.", color = LosTextMuted)
                }
            } else {
                items(myEntries) { entry ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LosSurfaceLighter),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            when (entry.category) {
                                                "Работа" -> LosSuccess
                                                "Настройка" -> LosWarning
                                                "Ремонт" -> LosError
                                                else -> LosTextMuted
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = entry.category, fontWeight = FontWeight.Bold, color = LosTextOnDark)
                                    Text(
                                        text = "Начало: ${formatTime(entry.startTime)} · Конец: ${if (entry.endTime == 0L) "В работе" else formatTime(entry.endTime)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LosTextMuted
                                    )
                                }
                            }
                            if (entry.endTime > 0L) {
                                Text(
                                    text = formatDuration(entry.endTime - entry.startTime),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = LosLimeAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 7. WAREHOUSE STOCKS SCREEN (СКЛАД) ---

@Composable
fun WarehouseScreen(viewModel: CrmViewModel) {
    val allWarehouseStocks by viewModel.allWarehouseStocks.collectAsState()
    val allWarehouseMovements by viewModel.allWarehouseMovements.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showBatchDialog by remember { mutableStateOf(false) }
    var selectedProdId by remember { mutableStateOf("") }
    var batchQty by remember { mutableStateOf("") }
    var supplierText by remember { mutableStateOf("") }
    var batchNoText by remember { mutableStateOf("") }

    var showAdjustDialog by remember { mutableStateOf(false) }
    var adjustType by remember { mutableStateOf("Корректировка") }
    var adjustQty by remember { mutableStateOf("") }
    var adjustComment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SectionHeader(
                title = "Склад материалов и остатков",
                icon = { Icon(Icons.Default.List, null, tint = LosLimeAccent) },
                modifier = Modifier.weight(1f)
            )

            if (currentUser?.role in listOf("admin", "manager")) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (allProducts.isNotEmpty()) selectedProdId = allProducts.first().id
                            showBatchDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent)
                    ) {
                        Text("Приход", color = LosOliveText, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (allProducts.isNotEmpty()) selectedProdId = allProducts.first().id
                            showAdjustDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LosDarkOlive),
                        border = BorderStroke(1.dp, LosLimeAccent)
                    ) {
                        Text("Инвентаризация", color = LosLimeAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stock balances listing table
        Card(
            colors = CardDefaults.cardColors(containerColor = LosSurface),
            border = BorderStroke(1.dp, LosSurfaceLighter),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ТЕКУЩИЕ ОСТАТКИ НА СКЛАДЕ",
                    fontWeight = FontWeight.Bold,
                    color = LosLimeAccent,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                allWarehouseStocks.forEach { stock ->
                    val prodName = allProducts.find { it.id == stock.productId }?.name ?: stock.productId
                    val isLow = stock.stock <= stock.minStock

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column {
                            Text(text = prodName, fontWeight = FontWeight.Bold, color = LosTextOnDark)
                            Text(
                                text = "Партия: ${stock.batchNumber ?: "N/A"} · Поставщик: ${stock.supplierName ?: "N/A"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LosTextMuted
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${stock.stock} шт.",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isLow) LosError else LosLimeAccent,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Минимум: ${stock.minStock} шт.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LosTextMuted
                                )
                            }

                            if (isLow) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Мало сырья",
                                    tint = LosError,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Divider(color = LosSurfaceLighter)
                }
            }
        }

        // Ledgers Movements history
        SectionHeader(
            title = "Журнал складских движений",
            icon = { Icon(Icons.Default.List, null, tint = LosLimeAccent) }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (allWarehouseMovements.isEmpty()) {
                item {
                    Text("История движений склада пуста.", color = LosTextMuted, modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                items(allWarehouseMovements) { mov ->
                    val pName = allProducts.find { it.id == mov.productId }?.name ?: mov.productId
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LosSurfaceLighter),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${mov.movementType}: $pName",
                                    fontWeight = FontWeight.Bold,
                                    color = when (mov.movementType) {
                                        "Приход" -> LosSuccess
                                        "Передача в работу" -> LosLimeAccent
                                        "Брак" -> LosError
                                        else -> LosWarning
                                    }
                                )
                                Text(
                                    text = "${if (mov.qty > 0 && mov.movementType == "Приход") "+" else ""}${mov.qty} шт.",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LosTextOnDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = mov.comment, style = MaterialTheme.typography.bodySmall, color = LosTextOnDark)
                            Text(
                                text = "Исполнитель: ${mov.userId} · ${formatTime(mov.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LosTextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue: Batch arrival
    if (showBatchDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            containerColor = LosSurface,
            title = { Text("Оприходование партии сырья", color = LosLimeAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Выберите тип заготовки:")
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allProducts.forEach { p ->
                            FilterChip(
                                selected = selectedProdId == p.id,
                                onClick = { selectedProdId = p.id },
                                label = { Text(p.name) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = batchQty,
                        onValueChange = { batchQty = it },
                        label = { Text("Количество заготовок (шт.)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supplierText,
                        onValueChange = { supplierText = it },
                        label = { Text("Поставщик") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = batchNoText,
                        onValueChange = { batchNoText = it },
                        label = { Text("Номер партии") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = batchQty.toIntOrNull()
                        if (selectedProdId.isNotBlank() && q != null && q > 0) {
                            viewModel.receiveStockBatch(
                                productId = selectedProdId,
                                qty = q,
                                supplier = supplierText.ifBlank { "Завод-Партнер" },
                                batchNumber = batchNoText.ifBlank { "ПАРТ-001" }
                            )
                            batchQty = ""
                            supplierText = ""
                            batchNoText = ""
                            showBatchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent)
                ) {
                    Text("ПРИНЯТЬ", color = LosOliveText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }

    // Modal dialog: Inventory adjust
    if (showAdjustDialog) {
        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            containerColor = LosSurface,
            title = { Text("Корректировка / Инвентаризация", color = LosWarning, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Выберите заготовку:")
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allProducts.forEach { p ->
                            FilterChip(
                                selected = selectedProdId == p.id,
                                onClick = { selectedProdId = p.id },
                                label = { Text(p.name) }
                            )
                        }
                    }

                    Text("Тип изменения:")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = adjustType == "Инвентаризация",
                            onClick = { adjustType = "Инвентаризация" },
                            label = { Text("Инвентаризация") }
                        )
                        FilterChip(
                            selected = adjustType == "Корректировка",
                            onClick = { adjustType = "Корректировка" },
                            label = { Text("Корректировка") }
                        )
                    }

                    OutlinedTextField(
                        value = adjustQty,
                        onValueChange = { adjustQty = it },
                        label = { Text("Новое точное количество (шт.)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosWarning),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = adjustComment,
                        onValueChange = { adjustComment = it },
                        label = { Text("Обоснование / Причина корректировки") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosWarning),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = adjustQty.toIntOrNull()
                        if (selectedProdId.isNotBlank() && q != null) {
                            viewModel.adjustWarehouseStockManually(
                                productId = selectedProdId,
                                type = adjustType,
                                qty = q,
                                comment = adjustComment.ifBlank { "Ручная ревизия склада" }
                            )
                            adjustQty = ""
                            adjustComment = ""
                            showAdjustDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosWarning)
                ) {
                    Text("ПРИМЕНИТЬ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustDialog = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }
}

// --- 8. DEFECTS / SCRAP LIST (БРАК) ---

@Composable
fun DefectsScreen(viewModel: CrmViewModel) {
    val allDefects by viewModel.allDefects.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Реестр зарегистрированного брака",
            icon = { Icon(Icons.Default.Delete, null, tint = LosError) }
        )

        // Simple analytic visual chart representation (Section 4.8)
        Card(
            colors = CardDefaults.cardColors(containerColor = LosSurface),
            border = BorderStroke(1.dp, LosSurfaceLighter),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "АНАЛИТИКА ТРЕНДА БРАКА",
                    style = MaterialTheme.typography.titleSmall,
                    color = LosTextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                // Graphical bars showing defect ratios per day/job
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    val mockHeights = listOf(0.2f, 0.5f, 0.1f, 0.8f, 0.3f, 0.1f, 0.0f)
                    
                    days.forEachIndexed { idx, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .fillMaxHeight(mockHeights[idx])
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (mockHeights[idx] > 0.5f) LosError else LosLimeAccent)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = day, style = MaterialTheme.typography.labelSmall, color = LosTextMuted)
                        }
                    }
                }
            }
        }

        // Register list of defects log items
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (allDefects.isEmpty()) {
                item {
                    Text("Случаи брака не зарегистрированы. Поздравляем!", color = LosTextMuted, modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                items(allDefects) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LosSurface),
                        border = BorderStroke(1.dp, LosSurfaceLighter),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${log.productName} · ${log.qty} шт.",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LosError
                                )

                                // Color indicator based on defect status
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (log.status) {
                                            "Зафиксирован" -> LosDarkOlive
                                            "В переработке" -> LosDarkOlive
                                            else -> LosDarkOlive
                                        }
                                    )
                                ) {
                                    Text(
                                        text = log.status.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (log.status) {
                                            "Зафиксирован" -> LosError
                                            "В переработке" -> LosWarning
                                            else -> LosSuccess
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Операция: ${log.operationName}", style = MaterialTheme.typography.bodyMedium, color = LosTextOnDark)
                            Text(text = "Причина брака: ${log.reason}", style = MaterialTheme.typography.bodySmall, color = LosTextOnDark)
                            if (log.supplierName != null) {
                                Text(text = "Поставщик сырья: ${log.supplierName}", style = MaterialTheme.typography.labelSmall, color = LosTextMuted)
                            }
                            Text(text = "Дата фиксации: ${formatTime(log.timestamp)}", style = MaterialTheme.typography.labelSmall, color = LosTextMuted)

                            // Status updates workflows for technologists or admin
                            if (currentUser?.role in listOf("admin", "technologist", "manager")) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = LosSurfaceLighter)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Действия:", style = MaterialTheme.typography.labelSmall, color = LosTextMuted)
                                    
                                    FilterChip(
                                        selected = log.status == "В переработке",
                                        onClick = { viewModel.updateDefectStatus(log.id, "В переработке") },
                                        label = { Text("На переработку", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = log.status == "Списан",
                                        onClick = { viewModel.updateDefectStatus(log.id, "Списан") },
                                        label = { Text("Списать", fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 9. TOOLS MANAGER SCREEN (ИНСТРУМЕНТ) ---

@Composable
fun ToolsScreen(viewModel: CrmViewModel) {
    val allTools by viewModel.allTools.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddTool by remember { mutableStateOf(false) }
    var tName by remember { mutableStateOf("") }
    var tArticle by remember { mutableStateOf("") }
    var tDiameter by remember { mutableStateOf("") }
    var tMaxLife by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SectionHeader(
                title = "База ЧПУ-Инструментов",
                icon = { Icon(Icons.Default.Build, null, tint = LosLimeAccent) },
                modifier = Modifier.weight(1f)
            )

            if (currentUser?.role in listOf("admin", "technologist")) {
                Button(
                    onClick = { showAddTool = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent)
                ) {
                    Icon(Icons.Default.Add, null, tint = LosOliveText)
                    Text("Создать инструмент", color = LosOliveText, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(allTools) { tool ->
                val remainingRatio = (tool.maxLifetimeMeters - tool.metersUsed) / tool.maxLifetimeMeters
                val remPercent = (remainingRatio * 100).coerceAtLeast(0f).roundToInt()
                val isLow = remPercent < 15

                Card(
                    colors = CardDefaults.cardColors(containerColor = LosSurface),
                    border = BorderStroke(1.dp, if (isLow) LosError else LosSurfaceLighter),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "${tool.name} (Ø${tool.diameter} мм)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LosLimeAccent
                                )
                                Text(text = "Артикул: ${tool.article}", style = MaterialTheme.typography.bodySmall, color = LosTextMuted)
                            }

                            // Interactive status badge settings
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LosDarkOlive)
                            ) {
                                Text(
                                    text = tool.status,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when (tool.status) {
                                        "Новый" -> LosLimeAccent
                                        "В работе" -> LosSuccess
                                        "В ремонте" -> LosWarning
                                        else -> LosError
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Lifespan indicator
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Износ ресурса в метрах:", style = MaterialTheme.typography.bodySmall, color = LosTextOnDark)
                            Text(
                                text = "Остаток: $remPercent%",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLow) LosError else LosSuccess
                            )
                        }

                        LinearProgressIndicator(
                            progress = (tool.metersUsed / tool.maxLifetimeMeters).coerceIn(0f, 1f),
                            color = if (isLow) LosError else LosLimeAccent,
                            trackColor = LosDarkOlive,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Text(
                            text = "Наработка: ${tool.metersUsed.roundToInt()} м / Лимит: ${tool.maxLifetimeMeters.roundToInt()} м",
                            style = MaterialTheme.typography.bodySmall,
                            color = LosTextMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = LosSurfaceLighter)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Triggering requisition / Repair setup workflows
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (currentUser?.role in listOf("admin", "operator", "technologist")) {
                                    Button(
                                        onClick = { viewModel.orderNewToolTask(tool.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = LosDarkOlive),
                                        modifier = Modifier.testTag("order_tool_button_${tool.id}")
                                    ) {
                                        Icon(Icons.Default.Build, null, tint = LosLimeAccent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ЗАКАЗАТЬ НОВЫЙ", color = LosLimeAccent, fontSize = 10.sp)
                                    }
                                }
                            }

                            if (currentUser?.role in listOf("admin", "technologist")) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilterChip(
                                        selected = tool.status == "В работе",
                                        onClick = { viewModel.updateToolStatus(tool.id, "В работе") },
                                        label = { Text("В работу", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = tool.status == "В ремонте",
                                        onClick = { viewModel.updateToolStatus(tool.id, "В ремонте") },
                                        label = { Text("В ремонт", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = tool.status == "Списан",
                                        onClick = { viewModel.updateToolStatus(tool.id, "Списан") },
                                        label = { Text("Списать", fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Create Tool Dialog
    if (showAddTool) {
        AlertDialog(
            onDismissRequest = { showAddTool = false },
            containerColor = LosSurface,
            title = { Text("Зарегистрировать ЧПУ-инструмент", color = LosLimeAccent, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tName,
                        onValueChange = { tName = it },
                        label = { Text("Название (напр., Фреза алмазная)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tArticle,
                        onValueChange = { tArticle = it },
                        label = { Text("Заводской артикул") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tDiameter,
                        onValueChange = { tDiameter = it },
                        label = { Text("Диаметр фрезы/сверла (мм)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tMaxLife,
                        onValueChange = { tMaxLife = it },
                        label = { Text("Максимальный пробег (в метрах реза)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val d = tDiameter.toFloatOrNull() ?: 4.0f
                        val m = tMaxLife.toFloatOrNull() ?: 1000f
                        if (tName.isNotBlank() && tArticle.isNotBlank()) {
                            viewModel.addNewTool(tName, tArticle, d, m)
                            tName = ""
                            tArticle = ""
                            tDiameter = ""
                            tMaxLife = ""
                            showAddTool = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent)
                ) {
                    Text("СОХРАНИТЬ", color = LosOliveText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTool = false }) {
                    Text("ОТМЕНА", color = LosTextMuted)
                }
            }
        )
    }
}

// --- 10. CHAT CONTEXT MESSAGES (СООБЩЕНИЯ) ---

@Composable
fun ChatScreen(viewModel: CrmViewModel) {
    val filteredMessages by viewModel.filteredMessages.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val searchQ by viewModel.messageSearchQuery.collectAsState()

    var chatText by remember { mutableStateOf("") }
    var selectedProductIdForChat by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Производственный чат линии",
            icon = { Icon(Icons.Default.Email, null, tint = LosLimeAccent) }
        )

        // Global search input
        OutlinedTextField(
            value = searchQ,
            onValueChange = { viewModel.updateMessageSearchQuery(it) },
            placeholder = { Text("Поиск сообщений...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = LosLimeAccent) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )

        // Messages container list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LosSurface)
                .border(1.dp, LosSurfaceLighter, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (filteredMessages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Сообщения отсутствуют.", color = LosTextMuted)
                }
            } else {
                LazyColumn(
                    reverseLayout = true, // keeps the newest messages showing at the bottom
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMessages) { msg ->
                        val isMine = msg.userId == (currentUser?.id ?: "")
                        val isSystem = msg.userId == "system"

                        Column(
                            horizontalAlignment = if (isSystem) Alignment.CenterHorizontally else if (isMine) Alignment.End else Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isSystem -> LosDarkOlive
                                        isMine -> LosLimeAccent
                                        else -> LosSurfaceLighter
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.widthIn(max = 300.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (!isSystem) {
                                        Text(
                                            text = msg.userName,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = if (isMine) LosOliveText else LosLimeAccent
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMine) LosOliveText else LosTextOnDark
                                    )

                                    if (msg.productId != null) {
                                        val pName = allProducts.find { it.id == msg.productId }?.name ?: msg.productId
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = LosDarkBg.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "🔗 Контекст: $pName",
                                                fontSize = 10.sp,
                                                color = LosLimeAccent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = formatTime(msg.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = LosTextMuted,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                            )
                            if (isMine || currentUser?.role == "admin") {
                                TextButton(
                                    onClick = { viewModel.deleteOwnMessage(msg.id) },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(20.dp)
                                ) {
                                    Text("Удалить", fontSize = 10.sp, color = LosError)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Context Selector (Section 4.10)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Привязать контекст:", fontSize = 11.sp, color = LosTextMuted)
            
            FilterChip(
                selected = selectedProductIdForChat == null,
                onClick = { selectedProductIdForChat = null },
                label = { Text("Без контекста", fontSize = 10.sp) }
            )

            allProducts.take(3).forEach { p ->
                FilterChip(
                    selected = selectedProductIdForChat == p.id,
                    onClick = { selectedProductIdForChat = p.id },
                    label = { Text(p.name, fontSize = 10.sp) }
                )
            }
        }

        // Input row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = chatText,
                onValueChange = { chatText = it },
                placeholder = { Text("Введите ваше сообщение...") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (chatText.isNotBlank()) {
                        viewModel.sendMessage(chatText.trim(), selectedProductIdForChat)
                        chatText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(LosLimeAccent, RoundedCornerShape(24.dp))
                    .testTag("send_chat_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Отправить", tint = LosOliveText)
            }
        }
    }
}

// --- 11. REPORTS GENERATOR (ОТЧЕТЫ) ---

@Composable
fun ReportsScreen(viewModel: CrmViewModel) {
    val allOperationLogs by viewModel.allOperationLogs.collectAsState()
    val allDefects by viewModel.allDefects.collectAsState()
    val allTools by viewModel.allTools.collectAsState()

    var showReportType by remember { mutableStateOf("Производство") }
    var mockExportSuccess by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Аналитические отчеты и выгрузки",
            icon = { Icon(Icons.Default.Info, null, tint = LosLimeAccent) }
        )

        // Select period and report cards
        Card(
            colors = CardDefaults.cardColors(containerColor = LosSurface),
            border = BorderStroke(1.dp, LosSurfaceLighter),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "ВЫБЕРИТЕ ОТЧЕТ ДЛЯ СКАЧИВАНИЯ:", fontWeight = FontWeight.Bold, color = LosLimeAccent)
                Spacer(modifier = Modifier.height(10.dp))

                val rTypes = listOf("Производство", "Брак сырья", "Инструменты", "Склад")
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rTypes.forEach { t ->
                        FilterChip(
                            selected = showReportType == t,
                            onClick = { showReportType = t },
                            label = { Text(t) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            mockExportSuccess = "📥 Успешно сгенерирован Excel-отчет '$showReportType' за июнь 2026. Файл 'LOS_REPORT_${showReportType.uppercase()}.xlsx' сохранен в выгрузки устройства."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = LosOliveText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("СКАЧАТЬ EXCEL", color = LosOliveText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            mockExportSuccess = "📄 Сформирован PDF-документ '$showReportType' для печати. LOS_PRINT_SHEET.pdf отправлен в спулер принтера."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LosDarkOlive),
                        border = BorderStroke(1.dp, LosLimeAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Info, null, tint = LosLimeAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ПЕЧАТЬ PDF", color = LosLimeAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Export Success Snackbar-like banner
        AnimatedVisibility(visible = mockExportSuccess != null) {
            mockExportSuccess?.let { txt ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = LosDarkOlive),
                    border = BorderStroke(1.dp, LosLimeAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { mockExportSuccess = null }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = LosLimeAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = txt, color = LosTextOnDark, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Reports dynamic statistics preview based on selected category
        SectionHeader(
            title = "Предпросмотр данных: $showReportType",
            icon = { Icon(Icons.Default.Menu, null, tint = LosLimeAccent) }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (showReportType) {
                "Производство" -> {
                    if (allOperationLogs.isEmpty()) {
                        item { Text("Сменные журналы отсутствуют.", color = LosTextMuted) }
                    } else {
                        items(allOperationLogs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LosSurfaceLighter),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(text = log.productName, fontWeight = FontWeight.Bold, color = LosTextOnDark)
                                        Text(text = "Операция: ${log.operationName}", style = MaterialTheme.typography.bodySmall, color = LosTextOnDark)
                                        Text(text = "Оператор: ${log.operatorName}", style = MaterialTheme.typography.labelSmall, color = LosTextMuted)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Годных: ${log.qtyOk} шт.", color = LosSuccess, fontWeight = FontWeight.Bold)
                                        Text(text = "Брак: ${log.qtyDefect} шт.", color = LosError, fontWeight = FontWeight.Bold)
                                        Text(text = "Расход: ${log.metersUsed.roundToInt()} м", style = MaterialTheme.typography.labelSmall, color = LosTextMuted)
                                    }
                                }
                            }
                        }
                    }
                }

                "Брак сырья" -> {
                    val defectsGrouped = allDefects.groupBy { it.reason }
                    if (defectsGrouped.isEmpty()) {
                        item { Text("Реестр брака чист.", color = LosTextMuted) }
                    } else {
                        defectsGrouped.forEach { (reason, list) ->
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = LosSurfaceLighter),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(text = reason, fontWeight = FontWeight.Bold, color = LosTextOnDark)
                                        Text(text = "Всего: ${list.sumOf { it.qty }} шт.", color = LosError, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                "Инструменты" -> {
                    items(allTools) { tool ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LosSurfaceLighter),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(text = tool.name, fontWeight = FontWeight.Bold, color = LosTextOnDark)
                                Text(
                                    text = "Наработка: ${tool.metersUsed.roundToInt()} м",
                                    color = if (tool.metersUsed >= tool.maxLifetimeMeters * 0.85f) LosError else LosLimeAccent
                                )
                            }
                        }
                    }
                }

                else -> {
                    item {
                        Text("Данные склада актуальны и засинхронизированы с базой данных PostgreSQL.", color = LosTextOnDark)
                    }
                }
            }
        }
    }
}

// --- 12. ADMIN & ACCESS CONTROL PANEL (ДОСТУП) ---

@Composable
fun AdminScreen(viewModel: CrmViewModel) {
    val allUsers by viewModel.allUsers.collectAsState()
    val allInvites by viewModel.allInvites.collectAsState()
    val allAuditLogs by viewModel.allAuditLogs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var inviteCodeInput by remember { mutableStateOf("") }
    var inviteRoleInput by remember { mutableStateOf("operator") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Панель администрирования & Доступа",
            icon = { Icon(Icons.Default.Lock, null, tint = LosLimeAccent) }
        )

        // Invite generation card
        Card(
            colors = CardDefaults.cardColors(containerColor = LosSurface),
            border = BorderStroke(1.dp, LosSurfaceLighter),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "ГЕНЕРАЦИЯ ИНВАЙТ-КОДА", fontWeight = FontWeight.Bold, color = LosLimeAccent)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inviteCodeInput,
                    onValueChange = { inviteCodeInput = it },
                    label = { Text("Уникальный инвайт-код (напр., OP-START)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LosLimeAccent),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Роль для инвайта:")
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val roles = listOf("operator" to "Оператор", "technologist" to "Технолог", "manager" to "Начальник")
                    roles.forEach { (roleId, label) ->
                        FilterChip(
                            selected = inviteRoleInput == roleId,
                            onClick = { inviteRoleInput = roleId },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (inviteCodeInput.isNotBlank()) {
                            viewModel.generateInviteCode(inviteCodeInput.trim().uppercase(), inviteRoleInput)
                            inviteCodeInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LosLimeAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("СОЗДАТЬ ИНВАЙТ-КОД", color = LosOliveText, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active user cards listing & blocks (Section 4.12)
        SectionHeader(
            title = "Управление учетными записями сотрудников",
            icon = { Icon(Icons.Default.Person, null, tint = LosLimeAccent) }
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = LosSurface),
            border = BorderStroke(1.dp, LosSurfaceLighter),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                allUsers.forEach { user ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                color = if (user.isBlocked) LosError else LosTextOnDark
                            )
                            Text(text = "Логин: ${user.id} · Роль: ${user.role}", style = MaterialTheme.typography.bodySmall, color = LosTextMuted)
                        }

                        // Block unblock toggle
                        if (user.id != (currentUser?.id ?: "") && user.id != "admin") {
                            Button(
                                onClick = { viewModel.toggleUserBlock(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (user.isBlocked) LosSuccess else LosError)
                            ) {
                                Text(text = if (user.isBlocked) "РАЗБЛОКИРОВАТЬ" else "ЗАБЛОКИРОВАТЬ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Divider(color = LosSurfaceLighter)
                }
            }
        }

        // Audit Trails history log
        SectionHeader(
            title = "Системный журнал аудита (Audit Log)",
            icon = { Icon(Icons.Default.List, null, tint = LosLimeAccent) }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(allAuditLogs) { audit ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = LosSurfaceLighter),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = audit.action, style = MaterialTheme.typography.bodySmall, color = LosTextOnDark)
                        Text(
                            text = "Автор: ${audit.userName} (${audit.userId}) · ${formatTime(audit.timestamp)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LosTextMuted
                        )
                    }
                }
            }
        }
    }
}
