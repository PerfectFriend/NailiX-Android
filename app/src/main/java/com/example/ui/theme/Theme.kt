package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke


// Centralized Skin definition with custom Material 3 color schemes (Light & Dark variants)
val GaudiColorScheme = lightColorScheme(
    primary = Color(0xFF1D539F), // Cobalto Blue
    secondary = Color(0xFF00A896), // Park Güell Turquoise
    tertiary = Color(0xFFF9C80E), // Barcelona Yellow
    background = Color(0xFFEFF6FF), // Soft Sky Blue
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F2C59),
    onSurface = Color(0xFF0F2C59),
    outlineVariant = Color(0xFF93C5FD)
)

val GaudiDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA), // Soft Cobalto Blue
    secondary = Color(0xFF2DD4BF), // Park Güell Mint
    tertiary = Color(0xFFFBBF24), // Barcelona Amber
    background = Color(0xFF0B132B), // Deep Sagrada Night
    surface = Color(0xFF1C2541), // Glazed Ceramic Dark Blue
    onPrimary = Color(0xFF0B132B),
    onSecondary = Color(0xFF0B132B),
    onBackground = Color(0xFFEFF6FF),
    onSurface = Color(0xFFEFF6FF),
    outlineVariant = Color(0xFF3A506B)
)

val BarcelonaColorScheme = lightColorScheme(
    primary = Color(0xFFF7B538), // Golden Amber
    secondary = Color(0xFFF05D5E), // Terracotta Red
    tertiary = Color(0xFF0F2C59), // Deep Cobalto
    background = Color(0xFFFFFBEB), // Warm Sun Glow Cream
    surface = Color(0xFFFFF9E6),
    onPrimary = Color(0xFF332200),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF3D2A04),
    onSurface = Color(0xFF4D3300),
    outlineVariant = Color(0xFFFDE68A)
)

val BarcelonaDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFBBF24), // Sunlit Gold
    secondary = Color(0xFFF87171), // Soft Terracotta
    tertiary = Color(0xFF60A5FA), // Pale Cobalto
    background = Color(0xFF1C1105), // Deep Catalan Earth
    surface = Color(0xFF2D1E0F), // Glazed Clay
    onPrimary = Color(0xFF1C1105),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFEF3C7),
    onSurface = Color(0xFFFEF3C7),
    outlineVariant = Color(0xFF452C15)
)

val VanGoghLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706), // Wheatfield Amber
    secondary = Color(0xFF0284C7), // Sky Blue Brushstroke
    tertiary = Color(0xFF6366F1), // Irises Violet
    background = Color(0xFFF0F9FF), // Glazed Canvas Blue
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0C4A6E),
    onSurface = Color(0xFF0C4A6E),
    outlineVariant = Color(0xFFBAE6FD)
)

val VanGoghColorScheme = darkColorScheme(
    primary = Color(0xFFFCD34D), // Starry yellow
    secondary = Color(0xFF38BDF8), // Cypresses blue
    tertiary = Color(0xFF818CF8), // Astral indigo
    background = Color(0xFF0F172A), // Night sky black-blue
    surface = Color(0xFF1E293B),
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF1F5F9),
    outlineVariant = Color(0xFF334155)
)

val AmsterdamColorScheme = lightColorScheme(
    primary = Color(0xFFE11D48), // Brick red
    secondary = Color(0xFF059669), // Tulip Green
    tertiary = Color(0xFFD97706), // Canal bronze
    background = Color(0xFFFAFAF9), // Dutch tile white
    surface = Color(0xFFF5F5F4),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1917),
    onSurface = Color(0xFF292524),
    outlineVariant = Color(0xFFE7E5E4)
)

val AmsterdamDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFB7185), // Soft Brick Red
    secondary = Color(0xFF34D399), // Tulip Green Mint
    tertiary = Color(0xFFF59E0B), // Warm Bronze
    background = Color(0xFF1C1917), // Evening Canal Charcoal
    surface = Color(0xFF292524), // Dutch Brick Dark
    onPrimary = Color(0xFF1C1917),
    onSecondary = Color(0xFF1C1917),
    onBackground = Color(0xFFF5F5F4),
    onSurface = Color(0xFFF5F5F4),
    outlineVariant = Color(0xFF44403C)
)

