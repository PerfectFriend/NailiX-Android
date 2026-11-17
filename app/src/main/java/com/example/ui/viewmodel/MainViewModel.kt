package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Booking
import com.example.data.Reminder
import com.example.data.ServiceItem
import com.example.data.UserLoyalty
import com.example.data.ReferredFriend
import com.example.data.LoyaltyTransaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.media.MediaPlayer
import android.media.AudioAttributes

data class RadioStation(
    val id: String,
    val name: String,
    val genre: String,
    val country: String,
    val url: String
)

data class ClientProfile(
    val name: String,
    val phone: String,
    val email: String = "",
    val totalBookings: Int = 0,
    val totalSpent: Double = 0.0,
    val isSubscribed: Boolean = false,
    val rating: Double = 5.0,
    val ratingCount: Int = 1,
    val homeAddress: String = "Calella, Carrer de Jovara 100",
    val homeLat: Double = 41.6140,
    val homeLon: Double = 2.6600,
    val workAddress: String = "Calella, Carrer de Sant Jaume 150",
    val workLat: Double = 41.6160,
    val workLon: Double = 2.6630,
    val referralCode: String = "VIC-8842-NIL"
)

data class AdminStats(
    val totalRevenue: Double = 0.0,
    val activeBookings: Int = 0,
    val completedBookings: Int = 0,
    val popularCategory: String = "Нет",
    val topMaster: String = "Нет"
)

data class MasterProfile(
    val id: Int,
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val workingSchedule: String,
    val experienceYears: Int,
    val languages: List<String>,
    val instagramUsername: String,
    val rating: Double = 5.0,
    val ratingCount: Int = 1,
    val isUserCreated: Boolean = false,
    val currency: String = "EUR"
)

data class AggregatorReview(
    val id: Int,
    val targetId: String, // master ID (stringified) or client phone
    val targetType: String, // "master" or "client"
    val authorName: String,
    val rating: Int,
    val comment: String,
    val dateStr: String
)

data class CustomAd(
    val id: Int,
    val masterName: String,
    val title: String,
    val description: String,
    val discount: String,
    val themeColor: String,
    val ctaText: String = "Записаться"
)

data class NailContest(
    val id: String,
    val title: String,
    val desc: String,
    val prize: String,
    val endDate: String,
    val icon: String
)

