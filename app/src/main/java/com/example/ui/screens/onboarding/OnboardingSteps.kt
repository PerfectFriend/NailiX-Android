package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.viewmodel.ClientProfile
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MasterProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
internal fun OnboardingStepLanguage(
    language: String,
    dict: Map<String, String>,
    colors: OnboardingColors,
    savedClientsCount: Int,
    savedMastersCount: Int,
    onSelectLanguage: (String) -> Unit,
    onNextStep: (Int) -> Unit
) {
    val languagesList = listOf(
        Triple("ru", "Русский", "🇷🇺"),
        Triple("en", "English", "🇬🇧"),
        Triple("es", "Español", "🇪🇸"),
        Triple("zh", "中文", "🇨🇳"),
        Triple("hi", "हिन्दी", "🇮🇳"),
        Triple("ar", "العربية", "🇸🇦"),
        Triple("pt", "Português", "🇵🇹"),
        Triple("fr", "Français", "🇫🇷"),
        Triple("he", "עברית", "🇮🇱"),
        Triple("tr", "Türkçe", "🇹🇷")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(colors.cardBg, RoundedCornerShape(24.dp))
            .gaudiOrnament(isButton = false, alphaMultiplier = 1.8f, isDark = colors.isDarkTheme)
            .gaudiWaveBorder(alpha = 0.8f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(4.dp, colors.cardBorder),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = (dict["select_lang"] ?: "Language").uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                color = colors.cardTitle,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                languagesList.forEach { (langCode, langName, flag) ->
                    val isSelected = language == langCode
                    val itemBgColor = if (isSelected) {
                        if (colors.isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
                    } else {
                        if (colors.isDarkTheme) Color(0xFF334155) else Color.White
                    }
                    val itemTextColor = if (isSelected) {
                        if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White
                    } else {
                        if (colors.isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF0F2C59)
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(langCode) }
                            .background(itemBgColor, RoundedCornerShape(16.dp))
                            .gaudiWaveBorder(alpha = if (isSelected) 0.9f else 0.3f)
                            .testTag("lang_$langCode"),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(
                            width = if (isSelected) 3.dp else 1.5.dp,
                            color = if (isSelected) colors.cardBorder else Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = flag, fontSize = 36.sp, modifier = Modifier.padding(end = 14.dp))
                            Text(
                                text = langName,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Black),
                                color = itemTextColor
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = itemTextColor, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (savedClientsCount > 0 || savedMastersCount > 0) onNextStep(5) else onNextStep(2)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .gaudiOrnament(isButton = true, alphaMultiplier = 2.0f)
                    .gaudiWaveBorder(alpha = 0.8f)
                    .testTag("lang_next_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D539F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(dict["next"]?.uppercase() ?: "NEXT", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
internal fun OnboardingStepRole(
    role: String?,
    language: String,
    dict: Map<String, String>,
    colors: OnboardingColors,
    hasSavedProfiles: Boolean,
    viewModel: MainViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(colors.cardBg, RoundedCornerShape(20.dp))
            .gaudiOrnament(isButton = false, alphaMultiplier = 1.2f, isDark = colors.isDarkTheme)
            .gaudiWaveBorder(alpha = 0.6f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(2.5.dp, colors.cardBorder),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = (dict["select_role"] ?: "Role").uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = colors.cardTitle,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            RoleOptionCard(
                title = (dict["role_client"] ?: "Client").uppercase(),
                desc = dict["role_client_desc"] ?: "Find master, book nail appointments",
                icon = Icons.Default.Person,
                isSelected = role == "client",
                accentLight = Color(0xFF1D539F),
                accentDark = Color(0xFF38BDF8),
                isDarkTheme = colors.isDarkTheme,
                tag = "role_client_card",
                onClick = { viewModel.setOnboardingRole("client") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            RoleOptionCard(
                title = (dict["role_master"] ?: "Master").uppercase(),
                desc = dict["role_master_desc"] ?: "Manage appointments, publish designs",
                icon = Icons.Default.Face,
                isSelected = role == "master",
                accentLight = Color(0xFF00A896),
                accentDark = Color(0xFF2DD4BF),
                isDarkTheme = colors.isDarkTheme,
                tag = "role_master_card",
                onClick = { viewModel.setOnboardingRole("master") }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { viewModel.setOnboardingStep(if (hasSavedProfiles) 5 else 0) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.5.dp, colors.cardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.cardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(dict["back"]?.uppercase() ?: "BACK", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, color = colors.cardBorder)
                }
                Button(
                    onClick = { viewModel.setOnboardingStep(3) },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("role_next_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        dict["next"]?.uppercase() ?: "NEXT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        color = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val user = runCatching { Firebase.auth.currentUser }.getOrNull()
                    viewModel.completeQuickStart(
                        role = role ?: "client",
                        fallbackName = user?.displayName.orEmpty(),
                        fallbackEmail = user?.email.orEmpty()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("quick_start_role_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colors.cardBorder.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.cardBorder)
            ) {
                val roleLabel = if (role == "master") (dict["role_master"] ?: "Master") else (dict["role_client"] ?: "Client")
                Text(
                    text = when (language) {
                        "ru" -> "⚡ Быстрый старт ($roleLabel)"
                        "es" -> "⚡ Inicio rápido ($roleLabel)"
                        else -> "⚡ Quick start ($roleLabel)"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.cardBorder
                )
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentLight: Color,
    accentDark: Color,
    isDarkTheme: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) (if (isDarkTheme) accentDark else accentLight) else (if (isDarkTheme) Color(0xFF334155) else Color.White)
    val textColor = if (isSelected) (if (isDarkTheme) Color(0xFF0F172A) else Color.White) else (if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF0F2C59))
    val descColor = if (isSelected) textColor.copy(alpha = 0.88f) else (if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF334155))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bgColor, RoundedCornerShape(16.dp))
            .gaudiWaveBorder(alpha = if (isSelected) 0.8f else 0.2f)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(if (isSelected) 3.dp else 1.5.dp, if (isSelected) accentLight else Color(0xFFCBD5E1)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(textColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = textColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Black), color = textColor)
                Text(desc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), maxLines = 2, overflow = TextOverflow.Ellipsis, color = descColor)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = textColor, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
internal fun OnboardingStepProfile(
    role: String?,
    language: String,
    dict: Map<String, String>,
    colors: OnboardingColors,
    name: String,
    surname: String,
    phone: String,
    email: String,
    alias: String,
    avatarUrl: String?,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onPickAvatarPhoto: () -> Unit,
    onOpenAiAvatarDialog: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val fieldLabelColor = if (colors.isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
    val fieldTextColor = if (colors.isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F2C59)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(colors.cardBg, RoundedCornerShape(24.dp))
            .gaudiOrnament(isButton = false, alphaMultiplier = 1.8f, isDark = colors.isDarkTheme)
            .gaudiWaveBorder(alpha = 0.8f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(4.dp, colors.cardBorder),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = (dict["profile_setup"] ?: "Profile").uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                color = colors.cardTitle,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            listOf(
                Triple(name, onNameChange, Pair(dict["name"] ?: "Name *", "reg_name_field")),
                Triple(surname, onSurnameChange, Pair(dict["surname"] ?: "Surname", "reg_surname_field")),
                Triple(phone, onPhoneChange, Pair(dict["phone"] ?: "Phone *", "reg_phone_field")),
                Triple(email, onEmailChange, Pair(dict["email"] ?: "Email", "reg_email_field")),
                Triple(alias, onAliasChange, Pair(dict["alias"] ?: "Instagram", "reg_alias_field"))
            ).forEachIndexed { idx, (value, onValueChange, meta) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(meta.first, fontWeight = FontWeight.Bold) },
                    keyboardOptions = if (idx == 2) KeyboardOptions(keyboardType = KeyboardType.Phone) else KeyboardOptions.Default,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = fieldTextColor),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = fieldLabelColor,
                        unfocusedBorderColor = colors.innerCardBorder,
                        focusedLabelColor = fieldLabelColor,
                        unfocusedLabelColor = fieldLabelColor
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.inputBgColor, RoundedCornerShape(12.dp))
                        .testTag(meta.second),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = dict["avatar"] ?: "Avatar",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Black),
                color = colors.cardTitle,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .border(4.dp, colors.cardBorder, CircleShape)
                        .background(colors.innerCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(model = avatarUrl, contentDescription = "Avatar", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon_gaudi_n_1793575273048),
                            contentDescription = "NailiX Icon Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = onPickAvatarPhoto,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("upload_device_avatar_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.cardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Photo Picker", tint = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("ФОТО ИЗ ГАЛЕРЕИ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White)
                    }

                    FilledTonalButton(
                        onClick = onOpenAiAvatarDialog,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_ai_avatar_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Avatar")
                        Spacer(Modifier.width(6.dp))
                        Text(dict["ai_synth"] ?: "AI-аватар", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (avatarUrl != null) {
                        OutlinedButton(
                            onClick = { viewModel.setOnboardingAvatar(null) },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reset_avatar_btn"),
                            border = BorderStroke(1.5.dp, Color(0xFFF05D5E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Reset", tint = Color(0xFFF05D5E))
                            Spacer(Modifier.width(6.dp))
                            Text("ИКОНКА NAILIX", fontWeight = FontWeight.Bold, color = Color(0xFFF05D5E), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { viewModel.setOnboardingStep(2) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    border = BorderStroke(2.dp, colors.cardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(dict["back"]?.uppercase() ?: "CANCEL", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.cardBorder)
                }
                Button(
                    onClick = {
                        val finalName = if (name.isBlank()) (if (role == "master") "Мастер NailiX" else "Клиент NailiX") else "$name $surname".trim()
                        if (role == "master") {
                            viewModel.setOnboardingStep(4)
                        } else {
                            viewModel.registerClientProfile(
                                name = finalName,
                                phone = phone.trim(),
                                homeAddr = "Calella Center, Moragas 2",
                                workAddr = "Calella beach, Jovara 120"
                            )
                            viewModel.completeOnboarding()
                            Toast.makeText(context, "Добро пожаловать, $finalName! 🎉", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .gaudiOrnament(isButton = true, alphaMultiplier = 2.0f, isDark = colors.isDarkTheme)
                        .testTag("profile_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (role == "master") (dict["next"]?.uppercase() ?: "NEXT") else (dict["complete_reg"]?.uppercase() ?: "DONE"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    val user = runCatching { Firebase.auth.currentUser }.getOrNull()
                    viewModel.completeQuickStart(
                        role = role ?: "client",
                        fallbackName = name.ifBlank { user?.displayName.orEmpty() },
                        fallbackEmail = email.ifBlank { user?.email.orEmpty() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("step3_guest_btn"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, colors.cardBorder.copy(alpha = 0.7f))
            ) {
                Text(
                    text = if (language == "ru") "⚡ Быстрый старт без заполнения" else "⚡ Quick start",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.cardBorder
                )
            }
        }
    }
}

@Composable
internal fun OnboardingStepStartupProfiles(
    language: String,
    dict: Map<String, String>,
    colors: OnboardingColors,
    savedClients: List<ClientProfile>,
    savedMasters: List<MasterProfile>,
    viewModel: MainViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color(0xFFE0F2FE), RoundedCornerShape(24.dp))
            .gaudiOrnament(isButton = false, alphaMultiplier = 1.8f)
            .gaudiWaveBorder(alpha = 0.8f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(4.dp, Color(0xFF1D539F)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (language == "ru") "ВЫБЕРИТЕ ПРОФИЛЬ" else "SELECT PROFILE",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                color = Color(0xFF0F2C59),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (savedClients.isNotEmpty()) {
                Text("Клиенты", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1D539F), modifier = Modifier.align(Alignment.Start))
                savedClients.forEach { client ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable {
                            viewModel.activateClientProfile(client.phone)
                            viewModel.completeOnboarding()
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1D539F))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(client.name, fontWeight = FontWeight.Bold, color = Color(0xFF0F2C59))
                                Text(client.phone, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    }
                }
            }

            if (savedMasters.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("Мастера", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF00A896), modifier = Modifier.align(Alignment.Start))
                savedMasters.forEach { master ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable {
                            viewModel.activateMasterProfile(master.id)
                            viewModel.completeOnboarding()
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF00A896))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(master.name, fontWeight = FontWeight.Bold, color = Color(0xFF0F2C59))
                                Text(if (master.instagramUsername.isNotEmpty()) "@${master.instagramUsername}" else master.address, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { viewModel.setOnboardingStep(2) },
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("add_profile_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White)
                Spacer(Modifier.width(8.dp))
                Text(if (language == "ru") "ДОБАВИТЬ ПРОФИЛЬ" else "ADD PROFILE", fontWeight = FontWeight.Bold, color = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.setOnboardingStep(1) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, colors.cardBorder)
            ) {
                Text(dict["back"]?.uppercase() ?: "BACK", fontWeight = FontWeight.Bold, color = colors.cardBorder)
            }
        }
    }
}