val MoscowColorScheme = lightColorScheme(
    primary = Color(0xFFDC2626), // Ruby Red
    secondary = Color(0xFFD97706), // Gold Dome
    tertiary = Color(0xFF1E3A8A), // Moscow Navy
    background = Color(0xFFFEF2F2), // Pale snow-rose
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF450A0A),
    onSurface = Color(0xFF450A0A),
    outlineVariant = Color(0xFFFCA5A5)
)

val MoscowDarkColorScheme = darkColorScheme(
    primary = Color(0xFFEF4444), // Bright Ruby Red
    secondary = Color(0xFFF59E0B), // Golden Kremlin Dome
    tertiary = Color(0xFF3B82F6), // Kremlin Blue
    background = Color(0xFF2D0505), // Dark Snow Winter Red
    surface = Color(0xFF3D0A0A), // Red Square Stone Dark
    onPrimary = Color(0xFF2D0505),
    onSecondary = Color(0xFF2D0505),
    onBackground = Color(0xFFFEE2E2),
    onSurface = Color(0xFFFEE2E2),
    outlineVariant = Color(0xFF7F1D1D)
)

val ParisColorScheme = lightColorScheme(
    primary = Color(0xFFEC4899), // Rose Gold
    secondary = Color(0xFF8B5CF6), // Louvre Lavender
    tertiary = Color(0xFFD97706), // Antique Bronze
    background = Color(0xFFFAF5FF), // Seine Mist lavender
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF3B0764),
    onSurface = Color(0xFF3B0764),
    outlineVariant = Color(0xFFF3E8FF)
)

val ParisDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF472B6), // Soft Rose Gold
    secondary = Color(0xFFA78BFA), // Pastel Lavender
    tertiary = Color(0xFFF59E0B), // Warm Bronze
    background = Color(0xFF2E1065), // Midnight Seine Purple
    surface = Color(0xFF3B0764), // Louvre Amethyst
    onPrimary = Color(0xFF2E1065),
    onSecondary = Color(0xFF2E1065),
    onBackground = Color(0xFFF5F3FF),
    onSurface = Color(0xFFF5F3FF),
    outlineVariant = Color(0xFF5B21B6)
)

val NailsColorScheme = lightColorScheme(
    primary = Color(0xFFF43F5E), // Strawberry Gel
    secondary = Color(0xFFFDA4AF), // Powder Rose
    tertiary = Color(0xFFFB923C), // Peach Polish
    background = Color(0xFFFFF1F2), // Soft Manicure Pink
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF4C0519),
    onBackground = Color(0xFF4C0519),
    onSurface = Color(0xFF4C0519),
    outlineVariant = Color(0xFFFFE4E6)
)

val NailsDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFB7185), // Vibrant Rose Gel
    secondary = Color(0xFFF43F5E), // Bold Cherry
    tertiary = Color(0xFFFDBA74), // Apricot Sheen
    background = Color(0xFF4C0519), // Dark Velvet Burgundy
    surface = Color(0xFF5E0B25), // Glazed Cherry Shellac
    onPrimary = Color(0xFF4C0519),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFE4E6),
    onSurface = Color(0xFFFFE4E6),
    outlineVariant = Color(0xFF881337)
)

val FlowersColorScheme = lightColorScheme(
    primary = Color(0xFFDB2777), // Orchid Bloom
    secondary = Color(0xFF10B981), // Fresh Stem Green
    tertiary = Color(0xFFF59E0B), // Sunflower
    background = Color(0xFFFDF2F8), // Rose Water
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF500730),
    onSurface = Color(0xFF500730),
    outlineVariant = Color(0xFFFBCFE8)
)

val FlowersDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF472B6), // Soft Orchid
    secondary = Color(0xFF34D399), // Mint Green
    tertiary = Color(0xFFFBBF24), // Sunflower Yellow
    background = Color(0xFF500730), // Dark Hibiscus Violet
    surface = Color(0xFF62093C), // Deep Blossom
    onPrimary = Color(0xFF500730),
    onSecondary = Color(0xFF500730),
    onBackground = Color(0xFFFDF2F8),
    onSurface = Color(0xFFFDF2F8),
    outlineVariant = Color(0xFF831843)
)

