package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.signOutFromGoogle
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun GlobalSettingsDialog(
    viewModel: MainViewModel,
    currentSkin: String,
    currentCurrency: String,
    currentLanguage: String,
    isDarkTheme: Boolean,
    isPremium: Boolean,
    onTogglePremium: () -> Unit,
    onSkinChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser = remember { runCatching { Firebase.auth.currentUser }.getOrNull() }
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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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

                // Google Account & Sign-Out section
                if (currentUser != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google Account",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = currentUser.displayName ?: "Аккаунт Google",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currentUser.email ?: currentUser.uid.take(12),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            TextButton(
                                onClick = {
                                    signOutFromGoogle(credentialManager, scope) {
                                        Toast.makeText(
                                            context,
                                            if (currentLanguage == "ru") "Вы вышли из аккаунта" else "Signed out",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("sign_out_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign out",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLanguage == "ru") "Выйти" else "Sign out",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

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
                        listOf(languages1, languages2).forEach { rowLangs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowLangs.forEach { (code, label) ->
                                    val isSelected = currentLanguage == code
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 48.dp)
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold)
                                        }
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
                                    .defaultMinSize(minHeight = 48.dp)
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
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
                        text = when (currentLanguage) {
                            "ru" -> "Базовая тема"
                            "es" -> "Tema base"
                            else -> "Base Theme"
                        },
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
                                    .defaultMinSize(minHeight = 48.dp)
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            "ru" -> "Выберите скин"
                            "es" -> "Seleccionar skin"
                            else -> "Select Skin"
                        },
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
                                        .defaultMinSize(minHeight = 48.dp)
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
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 6.dp),
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

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        viewModel.resetOnboarding()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
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
                        text = when (currentLanguage) {
                            "ru" -> "🌟 Пройти тур по приложению (Онбординг)"
                            "es" -> "🌟 Ver tour de bienvenida"
                            else -> "🌟 Replay Onboarding Tour"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        when (currentLanguage) {
                            "ru" -> "Готово"
                            "es" -> "Listo"
                            else -> "Done"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (paywallSkinCode != null) {
            AlertDialog(
                onDismissRequest = { paywallSkinCode = null },
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "ru" -> "👑 Разблокировать PRO стиль!"
                            "es" -> "👑 ¡Desbloquear estilo PRO!"
                            else -> "👑 Unlock PRO Style!"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = when (currentLanguage) {
                            "ru" -> "Стиль \"$paywallSkinCode\" — это премиум-дизайн.\n\nРазблокируйте все 10 дизайнерских тем и расширенный лимит профилей в PRO!"
                            "es" -> "El estilo \"$paywallSkinCode\" es un diseño premium.\n\nDesbloquea los 10 temas exclusivos y amplía tu límite de perfiles con PRO."
                            else -> "The style \"$paywallSkinCode\" is a premium design.\n\nUnlock all 10 designer themes and expand your profile limit with PRO."
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onTogglePremium()
                            paywallSkinCode?.let { onSkinChange(it) }
                            paywallSkinCode = null
                        }
                    ) {
                        Text(
                            text = if (currentLanguage == "ru") "Активировать PRO" else "Activate PRO",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { paywallSkinCode = null }) {
                        Text(if (currentLanguage == "ru") "Отмена" else "Cancel")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
