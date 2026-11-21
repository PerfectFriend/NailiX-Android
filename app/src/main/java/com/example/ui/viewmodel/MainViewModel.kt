package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiAdviceUiState
import com.example.ai.AiImageUiState
import com.example.ai.NailAiRepository
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Booking
import com.example.data.CloudSyncRepository
import com.example.data.InstagramPost
import com.example.data.InstagramRealScraper
import com.example.data.LoyaltyTransaction
import com.example.data.MasterNailArtEntity
import com.example.data.ReferredFriend
import com.example.data.Reminder
import com.example.data.ServiceEntity
import com.example.data.ServiceItem
import com.example.data.SovereignNodeManager
import com.example.data.UserLoyalty
import com.example.notification.FcmNotificationManager
import com.example.util.VisitTicketCryptoManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainViewModel @JvmOverloads constructor(
    application: Application,
    val currentUserId: String = "",
    private val cloudSyncRepository: CloudSyncRepository? = null,
    private val nailAiRepository: NailAiRepository = NailAiRepository()
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)
    private val prefs = application.getSharedPreferences("nailix_prefs", Context.MODE_PRIVATE)
    private val radioController = RadioPlayerController(viewModelScope)

    // ----------------------------------------------------
    // SALON AGGREGATOR ENGINE STATES
    // ----------------------------------------------------
    private val _userRole = MutableStateFlow("client") // "client", "master"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    val isPremium = MutableStateFlow(false)
    val savedClientProfiles = MutableStateFlow<List<ClientProfile>>(emptyList())
    val savedMasterProfiles = MutableStateFlow<List<MasterProfile>>(emptyList())
    val primaryClientPhone = MutableStateFlow("")
    val primaryMasterId = MutableStateFlow<Int?>(null)

    val clientName = MutableStateFlow("")
    val clientPhone = MutableStateFlow("")

    private fun clientProfileToJson(profile: ClientProfile): String {
        val json = JSONObject()
        json.put("name", profile.name)
        json.put("phone", profile.phone)
        json.put("email", profile.email)
        json.put("totalBookings", profile.totalBookings)
        json.put("totalSpent", profile.totalSpent)
        json.put("isSubscribed", profile.isSubscribed)
        json.put("rating", profile.rating)
        json.put("ratingCount", profile.ratingCount)
        json.put("homeAddress", profile.homeAddress)
        json.put("homeLat", profile.homeLat)
        json.put("homeLon", profile.homeLon)
        json.put("workAddress", profile.workAddress)
        json.put("workLat", profile.workLat)
        json.put("workLon", profile.workLon)
        json.put("referralCode", profile.referralCode)
        return json.toString()
    }

    private fun jsonToClientProfile(jsonStr: String): ClientProfile {
        val json = JSONObject(jsonStr)
        val fallbackCode = if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
            cloudSyncRepository.generateReferralCodeForUid(currentUserId)
        } else {
            ""
        }
        return ClientProfile(
            name = json.optString("name", ""),
            phone = json.optString("phone", ""),
            email = json.optString("email", ""),
            totalBookings = json.optInt("totalBookings", 0),
            totalSpent = json.optDouble("totalSpent", 0.0),
            isSubscribed = json.optBoolean("isSubscribed", false),
            rating = json.optDouble("rating", 5.0),
            ratingCount = json.optInt("ratingCount", 1),
            homeAddress = json.optString("homeAddress", "Calella"),
            homeLat = json.optDouble("homeLat", 41.6140),
            homeLon = json.optDouble("homeLon", 2.6600),
            workAddress = json.optString("workAddress", "Calella"),
            workLat = json.optDouble("workLat", 41.6160),
            workLon = json.optDouble("workLon", 2.6630),
            referralCode = json.optString("referralCode", fallbackCode).ifBlank { fallbackCode }
        )
    }

    private fun masterProfileToJson(profile: MasterProfile): String {
        val json = JSONObject()
        json.put("id", profile.id)
        json.put("name", profile.name)
        json.put("address", profile.address)
        json.put("lat", profile.lat)
        json.put("lon", profile.lon)
        json.put("workingSchedule", profile.workingSchedule)
        json.put("experienceYears", profile.experienceYears)
        val langsArr = JSONArray()
        profile.languages.forEach { langsArr.put(it) }
        json.put("languages", langsArr)
        json.put("instagramUsername", profile.instagramUsername)
        json.put("rating", profile.rating)
        json.put("ratingCount", profile.ratingCount)
        json.put("isUserCreated", profile.isUserCreated)
        json.put("currency", profile.currency)
        json.put("ownerUid", profile.ownerUid)
        return json.toString()
    }

    private fun jsonToMasterProfile(jsonStr: String): MasterProfile {
        val json = JSONObject(jsonStr)
        val langsArr = json.optJSONArray("languages")
        val langsList = mutableListOf<String>()
        if (langsArr != null) {
            for (i in 0 until langsArr.length()) {
                langsList.add(langsArr.getString(i))
            }
        }
        return MasterProfile(
            id = json.optInt("id", 0),
            name = json.optString("name", ""),
            address = json.optString("address", ""),
            lat = json.optDouble("lat", 0.0),
            lon = json.optDouble("lon", 0.0),
            workingSchedule = json.optString("workingSchedule", ""),
            experienceYears = json.optInt("experienceYears", 0),
            languages = langsList,
            instagramUsername = json.optString("instagramUsername", ""),
            rating = json.optDouble("rating", 5.0),
            ratingCount = json.optInt("ratingCount", 1),
            isUserCreated = json.optBoolean("isUserCreated", false),
            currency = json.optString("currency", "EUR"),
            ownerUid = json.optString("ownerUid", "")
        )
    }

    fun saveProfiles() {
        val clientArray = JSONArray()
        savedClientProfiles.value.forEach { clientArray.put(clientProfileToJson(it)) }

        val masterArray = JSONArray()
        savedMasterProfiles.value.forEach { masterArray.put(masterProfileToJson(it)) }

        prefs.edit()
            .putString("saved_clients", clientArray.toString())
            .putString("saved_masters", masterArray.toString())
            .putString("primary_client_phone", primaryClientPhone.value)
            .putInt("primary_master_id", primaryMasterId.value ?: -1)
            .putBoolean("is_premium_sub", isPremium.value)
            .putString("global_skin", _selectedSkin.value)
            .putString("global_currency", _globalCurrency.value)
            .putString("onboarding_language", _onboardingLanguage.value)
            .putBoolean("is_onboarded", _isOnboarded.value)
            .putBoolean("is_dark_theme", _isDarkTheme.value)
            .apply()
    }

    fun loadProfiles() {
        val clientsJsonStr = prefs.getString("saved_clients", null)
        val mastersJsonStr = prefs.getString("saved_masters", null)
        val primClientPhone = prefs.getString("primary_client_phone", "") ?: ""
        val primMasterId = prefs.getInt("primary_master_id", -1)
        val isPrem = prefs.getBoolean("is_premium_sub", false)

        _selectedSkin.value = prefs.getString("global_skin", "gaudi") ?: "gaudi"
        _globalCurrency.value = prefs.getString("global_currency", "EUR") ?: "EUR"
        _isDarkTheme.value = prefs.getBoolean("is_dark_theme", true)
        val savedLang = prefs.getString("onboarding_language", null)
        if (savedLang != null) {
            _onboardingLanguage.value = savedLang
        }

        isPremium.value = isPrem
        _isOnboarded.value = prefs.getBoolean("is_onboarded", false)

        val clientList = mutableListOf<ClientProfile>()
        if (clientsJsonStr != null) {
            try {
                val arr = JSONArray(clientsJsonStr)
                for (i in 0 until arr.length()) {
                    clientList.add(jsonToClientProfile(arr.getString(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val masterList = mutableListOf<MasterProfile>()
        if (mastersJsonStr != null) {
            try {
                val arr = JSONArray(mastersJsonStr)
                for (i in 0 until arr.length()) {
                    masterList.add(jsonToMasterProfile(arr.getString(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        clientList.removeAll {
            it.phone == "+34612345678" ||
                it.name == "Дарья Козлова" ||
                it.phone == "+7 999 000-00-00"
        }
        masterList.removeAll {
            it.id == 99 ||
                it.id in listOf(1, 2, 3) ||
                it.instagramUsername == "victoria1001art"
        }

        primaryClientPhone.value = primClientPhone
        primaryMasterId.value = if (primMasterId != -1) primMasterId else null

        savedClientProfiles.value = clientList
        savedMasterProfiles.value = masterList

        val resolvedCode = if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
            cloudSyncRepository.generateReferralCodeForUid(currentUserId)
        } else {
            ""
        }

        if (clientList.isNotEmpty()) {
            val activeClient = clientList.find { it.phone == primaryClientPhone.value } ?: clientList.first()
            _clientProfile.value = if (activeClient.referralCode.isBlank()) {
                activeClient.copy(referralCode = resolvedCode)
            } else {
                activeClient
            }
            clientName.value = activeClient.name
            clientPhone.value = activeClient.phone
        } else {
            _clientProfile.value = ClientProfile(
                name = "",
                phone = "",
                email = "",
                homeAddress = "",
                workAddress = "",
                referralCode = resolvedCode
            )
            clientName.value = ""
            clientPhone.value = ""
        }

        if (masterList.isNotEmpty()) {
            val activeMaster = masterList.find { it.id == primaryMasterId.value } ?: masterList.first()
            _registeredMasterProfile.value = activeMaster
            _mastersList.value = masterList
        } else {
            _registeredMasterProfile.value = null
            _mastersList.value = emptyList()
        }

        if (!_isOnboarded.value && (clientList.isNotEmpty() || masterList.isNotEmpty())) {
            _onboardingStep.value = 5
        } else if (!_isOnboarded.value) {
            _onboardingStep.value = 0
        }
    }

    fun activateClientProfile(phone: String): Boolean {
        val list = savedClientProfiles.value
        val isPrim = (phone == primaryClientPhone.value)
        if (!isPremium.value && !isPrim && list.size > 1) {
            return false
        }
        val profile = list.find { it.phone == phone } ?: return false
        _clientProfile.value = profile
        clientName.value = profile.name
        clientPhone.value = profile.phone
        _userRole.value = "client"
        syncUserProfileToCloud()
        return true
    }

    fun activateMasterProfile(id: Int): Boolean {
        val list = savedMasterProfiles.value
        val isPrim = (id == primaryMasterId.value)
        if (!isPremium.value && !isPrim && list.size > 1) {
            return false
        }
        val profile = list.find { it.id == id } ?: return false
        _registeredMasterProfile.value = profile
        _userRole.value = "master"
        syncUserProfileToCloud()
        return true
    }

    fun createNewClientProfile(name: String, phone: String, email: String = ""): String? {
        val list = savedClientProfiles.value
        val maxAllowed = if (isPremium.value) 3 else 1
        if (list.size >= maxAllowed) {
            return if (list.size >= 3) "MAX_REACHED" else "PREMIUM_REQUIRED"
        }
        if (list.any { it.phone == phone }) {
            return "DUPLICATE"
        }
        val newCode = if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
            cloudSyncRepository.generateReferralCodeForUid(currentUserId)
        } else {
            ""
        }
        val newProfile = ClientProfile(
            name = name,
            phone = phone,
            email = email,
            homeAddress = "Calella, Carrer de Jovara 100",
            homeLat = 41.6140,
            homeLon = 2.6600,
            workAddress = "Calella, Carrer de Sant Jaume 150",
            workLat = 41.6160,
            workLon = 2.6630,
            referralCode = newCode
        )
        val newList = list + newProfile
        savedClientProfiles.value = newList
        if (primaryClientPhone.value.isEmpty()) {
            primaryClientPhone.value = phone
        }
        saveProfiles()
        syncUserProfileToCloud()
        return null
    }

    fun createNewMasterProfile(
        name: String,
        address: String,
        schedule: String,
        experience: Int,
        languages: List<String>,
        instagram: String
    ): String? {
        val list = savedMasterProfiles.value
        val maxAllowed = if (isPremium.value) 3 else 1
        if (list.size >= maxAllowed) {
            return if (list.size >= 3) "MAX_REACHED" else "PREMIUM_REQUIRED"
        }
        if (list.any { it.instagramUsername.equals(instagram, ignoreCase = true) }) {
            return "DUPLICATE"
        }
        val newId = (list.maxOfOrNull { it.id } ?: 100) + 1
        val newMaster = MasterProfile(
            id = newId,
            name = name,
            address = address,
            lat = 41.6150,
            lon = 2.6620,
            workingSchedule = schedule.ifEmpty { "Пн-Пт: 09:00 - 18:00" },
            experienceYears = experience,
            languages = languages.ifEmpty { listOf("Испанский") },
            instagramUsername = instagram.ifEmpty { "nail_artist" },
            rating = 5.0,
            ratingCount = 1,
            isUserCreated = true
        )
        val newList = list + newMaster
        savedMasterProfiles.value = newList
        _mastersList.value = newList
        if (primaryMasterId.value == null) {
            primaryMasterId.value = newId
        }
        saveProfiles()
        publishMasterToCloud(newMaster)
        return null
    }

    fun setPrimaryClientProfile(phone: String) {
        primaryClientPhone.value = phone
        saveProfiles()
    }

    fun setPrimaryMasterProfile(id: Int) {
        primaryMasterId.value = id
        saveProfiles()
    }

    fun togglePremium() {
        isPremium.value = !isPremium.value
        saveProfiles()
    }

    fun switchUserRole(role: String) {
        _userRole.value = role
        if (role == "master" && _registeredMasterProfile.value == null && savedMasterProfiles.value.isNotEmpty()) {
            val prim = savedMasterProfiles.value.find { it.id == primaryMasterId.value } ?: savedMasterProfiles.value.first()
            _registeredMasterProfile.value = prim
        }
        syncUserProfileToCloud()
    }

    // ----------------------------------------------------
    // ONBOARDING, APPEARANCE & FIREBASE AI LOGIC STATES
    // ----------------------------------------------------
    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _onboardingStep = MutableStateFlow(0)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    private val _onboardingLanguage = MutableStateFlow("ru")
    val onboardingLanguage: StateFlow<String> = _onboardingLanguage.asStateFlow()

    val localizationMap: StateFlow<Map<String, String>> = combine(
        repository.allLocalizations,
        _onboardingLanguage
    ) { localizations, language ->
        localizations.filter { it.langCode == language }
            .associate { it.key to it.value }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    private val _selectedSkin = MutableStateFlow("gaudi")
    val selectedSkin: StateFlow<String> = _selectedSkin.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _globalCurrency = MutableStateFlow("EUR")
    val globalCurrency: StateFlow<String> = _globalCurrency.asStateFlow()

    fun setSelectedSkin(skin: String) {
        _selectedSkin.value = skin
        saveProfiles()
        syncUserProfileToCloud()
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        saveProfiles()
    }

    fun setGlobalCurrency(currency: String) {
        _globalCurrency.value = currency
        saveProfiles()
        syncUserProfileToCloud()
    }

    private val _onboardingRole = MutableStateFlow<String?>("client")
    val onboardingRole: StateFlow<String?> = _onboardingRole.asStateFlow()

    private val _onboardingAvatarUrl = MutableStateFlow<String?>(null)
    val onboardingAvatarUrl: StateFlow<String?> = _onboardingAvatarUrl.asStateFlow()

    private val _aiAvatarState = MutableStateFlow<AiImageUiState>(AiImageUiState.Idle)
    val aiAvatarState: StateFlow<AiImageUiState> = _aiAvatarState.asStateFlow()

    private val _isGeneratingAvatar = MutableStateFlow(false)
    val isGeneratingAvatar: StateFlow<Boolean> = _isGeneratingAvatar.asStateFlow()

    private val _aiAdviceState = MutableStateFlow<AiAdviceUiState>(AiAdviceUiState.Idle)
    val aiAdviceState: StateFlow<AiAdviceUiState> = _aiAdviceState.asStateFlow()

    private val _subscriptionTier = MutableStateFlow("free")
    val subscriptionTier: StateFlow<String> = _subscriptionTier.asStateFlow()

    private val _isInstagramConnecting = MutableStateFlow(false)
    val isInstagramConnecting: StateFlow<Boolean> = _isInstagramConnecting.asStateFlow()

    private val _instagramError = MutableStateFlow<String?>(null)
    val instagramError: StateFlow<String?> = _instagramError.asStateFlow()

    private val _instagramImportFeed = MutableStateFlow<List<InstagramPost>>(emptyList())
    val instagramImportFeed: StateFlow<List<InstagramPost>> = _instagramImportFeed.asStateFlow()

    private val _selectedImportPosts = MutableStateFlow<Set<Int>>(emptySet())
    val selectedImportPosts: StateFlow<Set<Int>> = _selectedImportPosts.asStateFlow()

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step
    }

    fun setOnboardingLanguage(lang: String) {
        _onboardingLanguage.value = lang
        saveProfiles()
        syncUserProfileToCloud()
    }

    fun setOnboardingRole(role: String) {
        _onboardingRole.value = role
        _userRole.value = role
    }

    fun setOnboardingAvatar(url: String?) {
        _onboardingAvatarUrl.value = url
    }

    fun upgradeSubscription(tier: String) {
        _subscriptionTier.value = tier
    }

    fun generateAiAvatarWithGemini(prompt: String) {
        viewModelScope.launch {
            _isGeneratingAvatar.value = true
            _aiAvatarState.value = AiImageUiState.Loading
            val result = nailAiRepository.generateAvatarOrNailConcept(getApplication(), prompt)
            result.onSuccess { (uriString, bitmap) ->
                _onboardingAvatarUrl.value = uriString
                _aiAvatarState.value = AiImageUiState.Success(uriString, bitmap)
            }.onFailure { err ->
                _aiAvatarState.value = AiImageUiState.Error(
                    err.localizedMessage ?: "Не удалось сгенерировать изображение через Gemini AI."
                )
            }
            _isGeneratingAvatar.value = false
        }
    }

    fun resetAiAvatarState() {
        _aiAvatarState.value = AiImageUiState.Idle
    }

    fun requestAiStylistAdvice(
        occasion: String,
        preferredLength: String,
        preferredShape: String,
        preferredPalette: String
    ) {
        viewModelScope.launch {
            _aiAdviceState.value = AiAdviceUiState.Loading
            val result = nailAiRepository.generateNailStylistAdvice(
                occasion = occasion,
                preferredLength = preferredLength,
                preferredShape = preferredShape,
                preferredPalette = preferredPalette,
                languageCode = _onboardingLanguage.value
            )
            result.onSuccess { advice ->
                _aiAdviceState.value = AiAdviceUiState.Success(advice)
            }.onFailure { err ->
                _aiAdviceState.value = AiAdviceUiState.Error(
                    err.localizedMessage ?: "Ошибка обращения к AI-стилисту NailiX."
                )
            }
        }
    }

    fun requestAiVisionPhotoAdvice(
        bitmap: android.graphics.Bitmap,
        question: String
    ) {
        viewModelScope.launch {
            _aiAdviceState.value = AiAdviceUiState.Loading
            val result = nailAiRepository.analyzeHandOrInspirationPhoto(
                bitmap = bitmap,
                question = question,
                languageCode = _onboardingLanguage.value
            )
            result.onSuccess { advice ->
                _aiAdviceState.value = AiAdviceUiState.Success(advice)
            }.onFailure { err ->
                _aiAdviceState.value = AiAdviceUiState.Error(
                    err.localizedMessage ?: "Ошибка анализа фото через Gemini Vision."
                )
            }
        }
    }

    fun clearInstagramError() {
        _instagramError.value = null
    }

    fun loadTop20InstagramPosts(username: String = "") {
        viewModelScope.launch {
            _isInstagramConnecting.value = true
            _instagramError.value = null
            val target = username.trim().removePrefix("@")
                .ifBlank { _instagramUsername.value.removePrefix("@") }
            if (target.isBlank()) {
                _instagramError.value = "Укажите имя профиля Instagram (без @) или загрузите фото из галереи."
                _instagramImportFeed.value = emptyList()
                _selectedImportPosts.value = emptySet()
                _isInstagramConnecting.value = false
                return@launch
            }
            _instagramUsername.value = target
            val result = InstagramRealScraper.fetchPostsResult(target)
            result.onSuccess { posts ->
                _instagramImportFeed.value = posts
                _selectedImportPosts.value = posts.map { it.id }.toSet()
            }.onFailure { err ->
                _instagramImportFeed.value = emptyList()
                _selectedImportPosts.value = emptySet()
                _instagramError.value = err.localizedMessage ?: "Не удалось загрузить профиль @$target."
            }
            _isInstagramConnecting.value = false
        }
    }

    fun selectAllImportPosts() {
        _selectedImportPosts.value = _instagramImportFeed.value.take(20).map { it.id }.toSet()
    }

    fun deselectAllImportPosts() {
        _selectedImportPosts.value = emptySet()
    }

    fun authorizeAndImportInstagram(username: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isInstagramParsing.value = true
            _instagramError.value = null
            val targetUser = username.trim().removePrefix("@").ifBlank { _instagramUsername.value.removePrefix("@") }
            if (targetUser.isBlank()) {
                _instagramError.value = "Укажите имя профиля Instagram."
                _isInstagramParsing.value = false
                return@launch
            }
            _instagramUsername.value = targetUser
            val result = InstagramRealScraper.fetchPostsResult(targetUser)
            result.onSuccess { top20Posts ->
                val currentList = _parsedInstagramPosts.value.toMutableList()
                val existingIds = currentList.map { it.id }.toSet()
                val uniqueNew = top20Posts.filter { it.id !in existingIds }
                currentList.addAll(0, uniqueNew)
                _parsedInstagramPosts.value = currentList

                val displayIds = _selectedDisplayPostIds.value.toMutableSet()
                displayIds.addAll(top20Posts.map { it.id })
                _selectedDisplayPostIds.value = displayIds.take(20).toSet()
                onComplete()
            }.onFailure { err ->
                _instagramError.value = err.localizedMessage ?: "Не удалось загрузить работы @$targetUser."
            }
            _isInstagramParsing.value = false
        }
    }

    fun importLocalGalleryPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val importedPosts = withContext(Dispatchers.IO) {
                val galleryDir = File(ctx.filesDir, "portfolio_gallery")
                if (!galleryDir.exists()) galleryDir.mkdirs()
                val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                val author = _instagramUsername.value.ifBlank {
                    _registeredMasterProfile.value?.instagramUsername ?: "nailix_gallery"
                }
                uris.take(20).mapIndexedNotNull { idx, uri ->
                    runCatching {
                        val destFile = File(galleryDir, "work_${System.currentTimeMillis()}_$idx.jpg")
                        ctx.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        } ?: return@runCatching null
                        InstagramPost(
                            id = (System.currentTimeMillis().toInt() and 0x0FFFFFFF) + idx,
                            imageUrl = Uri.fromFile(destFile).toString(),
                            caption = "Работа мастера • добавлено из галереи",
                            originalUrl = null,
                            dateStr = todayStr,
                            likesCount = 0,
                            username = author
                        )
                    }.getOrNull()
                }
            }
            if (importedPosts.isNotEmpty()) {
                _instagramError.value = null
                val updated = importedPosts + _parsedInstagramPosts.value
                _parsedInstagramPosts.value = updated
                _selectedDisplayPostIds.value = updated.take(20).map { it.id }.toSet()
            } else {
                _instagramError.value = "Не удалось прочитать выбранные фотографии из галереи."
            }
        }
    }

    fun toggleImportPostSelection(postId: Int) {
        val current = _selectedImportPosts.value
        if (current.contains(postId)) {
            _selectedImportPosts.value = current - postId
        } else {
            if (current.size >= 20) return
            _selectedImportPosts.value = current + postId
        }
    }

    fun completeInstagramImportAndLoad() {
        val selectedIds = _selectedImportPosts.value
        val postsToImport = _instagramImportFeed.value.filter { it.id in selectedIds }

        val limit = when (_subscriptionTier.value) {
            "free" -> 20
            "pro" -> 100
            "promax" -> 1000
            else -> 20
        }

        val currentList = _parsedInstagramPosts.value.toMutableList()
        val existingIds = currentList.map { it.id }.toSet()
        val uniqueNew = postsToImport.filter { it.id !in existingIds }

        currentList.addAll(0, uniqueNew)
        val finalSelection = currentList.take(limit)
        _parsedInstagramPosts.value = finalSelection
        _selectedDisplayPostIds.value = finalSelection.take(20).map { it.id }.toSet()
    }

    fun completeOnboarding() {
        _isOnboarded.value = true
        saveProfiles()
        syncUserProfileToCloud()
    }

    fun completeQuickStart(
        role: String,
        fallbackName: String = "",
        fallbackEmail: String = ""
    ) {
        val cleanRole = if (role == "master") "master" else "client"
        _onboardingRole.value = cleanRole
        _userRole.value = cleanRole

        if (cleanRole == "master") {
            if (_registeredMasterProfile.value == null) {
                val existing = savedMasterProfiles.value.firstOrNull()
                if (existing != null) {
                    _registeredMasterProfile.value = existing
                } else {
                    registerMasterProfile(
                        name = fallbackName.ifBlank { "Мастер NailiX" },
                        address = "Calella",
                        schedule = "10:00 - 19:00",
                        experience = 1,
                        languages = listOf("Русский", "Español"),
                        instagram = ""
                    )
                }
            }
        } else {
            val resolvedName = _clientProfile.value.name.ifBlank { fallbackName }.ifBlank { "Клиент NailiX" }
            val resolvedEmail = _clientProfile.value.email.ifBlank { fallbackEmail }
            val resolvedCode = _clientProfile.value.referralCode.ifBlank {
                if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
                    cloudSyncRepository.generateReferralCodeForUid(currentUserId)
                } else {
                    ""
                }
            }
            val updated = _clientProfile.value.copy(
                name = resolvedName,
                email = resolvedEmail,
                referralCode = resolvedCode
            )
            _clientProfile.value = updated
            clientName.value = resolvedName
        }

        _isOnboarded.value = true
        saveProfiles()
        syncUserProfileToCloud()
    }

    fun resetOnboarding() {
        _isOnboarded.value = false
        val sysLang = Locale.getDefault().language.lowercase()
        val supportedLangs = setOf("en", "ru", "es", "zh", "hi", "ar", "pt", "fr", "he", "tr")
        _onboardingLanguage.value = if (sysLang in supportedLangs) sysLang else "en"
        _onboardingStep.value = 0
        _onboardingAvatarUrl.value = null
        saveProfiles()
    }

    fun addManualPortfolioPost(post: InstagramPost): Boolean {
        val limit = when (_subscriptionTier.value) {
            "free" -> 20
            "pro" -> 100
            "promax" -> 100
            else -> 20
        }
        val currentSize = _parsedInstagramPosts.value.size
        if (currentSize >= limit) return false
        val current = _parsedInstagramPosts.value.toMutableList()
        current.add(0, post)
        _parsedInstagramPosts.value = current
        return true
    }

    fun addNewPortfolioPost(post: InstagramPost) {
        addManualPortfolioPost(post)
    }

    fun deletePortfolioPost(postId: Int) {
        _parsedInstagramPosts.value = _parsedInstagramPosts.value.filter { it.id != postId }
    }

    // Bookmarks for Masters
    private val _bookmarkedMasterIds = MutableStateFlow<Set<Int>>(emptySet())
    val bookmarkedMasterIds: StateFlow<Set<Int>> = _bookmarkedMasterIds.asStateFlow()

    fun toggleBookmarkMaster(masterId: Int) {
        val current = _bookmarkedMasterIds.value
        _bookmarkedMasterIds.value = if (current.contains(masterId)) {
            current - masterId
        } else {
            current + masterId
        }
    }

    private val _mastersList = MutableStateFlow<List<MasterProfile>>(emptyList())
    val mastersList: StateFlow<List<MasterProfile>> = _mastersList.asStateFlow()

    private val _clientProfile = MutableStateFlow(
        ClientProfile(
            name = "",
            phone = "",
            email = "",
            rating = 5.0,
            ratingCount = 0,
            homeAddress = "",
            homeLat = 41.6140,
            homeLon = 2.6600,
            workAddress = "",
            workLat = 41.6160,
            workLon = 2.6630,
            referralCode = ""
        )
    )
    val clientProfile: StateFlow<ClientProfile> = _clientProfile.asStateFlow()

    private val _registeredMasterProfile = MutableStateFlow<MasterProfile?>(null)
    val registeredMasterProfile: StateFlow<MasterProfile?> = _registeredMasterProfile.asStateFlow()

    private val _reviewsList = MutableStateFlow<List<AggregatorReview>>(emptyList())
    val reviewsList: StateFlow<List<AggregatorReview>> = _reviewsList.asStateFlow()

    // Honest studio booking confirmation sheet state (no fake escrow card inputs)
    private val _pendingConfirmationBooking = MutableStateFlow<Booking?>(null)
    val pendingConfirmationBooking: StateFlow<Booking?> = _pendingConfirmationBooking.asStateFlow()

    private val _feeWaiverUnlocked = MutableStateFlow(false)
    val feeWaiverUnlocked: StateFlow<Boolean> = _feeWaiverUnlocked.asStateFlow()

    init {
        val sysLang = Locale.getDefault().language.lowercase()
        val supportedLangs = setOf("en", "ru", "es", "zh", "hi", "ar", "pt", "fr", "he", "tr")
        _onboardingLanguage.value = if (sysLang in supportedLangs) sysLang else "en"

        viewModelScope.launch {
            delay(2500)
            _showSplash.value = false
        }

        loadProfiles()

        viewModelScope.launch {
            repository.ensureLoyaltyInitialized()
            repository.ensureServicesInitialized()
            repository.ensureLocalizationsInitialized()
            repository.ensureMasterNailArtsInitialized()
            generateVotingPair()
        }

        if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
            attachCloudListeners(currentUserId, cloudSyncRepository)
        }
    }

    private val bookingCloudDocIds = mutableMapOf<Int, String>()

    private fun attachCloudListeners(uid: String, cloud: CloudSyncRepository) {
        viewModelScope.launch {
            cloud.observeUserProfile(uid)
                .catch { /* logged by CloudSyncRepository */ }
                .collect { cloudProfile ->
                    if (cloudProfile != null) {
                        val updatedClient = _clientProfile.value.copy(
                            name = _clientProfile.value.name.ifBlank { cloudProfile.displayName },
                            phone = _clientProfile.value.phone.ifBlank { cloudProfile.phone },
                            email = _clientProfile.value.email.ifBlank { cloudProfile.email },
                            referralCode = cloudProfile.referralCode.ifBlank {
                                cloud.generateReferralCodeForUid(uid)
                            }
                        )
                        _clientProfile.value = updatedClient
                        if (clientName.value.isBlank()) clientName.value = updatedClient.name
                        if (clientPhone.value.isBlank()) clientPhone.value = updatedClient.phone
                    }
                }
        }

        viewModelScope.launch {
            cloud.observePublicMasters()
                .catch { /* logged by CloudSyncRepository */ }
                .collect { cloudMasters ->
                    if (cloudMasters.isNotEmpty()) {
                        val localList = savedMasterProfiles.value
                        val merged = localList.toMutableList()
                        cloudMasters.forEachIndexed { index, cm ->
                            val resolvedLat = cm.lat ?: 41.6150
                            val resolvedLon = cm.lon ?: 2.6620
                            val existingIndex = merged.indexOfFirst {
                                it.name.equals(cm.name, ignoreCase = true) &&
                                    it.instagramUsername.equals(cm.instagramUsername, ignoreCase = true)
                            }
                            if (existingIndex == -1) {
                                merged.add(
                                    MasterProfile(
                                        id = 5000 + index,
                                        name = cm.name,
                                        address = cm.address,
                                        lat = resolvedLat,
                                        lon = resolvedLon,
                                        workingSchedule = cm.workingSchedule,
                                        experienceYears = cm.experienceYears.toInt(),
                                        languages = cm.languages,
                                        instagramUsername = cm.instagramUsername,
                                        rating = cm.ratingAvg,
                                        ratingCount = cm.ratingCount.toInt(),
                                        isUserCreated = cm.userId == uid,
                                        currency = cm.currency,
                                        ownerUid = cm.userId
                                    )
                                )
                            } else {
                                val curr = merged[existingIndex]
                                merged[existingIndex] = curr.copy(
                                    ownerUid = curr.ownerUid.ifBlank { cm.userId },
                                    lat = cm.lat ?: curr.lat,
                                    lon = cm.lon ?: curr.lon
                                )
                            }
                        }
                        _mastersList.value = merged
                    }
                }
        }

        viewModelScope.launch {
            cloud.observePublicServices()
                .catch { /* logged by CloudSyncRepository */ }
                .collect { cloudServices ->
                    if (cloudServices.isNotEmpty()) {
                        val existing = repository.serviceDao.getAllServicesDirect()
                        cloudServices.forEach { cs ->
                            val match = existing.any {
                                it.name.equals(cs.name, ignoreCase = true) &&
                                    it.category.equals(cs.category, ignoreCase = true)
                            }
                            if (!match) {
                                repository.serviceDao.insertService(
                                    ServiceEntity(
                                        name = cs.name,
                                        category = cs.category,
                                        price = cs.price,
                                        durationMin = cs.durationMin.toInt(),
                                        description = cs.description,
                                        imageUrl = cs.imageUrl
                                    )
                                )
                            }
                        }
                    }
                }
        }

        viewModelScope.launch {
            cloud.observePublicNailArts()
                .catch { /* logged by CloudSyncRepository */ }
                .collect { cloudArts ->
                    if (cloudArts.isNotEmpty()) {
                        val existing = repository.masterNailArtDao.getAllMasterNailArtsDirect()
                        cloudArts.forEach { ca ->
                            val match = existing.any {
                                it.title.equals(ca.title, ignoreCase = true) &&
                                    it.masterName.equals(ca.masterName, ignoreCase = true)
                            }
                            if (!match) {
                                repository.masterNailArtDao.insertMasterNailArt(
                                    MasterNailArtEntity(
                                        title = ca.title,
                                        masterName = ca.masterName,
                                        complexity = ca.complexity,
                                        extraPrice = ca.extraPrice,
                                        imageUrl = ca.imageUrl,
                                        likes = ca.likes.toInt(),
                                        views = ca.views.toInt(),
                                        description = ca.description
                                    )
                                )
                            }
                        }
                    }
                }
        }

        viewModelScope.launch {
            combine(
                cloud.observeClientBookings(uid).catch { emit(emptyList()) },
                cloud.observeMasterBookings(uid).catch { emit(emptyList()) }
            ) { clientList, masterList ->
                (clientList + masterList).distinctBy { it.id }
            }.collect { cloudBookings ->
                cloudBookings.forEach { cb ->
                    val localMatch = bookings.value.find { local ->
                        val docId = bookingCloudDocIds[local.id]
                        (docId != null && docId == cb.id) ||
                            (local.serviceName.equals(cb.serviceName, ignoreCase = true) &&
                                local.date == cb.date &&
                                local.timeSlot == cb.timeSlot &&
                                local.clientPhone == cb.clientPhone)
                    }
                    if (localMatch == null) {
                        val insertedId = repository.bookingDao.insertBooking(
                            Booking(
                                serviceName = cb.serviceName,
                                serviceCategory = cb.serviceCategory,
                                date = cb.date,
                                timeSlot = cb.timeSlot,
                                masterName = cb.masterName,
                                price = cb.price,
                                clientName = cb.clientName,
                                clientPhone = cb.clientPhone,
                                status = cb.status
                            )
                        ).toInt()
                        if (cb.id.isNotBlank()) {
                            bookingCloudDocIds[insertedId] = cb.id
                        }
                    } else {
                        if (cb.id.isNotBlank()) {
                            bookingCloudDocIds[localMatch.id] = cb.id
                        }
                        if (localMatch.status != cb.status) {
                            repository.bookingDao.updateBookingStatus(localMatch.id, cb.status)
                        }
                        if (localMatch.date != cb.date || localMatch.timeSlot != cb.timeSlot) {
                            repository.bookingDao.updateBookingSchedule(localMatch.id, cb.date, cb.timeSlot)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            cloud.observePublicReviews()
                .catch { /* logged by CloudSyncRepository */ }
                .collect { cloudReviews ->
                    if (cloudReviews.isNotEmpty()) {
                        _reviewsList.value = cloudReviews.mapIndexed { idx, cr ->
                            AggregatorReview(
                                id = idx + 1,
                                targetId = cr.targetId,
                                targetType = cr.targetType,
                                authorName = cr.authorName,
                                rating = cr.rating.toInt(),
                                comment = cr.comment,
                                dateStr = cr.dateStr
                            )
                        }
                    }
                }
        }
    }

    private fun syncUserProfileToCloud() {
        val cloud = cloudSyncRepository ?: return
        if (currentUserId.isBlank()) return
        val client = _clientProfile.value
        val loyalty = loyaltyInfo.value
        viewModelScope.launch {
            try {
                cloud.upsertUserProfile(
                    displayName = client.name.ifBlank { clientName.value }.ifBlank { "NailiX User" },
                    role = _userRole.value,
                    loyaltyPoints = loyalty?.points ?: 150,
                    loyaltyLevel = loyalty?.level ?: "Silver",
                    friendsReferred = loyalty?.friendsReferred ?: 0,
                    homeAddress = client.homeAddress,
                    workAddress = client.workAddress,
                    phone = client.phone.ifBlank { clientPhone.value },
                    email = client.email,
                    preferredSkin = _selectedSkin.value,
                    preferredCurrency = _globalCurrency.value,
                    preferredLanguage = _onboardingLanguage.value,
                    fcmToken = FcmNotificationManager.fcmToken.value.takeIf { it.length > 40 }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Logged inside CloudSyncRepository
            }
        }
    }

    private fun publishMasterToCloud(master: MasterProfile) {
        val cloud = cloudSyncRepository ?: return
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            cloud.publishMasterProfile(
                masterDocId = "master_${currentUserId}_${master.id}",
                name = master.name,
                address = master.address,
                schedule = master.workingSchedule,
                experienceYears = master.experienceYears,
                languages = master.languages,
                instagram = master.instagramUsername,
                currency = master.currency,
                lat = master.lat,
                lon = master.lon
            )
        }
    }

    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toInt()
    }

    fun updateClientLocations(
        homeAddr: String,
        homeLat: Double,
        homeLon: Double,
        workAddr: String,
        workLat: Double,
        workLon: Double
    ) {
        _clientProfile.value = _clientProfile.value.copy(
            homeAddress = homeAddr,
            homeLat = homeLat,
            homeLon = homeLon,
            workAddress = workAddr,
            workLat = workLat,
            workLon = workLon
        )
        saveProfiles()
        syncUserProfileToCloud()
    }

    fun registerMasterProfile(
        name: String,
        address: String,
        schedule: String,
        experience: Int,
        languages: List<String>,
        instagram: String,
        currency: String = "EUR"
    ) {
        val newId = (savedMasterProfiles.value.maxOfOrNull { it.id } ?: 100) + 1
        val cleanInsta = instagram.trim().removePrefix("@")
        val newMaster = MasterProfile(
            id = newId,
            name = name,
            address = address,
            lat = 41.6150,
            lon = 2.6620,
            workingSchedule = schedule.ifEmpty { "Пн-Пт: 09:00 - 18:00" },
            experienceYears = experience,
            languages = languages.ifEmpty { listOf("Испанский") },
            instagramUsername = cleanInsta,
            rating = 5.0,
            ratingCount = 1,
            isUserCreated = true,
            currency = currency,
            ownerUid = currentUserId
        )
        _registeredMasterProfile.value = newMaster
        _userRole.value = "master"
        if (cleanInsta.isNotBlank()) {
            _instagramUsername.value = cleanInsta
        }

        val currentList = savedMasterProfiles.value.toMutableList()
        val index = currentList.indexOfFirst {
            cleanInsta.isNotBlank() && it.instagramUsername.equals(cleanInsta, ignoreCase = true)
        }
        if (index != -1) {
            currentList[index] = newMaster
        } else {
            currentList.add(newMaster)
        }
        savedMasterProfiles.value = currentList
        _mastersList.value = currentList
        if (primaryMasterId.value == null) {
            primaryMasterId.value = newId
        }
        saveProfiles()
        publishMasterToCloud(newMaster)
        syncUserProfileToCloud()
        geocodeMasterAddressAsync(newMaster.id, address)
    }

    private fun geocodeMasterAddressAsync(masterId: Int, address: String) {
        if (address.isBlank()) return
        viewModelScope.launch {
            com.example.util.LocationGeocoderHelper.geocodeAddress(getApplication(), address)
                .onSuccess { (resolvedLat, resolvedLon) ->
                    val list = savedMasterProfiles.value.toMutableList()
                    val idx = list.indexOfFirst { it.id == masterId }
                    if (idx != -1) {
                        val updated = list[idx].copy(lat = resolvedLat, lon = resolvedLon)
                        list[idx] = updated
                        savedMasterProfiles.value = list
                        _mastersList.value = list
                        if (_registeredMasterProfile.value?.id == masterId) {
                            _registeredMasterProfile.value = updated
                        }
                        saveProfiles()
                        publishMasterToCloud(updated)
                    }
                }
        }
    }

    fun updateMasterSchedule(schedule: String) {
        val current = _registeredMasterProfile.value ?: return
        val updated = current.copy(workingSchedule = schedule)
        _registeredMasterProfile.value = updated
        val currentList = savedMasterProfiles.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == current.id }
        if (index != -1) {
            currentList[index] = updated
            savedMasterProfiles.value = currentList
            _mastersList.value = currentList
            saveProfiles()
            publishMasterToCloud(updated)
        }
    }

    fun updateMasterProfileDetails(
        name: String,
        address: String,
        schedule: String,
        languages: List<String>,
        instagram: String,
        currency: String
    ) {
        val current = _registeredMasterProfile.value ?: return
        val updated = current.copy(
            name = name.ifBlank { current.name },
            address = address.ifBlank { current.address },
            workingSchedule = schedule.ifBlank { current.workingSchedule },
            languages = languages.ifEmpty { current.languages },
            instagramUsername = instagram.trim().removePrefix("@").ifBlank { current.instagramUsername },
            currency = currency,
            ownerUid = current.ownerUid.ifBlank { currentUserId }
        )
        _registeredMasterProfile.value = updated
        _instagramUsername.value = updated.instagramUsername
        val currentList = savedMasterProfiles.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == current.id }
        if (index != -1) {
            currentList[index] = updated
            savedMasterProfiles.value = currentList
            _mastersList.value = currentList
            saveProfiles()
            publishMasterToCloud(updated)
            geocodeMasterAddressAsync(updated.id, updated.address)
        }
    }

    fun updateBookingStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(id, status)
        }
    }

    // ----------------------------------------------------
    // SOVEREIGN ONION NODE & LOCAL SNAPSHOTS
    // ----------------------------------------------------
    val sovereignNodeStatus = SovereignNodeManager.nodeStatus
    val simplexQueue = SovereignNodeManager.simplexQueue
    val simplexMessages = SovereignNodeManager.simplexMessages
    val p2pSyncState = SovereignNodeManager.p2pSyncState
    val roiCalc = SovereignNodeManager.roiCalc

    fun pingSovereignNode() {
        viewModelScope.launch {
            SovereignNodeManager.pingNode()
        }
    }

    fun toggleTorRouting(enabled: Boolean) {
        SovereignNodeManager.toggleTorRouting(enabled)
    }

    fun updateNodeAddress(onion: String, local: String) {
        SovereignNodeManager.updateNodeAddress(onion, local)
    }

    fun computeRoomStateSnapshot() {
        viewModelScope.launch {
            SovereignNodeManager.computeRoomStateSnapshot(
                bookings = bookings.value,
                services = services.value,
                nailArts = masterNailArts.value
            )
        }
    }

    fun updateRoiParams(masters: Int, fee: Double, aiGens: Int) {
        SovereignNodeManager.updateRoiParams(masters, fee, aiGens)
    }

    fun exportSnapshotJson(): String {
        return SovereignNodeManager.exportSnapshotJson(
            bookings = bookings.value,
            services = services.value,
            nailArts = masterNailArts.value
        )
    }

    // ----------------------------------------------------
    // FIREBASE CLOUD MESSAGING & WORKMANAGER NOTIFICATIONS
    // ----------------------------------------------------
    val fcmToken = FcmNotificationManager.fcmToken
    val fcmError = FcmNotificationManager.fcmError
    val fcmNotifications = FcmNotificationManager.notifications

    fun sendFcmAppointmentReminder(context: Context) {
        val master = _registeredMasterProfile.value?.name ?: "Мастер NailiX"
        FcmNotificationManager.showSystemNotification(
            context = context,
            title = "Напоминание о записи 💅",
            body = "Ждем вас на сеанс в студии NailiX. Мастер: $master.",
            type = "appointment_reminder",
            data = mapOf("master" to master)
        )
    }

    fun sendFcmPromotion(context: Context, title: String, text: String, promoCode: String) {
        val finalTitle = title.ifBlank { "Акция от NailiX 🎁" }
        val finalText = text.ifBlank { "Спецпредложение по промокоду $promoCode!" }
        FcmNotificationManager.showSystemNotification(
            context = context,
            title = finalTitle,
            body = finalText,
            type = "promotion",
            data = mapOf("promoCode" to promoCode, "type" to "promotion")
        )
    }

    fun registerClientProfile(
        name: String,
        phone: String,
        homeAddr: String,
        workAddr: String
    ) {
        val resolvedCode = _clientProfile.value.referralCode.ifBlank {
            if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
                cloudSyncRepository.generateReferralCodeForUid(currentUserId)
            } else {
                ""
            }
        }
        val newProfile = _clientProfile.value.copy(
            name = name,
            phone = phone,
            homeAddress = homeAddr,
            workAddress = workAddr,
            homeLat = 41.6140,
            homeLon = 2.6600,
            workLat = 41.6160,
            workLon = 2.6630,
            referralCode = resolvedCode
        )
        _clientProfile.value = newProfile
        clientName.value = name
        clientPhone.value = phone

        val currentList = savedClientProfiles.value.toMutableList()
        val index = currentList.indexOfFirst { it.phone == phone }
        if (index != -1) {
            currentList[index] = newProfile
        } else {
            currentList.add(newProfile)
        }
        savedClientProfiles.value = currentList
        if (primaryClientPhone.value.isEmpty()) {
            primaryClientPhone.value = phone
        }
        saveProfiles()
        syncUserProfileToCloud()
    }

    fun leaveFeedback(
        targetId: String,
        targetType: String,
        rating: Int,
        comment: String,
        author: String
    ) {
        val nextId = _reviewsList.value.size + 1
        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val finalAuthor = author.ifEmpty { "Гость NailiX" }
        val newReview = AggregatorReview(
            id = nextId,
            targetId = targetId,
            targetType = targetType,
            authorName = finalAuthor,
            rating = rating,
            comment = comment,
            dateStr = dateStr
        )
        _reviewsList.value = _reviewsList.value + newReview

        if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
            viewModelScope.launch {
                cloudSyncRepository.createReview(
                    targetId = targetId,
                    targetType = targetType,
                    authorName = finalAuthor,
                    rating = rating,
                    comment = comment,
                    dateStr = dateStr
                )
            }
        }

        if (targetType == "master") {
            val masterId = targetId.toIntOrNull() ?: return
            _mastersList.value = _mastersList.value.map { master ->
                if (master.id == masterId) {
                    val totalRatingSum = (master.rating * master.ratingCount) + rating
                    val newCount = master.ratingCount + 1
                    master.copy(
                        rating = (totalRatingSum / newCount).let { String.format(Locale.US, "%.1f", it).toDouble() },
                        ratingCount = newCount
                    )
                } else master
            }
            val currentRegistered = _registeredMasterProfile.value
            if (currentRegistered != null && currentRegistered.id == masterId) {
                val totalRatingSum = (currentRegistered.rating * currentRegistered.ratingCount) + rating
                val newCount = currentRegistered.ratingCount + 1
                _registeredMasterProfile.value = currentRegistered.copy(
                    rating = (totalRatingSum / newCount).let { String.format(Locale.US, "%.1f", it).toDouble() },
                    ratingCount = newCount
                )
            }
        } else if (targetType == "client") {
            val client = _clientProfile.value
            if (client.phone == targetId) {
                val totalRatingSum = (client.rating * client.ratingCount) + rating
                val newCount = client.ratingCount + 1
                _clientProfile.value = client.copy(
                    rating = (totalRatingSum / newCount).let { String.format(Locale.US, "%.1f", it).toDouble() },
                    ratingCount = newCount
                )
            }
        }
    }

    fun exchangePointsForFeeWaiver(): Boolean {
        val loyalty = loyaltyInfo.value ?: return false
        val rating = _clientProfile.value.rating

        if (loyalty.points < 400) {
            viewModelScope.launch {
                _referralMessage.emit("Недостаточно баллов! Требуется 400 баллов (у вас ${loyalty.points}).")
            }
            return false
        }
        if (rating < 4.8) {
            viewModelScope.launch {
                _referralMessage.emit("Требуется рейтинг 4.8+ (у вас $rating).")
            }
            return false
        }

        viewModelScope.launch {
            repository.spendBonusPoints(400, "Активация льготного статуса 0% комиссии")
            _feeWaiverUnlocked.value = true
            syncUserProfileToCloud()
            _referralMessage.emit("Поздравляем! 🎉 Льготный статус 0% комиссии успешно активирован!")
        }
        return true
    }

    fun requestBookingConfirmation() {
        val service = _selectedService.value ?: return
        val master = _selectedMaster.value ?: return
        val date = _selectedDate.value ?: return
        val slot = _selectedTimeSlot.value ?: return
        val name = clientName.value.trim().ifEmpty { _clientProfile.value.name }
        val phone = clientPhone.value.trim().ifEmpty { _clientProfile.value.phone }

        _pendingConfirmationBooking.value = Booking(
            serviceName = service.name,
            serviceCategory = service.category,
            date = date,
            timeSlot = slot,
            masterName = master,
            price = service.price,
            clientName = name,
            clientPhone = phone
        )
    }

    fun dismissBookingConfirmation() {
        _pendingConfirmationBooking.value = null
    }

    fun confirmStudioBooking(paymentMethod: String = "studio_visit") {
        val booking = _pendingConfirmationBooking.value ?: return
        viewModelScope.launch {
            _bookingLoading.value = true
            val newId = repository.insertBooking(booking)
            val savedBooking = booking.copy(id = newId.toInt())

            val paymentLabel = if (paymentMethod == "studio_transfer") {
                "Перевод по реквизитам студии"
            } else {
                "Оплата при визите в студии"
            }

            repository.addReminder(
                Reminder(
                    bookingId = savedBooking.id,
                    title = "Запись подтверждена ($paymentLabel) ✅",
                    message = "Услуга '${savedBooking.serviceName}' у мастера ${savedBooking.masterName} (${savedBooking.date}, ${savedBooking.timeSlot}).",
                    triggerTimeBefore = "Сразу"
                )
            )
            repository.addReminder(
                Reminder(
                    bookingId = savedBooking.id,
                    title = "Запись на завтра 💅",
                    message = "Мастер ${savedBooking.masterName} ждет вас в ${savedBooking.timeSlot} на '${savedBooking.serviceName}'!",
                    triggerTimeBefore = "За 24 часа"
                )
            )

            // Real WorkManager reminder scheduling (24 hours / 1440 min ahead; or shortly for immediate testing if same day)
            FcmNotificationManager.scheduleBookingReminder(
                context = getApplication(),
                title = "Скоро визит в NailiX 💅",
                body = "Напоминаем: ${savedBooking.serviceName} у мастера ${savedBooking.masterName} (${savedBooking.date}, ${savedBooking.timeSlot}).",
                delayMinutes = 1440L
            )

            SovereignNodeManager.recordLocalBookingEvent(
                bookingId = savedBooking.id,
                serviceName = savedBooking.serviceName,
                clientName = savedBooking.clientName,
                date = savedBooking.date,
                time = savedBooking.timeSlot
            )

            if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
                val ticketToken = VisitTicketCryptoManager.generateSecurityToken(savedBooking)
                val targetMasterUid = _mastersList.value
                    .find { it.name.equals(savedBooking.masterName, ignoreCase = true) }
                    ?.ownerUid
                    ?.takeIf { it.isNotBlank() }
                val cloudDocId = "bk_${currentUserId}_${savedBooking.id}"
                bookingCloudDocIds[savedBooking.id] = cloudDocId
                cloudSyncRepository.createBooking(
                    bookingDocId = cloudDocId,
                    booking = savedBooking,
                    ticketToken = ticketToken,
                    paymentMethod = paymentMethod,
                    masterUid = targetMasterUid
                )
                syncUserProfileToCloud()
            }

            _selectedService.value = null
            _selectedMaster.value = null
            _selectedDate.value = null
            _selectedTimeSlot.value = null
            _pendingConfirmationBooking.value = null
            _bookingSuccessDialog.value = savedBooking
            _bookingLoading.value = false
        }
    }

    // Active screen navigation
    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    fun navigateTo(tab: String) {
        _currentTab.value = tab
    }

    val isAdminMode = MutableStateFlow(false)
    val isSyncing = MutableStateFlow(false)
    val ownerSyncStatus = MutableStateFlow("Синхронизировано")

    val bookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val loyaltyInfo: StateFlow<UserLoyalty?> = repository.loyaltyState
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDesignIds: StateFlow<List<Int>> = repository.favoriteDesigns
        .map { list -> list.map { it.designId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ServiceItem>> = repository.dynamicServices
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val referredFriends: StateFlow<List<ReferredFriend>> = repository.allReferredFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loyaltyTransactions: StateFlow<List<LoyaltyTransaction>> = repository.allLoyaltyTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterNailArts: StateFlow<List<MasterNailArtEntity>> = repository.allMasterNailArts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedNailArtForBooking = MutableStateFlow<MasterNailArtEntity?>(null)

    fun likeMasterNailArt(id: Int) {
        viewModelScope.launch {
            repository.masterNailArtDao.incrementLikes(id)
        }
    }

    fun viewMasterNailArt(id: Int) {
        viewModelScope.launch {
            repository.masterNailArtDao.incrementViews(id)
        }
    }

    fun publishMasterNailArt(
        title: String,
        masterName: String,
        complexity: String,
        extraPrice: Double,
        imageUrl: String,
        description: String
    ) {
        viewModelScope.launch {
            val entity = MasterNailArtEntity(
                title = title,
                masterName = masterName,
                complexity = complexity,
                extraPrice = extraPrice,
                imageUrl = imageUrl,
                description = description
            )
            val id = repository.masterNailArtDao.insertMasterNailArt(entity)
            if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
                cloudSyncRepository.publishNailArt(
                    artDocId = "art_${currentUserId}_$id",
                    title = title,
                    masterName = masterName,
                    complexity = complexity,
                    extraPrice = extraPrice,
                    imageUrl = imageUrl,
                    description = description
                )
            }
        }
    }

    val clientProfiles: StateFlow<List<ClientProfile>> = combine(
        bookings, loyaltyInfo
    ) { bookingList, loyalty ->
        val map = mutableMapOf<String, ClientProfile>()

        bookingList.forEach { booking ->
            val existing = map[booking.clientPhone]
            if (existing == null) {
                map[booking.clientPhone] = ClientProfile(
                    name = booking.clientName,
                    phone = booking.clientPhone,
                    totalBookings = 1,
                    totalSpent = booking.price
                )
            } else {
                map[booking.clientPhone] = existing.copy(
                    totalBookings = existing.totalBookings + 1,
                    totalSpent = existing.totalSpent + booking.price
                )
            }
        }

        if (loyalty != null && loyalty.isMailingSubscribed && loyalty.subscribedPhone != null) {
            val phone = loyalty.subscribedPhone
            val existing = map[phone]
            if (existing == null) {
                map[phone] = ClientProfile(
                    name = "Клиент (Подписчик)",
                    phone = phone,
                    email = loyalty.subscribedEmail ?: "",
                    isSubscribed = true
                )
            } else {
                map[phone] = existing.copy(
                    email = loyalty.subscribedEmail ?: "",
                    isSubscribed = true
                )
            }
        }

        map.values.toList().sortedByDescending { it.totalSpent }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statistics: StateFlow<AdminStats> = bookings.map { list ->
        val confirmed = list.filter { it.status != "Отменена" }
        val revenue = confirmed.sumOf { it.price }
        val activeCount = list.filter { it.status == "Подтверждена" }.size
        val completedCount = list.filter { it.status == "Выполнена" }.size

        val categoryPopularity = confirmed.groupBy { it.serviceCategory }.mapValues { it.value.size }
        val popularCategory = categoryPopularity.maxByOrNull { it.value }?.key ?: "Нет"

        val masterPopularity = confirmed.groupBy { it.masterName }.mapValues { it.value.size }
        val topMaster = masterPopularity.maxByOrNull { it.value }?.key ?: "Нет"

        AdminStats(
            totalRevenue = revenue,
            activeBookings = activeCount,
            completedBookings = completedCount,
            popularCategory = popularCategory,
            topMaster = topMaster
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminStats())

    // ----------------------------------------------------
    // ONLINE BOOKING FLOW STATES
    // ----------------------------------------------------
    private val _selectedService = MutableStateFlow<ServiceItem?>(null)
    val selectedService: StateFlow<ServiceItem?> = _selectedService.asStateFlow()

    private val _selectedMaster = MutableStateFlow<String?>(null)
    val selectedMaster: StateFlow<String?> = _selectedMaster.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    private val _selectedTimeSlot = MutableStateFlow<String?>(null)
    val selectedTimeSlot: StateFlow<String?> = _selectedTimeSlot.asStateFlow()

    private val _bookingLoading = MutableStateFlow(false)
    val bookingLoading: StateFlow<Boolean> = _bookingLoading.asStateFlow()

    private val _bookingSuccessDialog = MutableStateFlow<Booking?>(null)
    val bookingSuccessDialog: StateFlow<Booking?> = _bookingSuccessDialog.asStateFlow()

    val masters = listOf("Виктория (Топ-Мастер)", "Карина (Стилист)", "Анна (Про-Мастер)", "София (Младший Мастер)")
    val dates = generateNext7Days()
    val timeSlots = listOf("09:00", "11:00", "13:00", "15:00", "17:00", "19:00")

    fun selectService(service: ServiceItem?) { _selectedService.value = service }
    fun selectMaster(master: String?) { _selectedMaster.value = master }
    fun selectDate(date: String?) { _selectedDate.value = date }
    fun selectTimeSlot(slot: String?) { _selectedTimeSlot.value = slot }
    fun dismissSuccessDialog() { _bookingSuccessDialog.value = null }

    fun cancelBooking(booking: Booking) {
        viewModelScope.launch {
            repository.deleteBooking(booking.id, booking.price)
            if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
                val docId = bookingCloudDocIds[booking.id] ?: "bk_${currentUserId}_${booking.id}"
                cloudSyncRepository.updateBookingStatus(docId, "Отменена")
            }
        }
    }

    fun completeBooking(booking: Booking) {
        viewModelScope.launch {
            repository.completeBooking(booking.id)
            if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
                val docId = bookingCloudDocIds[booking.id] ?: "bk_${currentUserId}_${booking.id}"
                cloudSyncRepository.updateBookingStatus(docId, "Выполнена")
            }
        }
    }

    fun rescheduleBooking(booking: Booking, newDate: String, newTimeSlot: String) {
        viewModelScope.launch {
            repository.rescheduleBooking(booking.id, newDate, newTimeSlot)
            repository.addReminder(
                Reminder(
                    bookingId = booking.id,
                    title = "Запись перенесена 📅",
                    message = "${booking.serviceName} у мастера ${booking.masterName}: новое время $newDate в $newTimeSlot.",
                    triggerTimeBefore = "Обновлено"
                )
            )
            FcmNotificationManager.scheduleBookingReminder(
                context = getApplication(),
                title = "Обновлённый визит в NailiX 💅",
                body = "${booking.serviceName} у ${booking.masterName} перенесён на $newDate в $newTimeSlot.",
                delayMinutes = 1440L
            )
            if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
                val docId = bookingCloudDocIds[booking.id] ?: "bk_${currentUserId}_${booking.id}"
                cloudSyncRepository.updateBookingSchedule(docId, newDate, newTimeSlot)
            }
        }
    }

    fun repeatBooking(booking: Booking) {
        val matchingService = services.value.find {
            it.name.equals(booking.serviceName, ignoreCase = true)
        } ?: ServiceItem(
            id = 9000 + booking.id,
            name = booking.serviceName,
            category = booking.serviceCategory,
            price = booking.price,
            durationMin = 75,
            description = "Повторная запись"
        )
        _selectedService.value = matchingService
        _selectedMaster.value = booking.masterName
        _selectedDate.value = null
        _selectedTimeSlot.value = null
        navigateTo("booking")
    }

    data class QuickSlotSuggestion(
        val master: MasterProfile,
        val service: ServiceItem,
        val date: String,
        val timeSlot: String
    )

    fun findNextAvailableQuickSlot(): QuickSlotSuggestion? {
        val activeMasters = _mastersList.value
        val activeServices = services.value
        val preferredService = activeServices.firstOrNull() ?: return null
        val preferredMaster = activeMasters.firstOrNull { bookmarkedMasterIds.value.contains(it.id) }
            ?: activeMasters.firstOrNull()
            ?: return null

        val candidateBookings = bookings.value.filter { it.status == "Подтверждена" }
        for (date in dates.take(3)) {
            for (slot in timeSlots) {
                val occupied = candidateBookings.any { b ->
                    b.masterName.equals(preferredMaster.name, ignoreCase = true) &&
                        b.date == date &&
                        com.example.util.SmartSchedulerEngine.isSlotOverlappingWithExistingBooking(
                            targetSlot = slot,
                            targetDurationMinutes = preferredService.durationMin,
                            bookedSlot = b.timeSlot,
                            bookedDurationMinutes = 90
                        )
                }
                if (!occupied) {
                    return QuickSlotSuggestion(
                        master = preferredMaster,
                        service = preferredService,
                        date = date,
                        timeSlot = slot
                    )
                }
            }
        }
        return null
    }

    fun quickBookNextAvailableSlot(suggestion: QuickSlotSuggestion) {
        _selectedService.value = suggestion.service
        _selectedMaster.value = suggestion.master.name
        _selectedDate.value = suggestion.date
        _selectedTimeSlot.value = suggestion.timeSlot
        requestBookingConfirmation()
    }

    // ----------------------------------------------------
    // BASE DESIGNS GALLERY + FILTERS
    // ----------------------------------------------------
    val lengthFilter = MutableStateFlow<String?>(null)
    val shapeFilter = MutableStateFlow<String?>(null)
    val paletteFilter = MutableStateFlow<String?>(null)

    val baseDesigns = repository.baseDesigns

    val filteredDesigns = combine(
        lengthFilter, shapeFilter, paletteFilter
    ) { len, shp, pal ->
        baseDesigns.filter { design ->
            (len == null || design.length == len) &&
                (shp == null || design.shape == shp) &&
                (pal == null || design.palette == pal)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), baseDesigns)

    fun toggleFavoriteDesign(designId: Int) {
        viewModelScope.launch {
            val favs = favoriteDesignIds.value
            if (favs.contains(designId)) {
                repository.removeFavoriteDesign(designId)
            } else {
                repository.addFavoriteDesign(designId)
            }
        }
    }

    // ----------------------------------------------------
    // PORTFOLIO + INSTAGRAM & LOCAL GALLERY
    // ----------------------------------------------------
    private val _showOriginalInstagramLinks = MutableStateFlow(true)
    val showOriginalInstagramLinks: StateFlow<Boolean> = _showOriginalInstagramLinks.asStateFlow()

    private val _isInstagramParsing = MutableStateFlow(false)
    val isInstagramParsing: StateFlow<Boolean> = _isInstagramParsing.asStateFlow()

    private val _parsedInstagramPosts = MutableStateFlow<List<InstagramPost>>(emptyList())
    val parsedInstagramPosts: StateFlow<List<InstagramPost>> = _parsedInstagramPosts.asStateFlow()

    private val _instagramUsername = MutableStateFlow("")
    val instagramUsername: StateFlow<String> = _instagramUsername.asStateFlow()

    private val _selectedDisplayPostIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedDisplayPostIds: StateFlow<Set<Int>> = _selectedDisplayPostIds.asStateFlow()

    fun toggleDisplayPostSelection(postId: Int, context: Context? = null): Boolean {
        val current = _selectedDisplayPostIds.value
        if (current.contains(postId)) {
            _selectedDisplayPostIds.value = current - postId
            return true
        } else {
            if (current.size >= 20) {
                if (context != null) {
                    Toast.makeText(context, "Можно выбрать не более 20 публикаций для отображения!", Toast.LENGTH_SHORT).show()
                }
                return false
            }
            _selectedDisplayPostIds.value = current + postId
            return true
        }
    }

    fun updateInstagramUsername(username: String) {
        _instagramUsername.value = username
    }

    fun toggleInstagramLinks(show: Boolean) {
        _showOriginalInstagramLinks.value = show
    }

    fun triggerInstagramParsing() {
        authorizeAndImportInstagram(_instagramUsername.value)
    }

    // ----------------------------------------------------
    // NEWSLETTER MAILING LIST
    // ----------------------------------------------------
    val mailingEmail = MutableStateFlow("")
    val mailingPhone = MutableStateFlow("")
    private val _isSubscribedSuccessfully = MutableSharedFlow<String>()
    val isSubscribedSuccessfully: SharedFlow<String> = _isSubscribedSuccessfully.asSharedFlow()

    fun subscribeToNewsletter() {
        val email = mailingEmail.value.trim()
        val phone = mailingPhone.value.trim()
        if (email.isEmpty() || phone.isEmpty()) return

        viewModelScope.launch {
            repository.subscribeMailingList(email, phone)
            syncUserProfileToCloud()
            if (currentUserId.isNotBlank()) {
                cloudSyncRepository?.recordLoyaltyTransaction("Бонус за подписку на рассылку 📩", 100)
            }
            _isSubscribedSuccessfully.emit("Успешная подписка! На ваш баланс зачислено +100 приветственных бонусов!")
            mailingEmail.value = ""
            mailingPhone.value = ""
        }
    }

    // ----------------------------------------------------
    // HONEST CLOUD REFERRAL PROGRAM
    // ----------------------------------------------------
    val referralInputCode = MutableStateFlow("")
    private val _referralMessage = MutableSharedFlow<String>()
    val referralMessage: SharedFlow<String> = _referralMessage.asSharedFlow()

    fun claimReferralCode() {
        val code = referralInputCode.value.trim().uppercase()
        if (code.isEmpty()) return

        viewModelScope.launch {
            val ownCode = _clientProfile.value.referralCode
            if (ownCode.isNotBlank() && code == ownCode) {
                _referralMessage.emit("Нельзя активировать собственный реферальный код.")
                return@launch
            }
            val cloud = cloudSyncRepository
            if (cloud == null || currentUserId.isBlank()) {
                _referralMessage.emit("Требуется авторизация через Google для проверки реферального кода.")
                return@launch
            }
            val claimerName = _clientProfile.value.name.ifBlank { clientName.value }.ifBlank { "Клиент NailiX" }
            val result = cloud.redeemReferralCode(code, claimerName)
            result.onSuccess { inviterName ->
                repository.claimReferral(code, inviterName)
                syncUserProfileToCloud()
                cloud.recordLoyaltyTransaction("Активация кода $code ($inviterName) 🎁", 200)
                _referralMessage.emit("Код $code активирован! Пригласивший: $inviterName. Вам начислено +200 бонусных баллов!")
                referralInputCode.value = ""
            }.onFailure { err ->
                _referralMessage.emit(err.localizedMessage ?: "Не удалось активировать код $code.")
            }
        }
    }

    fun spendBonusPoints(amount: Int, reason: String) {
        viewModelScope.launch {
            repository.spendBonusPoints(amount, reason)
            syncUserProfileToCloud()
        }
    }

    // ----------------------------------------------------
    // REAL LOCAL + FCM NOTIFICATIONS
    // ----------------------------------------------------
    private val _inAppNotification = MutableStateFlow<String?>(null)
    val inAppNotification: StateFlow<String?> = _inAppNotification.asStateFlow()

    fun dismissInAppNotification() {
        _inAppNotification.value = null
    }

    fun triggerLocalReminderNotification(context: Context, reminder: Reminder) {
        viewModelScope.launch {
            repository.markReminderSent(reminder.id)
            FcmNotificationManager.showSystemNotification(
                context = context,
                title = reminder.title,
                body = reminder.message,
                type = "appointment_reminder"
            )
            _inAppNotification.value = "${reminder.title}\n${reminder.message}"
        }
    }

    private fun generateNext7Days(): List<String> {
        val sdf = SimpleDateFormat("dd.MM (EEEE)", Locale.forLanguageTag("ru"))
        val list = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        for (i in 0..6) {
            list.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    // ----------------------------------------------------
    // SALON OWNER ADMIN METHODS
    // ----------------------------------------------------
    fun addNewService(
        name: String,
        category: String,
        price: Double,
        durationMin: Int,
        description: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            isSyncing.value = true
            ownerSyncStatus.value = "Синхронизация..."
            val serviceEntity = ServiceEntity(
                name = name,
                category = category,
                price = price,
                durationMin = durationMin,
                description = description,
                imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60" }
            )
            val newId = repository.insertService(serviceEntity)
            if (currentUserId.isNotBlank() && cloudSyncRepository != null) {
                cloudSyncRepository.publishService(
                    serviceDocId = "svc_${currentUserId}_$newId",
                    name = serviceEntity.name,
                    category = serviceEntity.category,
                    price = serviceEntity.price,
                    durationMin = serviceEntity.durationMin,
                    description = serviceEntity.description,
                    imageUrl = serviceEntity.imageUrl
                )
            }
            isSyncing.value = false
            ownerSyncStatus.value = "Обновлено в каталоге: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
        }
    }

    fun updateServicePrice(id: Int, price: Double) {
        viewModelScope.launch {
            isSyncing.value = true
            repository.updateServicePrice(id, price)
            isSyncing.value = false
            ownerSyncStatus.value = "Прайс обновлен: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
        }
    }

    fun deleteService(id: Int) {
        viewModelScope.launch {
            isSyncing.value = true
            repository.deleteService(id)
            isSyncing.value = false
            ownerSyncStatus.value = "Услуга удалена: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
        }
    }

    fun triggerAutomaticPushSync() {
        viewModelScope.launch {
            isSyncing.value = true
            ownerSyncStatus.value = "Синхронизация с Cloud Firestore и расчёт SHA-256 снимка..."
            computeRoomStateSnapshot()
            syncUserProfileToCloud()
            isSyncing.value = false
            ownerSyncStatus.value = "Синхронизировано: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
        }
    }

    private val exchangeRates = mapOf(
        "EUR" to 1.0,
        "USD" to 1.1,
        "RUB" to 100.0,
        "GBP" to 0.85,
        "CHF" to 0.95,
        "ILS" to 4.0,
        "AED" to 4.0
    )

    fun formatPrice(price: Double, originalCurrency: String): String {
        val targetCurrency = _globalCurrency.value.uppercase()
        val rateToEur = exchangeRates[originalCurrency.uppercase()] ?: 1.0
        val priceInEur = price / rateToEur
        val rateFromEur = exchangeRates[targetCurrency] ?: 1.0
        val convertedPrice = priceInEur * rateFromEur

        val symbol = when (targetCurrency) {
            "USD" -> "$"
            "RUB" -> "₽"
            "GBP" -> "£"
            "CHF" -> "₣"
            "ILS" -> "₪"
            "AED" -> "د.إ"
            "EUR" -> "€"
            else -> "€"
        }
        return when (targetCurrency) {
            "USD", "GBP", "EUR", "CHF" -> "$symbol${convertedPrice.toInt()}"
            else -> "${convertedPrice.toInt()} $symbol"
        }
    }

    fun formatCurrencyName(code: String): String {
        return when (code.uppercase()) {
            "USD" -> "$ (USD)"
            "RUB" -> "₽ (RUB)"
            "GBP" -> "£ (GBP)"
            "CHF" -> "₣ (CHF)"
            "ILS" -> "₪ (ILS)"
            "AED" -> "د.إ (AED)"
            "EUR" -> "€ (EUR)"
            else -> "€ (EUR)"
        }
    }

    // ----------------------------------------------------
    // IN-APP SALON INTERNET RADIO STREAMER (Delegated)
    // ----------------------------------------------------
    val topRadioStations: List<RadioStation> get() = radioController.topRadioStations
    val radioIsPlaying: StateFlow<Boolean> get() = radioController.radioIsPlaying
    val radioIsLoading: StateFlow<Boolean> get() = radioController.radioIsLoading
    val selectedStation: StateFlow<RadioStation?> get() = radioController.selectedStation
    val radioError: StateFlow<String?> get() = radioController.radioError

    fun playRadioStation(station: RadioStation) = radioController.playRadioStation(station)
    fun toggleRadioPlayback() = radioController.toggleRadioPlayback()
    fun pauseRadio() = radioController.pauseRadio()
    fun stopRadio() = radioController.stopRadio()
    fun playRandomStation() = radioController.playRandomStation()

    // ----------------------------------------------------
    // IN-APP NAIL ART CHALLENGES & BATTLES
    // ----------------------------------------------------
    private val _hasVipChallengerPass = MutableStateFlow(prefs.getBoolean("has_vip_challenger_pass", false))
    val hasVipChallengerPass: StateFlow<Boolean> = _hasVipChallengerPass.asStateFlow()

    fun toggleVipChallengerPass() {
        val next = !_hasVipChallengerPass.value
        _hasVipChallengerPass.value = next
        prefs.edit().putBoolean("has_vip_challenger_pass", next).apply()
    }

    val activeContests = MutableStateFlow(
        listOf(
            NailContest(
                id = "wedding",
                title = "Свадебная нежность 👰",
                desc = "Лучший нежный френч, нюд, жемчужные втирки и минимализм. Покажите романтику ногтевой эстетики!",
                prize = "СПА-набор от NailiX + 1000 бонусных баллов!",
                endDate = "31.07.2026",
                icon = "🏆"
            ),
            NailContest(
                id = "neon",
                title = "Летний неон ☀️",
                desc = "Сочные неоновые оттенки, яркие градиенты, фрукты, геометрия и максимальный заряд энергии летнего солнца!",
                prize = "Бесплатный Маникюр с покрытием + 500 бонусов!",
                endDate = "15.08.2026",
                icon = "🔥"
            ),
            NailContest(
                id = "gothic",
                title = "Готический шик 🖤",
                desc = "Угольно-черный, темный винный, матовые велюровые топы, стразы, загадочная поталь и геометрия ночи.",
                prize = "Купон на скидку 1500р + Масло для кутикулы!",
                endDate = "25.08.2026",
                icon = "🌟"
            )
        )
    )

    val selectedContestId = MutableStateFlow("wedding")

    private val _contestEntries = MutableStateFlow(
        listOf(
            ContestEntry("w1", "wedding", "Жемчужная пыльца", "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60", "@nastya_nails", 342),
            ContestEntry("w2", "wedding", "Классический френч", "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60", "@elena_salon", 289),
            ContestEntry("w3", "wedding", "Розовый минимализм", "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60", "@masha_beauty", 152),
            ContestEntry("w4", "wedding", "Золотая поталь на молоке", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60", "@kate_perfect", 405),
            ContestEntry("n1", "neon", "Кислотный лайм", "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60", "@bright_girl", 512),
            ContestEntry("n2", "neon", "Коралловая искра", "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60", "@elena_salon", 418),
            ContestEntry("n3", "neon", "Апельсиновый градиент", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60", "@nails_by_me", 265),
            ContestEntry("g1", "gothic", "Черный велюр", "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60", "@goth_girl", 423),
            ContestEntry("g2", "gothic", "Бордовые стразы", "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60", "@dark_beauty", 389),
            ContestEntry("g3", "gothic", "Изумруд и поталь", "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60", "@emerald_nails", 215)
        )
    )
    val contestEntries: StateFlow<List<ContestEntry>> = _contestEntries.asStateFlow()

    private val _votingPair = MutableStateFlow<Pair<ContestEntry, ContestEntry>?>(null)
    val votingPair: StateFlow<Pair<ContestEntry, ContestEntry>?> = _votingPair.asStateFlow()

    fun selectContest(contestId: String) {
        selectedContestId.value = contestId
        generateVotingPair()
    }

    fun generateVotingPair() {
        val currentContest = selectedContestId.value
        val list = _contestEntries.value.filter { it.contestId == currentContest }
        if (list.size >= 2) {
            val shuffled = list.shuffled()
            _votingPair.value = Pair(shuffled[0], shuffled[1])
        } else {
            _votingPair.value = null
        }
    }

    fun voteForEntry(winnerId: String, @Suppress("UNUSED_PARAMETER") loserId: String) {
        viewModelScope.launch {
            _contestEntries.value = _contestEntries.value.map { entry ->
                if (entry.id == winnerId) entry.copy(votes = entry.votes + 1) else entry
            }
            val pointsAwarded = if (_hasVipChallengerPass.value) 10 else 5
            repository.addBonusPoints(pointsAwarded, "Голосование в конкурсе 🗳️")
            syncUserProfileToCloud()
            generateVotingPair()
        }
    }

    fun submitDesignToContest(
        contestId: String,
        title: String,
        imageUrl: String,
        authorName: String,
        context: Context
    ): Boolean {
        val loyalty = loyaltyInfo.value
        val currentPoints = loyalty?.points ?: 0
        val cost = 30

        if (!_hasVipChallengerPass.value && currentPoints < cost) {
            Toast.makeText(
                context,
                "Недостаточно бонусов! Стоимость участия: $cost Б.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        viewModelScope.launch {
            if (!_hasVipChallengerPass.value) {
                repository.spendBonusPoints(cost, "Участие в конкурсе '$contestId'")
                syncUserProfileToCloud()
            }

            val newEntry = ContestEntry(
                id = "user_${System.currentTimeMillis()}",
                contestId = contestId,
                title = title,
                imageUrl = imageUrl,
                authorName = authorName,
                votes = 0,
                isUserSubmitted = true
            )

            _contestEntries.value = _contestEntries.value + newEntry
            generateVotingPair()
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        radioController.stopRadio()
    }

    companion object {
        fun provideFactory(application: Application, uid: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(
                        application = application,
                        currentUserId = uid,
                        cloudSyncRepository = CloudSyncRepository(application.applicationContext)
                    ) as T
                }
            }
        }
    }
}
