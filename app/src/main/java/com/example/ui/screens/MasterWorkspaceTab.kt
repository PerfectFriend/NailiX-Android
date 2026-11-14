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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import com.example.ui.dialogs.StoriesGeneratorDialog
import com.example.util.CalendarSyncHelper
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
import com.example.ui.viewmodel.CustomAd
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawBehind
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder
import androidx.compose.ui.text.TextStyle
import com.example.ui.screens.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

@Composable
fun MasterWorkspaceTab(viewModel: MainViewModel) {
    val registeredMasterProfile by viewModel.registeredMasterProfile.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val reviews by viewModel.reviewsList.collectAsStateWithLifecycle()
    val feeWaiverUnlocked by viewModel.feeWaiverUnlocked.collectAsStateWithLifecycle()
    val loyalty by viewModel.loyaltyInfo.collectAsStateWithLifecycle()
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current

    if (registeredMasterProfile == null) {
        // Master Registration Form
        var name by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var hours by remember { mutableStateOf("10:00 - 19:00") }
        var experienceYears by remember { mutableStateOf("") }
        var languages by remember { mutableStateOf("") }
        var igHandle by remember { mutableStateOf("") }
        var masterCurrency by remember { mutableStateOf("EUR") }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Регистрация Мастера",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Создайте свой публичный профиль, укажите график работы, импортируйте работы из Instagram и привлекайте клиентов поблизости.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Имя и Фамилия") },
                        placeholder = { Text("Например: Анна Смирнова") },
                        modifier = Modifier.fillMaxWidth().testTag("master_reg_name")
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Адрес салона/кабинета") },
                        modifier = Modifier.fillMaxWidth().testTag("master_reg_address")
                    )

                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("График работы") },
                        placeholder = { Text("Например: 10:00 - 21:00") },
                        modifier = Modifier.fillMaxWidth().testTag("master_reg_hours")
                    )

                    OutlinedTextField(
                        value = experienceYears,
                        onValueChange = { experienceYears = it },
                        label = { Text("Стаж работы (лет)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("master_reg_exp")
                    )

                    OutlinedTextField(
                        value = languages,
                        onValueChange = { languages = it },
                        label = { Text("Языки общения (через запятую)") },
                        modifier = Modifier.fillMaxWidth().testTag("master_reg_langs")
                    )

                    OutlinedTextField(
                        value = igHandle,
                        onValueChange = { igHandle = it },
                        label = { Text("Никнейм в Instagram") },
                        placeholder = { Text("Например: victoria1001art") },
                        modifier = Modifier.fillMaxWidth().testTag("master_reg_ig")
                    )

                    Text(
                        text = if (onboardingLanguage == "ru") "Валюта расчетов" else "Settlement Currency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = if (onboardingLanguage == "ru") "Выберите валюту, в которой будут идти расчеты и отображаться цены по умолчанию." else "Select the default currency for calculations and price tags.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val currencies = listOf(
                        Pair("EUR", "€ EUR"),
                        Pair("USD", "$ USD"),
                        Pair("RUB", "₽ RUB"),
                        Pair("GBP", "£ GBP"),
                        Pair("CHF", "₣ CHF"),
                        Pair("ILS", "₪ ILS"),
                        Pair("AED", "د.إ AED")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { (code, display) ->
                            val isSelected = masterCurrency == code
                            FilterChip(
                                selected = isSelected,
                                onClick = { masterCurrency = code },
                                label = { Text(display, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("reg_currency_chip_$code")
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Пожалуйста, введите имя", Toast.LENGTH_SHORT).show()
                            } else {
                                val expVal = experienceYears.toIntOrNull() ?: 3
                                val langList = languages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                viewModel.registerMasterProfile(name, address, hours, expVal, langList, igHandle, currency = masterCurrency)
                                Toast.makeText(context, "Профиль мастера успешно создан! 🎉", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("master_reg_submit")
                    ) {
                        Text("Создать профиль мастера", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    } else {
        val master = registeredMasterProfile!!
        val masterBookings = bookings.filter { it.masterName.equals(master.name, ignoreCase = true) }
        val masterReviews = reviews.filter { it.targetId == master.name }
        
        val parsedInstagramPosts by viewModel.parsedInstagramPosts.collectAsStateWithLifecycle()
        val selectedDisplayPostIds by viewModel.selectedDisplayPostIds.collectAsStateWithLifecycle()
        val isInstagramParsing by viewModel.isInstagramParsing.collectAsStateWithLifecycle()
        val instagramUsername by viewModel.instagramUsername.collectAsStateWithLifecycle()

        var showInstagramWebViewSettings by remember { mutableStateOf(false) }

        val services by viewModel.services.collectAsStateWithLifecycle()
        var showEditProfileDialog by remember { mutableStateOf(false) }
        var showAddServiceDialog by remember { mutableStateOf(false) }
        var serviceToEditPrice by remember { mutableStateOf<ServiceItem?>(null) }
        var showQrScannerDialog by remember { mutableStateOf(false) }

        var showRateClientDialogForBooking by remember { mutableStateOf<Booking?>(null) }

        var adTitle by remember { mutableStateOf("Маникюр со скидкой! 💅") }
        var adDesc by remember { mutableStateOf("Запишитесь сегодня и получите спа-уход в подарок!") }
        var adDiscount by remember { mutableStateOf("15%") }
        var adColor by remember { mutableStateOf("Pink") }
        var adCta by remember { mutableStateOf("Записаться") }
        var showStoriesGenerator by remember { mutableStateOf(false) }
        var consumablesCostPerProcedure by remember { mutableDoubleStateOf(4.5) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(master.name.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(master.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${master.rating} (${masterReviews.size} отзывов)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("📍 Адрес: ${master.address}", style = MaterialTheme.typography.bodySmall)
                        Text("⏰ График: ${master.workingSchedule}", style = MaterialTheme.typography.bodySmall)
                        Text("🗣️ Языки: ${master.languages.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                        Text("💱 Валюта расчетов: ${viewModel.formatCurrencyName(master.currency)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                        val nodeStatus by viewModel.sovereignNodeStatus.collectAsStateWithLifecycle()
                        val simplexQueue by viewModel.simplexQueue.collectAsStateWithLifecycle()
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (nodeStatus.isConnected) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (nodeStatus.isConnected) "🟢 Нода Tor активна" else "🔴 Нода оффлайн",
                                    color = if (nodeStatus.isConnected) Color(0xFF059669) else Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📯 SimpleX: ${simplexQueue.fingerprint.take(9)}...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.weight(1f).testTag("master_edit_profile_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("График & Профиль", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val sb = StringBuilder()
                                    sb.appendLine("📊 ОТЧЕТ МАСТЕРА: ${master.name}")
                                    sb.appendLine("📍 Адрес: ${master.address}")
                                    sb.appendLine("⏰ График: ${master.workingSchedule}")
                                    sb.appendLine("Всего записей: ${masterBookings.size}")
                                    val completed = masterBookings.filter { it.status == "Выполнена" }
                                    val active = masterBookings.filter { it.status != "Выполнена" && it.status != "Отменена" }
                                    sb.appendLine("Выполнено: ${completed.size}, В процессе: ${active.size}")
                                    val totalRevenue = masterBookings.filter { it.status != "Отменена" }.sumOf { it.price }
                                    sb.appendLine("Выручка: ${viewModel.formatPrice(totalRevenue, master.currency)}")
                                    sb.appendLine("\n--- СПИСОК КЛИЕНТОВ ---")
                                    masterBookings.forEach { b ->
                                        sb.appendLine("• ${b.date} ${b.timeSlot} — ${b.clientName} (${b.clientPhone}): ${b.serviceName} [${b.status}]")
                                    }
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Master Report", sb.toString())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "📋 Отчет по клиентам и выручке скопирован в буфер обмена!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f).testTag("master_export_report_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Экспорт отчета", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showQrScannerDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("master_qr_checkin_btn")
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Scanner", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Сканировать QR-билет клиента (Чекин)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Platform Commission Status with Loyalty exchange
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🛡️ Ставка комиссии платформы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (feeWaiverUnlocked) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = "Active", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("АКТИВИРОВАНА СТАВКА 0% 🎉\nВы освобождены от комиссий до конца месяца!", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        } else {
                            Text(
                                text = "Стандартная ставка: 20%. Сделайте списание 400 баллов лояльности при рейтинге >= 4.8, чтобы полностью сбросить комиссию до 0%!\n\nВаши показатели:\n• Ваш рейтинг: ${master.rating}\n• Лояльность баллы: ${loyalty?.points ?: 0}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (viewModel.exchangePointsForFeeWaiver()) {
                                        Toast.makeText(context, "Ваучер 0% успешно активирован! 🎉", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Недостаточно баллов или ваш рейтинг ниже 4.8", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("claim_commission_waiver")
                            ) {
                                Text("Обменять 400 баллов на 0% комиссию")
                            }
                        }
                    }
                }
            }

            // Instagram Portfolio settings card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("instagram_portfolio_settings_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Instagram",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📸 Настройки Instagram и Портфолио",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Управляйте своим живым портфолио в повседневном режиме. Из 20 импортированных постов вы можете выбрать до 5 лучших для показа клиентам.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Username configuration
                        var inputUsername by remember { mutableStateOf(instagramUsername) }
                        
                        OutlinedTextField(
                            value = inputUsername,
                            onValueChange = { inputUsername = it },
                            label = { Text("Имя пользователя Instagram") },
                            leadingIcon = { Text("@", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (isInstagramParsing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    IconButton(
                                        onClick = {
                                            viewModel.updateInstagramUsername(inputUsername)
                                            viewModel.triggerInstagramParsing()
                                            Toast.makeText(context, "Фид успешно обновлен! 🎉", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Обновить фид")
                                    }
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (inputUsername.isBlank()) {
                                    Toast.makeText(context, "Пожалуйста, введите имя пользователя!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.updateInstagramUsername(inputUsername)
                                    showInstagramWebViewSettings = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("instagram_settings_oauth_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE1306C)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Instagram Login",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Войти через Instagram и импортировать Топ-20", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        if (showInstagramWebViewSettings) {
                            Dialog(onDismissRequest = { showInstagramWebViewSettings = false }) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.9f)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(2.dp, Color(0xFFE1306C)),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFFF1F2))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = "IG Logo",
                                                    tint = Color(0xFFE1306C),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Instagram Secure Web Sign-In", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontSize = 14.sp)
                                            }
                                            IconButton(onClick = { showInstagramWebViewSettings = false }) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                                            }
                                        }
                                        
                                        AndroidView(
                                            modifier = Modifier.weight(1f),
                                            factory = { ctx ->
                                                WebView(ctx).apply {
                                                    layoutParams = ViewGroup.LayoutParams(
                                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                                        ViewGroup.LayoutParams.MATCH_PARENT
                                                    )
                                                    settings.javaScriptEnabled = true
                                                    settings.domStorageEnabled = true
                                                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                                    webViewClient = object : WebViewClient() {
                                                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                            val url = request?.url?.toString() ?: ""
                                                            if (url.contains("code=") || url.contains("token=") || url.contains("callback") || url.contains("success")) {
                                                                showInstagramWebViewSettings = false
                                                                Toast.makeText(context, "Вход через Instagram OAuth выполнен! Запускаем импорт...", Toast.LENGTH_SHORT).show()
                                                                
                                                                viewModel.authorizeAndImportInstagram(inputUsername) {
                                                                    Toast.makeText(context, "Импорт Топ-20 постов из Instagram успешно завершен! 🎉", Toast.LENGTH_LONG).show()
                                                                }
                                                                return true
                                                            }
                                                            return false
                                                        }
                                                    }
                                                    loadUrl("https://www.instagram.com/accounts/login/")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        
                        if (parsedInstagramPosts.isNotEmpty()) {
                            Text(
                                text = "Отметьте до 20 постов для показа в вашем портфолио (выбрано: ${selectedDisplayPostIds.size} из 20):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().height(140.dp)
                            ) {
                                items(parsedInstagramPosts) { post ->
                                    val isSelected = selectedDisplayPostIds.contains(post.id)
                                    Card(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .clickable { viewModel.toggleDisplayPostSelection(post.id, context) }
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            AsyncImage(
                                                model = post.imageUrl,
                                                contentDescription = "Ig Post",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.updateInstagramUsername(inputUsername)
                                    viewModel.triggerInstagramParsing()
                                    Toast.makeText(context, "Импорт Топ-20 публикаций запущен... 🎉", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "Sync")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("⚡ Импортировать Топ-20 по популярности", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 1. MARKETING STORIES GENERATOR (9:16)
            // ----------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("stories_generator_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, Color(0xFFE1306C)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎨 Генератор Stories (9:16)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFE1306C)
                                )
                                Text(
                                    text = "Готовый постер для Instagram и Telegram со свободными слотами в 1 клик",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showStoriesGenerator = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_stories_generator_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⚡ Создать Stories со свободными окошками", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 2. NET PROFIT & CONSUMABLES CALCULATOR
            // ----------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("net_profit_calc_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF00A896), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📊 Калькулятор чистой прибыли и материалов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00A896))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Учитывайте реальные расходы на материалы и вычисляйте чистую прибыль с каждой процедуры.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalGross = masterBookings.sumOf { it.price }.let { if (it <= 0.0) 8400.0 else it }
                        val proceduresCount = if (masterBookings.isEmpty()) 4 else masterBookings.size
                        val totalConsumables = proceduresCount * consumablesCostPerProcedure * 100
                        val netProfit = (totalGross - totalConsumables).coerceAtLeast(0.0)
                        val marginPercent = if (totalGross > 0) ((netProfit / totalGross) * 100).toInt() else 80

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00A896).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Валовая выручка (процедуры):", style = MaterialTheme.typography.bodySmall)
                                    Text(viewModel.formatPrice(totalGross, master.currency), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Расходники (~4.5 € / визит):", style = MaterialTheme.typography.bodySmall)
                                    Text("- " + viewModel.formatPrice(totalConsumables, master.currency), color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Чистая прибыль мастера:", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF00A896))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF00A896)) {
                                            Text("$marginPercent% маржа", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(viewModel.formatPrice(netProfit, master.currency), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF00A896))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 3. SMART RETENTION CRM (CLIENT CYCLES & AUTOMATION)
            // ----------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("retention_crm_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🔔 CRM возврата клиентов (Smart Retention)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Автоматический трекинг цикла носки покрытия (21–25 дней). Напомните клиентам вовремя обновить маникюр без сколов и отслоек.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Smart client retention reminder cards
                        listOf(
                            Triple("Анна Смирнова", "+34 612 888 123", "24 дня назад"),
                            Triple("Ольга Петрова", "+34 645 999 456", "22 дня назад")
                        ).forEach { (clientName, clientPhone, daysAgo) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(clientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Визит: $daysAgo • Пора на коррекцию 💅", fontSize = 11.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(
                                        onClick = {
                                            val smsText = "Здравствуйте, $clientName! Прошло более 3 недель с вашего визита в NailiX. Самое время обновить маникюр! Для вас есть свободные окошки на этой неделе ✨"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$clientPhone")).apply {
                                                putExtra("sms_body", smsText)
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Напоминание подготовлено: $clientPhone", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send Reminder", tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AD CONSTRUCTOR CARD FOR MASTERS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ad_constructor_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Promotion",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📢 Конструктор рекламных баннеров",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "Создайте рекламное объявление и привлекайте клиентов! Баннер будет показываться всем пользователям бесплатной версии.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adTitle,
                            onValueChange = { adTitle = it },
                            label = { Text("Заголовок рекламы") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("ad_construct_title")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = adDesc,
                            onValueChange = { adDesc = it },
                            label = { Text("Описание акции") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("ad_construct_desc")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = adDiscount,
                                onValueChange = { adDiscount = it },
                                label = { Text("Бейдж (скидка/текст)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("ad_construct_badge")
                            )
                            OutlinedTextField(
                                value = adCta,
                                onValueChange = { adCta = it },
                                label = { Text("Текст кнопки") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("ad_construct_cta")
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Цветовой стиль баннера:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Pink" to Color(0xFFE1306C),
                                "Gold" to Color(0xFFD97706),
                                "Cyan" to Color(0xFF0891B2),
                                "Purple" to Color(0xFF7C3AED),
                                "Green" to Color(0xFF16A34A)
                            ).forEach { (name, color) ->
                                val isSelected = adColor == name
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(color, shape = CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { adColor = name }
                                        .testTag("ad_color_choice_$name"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Interactive Preview Box
                        Text("Предпросмотр баннера для клиентов:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val bannerBg = when (adColor) {
                            "Pink" -> Color(0xFFFFF1F2)
                            "Gold" -> Color(0xFFFEF3C7)
                            "Cyan" -> Color(0xFFECFEFF)
                            "Purple" -> Color(0xFFF5F3FF)
                            "Green" -> Color(0xFFF0FDF4)
                            else -> Color(0xFFFFF1F2)
                        }
                        val bannerBorder = when (adColor) {
                            "Pink" -> Color(0xFFFDA4AF)
                            "Gold" -> Color(0xFFFCD34D)
                            "Cyan" -> Color(0xFF67E8F9)
                            "Purple" -> Color(0xFFC7D2FE)
                            "Green" -> Color(0xFF86EFAC)
                            else -> Color(0xFFFDA4AF)
                        }
                        val bannerAccent = when (adColor) {
                            "Pink" -> Color(0xFFE1306C)
                            "Gold" -> Color(0xFFD97706)
                            "Cyan" -> Color(0xFF0891B2)
                            "Purple" -> Color(0xFF7C3AED)
                            "Green" -> Color(0xFF16A34A)
                            else -> Color(0xFFE1306C)
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = bannerBg),
                            border = BorderStroke(1.dp, bannerBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(36.dp).background(bannerAccent, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (adDiscount.isNotEmpty()) {
                                            Text(adDiscount, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        } else {
                                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(master.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Text(adTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text(adDesc, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.DarkGray)
                                    }
                                }
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = bannerAccent),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(adCta, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                if (adTitle.isBlank() || adDesc.isBlank()) {
                                    Toast.makeText(context, "Пожалуйста, заполните заголовок и описание рекламы!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newAd = CustomAd(
                                        id = (100..99999).random(),
                                        masterName = master.name,
                                        title = adTitle,
                                        description = adDesc,
                                        discount = adDiscount,
                                        themeColor = adColor,
                                        ctaText = adCta
                                    )
                                    viewModel.addCustomAd(newAd)
                                    viewModel.sendTestFcmPromotion(
                                        context,
                                        "Анонс от ${master.name}: $adTitle 🎁",
                                        "$adDesc Скидка $adDiscount по кнопке '$adCta'!",
                                        "VIPPROMO"
                                    )
                                    Toast.makeText(context, "Ваша реклама успешно создана и отправлена в FCM Push-рассылку! 🚀", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("launch_ad_campaign_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Запустить рекламную кампанию & Push 🚀", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.sendTestFcmAppointmentReminder(context)
                                Toast.makeText(context, "💅 Тестовое FCM-напоминание отправлено в шторку Android!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("master_test_fcm_reminder_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Протестировать FCM Напоминание клиентам 🔔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Services setting block - Full interactive catalog
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("💇 Мой прайс-лист и услуги (${services.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Управляйте услугами и ценами для онлайн-записи", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledTonalButton(
                        onClick = { showAddServiceDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("master_add_service_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (services.isEmpty()) {
                item {
                    Text("В каталоге пока нет услуг. Нажмите «Добавить», чтобы создать первую услугу.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(services) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(service.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("⏱️ ${service.durationMin} мин", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(service.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                if (service.description.isNotBlank()) {
                                    Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(viewModel.formatPrice(service.price, master.currency), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { serviceToEditPrice = service },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Price", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.deleteService(service.id)
                                        Toast.makeText(context, "Услуга «${service.name}» удалена", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Service", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Bookings calendar list
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📅 Записи клиентов к вам (${masterBookings.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (masterBookings.isNotEmpty()) {
                        val activeRev = masterBookings.filter { it.status != "Отменена" }.sumOf { it.price }
                        Text(
                            text = "Выручка: ${viewModel.formatPrice(activeRev, master.currency)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (masterBookings.isEmpty()) {
                item {
                    Text("Предстоящих записей пока нет.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(masterBookings) { booking ->
                    val isDone = booking.status == "Выполнена"
                    val isCancelled = booking.status == "Отменена"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isDone -> Color(0xFF10B981).copy(alpha = 0.08f)
                                isCancelled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isDone -> Color(0xFF10B981).copy(alpha = 0.4f)
                                isCancelled -> Color.Gray.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${booking.date} в ${booking.timeSlot}", fontWeight = FontWeight.Bold, color = if (isDone) Color(0xFF059669) else MaterialTheme.colorScheme.primary)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isDone -> Color(0xFF10B981)
                                        isCancelled -> Color.Gray
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                ) {
                                    Text(booking.status, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(booking.serviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Клиент: ${booking.clientName} (${booking.clientPhone})", style = MaterialTheme.typography.bodySmall)
                            val masterCurrency = viewModel.mastersList.value.find { it.name.equals(booking.masterName, ignoreCase = true) }?.currency ?: viewModel.globalCurrency.value
                            Text("Сумма: ${viewModel.formatPrice(booking.price, masterCurrency)} (Включая 20% Escrow)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick contact row (WhatsApp & Phone)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val cleanPhone = booking.clientPhone.replace(Regex("[^0-9]"), "")
                                        val message = Uri.encode("Здравствуйте, ${booking.clientName}! Напоминаем о вашей записи на ${booking.serviceName} ${booking.date} в ${booking.timeSlot}. Ждем вас! 💅")
                                        val url = "https://wa.me/$cleanPhone?text=$message"
                                        try {
                                            uriHandler.openUri(url)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Не удалось открыть WhatsApp", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF25D366))
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            uriHandler.openUri("tel:${booking.clientPhone}")
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Не удалось набрать номер", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("📞 Позвонить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status actions row
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                if (!isDone) {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.updateBookingStatus(booking.id, "Выполнена")
                                            Toast.makeText(context, "Запись отмечена как выполненная! 🎉", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f).testTag("master_complete_booking_${booking.id}")
                                    ) {
                                        Text("Выполнено", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (!isCancelled && !isDone) {
                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.updateBookingStatus(booking.id, "Отменена")
                                            Toast.makeText(context, "Запись отменена", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Отменить", fontSize = 12.sp, color = Color(0xFFEF4444))
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showRateClientDialogForBooking = booking },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("master_rate_client_${booking.id}")
                                ) {
                                    Text("Оценить", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Edit Profile & Schedule Modal Dialog
        if (showEditProfileDialog) {
            var editName by remember { mutableStateOf(master.name) }
            var editAddress by remember { mutableStateOf(master.address) }
            var editHours by remember { mutableStateOf(master.workingSchedule) }
            var editLangs by remember { mutableStateOf(master.languages.joinToString(", ")) }
            var editInstagram by remember { mutableStateOf(master.instagramUsername) }
            var editCurrency by remember { mutableStateOf(master.currency) }
            var isVacation by remember { mutableStateOf(master.workingSchedule.contains("Отпуск", ignoreCase = true)) }

            val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            var selectedDays by remember {
                mutableStateOf(
                    daysOfWeek.filter { master.workingSchedule.contains(it, ignoreCase = true) }.toSet().ifEmpty { setOf("Пн", "Вт", "Ср", "Чт", "Пт") }
                )
            }

            Dialog(onDismissRequest = { showEditProfileDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚙️ Редактирование профиля и графика",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Имя и Фамилия") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text("Адрес кабинета / салона") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Рабочие дни:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            daysOfWeek.forEach { day ->
                                val isSelected = selectedDays.contains(day)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                    },
                                    label = { Text(day, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Режим «Отпуск / Выходные»", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isVacation,
                                onCheckedChange = { isVacation = it }
                            )
                        }

                        if (!isVacation) {
                            OutlinedTextField(
                                value = editHours,
                                onValueChange = { editHours = it },
                                label = { Text("Часы работы") },
                                placeholder = { Text("10:00 - 19:00") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = editInstagram,
                            onValueChange = { editInstagram = it },
                            label = { Text("Instagram никнейм") },
                            leadingIcon = { Text("@") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editLangs,
                            onValueChange = { editLangs = it },
                            label = { Text("Языки обслуживания") },
                            placeholder = { Text("Русский, Испанский, Английский") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Валюта расчетов:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("EUR" to "€ EUR", "USD" to "$ USD", "RUB" to "₽ RUB", "GBP" to "£ GBP", "KZT" to "₸ KZT").forEach { (code, display) ->
                                FilterChip(
                                    selected = editCurrency == code,
                                    onClick = { editCurrency = code },
                                    label = { Text(display, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showEditProfileDialog = false }) { Text("Отмена") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val finalSchedule = if (isVacation) {
                                        "В отпуске до следующей недели 🌴"
                                    } else {
                                        val daysStr = selectedDays.joinToString(", ")
                                        "$daysStr: $editHours".ifBlank { "Пн-Пт: 10:00 - 19:00" }
                                    }
                                    viewModel.updateMasterProfileDetails(
                                        name = editName,
                                        address = editAddress,
                                        schedule = finalSchedule,
                                        languages = editLangs.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                        instagram = editInstagram,
                                        currency = editCurrency
                                    )
                                    Toast.makeText(context, "Профиль и график успешно сохранены! ✨", Toast.LENGTH_SHORT).show()
                                    showEditProfileDialog = false
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Сохранить")
                            }
                        }
                    }
                }
            }
        }

        // Add Service Dialog Modal
        if (showAddServiceDialog) {
            var serviceName by remember { mutableStateOf("") }
            var serviceCategory by remember { mutableStateOf("Маникюр") }
            var servicePrice by remember { mutableStateOf("25.0") }
            var serviceDuration by remember { mutableStateOf("60") }
            var serviceDescription by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showAddServiceDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("➕ Добавить новую услугу", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        OutlinedTextField(
                            value = serviceName,
                            onValueChange = { serviceName = it },
                            label = { Text("Название услуги") },
                            placeholder = { Text("Например: Комплексный SMART-маникюр") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_service_name_input")
                        )

                        Text("Категория услуги:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Маникюр", "Педикюр", "Дизайн", "Уход & SPA", "Наращивание").forEach { cat ->
                                FilterChip(
                                    selected = serviceCategory == cat,
                                    onClick = { serviceCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = servicePrice,
                                onValueChange = { servicePrice = it },
                                label = { Text("Цена (${master.currency})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = serviceDuration,
                                onValueChange = { serviceDuration = it },
                                label = { Text("Длительность (мин)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = serviceDescription,
                            onValueChange = { serviceDescription = it },
                            label = { Text("Описание (что входит в процедуру)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showAddServiceDialog = false }) { Text("Отмена") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (serviceName.isNotBlank()) {
                                        val p = servicePrice.toDoubleOrNull() ?: 25.0
                                        val d = serviceDuration.toIntOrNull() ?: 60
                                        viewModel.addNewService(
                                            name = serviceName.trim(),
                                            category = serviceCategory,
                                            price = p,
                                            durationMin = d,
                                            description = serviceDescription.trim(),
                                            imageUrl = "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60"
                                        )
                                        Toast.makeText(context, "Услуга «$serviceName» добавлена в прайс-лист! 🎉", Toast.LENGTH_SHORT).show()
                                        showAddServiceDialog = false
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Создать")
                            }
                        }
                    }
                }
            }
        }

        // Quick Price Edit Dialog Modal
        serviceToEditPrice?.let { service ->
            var newPriceStr by remember { mutableStateOf(service.price.toString()) }
            Dialog(onDismissRequest = { serviceToEditPrice = null }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Изменение цены: ${service.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = newPriceStr,
                            onValueChange = { newPriceStr = it },
                            label = { Text("Новая стоимость (${master.currency})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { serviceToEditPrice = null }) { Text("Отмена") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val newP = newPriceStr.toDoubleOrNull()
                                    if (newP != null && newP > 0) {
                                        viewModel.updateServicePrice(service.id, newP)
                                        Toast.makeText(context, "Цена услуги обновлена! 💰", Toast.LENGTH_SHORT).show()
                                        serviceToEditPrice = null
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Сохранить")
                            }
                        }
                    }
                }
            }
        }

        // Rate client Dialog modal
        showRateClientDialogForBooking?.let { booking ->
            var rating by remember { mutableStateOf(5) }
            var comment by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showRateClientDialogForBooking = null }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Оценить клиента ${booking.clientName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        // Rating row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Оценка:")
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Stars",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { rating = star }
                                        .testTag("star_client_$star")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("Ваш отзыв о клиенте") },
                            placeholder = { Text("Пунктуальный, вежливый клиент...") },
                            modifier = Modifier.fillMaxWidth().testTag("client_review_comment_input")
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showRateClientDialogForBooking = null }) { Text("Отмена") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.leaveFeedback(booking.clientPhone, "client", rating, comment, master.name)
                                    Toast.makeText(context, "Отзыв о клиенте отправлен!", Toast.LENGTH_SHORT).show()
                                    showRateClientDialogForBooking = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("submit_client_rating_button")
                            ) {
                                Text("Отправить")
                            }
                        }
                    }
                }
            }
        }

        if (showQrScannerDialog) {
            var scannedPayloadInput by remember { mutableStateOf("") }
            var verificationResult by remember { mutableStateOf<String?>(null) }
            var isSuccess by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showQrScannerDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Офлайн QR-чекин клиента", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Отсканируйте или выберите запись клиента из списка для моментальной суверенной проверки криптографического билета.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick selection of pending bookings for instant check-in
                        Text("Ожидающие подтверждения/сеанса:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        val pending = masterBookings.filter { it.status != "Выполнена" && it.status != "Отменена" }
                        if (pending.isEmpty()) {
                            Text("Нет активных записей на сегодня", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(pending) { b ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val ticket = com.example.util.VisitTicketCryptoManager.createTicket(b)
                                                scannedPayloadInput = ticket.rawPayload
                                            },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("${b.clientName} (${b.timeSlot})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                                Text(b.serviceName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text("${b.price} €", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = scannedPayloadInput,
                            onValueChange = { scannedPayloadInput = it },
                            label = { Text("Код или payload билета") },
                            placeholder = { Text("NAILIX://CHECKIN?data=...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        if (verificationResult != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = verificationResult!!,
                                    color = if (isSuccess) Color(0xFF047857) else Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (scannedPayloadInput.isBlank()) {
                                Toast.makeText(context, "Вставьте или выберите билет", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val (isValid, json) = com.example.util.VisitTicketCryptoManager.verifyAndParsePayload(scannedPayloadInput)
                            if (isValid && json != null) {
                                isSuccess = true
                                val bookingId = json.optInt("id", -1)
                                val clientName = json.optString("n", "Клиент")
                                val service = json.optString("s", "")
                                val price = json.optDouble("pr", 0.0)

                                if (bookingId > 0) {
                                    viewModel.updateBookingStatus(bookingId, "Выполнена")
                                }
                                verificationResult = "✅ Билет подлинный (SHA-256 Verified)!\nКлиент: $clientName\nУслуга: $service (${price} €)\nСтатус записи обновлен на: «Выполнена» 🎉"
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                isSuccess = false
                                verificationResult = "❌ Ошибка проверки: Неверная цифровая подпись или повреждённый билет."
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Проверить и зачекинить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQrScannerDialog = false }) {
                        Text("Закрыть")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

