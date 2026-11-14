package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.InstagramPost
import com.example.ui.viewmodel.MainViewModel

/**
 * Portfolio / Gallery screen completely dedicated to full-space photo display.
 * Instagram parsing & synchronization is moved into its own dedicated modal window,
 * and photos expand into fullscreen upon tap, returning back to gallery on second tap.
 */
@Composable
fun PortfolioScreenTab(viewModel: MainViewModel) {
    val showLinks by viewModel.showOriginalInstagramLinks.collectAsStateWithLifecycle()
    val parsingLoading by viewModel.isInstagramParsing.collectAsStateWithLifecycle()
    val parsedPosts by viewModel.parsedInstagramPosts.collectAsStateWithLifecycle()
    val selectedDisplayPostIds by viewModel.selectedDisplayPostIds.collectAsStateWithLifecycle()
    val instagramUsername by viewModel.instagramUsername.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    // State for Instagram Import separate window
    var showImportDialog by remember { mutableStateOf(false) }

    // State for Fullscreen Photo Viewer (null = gallery view, non-null = fullscreen view)
    var fullScreenPost by remember { mutableStateOf<InstagramPost?>(null) }

    // Grid density: 2 columns for large detailed view, 3 columns for overview
    var gridColumns by remember { mutableIntStateOf(2) }

    // Mode: "grid" (classic compact gallery) vs "feed" (Instagram vertical stream)
    var isFeedMode by remember { mutableStateOf(true) }

    // Likes state tracking for local reactive feed interaction
    val likedPostIds = remember { mutableStateListOf<Int>() }

    // Dialog for adding a new manicure post/photo
    var showCreatePostDialog by remember { mutableStateOf(false) }

    val displayedPosts = remember(parsedPosts, selectedDisplayPostIds) {
        if (selectedDisplayPostIds.isEmpty()) {
            parsedPosts
        } else {
            parsedPosts.filter { it.id in selectedDisplayPostIds }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            // COMPACT TOP CONTROL BAR (Dedicated to Gallery, freeing all screen space for photos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and photo count badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Галерея",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            text = "${displayedPosts.size} работ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Action buttons: Add Post + View Mode (Feed vs Grid) + Instagram Import
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Create New Post button
                    IconButton(
                        onClick = { showCreatePostDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = "Добавить пост",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Feed Mode Toggle (Feed vs Grid)
                    IconButton(
                        onClick = { isFeedMode = !isFeedMode },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFeedMode) Icons.Default.GridView else Icons.Default.ViewAgenda,
                            contentDescription = "Режим отображения",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!isFeedMode) {
                        // Column toggle (2 columns vs 3 columns)
                        IconButton(
                            onClick = { gridColumns = if (gridColumns == 2) 3 else 2 },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("toggle_gallery_grid_btn")
                        ) {
                            Icon(
                                imageVector = if (gridColumns == 2) Icons.Default.ViewComfy else Icons.Default.GridView,
                                contentDescription = "Масштаб сетки",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Button opening the separate Instagram Import & Parsing window
                    FilledTonalButton(
                        onClick = { showImportDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("open_instagram_import_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Импорт",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Импорт",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // FULL-SPACE PHOTOS (Instagram Feed or Grid)
            if (displayedPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "В ленте пока нет публикаций",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showCreatePostDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Опубликовать первую работу")
                        }
                    }
                }
            } else if (isFeedMode) {
                // INSTAGRAM FEED MODE (Vertical Scroll, full-width photo, username, likes, comments)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("instagram_feed_list"),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayedPosts, key = { it.id }) { post ->
                        val isLiked = likedPostIds.contains(post.id)
                        val likes = post.likesCount + if (isLiked) 1 else 0

                        InstagramFeedPostCard(
                            post = post,
                            isLiked = isLiked,
                            likesCount = likes,
                            onToggleLike = {
                                if (likedPostIds.contains(post.id)) {
                                    likedPostIds.remove(post.id)
                                } else {
                                    likedPostIds.add(post.id)
                                }
                            },
                            onPhotoClick = { fullScreenPost = post }
                        )
                    }
                }
            } else {
                // CLASSIC GRID MODE
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("gallery_photo_grid"),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp, start = 2.dp, end = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedPosts, key = { it.id }) { post ->
                        GalleryPhotoItem(
                            post = post,
                            gridColumns = gridColumns,
                            onPhotoClick = {
                                // Tap on photo opens it in fullscreen!
                                fullScreenPost = post
                            }
                        )
                    }
                }
            }
        }

        // SEPARATE WINDOW: Instagram Import and Parsing Dialog
        if (showImportDialog) {
            InstagramImportDialog(
                viewModel = viewModel,
                instagramUsername = instagramUsername,
                showLinks = showLinks,
                parsingLoading = parsingLoading,
                postCount = parsedPosts.size,
                onDismiss = { showImportDialog = false }
            )
        }

        // CREATE NEW POST DIALOG
        if (showCreatePostDialog) {
            CreateNewManicurePostDialog(
                viewModel = viewModel,
                onDismiss = { showCreatePostDialog = false }
            )
        }

        // FULLSCREEN PHOTO VIEWER: Expands to fullscreen, returns on repeat click!
        fullScreenPost?.let { post ->
            val currentIndex = displayedPosts.indexOfFirst { it.id == post.id }
            FullscreenImageViewer(
                post = post,
                currentIndex = currentIndex,
                totalCount = displayedPosts.size,
                showLinks = showLinks,
                onClose = { fullScreenPost = null },
                onNavigate = { newIndex ->
                    if (newIndex in displayedPosts.indices) {
                        fullScreenPost = displayedPosts[newIndex]
                    }
                }
            )
        }
    }
}