val CosmosLightColorScheme = lightColorScheme(
    primary = Color(0xFF6D28D9), // Nebula Indigo
    secondary = Color(0xFF0891B2), // Sky Cyan
    tertiary = Color(0xFFDB2777), // Rose Pink
    background = Color(0xFFF5F3FF), // Soft Stardust Lavender
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1B4B),
    onSurface = Color(0xFF1E1B4B),
    outlineVariant = Color(0xFFDDD6FE)
)

val CosmosColorScheme = darkColorScheme(
    primary = Color(0xFF8B5CF6), // Orion Purple
    secondary = Color(0xFF06B6D4), // Cyan Nebula
    tertiary = Color(0xFFEC4899), // Supernova Pink
    background = Color(0xFF03001C), // Deep Vacuum Black
    surface = Color(0xFF0B0B26), // Cosmic Ship Purple
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF03001C),
    onBackground = Color(0xFFE0E7FF),
    onSurface = Color(0xFFE0E7FF),
    outlineVariant = Color(0xFF312E81)
)

val EsotericLightColorScheme = lightColorScheme(
    primary = Color(0xFFB45309), // Alchemical Bronze
    secondary = Color(0xFF6D28D9), // Mystical Amethyst
    tertiary = Color(0xFF047857), // Emerald Stone
    background = Color(0xFFFAF9F6), // Old Alchemical Parchment
    surface = Color(0xFFF4F1EA),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF451A03),
    onSurface = Color(0xFF451A03),
    outlineVariant = Color(0xFFE6DFD3)
)

val EsotericColorScheme = darkColorScheme(
    primary = Color(0xFFD4AF37), // Alchemical Gold
    secondary = Color(0xFFA78BFA), // Tarot Purple
    tertiary = Color(0xFF10B981), // Emerald Stone
    background = Color(0xFF18181B), // Charcoal Velvet
    surface = Color(0xFF27272A),
    onPrimary = Color(0xFF18181B),
    onSecondary = Color(0xFF18181B),
    onBackground = Color(0xFFF4F4F5),
    onSurface = Color(0xFFF4F4F5),
    outlineVariant = Color(0xFF3F3F46)
)

// Dynamic typography based on font-pairings for specific themes
fun getSkinTypography(fontFamily: FontFamily): Typography {
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 40.sp,
            lineHeight = 52.sp,
            letterSpacing = 2.sp
        )
    )
}

@Composable
fun MyApplicationTheme(
    skinCode: String = "gaudi",
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) {
        when (skinCode) {
            "gaudi" -> GaudiDarkColorScheme
            "barcelona" -> BarcelonaDarkColorScheme
            "van_gogh" -> VanGoghColorScheme
            "amsterdam" -> AmsterdamDarkColorScheme
            "moscow" -> MoscowDarkColorScheme
            "paris" -> ParisDarkColorScheme
            "nails" -> NailsDarkColorScheme
            "flowers" -> FlowersDarkColorScheme
            "cosmos" -> CosmosColorScheme
            "esoteric" -> EsotericColorScheme
            else -> GaudiDarkColorScheme
        }
    } else {
        when (skinCode) {
            "gaudi" -> GaudiColorScheme
            "barcelona" -> BarcelonaColorScheme
            "van_gogh" -> VanGoghLightColorScheme
            "amsterdam" -> AmsterdamColorScheme
            "moscow" -> MoscowColorScheme
            "paris" -> ParisColorScheme
            "nails" -> NailsColorScheme
            "flowers" -> FlowersColorScheme
            "cosmos" -> CosmosLightColorScheme
            "esoteric" -> EsotericLightColorScheme
            else -> GaudiColorScheme
        }
    }

    val fontFamily = when (skinCode) {
        "van_gogh", "moscow", "paris", "flowers", "esoteric" -> FontFamily.Serif
        "cosmos" -> FontFamily.Monospace
        else -> FontFamily.Default
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getSkinTypography(fontFamily),
        content = content
    )
}