data class ContestEntry(
    val id: String,
    val contestId: String,
    val title: String,
    val imageUrl: String,
    val authorName: String,
    val votes: Int,
    val isUserSubmitted: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)
    private val prefs = application.getSharedPreferences("nailix_prefs", android.content.Context.MODE_PRIVATE)

    // ----------------------------------------------------
    // ADVERTISING SYSTEM FOR FREE VERSION
    // ----------------------------------------------------
    private val _adSource = MutableStateFlow(prefs.getString("ad_source", "google") ?: "google")
    val adSource: StateFlow<String> = _adSource.asStateFlow()

    private val _customAds = MutableStateFlow<List<CustomAd>>(emptyList())
    val customAds: StateFlow<List<CustomAd>> = _customAds.asStateFlow()

    fun setAdSource(source: String) {
        _adSource.value = source
        prefs.edit().putString("ad_source", source).apply()
    }

    private fun customAdToJson(ad: CustomAd): String {
        val json = org.json.JSONObject()
        json.put("id", ad.id)
        json.put("masterName", ad.masterName)
        json.put("title", ad.title)
        json.put("description", ad.description)
        json.put("discount", ad.discount)
        json.put("themeColor", ad.themeColor)
        json.put("ctaText", ad.ctaText)
        return json.toString()
    }

    private fun jsonToCustomAd(jsonStr: String): CustomAd {
        val json = org.json.JSONObject(jsonStr)
        return CustomAd(
            id = json.optInt("id", 0),
            masterName = json.optString("masterName", ""),
            title = json.optString("title", ""),
            description = json.optString("description", ""),
            discount = json.optString("discount", ""),
            themeColor = json.optString("themeColor", "Pink"),
            ctaText = json.optString("ctaText", "Записаться")
        )
    }

    fun loadCustomAds() {
        val jsonStr = prefs.getString("custom_ads", null)
        if (jsonStr == null) {
            _customAds.value = listOf(
                CustomAd(
                    id = 1,
                    masterName = "Виктория (Ручная Роспись)",
                    title = "Скидка на первый визит! 💅",
                    description = "Попробуйте уникальный дизайн ручной росписи со скидкой!",
                    discount = "10%",
                    themeColor = "Pink",
                    ctaText = "Хочу скидку!"
                ),
                CustomAd(
                    id = 2,
                    masterName = "Елена (SMART Аппаратный Педикюр)",
                    title = "Гладкие пяточки к лету! 🦶✨",
                    description = "SMART педикюр премиум-уровня с гарантией результата.",
                    discount = "15%",
                    themeColor = "Gold",
                    ctaText = "Записаться"
                )
            )
        } else {
            val list = mutableListOf<CustomAd>()
            try {
                val arr = org.json.JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    list.add(jsonToCustomAd(arr.getString(i)))
                }
                _customAds.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveCustomAds(list: List<CustomAd>) {
        _customAds.value = list
        val arr = org.json.JSONArray()
        list.forEach { arr.put(customAdToJson(it)) }
        prefs.edit().putString("custom_ads", arr.toString()).apply()
    }

    fun addCustomAd(ad: CustomAd) {
        val current = _customAds.value.toMutableList()
        current.add(0, ad)
        saveCustomAds(current)
    }

    // ----------------------------------------------------
    // SALON AGGREGATOR ENGINE STATES
    // ----------------------------------------------------
    private val _userRole = MutableStateFlow("client") // "client", "master"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    // Multiple profiles on device
    val isPremium = MutableStateFlow(false)
    val savedClientProfiles = MutableStateFlow<List<ClientProfile>>(emptyList())
    val savedMasterProfiles = MutableStateFlow<List<MasterProfile>>(emptyList())
    val primaryClientPhone = MutableStateFlow("")
    val primaryMasterId = MutableStateFlow<Int?>(null)

    val clientName = MutableStateFlow("")
    val clientPhone = MutableStateFlow("")

    // JSON Helper methods for ClientProfile
    private fun clientProfileToJson(profile: ClientProfile): String {
        val json = org.json.JSONObject()
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
        val json = org.json.JSONObject(jsonStr)
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
            referralCode = json.optString("referralCode", "VIC-8842-NIL")
        )
    }

    // JSON Helper methods for MasterProfile
    private fun masterProfileToJson(profile: MasterProfile): String {
        val json = org.json.JSONObject()
        json.put("id", profile.id)
        json.put("name", profile.name)
        json.put("address", profile.address)
        json.put("lat", profile.lat)
        json.put("lon", profile.lon)
        json.put("workingSchedule", profile.workingSchedule)
        json.put("experienceYears", profile.experienceYears)
        val langsArr = org.json.JSONArray()
        profile.languages.forEach { langsArr.put(it) }
        json.put("languages", langsArr)
        json.put("instagramUsername", profile.instagramUsername)
        json.put("rating", profile.rating)
        json.put("ratingCount", profile.ratingCount)
        json.put("isUserCreated", profile.isUserCreated)
        json.put("currency", profile.currency)
        return json.toString()
    }

    private fun jsonToMasterProfile(jsonStr: String): MasterProfile {
        val json = org.json.JSONObject(jsonStr)
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
            currency = json.optString("currency", "EUR")
        )
    }

    fun saveProfiles() {
        val clientArray = org.json.JSONArray()
        savedClientProfiles.value.forEach { clientArray.put(clientProfileToJson(it)) }
        
        val masterArray = org.json.JSONArray()
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
        _isDarkTheme.value = prefs.getBoolean("is_dark_theme", true) // Default is dark theme!
        val savedLang = prefs.getString("onboarding_language", null)
        if (savedLang != null) {
            _onboardingLanguage.value = savedLang
        }

        isPremium.value = isPrem
        _isOnboarded.value = prefs.getBoolean("is_onboarded", false)

        val clientList = mutableListOf<ClientProfile>()
        if (clientsJsonStr != null) {
            try {
                val arr = org.json.JSONArray(clientsJsonStr)
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
                val arr = org.json.JSONArray(mastersJsonStr)
                for (i in 0 until arr.length()) {
                    masterList.add(jsonToMasterProfile(arr.getString(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Purge any legacy default mock profile data if present
        clientList.removeAll { it.phone == "+34612345678" || it.name == "Дарья Козлова" || it.phone == "+7 999 000-00-00" || it.name == "Гость NailiX" || it.name == "NailiX Guest" }
        masterList.removeAll { it.id == 99 || it.id in listOf(1, 2, 3) || it.name.contains("Виктория (Ручная Роспись)") || it.name.contains("Виктория Мастер") || it.instagramUsername == "victoria1001art" }

        primaryClientPhone.value = primClientPhone
        primaryMasterId.value = if (primMasterId != -1) primMasterId else null

        savedClientProfiles.value = clientList
        savedMasterProfiles.value = masterList

        if (clientList.isNotEmpty()) {
            val activeClient = clientList.find { it.phone == primaryClientPhone.value } ?: clientList.first()
            _clientProfile.value = activeClient
            clientName.value = activeClient.name
            clientPhone.value = activeClient.phone
        } else {
            _clientProfile.value = ClientProfile(name = "", phone = "", email = "", homeAddress = "", workAddress = "")
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
            return false // Frozen!
        }
        val profile = list.find { it.phone == phone } ?: return false
        _clientProfile.value = profile
        clientName.value = profile.name
        clientPhone.value = profile.phone
        _userRole.value = "client"
        return true
    }

    fun activateMasterProfile(id: Int): Boolean {
        val list = savedMasterProfiles.value
        val isPrim = (id == primaryMasterId.value)
        if (!isPremium.value && !isPrim && list.size > 1) {
            return false // Frozen!
        }
        val profile = list.find { it.id == id } ?: return false
        _registeredMasterProfile.value = profile
        _userRole.value = "master"
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
        val newProfile = ClientProfile(
            name = name,
            phone = phone,
            email = email,
            homeAddress = "Calella, Carrer de Jovara 100",
            homeLat = 41.6140,
            homeLon = 2.6600,
            workAddress = "Calella, Carrer de Sant Jaume 150",
            workLat = 41.6160,
            workLon = 2.6630
        )
        val newList = list + newProfile
        savedClientProfiles.value = newList
        if (primaryClientPhone.value.isEmpty()) {
            primaryClientPhone.value = phone
        }
        saveProfiles()
        return null
    }

    fun createNewMasterProfile(name: String, address: String, schedule: String, experience: Int, languages: List<String>, instagram: String): String? {
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
            lat = 41.6150 + (0.001 * (-2..2).random()),
            lon = 2.6620 + (0.001 * (-2..2).random()),
            workingSchedule = schedule.ifEmpty { "Пн-Пт: 09:00 - 18:00" },
            experienceYears = experience,
            languages = languages.ifEmpty { listOf("Испанский") },
            instagramUsername = instagram.ifEmpty { "new_nail_artist" },
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
    }

    // Onboarding Engine States
    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _onboardingStep = MutableStateFlow(0) // 0: Welcome Feature Tour, 1: Language (optional), 2: Role, 3: Profile Info, 4: Master Setup, 5: Profile Chooser
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    private val _onboardingLanguage = MutableStateFlow("ru") // "ru", "en", "es"
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

    private val _selectedSkin = MutableStateFlow("gaudi") // "gaudi", "barcelona", "van_gogh", "amsterdam", "moscow", "paris", "nails", "flowers", "cosmos", "esoteric"
    val selectedSkin: StateFlow<String> = _selectedSkin.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true) // Dark by default!
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _globalCurrency = MutableStateFlow("EUR")
    val globalCurrency: StateFlow<String> = _globalCurrency.asStateFlow()

    fun setSelectedSkin(skin: String) {
        _selectedSkin.value = skin
        saveProfiles()
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        saveProfiles()
    }

    fun setGlobalCurrency(currency: String) {
        _globalCurrency.value = currency
        saveProfiles()
    }

    private val _onboardingRole = MutableStateFlow<String?>("client") // Default to client, can select
    val onboardingRole: StateFlow<String?> = _onboardingRole.asStateFlow()

    private val _onboardingAvatarUrl = MutableStateFlow<String?>(null)
    val onboardingAvatarUrl: StateFlow<String?> = _onboardingAvatarUrl.asStateFlow()

    private val _isGeneratingAvatar = MutableStateFlow(false)
    val isGeneratingAvatar: StateFlow<Boolean> = _isGeneratingAvatar.asStateFlow()

    private val _subscriptionTier = MutableStateFlow("free") // "free", "pro", "promax"
    val subscriptionTier: StateFlow<String> = _subscriptionTier.asStateFlow()

    private val _isInstagramConnecting = MutableStateFlow(false)
    val isInstagramConnecting: StateFlow<Boolean> = _isInstagramConnecting.asStateFlow()

    private val _instagramImportFeed = MutableStateFlow<List<com.example.data.InstagramPost>>(emptyList())
    val instagramImportFeed: StateFlow<List<com.example.data.InstagramPost>> = _instagramImportFeed.asStateFlow()

    private val _selectedImportPosts = MutableStateFlow<Set<Int>>(emptySet())
    val selectedImportPosts: StateFlow<Set<Int>> = _selectedImportPosts.asStateFlow()

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step
    }

    fun setOnboardingLanguage(lang: String) {
        _onboardingLanguage.value = lang
        saveProfiles()
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

    fun simulateAiAvatarGeneration(prompt: String) {
        viewModelScope.launch {
            _isGeneratingAvatar.value = true
            delay(2500) // Synthesizing latency
            val lowerPrompt = prompt.lowercase()
            val simulatedUrl = when {
                lowerPrompt.contains("pink") || lowerPrompt.contains("розов") || lowerPrompt.contains("мими") -> 
                    "https://images.unsplash.com/photo-1604303739265-fd53384d78af?w=500&auto=format&fit=crop&q=60"
                lowerPrompt.contains("dark") || lowerPrompt.contains("темн") || lowerPrompt.contains("black") || lowerPrompt.contains("черн") -> 
                    "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60"
                lowerPrompt.contains("gold") || lowerPrompt.contains("золот") || lowerPrompt.contains("блест") -> 
                    "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60"
                else -> 
                    "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60"
            }
            _onboardingAvatarUrl.value = simulatedUrl
            _isGeneratingAvatar.value = false
        }
    }

    fun loadTop20InstagramPosts(username: String = "") {
        viewModelScope.launch {
            _isInstagramConnecting.value = true
            val target = username.trim().removePrefix("@").ifBlank { _instagramUsername.value.removePrefix("@").ifBlank { "art_nail_master" } }
            _instagramUsername.value = target
            try {
                val scrapedPosts = com.example.data.InstagramRealScraper.fetchPosts(target)
                val sortedPosts = scrapedPosts.sortedByDescending { it.likesCount }.take(20)
                _instagramImportFeed.value = sortedPosts
                // Mark all top 20 posts selected by default for shortest 1-tap import
                _selectedImportPosts.value = sortedPosts.map { it.id }.toSet()
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error fetching top 20 posts", e)
                val backup = com.example.data.InstagramRealScraper.getTop20PopularPosts(target)
                _instagramImportFeed.value = backup
                _selectedImportPosts.value = backup.map { it.id }.toSet()
            }
            _isInstagramConnecting.value = false
        }
    }

    fun startInstagramSimulationImport() {
        loadTop20InstagramPosts(_instagramUsername.value)
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
            val targetUser = username.trim().removePrefix("@").ifBlank { "art_nail_master" }
            _instagramUsername.value = targetUser
            
            try {
                val scrapedPosts = com.example.data.InstagramRealScraper.fetchPosts(targetUser)
                val top20Posts = scrapedPosts.sortedByDescending { it.likesCount }.take(20)
                
                val currentList = _parsedInstagramPosts.value.toMutableList()
                val existingIds = currentList.map { it.id }.toSet()
                val uniqueNew = top20Posts.filter { it.id !in existingIds }
                
                currentList.addAll(0, uniqueNew)
                _parsedInstagramPosts.value = currentList
                
                val displayIds = _selectedDisplayPostIds.value.toMutableSet()
                displayIds.addAll(top20Posts.map { it.id })
                _selectedDisplayPostIds.value = displayIds.take(20).toSet()
            } catch (e: Exception) {
                val backup = com.example.data.InstagramRealScraper.getTop20PopularPosts(targetUser)
                val currentList = _parsedInstagramPosts.value.toMutableList()
                val existingIds = currentList.map { it.id }.toSet()
                val uniqueNew = backup.filter { it.id !in existingIds }
                
                currentList.addAll(0, uniqueNew)
                _parsedInstagramPosts.value = currentList
                
                val displayIds = _selectedDisplayPostIds.value.toMutableSet()
                displayIds.addAll(backup.map { it.id })
                _selectedDisplayPostIds.value = displayIds.take(20).toSet()
            }
            _isInstagramParsing.value = false
            onComplete()
        }
    }

    fun toggleImportPostSelection(postId: Int) {
        val current = _selectedImportPosts.value
        if (current.contains(postId)) {
            _selectedImportPosts.value = current - postId
        } else {
            if (current.size >= 20) {
                // Max 20 posts allowed
                return
            }
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
        
        // Select up to 20 posts for display
        _selectedDisplayPostIds.value = finalSelection.take(20).map { it.id }.toSet()
    }

    fun completeOnboarding() {
        _isOnboarded.value = true
        saveProfiles()
    }

    fun resetOnboarding() {
        _isOnboarded.value = false
        val sysLang = Locale.getDefault().language.lowercase()
        val supportedLangs = setOf("en", "ru", "es", "zh", "hi", "ar", "pt", "fr", "he", "tr")
        val resolvedLang = if (sysLang in supportedLangs) sysLang else "en"
        _onboardingLanguage.value = resolvedLang
        _onboardingStep.value = 0
        _onboardingAvatarUrl.value = null
        saveProfiles()
    }

    fun quickStartAsGuest(role: String = "client") {
        _userRole.value = role
        _onboardingRole.value = role
        if (role == "client") {
            val defaultName = if (_onboardingLanguage.value == "ru") "Гость NailiX" else "NailiX Guest"
            registerClientProfile(
                name = defaultName,
                phone = "+7 999 000-00-00",
                homeAddr = "Calella Center",
                workAddr = "Barcelona"
            )
        } else {
            if (savedMasterProfiles.value.isNotEmpty()) {
                activateMasterProfile(savedMasterProfiles.value.first().id)
            }
        }
        _isOnboarded.value = true
        saveProfiles()
    }

    fun addManualPortfolioPost(post: com.example.data.InstagramPost): Boolean {
        val limit = when (_subscriptionTier.value) {
            "free" -> 20
            "pro" -> 100
            "promax" -> 100
            else -> 20
        }
        val currentSize = _parsedInstagramPosts.value.size
        if (currentSize >= limit) {
            return false
        }
        val current = _parsedInstagramPosts.value.toMutableList()
        current.add(0, post)
        _parsedInstagramPosts.value = current
        return true
    }

    fun addNewPortfolioPost(post: com.example.data.InstagramPost) {
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

    // Default Masters (empty by default - masters are created by users in onboarding)
    private val defaultMasters = emptyList<MasterProfile>()

    private val _mastersList = MutableStateFlow<List<MasterProfile>>(emptyList())
    val mastersList: StateFlow<List<MasterProfile>> = _mastersList.asStateFlow()

    // Clean user profile state (empty until created by user in onboarding)
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
            workLon = 2.6630
        )
    )
    val clientProfile: StateFlow<ClientProfile> = _clientProfile.asStateFlow()

    // User's Registered Master Profile
    private val _registeredMasterProfile = MutableStateFlow<MasterProfile?>(null)
    val registeredMasterProfile: StateFlow<MasterProfile?> = _registeredMasterProfile.asStateFlow()

    // Initial Reviews database
    private val defaultReviews = listOf(
        AggregatorReview(1, "1", "master", "Мария Резникова", 5, "Лучший мастер в Каталонии! Рисунки просто невероятные, настоящие произведения искусства. Очень приятная атмосфера у Виктории в студии.", "05.07.2026"),
        AggregatorReview(2, "1", "master", "Анна Смирнова", 5, "Очень аккуратный маникюр с зеркальным хромом. Держится уже 4 недели идеально! Всем рекомендую.", "02.07.2026"),
        AggregatorReview(3, "1", "master", "Ирина Белова", 5, "Тончайшие линии ручной росписи, рисует словно принтер! Обожаю её работы.", "29.06.2026"),
        AggregatorReview(4, "2", "master", "Ольга Петрова", 5, "Шикарный смарт-педикюр! Пяточки нежные, как у младенца. Очень рекомендую Елену.", "04.07.2026"),
        AggregatorReview(5, "2", "master", "Татьяна Соколова", 4, "Быстро и качественно, очень приятная мастер. Обязательно вернусь.", "01.07.2026")
    )

    private val _reviewsList = MutableStateFlow<List<AggregatorReview>>(defaultReviews)
    val reviewsList: StateFlow<List<AggregatorReview>> = _reviewsList.asStateFlow()

    // Escrow payment state during checkout
    private val _escrowCheckoutBooking = MutableStateFlow<Booking?>(null)
    val escrowCheckoutBooking: StateFlow<Booking?> = _escrowCheckoutBooking.asStateFlow()

    // Fee Waiver Unlocked (Exchange 400 pts for 0% commission)
    private val _feeWaiverUnlocked = MutableStateFlow(false)
    val feeWaiverUnlocked: StateFlow<Boolean> = _feeWaiverUnlocked.asStateFlow()

    init {
        // Automatically check current system language settings
        val sysLang = Locale.getDefault().language.lowercase()
        val supportedLangs = setOf("en", "ru", "es", "zh", "hi", "ar", "pt", "fr", "he", "tr")
        val resolvedLang = if (sysLang in supportedLangs) sysLang else "en"
        _onboardingLanguage.value = resolvedLang

        // Splash auto-dismiss timer (4 seconds)
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            _showSplash.value = false
        }

        loadProfiles()
        loadCustomAds()

        viewModelScope.launch {
            repository.ensureLoyaltyInitialized()
            repository.ensureServicesInitialized()
            repository.ensureLocalizationsInitialized()
            repository.ensureMasterNailArtsInitialized()
            generateVotingPair()
        }
    }

    // Math calculation for walking distance in meters (Haversine formula)
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toInt()
    }

    // Update client home and work addresses and coordinates
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
    }

    // Custom Master Registration Flow
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
        val newMaster = MasterProfile(
            id = newId,
            name = name,
            address = address,
            lat = 41.6150 + (0.001 * (-2..2).random()), // simulated nearby coordinate
            lon = 2.6620 + (0.001 * (-2..2).random()),
            workingSchedule = schedule.ifEmpty { "Пн-Пт: 09:00 - 18:00" },
            experienceYears = experience,
            languages = languages.ifEmpty { listOf("Испанский") },
            instagramUsername = instagram.ifEmpty { "new_nail_artist" },
            rating = 5.0,
            ratingCount = 1,
            isUserCreated = true,
            currency = currency
        )
        _registeredMasterProfile.value = newMaster
        _userRole.value = "master"
        _instagramUsername.value = instagram.ifEmpty { "new_nail_artist" }
        triggerInstagramParsing()

        val currentList = savedMasterProfiles.value.toMutableList()
        val index = currentList.indexOfFirst { it.instagramUsername.equals(instagram, ignoreCase = true) }
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
            currency = currency
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
        }
    }

    fun updateBookingStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(id, status)
        }
    }

    // ----------------------------------------------------
    // SOVEREIGN ONION NODE & SIMPLEX MQ INTEGRATION
    // ----------------------------------------------------
    val sovereignNodeStatus = com.example.data.SovereignNodeManager.nodeStatus
    val simplexQueue = com.example.data.SovereignNodeManager.simplexQueue
    val simplexMessages = com.example.data.SovereignNodeManager.simplexMessages
    val p2pSyncState = com.example.data.SovereignNodeManager.p2pSyncState
    val roiCalc = com.example.data.SovereignNodeManager.roiCalc

    fun pingSovereignNode() {
        viewModelScope.launch {
            com.example.data.SovereignNodeManager.pingNode()
        }
    }

    fun toggleTorRouting(enabled: Boolean) {
        com.example.data.SovereignNodeManager.toggleTorRouting(enabled)
    }

    fun updateNodeAddress(onion: String, local: String) {
        com.example.data.SovereignNodeManager.updateNodeAddress(onion, local)
    }

    fun triggerP2PSync() {
        viewModelScope.launch {
            com.example.data.SovereignNodeManager.triggerP2PSync()
        }
    }

    fun updateRoiParams(masters: Int, fee: Double, aiGens: Int) {
        com.example.data.SovereignNodeManager.updateRoiParams(masters, fee, aiGens)
    }

    fun exportEncryptedBackup(): String {
        return com.example.data.SovereignNodeManager.exportEncryptedBackupJson()
    }

    // ----------------------------------------------------
    // FIREBASE CLOUD MESSAGING (FCM) NOTIFICATIONS
    // ----------------------------------------------------
    val fcmToken = com.example.notification.FcmNotificationManager.fcmToken
    val fcmNotifications = com.example.notification.FcmNotificationManager.notifications

    fun sendTestFcmAppointmentReminder(context: android.content.Context) {
        val master = _registeredMasterProfile.value?.name ?: "Виктория"
        com.example.notification.FcmNotificationManager.showSystemNotification(
            context = context,
            title = "Напоминание о записи 💅",
            body = "Здравствуйте! Ждем вас завтра в 15:00 на сеанс в студии NailiX. Мастер: $master. До встречи!",
            type = "appointment_reminder",
            data = mapOf("date" to "Завтра", "time" to "15:00", "master" to master)
        )
    }

    fun sendTestFcmPromotion(context: android.content.Context, title: String, text: String, promoCode: String) {
        val finalTitle = title.ifBlank { "Специальная акция от NailiX 🎁" }
        val finalText = text.ifBlank { "Скидка -20% на маникюр и дизайн по промокоду $promoCode!" }
        com.example.notification.FcmNotificationManager.showSystemNotification(
            context = context,
            title = finalTitle,
            body = finalText,
            type = "promotion",
            data = mapOf("promoCode" to promoCode, "type" to "promotion")
        )
    }

    // Custom Client Registration Flow
    fun registerClientProfile(
        name: String,
        phone: String,
        homeAddr: String,
        workAddr: String
    ) {
        val newProfile = _clientProfile.value.copy(
            name = name,
            phone = phone,
            homeAddress = homeAddr,
            workAddress = workAddr,
            homeLat = 41.6140 + (0.0008 * (-1..1).random()),
            homeLon = 2.6600 + (0.0008 * (-1..1).random()),
            workLat = 41.6160 + (0.0008 * (-1..1).random()),
            workLon = 2.6630 + (0.0008 * (-1..1).random())
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
    }

    // Leave feedback review for Master or Client
    fun leaveFeedback(
        targetId: String,
        targetType: String,
        rating: Int,
        comment: String,
        author: String
    ) {
        val nextId = _reviewsList.value.size + 1
        val newReview = AggregatorReview(
            id = nextId,
            targetId = targetId,
            targetType = targetType,
            authorName = author.ifEmpty { "Анонимный гость" },
            rating = rating,
            comment = comment,
            dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        )
        _reviewsList.value = _reviewsList.value + newReview

        // Recompute Target Master/Client ratings dynamically in state!
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
            // Update selected user master state if match
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

    // Swap Bonus points to unlock Platform Fee Waiver
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
                _referralMessage.emit("Слишком низкий рейтинг доверия! Требуется идеально чистая репутация и рейтинг 4.8+ (у вас $rating).")
            }
            return false
        }

        viewModelScope.launch {
            val updatedLoyalty = loyalty.copy(points = loyalty.points - 400)
            repository.loyaltyDao.insertOrUpdateLoyalty(updatedLoyalty)
            _feeWaiverUnlocked.value = true
            _referralMessage.emit("Поздравляем! 🎉 0% комиссия сервиса успешно активирована благодаря вашей идеальной репутации и баллам!")
        }
        return true
    }

    // Start booking process (opens escrow payment sheet)
    fun requestBookingEscrowCheckout() {
        val service = _selectedService.value ?: return
        val master = _selectedMaster.value ?: return
        val date = _selectedDate.value ?: return
        val slot = _selectedTimeSlot.value ?: return
        val name = clientName.value.trim().ifEmpty { _clientProfile.value.name }
        val phone = clientPhone.value.trim().ifEmpty { _clientProfile.value.phone }

        val previewBooking = Booking(
            serviceName = service.name,
            serviceCategory = service.category,
            date = date,
            timeSlot = slot,
            masterName = master,
            price = service.price,
            clientName = name,
            clientPhone = phone
        )
        _escrowCheckoutBooking.value = previewBooking
    }

    fun dismissEscrowCheckout() {
        _escrowCheckoutBooking.value = null
    }

    // Finalize booking after 20% deposit payment
    fun payEscrowAndCompleteBooking() {
        val booking = _escrowCheckoutBooking.value ?: return
        viewModelScope.launch {
            _bookingLoading.value = true
            delay(1500) // Simulated checkout processing

            val newId = repository.insertBooking(booking)
            val savedBooking = booking.copy(id = newId.toInt())

            // Create notification alerts
            repository.addReminder(
                Reminder(
                    bookingId = savedBooking.id,
                    title = "Депозит Escrow (20%) зачислен! 🔒",
                    message = "Внесено гарантированное обеспечение за услугу '${savedBooking.serviceName}' у мастера ${savedBooking.masterName}.",
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

            // Clear selected fields
            _selectedService.value = null
            _selectedMaster.value = null
            _selectedDate.value = null
            _selectedTimeSlot.value = null
            _escrowCheckoutBooking.value = null
            _bookingSuccessDialog.value = savedBooking
            _bookingLoading.value = false
        }
    }

    // Active screen navigation
    // "home", "booking", "designs", "loyalty", "portfolio"
    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    fun navigateTo(tab: String) {
        _currentTab.value = tab
    }

    // Admin Mode toggle and sync status
    val isAdminMode = MutableStateFlow(false)
    val isSyncing = MutableStateFlow(false)
    val ownerSyncStatus = MutableStateFlow("Синхронизировано")

    // Exposed Flows from Repository
    val bookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loyaltyInfo: StateFlow<UserLoyalty?> = repository.loyaltyState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDesignIds: StateFlow<List<Int>> = repository.favoriteDesigns
        .map { list -> list.map { it.designId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Price List Services flow from Room
    val services: StateFlow<List<ServiceItem>> = repository.dynamicServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referredFriends: StateFlow<List<ReferredFriend>> = repository.allReferredFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loyaltyTransactions: StateFlow<List<LoyaltyTransaction>> = repository.allLoyaltyTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterNailArts: StateFlow<List<com.example.data.MasterNailArtEntity>> = repository.allMasterNailArts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedNailArtForBooking = MutableStateFlow<com.example.data.MasterNailArtEntity?>(null)

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

    fun publishMasterNailArt(title: String, masterName: String, complexity: String, extraPrice: Double, imageUrl: String, description: String) {
        viewModelScope.launch {
            val entity = com.example.data.MasterNailArtEntity(
                title = title,
                masterName = masterName,
                complexity = complexity,
                extraPrice = extraPrice,
                imageUrl = imageUrl,
                description = description
            )
            repository.masterNailArtDao.insertMasterNailArt(entity)
        }
    }

    // Compile customer profile list dynamically from Database
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

    // Compile analytical statistics
    val statistics: StateFlow<AdminStats> = bookings.map { list ->
        val confirmed = list.filter { it.status != "Отменена" }
        val revenue = confirmed.sumOf { it.price }
        val activeCount = list.filter { it.status == "Подтверждена" }.size
        val completedCount = list.filter { it.status == "Выполнена" }.size
        
        val categoryPopularity = confirmed.groupBy { it.serviceCategory }
            .mapValues { it.value.size }
        val popularCategory = categoryPopularity.maxByOrNull { it.value }?.key ?: "Нет"
        
        val masterPopularity = confirmed.groupBy { it.masterName }
            .mapValues { it.value.size }
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

    fun submitBooking() {
        val service = _selectedService.value ?: return
        val master = _selectedMaster.value ?: return
        val date = _selectedDate.value ?: return
        val slot = _selectedTimeSlot.value ?: return
        val name = clientName.value.trim()
        val phone = clientPhone.value.trim()

        if (name.isEmpty() || phone.isEmpty()) return

        viewModelScope.launch {
            _bookingLoading.value = true
            delay(1500) // Realistic delay

            val newBooking = Booking(
                serviceName = service.name,
                serviceCategory = service.category,
                date = date,
                timeSlot = slot,
                masterName = master,
                price = service.price,
                clientName = name,
                clientPhone = phone
            )

            val newId = repository.insertBooking(newBooking)
            val savedBooking = newBooking.copy(id = newId.toInt())
            
            // Automatically queue reminder simulation notifications for the client!
            val currentLang = _onboardingLanguage.value
            val reminderTitle1 = if (currentLang == "ru") "Напоминание о визите 💅" else if (currentLang == "es") "Recordatorio de visita 💅" else "Appointment Reminder 💅"
            val reminderMsg1 = if (currentLang == "ru") {
                "Студия NailiX ждет вас завтра в ${savedBooking.timeSlot} на услугу '${savedBooking.serviceName}' к мастеру ${savedBooking.masterName}!"
            } else if (currentLang == "es") {
                "¡El salón NailiX te espera mañana a las ${savedBooking.timeSlot} para el servicio '${savedBooking.serviceName}' con ${savedBooking.masterName}!"
            } else {
                "NailiX is waiting for you tomorrow at ${savedBooking.timeSlot} for the '${savedBooking.serviceName}' service with ${savedBooking.masterName}!"
            }

            val reminderTitle2 = if (currentLang == "ru") "Скоро запись! ✨" else if (currentLang == "es") "¡Tu cita pronto! ✨" else "Upcoming appointment! ✨"
            val reminderMsg2 = if (currentLang == "ru") {
                "Напоминаем: у вас запись сегодня в ${savedBooking.timeSlot} на '${savedBooking.serviceName}'. Адрес: Испания, Каталония, г. Калейя, ул. Moragas, д. 74."
            } else if (currentLang == "es") {
                "Recuerda: tienes una cita hoy a las ${savedBooking.timeSlot} para '${savedBooking.serviceName}'. Dirección: España, Cataluña, Calella, Moragas 74."
            } else {
                "Reminder: you have an appointment today at ${savedBooking.timeSlot} for '${savedBooking.serviceName}'. Address: Spain, Catalonia, Calella, Moragas 74."
            }

            val trigger1 = if (currentLang == "ru") "За 24 часа" else if (currentLang == "es") "24 horas antes" else "24 hours before"
            val trigger2 = if (currentLang == "ru") "За 2 часа" else if (currentLang == "es") "2 horas antes" else "2 hours before"

            repository.addReminder(
                Reminder(
                    bookingId = savedBooking.id,
                    title = reminderTitle1,
                    message = reminderMsg1,
                    triggerTimeBefore = trigger1
                )
            )
            repository.addReminder(
                Reminder(
                    bookingId = savedBooking.id,
                    title = reminderTitle2,
                    message = reminderMsg2,
                    triggerTimeBefore = trigger2
                )
            )

            // Sovereign SimpleX MQ E2E message dispatch
            com.example.data.SovereignNodeManager.sendSimplexBooking(
                bookingId = savedBooking.id,
                serviceName = savedBooking.serviceName,
                clientName = savedBooking.clientName,
                date = savedBooking.date,
                time = savedBooking.timeSlot
            )

            _bookingSuccessDialog.value = savedBooking
            _bookingLoading.value = false

            // Clear booking selection state
            _selectedService.value = null
            _selectedMaster.value = null
            _selectedDate.value = null
            _selectedTimeSlot.value = null
        }
    }

    fun cancelBooking(booking: Booking) {
        viewModelScope.launch {
            repository.deleteBooking(booking.id, booking.price)
        }
    }

    fun completeBooking(booking: Booking) {
        viewModelScope.launch {
            repository.completeBooking(booking.id)
        }
    }

    // ----------------------------------------------------
    // BASE DESIGNS GALLERY + FILTERS
    // ----------------------------------------------------
    val lengthFilter = MutableStateFlow<String?>(null)
    val shapeFilter = MutableStateFlow<String?>(null)
    val paletteFilter = MutableStateFlow<String?>(null)

    val baseDesigns = repository.baseDesigns

    val filteredDesigns: StateFlow<List<com.example.data.BaseDesignItem>> = combine(
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
    // PORTFOLIO + INSTAGRAM PARSING SIMULATION
    // ----------------------------------------------------
    private val _showOriginalInstagramLinks = MutableStateFlow(true)
    val showOriginalInstagramLinks: StateFlow<Boolean> = _showOriginalInstagramLinks.asStateFlow()

    private val _isInstagramParsing = MutableStateFlow(false)
    val isInstagramParsing: StateFlow<Boolean> = _isInstagramParsing.asStateFlow()

    private val _parsedInstagramPosts = MutableStateFlow<List<com.example.data.InstagramPost>>(emptyList())
    val parsedInstagramPosts: StateFlow<List<com.example.data.InstagramPost>> = _parsedInstagramPosts.asStateFlow()

    private val _instagramUsername = MutableStateFlow("")
    val instagramUsername: StateFlow<String> = _instagramUsername.asStateFlow()

    private val _selectedDisplayPostIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedDisplayPostIds: StateFlow<Set<Int>> = _selectedDisplayPostIds.asStateFlow()

    fun toggleDisplayPostSelection(postId: Int, context: android.content.Context? = null): Boolean {
        val current = _selectedDisplayPostIds.value
        if (current.contains(postId)) {
            _selectedDisplayPostIds.value = current - postId
            return true
        } else {
            if (current.size >= 20) {
                if (context != null) {
                    android.widget.Toast.makeText(context, "Можно выбрать не более 20 публикаций для отображения!", android.widget.Toast.LENGTH_SHORT).show()
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
        viewModelScope.launch {
            _isInstagramParsing.value = true
            delay(1200) // Simulated network parsing latency
            
            val usernameToParse = _instagramUsername.value.trim().removePrefix("@").ifBlank { "art_nail_master" }
            val top20 = com.example.data.InstagramRealScraper.getTop20PopularPosts(usernameToParse)
            
            val currentList = _parsedInstagramPosts.value.toMutableList()
            val existingIds = currentList.map { it.id }.toSet()
            val uniqueNew = top20.filter { it.id !in existingIds }
            currentList.addAll(0, uniqueNew)
            
            val limit = when (_subscriptionTier.value) {
                "free" -> 20
                "pro" -> 100
                "promax" -> 1000
                else -> 20
            }
            val finalSelection = currentList.take(limit)
            _parsedInstagramPosts.value = finalSelection
            _selectedDisplayPostIds.value = finalSelection.take(20).map { it.id }.toSet()
            _isInstagramParsing.value = false
        }
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
            _isSubscribedSuccessfully.emit("Успешная подписка! На ваш баланс зачислено +100 приветственных бонусов!")
            mailingEmail.value = ""
            mailingPhone.value = ""
        }
    }

    // ----------------------------------------------------
    // REFERRAL PROGRAM
    // ----------------------------------------------------
    val referralInputCode = MutableStateFlow("")
    private val _referralMessage = MutableSharedFlow<String>()
    val referralMessage: SharedFlow<String> = _referralMessage.asSharedFlow()

    fun claimReferralCode() {
        val code = referralInputCode.value.trim().uppercase()
        if (code.isEmpty()) return

        viewModelScope.launch {
            if (code == "VIC-8842-NIL") {
                _referralMessage.emit("Вы не можете использовать собственный реферальный код!")
                return@launch
            }
            if (!code.startsWith("VIC-") || code.length < 8) {
                _referralMessage.emit("Неверный формат реферального кода. Код должен начинаться с 'VIC-'")
                return@launch
            }
            repository.claimReferral(code)
            _referralMessage.emit("Код активирован! Вам начислено +200 бонусных баллов! Ваш пригласивший друг тоже получит бонус.")
            referralInputCode.value = ""
        }
    }

    fun simulateReferralInvite(friendName: String) {
        val name = friendName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.simulateReferralInvite(name)
            _referralMessage.emit("Реферал $name успешно добавлен в систему!")
        }
    }

    fun spendBonusPoints(amount: Int, reason: String) {
        viewModelScope.launch {
            repository.spendBonusPoints(amount, reason)
        }
    }

    // ----------------------------------------------------
    // PUSH NOTIFICATIONS / REMINDERS SIMULATOR
    // ----------------------------------------------------
    private val _inAppNotification = MutableStateFlow<String?>(null)
    val inAppNotification: StateFlow<String?> = _inAppNotification.asStateFlow()

    fun dismissInAppNotification() {
        _inAppNotification.value = null
    }

    fun simulateNotificationTrigger(reminder: Reminder) {
        viewModelScope.launch {
            // Update reminder isSent status in database
            repository.markReminderSent(reminder.id)
            // Trigger beautiful in-app notification banner
            _inAppNotification.value = "${reminder.title}\n${reminder.message}"
        }
    }

    // Helpers
    private fun generateNext7Days(): List<String> {
        val sdf = SimpleDateFormat("dd.MM (EEEE)", Locale("ru"))
        val list = mutableListOf<String>()
        val calendar = java.util.Calendar.getInstance()
        for (i in 0..6) {
            list.add(sdf.format(calendar.time))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    // ----------------------------------------------------
    // SALON OWNER ADMIN METHODS
    // ----------------------------------------------------
    fun addNewService(name: String, category: String, price: Double, durationMin: Int, description: String, imageUrl: String) {
        viewModelScope.launch {
            val serviceEntity = com.example.data.ServiceEntity(
                name = name,
                category = category,
                price = price,
                durationMin = durationMin,
                description = description,
                imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60" }
            )
            repository.insertService(serviceEntity)
            triggerAutomaticPushSync()
        }
    }

    fun updateServicePrice(id: Int, price: Double) {
        viewModelScope.launch {
            repository.updateServicePrice(id, price)
            triggerAutomaticPushSync()
        }
    }

    fun deleteService(id: Int) {
        viewModelScope.launch {
            repository.deleteService(id)
            triggerAutomaticPushSync()
        }
    }

    fun triggerAutomaticPushSync() {
        viewModelScope.launch {
            isSyncing.value = true
            ownerSyncStatus.value = "Синхронизация..."
            delay(1500)
            isSyncing.value = false
            ownerSyncStatus.value = "Обновлено на устройствах клиентов: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
            _inAppNotification.value = "📢 База данных синхронизирована!\nИзменения цен и карточек новых услуг автоматически доставлены на устройства клиентов с вашего устройства."
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
    // IN-APP SALON INTERNET RADIO STREAMER (SomaFM & Free Global Stations)
    // ----------------------------------------------------
    val topRadioStations = listOf(
        RadioStation("lofigirl", "Lofi Girl Beats 🌸", "Lofi / Chill", "Global", "https://ice1.somafm.com/lofi-128-mp3"),
        RadioStation("groovesalad", "SomaFM Groove Salad 🥗", "Ambient / Chillout", "Global", "https://ice1.somafm.com/groovesalad-128-mp3"),
        RadioStation("lounge", "SomaFM Lounge 🛋️", "Jazz / Lounge", "Global", "https://ice1.somafm.com/lounge-128-mp3"),
        RadioStation("paris", "Café de Paris 🥐", "Chanson / Acoustic", "France", "https://chantefrance.ice.infomaniak.ch/chantefrance-96.mp3"),
        RadioStation("swissjazz", "Radio Swiss Jazz 🎷", "Jazz / Smooth", "Switzerland", "https://stream.srg-ssr.ch/m/rsj/mp3_128"),
        RadioStation("swissclassic", "Radio Swiss Classic 🎻", "Classical", "Switzerland", "https://stream.srg-ssr.ch/m/rsc_de/mp3_128"),
        RadioStation("antenne", "Antenne Bayern Chill 🍹", "Lounge / Relax", "Germany", "https://stream.antenne.de/chillout/stream/mp3"),
        RadioStation("secretagent", "SomaFM Secret Agent 🕶️", "Beat / Lounge", "Global", "https://ice1.somafm.com/secretagent-128-mp3"),
        RadioStation("goa", "SomaFM Suburbs of Goa 👳", "World / Ethno-Chill", "Global", "https://ice1.somafm.com/suburbsofgoa-128-mp3"),
        RadioStation("deepspace", "SomaFM Deep Space One 🚀", "Ambient / Space", "Global", "https://ice1.somafm.com/deepspaceone-128-mp3"),
        RadioStation("poptron", "SomaFM PopTron ⚡", "Pop / Synthwave", "Global", "https://ice1.somafm.com/poptron-128-mp3"),
        RadioStation("indie", "SomaFM Indie Pop 🎸", "Indie / Alternative", "Global", "https://ice1.somafm.com/indiepop-128-mp3"),
        RadioStation("illinois", "Illinois Street Lounge 🍹", "Retro Lounge", "Global", "https://ice1.somafm.com/illinois-128-mp3"),
        RadioStation("bootliquor", "SomaFM Boot Liquor 🤠", "Americana / Roots", "Global", "https://ice1.somafm.com/bootliquor-128-mp3"),
        RadioStation("sonic", "SomaFM Sonic Universe 🌌", "Avant-garde Jazz", "Global", "https://ice1.somafm.com/sonicuniverse-128-mp3"),
        RadioStation("cliqhop", "SomaFM Cliqhop 👾", "Electronic / IDM", "Global", "https://ice1.somafm.com/cliqhop-128-mp3"),
        RadioStation("fluid", "SomaFM Fluid 🌊", "Instrumental Hip Hop", "Global", "https://ice1.somafm.com/fluid-128-mp3"),
        RadioStation("christmas", "Cozy Christmas Radio ❄️", "Holiday / Jazz", "Global", "https://ice1.somafm.com/christmas-128-mp3"),
        RadioStation("lush", "SomaFM Lush 💋", "Sensuous Vocals", "Global", "https://ice1.somafm.com/lush-128-mp3"),
        RadioStation("covers", "SomaFM Covers 🎤", "Acoustic Covers", "Global", "https://ice1.somafm.com/covers-128-mp3")
    )

    private var mediaPlayer: MediaPlayer? = null
    private var isMediaPlayerPrepared = false
    private var isMediaPlayerInError = false
    private var radioSessionId = 0L

    private val _radioIsPlaying = MutableStateFlow(false)
    val radioIsPlaying: StateFlow<Boolean> = _radioIsPlaying.asStateFlow()

    private val _radioIsLoading = MutableStateFlow(false)
    val radioIsLoading: StateFlow<Boolean> = _radioIsLoading.asStateFlow()

    private val _selectedStation = MutableStateFlow<RadioStation?>(
        RadioStation("lofigirl", "Lofi Girl Beats 🌸", "Lofi / Chill", "Global", "https://ice1.somafm.com/lofi-128-mp3")
    )
    val selectedStation: StateFlow<RadioStation?> = _selectedStation.asStateFlow()

    private val _radioError = MutableStateFlow<String?>(null)
    val radioError: StateFlow<String?> = _radioError.asStateFlow()

    private fun releaseMediaPlayer() {
        val currentMp = mediaPlayer ?: return
        val canStop = isMediaPlayerPrepared && !isMediaPlayerInError
        mediaPlayer = null
        isMediaPlayerPrepared = false
        isMediaPlayerInError = false

        runCatching {
            currentMp.setOnPreparedListener(null)
            currentMp.setOnErrorListener(null)
            currentMp.setOnCompletionListener(null)
            if (canStop) {
                if (currentMp.isPlaying) {
                    currentMp.pause()
                }
                currentMp.stop()
            }
        }
        runCatching {
            currentMp.release()
        }
    }

    fun playRadioStation(station: RadioStation) {
        val sessionId = ++radioSessionId
        viewModelScope.launch {
            var newMp: MediaPlayer? = null
            try {
                _radioError.value = null
                _selectedStation.value = station
                _radioIsLoading.value = true
                _radioIsPlaying.value = false

                // Safely release any previous session without calling stop() in PREPARING/IDLE/ERROR states
                releaseMediaPlayer()

                val mp = MediaPlayer()
                newMp = mp
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mp.setDataSource(station.url)

                if (sessionId != radioSessionId) {
                    runCatching { mp.release() }
                    return@launch
                }

                isMediaPlayerPrepared = false
                isMediaPlayerInError = false
                mediaPlayer = mp

                mp.setOnPreparedListener { preparedMp ->
                    if (mediaPlayer == preparedMp && sessionId == radioSessionId) {
                        isMediaPlayerPrepared = true
                        isMediaPlayerInError = false
                        _radioIsLoading.value = false
                        _radioIsPlaying.value = true
                        runCatching {
                            preparedMp.start()
                        }.onFailure { e ->
                            isMediaPlayerPrepared = false
                            isMediaPlayerInError = true
                            _radioIsPlaying.value = false
                            _radioError.value = e.localizedMessage ?: "Ошибка запуска радио"
                            releaseMediaPlayer()
                        }
                    } else {
                        runCatching { preparedMp.release() }
                    }
                }

                mp.setOnErrorListener { failedMp, what, extra ->
                    if (mediaPlayer == failedMp && sessionId == radioSessionId) {
                        isMediaPlayerPrepared = false
                        isMediaPlayerInError = true
                        _radioIsLoading.value = false
                        _radioIsPlaying.value = false
                        _radioError.value = "Ошибка стрима ($what, $extra)"
                        releaseMediaPlayer()
                    } else {
                        runCatching { failedMp.release() }
                    }
                    true
                }

                mp.setOnCompletionListener { completedMp ->
                    if (mediaPlayer == completedMp && sessionId == radioSessionId) {
                        _radioIsPlaying.value = false
                    }
                }

                mp.prepareAsync()
            } catch (e: Exception) {
                if (sessionId == radioSessionId) {
                    releaseMediaPlayer()
                    _radioIsLoading.value = false
                    _radioIsPlaying.value = false
                    _radioError.value = e.localizedMessage ?: "Ошибка подключения к радио"
                } else {
                    runCatching { newMp?.release() }
                }
            }
        }
    }

    fun toggleRadioPlayback() {
        val current = _selectedStation.value ?: return
        when {
            _radioIsLoading.value -> stopRadio()
            _radioIsPlaying.value -> pauseRadio()
            isMediaPlayerPrepared && !isMediaPlayerInError && mediaPlayer != null -> {
                runCatching {
                    mediaPlayer?.start()
                    _radioIsPlaying.value = true
                }.onFailure {
                    playRadioStation(current)
                }
            }
            else -> playRadioStation(current)
        }
    }

    fun pauseRadio() {
        try {
            mediaPlayer?.let { mp ->
                if (isMediaPlayerPrepared && !isMediaPlayerInError && mp.isPlaying) {
                    mp.pause()
                }
            }
            _radioIsPlaying.value = false
        } catch (e: Exception) {
            _radioError.value = e.localizedMessage
        }
    }

    fun stopRadio() {
        radioSessionId++
        releaseMediaPlayer()
        _radioIsPlaying.value = false
        _radioIsLoading.value = false
    }

    fun playRandomStation() {
        if (topRadioStations.isEmpty()) return
        val current = _selectedStation.value
        val available = topRadioStations.filter { it.id != current?.id }
        val nextStation = if (available.isNotEmpty()) available.random() else topRadioStations.random()
        playRadioStation(nextStation)
    }

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

    val activeContests = MutableStateFlow<List<NailContest>>(
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

    val selectedContestId = MutableStateFlow<String>("wedding")

    private val _contestEntries = MutableStateFlow<List<ContestEntry>>(
        listOf(
            // Wedding Contests
            ContestEntry("w1", "wedding", "Жемчужная пыльца", "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60", "@nastya_nails", 342),
            ContestEntry("w2", "wedding", "Классический френч", "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60", "@elena_salon", 289),
            ContestEntry("w3", "wedding", "Розовый минимализм", "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60", "@masha_beauty", 152),
            ContestEntry("w4", "wedding", "Золотая поталь на молоке", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60", "@kate_perfect", 405),

            // Neon Contests
            ContestEntry("n1", "neon", "Кислотный лайм", "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60", "@bright_girl", 512),
            ContestEntry("n2", "neon", "Коралловая искра", "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60", "@elena_salon", 418),
            ContestEntry("n3", "neon", "Апельсиновый градиент", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60", "@nails_by_me", 265),

            // Gothic Contests
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

    fun voteForEntry(winnerId: String, loserId: String) {
        viewModelScope.launch {
            _contestEntries.value = _contestEntries.value.map { entry ->
                if (entry.id == winnerId) {
                    entry.copy(votes = entry.votes + 1)
                } else {
                    entry
                }
            }
            val pointsAwarded = if (_hasVipChallengerPass.value) 10 else 5
            repository.addBonusPoints(pointsAwarded, "Голосование в конкурсе 🗳️")
            generateVotingPair()
        }
    }

    fun submitDesignToContest(
        contestId: String,
        title: String,
        imageUrl: String,
        authorName: String,
        context: android.content.Context
    ): Boolean {
        val loyalty = loyaltyInfo.value
        val currentPoints = loyalty?.points ?: 0
        val cost = 30
        
        if (!_hasVipChallengerPass.value && currentPoints < cost) {
            android.widget.Toast.makeText(
                context, 
                "Недостаточно бонусов! Стоимость участия: $cost Б. Или приобретите сверхдешевый 'Золотой абонемент' для бесплатных заявок!", 
                android.widget.Toast.LENGTH_LONG
            ).show()
            return false
        }
        
        viewModelScope.launch {
            if (!_hasVipChallengerPass.value) {
                repository.spendBonusPoints(cost, "Участие в конкурсе '$contestId'")
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
        stopRadio()
    }
}