/**
 * Individual Photo Item in the Gallery Grid.
 * Maximizes image space with subtle overlay for likes and smooth elevation.
 */
@Composable
private fun GalleryPhotoItem(
    post: InstagramPost,
    gridColumns: Int,
    onPhotoClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (gridColumns == 2) 0.92f else 1f) // Portrait ratio for manicure works
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onPhotoClick
            )
            .testTag("gallery_photo_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.caption.ifEmpty { "Фото маникюра" },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Subtle dark gradient at bottom for contrast
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )

            // Likes badge in bottom right corner
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${post.likesCount}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Author watermark in bottom left
            if (gridColumns == 2) {
                Text(
                    text = "@${post.username}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Authentic Instagram-styled feed card:
 * Author avatar, username, location, square photo, interactive like button, comments list and comment input!
 */
@Composable
private fun InstagramFeedPostCard(
    post: InstagramPost,
    isLiked: Boolean,
    likesCount: Int,
    onToggleLike: () -> Unit,
    onPhotoClick: () -> Unit
) {
    var isSaved by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    val postComments = remember(post.id) {
        mutableStateListOf(
            "Какая красота! 😍",
            "Идеальная кутикула и форма! 💅✨",
            "Хочу точно такой же френч на пятницу!"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("insta_feed_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Post Header (Author info)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.username.take(1).uppercase(),
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = post.username,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Студия маникюра NailiX • Calella",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Опции",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Post Photo (Square 1:1 Instagram standard)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onPhotoClick() }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = post.caption,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Action Icons Row: Like, Comment, Share, Bookmark
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleLike,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Лайк",
                            tint = if (isLiked) Color(0xFFFF3040) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { showCommentsSheet = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Комментарии",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Поделиться",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { isSaved = !isSaved },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Сохранить",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Likes count
            Text(
                text = "Нравится: $likesCount",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            // Caption
            if (post.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = post.username + " ",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // View comments button
            Text(
                text = "Посмотреть все комментарии (${postComments.size})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .clickable { showCommentsSheet = true }
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )

            Text(
                text = post.dateStr,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 14.dp, bottom = 12.dp)
            )
        }
    }

    if (showCommentsSheet) {
        var newCommentText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCommentsSheet = false },
            title = {
                Text("Комментарии (${postComments.size})", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(postComments) { comment ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = comment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Оставьте комментарий...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        trailingIcon = {
                            if (newCommentText.isNotBlank()) {
                                IconButton(onClick = {
                                    postComments.add(newCommentText.trim())
                                    newCommentText = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Отправить",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showCommentsSheet = false }) {
                    Text("Закрыть")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}


/**
 * Separate Window / Dialog for Instagram Parsing & Import.
 * Keeps the gallery entirely clean and uncluttered.
 */
@Composable
private fun InstagramImportDialog(
    viewModel: MainViewModel,
    instagramUsername: String,
    showLinks: Boolean,
    parsingLoading: Boolean,
    postCount: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("instagram_import_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Instagram",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Импорт Instagram",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Синхронизация портфолио",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_import_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Загрузите свежие работы, дизайны и отзывы прямо из Instagram студии или мастера.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Account input field
                OutlinedTextField(
                    value = instagramUsername,
                    onValueChange = { viewModel.updateInstagramUsername(it) },
                    label = { Text("Имя аккаунта Instagram") },
                    placeholder = { Text("например: victoria1001art") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (instagramUsername.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateInstagramUsername("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instagram_username_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Options card: toggle links to original
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ссылки на оригиналы",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Возможность перейти в пост Instagram",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showLinks,
                            onCheckedChange = { viewModel.toggleInstagramLinks(it) },
                            modifier = Modifier.testTag("toggle_instagram_links_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "В галерее сейчас:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$postCount постов",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons: Top-20 Popularity Import + Refresh Sync
                Button(
                    onClick = {
                        viewModel.authorizeAndImportInstagram(instagramUsername) {
                            Toast.makeText(context, "Топ-20 постов по популярности успешно импортированы! 🎉", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !parsingLoading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("parse_instagram_top20_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (parsingLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Импорт Топ-20...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⚡ Импортировать Топ-20 по популярности", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.triggerInstagramParsing()
                        Toast.makeText(context, "Запущена синхронизация с Instagram...", Toast.LENGTH_SHORT).show()
                    },
                    enabled = !parsingLoading && instagramUsername.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("parse_instagram_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Синхронизировать новые посты", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Готово")
                }
            }
        }
    }
}

/**
 * Fullscreen Image Viewer Modal.
 * Expands image to complete fullscreen on dark backdrop.
 * CRITICAL USER REQUIREMENT: Tapping on the photo immediately closes fullscreen
 * and returns back to the original gallery view ("при повторном обратно возвращаться в исходное").
 */
@Composable
private fun FullscreenImageViewer(
    post: InstagramPost,
    currentIndex: Int,
    totalCount: Int,
    showLinks: Boolean,
    onClose: () -> Unit,
    onNavigate: (Int) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    // Handle Android system back button
    BackHandler {
        onClose()
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                // Clicking anywhere on backdrop or photo returns back to the gallery
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
                .testTag("fullscreen_image_viewer_container")
        ) {
            // Main Photo centered, taking full screen space.
            // Clicking directly on the photo triggers onClose() returning back to gallery
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = post.caption.ifEmpty { "Полноэкранное фото" },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose // "при повторном обратно возвращаться в исходное"
                    )
                    .testTag("fullscreen_active_photo"),
                contentScale = ContentScale.Fit
            )

            // TOP OVERLAY: Close button & navigation hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back / Close icon
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("fullscreen_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть полноэкранный режим",
                        tint = Color.White
                    )
                }

                // Friendly hint banner explaining tap-to-return
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "Нажмите на фото, чтобы вернуться",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Counter e.g. "3 / 12"
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = if (totalCount > 0) "${currentIndex + 1}/$totalCount" else "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // PREVIOUS / NEXT ARROWS for convenient browsing
            if (currentIndex > 0) {
                IconButton(
                    onClick = { onNavigate(currentIndex - 1) },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .size(46.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("fullscreen_prev_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Предыдущее фото",
                        tint = Color.White
                    )
                }
            }

            if (currentIndex < totalCount - 1 && currentIndex >= 0) {
                IconButton(
                    onClick = { onNavigate(currentIndex + 1) },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(46.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("fullscreen_next_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Следующее фото",
                        tint = Color.White
                    )
                }
            }

            // BOTTOM OVERLAY: Author, Date, Likes and Caption
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f), Color.Black)
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE1306C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "@${post.username}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = post.dateStr,
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Likes badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${post.likesCount}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Link to original Instagram post if available and enabled
                        if (showLinks && post.originalUrl != null) {
                            Button(
                                onClick = { uriHandler.openUri(post.originalUrl) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                                modifier = Modifier.testTag("fullscreen_insta_link_btn")
                            ) {
                                Text("В Instagram 🔗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (post.caption.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = post.caption,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Dialog for creating and publishing a new manicure post to the studio feed.
 */
@Composable
private fun CreateNewManicurePostDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var caption by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("victoria_art") }
    var selectedPhotoUrl by remember {
        mutableStateOf("https://images.unsplash.com/photo-1632345031435-8727f6897d53?w=800")
    }

    val samplePhotos = listOf(
        "https://images.unsplash.com/photo-1632345031435-8727f6897d53?w=800",
        "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=800",
        "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=800",
        "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=800"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Новая публикация", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Выберите фото работы и добавьте описание к публикации:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Photo preview & selector
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(samplePhotos) { photoUrl ->
                        val isSelected = selectedPhotoUrl == photoUrl
                        Card(
                            modifier = Modifier
                                .size(72.dp)
                                .clickable { selectedPhotoUrl = photoUrl },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(if (isSelected) 3.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        ) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Имя автора / Instagram") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Описание (дизайн, форма, материалы)") },
                    placeholder = { Text("Аппаратный маникюр, нюдовая база, матовый топ 💅") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newPost = InstagramPost(
                        id = (System.currentTimeMillis() % 1000000).toInt(),
                        imageUrl = selectedPhotoUrl,
                        caption = caption.ifBlank { "Свежая работа в студии NailiX ✨" },
                        originalUrl = null,
                        likesCount = 1,
                        username = author.ifBlank { "nailix_studio" },
                        dateStr = "Сегодня"
                    )
                    viewModel.addNewPortfolioPost(newPost)
                    Toast.makeText(context, "Публикация успешно добавлена в ленту! 🎉", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Опубликовать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

