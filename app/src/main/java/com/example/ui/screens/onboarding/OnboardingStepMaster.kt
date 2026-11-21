package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.InstagramPost
import com.example.ui.viewmodel.MainViewModel

@Composable
internal fun OnboardingStepMaster(
    language: String,
    dict: Map<String, String>,
    colors: OnboardingColors,
    name: String,
    surname: String,
    alias: String,
    masterAddress: String,
    masterHours: String,
    masterExp: String,
    masterLangs: String,
    masterCurrency: String,
    parsedInstagramPosts: List<InstagramPost>,
    selectedDisplayPostIds: Set<Int>,
    instagramError: String?,
    onAliasChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onOpenIgFeedDialog: () -> Unit,
    onPickGalleryPhotos: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var selectedTemplateIndex by remember { mutableIntStateOf(0) }

    val templates = listOf(
        Triple("Базовый маникюр и педикюр", "Классика и гель-лак • Смарт-педикюр • Дизайн", "Для универсалов"),
        Triple("Топ-Дизайн и Ручная роспись", "Акварельные рисунки • Френч • Инкрустация", "Для топ-дизайнеров"),
        Triple("Минимализм и Укрепление", "Медицинский уход • Японский маникюр • IBX", "Для эстетического ухода")
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
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = (dict["services_templates"] ?: "Template").uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Black),
                color = colors.cardTitle,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                templates.forEachIndexed { idx, (title, servicesTxt, recom) ->
                    val selected = selectedTemplateIndex == idx
                    val bgColor = if (selected) (if (colors.isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)) else (if (colors.isDarkTheme) Color(0xFF334155) else Color.White)
                    val txtColor = if (selected) (if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White) else (if (colors.isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF0F2C59))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTemplateIndex = idx }
                            .background(bgColor, RoundedCornerShape(16.dp))
                            .testTag("template_card_$idx"),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(if (selected) 3.dp else 1.5.dp, if (selected) colors.cardBorder else Color(0xFF94A3B8)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = txtColor)
                            Spacer(Modifier.height(4.dp))
                            Text(servicesTxt, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = txtColor.copy(alpha = 0.85f))
                            Text(recom, fontSize = 12.sp, color = txtColor.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Currency Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.innerCardBg, RoundedCornerShape(20.dp))
                    .testTag("currency_selection_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.5.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, contentDescription = "Currency", tint = colors.innerCardText)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (language == "ru") "Валюта расчетов" else "Settlement Currency",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = colors.innerCardText
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    val currencies = listOf("EUR" to "€ EUR", "USD" to "$ USD", "RUB" to "₽ RUB", "GBP" to "£ GBP", "CHF" to "₣ CHF")
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { (code, display) ->
                            FilterChip(
                                selected = masterCurrency == code,
                                onClick = { onCurrencyChange(code) },
                                label = { Text(display, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                modifier = Modifier.testTag("onboarding_currency_chip_$code")
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Honest Instagram + Photo Picker Portfolio Section
            val igColor = if (colors.isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF00A896)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.innerCardBg, RoundedCornerShape(20.dp))
                    .testTag("insta_portfolio_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.5.dp, igColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Portfolio Import", tint = igColor)
                        Spacer(Modifier.width(8.dp))
                        Text("ПОРТФОЛИО МАСТЕРА", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.innerCardText)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Импортируйте публичные посты из Instagram или выберите фото работ напрямую из галереи.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.bodyTextColor
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = alias,
                        onValueChange = onAliasChange,
                        label = { Text("Instagram никнейм (без @)") },
                        placeholder = { Text("art_nail_master") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().background(colors.inputBgColor, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.loadTop20InstagramPosts(alias)
                            onOpenIgFeedDialog()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = igColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("ig_sync_wizard_btn")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Download", tint = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Загрузить из Instagram", fontWeight = FontWeight.Bold, color = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White)
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onPickGalleryPhotos,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, igColor),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gallery_portfolio_picker_btn")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick Photos", tint = igColor)
                        Spacer(Modifier.width(8.dp))
                        Text("Выбрать фото из галереи (до 20)", color = igColor, fontWeight = FontWeight.Bold)
                    }

                    if (!instagramError.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(text = "⚠️ $instagramError", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    if (parsedInstagramPosts.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Добавлено работ: ${parsedInstagramPosts.size}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().height(120.dp)) {
                            items(parsedInstagramPosts) { post ->
                                val isSelected = selectedDisplayPostIds.contains(post.id)
                                Card(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clickable { viewModel.toggleDisplayPostSelection(post.id, context) }
                                        .border(if (isSelected) 3.dp else 1.dp, if (isSelected) igColor else Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(model = post.imageUrl, contentDescription = "Work", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier.size(22.dp).align(Alignment.TopEnd).padding(3.dp).background(igColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { viewModel.setOnboardingStep(3) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    border = BorderStroke(2.dp, colors.cardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(dict["back"]?.uppercase() ?: "CANCEL", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.cardBorder)
                }
                Button(
                    onClick = {
                        val finalMasterName = "$name $surname".trim().ifEmpty { name.ifBlank { "Мастер NailiX" } }
                        viewModel.registerMasterProfile(
                            name = finalMasterName,
                            address = masterAddress.ifBlank { "Calella" },
                            schedule = masterHours.ifBlank { "10:00 - 19:00" },
                            experience = masterExp.toIntOrNull() ?: 5,
                            languages = masterLangs.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("Русский", "Испанский") },
                            instagram = alias.trim(),
                            currency = masterCurrency
                        )
                        viewModel.completeOnboarding()
                        Toast.makeText(context, "Профиль мастера $finalMasterName создан! 💅🎨", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.weight(1f).height(54.dp).gaudiOrnament(isButton = true, alphaMultiplier = 2.0f).testTag("master_onboarding_complete_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = dict["complete_reg"]?.uppercase() ?: "DONE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (colors.isDarkTheme) Color(0xFF0F172A) else Color.White
                    )
                }
            }
        }
    }
}
