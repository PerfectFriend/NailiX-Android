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
import com.example.ui.viewmodel.CustomAd
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawBehind
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder
import androidx.compose.ui.text.TextStyle
import com.example.ui.screens.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

@Composable
fun DesignsScreenTab(viewModel: MainViewModel) {
    val lengthFilter by viewModel.lengthFilter.collectAsStateWithLifecycle()
    val shapeFilter by viewModel.shapeFilter.collectAsStateWithLifecycle()
    val paletteFilter by viewModel.paletteFilter.collectAsStateWithLifecycle()
    val designs by viewModel.filteredDesigns.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteDesignIds.collectAsStateWithLifecycle()
    val masterNailArts by viewModel.masterNailArts.collectAsStateWithLifecycle()
    val globalCurrency by viewModel.globalCurrency.collectAsStateWithLifecycle()
    val localizationMap by viewModel.localizationMap.collectAsStateWithLifecycle()
    val localText = { key: String, default: String -> localizationMap[key] ?: default }

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Inspiration, 1: Master Catalog
    var selectedArtDetails by remember { mutableStateOf<com.example.data.MasterNailArtEntity?>(null) }
    var showPublishDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // TAB SELECTOR FOR SUB-SECTIONS
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text(localText("inspiration_ideas", "Идеи вдохновения"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text(localText("masters_catalog", "Каталог мастеров 👑"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium) }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (selectedSubTab == 0) {
            // ----------------------------------------------------
            // SUB-TAB 0: INSPIRATION IDEAS (EXISTING DESIGN LIST)
            // ----------------------------------------------------
            Column(modifier = Modifier.fillMaxWidth()) {
                // TOP HORIZONTAL FILTERS LIST
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(localText("inspiration_title", "Идеи для вдохновения:"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Length Filter Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(localText("length_label", "Длина: "), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            item {
                                FilterChip(
                                    selected = lengthFilter == null,
                                    onClick = { viewModel.lengthFilter.value = null },
                                    label = { Text(localText("all_filter", "Все")) }
                                )
                            }
                            items(listOf("Короткие", "Средние", "Длинные")) { len ->
                                val localizedLen = when (len) {
                                    "Короткие" -> localText("length_short", "Короткие")
                                    "Средние" -> localText("length_medium", "Средние")
                                    "Длинные" -> localText("length_long", "Длинные")
                                    else -> len
                                }
                                FilterChip(
                                    selected = lengthFilter == len,
                                    onClick = { viewModel.lengthFilter.value = len },
                                    label = { Text(localizedLen) },
                                    modifier = Modifier.testTag("filter_len_$len")
                                )
                            }
                        }
                    }

                    // Shape Filter Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(localText("shape_label", "Форма: "), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            item {
                                FilterChip(
                                    selected = shapeFilter == null,
                                    onClick = { viewModel.shapeFilter.value = null },
                                    label = { Text(localText("all_filter", "Все")) }
                                )
                            }
                            items(listOf("Овал", "Квадрат", "Миндаль", "Стилет")) { shp ->
                                val localizedShp = when (shp) {
                                    "Овал" -> localText("shape_oval", "Овал")
                                    "Квадрат" -> localText("shape_square", "Квадрат")
                                    "Миндаль" -> localText("shape_almond", "Миндаль")
                                    "Стилет" -> localText("shape_stiletto", "Стилет")
                                    else -> shp
                                }
                                FilterChip(
                                    selected = shapeFilter == shp,
                                    onClick = { viewModel.shapeFilter.value = shp },
                                    label = { Text(localizedShp) },
                                    modifier = Modifier.testTag("filter_shp_$shp")
                                )
                            }
                        }
                    }

                    // Palette Filter Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(localText("palette_label", "Палитра: "), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            item {
                                FilterChip(
                                    selected = paletteFilter == null,
                                    onClick = { viewModel.paletteFilter.value = null },
                                    label = { Text(localText("all_filter", "Все")) }
                                )
                            }
                            items(listOf("Нюдовый", "Яркий", "Темный", "Френч", "Глиттер")) { pal ->
                                val localizedPal = when (pal) {
                                    "Нюдовый" -> localText("pal_nude", "Нюдовый")
                                    "Яркий" -> localText("pal_bright", "Яркий")
                                    "Темный" -> localText("pal_dark", "Темный")
                                    "Френч" -> localText("pal_french", "Френч")
                                    "Глиттер" -> localText("pal_glitter", "Глиттер")
                                    else -> pal
                                }
                                FilterChip(
                                    selected = paletteFilter == pal,
                                    onClick = { viewModel.paletteFilter.value = pal },
                                    label = { Text(localizedPal) },
                                    modifier = Modifier.testTag("filter_pal_$pal")
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (designs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FilterListOff, contentDescription = "Empty filter", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(localText("no_designs_found", "Дизайны по вашим фильтрам не найдены"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(localText("reset_filters", "Попробуйте сбросить фильтры"), color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                                viewModel.lengthFilter.value = null
                                viewModel.shapeFilter.value = null
                                viewModel.paletteFilter.value = null
                            })
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(designs) { design ->
                            val isFav = favorites.contains(design.id)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                                    AsyncImage(
                                        model = design.imgUrl,
                                        contentDescription = design.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.toggleFavoriteDesign(design.id) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                                            .size(32.dp)
                                            .testTag("favorite_design_${design.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Save design",
                                            tint = if (isFav) Color.Red else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${design.length} • ${design.shape}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = design.title,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = design.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ----------------------------------------------------
            // SUB-TAB 1: MASTERS' ART CATALOG (RICH COMMUNITY DB)
            // ----------------------------------------------------
            Box(modifier = Modifier.fillMaxSize()) {
                if (masterNailArts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(localText("loading_catalog", "Загрузка каталога мастеров..."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(masterNailArts) { art ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.viewMasterNailArt(art.id)
                                        selectedArtDetails = art
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                                    AsyncImage(
                                        model = art.imageUrl,
                                        contentDescription = art.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Complexity tag
                                    val complexityColor = when (art.complexity.lowercase()) {
                                        "easy", "простой" -> Color(0xFF4CAF50)
                                        "medium", "средний" -> Color(0xFFFFC107)
                                        "hard", "сложный" -> Color(0xFFFF5722)
                                        else -> Color(0xFF9C27B0)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .background(complexityColor.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = art.complexity,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Extra surcharge tag
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        val symbol = if (globalCurrency == "RUB") "₽" else if (globalCurrency == "EUR") "€" else "$"
                                        val rate = if (globalCurrency == "RUB") 90.0 else if (globalCurrency == "EUR") 0.92 else 1.0
                                        val formattedVal = (art.extraPrice * rate).toInt()
                                        Text(
                                            text = "+$formattedVal $symbol",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = art.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = art.masterName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Icon(Icons.Default.Favorite, contentDescription = "Likes", tint = Color.Red, modifier = Modifier.size(12.dp))
                                                Text(art.likes.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Icon(Icons.Default.Visibility, contentDescription = "Views", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                                Text(art.views.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Text(
                                            text = localText("more_details", "Детали"),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // FAB to publish art
                ExtendedFloatingActionButton(
                    onClick = { showPublishDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("publish_art_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(localText("publish_art_btn", "Опубликовать")) }
                )
            }
        }
    }

    // DETAIL DIALOG FOR MASTER ART
    selectedArtDetails?.let { art ->
        Dialog(onDismissRequest = { selectedArtDetails = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).heightIn(max = 240.dp)) {
                        AsyncImage(
                            model = art.imageUrl,
                            contentDescription = art.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedArtDetails = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(art.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val symbol = if (globalCurrency == "RUB") "₽" else if (globalCurrency == "EUR") "€" else "$"
                            val rate = if (globalCurrency == "RUB") 90.0 else if (globalCurrency == "EUR") 0.92 else 1.0
                            val priceVal = (art.extraPrice * rate).toInt()
                            Text("+$priceVal $symbol", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("${localText("master", "Мастер")}: ${art.masterName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(art.complexity, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }

                        Text(art.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.clickable { viewModel.likeMasterNailArt(art.id) }
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    Text((art.likes + 1).toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) // instant UI feedback
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Views", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    Text(art.views.toString(), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.selectedNailArtForBooking.value = art
                                selectedArtDetails = null
                                // Navigate the user to the booking flow
                                // We can show a toast or a visual indicator
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(localText("book_with_this_art", "Выбрать арт для записи 💅"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // PUBLISH ART DIALOG FORM
    if (showPublishDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newMaster by remember { mutableStateOf("") }
        var newComplexity by remember { mutableStateOf("Medium") }
        var newExtraPrice by remember { mutableStateOf("15.0") }
        var newDesc by remember { mutableStateOf("") }
        var selectedImgPreset by remember { mutableStateOf("https://images.unsplash.com/photo-1604654894610-df63bc536371?q=80&w=600&auto=format&fit=crop") }

        val imagePresets = listOf(
            "Cosmic" to "https://images.unsplash.com/photo-1604654894610-df63bc536371?q=80&w=600&auto=format&fit=crop",
            "Baroque" to "https://images.unsplash.com/photo-1632345031435-8797b2d58045?q=80&w=600&auto=format&fit=crop",
            "Sakura" to "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?q=80&w=600&auto=format&fit=crop",
            "Minimal" to "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?q=80&w=600&auto=format&fit=crop",
            "Mirror" to "https://images.unsplash.com/photo-1607606325015-acf121387212?q=80&w=600&auto=format&fit=crop"
        )

        Dialog(onDismissRequest = { showPublishDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(localText("publish_art_title", "Опубликовать работу ногтевого арта"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    item {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text(localText("art_title_label", "Название дизайна")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newMaster,
                            onValueChange = { newMaster = it },
                            label = { Text(localText("master_name_label", "Имя мастера")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(localText("complexity_label", "Сложность исполнения:"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Easy", "Medium", "Hard", "Masterpiece").forEach { comp ->
                                val localizedComp = when (comp) {
                                    "Easy" -> localText("complexity_easy", "Простой")
                                    "Medium" -> localText("complexity_medium", "Средний")
                                    "Hard" -> localText("complexity_hard", "Сложный")
                                    "Masterpiece" -> localText("complexity_masterpiece", "Шедевр")
                                    else -> comp
                                }
                                val isSelected = newComplexity == comp
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { newComplexity = comp },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Box(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(localizedComp, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = newExtraPrice,
                            onValueChange = { newExtraPrice = it },
                            label = { Text(localText("price_surcharge_label", "Стоимость арта (USD)")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(localText("select_preset_image", "Выберите стиль изображения:"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            items(imagePresets) { (name, url) ->
                                val isSelected = selectedImgPreset == url
                                Card(
                                    modifier = Modifier
                                        .clickable { selectedImgPreset = url }
                                        .width(70.dp),
                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.height(45.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                                        Text(name, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            label = { Text(localText("desc_materials_label", "Материалы и описание техники")) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showPublishDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(localText("cancel_btn", "Отмена"))
                            }
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank() && newMaster.isNotBlank()) {
                                        val price = newExtraPrice.toDoubleOrNull() ?: 15.0
                                        viewModel.publishMasterNailArt(
                                            title = newTitle,
                                            masterName = newMaster,
                                            complexity = newComplexity,
                                            extraPrice = price,
                                            imageUrl = selectedImgPreset,
                                            description = newDesc
                                        )
                                        showPublishDialog = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(localText("publish_btn", "Опубликовать"))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------
// TAB 4: LOYALTY CARD, BONUS SYSTEM, REFERRAL INTERFACES
