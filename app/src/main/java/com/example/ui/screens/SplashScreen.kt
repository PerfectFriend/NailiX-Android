package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.screens.Text

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val language by viewModel.onboardingLanguage.collectAsStateWithLifecycle()

    // Smooth fade-in and scale animations
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        startAnim = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = LinearOutSlowInEasing
        ),
        label = "AlphaAnimation"
    )

    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1.05f else 0.95f,
        animationSpec = tween(
            durationMillis = 1800,
            easing = FastOutSlowInEasing
        ),
        label = "ScaleAnimation"
    )

    // Dynamic localized strings for Splash screen based on determined system language
    val splashText = when (language) {
        "ru" -> "Определяем язык системы..."
        "es" -> "Detectando idioma del sistema..."
        "zh" -> "正在检测系统语言..."
        "hi" -> "सिस्टम भाषा का पता लगाया जा रहा है..."
        "ar" -> "جاري تحديد لغة النظام..."
        "pt" -> "Detectando idioma do sistema..."
        "fr" -> "Détection de la langue du système..."
        else -> "Detecting system language..."
    }

    val welcomeText = when (language) {
        "ru" -> "ДОБРО ПОЖАЛОВАТЬ"
        "es" -> "BIENVENIDO"
        "zh" -> "欢迎"
        "hi" -> "स्वागत है"
        "ar" -> "مرحباً بكم"
        "pt" -> "BEM-VINDO"
        "fr" -> "BIENVENUE"
        else -> "WELCOME"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F2C59)) // Deep, rich dark blue canvas
    ) {
        // Van Gogh masterpiece image representing manicure artwork
        Image(
            painter = painterResource(id = R.drawable.img_splash_manicure_1783727467218),
            contentDescription = "Van Gogh Manicure Art",
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .alpha(alpha * 0.85f),
            contentScale = ContentScale.Crop
        )

        // Gradient shader overlays for text readability and cinematic lighting contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.4f to Color(0x800F2C59),
                        1.0f to Color(0xE60F2C59)
                    )
                )
        )

        // Layout containing title and loading status
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stylized heading with new Gaudí N emblem
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Card(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(scale)
                        .alpha(alpha),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(2.dp, Color(0xFFF9C80E).copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_gaudi_n_1793575273048),
                        contentDescription = "NailiX Gaudí Icon",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "NAILIX",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 44.sp,
                        letterSpacing = 6.sp,
                        color = Color(0xFFF9C80E) // Golden Barcelona / Starry Night yellow
                    ),
                    modifier = Modifier.alpha(alpha)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = welcomeText,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.alpha(alpha * 0.9f)
                )
            }

            // Bottom section: glowing Park Güell turquoise spinner & language loader status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF00A896), // Antoni Gaudí Turquoise
                    strokeWidth = 4.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(alpha)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = splashText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.alpha(alpha)
                )
            }
        }
    }
}
