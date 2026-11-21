package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    var activeTab by remember { mutableIntStateOf(if (activeRole == "master") 1 else 0) }
    var showCreateForm by remember { mutableStateOf(false) }

    var newClientName by remember { mutableStateOf("") }
    var newClientPhone by remember { mutableStateOf("") }
    var newClientEmail by remember { mutableStateOf("") }

    var newMasterName by remember { mutableStateOf("") }
    var newMasterAddress by remember { mutableStateOf("") }
    var newMasterSchedule by remember { mutableStateOf("") }
    var newMasterExp by remember { mutableStateOf("1") }
    var newMasterInsta by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPremium) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (isPremium) (if (onboardingLanguage == "ru") "Отключить" else "Disable")
                                else (if (onboardingLanguage == "ru") "Включить" else "Enable"),
                                fontWeight = FontWeight.Bold,
                                color = if (isPremium) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SecondaryTabRow(
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
                        Text(
                            text = if (onboardingLanguage == "ru") "Клиенты (${savedClients.size})" else "Clients (${savedClients.size})",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    Tab(
                        selected = activeTab == 1,
                        onClick = {
                            activeTab = 1
                            showCreateForm = false
                        }
                    ) {
                        Text(
                            text = if (onboardingLanguage == "ru") "Мастера (${savedMasters.size})" else "Masters (${savedMasters.size})",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!showCreateForm) {
                    val maxProfiles = if (isPremium) 3 else 1

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
                                                Toast.makeText(
                                                    context,
                                                    if (onboardingLanguage == "ru") "Профиль ${profile.name} заморожен. Включите Премиум!" else "Profile ${profile.name} is frozen.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                viewModel.activateClientProfile(profile.phone)
                                                Toast.makeText(
                                                    context,
                                                    if (onboardingLanguage == "ru") "Переключено на клиента: ${profile.name}" else "Switched to client: ${profile.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
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
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                                        Icon(Icons.Default.Star, contentDescription = "Primary", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                Text(text = profile.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                        if (isFrozen) {
                                            Text(
                                                text = if (onboardingLanguage == "ru") "Заморожен ❄️" else "Frozen ❄️",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        } else if (!isPrimary) {
                                            IconButton(onClick = { viewModel.setPrimaryClientProfile(profile.phone) }) {
                                                Icon(Icons.Default.StarBorder, contentDescription = "Set primary", tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
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
                                                Toast.makeText(
                                                    context,
                                                    if (onboardingLanguage == "ru") "Профиль мастера ${profile.name} заморожен." else "Master profile ${profile.name} is frozen.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                viewModel.activateMasterProfile(profile.id)
                                                Toast.makeText(
                                                    context,
                                                    if (onboardingLanguage == "ru") "Переключено на мастера: ${profile.name}" else "Switched to master: ${profile.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
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
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                                        Icon(Icons.Default.Star, contentDescription = "Primary", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                Text(text = "@${profile.instagramUsername}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                        if (isFrozen) {
                                            Text(
                                                text = if (onboardingLanguage == "ru") "Заморожен ❄️" else "Frozen ❄️",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        } else if (!isPrimary) {
                                            IconButton(onClick = { viewModel.setPrimaryMasterProfile(profile.id) }) {
                                                Icon(Icons.Default.StarBorder, contentDescription = "Set primary", tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val size = if (activeTab == 0) savedClients.size else savedMasters.size
                    if (size < maxProfiles) {
                        Button(
                            onClick = { showCreateForm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
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
                        Text(
                            text = if (size >= 3) {
                                if (onboardingLanguage == "ru") "Достигнут максимальный лимит (3 профиля)" else "Maximum limit reached (3 profiles)"
                            } else {
                                if (onboardingLanguage == "ru") "Для создания до 3-х профилей включите Премиум!" else "Enable Premium to create up to 3 profiles!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (size >= 3) Color.Gray else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
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
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
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
                                        if (err != null) {
                                            Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
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
                                        if (err != null) {
                                            Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
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
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
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

    Dialog(onDismissRequest = onDismiss) {
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
                    textAlign = TextAlign.Center,
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
                    textAlign = TextAlign.Center,
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
                                            tint = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(profile.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                if (isPrimary) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(Icons.Default.Star, contentDescription = "Primary", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(profile.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                                            tint = if (isFrozen) Color.Gray else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(profile.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                if (isPrimary) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(Icons.Default.Star, contentDescription = "Primary", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text("@${profile.instagramUsername}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (onboardingLanguage == "ru") "Закрыть" else "Dismiss", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
