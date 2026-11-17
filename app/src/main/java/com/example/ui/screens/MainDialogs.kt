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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
fun CustomizeLocationDialog(
    client: ClientProfile,
    currentLang: String,
    skinCode: String,
    isDarkTheme: Boolean,
    onLangChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, String, Double, Double) -> Unit
) {
    var homeAddr by remember { mutableStateOf(client.homeAddress) }
    var homeLatStr by remember { mutableStateOf(client.homeLat.toString()) }
    var homeLonStr by remember { mutableStateOf(client.homeLon.toString()) }

    var workAddr by remember { mutableStateOf(client.workAddress) }
    var workLatStr by remember { mutableStateOf(client.workLat.toString()) }
    var workLonStr by remember { mutableStateOf(client.workLon.toString()) }

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
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⚙️ Настройка координат клиента",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text("Измените ваши адреса и GPS координаты, чтобы пересчитать шаги и отображение на интерактивной карте.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                HorizontalDivider()

                Text("🏠 ДОМАШНИЙ АДРЕС", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                OutlinedTextField(
                    value = homeAddr,
                    onValueChange = { homeAddr = it },
                    label = { Text("Адрес дома") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_home_address")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = homeLatStr,
                        onValueChange = { homeLatStr = it },
                        label = { Text("Latitude (Широта)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("edit_home_lat")
                    )
                    OutlinedTextField(
                        value = homeLonStr,
                        onValueChange = { homeLonStr = it },
                        label = { Text("Longitude (Долгота)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("edit_home_lon")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("💼 РАБОЧИЙ АДРЕС", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                OutlinedTextField(
                    value = workAddr,
                    onValueChange = { workAddr = it },
                    label = { Text("Адрес работы") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_work_address")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = workLatStr,
                        onValueChange = { workLatStr = it },
                        label = { Text("Latitude (Широта)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("edit_work_lat")
                    )
                    OutlinedTextField(
                        value = workLonStr,
                        onValueChange = { workLonStr = it },
                        label = { Text("Longitude (Долгота)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("edit_work_lon")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (currentLang == "ru") "🌐 ЯЗЫК ПРИЛОЖЕНИЯ" else if (currentLang == "es") "🌐 IDIOMA DE LA APLICACIÓN" else "🌐 APP LANGUAGE",
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
                                .clickable { onLangChange(code) }
                                .skinWaveBorder(skinCode = skinCode, alpha = if (isSelected) 0.55f else 0.1f)
                                .let { if (isSelected) it.skinOrnament(skinCode = skinCode, isButton = false, alphaMultiplier = 0.8f, isDark = isDarkTheme) else it },
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
                                    .padding(vertical = 8.dp),
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
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
                        modifier = Modifier.weight(1.2f),
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
fun GlobalSettingsDialog(
    viewModel: MainViewModel,
    currentSkin: String,
    currentCurrency: String,
    currentLanguage: String,
    isDarkTheme: Boolean,
    isPremium: Boolean,
    currentAdSource: String,
    onAdSourceChange: (String) -> Unit,
    onTogglePremium: () -> Unit,
    onSkinChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val localizationMap by viewModel.localizationMap.collectAsStateWithLifecycle()
    val localText = { key: String, default: String -> localizationMap[key] ?: default }
    var paywallSkinCode by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .skinOrnament(skinCode = currentSkin, alphaMultiplier = 0.3f, isDark = isDarkTheme)
                .skinWaveBorder(skinCode = currentSkin, alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = localText("global_settings", "Global Settings"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = localText("interface_language", "Interface Language"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val languages1 = listOf(
                        "ru" to "🇷🇺 RU",
                        "en" to "🇬🇧 EN",
                        "es" to "🇪🇸 ES",
                        "zh" to "🇨🇳 ZH",
                        "fr" to "🇫🇷 FR"
                    )
                    val languages2 = listOf(
                        "de" to "🇩🇪 DE",
                        "pt" to "🇵🇹 PT",
                        "it" to "🇮🇹 IT",
                        "tr" to "🇹🇷 TR",
                        "ja" to "🇯🇵 JA"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            languages1.forEach { (code, label) ->
                                val isSelected = currentLanguage == code
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onLanguageChange(code) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            languages2.forEach { (code, label) ->
                                val isSelected = currentLanguage == code
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onLanguageChange(code) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = localText("main_currency", "Main Currency"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EUR", "USD", "RUB").forEach { curr ->
                            val isSelected = currentCurrency == curr
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onCurrencyChange(curr) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(curr, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (currentLanguage == "ru") "Базовая тема" else if (currentLanguage == "es") "Tema base" else "Base Theme",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            false to (if (currentLanguage == "ru") "☀️ Светлая" else if (currentLanguage == "es") "☀️ Claro" else "☀️ Light"),
                            true to (if (currentLanguage == "ru") "🌙 Тёмная" else if (currentLanguage == "es") "🌙 Oscuro" else "🌙 Dark")
                        ).forEach { (isDark, label) ->
                            val isSelected = isDarkTheme == isDark
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onDarkThemeChange(isDark) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (!isPremium) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (currentLanguage == "ru") "Источник рекламы (для бесплатной версии)" 
                                   else if (currentLanguage == "es") "Origen de anuncios" 
                                   else "Ad Source (Free Version)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "google" to (if (currentLanguage == "ru") "Google Ads 🌐" else "Google Ads 🌐"),
                                "custom" to (if (currentLanguage == "ru") "Спонсорская 💅" else "Sponsored 💅")
                            ).forEach { (sourceCode, label) ->
                                val isSelected = currentAdSource == sourceCode
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onAdSourceChange(sourceCode) }
                                        .testTag("ad_source_chip_$sourceCode"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (currentLanguage == "ru") "Выберите скин" else if (currentLanguage == "es") "Seleccionar skin" else "Select Skin",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val skins = listOf(
                        "gaudi" to ("Гауди 🎨" to Color(0xFF1D539F)),
                        "barcelona" to ("Барселона 🇪🇸" to Color(0xFFF7B538)),
                        "van_gogh" to ("Ван Гог 🌻" to Color(0xFFFCD34D)),
                        "amsterdam" to ("Амстердам 🌷" to Color(0xFF059669)),
                        "moscow" to ("Москва 🔴" to Color(0xFFD97706)),
                        "paris" to ("Париж 🗼" to Color(0xFFEC4899)),
                        "nails" to ("Nail Art 💅" to Color(0xFFF43F5E)),
                        "flowers" to ("Цветы 🌸" to Color(0xFFEC4899)),
                        "cosmos" to ("Космос 🌌" to Color(0xFF06B6D4)),
                        "esoteric" to ("Эзотерика 🔮" to Color(0xFFD4AF37))
                    )

                    Box(modifier = Modifier.height(180.dp)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(skins) { (skinCode, info) ->
                                val (name, color) = info
                                val isSelected = currentSkin == skinCode
                                val isFreeSkin = skinCode == "gaudi" || skinCode == "barcelona" || skinCode == "van_gogh"
                                val isLocked = !isFreeSkin && !isPremium

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isLocked) {
                                                paywallSkinCode = skinCode
                                            } else {
                                                onSkinChange(skinCode)
                                            }
                                        }
                                        .skinOrnament(skinCode = skinCode, isButton = true, alphaMultiplier = 0.5f, isDark = isDarkTheme),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            if (isLocked) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        viewModel.resetOnboarding()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("replay_onboarding_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == "ru") "🌟 Пройти тур по приложению (Онбординг)"
                               else if (currentLanguage == "es") "🌟 Ver tour de bienvenida"
                               else "🌟 Replay Onboarding Tour",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (currentLanguage == "ru") "Готово" else if (currentLanguage == "es") "Listo" else "Done", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (paywallSkinCode != null) {
            val skins = listOf(
                "gaudi" to ("Гауди 🎨" to Color(0xFF1D539F)),
                "barcelona" to ("Барселона 🇪🇸" to Color(0xFFF7B538)),
                "van_gogh" to ("Ван Гог 🌻" to Color(0xFFFCD34D)),
                "amsterdam" to ("Амстердам 🌷" to Color(0xFF059669)),
                "moscow" to ("Москва 🔴" to Color(0xFFD97706)),
                "paris" to ("Париж 🗼" to Color(0xFFEC4899)),
                "nails" to ("Nail Art 💅" to Color(0xFFF43F5E)),
                "flowers" to ("Цветы 🌸" to Color(0xFFEC4899)),
                "cosmos" to ("Космос 🌌" to Color(0xFF06B6D4)),
                "esoteric" to ("Эзотерика 🔮" to Color(0xFFD4AF37))
            )
            val lockedSkinName = skins.firstOrNull { it.first == paywallSkinCode }?.second?.first ?: paywallSkinCode
            AlertDialog(
                onDismissRequest = { paywallSkinCode = null },
                title = {
                    Text(
                        text = if (currentLanguage == "ru") "👑 Разблокировать PRO стиль!" 
                               else if (currentLanguage == "es") "👑 ¡Desbloquear estilo PRO!" 
                               else "👑 Unlock PRO Style!",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (currentLanguage == "ru") "Стиль \"$lockedSkinName\" — это премиум-дизайн.\n\nРазблокируйте все 10 эксклюзивных дизайнерских тем (Амстердам, Москва, Париж, Nail Art, Цветы, Космос и Эзотерика) и увеличьте лимит профилей по подписке PRO всего за $5.00/месяц!"
                               else if (currentLanguage == "es") "El estilo \"$lockedSkinName\" es un diseño premium.\n\n¡Desbloquea los 10 temas de diseño exclusivos (Ámsterdam, Moscú, París, Uñas, Flores, Cosmos y Esotérico) y amplía tu límite de perfiles con la suscripción PRO por solo $5.00/mes!"
                               else "The style \"$lockedSkinName\" is a premium design.\n\nUnlock all 10 exclusive designer themes (Amsterdam, Moscow, Paris, Nails, Flowers, Cosmos, and Esoteric) and expand your profile limit by upgrading to PRO for just $5.00/month!"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onTogglePremium() // Activate Premium!
                            paywallSkinCode?.let { onSkinChange(it) } // Select the skin!
                            paywallSkinCode = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (currentLanguage == "ru") "Активировать PRO за $5/мес" 
                                   else if (currentLanguage == "es") "Activar PRO por $5/mes" 
                                   else "Activate PRO for $5/mo",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { paywallSkinCode = null }) {
                        Text(
                            text = if (currentLanguage == "ru") "Отмена" 
                                   else if (currentLanguage == "es") "Cancelar" 
                                   else "Cancel"
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
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

    var userRating by remember { mutableStateOf(5) }
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
                // Header with custom title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Карточка Мастера", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                    // Bio & Stats
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
                                    Text(master.name.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
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

                            Text("🏠 Локация кабинета:Spain, Calella, Moragas 74", style = MaterialTheme.typography.bodyMedium)
                            Text("⏰ Режим приема: ${master.workingSchedule}", style = MaterialTheme.typography.bodyMedium)
                            Text("🗣️ Языки обслуживания: ${master.languages.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                            Text("🏅 Стаж: ${master.experienceYears} лет безупречного сервиса", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Instagram Live Portfolio Grid
                    item {
                        Column {
                            Text(
                                text = "📸 Портфолио (Instagram live @${master.instagramUsername})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (parsedInstagramPosts.isEmpty()) {
                                Text("Работы импортируются... Перейдите во вкладку 'Галерея' для ручного парсинга.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text("❤️ ${post.likesCount}", style = MaterialTheme.typography.labelSmall)
                                                        Text("💬 ${post.likesCount / 4 + 1}", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    Text(post.caption, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reviews List block
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

                    // Leave review form
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Оценить мастера ${master.name}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                
                                // Star selections
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    (1..5).forEach { star ->
                                        Icon(
                                            imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "$star star selection",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable { userRating = star }
                                                .testTag("rate_master_star_$star")
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = userComment,
                                    onValueChange = { userComment = it },
                                    label = { Text("Ваш отзыв") },
                                    placeholder = { Text("Поделитесь впечатлениями о мастере...") },
                                    modifier = Modifier.fillMaxWidth().testTag("master_review_comment_input")
                                )

                                Button(
                                    onClick = {
                                        if (userComment.isBlank()) {
                                            Toast.makeText(context, "Пожалуйста, введите комментарий", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val author = if (clientName.isBlank()) "Аноним" else clientName
                                            viewModel.leaveFeedback(master.name, "master", userRating, userComment, author)
                                            Toast.makeText(context, "Спасибо за отзыв! Он сразу опубликован 🎉", Toast.LENGTH_SHORT).show()
                                            userComment = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End).testTag("master_review_submit")
                                ) {
                                    Text("Опубликовать")
                                }
                            }
                        }
                    }
                }

                // Call to action booking
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.selectMaster(master.name)
                                viewModel.navigateTo("booking")
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("sheet_book_${master.id}")
                        ) {
                            Text("Записаться к мастеру", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ProfileManagerDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()

    val savedClients by viewModel.savedClientProfiles.collectAsStateWithLifecycle()
    val savedMasters by viewModel.savedMasterProfiles.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val primaryClientPhone by viewModel.primaryClientPhone.collectAsStateWithLifecycle()
    val primaryMasterId by viewModel.primaryMasterId.collectAsStateWithLifecycle()
    val activeRole by viewModel.userRole.collectAsStateWithLifecycle()

    val activeClientProfile by viewModel.clientProfile.collectAsStateWithLifecycle()
    val activeMasterProfile by viewModel.registeredMasterProfile.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(if (activeRole == "master") 1 else 0) } // 0: Clients, 1: Masters
    var showCreateForm by remember { mutableStateOf(false) }

    // Client Form fields
    var newClientName by remember { mutableStateOf("") }
    var newClientPhone by remember { mutableStateOf("") }
    var newClientEmail by remember { mutableStateOf("") }

    // Master Form fields
    var newMasterName by remember { mutableStateOf("") }
    var newMasterAddress by remember { mutableStateOf("") }
    var newMasterSchedule by remember { mutableStateOf("") }
    var newMasterExp by remember { mutableStateOf("1") }
    var newMasterInsta by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Profile icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (onboardingLanguage == "ru") "Менеджер профилей" else "Profile Manager",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Premium subscription indicator & switch simulator
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPremium) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isPremium) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else Color.Gray.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (onboardingLanguage == "ru") "Тарифный план:" else "Subscription Tier:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isPremium) {
                                        if (onboardingLanguage == "ru") "👑 Премиум (Лимит: 3+3)" else "👑 Premium (Limit: 3+3)"
                                    } else {
                                        if (onboardingLanguage == "ru") "❄️ Бесплатный (Лимит: 1+1)" else "❄️ Free (Limit: 1+1)"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.togglePremium()
                                    val msg = if (onboardingLanguage == "ru") {
                                        if (!isPremium) "Премиум активирован! Лимит увеличен до 3+3." else "Премиум отключен! Личные профили заморожены."
                                    } else {
                                        if (!isPremium) "Premium activated! Limit increased to 3+3." else "Premium deactivated! Extra profiles frozen."
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPremium) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isPremium) {
                                        if (onboardingLanguage == "ru") "Отключить" else "Disable"
                                    } else {
                                        if (onboardingLanguage == "ru") "Включить" else "Enable"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs (Client / Master)
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = {
                            activeTab = 0
                            showCreateForm = false
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (onboardingLanguage == "ru") "Клиенты (${savedClients.size})" else "Clients (${savedClients.size})",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Tab(
                        selected = activeTab == 1,
                        onClick = {
                            activeTab = 1
                            showCreateForm = false
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (onboardingLanguage == "ru") "Мастера (${savedMasters.size})" else "Masters (${savedMasters.size})",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!showCreateForm) {
                    // Profile List
                    val maxProfiles = if (isPremium) 3 else 1

                    // Info banner about frozen profiles
                    if (!isPremium && ((activeTab == 0 && savedClients.size > 1) || (activeTab == 1 && savedMasters.size > 1))) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (onboardingLanguage == "ru") {
                                    "⚠️ Подписка неактивна. Только основной (primary) профиль доступен для выбора. Остальные заморожены без удаления данных."
                                } else {
                                    "⚠️ Subscription inactive. Only primary profile is accessible. Others are frozen without data deletion."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (activeTab == 0) {
                            // Client profile list
                            savedClients.forEach { profile ->
                                val isPrimary = (profile.phone == primaryClientPhone)
                                val isActive = (profile.phone == activeClientProfile.phone && activeRole == "client")
                                val isFrozen = (!isPremium && !isPrimary && savedClients.size > 1)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            if (isFrozen) {
                                                val msg = if (onboardingLanguage == "ru") {
                                                    "Профиль ${profile.name} заморожен. Включите Премиум для разблокировки!"
                                                } else {
                                                    "Profile ${profile.name} is frozen. Enable Premium to unlock!"
                                                }
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            } else {
                                                viewModel.activateClientProfile(profile.phone)
                                                val msg = if (onboardingLanguage == "ru") {
                                                    "Переключено на клиента: ${profile.name}"
                                                } else {
                                                    "Switched to client: ${profile.name}"
                                                }
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isFrozen) 0.5f else 1.0f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (isFrozen) Icons.Default.Lock else Icons.Default.Person,
                                                contentDescription = "Profile icon",
                                                tint = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = profile.name,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (isPrimary) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Primary",
                                                            tint = Color(0xFFFFB300),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = profile.phone,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isFrozen) {
                                                Text(
                                                    text = if (onboardingLanguage == "ru") "Заморожен ❄️" else "Frozen ❄️",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )
                                            } else if (!isPrimary) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.setPrimaryClientProfile(profile.phone)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.StarBorder,
                                                        contentDescription = "Set primary",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Master profile list
                            savedMasters.forEach { profile ->
                                val isPrimary = (profile.id == primaryMasterId)
                                val isActive = (activeMasterProfile?.id == profile.id && activeRole == "master")
                                val isFrozen = (!isPremium && !isPrimary && savedMasters.size > 1)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            if (isFrozen) {
                                                val msg = if (onboardingLanguage == "ru") {
                                                    "Профиль мастера ${profile.name} заморожен. Включите Премиум для разблокировки!"
                                                } else {
                                                    "Master profile ${profile.name} is frozen. Enable Premium to unlock!"
                                                }
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            } else {
                                                viewModel.activateMasterProfile(profile.id)
                                                val msg = if (onboardingLanguage == "ru") {
                                                    "Переключено на мастера: ${profile.name}"
                                                } else {
                                                    "Switched to master: ${profile.name}"
                                                }
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isFrozen) 0.5f else 1.0f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (isFrozen) Icons.Default.Lock else Icons.Default.Face,
                                                contentDescription = "Profile icon",
                                                tint = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = profile.name,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (isPrimary) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Primary",
                                                            tint = Color(0xFFFFB300),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "@${profile.instagramUsername}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isFrozen) {
                                                Text(
                                                    text = if (onboardingLanguage == "ru") "Заморожен ❄️" else "Frozen ❄️",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )
                                            } else if (!isPrimary) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.setPrimaryMasterProfile(profile.id)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.StarBorder,
                                                        contentDescription = "Set primary",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Add Profile" action
                    val size = if (activeTab == 0) savedClients.size else savedMasters.size
                    if (size < maxProfiles) {
                        Button(
                            onClick = { showCreateForm = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (onboardingLanguage == "ru") "Создать новый профиль" else "Create New Profile",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Warn about reaching limit
                        Text(
                            text = if (size >= 3) {
                                if (onboardingLanguage == "ru") "Достигнут максимальный лимит (3 профиля)" else "Maximum limit reached (3 profiles)"
                            } else {
                                if (onboardingLanguage == "ru") "Для создания до 3-х профилей включите Премиум!" else "Enable Premium to create up to 3 profiles!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (size >= 3) Color.Gray else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                } else {
                    // Create Form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (onboardingLanguage == "ru") "Новый профиль" else "New Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (activeTab == 0) {
                            // Client fields
                            OutlinedTextField(
                                value = newClientName,
                                onValueChange = { newClientName = it },
                                label = { Text(if (onboardingLanguage == "ru") "Имя" else "Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newClientPhone,
                                onValueChange = { newClientPhone = it },
                                label = { Text(if (onboardingLanguage == "ru") "Телефон" else "Phone") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newClientEmail,
                                onValueChange = { newClientEmail = it },
                                label = { Text("Email (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        } else {
                            // Master fields
                            OutlinedTextField(
                                value = newMasterName,
                                onValueChange = { newMasterName = it },
                                label = { Text(if (onboardingLanguage == "ru") "Имя" else "Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newMasterAddress,
                                onValueChange = { newMasterAddress = it },
                                label = { Text(if (onboardingLanguage == "ru") "Адрес салона" else "Salon Address") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newMasterSchedule,
                                onValueChange = { newMasterSchedule = it },
                                label = { Text(if (onboardingLanguage == "ru") "График работы" else "Schedule") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Пн-Пт: 09:00 - 18:00") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newMasterExp,
                                onValueChange = { newMasterExp = it },
                                label = { Text(if (onboardingLanguage == "ru") "Опыт работы (лет)" else "Experience (years)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newMasterInsta,
                                onValueChange = { newMasterInsta = it },
                                label = { Text("Instagram Username") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCreateForm = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (onboardingLanguage == "ru") "Назад" else "Back")
                            }

                            Button(
                                onClick = {
                                    if (activeTab == 0) {
                                        if (newClientName.isBlank() || newClientPhone.isBlank()) {
                                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val err = viewModel.createNewClientProfile(newClientName, newClientPhone, newClientEmail)
                                        if (err == "DUPLICATE") {
                                            Toast.makeText(context, "Profile with this phone already exists!", Toast.LENGTH_LONG).show()
                                        } else if (err == "PREMIUM_REQUIRED") {
                                            Toast.makeText(context, "Premium subscription required to create more profiles!", Toast.LENGTH_LONG).show()
                                        } else if (err == "MAX_REACHED") {
                                            Toast.makeText(context, "Limit reached (max 3 profiles)", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Client profile created!", Toast.LENGTH_SHORT).show()
                                            showCreateForm = false
                                            newClientName = ""
                                            newClientPhone = ""
                                            newClientEmail = ""
                                        }
                                    } else {
                                        if (newMasterName.isBlank() || newMasterAddress.isBlank() || newMasterInsta.isBlank()) {
                                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val expInt = newMasterExp.toIntOrNull() ?: 1
                                        val err = viewModel.createNewMasterProfile(
                                            newMasterName,
                                            newMasterAddress,
                                            newMasterSchedule,
                                            expInt,
                                            listOf("Испанский", "Русский"),
                                            newMasterInsta
                                        )
                                        if (err == "DUPLICATE") {
                                            Toast.makeText(context, "Profile with this Instagram already exists!", Toast.LENGTH_LONG).show()
                                        } else if (err == "PREMIUM_REQUIRED") {
                                            Toast.makeText(context, "Premium subscription required to create more profiles!", Toast.LENGTH_LONG).show()
                                        } else if (err == "MAX_REACHED") {
                                            Toast.makeText(context, "Limit reached (max 3 profiles)", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Master profile created!", Toast.LENGTH_SHORT).show()
                                            showCreateForm = false
                                            newMasterName = ""
                                            newMasterAddress = ""
                                            newMasterSchedule = ""
                                            newMasterExp = "1"
                                            newMasterInsta = ""
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (onboardingLanguage == "ru") "Создать" else "Create")
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun StartupProfileSelectorDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val savedClients by viewModel.savedClientProfiles.collectAsStateWithLifecycle()
    val savedMasters by viewModel.savedMasterProfiles.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val primaryClientPhone by viewModel.primaryClientPhone.collectAsStateWithLifecycle()
    val primaryMasterId by viewModel.primaryMasterId.collectAsStateWithLifecycle()

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (onboardingLanguage == "ru") "Выберите рабочий профиль" else "Select Work Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (onboardingLanguage == "ru") {
                        "На вашем устройстве сохранено несколько профилей. Выберите нужный при запуске приложения."
                    } else {
                        "Multiple profiles are saved on this device. Select which one you want to use right now."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (savedClients.isNotEmpty()) {
                        Text(
                            text = if (onboardingLanguage == "ru") "Клиенты" else "Clients",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        savedClients.forEach { profile ->
                            val isPrimary = (profile.phone == primaryClientPhone)
                            val isFrozen = (!isPremium && !isPrimary && savedClients.size > 1)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (!isFrozen) {
                                            viewModel.activateClientProfile(profile.phone)
                                            onDismiss()
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isFrozen) 0.5f else 1.0f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isFrozen) Icons.Default.Lock else Icons.Default.Person,
                                            contentDescription = "Profile icon",
                                            tint = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = profile.name,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isPrimary) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Primary",
                                                        tint = Color(0xFFFFB300),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = profile.phone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    if (isFrozen) {
                                        Text(
                                            text = if (onboardingLanguage == "ru") "❄️ Заморожен" else "❄️ Frozen",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (savedMasters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (onboardingLanguage == "ru") "Мастера" else "Masters",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        savedMasters.forEach { profile ->
                            val isPrimary = (profile.id == primaryMasterId)
                            val isFrozen = (!isPremium && !isPrimary && savedMasters.size > 1)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (!isFrozen) {
                                            viewModel.activateMasterProfile(profile.id)
                                            onDismiss()
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isFrozen) 0.5f else 1.0f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isFrozen) Icons.Default.Lock else Icons.Default.Face,
                                            contentDescription = "Profile icon",
                                            tint = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = profile.name,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isPrimary) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Primary",
                                                        tint = Color(0xFFFFB300),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "@${profile.instagramUsername}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    if (isFrozen) {
                                        Text(
                                            text = if (onboardingLanguage == "ru") "❄️ Заморожен" else "❄️ Frozen",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (onboardingLanguage == "ru") "Закрыть" else "Dismiss", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioSelectorDialog(
    viewModel: com.example.ui.viewmodel.MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val selectedStation by viewModel.selectedStation.collectAsStateWithLifecycle()
    val radioIsPlaying by viewModel.radioIsPlaying.collectAsStateWithLifecycle()
    val radioIsLoading by viewModel.radioIsLoading.collectAsStateWithLifecycle()
    val stations = viewModel.topRadioStations
    
    val playInteractionSource = remember { MutableInteractionSource() }
    val isPlayPressed by playInteractionSource.collectIsPressedAsState()
    val playScale by animateFloatAsState(if (isPlayPressed) 0.88f else 1.0f, label = "dialog_play_scale")
    val playBgColor = if (radioIsPlaying) {
        if (isPlayPressed) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
    } else {
        if (isPlayPressed) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
    }
    val playContentColor = if (radioIsPlaying) Color.White else MaterialTheme.colorScheme.onPrimary

    val randomInteractionSource = remember { MutableInteractionSource() }
    val isRandomPressed by randomInteractionSource.collectIsPressedAsState()
    val randomScale by animateFloatAsState(if (isRandomPressed) 0.88f else 1.0f, label = "dialog_random_scale")
    val randomBgColor = if (isRandomPressed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer
    val randomContentColor = if (isRandomPressed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer
    
    var searchQuery by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    
    val filteredStations = stations.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.genre.contains(searchQuery, ignoreCase = true) ||
        it.country.contains(searchQuery, ignoreCase = true)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler { onDismiss() }

        var selectedGenre by remember { mutableStateOf("Все") }
        val genres = listOf("Все", "Lofi / Chill", "Jazz", "Ambient", "Classical", "Pop", "Retro Lounge")

        val genreFilteredStations = if (selectedGenre == "Все") {
            filteredStations
        } else {
            filteredStations.filter { it.genre.contains(selectedGenre, ignoreCase = true) }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Салонное Радио NailiX 📻",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "20+ станций мирового лаунджа и релакса",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад"
                                )
                            }
                        },
                        actions = {
                            FilledTonalButton(
                                onClick = onDismiss,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Свернуть", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Hero Vinyl / Player Card
                    val infiniteTransition = rememberInfiniteTransition(label = "dialog_radio_rotation")
                    val rotationAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rotating Vinyl Disc
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFF0F172A),
                                                    Color(0xFF1E293B),
                                                    Color(0xFF334155),
                                                    Color(0xFF0F172A)
                                                )
                                            )
                                        )
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Vinyl",
                                        tint = if (radioIsPlaying) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(32.dp)
                                            .rotate(if (radioIsPlaying) rotationAngle else 0f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (radioIsPlaying) Color(0xFF10B981) else Color(0xFF94A3B8))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (radioIsPlaying) "В ЭФИРЕ • LIVE" else "ОСТАНОВЛЕНО",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = if (radioIsPlaying) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = selectedStation?.name ?: "Выберите станцию ниже...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (selectedStation != null) {
                                        Text(
                                            text = "${selectedStation?.genre ?: ""} • ${selectedStation?.country ?: ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Big Control Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.toggleRadioPlayback() },
                                    interactionSource = playInteractionSource,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = playBgColor,
                                        contentColor = playContentColor
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(48.dp)
                                        .scale(playScale)
                                ) {
                                    if (radioIsLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = playContentColor
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (radioIsPlaying) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                                            contentDescription = "Play/Stop",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            radioIsPlaying -> "Остановить"
                                            radioIsLoading -> "Отмена"
                                            else -> "Включить"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.playRandomStation()
                                        Toast.makeText(context, "Случайная радиостанция 🎲", Toast.LENGTH_SHORT).show()
                                    },
                                    interactionSource = randomInteractionSource,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .scale(randomScale)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Random",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Микс 🎲",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Genre Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(genres) { genre ->
                            val isSelected = selectedGenre == genre
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedGenre = genre },
                                label = { Text(genre, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск по жанру или названию...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stations list header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Станции (${genreFilteredStations.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Нажмите для воспроизведения",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // List of stations
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(genreFilteredStations) { station ->
                            val isSelected = selectedStation?.id == station.id
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.playRadioStation(station) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.playRadioStation(station) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = station.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${station.genre} • ${station.country}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected && radioIsPlaying) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Playing",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("В эфире", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom Stream URL input
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🔗 Свой поток (стрим-ссылка):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customUrl,
                                    onValueChange = { customUrl = it },
                                    placeholder = { Text("https://example.com/radio.mp3", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (customUrl.isNotEmpty()) {
                                            val customStation = com.example.ui.viewmodel.RadioStation(
                                                id = "custom_" + System.currentTimeMillis(),
                                                name = "Свой поток 🔗",
                                                genre = "Custom Stream",
                                                country = "User Link",
                                                url = customUrl
                                            )
                                            viewModel.playRadioStation(customStation)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Играть", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}



@Composable
fun UpcomingBookingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedBookingForQr by remember { mutableStateOf<Booking?>(null) }

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
                    text = if (onboardingLanguage == "ru") "Предстоящие записи" else if (onboardingLanguage == "es") "Próximas citas" else "Upcoming Bookings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            val activeBookings = bookings.filter { it.status == "Подтверждена" }
            if (activeBookings.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeBookings) { booking ->
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
                                        text = booking.date + " • " + booking.timeSlot,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { viewModel.cancelBooking(booking) },
                                        modifier = Modifier.size(32.dp).testTag("cancel_booking_btn_${booking.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Cancel booking",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedBookingForQr = booking
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("QR-билет", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            com.example.util.CalendarSyncHelper.exportBookingToCalendar(context, booking)
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Календарь", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            text = if (onboardingLanguage == "ru") "Нет предстоящих записей.\nЗапишитесь к нашим лучшим мастерам! 💅" else "No upcoming bookings yet.\nBook your next premium service! 💅",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (onboardingLanguage == "ru") "Закрыть" else "Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )

    if (selectedBookingForQr != null) {
        SovereignQrTicketDialog(
            booking = selectedBookingForQr!!,
            onDismiss = { selectedBookingForQr = null }
        )
    }
}

@Composable
fun SovereignQrTicketDialog(
    booking: Booking,
    onDismiss: () -> Unit
) {
    val ticket = remember(booking) {
        com.example.util.VisitTicketCryptoManager.createTicket(booking)
    }
    val qrBitmap = remember(ticket) {
        com.example.util.VisitTicketCryptoManager.generateQrBitmap(ticket.rawPayload, 480)
    }
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Суверенный билет визита",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
                    text = "Предъявите этот QR-код мастеру при входе для бесконтактного и офлайн-чекина.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // QR Code Container
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

                // Security hash badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "SHA-256 Token: ${ticket.securityToken}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Booking specs
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Услуга:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(ticket.serviceName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Дата и время:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${ticket.date} • ${ticket.timeSlot}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Мастер:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(ticket.masterName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("К оплате:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${ticket.price} €", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        com.example.util.CalendarSyncHelper.exportBookingToCalendar(context, booking)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Синхронизировать с Google Календарём")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Закрыть")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}



