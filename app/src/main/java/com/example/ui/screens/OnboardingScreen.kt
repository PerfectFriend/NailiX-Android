package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val language by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val role by viewModel.onboardingRole.collectAsStateWithLifecycle()
    val avatarUrl by viewModel.onboardingAvatarUrl.collectAsStateWithLifecycle()
    val subTier by viewModel.subscriptionTier.collectAsStateWithLifecycle()
    val isConnectingIg by viewModel.isInstagramConnecting.collectAsStateWithLifecycle()
    val instagramError by viewModel.instagramError.collectAsStateWithLifecycle()
    val igFeed by viewModel.instagramImportFeed.collectAsStateWithLifecycle()
    val selectedIgPosts by viewModel.selectedImportPosts.collectAsStateWithLifecycle()
    val parsedInstagramPosts by viewModel.parsedInstagramPosts.collectAsStateWithLifecycle()
    val selectedDisplayPostIds by viewModel.selectedDisplayPostIds.collectAsStateWithLifecycle()
    val savedClients by viewModel.savedClientProfiles.collectAsStateWithLifecycle()
    val savedMasters by viewModel.savedMasterProfiles.collectAsStateWithLifecycle()
    val selectedSkin by viewModel.selectedSkin.collectAsStateWithLifecycle()

    val dict = OnboardingLocalizations[language] ?: OnboardingLocalizations["ru"]!!
    val colors = onboardingColorsFor(isDarkTheme)

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setOnboardingAvatar(uri.toString())
        }
    }

    val portfolioPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(20)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importLocalGalleryPhotos(uris)
        }
    }

    val currentUser = remember { runCatching { Firebase.auth.currentUser }.getOrNull() }
    var name by remember { mutableStateOf(currentUser?.displayName?.substringBefore(" ").orEmpty()) }
    var surname by remember { mutableStateOf(currentUser?.displayName?.substringAfter(" ", "").orEmpty()) }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentUser?.email.orEmpty()) }
    var alias by remember { mutableStateOf("") }

    var masterAddress by remember { mutableStateOf("Calella") }
    var masterHours by remember { mutableStateOf("10:00 - 20:00") }
    var masterExp by remember { mutableStateOf("5") }
    var masterLangs by remember { mutableStateOf("Español, Русский, English") }
    var masterCurrency by remember { mutableStateOf("EUR") }

    var showIgFeedDialog by remember { mutableStateOf(false) }
    var showLimitWarningDialog by remember { mutableStateOf(false) }
    var showAiAvatarDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedSkin == "gaudi") {
            Image(
                painter = painterResource(id = R.drawable.img_gaudi_background_1783510206970),
                contentDescription = "Gaudi Barcelona Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .skinOrnament(skinCode = selectedSkin, isButton = false, alphaMultiplier = 0.8f, isDark = isDarkTheme)
                    .skinWaveBorder(skinCode = selectedSkin, alpha = 0.6f)
            )
        }

        val overlayColors = if (isDarkTheme) {
            listOf(Color(0xFF0F172A).copy(alpha = 0.90f), Color(0xFF020617).copy(alpha = 0.95f))
        } else {
            listOf(Color(0xFFF8FAFC).copy(alpha = 0.93f), Color(0xFFEFF6FF).copy(alpha = 0.96f))
        }

        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = overlayColors)))

        if (step == 0) {
            OnboardingWelcomeTour(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                language = language,
                cardBorder = colors.cardBorder,
                cardBg = colors.cardBg
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 85.dp, max = 120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.cardBg)
                        .gaudiOrnament(isButton = false, alphaMultiplier = 1.2f, isDark = isDarkTheme)
                        .gaudiWaveBorder(alpha = 0.8f)
                        .border(3.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Card(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, colors.cardBorder.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_icon_gaudi_n_1793575273048),
                                contentDescription = "NailiX Gaudí Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = dict["welcome"]?.uppercase() ?: "NAILIX",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                fontSize = 26.sp
                            ),
                            color = colors.cardTitle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    for (i in 2..(if (role == "master") 4 else 3)) {
                        val active = step == i
                        Box(
                            modifier = Modifier
                                .size(width = if (active) 26.dp else 10.dp, height = 10.dp)
                                .clip(CircleShape)
                                .background(if (active) colors.cardBorder else if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1))
                        )
                    }
                }

                when (step) {
                    1 -> OnboardingStepLanguage(
                        language = language,
                        dict = dict,
                        colors = colors,
                        savedClientsCount = savedClients.size,
                        savedMastersCount = savedMasters.size,
                        onSelectLanguage = { viewModel.setOnboardingLanguage(it) },
                        onNextStep = { viewModel.setOnboardingStep(it) }
                    )
                    2 -> OnboardingStepRole(
                        role = role,
                        language = language,
                        dict = dict,
                        colors = colors,
                        hasSavedProfiles = savedClients.isNotEmpty() || savedMasters.isNotEmpty(),
                        viewModel = viewModel
                    )
                    3 -> OnboardingStepProfile(
                        role = role,
                        language = language,
                        dict = dict,
                        colors = colors,
                        name = name,
                        surname = surname,
                        phone = phone,
                        email = email,
                        alias = alias,
                        avatarUrl = avatarUrl,
                        onNameChange = { name = it },
                        onSurnameChange = { surname = it },
                        onPhoneChange = { phone = it },
                        onEmailChange = { email = it },
                        onAliasChange = { alias = it },
                        onPickAvatarPhoto = {
                            avatarPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onOpenAiAvatarDialog = {
                            viewModel.resetAiAvatarState()
                            showAiAvatarDialog = true
                        },
                        viewModel = viewModel
                    )
                    4 -> if (role == "master") {
                        OnboardingStepMaster(
                            language = language,
                            dict = dict,
                            colors = colors,
                            name = name,
                            surname = surname,
                            alias = alias,
                            masterAddress = masterAddress,
                            masterHours = masterHours,
                            masterExp = masterExp,
                            masterLangs = masterLangs,
                            masterCurrency = masterCurrency,
                            parsedInstagramPosts = parsedInstagramPosts,
                            selectedDisplayPostIds = selectedDisplayPostIds,
                            instagramError = instagramError,
                            onAliasChange = { alias = it },
                            onCurrencyChange = { masterCurrency = it },
                            onOpenIgFeedDialog = { showIgFeedDialog = true },
                            onPickGalleryPhotos = {
                                portfolioPhotosLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            viewModel = viewModel
                        )
                    }
                    5 -> OnboardingStepStartupProfiles(
                        language = language,
                        dict = dict,
                        colors = colors,
                        savedClients = savedClients,
                        savedMasters = savedMasters,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showAiAvatarDialog) {
        AiAvatarGeneratorDialog(
            viewModel = viewModel,
            onDismiss = { showAiAvatarDialog = false }
        )
    }

    if (showIgFeedDialog) {
        OnboardingIgFeedDialog(
            viewModel = viewModel,
            igFeed = igFeed,
            selectedIgPosts = selectedIgPosts,
            isConnectingIg = isConnectingIg,
            instagramError = instagramError,
            onPickGalleryPhotos = {
                showIgFeedDialog = false
                portfolioPhotosLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onApplySuccess = { showIgFeedDialog = false },
            onDismiss = { showIgFeedDialog = false }
        )
    }

    if (showLimitWarningDialog) {
        val limit = when (subTier) {
            "pro", "promax" -> 100
            else -> 20
        }
        OnboardingPortfolioLimitDialog(
            viewModel = viewModel,
            existingPosts = parsedInstagramPosts,
            selectedIgPostsCount = selectedIgPosts.size,
            limit = limit,
            onForceImport = {
                showLimitWarningDialog = false
                showIgFeedDialog = false
            },
            onDismiss = { showLimitWarningDialog = false }
        )
    }
}
