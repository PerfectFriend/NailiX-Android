package com.example.ui.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.InstagramPost
import com.example.ui.viewmodel.MasterProfile

@Composable
fun StoriesGeneratorDialog(
    master: MasterProfile,
    portfolioPosts: List<InstagramPost>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedStyleIndex by remember { mutableIntStateOf(0) }
    var slotsText by remember { mutableStateOf("Пн: 11:00, 15:00 • Вт: 12:30, 17:00 • Ср: 10:00, 14:00") }
    var offerText by remember { mutableStateOf("Скидка -15% на первое посещение + SPA-уход в подарок! 🎁") }

    val styleGradients = listOf(
        // 0: Instagram Sunset Luxe
        listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045)),
        // 1: Emerald Velvet
        listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF10B981)),
        // 2: Cyber Purple & Neon Pink
        listOf(Color(0xFF3B0764), Color(0xFF701A75), Color(0xFFEC4899)),
        // 3: Champagne Gold & Luxury Dark
        listOf(Color(0xFF18181B), Color(0xFF27272A), Color(0xFFD97706))
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, Color(0xFFE1306C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(styleGradients[selectedStyleIndex])),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Генератор Stories (9:16)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Маркетинг свободных окошек", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Style Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Sunset", "Emerald", "Neon Pink", "Gold").forEachIndexed { idx, name ->
                            val isSelected = selectedStyleIndex == idx
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedStyleIndex = idx },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFE1306C) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(1.5.dp, Color.White) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 9:16 Story Canvas Preview Card with responsive aspect ratio
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.CenterHorizontally)
                            .aspectRatio(9f / 16f)
                            .heightIn(min = 280.dp, max = 390.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(styleGradients[selectedStyleIndex]))
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Top Branding
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.4f)
                                    ) {
                                        Text(
                                            text = "✨ NAILIX STUDIO",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "⭐ ${master.rating} (${master.ratingCount})",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Master Header
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.3f))
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = master.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = master.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "📍 ${master.address}",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Open Slots Box
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.Black.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "СВОБОДНЫЕ ОКОШКИ",
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = slotsText,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Top 3 Portfolio Mini Thumbnails
                                if (portfolioPosts.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        portfolioPosts.take(3).forEach { post ->
                                            Box(
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(1.5.dp, Color.White, RoundedCornerShape(10.dp))
                                            ) {
                                                AsyncImage(
                                                    model = post.imageUrl,
                                                    contentDescription = "Design",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Surface(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .fillMaxWidth(),
                                                    color = Color.Black.copy(alpha = 0.65f)
                                                ) {
                                                    Text(
                                                        text = "❤️ ${post.likesCount}",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Special Offer Banner
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.95f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color(0xFFE1306C), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = offerText,
                                            color = Color(0xFF1E293B),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // CTA Footer
                                Text(
                                    text = "📲 Для записи пишите в Direct или бронируйте через NailiX!",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Slot text editor
                    OutlinedTextField(
                        value = slotsText,
                        onValueChange = { slotsText = it },
                        label = { Text("Окошки для записи") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 2
                    )

                    // Offer text editor
                    OutlinedTextField(
                        value = offerText,
                        onValueChange = { offerText = it },
                        label = { Text("Акция или бонус") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Share & Copy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareBody = "💅 Свободные окошки у мастера ${master.name}!\n\n📅 Расписание:\n$slotsText\n\n🎁 $offerText\n\n📍 Адрес: ${master.address}\nЗапись онлайн через приложение NailiX!"
                            clipboardManager.setText(AnnotatedString(shareBody))
                            Toast.makeText(context, "Текст скопирован в буфер обмена! 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Копировать")
                    }

                    Button(
                        onClick = {
                            val shareBody = "💅 Свободные окошки у мастера ${master.name}!\n\n📅 Расписание:\n$slotsText\n\n🎁 $offerText\n\n📍 Адрес: ${master.address}\nЗапись через NailiX!"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Свободные окошки • NailiX")
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Опубликовать в Stories / Telegram"))
                        },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Поделиться в Stories", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
