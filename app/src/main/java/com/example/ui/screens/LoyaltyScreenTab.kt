package com.example.ui.screens

import android.content.Intent
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoyaltyScreenTab(viewModel: MainViewModel) {
    val loyalty by viewModel.loyaltyInfo.collectAsStateWithLifecycle()
    val referralInput by viewModel.referralInputCode.collectAsStateWithLifecycle()
    val referredFriends by viewModel.referredFriends.collectAsStateWithLifecycle()
    val transactions by viewModel.loyaltyTransactions.collectAsStateWithLifecycle()
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val clientProfile by viewModel.clientProfile.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("card") } // "card", "referral", "history", "challenges"

    val activeContestsList by viewModel.activeContests.collectAsStateWithLifecycle()
    val selectedContest by viewModel.selectedContestId.collectAsStateWithLifecycle()
    val entries by viewModel.contestEntries.collectAsStateWithLifecycle()
    val votingPair by viewModel.votingPair.collectAsStateWithLifecycle()
    val hasVipPass by viewModel.hasVipChallengerPass.collectAsStateWithLifecycle()
    var showSubmitDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER INTRO
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "Клуб привилегий",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Копите бонусы за услуги и приглашайте друзей!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // SUB-TABS SELECTOR CHIPS
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeSubTab == "card",
                    onClick = { activeSubTab = "card" },
                    label = { Text("Карта и Бонусы") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = activeSubTab == "referral",
                    onClick = { activeSubTab = "referral" },
                    label = { Text("Рефералы 🎁") },
                    leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = activeSubTab == "history",
                    onClick = { activeSubTab = "history" },
                    label = { Text("История") },
                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = activeSubTab == "challenges",
                    onClick = { activeSubTab = "challenges" },
                    label = { Text("Конкурсы 🏆") },
                    leadingIcon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        when (activeSubTab) {
            "card" -> {
                // VIRTUAL LOYALTY CARD
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.586f)
                            .heightIn(min = 180.dp, max = 220.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "VIP CLIENT CARD",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.8.sp,
                                        fontSize = 13.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "VIP Status",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Баланс бонусов:",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${loyalty?.points ?: 250} Б",
                                        color = Color.White,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "Статус клуба:",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = loyalty?.level?.uppercase() ?: "SILVER",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Text(
                                        text = "СТУДИЯ NAILIX",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // QR CODE SECTION
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Персональный QR-код на скидку",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Покажите администратору при оплате, чтобы списать бонусы или получить кэшбэк (1 бонус = 1 рубль)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Barcode / QR simulator
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(8) { row ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            repeat(8) { col ->
                                                val isCorner = (row < 2 && col < 2) || (row > 5 && col < 2) || (row < 2 && col > 5)
                                                val fill = isCorner || ((row + col) % 3 == 0) || ((row * col) % 5 == 2)
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(
                                                            if (fill) MaterialTheme.colorScheme.primary else Color.Transparent
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Код лояльности: ${clientProfile.referralCode.ifBlank { loyalty?.referralCode?.takeIf { it.isNotBlank() && !it.startsWith("VIC-") } ?: "NLX-PENDING" }}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // PRIVILEGE LEVELS EXPANSION
                item {
                    Text(
                        text = "Уровни клуба привилегий",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            PrivilegeLevelCard(
                                title = "Silver ⭐",
                                desc = "Базовый уровень",
                                cashback = "10% кэшбэк",
                                requirement = "При регистрации",
                                isActive = (loyalty?.level ?: "Silver") == "Silver",
                                color = Color(0xFF90A4AE)
                            )
                        }
                        item {
                            PrivilegeLevelCard(
                                title = "Gold 👑",
                                desc = "Продвинутый уровень",
                                cashback = "15% кэшбэк",
                                requirement = "От 500 баллов",
                                isActive = (loyalty?.level ?: "Silver") == "Gold",
                                color = Color(0xFFFFB300)
                            )
                        }
                        item {
                            PrivilegeLevelCard(
                                title = "Platinum 💎",
                                desc = "Элитный уровень VIP",
                                cashback = "20% кэшбэк",
                                requirement = "От 1000 баллов",
                                isActive = (loyalty?.level ?: "Silver") == "Platinum",
                                color = Color(0xFF00B0FF)
                            )
                        }
                    }
                }

                // BONUS GIFT SHOP
                item {
                    Text(
                        text = "Потратить баллы лояльности 🛍️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "Обменяйте накопленные бонусы на премиальные подарки и услуги в нашей студии!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val giftItems = listOf(
                    GiftShopItem(name = "Масло для кутикулы NailiX 🧴", cost = 200, desc = "Ароматный уход с витаминами A и E"),
                    GiftShopItem(name = "Сложный дизайн на 2 ногтях 🎨", cost = 150, desc = "Стемпинг, слайдеры или роспись от мастера"),
                    GiftShopItem(name = "Парафинотерапия рук 🧴", cost = 300, desc = "СПА-уход для бархатной и нежной кожи"),
                    GiftShopItem(name = "Скидка 500 рублей на любой визит 🎟️", cost = 500, desc = "Электронный купон на оплату процедуры")
                )

                items(giftItems) { gift ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = gift.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text(text = gift.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${gift.cost} бонусов",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val currentPoints = loyalty?.points ?: 0
                                    if (currentPoints >= gift.cost) {
                                        viewModel.spendBonusPoints(gift.cost, gift.name)
                                        Toast.makeText(context, "Подарок оформлен! Предъявите код купона NLX-${(1000..9999).random()} мастеру.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Недостаточно бонусов! Пригласите подруг, чтобы накопить еще ${gift.cost - currentPoints} Б", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if ((loyalty?.points ?: 0) >= gift.cost) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "Обменять",
                                    color = if ((loyalty?.points ?: 0) >= gift.cost) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            "referral" -> {
                // REFERRAL ILLUSTRATED BANNER
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_referral_banner_1783995249167),
                                contentDescription = "Referral Program Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Дарите бонусы подругам!",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Получайте до 200 баллов за каждую рекомендацию",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // REFERRAL CODE AND SHARE ACTIONS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Делитесь и зарабатывайте 💸",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Отправьте свой код подруге. При её регистрации вы получите 50 бонусов, а когда она завершит свою первую процедуру, вам начислится еще 150 бонусов! Подруга также получит 200 приветственных баллов.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            val myRefCode = clientProfile.referralCode.ifBlank {
                                loyalty?.referralCode?.takeIf { it.isNotBlank() && !it.startsWith("VIC-") } ?: "NLX-PENDING"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Ваш уникальный код:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = myRefCode,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        val textToCopy = if (onboardingLanguage == "ru") {
                                            "Привет! Запишись на маникюр в студию NailiX, введи мой реферальный код $myRefCode и получи 200 приветственных бонусов!"
                                        } else {
                                            "Hello! Book a manicure at NailiX studio, use my referral code $myRefCode and get 200 welcome bonuses!"
                                        }
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(textToCopy))
                                        Toast.makeText(context, if (onboardingLanguage == "ru") "Промокод скопирован! 📋" else "Referral code copied! 📋", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Копировать", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            val shareText = if (onboardingLanguage == "ru") {
                                "Привет! Приглашаю в NailiX 💅 Введи мой код $myRefCode при записи и получи +200 бонусных баллов!"
                            } else {
                                "Join me on NailiX 💅 Use my referral code $myRefCode to get +200 bonus points!"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val tgIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            setPackage("org.telegram.messenger")
                                        }
                                        try {
                                            context.startActivity(tgIntent)
                                        } catch (_: Exception) {
                                            val fallback = Intent.createChooser(
                                                Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                },
                                                "Поделиться кодом NailiX"
                                            )
                                            context.startActivity(fallback)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("В Telegram", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val chooser = Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                            },
                                            "Поделиться реферальным кодом"
                                        )
                                        context.startActivity(chooser)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Поделиться", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // ENTER FRIEND'S CODE (Real Firestore Verification)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Ввести код от подруги",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Укажите реальный промокод подруги (например NLX-XXXXXX), чтобы активировать приветственные 200 баллов через облачную проверку.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = referralInput,
                                    onValueChange = { viewModel.referralInputCode.value = it },
                                    placeholder = { Text("NLX-XXXXXX") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("referral_code_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Button(
                                    onClick = { viewModel.claimReferralCode() },
                                    enabled = referralInput.isNotBlank(),
                                    modifier = Modifier.height(52.dp).testTag("referral_apply_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Ввод")
                                }
                            }
                        }
                    }
                }

                // REFERRED FRIENDS LIST
                item {
                    Text(
                        text = "Приглашенные подруги (${referredFriends.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                if (referredFriends.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Вы еще не пригласили подруг",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Поделитесь кодом выше, чтобы получать бонусы!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(referredFriends) { friend ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = friend.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = friend.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "${friend.status} • ${friend.date}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (friend.status.contains("визит")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "+${friend.rewardPoints} Б",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

            "history" -> {
                // TRANSACTIONS LEDGER
                item {
                    Text(
                        text = "История начислений и списаний",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "История пуста",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Здесь будут отображаться ваши начисления и списания баллов.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(transactions) { tx ->
                        val dateFormatted = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = tx.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = dateFormatted, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = if (tx.points >= 0) "+${tx.points} Б" else "${tx.points} Б",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (tx.points >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
            "challenges" -> {
                // VIP PASS PROMO BANNER (Monetization Showcase)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "VIP Pass",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Золотой абонемент 🏆",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Switch(
                                    checked = hasVipPass,
                                    onCheckedChange = { 
                                        viewModel.toggleVipChallengerPass()
                                        if (!hasVipPass) {
                                            Toast.makeText(context, "Золотой абонемент активирован! 🌟 Двойные баллы и бесплатное участие!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Подписка отключена", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Всего за 99 руб/мес вы получите БЕСПЛАТНОЕ участие во всех конкурсах и Х2 БОНУСЫ (10 Б вместо 5 Б) за каждый голос в баттлах ногтевого дизайна!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (hasVipPass) "✓ ПОДПИСКА АКТИВНА: Доступны все VIP-привилегии!" else "Цена: 99 ₽/мес (~$1.20) • Массовость и доступность для всех",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (hasVipPass) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ANONYMOUS BATTLE MATCH GAME
                item {
                    Text(
                        text = "Баттл дизайнов ногтей 💅",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "Голосуйте анонимно за понравившийся дизайн и получайте бонусы лояльности за вашу оценку!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    if (votingPair != null) {
                        val left = votingPair!!.first
                        val right = votingPair!!.second
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Какой дизайн вам нравится больше?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Left Option
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.voteForEntry(left.id, right.id)
                                                Toast.makeText(context, "Голос учтен! +${if (hasVipPass) 10 else 5} Б лояльности 💎", Toast.LENGTH_SHORT).show()
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = left.imageUrl,
                                                contentDescription = "Design Option A",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Вариант А",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }

                                    // Right Option
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.voteForEntry(right.id, left.id)
                                                Toast.makeText(context, "Голос учтен! +${if (hasVipPass) 10 else 5} Б лояльности 💎", Toast.LENGTH_SHORT).show()
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = right.imageUrl,
                                                contentDescription = "Design Option B",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Вариант Б",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.generateVotingPair() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Skip Match",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Пропустить / Другая пара 🎲",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Нет доступных пар для голосования в этой категории",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ACTIVE CONTESTS CAROUSEL
                item {
                    Text(
                        text = "Активные конкурсы месяца 🏆",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(activeContestsList) { contest ->
                    val isSelected = selectedContest == contest.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectContest(contest.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = contest.icon, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = contest.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Выбран",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = contest.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Главный приз: ${contest.prize}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "До: ${contest.endDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        viewModel.selectContest(contest.id)
                                        showSubmitDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Участвовать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // LEADERBOARD FOR THE SELECTED CONTEST
                item {
                    Text(
                        text = "Лидерборд конкурса: ${activeContestsList.find { it.id == selectedContest }?.title ?: ""} 📈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                val currentContestEntries = entries.filter { it.contestId == selectedContest }.sortedByDescending { it.votes }
                if (currentContestEntries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Пока нет заявок. Будьте первыми!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(currentContestEntries) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    AsyncImage(
                                        model = entry.imageUrl,
                                        contentDescription = "Entry image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = entry.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (entry.isUserSubmitted) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFFE082), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "Моя работа",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE65100)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Автор: ${entry.authorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Votes count",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${entry.votes}",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    // POPUP SUBMIT DIALOG OUTSIDE LAZYCOLUMN (In proper Composable context!)
    if (showSubmitDialog) {
        var designTitle by remember { mutableStateOf("") }
        var designAuthor by remember { mutableStateOf("@my_nails") }
        val randomNailImages = listOf(
            "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60"
        )
        var selectedImage by remember { mutableStateOf(randomNailImages[0]) }

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Принять участие в конкурсе!")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Вы подаете заявку на конкурс: ${activeContestsList.find { it.id == selectedContest }?.title ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = designTitle,
                        onValueChange = { designTitle = it },
                        label = { Text("Название дизайна") },
                        placeholder = { Text("например: Звездное Небо") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = designAuthor,
                        onValueChange = { designAuthor = it },
                        label = { Text("Имя автора / Instagram") },
                        placeholder = { Text("@my_nails") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Выберите фото дизайна (симуляция):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(randomNailImages) { imgUrl ->
                            val isImgSelected = imgUrl == selectedImage
                            Card(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable { selectedImage = imgUrl }
                                    .border(
                                        width = if (isImgSelected) 3.dp else 0.dp,
                                        color = if (isImgSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                                    ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = "Simulated Upload",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    if (hasVipPass) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Вход свободный по Золотому Абонементу! Стоимость: 0 Б",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    } else {
                        Text(
                            text = "Стоимость подачи заявки: 30 Бонусов 💎",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "(У вас на счету: ${viewModel.loyaltyInfo.collectAsStateWithLifecycle().value?.points ?: 0} Б)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (designTitle.trim().isEmpty()) {
                            Toast.makeText(context, "Укажите название дизайна!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val success = viewModel.submitDesignToContest(
                            contestId = selectedContest,
                            title = designTitle.trim(),
                            imageUrl = selectedImage,
                            authorName = designAuthor.trim(),
                            context = context
                        )
                        if (success) {
                            Toast.makeText(context, "Работа успешно отправлена на конкурс! 🎉 Удачи!", Toast.LENGTH_LONG).show()
                            showSubmitDialog = false
                        }
                    }
                ) {
                    Text("Отправить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}



@Composable
fun PrivilegeLevelCard(
    title: String,
    desc: String,
    cashback: String,
    requirement: String,
    isActive: Boolean,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(130.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) color else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (isActive) color else MaterialTheme.colorScheme.onSurface)
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .background(color, CircleShape)
                                .size(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column {
                Text(text = cashback, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = if (isActive) color else MaterialTheme.colorScheme.primary)
                Text(text = requirement, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

data class GiftShopItem(val name: String, val cost: Int, val desc: String)

// --------------------------------------------------------------------
// TAB 5: INSTAGRAM PARSING PORTFOLIO GALLERY
// --------------------------------------------------------------------


@Composable
fun LoyaltyBonusesDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val loyalty by viewModel.loyaltyInfo.collectAsStateWithLifecycle()
    val clientProfile by viewModel.clientProfile.collectAsStateWithLifecycle()
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val phoneInput = viewModel.mailingPhone.collectAsStateWithLifecycle().value

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (onboardingLanguage == "ru") "Мои Бонусы" else "My Bonuses",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Loyalty Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "VIP CLIENT CARD",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.5.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "VIP Status",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (onboardingLanguage == "ru") "Баланс бонусов:" else "Bonus balance:",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${loyalty?.points ?: 250} Б",
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = if (onboardingLanguage == "ru") "Статус клуба:" else "Club tier:",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = loyalty?.level?.uppercase() ?: "SILVER",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        text = "СТУДИЯ NAILIX",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Referral code invite
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (onboardingLanguage == "ru") "🎁 Пригласить друга" else "🎁 Invite Friends",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (onboardingLanguage == "ru") "Поделитесь кодом и получите по 200 бонусов!" else "Share code to earn 200 bonus points each!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = clientProfile.referralCode.ifBlank { loyalty?.referralCode?.takeIf { it.isNotBlank() && !it.startsWith("VIC-") } ?: "NLX-PENDING" },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Button(
                                    onClick = {
                                        val code = clientProfile.referralCode.ifBlank { loyalty?.referralCode?.takeIf { it.isNotBlank() && !it.startsWith("VIC-") } ?: "NLX-PENDING" }
                                        val textToCopy = if (onboardingLanguage == "ru") {
                                            "Привет! Запишись на шикарный маникюр в студию NailiX, введи мой реферальный код $code и получи 200 приветственных бонусов на процедуру! Ссылка: https://nailix.app"
                                        } else {
                                            "Hello! Book a premium manicure at NAILIX, use my referral code $code and get 200 welcome bonus points! Link: https://nailix.app"
                                        }
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(textToCopy))
                                        Toast.makeText(context, if (onboardingLanguage == "ru") "Ссылка скопирована!" else "Referral link copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (onboardingLanguage == "ru") "Копировать" else "Copy", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Newsletter promo subscriber
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (onboardingLanguage == "ru") "✨ Дополнительные акции" else "✨ Exclusive Offers",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = if (onboardingLanguage == "ru") "Подпишитесь на рассылку акций и получите 100 бонусов!" else "Subscribe to get hot deals and 100 welcome bonus points!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { viewModel.mailingPhone.value = it },
                                placeholder = { Text("+34...", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("mailing_phone_input_dlg"),
                                textStyle = TextStyle(fontSize = 12.sp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = { 
                                    viewModel.subscribeToNewsletter()
                                    Toast.makeText(context, if (onboardingLanguage == "ru") "Подписка оформлена! 🎉" else "Subscribed! 🎉", Toast.LENGTH_SHORT).show()
                                },
                                enabled = phoneInput.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("mailing_subscribe_btn_dlg")
                            ) {
                                Text(if (onboardingLanguage == "ru") "Подписаться" else "Subscribe", fontSize = 12.sp)
                            }
                        }
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
}

