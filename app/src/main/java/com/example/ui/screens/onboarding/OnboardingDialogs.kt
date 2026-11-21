package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ai.AiImageUiState
import com.example.data.InstagramPost
import com.example.ui.viewmodel.MainViewModel

@Composable
internal fun AiAvatarGeneratorDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val aiAvatarState by viewModel.aiAvatarState.collectAsStateWithLifecycle()
    var prompt by remember { mutableStateOf("Luxury emerald and gold Gaudí mosaic manicure studio avatar") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .gaudiWaveBorder(alpha = 0.6f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "AI-аватар NailiX (Gemini)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Опишите желаемый аватар") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_avatar_prompt_input")
                )

                when (val state = aiAvatarState) {
                    is AiImageUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(8.dp))
                                Text("Генерация через Firebase AI Logic...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    is AiImageUiState.Success -> {
                        Image(
                            bitmap = state.bitmap.asImageBitmap(),
                            contentDescription = "Сгенерированный AI-аватар",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                        Text("Аватар сохранён и применён ✨", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                    is AiImageUiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    AiImageUiState.Idle -> Unit
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Закрыть")
                    }
                    Button(
                        onClick = { viewModel.generateAiAvatarWithGemini(prompt) },
                        enabled = aiAvatarState !is AiImageUiState.Loading && prompt.isNotBlank(),
                        modifier = Modifier
                            .weight(1.2f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("ai_avatar_generate_btn")
                    ) {
                        Text("Сгенерировать")
                    }
                }
            }
        }
    }
}

@Composable
internal fun OnboardingIgFeedDialog(
    viewModel: MainViewModel,
    igFeed: List<InstagramPost>,
    selectedIgPosts: Set<Int>,
    isConnectingIg: Boolean,
    instagramError: String?,
    onPickGalleryPhotos: () -> Unit,
    onApplySuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(6.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .gaudiWaveBorder(alpha = 0.8f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(4.dp, Color(0xFF833AB4))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔥 ТОП-20 ИЗ INSTAGRAM",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Black),
                            color = Color(0xFF833AB4)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Публичные посты по популярности или импорт из фотогалереи устройства.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3E8FF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF833AB4)) {
                        Text(
                            text = "Отмечено: ${selectedIgPosts.size} из 20",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.selectAllImportPosts() }) {
                            Text("⚡ Выбрать все", fontWeight = FontWeight.Bold, color = Color(0xFF833AB4), fontSize = 12.sp)
                        }
                        TextButton(onClick = { viewModel.deselectAllImportPosts() }) {
                            Text("Снять", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isConnectingIg -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF833AB4), strokeWidth = 4.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Загрузка публикаций профиля...", fontWeight = FontWeight.Bold, color = Color(0xFF833AB4))
                            }
                        }
                    }
                    !instagramError.isNullOrBlank() && igFeed.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = instagramError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = onPickGalleryPhotos,
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Выбрать фото из галереи (до 20)")
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(igFeed) { index, post ->
                                val isSelected = selectedIgPosts.contains(post.id)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleImportPostSelection(post.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFFF3E8FF) else Color(0xFFF8FAFC)
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF833AB4) else Color(0xFFCBD5E1)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(76.dp)) {
                                            AsyncImage(
                                                model = post.imageUrl,
                                                contentDescription = "Ig Post",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                                            )
                                            Surface(
                                                modifier = Modifier.align(Alignment.TopStart).padding(3.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (index) {
                                                    0 -> Color(0xFFFFD700)
                                                    1, 2 -> Color(0xFFFF6D00)
                                                    else -> Color.Black.copy(alpha = 0.7f)
                                                }
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 10.sp,
                                                    color = if (index == 0) Color.Black else Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "❤️ ${post.likesCount} лайков",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                color = Color(0xFFE1306C)
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = post.caption,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            post.originalUrl?.let { url ->
                                                Row(
                                                    modifier = Modifier.clickable { uriHandler.openUri(url) },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF833AB4), modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Ссылка на Instagram ↗", fontSize = 11.sp, color = Color(0xFF833AB4), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { viewModel.toggleImportPostSelection(post.id) },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF833AB4))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.completeInstagramImportAndLoad()
                        onApplySuccess()
                        Toast.makeText(context, "Импортировано ${selectedIgPosts.size} работ 🎉", Toast.LENGTH_SHORT).show()
                    },
                    enabled = selectedIgPosts.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF833AB4)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .gaudiOrnament(isButton = true, alphaMultiplier = 1.5f)
                        .testTag("ig_feed_load_btn")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ИМПОРТИРОВАТЬ ТОП (${selectedIgPosts.size})", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF64748B)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("ОТМЕНА", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
internal fun OnboardingPortfolioLimitDialog(
    viewModel: MainViewModel,
    existingPosts: List<InstagramPost>,
    selectedIgPostsCount: Int,
    limit: Int,
    onForceImport: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val neededSpace = existingPosts.size + selectedIgPostsCount

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .gaudiWaveBorder(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(4.dp, Color.Red)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color.Red, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("МЕСТА НЕ ХВАТАЕТ!", style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.Black), color = Color.Red)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Лимит бесплатной версии: $limit постов. Сейчас в портфолио: ${existingPosts.size}, добавляете: $selectedIgPostsCount.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F2C59)
                )
                Spacer(modifier = Modifier.height(14.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(existingPosts) { post ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = post.imageUrl,
                                    contentDescription = "Portfolio image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(post.caption, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                    Text("❤️ ${post.likesCount}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.deletePortfolioPost(post.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete post", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.completeInstagramImportAndLoad()
                        onForceImport()
                        Toast.makeText(context, "Импорт завершен! 🎉", Toast.LENGTH_SHORT).show()
                    },
                    enabled = neededSpace <= limit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, disabledContainerColor = Color.LightGray),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("limit_force_import_btn")
                ) {
                    Text(
                        text = if (neededSpace <= limit) "ЗАВЕРШИТЬ ИМПОРТ" else "МЕСТА НЕ ХВАТАЕТ ($neededSpace / $limit)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, Color(0xFF0F2C59)),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("ОТМЕНА", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F2C59))
                }
            }
        }
    }
}
