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
fun BookingScreenTab(viewModel: MainViewModel) {
    val services by viewModel.services.collectAsStateWithLifecycle()
    val selectedService by viewModel.selectedService.collectAsStateWithLifecycle()
    val selectedMaster by viewModel.selectedMaster.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedSlot by viewModel.selectedTimeSlot.collectAsStateWithLifecycle()
    val clientName by viewModel.clientName.collectAsStateWithLifecycle()
    val clientPhone by viewModel.clientPhone.collectAsStateWithLifecycle()
    val loading by viewModel.bookingLoading.collectAsStateWithLifecycle()
    val mastersList by viewModel.mastersList.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val selectedNailArt by viewModel.selectedNailArtForBooking.collectAsStateWithLifecycle()
    val currentMasterCurrency = mastersList.find { it.name.equals(selectedMaster, ignoreCase = true) }?.currency ?: viewModel.globalCurrency.value

    val haptic = LocalHapticFeedback.current
    var activeStep by remember { mutableStateOf(1) } // 1: Service, 2: Details/Date, 3: Confirmation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Linear Progress Steps indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicator(step = 1, text = "1. Услуга", active = activeStep >= 1)
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (activeStep > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            StepIndicator(step = 2, text = "2. Дата/Мастер", active = activeStep >= 2)
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (activeStep > 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            StepIndicator(step = 3, text = "3. Клиент", active = activeStep >= 3)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.weight(1f)) {
            when (activeStep) {
                1 -> {
                    // STEP 1: SELECT SERVICE FROM PRICE LIST
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Выберите услугу маникюрной студии:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(services) { service ->
                                val isSelected = selectedService?.id == service.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectService(service) }
                                        .testTag("service_card_${service.id}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = service.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Категория: ${service.category} • ⏱ ${service.durationMin} мин",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = viewModel.formatPrice(service.price, currentMasterCurrency),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = service.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom action
                        Button(
                            onClick = { activeStep = 2 },
                            enabled = selectedService != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(50.dp)
                                .testTag("booking_next_step_1"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Выбрать мастера и дату", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> {
                    // STEP 2: SELECT MASTER, DATE, TIME SLOT
                    val durationBreakdown = remember(selectedService, selectedNailArt) {
                        com.example.util.SmartSchedulerEngine.calculateServiceDuration(
                            serviceName = selectedService?.name ?: "",
                            hasDesign = selectedNailArt != null,
                            hasOldCoverRemoval = selectedService?.name?.contains("Снятие", ignoreCase = true) == false,
                            hasSpaCare = false
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // BACK TO STEP 1 BUTTON
                        item {
                            OutlinedButton(
                                onClick = { activeStep = 1 },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Изменить услугу")
                            }
                        }

                        // Selected service card summary with Smart Duration breakdown
                        item {

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = selectedService?.name ?: "",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "${selectedService?.let { viewModel.formatPrice(it.price, currentMasterCurrency) } ?: ""} • Расчетное время: ${com.example.util.SmartSchedulerEngine.formatDurationString(durationBreakdown.totalMinutes)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Text(
                                                text = "База: ${durationBreakdown.baseMinutes} мин",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (durationBreakdown.nailDesignMinutes > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Дизайн: +${durationBreakdown.nailDesignMinutes} мин",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (durationBreakdown.removalMinutes > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surface
                                            ) {
                                                Text(
                                                    text = "Снятие: +${durationBreakdown.removalMinutes} мин",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // SELECT SPECIALIST
                        item {
                            Text("Выберите мастера:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(mastersList) { masterProfile ->
                                    val isSel = selectedMaster == masterProfile.name
                                    Card(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clickable { viewModel.selectMaster(masterProfile.name) }
                                            .testTag("master_card_${masterProfile.id}"),
                                        colors = CardDefaults.cardColors(containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = masterProfile.name.take(1).uppercase(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = masterProfile.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Rating",
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${masterProfile.rating}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "Опыт: ${masterProfile.experienceYears} лет",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // SELECT DATE
                        item {
                            Text("Выберите дату посещения:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(viewModel.dates) { date ->
                                    val isSel = selectedDate == date
                                    Card(
                                        modifier = Modifier
                                            .clickable { viewModel.selectDate(date) }
                                            .testTag("date_card_$date"),
                                        colors = CardDefaults.cardColors(containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = date, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // SELECT TIME SLOT
                        item {
                            Text("Доступное время:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier.height(120.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(viewModel.timeSlots) { slot ->
                                    val currentDurationMin = durationBreakdown.totalMinutes
                                    val isBooked = selectedMaster != null && selectedDate != null && bookings.any { b ->
                                        if (b.masterName.equals(selectedMaster, ignoreCase = true) && b.date == selectedDate && b.status != "Отменена") {
                                            com.example.util.SmartSchedulerEngine.isSlotOverlappingWithExistingBooking(
                                                targetSlot = slot,
                                                targetDurationMinutes = currentDurationMin,
                                                bookedSlot = b.timeSlot,
                                                bookedDurationMinutes = 90
                                            )
                                        } else false
                                    }
                                    val isSel = selectedSlot == slot
                                    Card(
                                        modifier = Modifier
                                            .clickable(enabled = !isBooked) { viewModel.selectTimeSlot(slot) }
                                            .testTag("slot_card_$slot"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSel) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else if (isBooked) {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSel) {
                                                MaterialTheme.colorScheme.primary
                                            } else if (isBooked) {
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            }
                                        )
                                    ) {
                                        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                                            if (isBooked) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = slot, style = MaterialTheme.typography.bodySmall, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                                    Text(text = "Занято", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Text(text = slot, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // NEXT ACTION
                        item {
                            Button(
                                onClick = { activeStep = 3 },
                                enabled = selectedMaster != null && selectedDate != null && selectedSlot != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("booking_next_step_2"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Продолжить", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                3 -> {
                    // STEP 3: CLIENT CONTACT DETAILS & CONFIRM
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            OutlinedButton(
                                onClick = { activeStep = 2 },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Изменить дату/мастера")
                            }
                        }

                        // Summary Check-out card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Проверка параметров записи:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Услуга:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(selectedService?.name ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (selectedNailArt != null) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Премиум Арт:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(selectedNailArt?.title ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Мастер:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(selectedMaster ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Дата & Время:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${selectedDate} в ${selectedSlot}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                    val customArtSurcharge = selectedNailArt?.extraPrice ?: 0.0
                                    val priceVal = (selectedService?.price ?: 0.0) + customArtSurcharge
                                    val escrowVal = priceVal * 0.20
                                    val salonVal = priceVal * 0.80
                                    val masterCurrency = mastersList.find { it.name.equals(selectedMaster, ignoreCase = true) }?.currency ?: viewModel.globalCurrency.value

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Полная стоимость услуги:", style = MaterialTheme.typography.bodySmall)
                                        Text(viewModel.formatPrice(priceVal, masterCurrency), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Депозит Escrow (20% сейчас):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(viewModel.formatPrice(escrowVal, masterCurrency), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Оплата в салоне (80% позже):", style = MaterialTheme.typography.bodySmall)
                                        Text(viewModel.formatPrice(salonVal, masterCurrency), style = MaterialTheme.typography.bodySmall)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Итого к оплате сейчас:", fontWeight = FontWeight.Bold)
                                        Text(viewModel.formatPrice(escrowVal, masterCurrency), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }

                        // Contact Fields Form
                        item {
                            Text("Введите ваши контактные данные:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { viewModel.clientName.value = it },
                                label = { Text("Ваше Имя") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                                modifier = Modifier.fillMaxWidth().testTag("client_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = clientPhone,
                                onValueChange = { viewModel.clientPhone.value = it },
                                label = { Text("Ваш телефон") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                                modifier = Modifier.fillMaxWidth().testTag("client_phone_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "☁️ Запись сохраняется в защищенном облаке Firebase (Firestore) и локально в приложении, а также планирует напоминание о визите.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        item {
                            if (loading) {
                                Box(modifier = Modifier.fillMaxWidth().padding(10.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.requestBookingConfirmation()
                                        activeStep = 1
                                    },
                                    enabled = clientName.isNotBlank() && clientPhone.isNotBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("confirm_booking_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Записаться онлайн (+ бонусы)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun StepIndicator(step: Int, text: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.toString(),
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --------------------------------------------------------------------
// --------------------------------------------------------------------
// TAB 3: BASE DESIGNS GALLERY FOR INSPIRATION WITH FILTERS
// --------------------------------------------------------------------


@Composable
fun BookingSuccessDialog(
    booking: Booking,
    language: String,
    masterAddress: String = "",
    masterLat: Double = 41.6150,
    masterLon: Double = 2.6620,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == "ru") {
                        "Запись успешна! 🎉"
                    } else if (language == "es") {
                        "¡Reserva exitosa! 🎉"
                    } else {
                        "Booking successful! 🎉"
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (language == "ru") {
                        "Вы успешно записаны в студию NailiX! Мы напомним вам о встрече push-уведомлением на главном экране."
                    } else if (language == "es") {
                        "¡Te has reservado correctamente en NailiX! Te recordaremos la cita mediante una notificación push en la pantalla principal."
                    } else {
                        "You have successfully booked at NailiX! We will remind you of the meeting via a push notification on the main screen."
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Услуга: ${booking.serviceName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("Мастер: ${booking.masterName}", style = MaterialTheme.typography.bodySmall)
                        Text("Дата: ${booking.date}", style = MaterialTheme.typography.bodySmall)
                        Text("Время: ${booking.timeSlot}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var showQrDialogInSuccess by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val haptic = LocalHapticFeedback.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showQrDialogInSuccess = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("QR-билет", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            com.example.util.CalendarSyncHelper.exportBookingToCalendar(context, booking)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Календарь", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        com.example.util.LocationGeocoderHelper.openMapsDirections(
                            context = context,
                            label = booking.masterName,
                            address = masterAddress,
                            lat = masterLat,
                            lon = masterLon
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("success_directions_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Маршрут в студию", fontWeight = FontWeight.Bold)
                }

                if (showQrDialogInSuccess) {
                    SovereignQrTicketDialog(
                        booking = booking,
                        masterAddress = masterAddress,
                        masterLat = masterLat,
                        masterLon = masterLon,
                        onDismiss = { showQrDialogInSuccess = false }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("dismiss_success_dialog_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Отлично", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --------------------------------------------------------------------
// SALON OWNER ADMIN PANEL
// --------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)


@Composable
fun BookingConfirmationDialog(
    booking: Booking,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val mastersList by viewModel.mastersList.collectAsStateWithLifecycle()
    val globalCurrency by viewModel.globalCurrency.collectAsStateWithLifecycle()
    var selectedPaymentMethod by remember { mutableStateOf("studio_visit") }

    val masterCurrency = mastersList.find { it.name.equals(booking.masterName, ignoreCase = true) }?.currency
        ?: globalCurrency

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Confirmation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (onboardingLanguage == "ru") "Подтверждение записи в студии" else "Studio Booking Confirmation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Мастер: ${booking.masterName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("Услуга: ${booking.serviceName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Дата и время: ${booking.date} в ${booking.timeSlot}", style = MaterialTheme.typography.bodyMedium)
                    Text("Клиент: ${booking.clientName} (${booking.clientPhone})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Итого к оплате в студии:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = viewModel.formatPrice(booking.price, masterCurrency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text("Способ оплаты (без предоплаты онлайн):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                listOf(
                    "studio_visit" to "Оплата на месте в студии (наличные / терминал)",
                    "studio_transfer" to "Перевод мастеру при встрече"
                ).forEach { (methodKey, label) ->
                    val isSelected = selectedPaymentMethod == methodKey
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = methodKey }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPaymentMethod = methodKey }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.confirmStudioBooking(selectedPaymentMethod)
                        Toast.makeText(context, "Запись подтверждена и сохранена! 💅", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_studio_booking_submit")
                ) {
                    Text("Подтвердить запись", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Вернуться к изменению")
                }
            }
        }
    }
}

