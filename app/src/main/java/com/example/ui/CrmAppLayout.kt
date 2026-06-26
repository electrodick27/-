package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.*
import com.example.ui.theme.*
import com.example.viewmodel.CrmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmAppLayout(viewModel: CrmViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val unreadCount by viewModel.unreadMessageCount.collectAsState()

    // Determine adaptive layout based on configuration
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    if (currentUser == null || currentScreen == "login") {
        // Enforce full bleed login page
        LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.elk_crm_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, LosLimeAccent, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ЛОСЬ CRM",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = LosLimeAccent
                            )
                        }
                    },
                    actions = {
                        // Display user avatar initials and logout trigger
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { viewModel.logout() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LosLimeAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.name?.take(2)?.uppercase() ?: "ЛО",
                                    color = LosOliveText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Выйти из системы",
                                tint = LosError,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LosSurface)
                )
            },
            bottomBar = {
                if (!isTablet) {
                    CrmBottomNavigationBar(currentScreen, unreadCount) { screen ->
                        viewModel.navigateTo(screen)
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(LosDarkBg)
            ) {
                // If Tablet landscape, render Left Navigation Rail
                if (isTablet) {
                    CrmNavigationRail(currentScreen, unreadCount) { screen ->
                        viewModel.navigateTo(screen)
                    }
                }

                // Main Central Module Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Central Route Router Switcher
                    when (currentScreen) {
                        "dashboard" -> DashboardScreen(viewModel = viewModel)
                        "plan" -> PlanScreen(viewModel = viewModel)
                        "products" -> ProductsScreen(viewModel = viewModel)
                        "work" -> WorkScreen(viewModel = viewModel)
                        "timecard" -> TimeCardScreen(viewModel = viewModel)
                        "warehouse" -> WarehouseScreen(viewModel = viewModel)
                        "defects" -> DefectsScreen(viewModel = viewModel)
                        "tools" -> ToolsScreen(viewModel = viewModel)
                        "chat" -> ChatScreen(viewModel = viewModel)
                        "reports" -> ReportsScreen(viewModel = viewModel)
                        "admin" -> AdminScreen(viewModel = viewModel)
                        else -> DashboardScreen(viewModel = viewModel)
                    }

                    // Global alert banners overlays
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        errorMessage?.let { msg ->
                            BannerAlert(text = msg, isError = true) {
                                viewModel.clearMessages()
                            }
                        }

                        successMessage?.let { msg ->
                            BannerAlert(text = msg, isError = false) {
                                viewModel.clearMessages()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrmBottomNavigationBar(
    currentScreen: String,
    unreadCount: Int,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = LosSurface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        listOf(
            Triple("dashboard", "Главная", Icons.Default.Home),
            Triple("work", "Работа", Icons.Default.PlayArrow),
            Triple("warehouse", "Склад", Icons.Default.List),
            Triple("chat", "Сообщения", Icons.Default.Email),
            Triple("reports", "Отчеты", Icons.Default.Info)
        ).forEach { (screen, label, icon) ->
            NavigationBarItem(
                selected = currentScreen == screen,
                onClick = { onNavigate(screen) },
                icon = {
                    if (screen == "chat" && unreadCount > 0) {
                        BadgedBox(badge = { Badge(containerColor = LosError) { Text("$unreadCount") } }) {
                            Icon(icon, contentDescription = label)
                        }
                    } else {
                        Icon(icon, contentDescription = label)
                    }
                },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LosOliveText,
                    selectedTextColor = LosLimeAccent,
                    indicatorColor = LosLimeAccent,
                    unselectedIconColor = LosTextMuted,
                    unselectedTextColor = LosTextMuted
                ),
                modifier = Modifier.testTag("nav_item_$screen")
            )
        }
    }
}

@Composable
fun CrmNavigationRail(
    currentScreen: String,
    unreadCount: Int,
    onNavigate: (String) -> Unit
) {
    NavigationRail(
        containerColor = LosSurface,
        modifier = Modifier.fillMaxHeight()
    ) {
        listOf(
            Triple("dashboard", "Главная", Icons.Default.Home),
            Triple("plan", "План", Icons.Default.List),
            Triple("products", "Корпуса", Icons.Default.Build),
            Triple("work", "Работа", Icons.Default.PlayArrow),
            Triple("timecard", "Время", Icons.Default.CheckCircle),
            Triple("warehouse", "Склад", Icons.Default.List),
            Triple("defects", "Брак", Icons.Default.Delete),
            Triple("tools", "Инструмент", Icons.Default.Build),
            Triple("chat", "Чат", Icons.Default.Email),
            Triple("reports", "Отчеты", Icons.Default.Info),
            Triple("admin", "Доступ", Icons.Default.Lock)
        ).forEach { (screen, label, icon) ->
            NavigationRailItem(
                selected = currentScreen == screen,
                onClick = { onNavigate(screen) },
                icon = {
                    if (screen == "chat" && unreadCount > 0) {
                        BadgedBox(badge = { Badge(containerColor = LosError) { Text("$unreadCount") } }) {
                            Icon(icon, contentDescription = label)
                        }
                    } else {
                        Icon(icon, contentDescription = label)
                    }
                },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = LosOliveText,
                    selectedTextColor = LosLimeAccent,
                    indicatorColor = LosLimeAccent,
                    unselectedIconColor = LosTextMuted,
                    unselectedTextColor = LosTextMuted
                ),
                modifier = Modifier.testTag("nav_rail_item_$screen")
            )
        }
    }
}

@Composable
fun BannerAlert(text: String, isError: Boolean, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isError) LosError else LosSuccess),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDismiss() }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Warning else Icons.Default.Check,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Закрыть",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
