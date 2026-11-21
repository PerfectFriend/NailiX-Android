package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt
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

@Composable
fun HomeScreenTab(viewModel: MainViewModel) {
    val loyalty by viewModel.loyaltyInfo.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val emailInput by viewModel.mailingEmail.collectAsStateWithLifecycle()
    val phoneInput by viewModel.mailingPhone.collectAsStateWithLifecycle()

    val masters by viewModel.mastersList.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarkedMasterIds.collectAsStateWithLifecycle()
    val client by viewModel.clientProfile.collectAsStateWithLifecycle()
    val onboardingLanguage by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val selectedSkin by viewModel.selectedSkin.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val quickSlot = remember(masters, bookings, services, bookmarks) {
        viewModel.findNextAvailableQuickSlot()
    }

    var filterOnlyBookmarked by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    var activeDetailMaster by remember { mutableStateOf<MasterProfile?>(null) }
    var showCustomizeLocationDialog by remember { mutableStateOf(false) }

    val filteredMasters = masters.filter { master ->
        val matchesBookmark = !filterOnlyBookmarked || bookmarks.contains(master.id)
        val matchesSearch = master.name.contains(searchQuery, ignoreCase = true) ||
                master.languages.any { it.contains(searchQuery, ignoreCase = true) } ||
                master.address.contains(searchQuery, ignoreCase = true)
        matchesBookmark && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Section: Row with Header and Geo-points
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Image branding card
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .height(124.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_salon_banner_1783410932283),
                        contentDescription = "Salon premium hero banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "NailiX",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (onboardingLanguage == "ru") "Лучшие мастера у вас в руках" else if (onboardingLanguage == "es") "Los mejores maestros" else "Best salon masters",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            // Geo Location Card
            Card(
                modifier = Modifier
                    .weight(0.9f)
                    .height(124.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 10.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (onboardingLanguage == "ru") "📍 Мои гео-точки" else if (onboardingLanguage == "es") "📍 Mis ubicaciones" else "📍 My Locations",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = { showCustomizeLocationDialog = true },
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .testTag("edit_locations_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Location", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column(modifier = Modifier.padding(end = 6.dp)) {
                        Text(
                            text = "🏠 " + (if (onboardingLanguage == "ru") "Дом" else if (onboardingLanguage == "es") "Casa" else "Home") + ": ${client.homeAddress.ifBlank { "—" }}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "💼 " + (if (onboardingLanguage == "ru") "Работа" else if (onboardingLanguage == "es") "Trabajo" else "Work") + ": ${client.workAddress.ifBlank { "—" }}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 1.5 Quick 1-Tap Next Available Slot Card
        quickSlot?.let { suggestion ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_slot_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚡ Ближайшее окно",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${suggestion.master.name} • ${suggestion.service.name}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${suggestion.date}, ${suggestion.timeSlot}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { viewModel.quickBookNextAvailableSlot(suggestion) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("quick_book_one_tap_btn"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Занять в 1 тап", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Search & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (onboardingLanguage == "ru") "Поиск мастера..." else if (onboardingLanguage == "es") "Buscar maestro..." else "Search master...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", modifier = Modifier.size(16.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(50.dp)
                    .testTag("search_masters_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            
            FilterChip(
                selected = filterOnlyBookmarked,
                onClick = { filterOnlyBookmarked = !filterOnlyBookmarked },
                label = { Text(if (onboardingLanguage == "ru") "★ Избранные" else if (onboardingLanguage == "es") "★ Favoritos" else "★ Bookmarks", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .weight(0.8f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("filter_bookmarked_masters"),
                leadingIcon = if (filterOnlyBookmarked) {
                    { Icon(Icons.Default.Check, contentDescription = "Selected", modifier = Modifier.size(12.dp)) }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Spacious full-width master selection dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (onboardingLanguage == "ru") "👩‍🎨 Выберите мастера" else if (onboardingLanguage == "es") "👩‍🎨 Seleccionar maestro" else "👩‍🎨 Select master",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (filteredMasters.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (onboardingLanguage == "ru") "Нет мастеров" else if (onboardingLanguage == "es") "No hay maestros" else "No masters found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredMasters) { master ->
                            CompactMasterCard(
                                master = master,
                                viewModel = viewModel,
                                client = client,
                                bookmarks = bookmarks,
                                onDetail = { activeDetailMaster = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // Render Dialog Modals
    activeDetailMaster?.let { master ->
        MasterDetailsSheet(
            master = master,
            viewModel = viewModel,
            onDismiss = { activeDetailMaster = null }
        )
    }

    if (showCustomizeLocationDialog) {
        CustomizeLocationDialog(
            client = client,
            currentLang = onboardingLanguage,
            skinCode = selectedSkin,
            isDarkTheme = isDarkTheme,
            onLangChange = { viewModel.setOnboardingLanguage(it) },
            onDismiss = { showCustomizeLocationDialog = false },
            onSave = { hAddr, hLat, hLon, wAddr, wLat, wLon ->
                viewModel.updateClientLocations(hAddr, hLat, hLon, wAddr, wLat, wLon)
            }
        )
    }
}



@Composable
fun CompactMasterCard(
    master: MasterProfile,
    viewModel: MainViewModel,
    client: ClientProfile,
    bookmarks: Set<Int>,
    onDetail: (MasterProfile) -> Unit
) {
    val distHome = viewModel.calculateDistanceMeters(client.homeLat, client.homeLon, master.lat, master.lon)
    val distWork = viewModel.calculateDistanceMeters(client.workLat, client.workLon, master.lat, master.lon)
    val isBookmarked = bookmarks.contains(master.id)
    val selectedSkin by viewModel.selectedSkin.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetail(master) }
            .testTag("master_card_${master.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = master.name.split(" ").firstOrNull()?.take(1)?.uppercase() ?: "M",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = master.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${master.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${master.experienceYears} лет",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleBookmarkMaster(master.id) },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .testTag("bookmark_toggle_${master.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark toggle",
                        tint = if (isBookmarked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Дом: ${distHome}м", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Работа: ${distWork}м", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



@Composable
fun InteractiveMastersMap(
    masters: List<MasterProfile>,
    client: ClientProfile,
    searchQuery: String,
    bookmarks: Set<Int>,
    viewModel: MainViewModel,
    onViewMasterDetails: (MasterProfile) -> Unit,
    onBookMaster: (MasterProfile) -> Unit
) {
    var selectedCenterType by remember { mutableStateOf("home") } // "home" or "work"
    var zoomLevel by remember { mutableStateOf(1.0f) } // 0.6f to 2.5f
    var selectedRadiusMeters by remember { mutableStateOf(1500) } // 200, 500, 1000, 1500 (All)
    
    var selectedMapMaster by remember { mutableStateOf<MasterProfile?>(null) }

    val currentCenterLat = if (selectedCenterType == "home") client.homeLat else client.workLat
    val currentCenterLon = if (selectedCenterType == "home") client.homeLon else client.workLon

    // Filter masters by selected radius relative to current active center
    val mastersWithDistance = masters.map { master ->
        val dist = viewModel.calculateDistanceMeters(currentCenterLat, currentCenterLon, master.lat, master.lon)
        master to dist
    }
    
    val mastersInRadius = mastersWithDistance.filter { (_, dist) ->
        selectedRadiusMeters >= 1500 || dist <= selectedRadiusMeters
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("interactive_map_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            // Map Viewport with responsive height constrained between 260.dp and 380.dp based on aspect ratio
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 380.dp)
                    .aspectRatio(1.22f, matchHeightConstraintsFirst = false)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()
                val density = LocalDensity.current

                // Marker radii in pixels for pixel-perfect relative positioning
                val anchorRadiusPx = with(density) { 24.dp.toPx() }
                val markerRadiusPx = with(density) { 24.dp.toPx() }

                // Map spans in degrees based on zoom level
                val latSpan = 0.007f / zoomLevel
                val lonSpan = 0.009f / zoomLevel

                // Helper function to project lat/lon to X, Y pixel coordinates
                fun getPixels(lat: Double, lon: Double): Pair<Float, Float> {
                    val relativeLat = lat - currentCenterLat
                    val relativeLon = lon - currentCenterLon
                    
                    val fractX = 0.5f + (relativeLon / lonSpan).toFloat()
                    val fractY = 0.5f - (relativeLat / latSpan).toFloat() // Invert Y for screen
                    
                    return (fractX * widthPx) to (fractY * heightPx)
                }

                // Render stylized background grid & landmarks via Canvas!
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerPx = (widthPx / 2f) to (heightPx / 2f)

                    // 1. Draw water / beach at the bottom (Calella is seaside!)
                    val (_, seaTopY) = getPixels(41.6115, 2.6580)
                    val seaHeight = heightPx - seaTopY
                    if (seaHeight > 0 && seaTopY < heightPx) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF80DEEA), Color(0xFF00ACC1)) // Light blue to Deep teal
                            ),
                            topLeft = Offset(0f, seaTopY),
                            size = Size(widthPx, seaHeight)
                        )
                        // Beach sand strip
                        drawRect(
                            color = Color(0xFFFFE082), // Warm sand yellow
                            topLeft = Offset(0f, seaTopY - 15f),
                            size = Size(widthPx, 15f)
                        )
                    }

                    // 2. Draw Parc de Dalmau green area (top-left)
                    val (parkX, parkY) = getPixels(41.6175, 2.6585)
                    drawCircle(
                        color = Color(0xFFC8E6C9).copy(alpha = 0.7f), // Soft green
                        radius = 160f * zoomLevel,
                        center = Offset(parkX, parkY)
                    )

                    // 3. Radial distance circles centered on our active client anchor (Home or Work)
                    val radiiMeters = listOf(200, 500, 1000)
                    radiiMeters.forEach { r ->
                        val degOffset = r / 111320.0
                        val (_, pxY) = getPixels(currentCenterLat + degOffset, currentCenterLon)
                        val radPx = abs(pxY - centerPx.second)
                        
                        // Draw circle outline
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.6f),
                            radius = radPx,
                            center = Offset(centerPx.first, centerPx.second),
                            style = Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }

                    // 4. Draw standard local roads as light gray grid lines (Carrer de Jovara, N-II, etc.)
                    val (_, hwyY) = getPixels(41.6150, 2.6600)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, hwyY),
                        end = Offset(widthPx, hwyY),
                        strokeWidth = 12f * zoomLevel
                    )

                    val (_, esglY) = getPixels(41.6135, 2.6600)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, esglY),
                        end = Offset(widthPx, esglY),
                        strokeWidth = 8f * zoomLevel
                    )

                    val (_, jovY) = getPixels(41.6140, 2.6600)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, jovY),
                        end = Offset(widthPx, jovY),
                        strokeWidth = 8f * zoomLevel
                    )

                    val (vRoad1X, _) = getPixels(41.6140, 2.6610)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(vRoad1X, 0f),
                        end = Offset(vRoad1X, heightPx),
                        strokeWidth = 8f * zoomLevel
                    )

                    val (vRoad2X, _) = getPixels(41.6140, 2.6630)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(vRoad2X, 0f),
                        end = Offset(vRoad2X, heightPx),
                        strokeWidth = 8f * zoomLevel
                    )

                    // If a master is selected, draw a dashed pink path line from client to master!
                    selectedMapMaster?.let { m ->
                        val (mX, mY) = getPixels(m.lat, m.lon)
                        drawLine(
                            color = Color(0xFFE91E63), // Pink brand color
                            start = Offset(centerPx.first, centerPx.second),
                            end = Offset(mX, mY),
                            strokeWidth = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                }

                // 5. Draw concentric circles texts dynamically aligned to the exact circle boundary
                val rad200Px = abs(getPixels(currentCenterLat + 200.0 / 111320.0, currentCenterLon).second - (heightPx / 2f))
                val rad500Px = abs(getPixels(currentCenterLat + 500.0 / 111320.0, currentCenterLon).second - (heightPx / 2f))

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset { IntOffset(0, -rad200Px.roundToInt()) }
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("200м", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset { IntOffset(0, -rad500Px.roundToInt()) }
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("500м", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // 6. DRAW CLIENT ANCHORS (🏠 HOME and 💼 WORK) with relative IntOffset
                val (homeX, homeY) = getPixels(client.homeLat, client.homeLon)
                val (workX, workY) = getPixels(client.workLat, client.workLon)

                if (homeX in 0f..widthPx && homeY in 0f..heightPx) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (homeX - anchorRadiusPx).roundToInt(),
                                    (homeY - anchorRadiusPx).roundToInt()
                                )
                            }
                            .size(40.dp)
                            .background(
                                if (selectedCenterType == "home") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                CircleShape
                            )
                            .clickable { selectedCenterType = "home" },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(Color(0xFF2196F3), CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                if (workX in 0f..widthPx && workY in 0f..heightPx) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (workX - anchorRadiusPx).roundToInt(),
                                    (workY - anchorRadiusPx).roundToInt()
                                )
                            }
                            .size(40.dp)
                            .background(
                                if (selectedCenterType == "work") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                CircleShape
                            )
                            .clickable { selectedCenterType = "work" },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(Color(0xFF009688), CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Work, contentDescription = "Work", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // 7. DRAW MASTERS PIN OVERLAYS with relative IntOffset
                mastersInRadius.forEach { (master, dist) ->
                    val (mX, mY) = getPixels(master.lat, master.lon)
                    val isSelected = selectedMapMaster?.id == master.id
                    val isBookmarked = bookmarks.contains(master.id)

                    if (mX in 0f..widthPx && mY in 0f..heightPx) {
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (mX - markerRadiusPx).roundToInt(),
                                        (mY - markerRadiusPx).roundToInt()
                                    )
                                }
                                .size(48.dp)
                                .clickable { selectedMapMaster = master }
                                .testTag("map_marker_${master.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        CircleShape
                                    )
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = master.name.split(" ").firstOrNull()?.take(1) ?: "💅",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isBookmarked) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Bookmarked",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                }

                // 8. FLOATING CONTROLS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(2.dp)
                    ) {
                        TextButton(
                            onClick = { selectedCenterType = "home" },
                            modifier = Modifier.height(28.dp).testTag("map_center_home"),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (selectedCenterType == "home") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (selectedCenterType == "home") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🏠 Дом", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { selectedCenterType = "work" },
                            modifier = Modifier.height(28.dp).testTag("map_center_work"),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (selectedCenterType == "work") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (selectedCenterType == "work") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("💼 Работа", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(2.5f) },
                            modifier = Modifier.size(28.dp).testTag("map_zoom_in")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtMost(2.5f).coerceAtLeast(0.6f) },
                            modifier = Modifier.size(28.dp).testTag("map_zoom_out")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 9. RADIUS SELECTION
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Радиус:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf(300, 600, 1000, 1500).forEach { radius ->
                        val label = if (radius >= 1500) "Все" else "${radius}м"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedRadiusMeters == radius) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { selectedRadiusMeters = radius }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (selectedRadiusMeters == radius) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // 10. EMPTY STATE OVERLAY
                if (mastersInRadius.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LocationOff, contentDescription = "No masters in radius", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Мастера в этом радиусе не найдены", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Попробуйте увеличить радиус поиска", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Bottom detail overlay card
            AnimatedVisibility(
                visible = selectedMapMaster != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedMapMaster?.let { master ->
                    val distHome = viewModel.calculateDistanceMeters(client.homeLat, client.homeLon, master.lat, master.lon)
                    val distWork = viewModel.calculateDistanceMeters(client.workLat, client.workLon, master.lat, master.lon)
                    val isBookmarked = bookmarks.contains(master.id)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(16.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(master.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${master.rating} (${master.ratingCount})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Опыт: ${master.experienceYears} лет", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            IconButton(onClick = { viewModel.toggleBookmarkMaster(master.id) }) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Distance badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = "Home walk", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Дом: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${distHome}м", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = "Work walk", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Работа: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${distWork}м", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onViewMasterDetails(master) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Подробнее", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onBookMaster(master) },
                                modifier = Modifier.weight(1.2f).height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Book", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Записаться", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


// --------------------------------------------------------------------
// TAB 2: ONLINE BOOKING FLOW
// --------------------------------------------------------------------





