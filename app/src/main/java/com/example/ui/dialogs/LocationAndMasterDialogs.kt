package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder
import com.example.ui.viewmodel.ClientProfile
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MasterProfile

import com.example.util.LocationGeocoderHelper

@Composable
fun CustomizeLocationDialog(
    client: ClientProfile,
    currentLang: String,
    skinCode: String,
    isDarkTheme: Boolean,
    onLangChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var homeAddr by remember { mutableStateOf(client.homeAddress) }
    var homeLatStr by remember { mutableStateOf(client.homeLat.toString()) }
    var homeLonStr by remember { mutableStateOf(client.homeLon.toString()) }
    var isGeocodingHome by remember { mutableStateOf(false) }

    var workAddr by remember { mutableStateOf(client.workAddress) }
    var workLatStr by remember { mutableStateOf(client.workLat.toString()) }
    var workLonStr by remember { mutableStateOf(client.workLon.toString()) }
    var isGeocodingWork by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⚙️ Настройка координат клиента",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Измените ваши адреса и GPS координаты, чтобы пересчитать шаги и отображение на интерактивной карте.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Text(
                    "🏠 ДОМАШНИЙ АДРЕС",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = homeAddr,
                    onValueChange = { homeAddr = it },
                    label = { Text("Адрес дома") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_home_address")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = homeLatStr,
                        onValueChange = { homeLatStr = it },
                        label = { Text("Latitude (Широта)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_home_lat")
                    )
                    OutlinedTextField(
                        value = homeLonStr,
                        onValueChange = { homeLonStr = it },
                        label = { Text("Longitude (Долгота)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_home_lon")
                    )
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isGeocodingHome = true
                            LocationGeocoderHelper.geocodeAddress(context, homeAddr)
                                .onSuccess { (lat, lon) ->
                                    homeLatStr = String.format(Locale.US, "%.5f", lat)
                                    homeLonStr = String.format(Locale.US, "%.5f", lon)
                                    Toast.makeText(context, "Координаты дома найдены 📍", Toast.LENGTH_SHORT).show()
                                }
                                .onFailure { err ->
                                    Toast.makeText(context, err.localizedMessage ?: "Не удалось найти адрес", Toast.LENGTH_SHORT).show()
                                }
                            isGeocodingHome = false
                        }
                    },
                    enabled = !isGeocodingHome && homeAddr.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("geocode_home_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isGeocodingHome) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Поиск координат...")
                    } else {
                        Icon(Icons.Default.LocationSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Найти по адресу 📍")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "💼 РАБОЧИЙ АДРЕС",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = workAddr,
                    onValueChange = { workAddr = it },
                    label = { Text("Адрес работы") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_work_address")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = workLatStr,
                        onValueChange = { workLatStr = it },
                        label = { Text("Latitude (Широта)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_work_lat")
                    )
                    OutlinedTextField(
                        value = workLonStr,
                        onValueChange = { workLonStr = it },
                        label = { Text("Longitude (Долгота)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_work_lon")
                    )
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isGeocodingWork = true
                            LocationGeocoderHelper.geocodeAddress(context, workAddr)
                                .onSuccess { (lat, lon) ->
                                    workLatStr = String.format(Locale.US, "%.5f", lat)
                                    workLonStr = String.format(Locale.US, "%.5f", lon)
                                    Toast.makeText(context, "Координаты работы найдены 📍", Toast.LENGTH_SHORT).show()
                                }
                                .onFailure { err ->
                                    Toast.makeText(context, err.localizedMessage ?: "Не удалось найти адрес", Toast.LENGTH_SHORT).show()
                                }
                            isGeocodingWork = false
                        }
                    },
                    enabled = !isGeocodingWork && workAddr.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("geocode_work_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isGeocodingWork) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Поиск координат...")
                    } else {
                        Icon(Icons.Default.LocationSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Найти по адресу 📍")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()

                Text(
                    text = when (currentLang) {
                        "ru" -> "🌐 ЯЗЫК ПРИЛОЖЕНИЯ"
                        "es" -> "🌐 IDIOMA DE LA APLICACIÓN"
                        else -> "🌐 APP LANGUAGE"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ru" to "🇷🇺 RU",
                        "en" to "🇬🇧 EN",
                        "es" to "🇪🇸 ES"
                    ).forEach { (code, label) ->
                        val isSelected = currentLang == code
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable { onLangChange(code) }
                                .skinWaveBorder(skinCode = skinCode, alpha = if (isSelected) 0.55f else 0.1f)
                                .let {
                                    if (isSelected) it.skinOrnament(skinCode = skinCode, isButton = false, alphaMultiplier = 0.8f, isDark = isDarkTheme) else it
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = {
                            val hLat = homeLatStr.toDoubleOrNull() ?: client.homeLat
                            val hLon = homeLonStr.toDoubleOrNull() ?: client.homeLon
                            val wLat = workLatStr.toDoubleOrNull() ?: client.workLat
                            val wLon = workLonStr.toDoubleOrNull() ?: client.workLon
                            onSave(homeAddr, hLat, hLon, workAddr, wLat, wLon)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Сохранить", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MasterDetailsSheet(
    master: MasterProfile,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reviews by viewModel.reviewsList.collectAsStateWithLifecycle()
    val parsedInstagramPosts by viewModel.parsedInstagramPosts.collectAsStateWithLifecycle()
    val selectedDisplayPostIds by viewModel.selectedDisplayPostIds.collectAsStateWithLifecycle()
    val clientName by viewModel.clientName.collectAsStateWithLifecycle()

    val masterReviews = reviews.filter { it.targetId == master.name || it.targetId == master.id.toString() }

    var userRating by remember { mutableIntStateOf(5) }
    var userComment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Карточка Мастера",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close detailed view")
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        master.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(master.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${master.rating} (${masterReviews.size} отзывов)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("🏠 Локация кабинета: ${master.address.ifBlank { "Уточняется" }}", style = MaterialTheme.typography.bodyMedium)
                            Text("⏰ Режим приема: ${master.workingSchedule}", style = MaterialTheme.typography.bodyMedium)
                            Text("🗣️ Языки обслуживания: ${master.languages.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                            Text("🏅 Стаж: ${master.experienceYears} лет сервиса", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    item {
                        Column {
                            Text(
                                text = "📸 Портфолио (@${master.instagramUsername.ifBlank { "nailix" }})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (parsedInstagramPosts.isEmpty()) {
                                Text(
                                    "Работы ещё не загружены. Перейдите во вкладку «Галерея» для импорта из Instagram или фотогалереи.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                val displayedPosts = if (selectedDisplayPostIds.isEmpty()) {
                                    parsedInstagramPosts.take(5)
                                } else {
                                    parsedInstagramPosts.filter { it.id in selectedDisplayPostIds }
                                }
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(displayedPosts) { post ->
                                        Card(
                                            modifier = Modifier.width(140.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column {
                                                AsyncImage(
                                                    model = post.imageUrl,
                                                    contentDescription = "Portfolio photo",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(100.dp),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Column(modifier = Modifier.padding(6.dp)) {
                                                    Text("❤️ ${post.likesCount}", style = MaterialTheme.typography.labelSmall)
                                                    Text(post.caption, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "💬 Отзывы клиентов (${masterReviews.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (masterReviews.isEmpty()) {
                        item {
                            Text("Отзывов пока нет. Будьте первыми, кто оставит оценку!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(masterReviews) { review ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(review.authorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        Row {
                                            (1..review.rating).forEach {
                                                Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(review.comment, style = MaterialTheme.typography.bodySmall)
                                    Text(review.dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Оценить мастера ${master.name}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    (1..5).forEach { star ->
                                        IconButton(
                                            onClick = { userRating = star },
                                            modifier = Modifier.testTag("rate_master_star_$star")
                                        ) {
                                            Icon(
                                                imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "$star star selection",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = userComment,
                                    onValueChange = { userComment = it },
                                    label = { Text("Ваш отзыв") },
                                    placeholder = { Text("Поделитесь впечатлениями о мастере...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("master_review_comment_input")
                                )

                                Button(
                                    onClick = {
                                        if (userComment.isBlank()) {
                                            Toast.makeText(context, "Пожалуйста, введите комментарий", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val author = if (clientName.isBlank()) "Клиент NailiX" else clientName
                                            viewModel.leaveFeedback(master.name, "master", userRating, userComment, author)
                                            Toast.makeText(context, "Спасибо за отзыв! Он сохранен 🎉", Toast.LENGTH_SHORT).show()
                                            userComment = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("master_review_submit")
                                ) {
                                    Text("Опубликовать")
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                LocationGeocoderHelper.openMapsDirections(
                                    context = context,
                                    label = master.name,
                                    address = master.address,
                                    lat = master.lat,
                                    lon = master.lon
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.85f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("sheet_directions_${master.id}")
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Маршрут", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Button(
                            onClick = {
                                viewModel.selectMaster(master.name)
                                viewModel.navigateTo("booking")
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.25f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("sheet_book_${master.id}")
                        ) {
                            Text("Записаться", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
