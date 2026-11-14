package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

data class OnboardingSlideData(
    val badge: String,
    val badgeColor: Color,
    val title: String,
    val subtitle: String,
    val imageRes: Int?,
    val isInteractiveNails: Boolean = false,
    val highlights: List<Triple<String, String, String>>
)

@Composable
fun OnboardingWelcomeTour(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    language: String,
    cardBorder: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    var selectedShapeIndex by remember { mutableIntStateOf(0) }
    val nailShapes = listOf(
        "Миндаль" to "Almond 💅",
        "Балерина" to "Ballerina 🩰",
        "Овал" to "Oval ✨",
        "Стилет" to "Stiletto ⚡",
        "Квадрат" to "Square 💎"
    )

    val slides: List<OnboardingSlideData> = remember(language) {
        when (language) {
            "ru" -> listOf(
                OnboardingSlideData(
                    badge = "✨ HAUTE BEAUTY",
                    badgeColor = Color(0xFFF59E0B),
                    title = "NailiX Studio",
                    subtitle = "Премиальный маникюр и авторский нейл-арт",
                    imageRes = R.drawable.img_splash_manicure_1783727467218,
                    highlights = listOf(
                        Triple("💅", "Трендовые покрытия", "Люкс-материалы, японский спа-уход и стойкий зеркальный глянец."),
                        Triple("🌿", "100% Стерильность", "Медицинский трёхступенчатый автоклав и крафт-пакеты."),
                        Triple("☕", "Атмосфера релакса", "Свежий кофе, напитки и персональная забота о каждом госте.")
                    )
                ),
                OnboardingSlideData(
                    badge = "⏱️ SMART BOOKING",
                    badgeColor = Color(0xFF00A896),
                    title = "Запись 24/7 без звонков",
                    subtitle = "Бронирование в 2 клика за 30 секунд",
                    imageRes = R.drawable.img_salon_banner_1783410932283,
                    highlights = listOf(
                        Triple("📅", "Выбор топ-мастера", "Смотрите рейтинг, специализацию и свободные слоты."),
                        Triple("🔔", "Умные напоминания", "СМС и push-уведомления за день и 2 часа до начала."),
                        Triple("📍", "Удобная локация", "Быстрый маршрут и интерактивная карта прямо в студию.")
                    )
                ),
                OnboardingSlideData(
                    badge = "🎨 ART & 3D ПРИМЕРКА",
                    badgeColor = Color(0xFFEC4899),
                    title = "ART и выбор формы",
                    subtitle = "Подберите безупречный дизайн до визита",
                    imageRes = null,
                    isInteractiveNails = true,
                    highlights = listOf(
                        Triple("📐", "Примерка формы", "Миндаль, Балерина, Овал, Стилет или Квадрат под вашу руку."),
                        Triple("🎨", "Каталог шедевров ART", "Сотни актуальных авторских дизайнов и градиентов."),
                        Triple("📸", "Избранное из Instagram", "Сохраняйте понравившиеся идеи в свой личный мудборд.")
                    )
                ),
                OnboardingSlideData(
                    badge = "💎 КЛУБ ПРИВИЛЕГИЙ",
                    badgeColor = Color(0xFF6366F1),
                    title = "Бонусы и привилегии",
                    subtitle = "Красота с приятной выгодой для вас",
                    imageRes = R.drawable.img_referral_banner_1783995249167,
                    highlights = listOf(
                        Triple("🎁", "Кэшбэк до 15%", "Баллы за каждое посещение для оплаты любых процедур."),
                        Triple("👯‍♀️", "500 ₽ за подругу", "Приглашайте друзей по реферальному промокоду."),
                        Triple("👑", "Подарки и VIP-статус", "Специальные сюрпризы в честь вашего Дня Рождения.")
                    )
                )
            )
            "es" -> listOf(
                OnboardingSlideData(
                    badge = "✨ ALTA MANICURA",
                    badgeColor = Color(0xFFF59E0B),
                    title = "NailiX Studio",
                    subtitle = "Estudio exclusivo de arte en uñas y cuidado de lujo",
                    imageRes = R.drawable.img_splash_manicure_1783727467218,
                    highlights = listOf(
                        Triple("💅", "Esmaltado en tendencia", "Materiales de alta gama y brillo duradero."),
                        Triple("🌿", "Esterilización 100%", "Protocolo médico con autoclave para máxima higiene."),
                        Triple("☕", "Confort total", "Ambiente relajante, café de especialidad y atención única.")
                    )
                ),
                OnboardingSlideData(
                    badge = "⏱️ CITA ONLINE 24/7",
                    badgeColor = Color(0xFF00A896),
                    title = "Reserva en 2 clics",
                    subtitle = "Elige estilista y horario sin esperas",
                    imageRes = R.drawable.img_salon_banner_1783410932283,
                    highlights = listOf(
                        Triple("📅", "Tu estilista favorito", "Consulta calificaciones y disponibilidad en vivo."),
                        Triple("🔔", "Recordatorios SMS", "Avisos automáticos para que nunca olvides tu cita."),
                        Triple("📍", "Ubicación céntrica", "Navegación directa y acceso fácil al salón.")
                    )
                ),
                OnboardingSlideData(
                    badge = "🎨 ART Y FORMAS",
                    badgeColor = Color(0xFFEC4899),
                    title = "ART y forma de uñas",
                    subtitle = "Encuentra tu estilo perfecto antes de llegar",
                    imageRes = null,
                    isInteractiveNails = true,
                    highlights = listOf(
                        Triple("📐", "Prueba de formas", "Almendra, Bailarina, Oval, Estilete o Cuadrada."),
                        Triple("🎨", "Galería ART exclusiva", "Colección de diseños de autor para cada ocasión."),
                        Triple("📸", "Guarda tus favoritos", "Sincroniza tus fotos preferidas de Instagram.")
                    )
                ),
                OnboardingSlideData(
                    badge = "💎 CLUB DE PRIVILEGIOS",
                    badgeColor = Color(0xFF6366F1),
                    title = "Recompensas NailiX",
                    subtitle = "Acumula puntos y regalos con cada cita",
                    imageRes = R.drawable.img_referral_banner_1783995249167,
                    highlights = listOf(
                        Triple("🎁", "Hasta 15% Cashback", "Puntos canjeables por servicios y tratamientos."),
                        Triple("👯‍♀️", "Premio por amigos", "Comparte tu código y gana saldo adicional."),
                        Triple("👑", "Beneficios VIP", "Detalles exclusivos y sorpresas de cumpleaños.")
                    )
                )
            )
            else -> listOf(
                OnboardingSlideData(
                    badge = "✨ HAUTE BEAUTY",
                    badgeColor = Color(0xFFF59E0B),
                    title = "NailiX Studio",
                    subtitle = "Haute nail art and premium manicure aesthetics",
                    imageRes = R.drawable.img_splash_manicure_1783727467218,
                    highlights = listOf(
                        Triple("💅", "Trending Coatings", "Top-tier formulas, Japanese spa care and mirror gloss."),
                        Triple("🌿", "100% Sterilization", "Medical three-stage autoclave hygiene standard."),
                        Triple("☕", "Relaxing Atmosphere", "Specialty coffee, soothing music and personalized care.")
                    )
                ),
                OnboardingSlideData(
                    badge = "⏱️ SMART BOOKING",
                    badgeColor = Color(0xFF00A896),
                    title = "24/7 Smart Booking",
                    subtitle = "Instant appointment in 2 taps without calling",
                    imageRes = R.drawable.img_salon_banner_1783410932283,
                    highlights = listOf(
                        Triple("📅", "Top Master Selection", "Check ratings, portfolios and live open slots."),
                        Triple("🔔", "Smart Alerts", "Automated SMS and push notifications for appointments."),
                        Triple("📍", "Prime Location", "Interactive route guidance straight to our studio.")
                    )
                ),
                OnboardingSlideData(
                    badge = "🎨 ART & VIRTUAL TRY-ON",
                    badgeColor = Color(0xFFEC4899),
                    title = "ART & Nail Shapes",
                    subtitle = "Discover your dream design before arriving",
                    imageRes = null,
                    isInteractiveNails = true,
                    highlights = listOf(
                        Triple("📐", "Interactive Shapes", "Almond, Ballerina, Oval, Stiletto, or Square."),
                        Triple("🎨", "Curated ART Gallery", "Seasonal runway inspirations and custom nail art."),
                        Triple("📸", "Instagram Moodboard", "Save reference photos directly to favorites.")
                    )
                ),
                OnboardingSlideData(
                    badge = "💎 PRIVILEGE CLUB",
                    badgeColor = Color(0xFF6366F1),
                    title = "Privileges & Cashback",
                    subtitle = "Rewards and benefits on every single visit",
                    imageRes = R.drawable.img_referral_banner_1783995249167,
                    highlights = listOf(
                        Triple("🎁", "Up to 15% Cashback", "Earn loyalty points towards any future service."),
                        Triple("👯‍♀️", "Referral Rewards", "Invite friends and unlock instant studio bonuses."),
                        Triple("👑", "VIP Status", "Birthday surprises and secret members-only specials.")
                    )
                )
            )
        }
    }

    val textColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F2C59)
    val subtextColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569)
    val cardBackground = if (isDarkTheme) Color(0xFF1E293B).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_gaudi_n_1793575273048),
                        contentDescription = "NailiX Gaudí Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column {
                    Text(
                        text = "NAILIX",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize = 18.sp
                        ),
                        color = textColor
                    )
                    Text(
                        text = "v1.0 Release",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = cardBorder
                    )
                }
            }

            // Language quick switcher & Skip button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "ru" to "🇷🇺",
                    "en" to "🇬🇧",
                    "es" to "🇪🇸"
                ).forEach { (code, flag) ->
                    val isSelected = language == code
                    Surface(
                        modifier = Modifier
                            .clickable { viewModel.setOnboardingLanguage(code) }
                            .padding(2.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) cardBorder.copy(alpha = 0.25f) else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.5.dp, cardBorder) else null
                    ) {
                        Text(
                            text = flag,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(
                    onClick = { viewModel.setOnboardingStep(2) },
                    modifier = Modifier.testTag("skip_tour_btn")
                ) {
                    Text(
                        text = if (language == "ru") "Пропустить" else if (language == "es") "Omitir" else "Skip",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = cardBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- HORIZONTAL PAGER SLIDES ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val slide = slides[pageIndex]

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .background(cardBackground, RoundedCornerShape(26.dp))
                    .skinOrnament(skinCode = "gaudi", isButton = false, alphaMultiplier = 1.0f, isDark = isDarkTheme)
                    .skinWaveBorder(skinCode = "gaudi", alpha = 0.5f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, cardBorder.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Badge Pill
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = slide.badgeColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, slide.badgeColor.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = slide.badge,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    fontSize = 12.sp
                                ),
                                color = slide.badgeColor,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Title & Subtitle
                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.Center
                            ),
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = slide.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            ),
                            color = subtextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Hero Banner or Interactive Shapes
                        if (slide.imageRes != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(18.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                border = BorderStroke(1.5.dp, cardBorder.copy(alpha = 0.4f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = slide.imageRes),
                                        contentDescription = slide.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.4f)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        } else if (slide.isInteractiveNails) {
                            // Interactive 3D Nail Shapes Selector
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFEFF6FF),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .border(1.5.dp, cardBorder.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (language == "ru") "Попробуйте форму ногтей прямо сейчас:" else "Try selecting nail shapes live:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = cardBorder
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    nailShapes.forEachIndexed { idx, (ruName, enName) ->
                                        val isSelected = selectedShapeIndex == idx
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedShapeIndex = idx }
                                                .testTag("shape_preview_$idx"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) cardBorder else if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                                            border = BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) cardBorder else cardBorder.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = if (isSelected) (if (isDarkTheme) Color(0xFF0F172A) else Color.White) else cardBorder,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = if (language == "ru") ruName else enName.split(" ")[0],
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    color = if (isSelected) (if (isDarkTheme) Color(0xFF0F172A) else Color.White) else textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Feature Highlights with Icons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            slide.highlights.forEach { (iconStr, hTitle, hDesc) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFF8FAFC),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .border(1.dp, cardBorder.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = iconStr,
                                        fontSize = 26.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = hTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = hDesc,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            ),
                                            color = subtextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- PAGER INDICATOR DOTS ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            for (i in 0 until 4) {
                val isSelected = pagerState.currentPage == i
                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 30.dp else 10.dp,
                    label = "DotWidth"
                )
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) cardBorder else cardBorder.copy(alpha = 0.25f)
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(i)
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- ACTION BUTTONS ---
        val isLastSlide = pagerState.currentPage == 3

        if (isLastSlide) {
            // Big final CTA on slide 3
            Button(
                onClick = { viewModel.setOnboardingStep(2) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .skinOrnament(skinCode = "gaudi", isButton = true, alphaMultiplier = 2.0f, isDark = isDarkTheme)
                    .skinWaveBorder(skinCode = "gaudi", alpha = 0.8f)
                    .testTag("tour_start_role_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (language == "ru") "🌟 Выбрать роль и начать"
                               else if (language == "es") "🌟 Elegir rol y comenzar"
                               else "🌟 Choose Role & Get Started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = if (isDarkTheme) Color(0xFF0F172A) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // Next / Skip Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.setOnboardingStep(2) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("tour_skip_bottom_btn"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, cardBorder.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cardBorder)
                ) {
                    Text(
                        text = if (language == "ru") "Пропустить" else if (language == "es") "Omitir" else "Skip",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = cardBorder
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(54.dp)
                        .skinOrnament(skinCode = "gaudi", isButton = true, alphaMultiplier = 1.8f, isDark = isDarkTheme)
                        .skinWaveBorder(skinCode = "gaudi", alpha = 0.8f)
                        .testTag("tour_next_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = cardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (language == "ru") "Далее" else if (language == "es") "Siguiente" else "Next",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Direct Quick Start as Guest (zero friction entry point)
        TextButton(
            onClick = { viewModel.quickStartAsGuest("client") },
            modifier = Modifier.testTag("quick_guest_start_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = cardBorder,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (language == "ru") "⚡ Быстрый вход (войти как Гость)"
                       else if (language == "es") "⚡ Inicio rápido (como Invitado)"
                       else "⚡ Quick start (enter as Guest)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = cardBorder
                )
            )
        }
    }
}
