package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val isOnboarded by viewModel.isOnboarded.collectAsStateWithLifecycle()

    if (showSplash) {
        SplashScreen(viewModel = viewModel)
        return
    }

    if (!isOnboarded) {
        OnboardingScreen(viewModel = viewModel)
        return
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val loyalty by viewModel.loyaltyInfo.collectAsStateWithLifecycle()
    val inAppNotification by viewModel.inAppNotification.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val savedClientProfiles by viewModel.savedClientProfiles.collectAsStateWithLifecycle()
    val savedMasterProfiles by viewModel.savedMasterProfiles.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val primaryClientPhone by viewModel.primaryClientPhone.collectAsStateWithLifecycle()
    val primaryMasterId by viewModel.primaryMasterId.collectAsStateWithLifecycle()
    val selectedSkin by viewModel.selectedSkin.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val radioIsPlaying by viewModel.radioIsPlaying.collectAsStateWithLifecycle()
    val mastersList by viewModel.mastersList.collectAsStateWithLifecycle()

    var showProfileManager by remember { mutableStateOf(false) }
    var showStartupProfileSelector by remember { mutableStateOf(false) }
    var hasTriggeredStartupSelector by remember { mutableStateOf(false) }
    var showGlobalSettingsDialog by remember { mutableStateOf(false) }
    var showRadioSelector by remember { mutableStateOf(false) }
    var showUpcomingBookingsOverlay by remember { mutableStateOf(false) }
    var showLoyaltyBonusesOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(savedClientProfiles, savedMasterProfiles) {
        if (!hasTriggeredStartupSelector && (savedClientProfiles.size + savedMasterProfiles.size > 1)) {
            showStartupProfileSelector = true
            hasTriggeredStartupSelector = true
        }
    }

    // Observe subscription toast notifications
    LaunchedEffect(Unit) {
        viewModel.isSubscribedSuccessfully.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.referralMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    BackHandler(
        enabled = showRadioSelector || isAdminMode || (userRole == "client" && currentTab != "home")
    ) {
        when {
            showRadioSelector -> showRadioSelector = false
            isAdminMode -> viewModel.isAdminMode.value = false
            userRole == "client" && currentTab != "home" -> viewModel.navigateTo("home")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // High quality wallpaper background matching the selected skin!
        when (selectedSkin) {
            "gaudi" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_gaudi_background_1783510206970),
                    contentDescription = "Gaudi Barcelona Wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            "van_gogh" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_van_gogh_1784604400808),
                    contentDescription = "Van Gogh Starry Night Wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            "cosmos" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_cosmos_1784604418322),
                    contentDescription = "Cosmic Nebulae Wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            "flowers" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_flowers_1784604434602),
                    contentDescription = "Floral Watercolor Wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .skinOrnament(skinCode = selectedSkin, isButton = false, alphaMultiplier = 0.8f, isDark = isDarkTheme)
                        .skinWaveBorder(skinCode = selectedSkin, alpha = 0.6f)
                )
            }
        }

        // Extremely translucent glassmorphic overlay so the gorgeous Barcelona pattern is fully visible!
        val overlayColors = if (isDarkTheme) {
            listOf(
                Color(0xFF0F172A).copy(alpha = 0.75f),
                Color(0xFF020617).copy(alpha = 0.85f)
            )
        } else {
            listOf(
                Color(0xFFEFF6FF).copy(alpha = 0.5f),
                Color(0xFFDBEAFE).copy(alpha = 0.6f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = overlayColors)
                )
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp && !isAdminMode && userRole == "client"

        Scaffold(
            containerColor = Color.Transparent, // Transparent so the beautiful Gaudi background shines through!
            contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .skinOrnament(skinCode = selectedSkin, isButton = false, alphaMultiplier = 0.5f, isDark = isDarkTheme)
                    .skinWaveBorder(skinCode = selectedSkin, alpha = 0.45f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            ) {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { showProfileManager = true },
                            modifier = Modifier.testTag("profile_manager_top_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Open Profile Manager",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                modifier = Modifier.size(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_icon_gaudi_n_1793575273048),
                                    contentDescription = "NailiX Gaudí icon",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NailiX",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (userRole == "master") {
                                // Masters see node administration/salon owner panel, with zero client-oriented shortcuts
                                IconButton(
                                    onClick = { viewModel.isAdminMode.value = !isAdminMode },
                                    modifier = Modifier.testTag("admin_mode_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isAdminMode) Icons.Default.Store else Icons.Outlined.Store,
                                        contentDescription = "Панель владельца",
                                        tint = if (isAdminMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            } else {
                                // Profile switch & quick account info
                                IconButton(
                                    onClick = { showProfileManager = true },
                                    modifier = Modifier.testTag("client_profile_top_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Profile",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showGlobalSettingsDialog = true },
                                modifier = Modifier.testTag("global_settings_top_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Global Settings",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isAdminMode && userRole == "client" && !useNavigationRail) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentTab == "home" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("home")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "home" && !showRadioSelector) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Главная",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Главная", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = currentTab == "booking" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("booking")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "booking" && !showRadioSelector) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                                contentDescription = "Запись",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Запись", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = currentTab == "designs" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("designs")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "designs" && !showRadioSelector) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "ART",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("ART", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = currentTab == "loyalty" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("loyalty")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "loyalty" && !showRadioSelector) Icons.Default.WorkspacePremium else Icons.Outlined.WorkspacePremium,
                                contentDescription = "Клуб",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Клуб", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = currentTab == "portfolio" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("portfolio")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "portfolio" && !showRadioSelector) Icons.Default.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                                contentDescription = "Галерея",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Галерея", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = showRadioSelector,
                        onClick = { showRadioSelector = true },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (radioIsPlaying) {
                                        Badge(
                                            containerColor = Color(0xFF10B981),
                                            contentColor = Color.White
                                        ) {
                                            Text("ON", fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (radioIsPlaying) Icons.Default.Radio else Icons.Outlined.Radio,
                                    contentDescription = "Радио",
                                    tint = if (radioIsPlaying) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = { Text("Радио", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (useNavigationRail) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier.testTag("adaptive_navigation_rail")
                ) {
                    NavigationRailItem(
                        selected = currentTab == "home" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("home")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "home" && !showRadioSelector) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Главная"
                            )
                        },
                        label = { Text("Главная", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationRailItem(
                        selected = currentTab == "booking" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("booking")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "booking" && !showRadioSelector) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                                contentDescription = "Запись"
                            )
                        },
                        label = { Text("Запись", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationRailItem(
                        selected = currentTab == "designs" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("designs")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "designs" && !showRadioSelector) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "ART"
                            )
                        },
                        label = { Text("ART", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationRailItem(
                        selected = currentTab == "loyalty" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("loyalty")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "loyalty" && !showRadioSelector) Icons.Default.WorkspacePremium else Icons.Outlined.WorkspacePremium,
                                contentDescription = "Клуб"
                            )
                        },
                        label = { Text("Клуб", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationRailItem(
                        selected = currentTab == "portfolio" && !showRadioSelector,
                        onClick = {
                            showRadioSelector = false
                            viewModel.navigateTo("portfolio")
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "portfolio" && !showRadioSelector) Icons.Default.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                                contentDescription = "Галерея"
                            )
                        },
                        label = { Text("Галерея", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                    NavigationRailItem(
                        selected = showRadioSelector,
                        onClick = { showRadioSelector = true },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (radioIsPlaying) {
                                        Badge(
                                            containerColor = Color(0xFF10B981),
                                            contentColor = Color.White
                                        ) {
                                            Text("ON", fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (radioIsPlaying) Icons.Default.Radio else Icons.Outlined.Radio,
                                    contentDescription = "Радио",
                                    tint = if (radioIsPlaying) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        },
                        label = { Text("Радио", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                }
            }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
            // Screen contents - Maximize full available screen without bottom ad overlays
            if (userRole == "master") {
                if (isAdminMode) {
                    AdminDashboardScreen(viewModel = viewModel)
                } else {
                    MasterWorkspaceTab(viewModel = viewModel)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        "home" -> HomeScreenTab(viewModel = viewModel)
                        "booking" -> BookingScreenTab(viewModel = viewModel)
                        "designs" -> DesignsScreenTab(viewModel = viewModel)
                        "loyalty" -> LoyaltyScreenTab(viewModel = viewModel)
                        "portfolio" -> PortfolioScreenTab(viewModel = viewModel)
                    }
                }
            }

            // Beautiful In-App simulated Push Notification alert banner
            androidx.compose.animation.AnimatedVisibility(
                visible = inAppNotification != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                inAppNotification?.let { text ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.dismissInAppNotification() }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Notification badge",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PUSH-УВЕДОМЛЕНИЕ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.dismissInAppNotification() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close push alert"
                                )
                            }
                        }
                    }
                }
            }

            // Booking Success dialog modal
            val successBooking by viewModel.bookingSuccessDialog.collectAsStateWithLifecycle()
            successBooking?.let { booking ->
                val bookedMaster = mastersList.find { it.name.equals(booking.masterName, ignoreCase = true) }
                BookingSuccessDialog(
                    booking = booking,
                    language = onboardingLanguage,
                    masterAddress = bookedMaster?.address.orEmpty(),
                    masterLat = bookedMaster?.lat ?: 41.6150,
                    masterLon = bookedMaster?.lon ?: 2.6620,
                    onDismiss = { viewModel.dismissSuccessDialog() }
                )
            }

            // Studio booking confirmation modal
            val pendingBooking by viewModel.pendingConfirmationBooking.collectAsStateWithLifecycle()
            pendingBooking?.let { booking ->
                BookingConfirmationDialog(
                    booking = booking,
                    viewModel = viewModel,
                    onDismiss = { viewModel.dismissBookingConfirmation() }
                )
            }


            if (showRadioSelector) {
                RadioSelectorDialog(
                    viewModel = viewModel,
                    onDismiss = { showRadioSelector = false }
                )
            }

            if (showProfileManager) {
                ProfileManagerDialog(
                    viewModel = viewModel,
                    onDismiss = { showProfileManager = false }
                )
            }

            if (showStartupProfileSelector) {
                StartupProfileSelectorDialog(
                    viewModel = viewModel,
                    onDismiss = { showStartupProfileSelector = false }
                )
            }

            if (showUpcomingBookingsOverlay) {
                UpcomingBookingsDialog(
                    viewModel = viewModel,
                    onDismiss = { showUpcomingBookingsOverlay = false }
                )
            }

            if (showLoyaltyBonusesOverlay) {
                LoyaltyBonusesDialog(
                    viewModel = viewModel,
                    onDismiss = { showLoyaltyBonusesOverlay = false }
                )
            }

            val globalCurrency by viewModel.globalCurrency.collectAsStateWithLifecycle()
            val isPremiumState by viewModel.isPremium.collectAsStateWithLifecycle()
            if (showGlobalSettingsDialog) {
                GlobalSettingsDialog(
                    viewModel = viewModel,
                    currentSkin = selectedSkin,
                    currentCurrency = globalCurrency,
                    currentLanguage = onboardingLanguage,
                    isDarkTheme = isDarkTheme,
                    isPremium = isPremiumState,
                    onTogglePremium = { viewModel.togglePremium() },
                    onSkinChange = { viewModel.setSelectedSkin(it) },
                    onCurrencyChange = { viewModel.setGlobalCurrency(it) },
                    onLanguageChange = { viewModel.setOnboardingLanguage(it) },
                    onDarkThemeChange = { viewModel.setDarkTheme(it) },
                    onDismiss = { showGlobalSettingsDialog = false }
                )
            }
        }
        }
        }
        }
    }
}
}

