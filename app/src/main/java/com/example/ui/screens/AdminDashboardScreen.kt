package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.Booking
import com.example.data.BaseDesignItem
import com.example.data.InstagramPost
import com.example.data.Reminder
import com.example.data.ServiceItem
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MasterProfile
import com.example.ui.viewmodel.ClientProfile
import com.example.ui.viewmodel.AggregatorReview
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawBehind
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder
import androidx.compose.ui.text.TextStyle
import com.example.ui.screens.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Аналитика, 1: Услуги, 2: Клиенты, 3: Нода & SimpleX
    val tabs = listOf("Аналитика", "Услуги", "Клиенты", "🧅 Нода & SimpleX")
    
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val clientProfiles by viewModel.clientProfiles.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val ownerSyncStatus by viewModel.ownerSyncStatus.collectAsStateWithLifecycle()
    val registeredMasterProfile by viewModel.registeredMasterProfile.collectAsStateWithLifecycle()

    val nodeStatus by viewModel.sovereignNodeStatus.collectAsStateWithLifecycle()
    val simplexQueue by viewModel.simplexQueue.collectAsStateWithLifecycle()
    val simplexMessages by viewModel.simplexMessages.collectAsStateWithLifecycle()
    val p2pSyncState by viewModel.p2pSyncState.collectAsStateWithLifecycle()
    val roiCalc by viewModel.roiCalc.collectAsStateWithLifecycle()

    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val fcmError by viewModel.fcmError.collectAsStateWithLifecycle()
    val fcmNotifications by viewModel.fcmNotifications.collectAsStateWithLifecycle()
    var fcmCustomTitle by remember { mutableStateOf("Скидка -20% на маникюр! 💅") }
    var fcmCustomBody by remember { mutableStateOf("Успейте записаться на этой неделе со скидкой по промокоду NAIL20!") }
    var fcmCustomPromoCode by remember { mutableStateOf("NAIL20") }
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Admin state for creating services
    var newSrvName by remember { mutableStateOf("") }
    var newSrvCategory by remember { mutableStateOf("Маникюр") }
    var newSrvPrice by remember { mutableStateOf("") }
    var newSrvDuration by remember { mutableStateOf("60") }
    var newSrvDesc by remember { mutableStateOf("") }
    var newSrvImageUrl by remember { mutableStateOf("") }
    var isCreatingService by remember { mutableStateOf(false) }
    
    val illustrationPresets = listOf(
        Pair("Классический красный", "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60"),
        Pair("Нежный миндаль", "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60"),
        Pair("Элегантный нюд", "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60"),
        Pair("СПА-массаж", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60"),
        Pair("Педикюр", "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60")
    )
    
    val categories = listOf("Маникюр", "Педикюр", "Моделирование", "СПА-уход", "Дизайн")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Owner Sync Status Indicator Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudQueue,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Локальная база владельца",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = ownerSyncStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Button(
                    onClick = { viewModel.triggerAutomaticPushSync() },
                    enabled = !isSyncing,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("sync_db_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Синхронизировать", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sub-tabs for owners
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("admin_tab_$index")
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    // TAB 1: ANALYTICS & BOOKING SESSIONS
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Numeric indicators grid
                        item {
                            Text(
                                text = "Сводные финансовые показатели",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Общая Выручка", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(viewModel.formatPrice(statistics.totalRevenue, viewModel.registeredMasterProfile.value?.currency ?: "EUR"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Активные записи", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${statistics.activeBookings} сеансов", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Популярная услуга", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(statistics.popularCategory, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Топ мастер", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(statistics.topMaster, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Sessions management
                        item {
                            Text(
                                text = "Управление записями клиентов (${bookings.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (bookings.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(
                                        text = "Пока никто не записался. Записи клиентов появятся здесь автоматически.",
                                        modifier = Modifier.padding(20.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(bookings) { booking ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(booking.clientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                Text(booking.clientPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            
                                            val statusColor = when (booking.status) {
                                                "Выполнена" -> MaterialTheme.colorScheme.primary
                                                "Отменена" -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.secondary
                                            }
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    booking.status,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = statusColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Услуга: ${booking.serviceName}", style = MaterialTheme.typography.bodyMedium)
                                                Text("Категория: ${booking.serviceCategory}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("Время: ${booking.date} в ${booking.timeSlot}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                Text("Мастер: ${booking.masterName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                val masterCurrency = viewModel.mastersList.value.find { it.name.equals(booking.masterName, ignoreCase = true) }?.currency ?: viewModel.globalCurrency.value
                                                Text("Цена: ${viewModel.formatPrice(booking.price, masterCurrency)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Booking actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (booking.status == "Подтверждена") {
                                                Button(
                                                    onClick = {
                                                        viewModel.completeBooking(booking)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier.weight(1f).height(36.dp).testTag("booking_done_${booking.id}"),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("Выполнен", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.cancelBooking(booking)
                                                    },
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                    modifier = Modifier.weight(1f).height(36.dp).testTag("booking_cancel_${booking.id}"),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("Отменить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: MANAGE SERVICES & PRICES
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Expanded Service Creation Block
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { isCreatingService = !isCreatingService },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AddBox, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Добавить новую услугу", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Icon(
                                            imageVector = if (isCreatingService) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                    }

                                    AnimatedVisibility(visible = isCreatingService) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = newSrvName,
                                                onValueChange = { newSrvName = it },
                                                label = { Text("Название услуги") },
                                                modifier = Modifier.fillMaxWidth().testTag("srv_name_input")
                                            )
                                            
                                            // Category selector
                                            Column {
                                                Text("Категория услуги:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    categories.forEach { cat ->
                                                        val isSel = newSrvCategory == cat
                                                        Card(
                                                            modifier = Modifier
                                                                .clickable { newSrvCategory = cat }
                                                                .testTag("srv_cat_$cat"),
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                                            ),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text(
                                                                text = cat,
                                                                fontSize = 10.sp,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = newSrvPrice,
                                                    onValueChange = { newSrvPrice = it },
                                                    label = { Text("Цена (₽)") },
                                                    modifier = Modifier.weight(1f).testTag("srv_price_input")
                                                )
                                                OutlinedTextField(
                                                    value = newSrvDuration,
                                                    onValueChange = { newSrvDuration = it },
                                                    label = { Text("Длительность (мин)") },
                                                    modifier = Modifier.weight(1f).testTag("srv_duration_input")
                                                )
                                            }

                                            OutlinedTextField(
                                                value = newSrvDesc,
                                                onValueChange = { newSrvDesc = it },
                                                label = { Text("Описание") },
                                                maxLines = 3,
                                                modifier = Modifier.fillMaxWidth().testTag("srv_desc_input")
                                            )

                                            // Illustration presets horizontal selection
                                            Column {
                                                Text("Выбрать иллюстрацию услуги:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    items(illustrationPresets) { preset ->
                                                        val isSelectedImg = newSrvImageUrl == preset.second
                                                        Card(
                                                            modifier = Modifier
                                                                .size(70.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable { newSrvImageUrl = preset.second }
                                                                .testTag("srv_preset_${preset.first}"),
                                                            border = BorderStroke(
                                                                width = if (isSelectedImg) 3.dp else 1.dp,
                                                                color = if (isSelectedImg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                            )
                                                        ) {
                                                            Box(modifier = Modifier.fillMaxSize()) {
                                                                AsyncImage(
                                                                    model = preset.second,
                                                                    contentDescription = preset.first,
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .background(Color.Black.copy(alpha = 0.5f))
                                                                        .align(Alignment.BottomCenter)
                                                                ) {
                                                                    Text(
                                                                        preset.first,
                                                                        color = Color.White,
                                                                        fontSize = 7.sp,
                                                                        maxLines = 1,
                                                                        modifier = Modifier.padding(2.dp),
                                                                        textAlign = TextAlign.Center
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    val pr = newSrvPrice.toDoubleOrNull() ?: 0.0
                                                    val dur = newSrvDuration.toIntOrNull() ?: 60
                                                    if (newSrvName.isNotEmpty() && pr > 0) {
                                                        viewModel.addNewService(
                                                            name = newSrvName,
                                                            category = newSrvCategory,
                                                            price = pr,
                                                            durationMin = dur,
                                                            description = newSrvDesc,
                                                            imageUrl = newSrvImageUrl
                                                        )
                                                        // Reset state
                                                        newSrvName = ""
                                                        newSrvPrice = ""
                                                        newSrvDesc = ""
                                                        newSrvImageUrl = ""
                                                        isCreatingService = false
                                                        Toast.makeText(context, "Услуга добавлена и синхронизирована!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Заполните Название и Цену!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_srv_button")
                                            ) {
                                                Text("Создать Карточку Услуги", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Listing of services
                        item {
                            Text("Все услуги и цены на устройствах клиентов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        items(services) { service ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom Illustration or Preset
                                    Card(
                                        modifier = Modifier.size(70.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = service.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500" },
                                            contentDescription = service.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = service.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Категория: ${service.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${service.durationMin} мин • ${service.description.take(50)}...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        // Quick price adjuster
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = viewModel.formatPrice(service.price, viewModel.registeredMasterProfile.value?.currency ?: "EUR"),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            
                                            // Direct pricing adjust buttons
                                            IconButton(
                                                onClick = { viewModel.updateServicePrice(service.id, maxOf(100.0, service.price - 100.0)) },
                                                modifier = Modifier.size(24.dp).testTag("price_minus_${service.id}")
                                            ) {
                                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Цена -100", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { viewModel.updateServicePrice(service.id, service.price + 100.0) },
                                                modifier = Modifier.size(24.dp).testTag("price_plus_${service.id}")
                                            ) {
                                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Цена +100", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteService(service.id) },
                                        modifier = Modifier.testTag("delete_srv_${service.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Удалить услугу",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 3: CUSTOMER DIRECTORY (CLIENTS)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "База клиентов салона (на устройстве владельца)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (clientProfiles.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(
                                        text = "База клиентов пока пуста. Как только клиенты запишутся на сеанс или подпишутся на акции, они появятся в базе.",
                                        modifier = Modifier.padding(20.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(clientProfiles) { profile ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // User avatar mockup
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = profile.name.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(profile.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                if (profile.isSubscribed) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Email,
                                                        contentDescription = "Newsletter Subscribed",
                                                        tint = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(profile.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (profile.email.isNotEmpty()) {
                                                Text(profile.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = "Записей: ${profile.totalBookings}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Потрачено: ${viewModel.formatPrice(profile.totalSpent, viewModel.registeredMasterProfile.value?.currency ?: "EUR")}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.sendFcmPromotion(
                                                    context,
                                                    "Персональное предложение для ${profile.name}",
                                                    "Специальная скидка в студии NailiX!",
                                                    fcmCustomPromoCode
                                                )
                                            },
                                            modifier = Modifier.testTag("contact_client_${profile.phone}")
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.SendToMobile, contentDescription = "Отправить акцию", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // TAB 4: SOVEREIGN NODE, TOR ONION & SIMPLEX MQ CONTROL CENTER
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Hardware & Node Telemetry (Beelink SER 9)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(if (nodeStatus.isConnected) Color(0xFF10B981) else Color.Red)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (nodeStatus.isConnected) "УЗЕЛ ДОСТУПЕН (${nodeStatus.latencyMs} мс)" else "ОФФЛАЙН",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = if (nodeStatus.isTorRoutingEnabled) "🧅 SOCKS5 :9050" else "TCP Direct",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Address row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Onion адрес (опционально):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = nodeStatus.onionAddress.ifBlank { "Не настроен" },
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (nodeStatus.onionAddress.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(nodeStatus.onionAddress))
                                                    Toast.makeText(context, "Onion-адрес скопирован!", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Копировать Onion", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Локальный адрес: ${nodeStatus.localAddress.ifBlank { "Не задан" }} • Пинг: ${if (nodeStatus.latencyMs >= 0) "${nodeStatus.latencyMs} мс" else "—"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Диагностика: ${nodeStatus.diagnosticMessage}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (nodeStatus.isConnected) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.pingSovereignNode()
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("ping_sovereign_node_btn"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Проверить связь (TCP Ping)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 2. Tor Routing Switch
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("🧅 Маршрутизация через Tor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("Сквозное шифрование через сеть Onion Services без раскрытия реального IP", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = nodeStatus.isTorRoutingEnabled,
                                        onCheckedChange = { viewModel.toggleTorRouting(it) },
                                        modifier = Modifier.testTag("tor_routing_switch")
                                    )
                                }
                            }
                        }

                        // 3. SimpleX MQ Messenger & Queue Monitor
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("📯 Очереди SimpleX MQ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text("Анонимный транспорт без постоянных ID", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text("E2EE Активно", color = Color(0xFF059669), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Очередь: ${simplexQueue.queueAddress}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Отпечаток Curve25519: ${simplexQueue.fingerprint}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Всего локальных событий: ${simplexQueue.totalMessages} (Ожидают сети: ${simplexQueue.pendingOfflineCount})", style = MaterialTheme.typography.bodySmall)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Журнал SHA-256 событий:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    simplexMessages.forEach { msg ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(msg.type, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                    Text(msg.senderFingerprint, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(msg.decryptedSummary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("SHA-256: ${msg.payloadSha256.take(24)}...", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Local Snapshot Integrity & JSON Export
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("🔄 Локальный снимок базы Room (SHA-256)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("SHA-256 State Hash: ${p2pSyncState.stateHash.take(28)}...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text("Записей в снимке: ${p2pSyncState.syncedRecordsCount} • Очередь офлайн: ${p2pSyncState.pendingConflicts}", style = MaterialTheme.typography.bodySmall)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.computeRoomStateSnapshot()
                                                Toast.makeText(context, "🔄 SHA-256 снимок базы Room пересчитан!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).testTag("trigger_p2p_sync_btn"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Снимок SHA-256", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                val backup = viewModel.exportSnapshotJson()
                                                clipboardManager.setText(AnnotatedString(backup))
                                                Toast.makeText(context, "📋 JSON-снимок Room скопирован в буфер!", Toast.LENGTH_LONG).show()
                                            },
                                            modifier = Modifier.weight(1f).testTag("export_p2p_backup_btn"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Экспорт JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Hardware ROI & Business Model Calculator (Beelink SER 9)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("📊 Окупаемость ноды Beelink SER 9 (Ryzen 9)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Text("Экономическая модель хостинга мастеров и локального ИИ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Metric highlights
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Выручка/мес", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("$${"%.1f".format(roiCalc.totalMonthlyRevenue)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF059669))
                                            }
                                        }
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Окупаемость", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("${"%.1f".format(roiCalc.paybackPeriodMonths)} мес", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("ROI (1 год)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("+${"%.0f".format(roiCalc.roi12MonthsPercent)}%", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFFD97706))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("Мастеров на обслуживании: ${roiCalc.hostedMastersCount} (по $${roiCalc.monthlyPricePerMaster}/мес = $${"%.0f".format(roiCalc.monthlyHostingRevenue)})", style = MaterialTheme.typography.bodySmall)
                                    Slider(
                                        value = roiCalc.hostedMastersCount.toFloat(),
                                        onValueChange = { viewModel.updateRoiParams(it.toInt(), roiCalc.monthlyPricePerMaster, roiCalc.monthlyAiGenerations) },
                                        valueRange = 1f..30f,
                                        steps = 28
                                    )

                                    Text("Локальных ИИ генераций в месяц: ${roiCalc.monthlyAiGenerations} (по $${roiCalc.pricePerAiGeneration} = $${"%.0f".format(roiCalc.monthlyAiRevenue)})", style = MaterialTheme.typography.bodySmall)
                                    Slider(
                                        value = roiCalc.monthlyAiGenerations.toFloat(),
                                        onValueChange = { viewModel.updateRoiParams(roiCalc.hostedMastersCount, roiCalc.monthlyPricePerMaster, it.toInt()) },
                                        valueRange = 50f..1500f,
                                        steps = 29
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("💡 Капитальные затраты на Beelink SER 9 ($${"%.0f".format(roiCalc.hardwareCost)}) окупаются менее чем за 12 месяцев при поддержке 15 мастеров!", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // 6. Firebase Cloud Messaging (FCM) Integration
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = "FCM", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("🔔 Firebase Cloud Messaging (FCM)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                Text("Push-уведомления о записях и промо-акциях", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text("FCM v1", color = Color(0xFF059669), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Device Registration Token row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("FCM Токен Устройства:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(fcmToken, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (fcmError != null) {
                                                Text(fcmError.orEmpty(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(fcmToken))
                                                Toast.makeText(context, "FCM Токен скопирован в буфер!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Копировать токен", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Локальные уведомления устройства:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.sendFcmAppointmentReminder(context)
                                            },
                                            modifier = Modifier.weight(1f).testTag("test_fcm_reminder_btn"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Напоминание", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.sendFcmPromotion(context, fcmCustomTitle, fcmCustomBody, fcmCustomPromoCode)
                                            },
                                            modifier = Modifier.weight(1f).testTag("test_fcm_promo_btn"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Промо-акция", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Создать локальное промо-уведомление:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = fcmCustomTitle,
                                        onValueChange = { fcmCustomTitle = it },
                                        label = { Text("Заголовок уведомления") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("fcm_custom_title_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = fcmCustomBody,
                                        onValueChange = { fcmCustomBody = it },
                                        label = { Text("Текст предложения") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().testTag("fcm_custom_body_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = fcmCustomPromoCode,
                                        onValueChange = { fcmCustomPromoCode = it },
                                        label = { Text("Промокод (опционально)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("fcm_custom_promocode_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            viewModel.sendFcmPromotion(context, fcmCustomTitle, fcmCustomBody, fcmCustomPromoCode)
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("launch_fcm_campaign_btn"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Отправить уведомление на устройство", fontWeight = FontWeight.Bold)
                                    }

                                    if (fcmNotifications.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("История отправленных push-уведомлений (${fcmNotifications.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        fcmNotifications.take(4).forEach { item ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                                        Text(if (item.type == "appointment_reminder") "📅 Запись" else "🎁 Промо", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(item.body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
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
    }
}

