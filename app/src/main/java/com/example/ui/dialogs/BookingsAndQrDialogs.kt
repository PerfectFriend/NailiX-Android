package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Booking
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CalendarSyncHelper
import com.example.util.LocationGeocoderHelper
import com.example.util.SmartSchedulerEngine
import com.example.util.VisitTicketCryptoManager

@Composable
fun UpcomingBookingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val mastersList by viewModel.mastersList.collectAsStateWithLifecycle()
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedBookingForQr by remember { mutableStateOf<Booking?>(null) }
    var bookingToReschedule by remember { mutableStateOf<Booking?>(null) }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: Active, 1: History

    val activeBookings = bookings.filter { it.status == "Подтверждена" }
    val historyBookings = bookings.filter { it.status == "Выполнена" || it.status == "Отменена" }
    val displayedBookings = if (selectedFilterTab == 0) activeBookings else historyBookings

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = when (onboardingLanguage) {
                        "ru" -> "Мои записи"
                        "es" -> "Mis citas"
                        else -> "My Bookings"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilterTab == 0,
                        onClick = { selectedFilterTab = 0 },
                        label = {
                            Text(
                                text = "Активные (${activeBookings.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("bookings_tab_active")
                    )
                    FilterChip(
                        selected = selectedFilterTab == 1,
                        onClick = { selectedFilterTab = 1 },
                        label = {
                            Text(
                                text = "История (${historyBookings.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("bookings_tab_history")
                    )
                }

                if (displayedBookings.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayedBookings, key = { it.id }) { booking ->
                            val matchedMaster = mastersList.find {
                                it.name.equals(booking.masterName, ignoreCase = true)
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${booking.date} • ${booking.timeSlot}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (booking.status == "Подтверждена") {
                                            IconButton(
                                                onClick = { viewModel.cancelBooking(booking) },
                                                modifier = Modifier
                                                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                                    .testTag("cancel_booking_btn_${booking.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Cancel,
                                                    contentDescription = "Cancel booking",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        } else {
                                            val isCompleted = booking.status == "Выполнена"
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isCompleted) {
                                                    MaterialTheme.colorScheme.primaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.errorContainer
                                                }
                                            ) {
                                                Text(
                                                    text = booking.status,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCompleted) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.onErrorContainer
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = booking.serviceName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = (if (onboardingLanguage == "ru") "Мастер: " else if (onboardingLanguage == "es") "Maestro: " else "Master: ") + booking.masterName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (booking.status == "Подтверждена") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { selectedBookingForQr = booking },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .defaultMinSize(minHeight = 48.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("QR-билет", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { CalendarSyncHelper.exportBookingToCalendar(context, booking) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .defaultMinSize(minHeight = 48.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Календарь", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { bookingToReschedule = booking },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .testTag("reschedule_booking_btn_${booking.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Перенести", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    LocationGeocoderHelper.openMapsDirections(
                                                        context = context,
                                                        label = booking.masterName,
                                                        address = matchedMaster?.address ?: booking.masterName,
                                                        lat = matchedMaster?.lat ?: 41.6150,
                                                        lon = matchedMaster?.lon ?: 2.6620
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .testTag("directions_booking_btn_${booking.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Маршрут", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                viewModel.repeatBooking(booking)
                                                onDismiss()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("repeat_booking_btn_${booking.id}"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Повторить запись", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (selectedFilterTab == 1) {
                                    "История записей пока пуста"
                                } else if (onboardingLanguage == "ru") {
                                    "Нет предстоящих записей.\nЗапишитесь к нашим лучшим мастерам! 💅"
                                } else {
                                    "No upcoming bookings yet.\nBook your next premium service! 💅"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (onboardingLanguage == "ru") "Закрыть" else "Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )

    selectedBookingForQr?.let { qrBooking ->
        val matchedMaster = mastersList.find { it.name.equals(qrBooking.masterName, ignoreCase = true) }
        SovereignQrTicketDialog(
            booking = qrBooking,
            masterAddress = matchedMaster?.address.orEmpty(),
            masterLat = matchedMaster?.lat ?: 41.6150,
            masterLon = matchedMaster?.lon ?: 2.6620,
            onDismiss = { selectedBookingForQr = null }
        )
    }

    bookingToReschedule?.let { targetBooking ->
        RescheduleBookingDialog(
            booking = targetBooking,
            viewModel = viewModel,
            onDismiss = { bookingToReschedule = null }
        )
    }
}

@Composable
private fun RescheduleBookingDialog(
    booking: Booking,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val allBookings by viewModel.bookings.collectAsStateWithLifecycle()
    var chosenDate by remember(booking.id) { mutableStateOf(booking.date) }
    var chosenSlot by remember(booking.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Перенос визита", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${booking.serviceName} • ${booking.masterName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Новая дата:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.dates) { date ->
                        val selected = chosenDate == date
                        Card(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable {
                                    chosenDate = date
                                    chosenSlot = null
                                }
                                .testTag("reschedule_date_$date"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(date, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text("Свободное время:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(124.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.timeSlots) { slot ->
                        val isOccupied = allBookings.any { existing ->
                            existing.id != booking.id &&
                                existing.status != "Отменена" &&
                                existing.masterName.equals(booking.masterName, ignoreCase = true) &&
                                existing.date == chosenDate &&
                                SmartSchedulerEngine.isSlotOverlappingWithExistingBooking(
                                    targetSlot = slot,
                                    targetDurationMinutes = 75,
                                    bookedSlot = existing.timeSlot,
                                    bookedDurationMinutes = 90
                                )
                        }
                        val isCurrent = chosenDate == booking.date && slot == booking.timeSlot
                        val isSelected = chosenSlot == slot
                        Card(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable(enabled = !isOccupied && !isCurrent) { chosenSlot = slot }
                                .testTag("reschedule_slot_$slot"),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    isOccupied || isCurrent -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = slot,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (isOccupied) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (isOccupied || isCurrent) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val slot = chosenSlot ?: return@Button
                    viewModel.rescheduleBooking(booking, chosenDate, slot)
                    onDismiss()
                },
                enabled = chosenSlot != null,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("confirm_reschedule_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Перенести")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SovereignQrTicketDialog(
    booking: Booking,
    masterAddress: String = "",
    masterLat: Double = 41.6150,
    masterLon: Double = 2.6620,
    onDismiss: () -> Unit
) {
    val ticket = remember(booking) { VisitTicketCryptoManager.createTicket(booking) }
    val qrBitmap = remember(ticket) { VisitTicketCryptoManager.generateQrBitmap(ticket.rawPayload, 480) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Суверенный билет визита", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Предъявите этот QR-код (ISO/IEC 18004) мастеру при входе для бесконтактного чекина.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code визита",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "SHA-256 Token: ${ticket.securityToken}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Услуга:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(ticket.serviceName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Дата и время:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${ticket.date} • ${ticket.timeSlot}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Мастер:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(ticket.masterName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("К оплате:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${ticket.price} €", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        CalendarSyncHelper.exportBookingToCalendar(context, booking)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Синхронизировать с Календарём")
                }

                OutlinedButton(
                    onClick = {
                        LocationGeocoderHelper.openMapsDirections(
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
                        .testTag("qr_directions_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Маршрут в студию")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Закрыть")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