// Reusable custom modifier that draws a beautiful, colorful skin pattern!
fun Modifier.skinOrnament(
    skinCode: String,
    isButton: Boolean = false,
    alphaMultiplier: Float = 1.0f,
    isDark: Boolean = false
): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height
    val seed = (width.toInt() * 1000) + height.toInt()
    val random = java.util.Random(seed.toLong())
    
    when (skinCode) {
        "gaudi" -> {
            val colors = if (isDark) {
                listOf(
                    Color(0xFF1E3A8A), // Deep dark blue
                    Color(0xFF0F766E), // Dark Teal
                    Color(0xFFB45309), // Dark Amber/Gold
                    Color(0xFF9F1239), // Dark Rose/Crimson
                    Color(0xFF166534), // Dark Green
                    Color(0xFF701A75), // Dark Violet
                    Color(0xFF1E1B4B)  // Very dark Indigo
                )
            } else {
                listOf(
                    Color(0xFF1D539F), // Cobalto Blue
                    Color(0xFF00A896), // Park Güell Turquoise
                    Color(0xFFF9C80E), // Sunny Barcelona Yellow
                    Color(0xFFF05D5E), // Warm Catalan Terracotta
                    Color(0xFF4CB944), // Organic Ceramic Green
                    Color(0xFFF7B538), // Amber Glaze
                    Color(0xFF9B5DE5), // Bright Violet Accent
                    Color(0xFFF15BB5)  // Hot Pink Rose
                )
            }
            // Beautiful underlying organic fluid gradient to guarantee zero solid backgrounds!
            val gradientColors = if (isDark) {
                listOf(
                    Color(0xFF0F172A).copy(alpha = 0.9f),
                    Color(0xFF1E3A8A).copy(alpha = 0.9f),
                    Color(0xFF0F1B29).copy(alpha = 0.9f)
                )
            } else if (isButton) {
                listOf(Color(0xFF1D539F).copy(alpha = 0.85f), Color(0xFF00A896).copy(alpha = 0.85f), Color(0xFFF7B538).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFEFF6FF).copy(alpha = 0.65f), Color(0xFFE0F2FE).copy(alpha = 0.75f), Color(0xFFFEF3C7).copy(alpha = 0.65f))
            }
            val brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradientColors)
            drawRect(brush = brush)

            val stepsX = if (isButton) 8 else 7
            val stepsY = if (isButton) 2 else 4
            val cellW = width / stepsX
            val cellH = height / stepsY
            for (i in 0 until stepsX) {
                for (j in 0 until stepsY) {
                    val tileColor = colors[random.nextInt(colors.size)]
                    val path = Path().apply {
                        val x0 = i * cellW + (random.nextFloat() * (cellW * 0.35f))
                        val y0 = j * cellH + (random.nextFloat() * (cellH * 0.35f))
                        val x1 = (i + 1) * cellW - (random.nextFloat() * (cellW * 0.35f))
                        val y1 = j * cellH + (random.nextFloat() * (cellH * 0.35f))
                        val x2 = (i + 1) * cellW - (random.nextFloat() * (cellW * 0.35f))
                        val y2 = (j + 1) * cellH - (random.nextFloat() * (cellH * 0.35f))
                        val x3 = i * cellW + (random.nextFloat() * (cellW * 0.35f))
                        val y3 = (j + 1) * cellH - (random.nextFloat() * (cellH * 0.35f))
                        moveTo(x0, y0)
                        lineTo(x1, y1)
                        lineTo(x2, y2)
                        lineTo(x3, y3)
                        close()
                    }
                    val baseAlpha = if (isDark) 0.35f else if (isButton) 0.68f else 0.45f
                    drawPath(path = path, color = tileColor.copy(alpha = baseAlpha * alphaMultiplier))
                    val outlineColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.8f)
                    drawPath(path = path, color = outlineColor, style = Stroke(width = if (isDark) 1.2f else 2.0f))
                }
            }
        }
        "van_gogh" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF0F172A).copy(alpha = 0.9f), Color(0xFF1E3A8A).copy(alpha = 0.9f))
            } else if (isButton) {
                listOf(Color(0xFF1E3A8A).copy(alpha = 0.85f), Color(0xFFF59E0B).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFEFF6FF).copy(alpha = 0.7f), Color(0xFFFEF3C7).copy(alpha = 0.7f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            val colors = listOf(Color(0xFFFCD34D), Color(0xFF38BDF8), Color(0xFF60A5FA), Color(0xFFF59E0B))
            for (i in 0 until 12) {
                val path = Path().apply {
                    val startX = random.nextFloat() * width
                    val startY = random.nextFloat() * height
                    moveTo(startX, startY)
                    cubicTo(
                        startX + (random.nextFloat() - 0.5f) * width * 0.6f,
                        startY + (random.nextFloat() - 0.5f) * height * 0.6f,
                        startX + (random.nextFloat() - 0.5f) * width * 0.6f,
                        startY + (random.nextFloat() - 0.5f) * height * 0.6f,
                        random.nextFloat() * width,
                        random.nextFloat() * height
                    )
                }
                drawPath(
                    path = path,
                    color = colors[random.nextInt(colors.size)].copy(alpha = 0.22f * alphaMultiplier),
                    style = Stroke(width = 8f + random.nextFloat() * 12f, join = StrokeJoin.Round)
                )
            }
        }
        "cosmos" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF020617).copy(alpha = 0.95f), Color(0xFF1E1B4B).copy(alpha = 0.95f))
            } else if (isButton) {
                listOf(Color(0xFF1E1B4B).copy(alpha = 0.85f), Color(0xFF06B6D4).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFF5F3FF).copy(alpha = 0.75f), Color(0xFFECFEFF).copy(alpha = 0.75f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            for (i in 0 until 18) {
                val cx = random.nextFloat() * width
                val cy = random.nextFloat() * height
                val sizeVal = 2f + random.nextFloat() * 4f
                val glowAlpha = 0.3f + random.nextFloat() * 0.6f
                drawCircle(
                    color = Color(0xFF06B6D4).copy(alpha = 0.4f * glowAlpha * alphaMultiplier),
                    radius = sizeVal * 2.5f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = glowAlpha * alphaMultiplier),
                    radius = sizeVal,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
            }
        }
        "esoteric" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF18181B).copy(alpha = 0.95f), Color(0xFF27272A).copy(alpha = 0.95f))
            } else if (isButton) {
                listOf(Color(0xFF1F2937).copy(alpha = 0.85f), Color(0xFFD4AF37).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFFAF9F6).copy(alpha = 0.75f), Color(0xFFF4F4F5).copy(alpha = 0.75f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            val color = Color(0xFFD4AF37)
            val cx = width / 2
            val cy = height / 2
            drawCircle(
                color = color.copy(alpha = 0.16f * alphaMultiplier),
                radius = Math.min(width, height) * 0.35f,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = color.copy(alpha = 0.10f * alphaMultiplier),
                radius = Math.min(width, height) * 0.22f,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                style = Stroke(width = 1.0f)
            )
            drawLine(
                color = color.copy(alpha = 0.12f * alphaMultiplier),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(width, height),
                strokeWidth = 1f
            )
            drawLine(
                color = color.copy(alpha = 0.12f * alphaMultiplier),
                start = androidx.compose.ui.geometry.Offset(width, 0f),
                end = androidx.compose.ui.geometry.Offset(0f, height),
                strokeWidth = 1f
            )
        }
        "flowers" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF450A0A).copy(alpha = 0.9f), Color(0xFF3B0764).copy(alpha = 0.9f))
            } else if (isButton) {
                listOf(Color(0xFFEC4899).copy(alpha = 0.85f), Color(0xFFFBBF24).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFFFF1F2).copy(alpha = 0.75f), Color(0xFFFEF3C7).copy(alpha = 0.75f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            val colors = listOf(Color(0xFFEC4899), Color(0xFFF472B6), Color(0xFF34D399), Color(0xFFFBBF24))
            for (i in 0 until 8) {
                val cx = random.nextFloat() * width
                val cy = random.nextFloat() * height
                val radius = 12f + random.nextFloat() * 20f
                for (petal in 0 until 5) {
                    val angle = (petal * 2 * Math.PI / 5).toFloat()
                    val px = cx + Math.cos(angle.toDouble()).toFloat() * radius * 0.8f
                    val py = cy + Math.sin(angle.toDouble()).toFloat() * radius * 0.8f
                    drawCircle(
                        color = colors[random.nextInt(colors.size)].copy(alpha = 0.18f * alphaMultiplier),
                        radius = radius * 0.6f,
                        center = androidx.compose.ui.geometry.Offset(px, py)
                    )
                }
                drawCircle(
                    color = Color(0xFFFCD34D).copy(alpha = 0.30f * alphaMultiplier),
                    radius = radius * 0.3f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
            }
        }
        "nails" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF4C0519).copy(alpha = 0.9f), Color(0xFF2E061B).copy(alpha = 0.9f))
            } else if (isButton) {
                listOf(Color(0xFFF43F5E).copy(alpha = 0.85f), Color(0xFFFB923C).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFFFF1F2).copy(alpha = 0.75f), Color(0xFFFFF7ED).copy(alpha = 0.75f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            val colors = listOf(Color(0xFFF43F5E), Color(0xFFFDA4AF), Color(0xFFFB923C))
            for (i in 0 until 14) {
                val cx = random.nextFloat() * width
                val cy = random.nextFloat() * height
                val radius = 8f + random.nextFloat() * 16f
                drawCircle(
                    color = colors[random.nextInt(colors.size)].copy(alpha = 0.20f * alphaMultiplier),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f * alphaMultiplier),
                    radius = radius * 0.5f,
                    center = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, cy - radius * 0.3f)
                )
            }
        }
        "barcelona" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF450A0A).copy(alpha = 0.9f), Color(0xFF1E1B4B).copy(alpha = 0.9f))
            } else if (isButton) {
                listOf(Color(0xFFF7B538).copy(alpha = 0.85f), Color(0xFFF05D5E).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFFEF2F2).copy(alpha = 0.75f), Color(0xFFFFFBEB).copy(alpha = 0.75f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            val color = Color(0xFFF7B538)
            val cx = width / 2
            val cy = 0f
            for (i in 0 until 16) {
                val angle = (i * Math.PI / 15).toFloat()
                val len = Math.max(width, height) * 1.5f
                drawLine(
                    color = color.copy(alpha = 0.12f * alphaMultiplier),
                    start = androidx.compose.ui.geometry.Offset(cx, cy),
                    end = androidx.compose.ui.geometry.Offset(cx + Math.cos(angle.toDouble()).toFloat() * len, cy + Math.sin(angle.toDouble()).toFloat() * len),
                    strokeWidth = 2f
                )
            }
        }
        "paris" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF3B0764).copy(alpha = 0.9f), Color(0xFF2E061B).copy(alpha = 0.9f))
            } else if (isButton) {
                listOf(Color(0xFFEC4899).copy(alpha = 0.85f), Color(0xFF8B5CF6).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFFDF2F8).copy(alpha = 0.75f), Color(0xFFF5F3FF).copy(alpha = 0.75f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            val color = Color(0xFFEC4899)
            drawRect(
                color = color.copy(alpha = 0.18f * alphaMultiplier),
                style = Stroke(width = 1.5f)
            )
            drawRect(
                color = color.copy(alpha = 0.10f * alphaMultiplier),
                style = Stroke(width = 0.5f),
                size = size * 0.95f,
                topLeft = androidx.compose.ui.geometry.Offset(width * 0.025f, height * 0.025f)
            )
        }
        "amsterdam" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF022C22).copy(alpha = 0.9f), Color(0xFF0F172A).copy(alpha = 0.9f))
            } else if (isButton) {
                listOf(Color(0xFF059669).copy(alpha = 0.85f), Color(0xFFF97316).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFECFDF5).copy(alpha = 0.75f), Color(0xFFFFF7ED).copy(alpha = 0.75f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            val color = Color(0xFF059669)
            for (i in 0 until 6) {
                val y = i * (height / 6)
                drawLine(
                    color = color.copy(alpha = 0.12f * alphaMultiplier),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1f
                )
            }
        }
        "moscow" -> {
            val gradColors = if (isDark) {
                listOf(Color(0xFF450A0A).copy(alpha = 0.9f), Color(0xFF1E293B).copy(alpha = 0.9f))
            } else if (isButton) {
                listOf(Color(0xFFD97706).copy(alpha = 0.85f), Color(0xFFDC2626).copy(alpha = 0.85f))
            } else {
                listOf(Color(0xFFFEF2F2).copy(alpha = 0.75f), Color(0xFFFFFBEB).copy(alpha = 0.75f))
            }
            drawRect(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = gradColors))

            val color = Color(0xFFD97706)
            for (i in 0 until 5) {
                val cx = random.nextFloat() * width
                val cy = random.nextFloat() * height
                val len = 15f + random.nextFloat() * 15f
                for (j in 0 until 8) {
                    val angle = (j * Math.PI / 4).toFloat()
                    drawLine(
                        color = color.copy(alpha = 0.16f * alphaMultiplier),
                        start = androidx.compose.ui.geometry.Offset(cx, cy),
                        end = androidx.compose.ui.geometry.Offset(cx + Math.cos(angle.toDouble()).toFloat() * len, cy + Math.sin(angle.toDouble()).toFloat() * len),
                        strokeWidth = 1.5f
                    )
                }
            }
        }
    }
}

// Reusable modifier that draws flowing organic borders for skins
fun Modifier.skinWaveBorder(
    skinCode: String,
    alpha: Float = 0.45f
): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height
    
    when (skinCode) {
        "gaudi", "barcelona" -> {
            val blue = if (skinCode == "gaudi") Color(0xFF1D539F) else Color(0xFFF7B538)
            val turquoise = if (skinCode == "gaudi") Color(0xFF00A896) else Color(0xFFF05D5E)
            val botPath = Path().apply {
                moveTo(0f, height - 12f)
                cubicTo(width * 0.25f, height - 32f, width * 0.5f, height + 8f, width * 0.75f, height - 22f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(path = botPath, color = turquoise.copy(alpha = alpha * 0.8f))
            
            val topPath = Path().apply {
                moveTo(0f, 12f)
                cubicTo(width * 0.2f, -8f, width * 0.55f, 28f, width * 0.8f, 2f)
                lineTo(width, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(path = topPath, color = blue.copy(alpha = alpha * 0.7f))
        }
        "van_gogh" -> {
            val color = Color(0xFFFCD34D)
            drawRect(
                color = color.copy(alpha = alpha * 0.7f),
                style = Stroke(width = 4f)
            )
        }
        "cosmos" -> {
            val cyan = Color(0xFF06B6D4)
            val purple = Color(0xFF8B5CF6)
            drawLine(
                color = cyan.copy(alpha = alpha * 0.9f),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(width, 0f),
                strokeWidth = 3f
            )
            drawLine(
                color = purple.copy(alpha = alpha * 0.9f),
                start = androidx.compose.ui.geometry.Offset(0f, height),
                end = androidx.compose.ui.geometry.Offset(width, height),
                strokeWidth = 3f
            )
        }
        "esoteric" -> {
            val gold = Color(0xFFD4AF37)
            drawRect(
                color = gold.copy(alpha = alpha * 0.7f),
                style = Stroke(width = 1f)
            )
            drawRect(
                color = gold.copy(alpha = alpha * 0.4f),
                style = Stroke(width = 0.5f),
                size = size * 0.97f,
                topLeft = androidx.compose.ui.geometry.Offset(width * 0.015f, height * 0.015f)
            )
        }
        "flowers", "nails", "paris" -> {
            val rose = Color(0xFFEC4899)
            drawRoundRect(
                color = rose.copy(alpha = alpha * 0.5f),
                style = Stroke(width = 1.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
        }
        else -> {
            drawRect(
                color = Color.LightGray.copy(alpha = alpha * 0.4f),
                style = Stroke(width = 1f)
            )
        }
    }
}


