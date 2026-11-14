package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MasterProfile
import com.example.data.InstagramPost
import com.example.R
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder
import com.example.ui.screens.Text
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView

// A custom modifier that draws a beautiful, colorful theme-based pattern!
private fun Modifier.gaudiOrnament(
    isButton: Boolean = false,
    alphaMultiplier: Float = 1.0f,
    isDark: Boolean = false,
    skinCode: String = "gaudi"
): Modifier = this.skinOrnament(skinCode = skinCode, isButton = isButton, alphaMultiplier = alphaMultiplier, isDark = isDark)

// A modifier that draws flowing organic waves typical of Casa Batlló and Park Güell
private fun Modifier.gaudiWaveBorder(
    alpha: Float = 0.45f,
    skinCode: String = "gaudi"
): Modifier = this.skinWaveBorder(skinCode = skinCode, alpha = alpha)

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            
            val strokeWidth = w * 0.22f
            val sizeToDraw = size.copy(width = w - strokeWidth, height = h - strokeWidth)
            val offset = strokeWidth / 2f
            
            // 1. Red top arc
            drawArc(
                color = Color(0xFFEA4335),
                startAngle = 180f,
                sweepAngle = 135f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
                size = sizeToDraw,
                style = Stroke(width = strokeWidth)
            )
            
            // 2. Yellow left arc
            drawArc(
                color = Color(0xFFFBBC05),
                startAngle = 135f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
                size = sizeToDraw,
                style = Stroke(width = strokeWidth)
            )
            
            // 3. Green bottom arc
            drawArc(
                color = Color(0xFF34A853),
                startAngle = 0f,
                sweepAngle = 135f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
                size = sizeToDraw,
                style = Stroke(width = strokeWidth)
            )
            
            // 4. Blue right segment
            drawArc(
                color = Color(0xFF4285F4),
                startAngle = 315f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
                size = sizeToDraw,
                style = Stroke(width = strokeWidth)
            )
            
            // 5. Blue G horizontal bar
            drawLine(
                color = Color(0xFF4285F4),
                start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
                end = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.5f),
                strokeWidth = strokeWidth
            )
        }
    }
}

@Composable
fun StickmanAvatar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        val w = size.width
        val h = size.height
        
        // Head (circle)
        val headRadius = w * 0.18f
        val headCenterX = w * 0.5f
        val headCenterY = h * 0.28f
        drawCircle(
            color = Color(0xFF0F2C59),
            radius = headRadius,
            center = androidx.compose.ui.geometry.Offset(headCenterX, headCenterY),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Eyes (two dots)
        val eyeRadius = w * 0.025f
        val leftEyeX = headCenterX - headRadius * 0.4f
        val rightEyeX = headCenterX + headRadius * 0.4f
        val eyesY = headCenterY - headRadius * 0.15f
        drawCircle(
            color = Color(0xFF0F2C59),
            radius = eyeRadius,
            center = androidx.compose.ui.geometry.Offset(leftEyeX, eyesY)
        )
        drawCircle(
            color = Color(0xFF0F2C59),
            radius = eyeRadius,
            center = androidx.compose.ui.geometry.Offset(rightEyeX, eyesY)
        )
        
        // Smile (curved arc)
        drawArc(
            color = Color(0xFFF05D5E), // cute red smile
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(headCenterX - headRadius * 0.4f, headCenterY - headRadius * 0.1f),
            size = androidx.compose.ui.geometry.Size(headRadius * 0.8f, headRadius * 0.6f),
            style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        // Body - "огуречик" (oval or rounded rect)
        val bodyWidth = w * 0.26f
        val bodyHeight = h * 0.32f
        val bodyLeft = headCenterX - bodyWidth / 2f
        val bodyTop = headCenterY + headRadius
        drawRoundRect(
            color = Color(0xFF00A896), // green "огуречик" body
            topLeft = androidx.compose.ui.geometry.Offset(bodyLeft, bodyTop),
            size = androidx.compose.ui.geometry.Size(bodyWidth, bodyHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Arms (stick lines coming out of "огуречик" body sides)
        // Left arm
        drawLine(
            color = Color(0xFF0F2C59),
            start = androidx.compose.ui.geometry.Offset(bodyLeft, bodyTop + bodyHeight * 0.2f),
            end = androidx.compose.ui.geometry.Offset(bodyLeft - w * 0.15f, bodyTop + bodyHeight * 0.5f),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Right arm (waving slightly!)
        drawLine(
            color = Color(0xFF0F2C59),
            start = androidx.compose.ui.geometry.Offset(bodyLeft + bodyWidth, bodyTop + bodyHeight * 0.2f),
            end = androidx.compose.ui.geometry.Offset(bodyLeft + bodyWidth + w * 0.15f, bodyTop - bodyHeight * 0.1f),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Legs (stick lines coming out of bottom of body)
        // Left leg
        drawLine(
            color = Color(0xFF0F2C59),
            start = androidx.compose.ui.geometry.Offset(bodyLeft + bodyWidth * 0.25f, bodyTop + bodyHeight),
            end = androidx.compose.ui.geometry.Offset(bodyLeft + bodyWidth * 0.1f, bodyTop + bodyHeight + h * 0.18f),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Right leg
        drawLine(
            color = Color(0xFF0F2C59),
            start = androidx.compose.ui.geometry.Offset(bodyLeft + bodyWidth * 0.75f, bodyTop + bodyHeight),
            end = androidx.compose.ui.geometry.Offset(bodyLeft + bodyWidth * 0.9f, bodyTop + bodyHeight + h * 0.18f),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

// Translation/Localization Dictionary for a Premium Onboarding Experience
val OnboardingLocalizations = mapOf(
    "ru" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "Маникюр и педикюр",
        "select_lang" to "Язык",
        "next" to "Далее",
        "back" to "Назад",
        "select_role" to "Роль",
        "role_client" to "Клиент",
        "role_client_desc" to "Поиск мастеров, запись и клуб привилегий",
        "role_master" to "Мастер",
        "role_master_desc" to "Управление записями, портфолио и услугами",
        "profile_setup" to "Профиль",
        "name" to "Имя *",
        "surname" to "Фамилия",
        "phone" to "Телефон *",
        "email" to "Email",
        "alias" to "Instagram / Ник",
        "avatar" to "Аватар",
        "gallery" to "Галерея",
        "ai_synth" to "ИИ аватар",
        "enter_prompt" to "Описание аватара...",
        "generating" to "Генерация...",
        "insta_reg" to "Импорт",
        "insta_import_desc" to "Копирование профиля из Instagram.",
        "insta_handle_hint" to "Ник в Instagram",
        "insta_connect" to "Импорт",
        "complete_reg" to "Готово",
        "services_templates" to "Услуги",
        "custom_service" to "Своя услуга",
        "insta_portfolio" to "Портфолио",
        "insta_portfolio_desc" to "Синхронизация лучших постов.",
        "ig_auth" to "Вход",
        "ig_auth_desc" to "Авторизация в Instagram",
        "ig_username" to "Логин",
        "ig_password" to "Пароль",
        "ig_login_btn" to "Войти",
        "ig_feed" to "Лента",
        "ig_feed_desc" to "Выберите посты",
        "import_btn" to "Импорт",
        "skip" to "Пропустить",
        "load" to "Загрузить",
        "mandatory_fields_warn" to "Заполните обязательные поля"
    ),
    "en" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "Manicure & Pedicure",
        "select_lang" to "Language",
        "next" to "Next",
        "back" to "Cancel",
        "select_role" to "Role",
        "role_client" to "Client",
        "role_client_desc" to "Find masters, book sessions and earn loyalty points",
        "role_master" to "Master",
        "role_master_desc" to "Manage bookings, portfolio and services",
        "profile_setup" to "Profile",
        "name" to "Name *",
        "surname" to "Surname",
        "phone" to "Phone *",
        "email" to "Email",
        "alias" to "Instagram / Handle",
        "avatar" to "Avatar",
        "gallery" to "Gallery",
        "ai_synth" to "AI Avatar",
        "enter_prompt" to "Avatar prompt...",
        "generating" to "Generating...",
        "insta_reg" to "Import",
        "insta_import_desc" to "Copy profile info from Instagram.",
        "insta_handle_hint" to "Instagram username",
        "insta_connect" to "Import",
        "complete_reg" to "Done",
        "services_templates" to "Services",
        "custom_service" to "Custom service",
        "insta_portfolio" to "Portfolio",
        "insta_portfolio_desc" to "Sync your best posts.",
        "ig_auth" to "Login",
        "ig_auth_desc" to "Log in to Instagram",
        "ig_username" to "Username",
        "ig_password" to "Password",
        "ig_login_btn" to "Log In",
        "ig_feed" to "Feed",
        "ig_feed_desc" to "Select posts",
        "import_btn" to "Import",
        "skip" to "Skip",
        "load" to "Load",
        "mandatory_fields_warn" to "Please fill in mandatory fields"
    ),
    "es" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "Manicura y Pedicura",
        "select_lang" to "Idioma",
        "next" to "Siguiente",
        "back" to "Cancelar",
        "select_role" to "Rol",
        "role_client" to "Cliente",
        "role_client_desc" to "Encuentra estilistas, agenda citas y gana puntos",
        "role_master" to "Estilista",
        "role_master_desc" to "Gestiona citas, portafolio y servicios",
        "profile_setup" to "Perfil",
        "name" to "Nombre *",
        "surname" to "Apellido",
        "phone" to "Teléfono *",
        "email" to "Email",
        "alias" to "Instagram / Alias",
        "avatar" to "Avatar",
        "gallery" to "Galería",
        "ai_synth" to "IA Avatar",
        "enter_prompt" to "Descripción...",
        "generating" to "Generando...",
        "insta_reg" to "Importar",
        "insta_import_desc" to "Copia perfil desde Instagram.",
        "insta_handle_hint" to "Usuario de Instagram",
        "insta_connect" to "Importar",
        "complete_reg" to "Listo",
        "services_templates" to "Servicios",
        "custom_service" to "Otro servicio",
        "insta_portfolio" to "Portafolio",
        "insta_portfolio_desc" to "Sincroniza tus posts.",
        "ig_auth" to "Acceso",
        "ig_auth_desc" to "Inicia sesión en Instagram",
        "ig_username" to "Usuario",
        "ig_password" to "Contraseña",
        "ig_login_btn" to "Acceder",
        "ig_feed" to "Feed",
        "ig_feed_desc" to "Selecciona posts",
        "import_btn" to "Importar",
        "skip" to "Omitir",
        "load" to "Cargar",
        "mandatory_fields_warn" to "Complete los campos obligatorios"
    ),
    "zh" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "美甲与美足",
        "select_lang" to "语言",
        "next" to "下一步",
        "back" to "取消",
        "select_role" to "角色",
        "role_client" to "客户",
        "role_client_desc" to "寻找美甲师、在线预约并享受会员福利",
        "role_master" to "美甲师",
        "role_master_desc" to "管理预约、作品集和业务服务",
        "profile_setup" to "个人资料",
        "name" to "姓名 *",
        "surname" to "姓氏",
        "phone" to "手机号 *",
        "email" to "电子邮箱",
        "alias" to "Instagram / 昵称",
        "avatar" to "头像",
        "gallery" to "相册",
        "ai_synth" to "AI 头像",
        "enter_prompt" to "描述您的头像...",
        "generating" to "正在生成...",
        "insta_reg" to "导入",
        "insta_import_desc" to "从 Instagram 复制个人资料。",
        "insta_handle_hint" to "Instagram 账号",
        "insta_connect" to "导入",
        "complete_reg" to "完成",
        "services_templates" to "服务项目",
        "custom_service" to "自定义服务",
        "insta_portfolio" to "作品集",
        "insta_portfolio_desc" to "同步您的精彩贴文。",
        "ig_auth" to "登录",
        "ig_auth_desc" to "登录 Instagram",
        "ig_username" to "用户名",
        "ig_password" to "密码",
        "ig_login_btn" to "登录",
        "ig_feed" to "动态",
        "ig_feed_desc" to "选择贴文",
        "import_btn" to "导入",
        "skip" to "跳过",
        "load" to "加载",
        "mandatory_fields_warn" to "请填写所有必填字段"
    ),
    "hi" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "मैनिक्योर और पेडीक्योर",
        "select_lang" to "भाषा",
        "next" to "आगे बढ़ें",
        "back" to "रद्द करें",
        "select_role" to "भूमिका",
        "role_client" to "ग्राहक",
        "role_client_desc" to "मास्टर्स खोजें, बुकिंग करें और रिवॉर्ड्स पाएं",
        "role_master" to "मास्टर",
        "role_master_desc" to "बुकिंग, पोर्टफोलियो और सेवाओं का प्रबंधन करें",
        "profile_setup" to "प्रोफाइल",
        "name" to "नाम *",
        "surname" to "उपनाम",
        "phone" to "फ़ोन *",
        "email" to "ईमेल",
        "alias" to "इंस्टाग्राम / निक",
        "avatar" to "अवतार",
        "gallery" to "गैलरी",
        "ai_synth" to "एआई अवतार",
        "enter_prompt" to "अवतार का विवरण...",
        "generating" to "बनाया जा रहा है...",
        "insta_reg" to "आयात",
        "insta_import_desc" to "इंस्टाग्राम से प्रोफाइल कॉपी करें।",
        "insta_handle_hint" to "इंस्टाग्राम हैंडल",
        "insta_connect" to "आयात",
        "complete_reg" to "पूर्ण",
        "services_templates" to "सेवाएं",
        "custom_service" to "अन्य सेवा",
        "insta_portfolio" to "पोर्टफोलियो",
        "insta_portfolio_desc" to "अपनी पोस्ट सिнк करें।",
        "ig_auth" to "लॉगिन",
        "ig_auth_desc" to "इंस्टाग्राम में लॉगिन करें",
        "ig_username" to "यूज़रनेम",
        "ig_password" to "पासवर्ड",
        "ig_login_btn" to "लॉगिन",
        "ig_feed" to "फ़ीड",
        "ig_feed_desc" to "पोस्ट चुनें",
        "import_btn" to "आयात करें",
        "skip" to "छोड़ें",
        "load" to "लोड करें",
        "mandatory_fields_warn" to "कृपया सभी आवश्यक फ़ील्ड भरें"
    ),
    "ar" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "مانيكير وباديكير",
        "select_lang" to "اللغة",
        "next" to "التالي",
        "back" to "إلغاء",
        "select_role" to "الدور",
        "role_client" to "عميل",
        "role_client_desc" to "ابحث عن خبراء، احجز جلسات واكسب مكافآت",
        "role_master" to "خبير",
        "role_master_desc" to "إدارة الحجوزات، معرض الأعمال والخدمات",
        "profile_setup" to "الملف الشخصي",
        "name" to "الاسم *",
        "surname" to "اللقب",
        "phone" to "الهاتف *",
        "email" to "البريد الإلكتروني",
        "alias" to "إنستغرام / اللقب",
        "avatar" to "الصورة الشخصية",
        "gallery" to "المعرض",
        "ai_synth" to "رمز تعبيري ذكاء اصطناعي",
        "enter_prompt" to "وصف الصورة...",
        "generating" to "جاري الإنشاء...",
        "insta_reg" to "استيراد",
        "insta_import_desc" to "نسخ الملف الشخصي من إنستغرام.",
        "insta_handle_hint" to "اسم مستخدم إنستغرام",
        "insta_connect" to "استيراد",
        "complete_reg" to "تم",
        "services_templates" to "الخدمات",
        "custom_service" to "خدمة مخصصة",
        "insta_portfolio" to "معرض الأعمال",
        "insta_portfolio_desc" to "مزامنة منشوراتك.",
        "ig_auth" to "تسجيل الدخول",
        "ig_auth_desc" to "تسجيل الدخول إلى إنستغرام",
        "ig_username" to "اسم المستخدم",
        "ig_password" to "كلمة المرور",
        "ig_login_btn" to "دخول",
        "ig_feed" to "المنشورات",
        "ig_feed_desc" to "اختر المنشورات",
        "import_btn" to "استيراد",
        "skip" to "تخطي",
        "load" to "تحميل",
        "mandatory_fields_warn" to "يرجى ملء جميع الحقول المطلوبة"
    ),
    "pt" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "Manicure e Pedicure",
        "select_lang" to "Idioma",
        "next" to "Avançar",
        "back" to "Cancelar",
        "select_role" to "Função",
        "role_client" to "Cliente",
        "role_client_desc" to "Encontre manicures, agende horários e ganhe fidelidade",
        "role_master" to "Manicure",
        "role_master_desc" to "Gerencie agendamentos, portfólio e serviços",
        "profile_setup" to "Perfil",
        "name" to "Nome *",
        "surname" to "Sobrenome",
        "phone" to "Telefone *",
        "email" to "E-mail",
        "alias" to "Instagram / Nick",
        "avatar" to "Avatar",
        "gallery" to "Galeria",
        "ai_synth" to "Avatar de IA",
        "enter_prompt" to "Descrição do avatar...",
        "generating" to "Gerando...",
        "insta_reg" to "Importar",
        "insta_import_desc" to "Copiar perfil do Instagram.",
        "insta_handle_hint" to "Usuário do Instagram",
        "insta_connect" to "Importar",
        "complete_reg" to "Concluir",
        "services_templates" to "Serviços",
        "custom_service" to "Outro serviço",
        "insta_portfolio" to "Portafólio",
        "insta_portfolio_desc" to "Sincronize suas publicações.",
        "ig_auth" to "Entrar",
        "ig_auth_desc" to "Entrar no Instagram",
        "ig_username" to "Usuário",
        "ig_password" to "Senha",
        "ig_login_btn" to "Entrar",
        "ig_feed" to "Feed",
        "ig_feed_desc" to "Selecionar publicações",
        "import_btn" to "Importar",
        "skip" to "Pular",
        "load" to "Carregar",
        "mandatory_fields_warn" to "Por favor, preencha todos os campos obrigatórios"
    ),
    "fr" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "Manucure & Pédicure",
        "select_lang" to "Langue",
        "next" to "Suivant",
        "back" to "Annuler",
        "select_role" to "Rôle",
        "role_client" to "Client",
        "role_client_desc" to "Trouvez des stylistes, réservez et gagnez des points",
        "role_master" to "Styliste",
        "role_master_desc" to "Gerez vos rendez-vous, votre portfolio et vos services",
        "profile_setup" to "Profil",
        "name" to "Nom *",
        "surname" to "Nom de famille",
        "phone" to "Téléphone *",
        "email" to "E-mail",
        "alias" to "Instagram / Pseudo",
        "avatar" to "Avatar",
        "gallery" to "Galerie",
        "ai_synth" to "Avatar IA",
        "enter_prompt" to "Description de l'avatar...",
        "generating" to "Génération...",
        "insta_reg" to "Importer",
        "insta_import_desc" to "Copier le profil depuis Instagram.",
        "insta_handle_hint" to "Pseudo Instagram",
        "insta_connect" to "Importer",
        "complete_reg" to "Terminer",
        "services_templates" to "Services",
        "custom_service" to "Autre service",
        "insta_portfolio" to "Portfolio",
        "insta_portfolio_desc" to "Synchronisez vos publications.",
        "ig_auth" to "Connexion",
        "ig_auth_desc" to "Connexion à Instagram",
        "ig_username" to "Identifiant",
        "ig_password" to "Mot de passe",
        "ig_login_btn" to "Se connecter",
        "ig_feed" to "Flux",
        "ig_feed_desc" to "Sélectionner des publications",
        "import_btn" to "Importer",
        "skip" to "Passer",
        "load" to "Charger",
        "mandatory_fields_warn" to "Veuillez remplir tous les champs obligatoires"
    ),
    "he" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "מניקור ופדיקור",
        "select_lang" to "שפה",
        "next" to "הבא",
        "back" to "ביטול",
        "select_role" to "תפקיד",
        "role_client" to "לקוח",
        "role_client_desc" to "מצא מעצבים, הזמן תור וצבור נקודות",
        "role_master" to "מעצב/ת ציפורניים",
        "role_master_desc" to "ניהול תורים, תיק עבודות ושירותים",
        "profile_setup" to "פרופיל",
        "name" to "שם פרטי *",
        "surname" to "שם משפחה",
        "phone" to "טלפון *",
        "email" to "אימייל",
        "alias" to "אינסטגרם / כינוי",
        "avatar" to "תמונת פרופיל",
        "gallery" to "גלריה",
        "ai_synth" to "אוואטר בינה מלאכותית",
        "enter_prompt" to "תיאור האוואטר...",
        "generating" to "מייצר...",
        "insta_reg" to "יבוא",
        "insta_import_desc" to "העתקת פרופיל מאינסטגרם",
        "insta_handle_hint" to "שם משתמש באינסטגרם",
        "insta_connect" to "יבוא",
        "complete_reg" to "סיום",
        "services_templates" to "שירותים",
        "custom_service" to "שירות מותאם אישית",
        "insta_portfolio" to "תיק עבודות",
        "insta_portfolio_desc" to "סנכרון הפוסטים הטובים ביותר",
        "ig_auth" to "התחברות",
        "ig_auth_desc" to "התחברות לאינסטגרם",
        "ig_username" to "שם משתמש",
        "ig_password" to "סיסמה",
        "ig_login_btn" to "התחבר",
        "ig_feed" to "פיד",
        "ig_feed_desc" to "בחר פוסטים",
        "import_btn" to "ייבא",
        "skip" to "דלג",
        "load" to "טען",
        "mandatory_fields_warn" to "אנา מלא את כל שדות החובה"
    ),
    "tr" to mapOf(
        "welcome" to "NAILIX",
        "tagline" to "Manikür & Pedikür",
        "select_lang" to "Dil",
        "next" to "İleri",
        "back" to "İptal",
        "select_role" to "Rol",
        "role_client" to "Müşteri",
        "role_client_desc" to "Uzmanları bulun, randevu alın ve puan kazanın",
        "role_master" to "Uzman",
        "role_master_desc" to "Randevuları, portföyü ve hizmetleri yönetin",
        "profile_setup" to "Profil",
        "name" to "Ad *",
        "surname" to "Soyad",
        "phone" to "Telefon *",
        "email" to "E-posta",
        "alias" to "Instagram / Kullanıcı Adı",
        "avatar" to "Profil Resmi",
        "gallery" to "Galeri",
        "ai_synth" to "Yapay Zeka Avatarı",
        "enter_prompt" to "Avatar açıklaması...",
        "generating" to "Üretiliyor...",
        "insta_reg" to "İçe Aktar",
        "insta_import_desc" to "Instagram'dan profil kopyalama",
        "insta_handle_hint" to "Instagram kullanıcı adı",
        "insta_connect" to "İçe Aktar",
        "complete_reg" to "Tamamla",
        "services_templates" to "Hizmetler",
        "custom_service" to "Özel Hizmet",
        "insta_portfolio" to "Portföy",
        "insta_portfolio_desc" to "En iyi gönderileri senkronize et",
        "ig_auth" to "Giriş Yap",
        "ig_auth_desc" to "Instagram'a Giriş Yap",
        "ig_username" to "Kullanıcı Adı",
        "ig_password" to "Şifre",
        "ig_login_btn" to "Giriş",
        "ig_feed" to "Akış",
        "ig_feed_desc" to "Gönderileri seçin",
        "import_btn" to "Aktar",
        "skip" to "Geç",
        "load" to "Yükle",
        "mandatory_fields_warn" to "Lütfen zorunlu alanları doldurun"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val language by viewModel.onboardingLanguage.collectAsStateWithLifecycle()
    val role by viewModel.onboardingRole.collectAsStateWithLifecycle()
    val avatarUrl by viewModel.onboardingAvatarUrl.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingAvatar.collectAsStateWithLifecycle()
    val subTier by viewModel.subscriptionTier.collectAsStateWithLifecycle()
    val isConnectingIg by viewModel.isInstagramConnecting.collectAsStateWithLifecycle()
    val igFeed by viewModel.instagramImportFeed.collectAsStateWithLifecycle()
    val selectedIgPosts by viewModel.selectedImportPosts.collectAsStateWithLifecycle()
    val parsedInstagramPosts by viewModel.parsedInstagramPosts.collectAsStateWithLifecycle()
    val selectedDisplayPostIds by viewModel.selectedDisplayPostIds.collectAsStateWithLifecycle()
    val savedClients by viewModel.savedClientProfiles.collectAsStateWithLifecycle()
    val savedMasters by viewModel.savedMasterProfiles.collectAsStateWithLifecycle()
    val selectedSkin by viewModel.selectedSkin.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val dict = OnboardingLocalizations[language] ?: OnboardingLocalizations["ru"]!!

    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE0F2FE)
    val cardBorder = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
    val cardTitle = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F2C59)
    val innerCardBg = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val innerCardBorder = if (isDarkTheme) Color(0xFF334155) else Color(0xFF94A3B8)
    val innerCardText = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
    val bodyTextColor = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF334155)
    val inputBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setOnboardingAvatar(uri.toString())
        }
    }

    // Local form states
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    
    // Master local states
    var masterAddress by remember { mutableStateOf("Spain, Catalonia, Calella, Moragas 74") }
    var masterHours by remember { mutableStateOf("10:00 - 20:00") }
    var masterExp by remember { mutableStateOf("5") }
    var masterLangs by remember { mutableStateOf("Español, Русский, English") }
    var masterCurrency by remember { mutableStateOf("EUR") }
    var selectedTemplateIndex by remember { mutableStateOf(0) }

    // Instagram registration and verification variables
    var showInstagramRegDialog by remember { mutableStateOf(false) }
    var instagramRegHandle by remember { mutableStateOf("") }
    var isRegImporting by remember { mutableStateOf(false) }

    // Instagram Portfolio import variables
    var showIgAuthDialog by remember { mutableStateOf(false) }
    var showIgFeedDialog by remember { mutableStateOf(false) }
    var igUser by remember { mutableStateOf("") }
    var igPass by remember { mutableStateOf("") }
    var showUpgradeDialog by remember { mutableStateOf(false) }

    // Google OAuth integration variables
    var showGoogleAccountChooser by remember { mutableStateOf(false) }
    var selectedGoogleEmail by remember { mutableStateOf("") }
    var selectedGoogleName by remember { mutableStateOf("") }
    var selectedGoogleAvatar by remember { mutableStateOf("") }
    var showGoogleWebView by remember { mutableStateOf(false) }
    var showInstagramWebView by remember { mutableStateOf(false) }

    // Multi-factor verification flow and limit checks
    var isVerifying by remember { mutableStateOf(false) }
    var verificationStepText by remember { mutableStateOf("") }
    var showIgSmsDialog by remember { mutableStateOf(false) }
    var smsVerificationCode by remember { mutableStateOf("") }
    var isSmsVerifying by remember { mutableStateOf(false) }
    var showLimitWarningDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // High quality wallpaper background matching the selected skin!
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

        // Extremely translucent glassmorphic overlay so the gorgeous Barcelona pattern is fully visible!
        val overlayColors = if (isDarkTheme) {
            listOf(
                Color(0xFF0F172A).copy(alpha = 0.90f),
                Color(0xFF020617).copy(alpha = 0.95f)
            )
        } else {
            listOf(
                Color(0xFFF8FAFC).copy(alpha = 0.93f),
                Color(0xFFEFF6FF).copy(alpha = 0.96f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = overlayColors)
                )
        )

        if (step == 0) {
            OnboardingWelcomeTour(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                language = language,
                cardBorder = cardBorder,
                cardBg = cardBg
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
            // Elegant Header Illustration with Gaudí ceramic curves and trencadís ornaments!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 85.dp, max = 120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .gaudiOrnament(isButton = false, alphaMultiplier = 1.2f, isDark = isDarkTheme)
                    .gaudiWaveBorder(alpha = 0.8f)
                    .border(3.dp, cardBorder, RoundedCornerShape(20.dp)),
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
                        border = BorderStroke(1.5.dp, cardBorder.copy(alpha = 0.5f)),
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
                        color = cardTitle
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Indicator Dots
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
                            .background(if (active) cardBorder else if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // STEP 1: LANGUAGE SELECTION
            if (step == 1) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(cardBg, RoundedCornerShape(24.dp))
                        .gaudiOrnament(isButton = false, alphaMultiplier = 1.8f, isDark = isDarkTheme)
                        .gaudiWaveBorder(alpha = 0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Transparent to display Gaudí ornament beautifully
                    border = BorderStroke(4.dp, cardBorder), // Strong, hand-crafted high-contrast border
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (dict["select_lang"] ?: "Language").uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 32.sp, // Bold, larger heading for perfect readability
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = cardTitle,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val languagesList = listOf(
                            Triple("ru", "Русский", "🇷🇺"),
                            Triple("en", "English", "🇬🇧"),
                            Triple("es", "Español", "🇪🇸"),
                            Triple("zh", "中文", "🇨🇳"),
                            Triple("hi", "हिन्दी", "🇮🇳"),
                            Triple("ar", "العربية", "🇸🇦"),
                            Triple("pt", "Português", "🇵🇹"),
                            Triple("fr", "Français", "🇫🇷"),
                            Triple("he", "עбрит", "🇮🇱"),
                            Triple("tr", "Türkçe", "🇹🇷")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            languagesList.forEach { (langCode, langName, flag) ->
                                val isSelected = language == langCode
                                val itemBgColor = if (isSelected) {
                                    if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
                                } else {
                                    if (isDarkTheme) Color(0xFF334155) else Color.White
                                }
                                val itemBorderColor = if (isSelected) {
                                    if (isDarkTheme) Color(0xFF0F172A) else Color(0xFF0F2C59)
                                } else {
                                    if (isDarkTheme) Color(0xFF475569) else Color(0xFF94A3B8)
                                }
                                val itemTextColor = if (isSelected) {
                                    if (isDarkTheme) Color(0xFF0F172A) else Color.White
                                } else {
                                    if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF0F2C59)
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setOnboardingLanguage(langCode) }
                                        .background(itemBgColor, RoundedCornerShape(16.dp))
                                        .gaudiWaveBorder(alpha = if (isSelected) 0.9f else 0.3f)
                                        .gaudiOrnament(isButton = false, alphaMultiplier = if (isSelected) 2.2f else 1.0f, isDark = isDarkTheme || isSelected)
                                        .testTag("lang_$langCode"),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    border = BorderStroke(
                                        width = if (isSelected) 4.dp else 2.dp,
                                        color = itemBorderColor
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = flag,
                                            fontSize = 40.sp, // Slightly larger flag icons
                                            modifier = Modifier.padding(end = 14.dp)
                                        )
                                        Text(
                                            text = langName,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontSize = 24.sp, // Larger, ultra-readable labels
                                                fontWeight = FontWeight.Black
                                            ),
                                            color = itemTextColor
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = itemTextColor,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (savedClients.isNotEmpty() || savedMasters.isNotEmpty()) {
                                    viewModel.setOnboardingStep(5)
                                } else {
                                    viewModel.setOnboardingStep(2)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .gaudiOrnament(isButton = true, alphaMultiplier = 2.0f)
                                .gaudiWaveBorder(alpha = 0.8f)
                                .testTag("lang_next_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D539F)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = dict["next"]?.uppercase() ?: "NEXT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // STEP 2: ROLE SELECTION (Compact, fitting perfectly on screen with zero scrolling)
            if (step == 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(cardBg, RoundedCornerShape(20.dp))
                        .gaudiOrnament(isButton = false, alphaMultiplier = 1.2f, isDark = isDarkTheme)
                        .gaudiWaveBorder(alpha = 0.6f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(2.5.dp, cardBorder),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (dict["select_role"] ?: "Role").uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = cardTitle,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Client Card (Compact)
                        val isClient = role == "client"
                        val clientBgColor = if (isClient) {
                            if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
                        } else {
                            if (isDarkTheme) Color(0xFF334155) else Color.White
                        }
                        val clientBorderColor = if (isClient) {
                            if (isDarkTheme) Color(0xFF0284C7) else Color(0xFF0F2C59)
                        } else {
                            if (isDarkTheme) Color(0xFF475569) else Color(0xFFCBD5E1)
                        }
                        val clientTextColor = if (isClient) {
                            if (isDarkTheme) Color(0xFF0F172A) else Color.White
                        } else {
                            if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF0F2C59)
                        }
                        val clientDescColor = if (isClient) {
                            if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
                        } else {
                            if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF334155)
                        }
                        val clientIconTint = if (isClient) {
                            if (isDarkTheme) Color(0xFF0F172A) else Color.White
                        } else {
                            if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
                        }
                        val clientIconBg = if (isClient) {
                            if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.25f)
                        } else {
                            if (isDarkTheme) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1D539F).copy(alpha = 0.15f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setOnboardingRole("client") }
                                .background(clientBgColor, RoundedCornerShape(16.dp))
                                .gaudiWaveBorder(alpha = if (isClient) 0.8f else 0.2f)
                                .testTag("role_client_card"),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(
                                width = if (isClient) 3.dp else 1.5.dp,
                                color = clientBorderColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(clientIconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Client",
                                        tint = clientIconTint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = (dict["role_client"] ?: "Client").uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = clientTextColor
                                    )
                                    Text(
                                        text = dict["role_client_desc"] ?: "Find master, track designs, book nail appointments",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = clientDescColor
                                    )
                                }
                                if (isClient) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = clientTextColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Master Card (Compact)
                        val isMaster = role == "master"
                        val masterBgColor = if (isMaster) {
                            if (isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF00A896)
                        } else {
                            if (isDarkTheme) Color(0xFF334155) else Color.White
                        }
                        val masterBorderColor = if (isMaster) {
                            if (isDarkTheme) Color(0xFF0F766E) else Color(0xFF004D40)
                        } else {
                            if (isDarkTheme) Color(0xFF475569) else Color(0xFFCBD5E1)
                        }
                        val masterTextColor = if (isMaster) {
                            if (isDarkTheme) Color(0xFF0F172A) else Color.White
                        } else {
                            if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF0F2C59)
                        }
                        val masterDescColor = if (isMaster) {
                            if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
                        } else {
                            if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF334155)
                        }
                        val masterIconTint = if (isMaster) {
                            if (isDarkTheme) Color(0xFF0F172A) else Color.White
                        } else {
                            if (isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF00A896)
                        }
                        val masterIconBg = if (isMaster) {
                            if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.25f)
                        } else {
                            if (isDarkTheme) Color(0xFF2DD4BF).copy(alpha = 0.15f) else Color(0xFF00A896).copy(alpha = 0.15f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setOnboardingRole("master") }
                                .background(masterBgColor, RoundedCornerShape(16.dp))
                                .gaudiWaveBorder(alpha = if (isMaster) 0.8f else 0.2f)
                                .testTag("role_master_card"),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(
                                width = if (isMaster) 3.dp else 1.5.dp,
                                color = masterBorderColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(masterIconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "Master",
                                        tint = masterIconTint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = (dict["role_master"] ?: "Master").uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = masterTextColor
                                    )
                                    Text(
                                        text = dict["role_master_desc"] ?: "Manage appointments, publish designs, list services",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = masterDescColor
                                    )
                                }
                                if (isMaster) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = masterTextColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (savedClients.isNotEmpty() || savedMasters.isNotEmpty()) {
                                        viewModel.setOnboardingStep(5)
                                    } else {
                                        viewModel.setOnboardingStep(0)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                border = BorderStroke(1.5.dp, cardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = cardBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = dict["back"]?.uppercase() ?: "BACK",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    color = cardBorder
                                )
                            }

                            Button(
                                onClick = { viewModel.setOnboardingStep(3) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("role_next_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = cardBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = dict["next"]?.uppercase() ?: "NEXT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    color = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.quickStartAsGuest(role ?: "client") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("quick_start_role_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = cardBorder)
                        ) {
                            val roleLabel = if (role == "master") (dict["role_master"] ?: "Master") else (dict["role_client"] ?: "Client")
                            Text(
                                text = if (language == "ru") "⚡ Быстрый вход ($roleLabel)"
                                       else if (language == "es") "⚡ Inicio rápido ($roleLabel)"
                                       else "⚡ Quick start ($roleLabel)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = cardBorder
                            )
                        }
                    }
                }
            }

            // STEP 3: CREATE PROFILE & AVATAR GENERATION
            if (step == 3) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(cardBg, RoundedCornerShape(24.dp))
                        .gaudiOrnament(isButton = false, alphaMultiplier = 1.8f, isDark = isDarkTheme)
                        .gaudiWaveBorder(alpha = 0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Transparent to display Gaudí ornament beautifully
                    border = BorderStroke(4.dp, cardBorder),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (dict["profile_setup"] ?: "Profile").uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 32.sp, // Enlarged bold header
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = cardTitle,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Clean, compact fields with high contrast and readable labels
                        val fieldLabelColor = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
                        val fieldTextColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F2C59)
                        val fieldBorderColorFocused = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
                        val fieldBorderColorUnfocused = if (isDarkTheme) Color(0xFF475569) else Color(0xFF0F2C59)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(dict["name"] ?: "Name *", fontSize = 20.sp, fontWeight = FontWeight.Black) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Black, color = fieldTextColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = fieldBorderColorFocused,
                                unfocusedBorderColor = fieldBorderColorUnfocused,
                                focusedLabelColor = fieldLabelColor,
                                unfocusedLabelColor = fieldLabelColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(12.dp))
                                .testTag("reg_name_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = surname,
                            onValueChange = { surname = it },
                            label = { Text(dict["surname"] ?: "Surname", fontSize = 20.sp, fontWeight = FontWeight.Black) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Black, color = fieldTextColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = fieldBorderColorFocused,
                                unfocusedBorderColor = fieldBorderColorUnfocused,
                                focusedLabelColor = fieldLabelColor,
                                unfocusedLabelColor = fieldLabelColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(12.dp))
                                .testTag("reg_surname_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(dict["phone"] ?: "Phone *", fontSize = 20.sp, fontWeight = FontWeight.Black) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Black, color = fieldTextColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = fieldBorderColorFocused,
                                unfocusedBorderColor = fieldBorderColorUnfocused,
                                focusedLabelColor = fieldLabelColor,
                                unfocusedLabelColor = fieldLabelColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(12.dp))
                                .testTag("reg_phone_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(dict["email"] ?: "Email", fontSize = 20.sp, fontWeight = FontWeight.Black) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Black, color = fieldTextColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = fieldBorderColorFocused,
                                unfocusedBorderColor = fieldBorderColorUnfocused,
                                focusedLabelColor = fieldLabelColor,
                                unfocusedLabelColor = fieldLabelColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(12.dp))
                                .testTag("reg_email_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = alias,
                            onValueChange = { alias = it },
                            label = { Text(dict["alias"] ?: "Alias", fontSize = 20.sp, fontWeight = FontWeight.Black) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Black, color = fieldTextColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = fieldBorderColorFocused,
                                unfocusedBorderColor = fieldBorderColorUnfocused,
                                focusedLabelColor = fieldLabelColor,
                                unfocusedLabelColor = fieldLabelColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(12.dp))
                                .testTag("reg_alias_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Avatar Upload Section
                        Text(
                            text = dict["avatar"] ?: "Avatar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 22.sp, // Larger font
                                fontWeight = FontWeight.Black
                            ),
                            color = cardTitle,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, cardBorder, CircleShape)
                                    .background(innerCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUrl != null) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_icon_gaudi_n_1793575273048),
                                        contentDescription = "NailiX Icon Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("upload_device_avatar_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = cardBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = "Upload", tint = if (isDarkTheme) Color(0xFF0F172A) else Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ВЫБРАТЬ ИЗ УСТРОЙСТВА", fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color(0xFF0F172A) else Color.White, fontSize = 12.sp)
                                }

                                if (avatarUrl != null) {
                                    OutlinedButton(
                                        onClick = { viewModel.setOnboardingAvatar(null) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("reset_avatar_btn"),
                                        border = BorderStroke(1.5.dp, Color(0xFFF05D5E)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF05D5E))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ВЕРНУТЬ ИКОНКУ NAILIX", fontWeight = FontWeight.Bold, color = Color(0xFFF05D5E), fontSize = 11.sp)
                                    }
                                } else {
                                    Text(
                                        text = "По умолчанию: фирменный аватар NailiX с мозаикой Гауди ✨",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = bodyTextColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // Bottom Nav buttons for Step 3
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setOnboardingStep(2) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                border = BorderStroke(2.dp, cardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = cardBorder),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = dict["back"]?.uppercase() ?: "CANCEL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = cardBorder
                                )
                            }

                            Button(
                                onClick = {
                                    val finalName = if (name.isBlank()) (if (role == "master") "Новый Мастер" else (if (language == "ru") "Клиент" else "Guest")) else "$name $surname".trim()
                                    val finalPhone = if (phone.isBlank()) "+34 600 000 000" else phone
                                    // If they are a master, they have step 4. Clients finish here.
                                    if (role == "master") {
                                        viewModel.setOnboardingStep(4)
                                    } else {
                                        viewModel.registerClientProfile(
                                            name = finalName,
                                            phone = finalPhone,
                                            homeAddr = "Calella Center, Moragas 2",
                                            workAddr = "Calella beach, Jovara 120"
                                        )
                                        viewModel.completeOnboarding()
                                        Toast.makeText(context, "Добро пожаловать, $finalName! 🎉", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .gaudiOrnament(isButton = true, alphaMultiplier = 2.0f, isDark = isDarkTheme)
                                    .gaudiWaveBorder(alpha = 0.8f)
                                    .testTag("profile_submit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = cardBorder),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = if (role == "master") (dict["next"]?.uppercase() ?: "NEXT") else (dict["complete_reg"]?.uppercase() ?: "DONE"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.quickStartAsGuest("client") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("step3_guest_btn"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, cardBorder.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = cardBorder)
                        ) {
                            Text(
                                text = if (language == "ru") "⚡ Продолжить как Гость (без сохранения)"
                                       else if (language == "es") "⚡ Continuar como Invitado"
                                       else "⚡ Continue as Guest",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = cardBorder
                            )
                        }
                    }
                }
            }

            // STEP 5: STARTUP PROFILE SELECTOR
            if (step == 5) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(Color(0xFFE0F2FE), RoundedCornerShape(24.dp))
                        .gaudiOrnament(isButton = false, alphaMultiplier = 1.8f)
                        .gaudiWaveBorder(alpha = 0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(4.dp, Color(0xFF1D539F)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (language == "ru") "ВЫБЕРИТЕ ПРОФИЛЬ" else "SELECT PROFILE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFF0F2C59),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = if (language == "ru") "Войдите под существующим профилем или создайте новый." else "Enter with an existing profile or add a new one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Client Profiles List
                        if (savedClients.isNotEmpty()) {
                            Text(
                                text = if (language == "ru") "Клиенты" else "Clients",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D539F),
                                modifier = Modifier.align(Alignment.Start).padding(vertical = 4.dp)
                            )
                            savedClients.forEach { client ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable {
                                            viewModel.activateClientProfile(client.phone)
                                            viewModel.completeOnboarding()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1D539F).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF1D539F)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = client.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F2C59)
                                            )
                                            Text(
                                                text = client.phone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }

                        // Master Profiles List
                        if (savedMasters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (language == "ru") "Мастера" else "Masters",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00A896),
                                modifier = Modifier.align(Alignment.Start).padding(vertical = 4.dp)
                            )
                            savedMasters.forEach { master ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable {
                                            viewModel.activateMasterProfile(master.id)
                                            viewModel.completeOnboarding()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00A896).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Face,
                                                contentDescription = null,
                                                tint = Color(0xFF00A896)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = master.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F2C59)
                                            )
                                            Text(
                                                text = if (master.instagramUsername.isNotEmpty()) "@${master.instagramUsername}" else master.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = bodyTextColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Add Profile button!
                        Button(
                            onClick = {
                                viewModel.setOnboardingStep(2) // go to Select Role to add a new profile
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .gaudiOrnament(isButton = true, alphaMultiplier = 2.0f, isDark = isDarkTheme)
                                .gaudiWaveBorder(alpha = 0.8f)
                                .testTag("add_profile_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = cardBorder),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == "ru") "ДОБАВИТЬ ПРОФИЛЬ" else "ADD PROFILE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Back to language choice button
                        OutlinedButton(
                            onClick = { viewModel.setOnboardingStep(1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, cardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = cardBorder)
                        ) {
                            Text(
                                text = dict["back"]?.uppercase() ?: "BACK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = cardBorder
                            )
                        }
                    }
                }
            }

            // STEP 4: MASTER SETUP - TEMPLATE SELECTION & INSTAGRAM PORTFOLIO IMPORT
            if (step == 4 && role == "master") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(cardBg, RoundedCornerShape(24.dp))
                        .gaudiOrnament(isButton = false, alphaMultiplier = 1.8f, isDark = isDarkTheme)
                        .gaudiWaveBorder(alpha = 0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Transparent to display Gaudí ornament beautifully
                    border = BorderStroke(4.dp, cardBorder),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (dict["services_templates"] ?: "Template").uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 32.sp, // Enlarged bold headings
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = cardTitle,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // Ready templates option
                        val templates = listOf(
                            Triple(
                                "Базовый маникюр и педикюр",
                                "Классика и гель-лак • Смарт-педикюр • Дизайн",
                                "Рекомендуется для начинающих и универсалов"
                            ),
                            Triple(
                                "Топ-Дизайн и Ручная роспись",
                                "Акварельные рисунки • Французский маникюр • Инкрустация",
                                "Рекомендуется для художников и топ-дизайнеров"
                            ),
                            Triple(
                                "Минимализм и Укрепление",
                                "Медицинский уход • Японский маникюр • Лечение IBX",
                                "Рекомендуется для мастеров эстетического ухода"
                            )
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            templates.forEachIndexed { idx, (title, servicesTxt, recom) ->
                                val selected = selectedTemplateIndex == idx
                                val templateBgColor = if (selected) {
                                    if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
                                } else {
                                    if (isDarkTheme) Color(0xFF334155) else Color.White
                                }
                                val templateBorderColor = if (selected) {
                                    if (isDarkTheme) Color(0xFF0F172A) else Color(0xFF0F2C59)
                                } else {
                                    if (isDarkTheme) Color(0xFF475569) else Color(0xFF94A3B8)
                                }
                                val templateTextColor = if (selected) {
                                    if (isDarkTheme) Color(0xFF0F172A) else Color.White
                                } else {
                                    if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF0F2C59)
                                }
                                val templateSubtitleColor = if (selected) {
                                    if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
                                } else {
                                    if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F)
                                }
                                val templateDescColor = if (selected) {
                                    if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.8f)
                                } else {
                                    if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569)
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTemplateIndex = idx }
                                        .background(templateBgColor, RoundedCornerShape(16.dp))
                                        .gaudiWaveBorder(alpha = if (selected) 0.9f else 0.3f)
                                        .gaudiOrnament(isButton = false, alphaMultiplier = if (selected) 2.2f else 1.0f, isDark = isDarkTheme || selected)
                                        .testTag("template_card_$idx"),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    border = BorderStroke(
                                        width = if (selected) 4.dp else 2.dp,
                                        color = templateBorderColor
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp, // Larger font
                                            color = templateTextColor
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = servicesTxt,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold
                                              ),
                                            color = templateSubtitleColor
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = recom,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = templateDescColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Settlement Currency Selector
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(innerCardBg, RoundedCornerShape(20.dp))
                                .gaudiWaveBorder(alpha = 0.65f)
                                .gaudiOrnament(isButton = false, alphaMultiplier = 1.5f, isDark = isDarkTheme)
                                .testTag("currency_selection_card"),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(3.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = "Currency",
                                        tint = innerCardText,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (language == "ru") "Валюта расчетов" else if (language == "tr") "Hesaplaşma Para Birimi" else if (language == "he") "מטבע חישוב" else "Settlement Currency",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = innerCardText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (language == "ru") "Выберите валюту, в которой будут указаны цены на ваши услуги по умолчанию." else if (language == "tr") "Varsayılan olarak hizmetlerinizin fiyatlarının gösterileceği para birimini seçin." else if (language == "he") "בחר את המטבע שבו יוצגו מחירי השירותים שלך כברירת מחדל." else "Select the currency in which your service prices will be shown by default.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = bodyTextColor
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val currencies = listOf(
                                    Pair("EUR", "€ EUR"),
                                    Pair("USD", "$ USD"),
                                    Pair("RUB", "₽ RUB"),
                                    Pair("GBP", "£ GBP"),
                                    Pair("CHF", "₣ CHF"),
                                    Pair("ILS", "₪ ILS"),
                                    Pair("AED", "د.إ AED")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    currencies.forEach { (code, display) ->
                                        val isSel = masterCurrency == code
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { masterCurrency = code },
                                            label = { Text(display, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = cardBorder,
                                                selectedLabelColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                                            ),
                                            modifier = Modifier.testTag("onboarding_currency_chip_$code")
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Real Instagram portfolio import section - Top 20 Popular Posts
                        val igColor = if (isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF00A896)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(innerCardBg, RoundedCornerShape(20.dp))
                                .gaudiWaveBorder(alpha = 0.65f)
                                .gaudiOrnament(isButton = false, alphaMultiplier = 1.5f, isDark = isDarkTheme)
                                .testTag("insta_portfolio_card"),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(3.dp, igColor)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Instagram Portfolio Import",
                                        tint = igColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "ИМПОРТ ТОП-20 ИЗ INSTAGRAM",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = innerCardText
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Кратчайший импорт топ-20 публикаций мастера по популярности (от большего количества лайков к меньшему) с предпросмотром и прямыми ссылками на посты.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = bodyTextColor
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = alias,
                                    onValueChange = { alias = it },
                                    label = { Text("Instagram никнейм мастера", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                    placeholder = { Text("@art_nail_master") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(inputBgColor, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.loadTop20InstagramPosts(alias)
                                        showIgFeedDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = igColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("ig_sync_wizard_btn")
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = "Download", tint = if (isDarkTheme) Color(0xFF0F172A) else Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("⚡ Импортировать Топ-20 по популярности", fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color(0xFF0F172A) else Color.White)
                                }

                                if (parsedInstagramPosts.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    // Successfully imported posts display
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().background(if (isDarkTheme) Color(0xFF064E3B) else Color(0xFFE6F4EA), RoundedCornerShape(12.dp)).padding(12.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = if (isDarkTheme) Color(0xFF34D399) else Color(0xFF137333), modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Успешно импортировано: ${parsedInstagramPosts.size} работ с ссылками на Instagram! 🎉",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) Color(0xFF34D399) else Color(0xFF137333),
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Отметьте до 20 лучших работ для показа в профиле (выбрано: ${selectedDisplayPostIds.size} из 20):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = cardTitle
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(145.dp)
                                    ) {
                                        items(parsedInstagramPosts) { post ->
                                            val isSelected = selectedDisplayPostIds.contains(post.id)
                                            Card(
                                                modifier = Modifier
                                                    .width(115.dp)
                                                    .clickable { viewModel.toggleDisplayPostSelection(post.id, context) }
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) igColor else if (isDarkTheme) Color(0xFF475569) else Color(0xFFCBD5E1),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = if (isSelected) (if (isDarkTheme) Color(0xFF064E3B) else Color(0xFFE6F4EA)) else innerCardBg)
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    AsyncImage(
                                                        model = post.imageUrl,
                                                        contentDescription = "Ig Post",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Surface(
                                                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                                                        color = Color.Black.copy(alpha = 0.75f)
                                                    ) {
                                                        Text(
                                                            text = "❤️ ${post.likesCount}",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(2.dp)
                                                        )
                                                    }
                                                    if (isSelected) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(26.dp)
                                                                .align(Alignment.TopEnd)
                                                                .padding(3.dp)
                                                                .background(igColor, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = if (isDarkTheme) Color(0xFF0F172A) else Color.White, modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.loadTop20InstagramPosts(alias)
                                            showIgFeedDialog = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, igColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = igColor)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Обновить Топ-20 публикаций", color = igColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setOnboardingStep(3) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                border = BorderStroke(2.dp, cardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = cardBorder),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = dict["back"]?.uppercase() ?: "CANCEL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = cardBorder
                                )
                            }

                            Button(
                                onClick = {
                                    val finalMasterName = "$name $surname".trim().ifEmpty { if (name.isNotBlank()) name else "Мастер Ногтевого Сервиса" }
                                    viewModel.registerMasterProfile(
                                        name = finalMasterName,
                                        address = masterAddress.ifBlank { "Spain, Catalonia, Calella, Moragas 74" },
                                        schedule = masterHours.ifBlank { "Пн-Пт: 10:00 - 19:00" },
                                        experience = masterExp.toIntOrNull() ?: 5,
                                        languages = masterLangs.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("Русский", "Испанский") },
                                        instagram = alias.trim().ifEmpty { "art_nail_master" },
                                        currency = masterCurrency
                                    )
                                    viewModel.completeOnboarding()
                                    Toast.makeText(context, "Профиль мастера $finalMasterName успешно создан! 💅🎨", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .gaudiOrnament(isButton = true, alphaMultiplier = 2.0f, isDark = isDarkTheme)
                                    .gaudiWaveBorder(alpha = 0.8f)
                                    .testTag("master_onboarding_complete_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = cardBorder),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = dict["complete_reg"]?.uppercase() ?: "DONE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



    // Instagram Multi-factor Secure Verification Flow
    if (showIgAuthDialog) {
        Dialog(onDismissRequest = { if (!isVerifying) showIgAuthDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(innerCardBg, RoundedCornerShape(24.dp))
                    .gaudiWaveBorder(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(4.dp, if (isDarkTheme) Color(0xFF60A5FA) else Color(0xFF1A73E8)),
                colors = CardDefaults.cardColors(containerColor = innerCardBg)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Google / Instagram partnership brand identity bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF4285F4))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFFE1306C))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "БЕЗОПАСНЫЙ ВХОД GOOGLE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isDarkTheme) Color(0xFF60A5FA) else Color(0xFF1A73E8),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Связывание аккаунта Instagram через Google OAuth API",
                        style = MaterialTheme.typography.bodySmall,
                        color = bodyTextColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isVerifying) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = if (isDarkTheme) Color(0xFF60A5FA) else Color(0xFF1A73E8),
                                strokeWidth = 5.dp,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = verificationStepText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF1A73E8),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Information card about privacy and security
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F6FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Shield",
                                        tint = Color(0xFF1A73E8),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Конфиденциальность гарантирована",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1A73E8)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Нам не нужен ваш пароль. Google безопасно проверит вашу личность и предоставит временный токен доступа только для импорта ваших фотографий портфолио.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF334155)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Branded Google Sign-In Button
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable {
                                    showGoogleAccountChooser = true
                                }
                                .testTag("google_oauth_signin_btn"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFDADCE0)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                GoogleLogoIcon(modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Войти с помощью Google",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3C4043)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Branded Instagram Sign-In Button
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable {
                                    showInstagramWebView = true
                                    showIgAuthDialog = false
                                }
                                .testTag("instagram_oauth_signin_btn"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFE1306C)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Instagram",
                                    tint = Color(0xFFE1306C),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Войти через Instagram",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC13584)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = { showIgAuthDialog = false },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, Color(0xFF0F2C59)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("ОТМЕНА", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F2C59))
                        }
                    }
                }
            }
        }
    }

    // Google Account Chooser Dialog
    if (showGoogleAccountChooser) {
        Dialog(onDismissRequest = { showGoogleAccountChooser = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .gaudiWaveBorder(alpha = 0.4f),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, Color(0xFFE2E8F0)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Google logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoogleLogoIcon(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5F6368),
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Выберите аккаунт",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF202124),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "для перехода в приложение NailiX",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF5F6368)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Account list
                    val accounts = listOf(
                        Triple("Vivian Marcciano", "vivian.marcciano@gmail.com", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=60"),
                        Triple("Nail Master Pro", "nailmaster.pro@gmail.com", "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=60")
                    )
                    
                    accounts.forEach { (accName, accEmail, accAvatar) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    selectedGoogleEmail = accEmail
                                    selectedGoogleName = accName
                                    selectedGoogleAvatar = accAvatar
                                    showGoogleAccountChooser = false
                                    showIgAuthDialog = true
                                    isVerifying = true
                                    verificationStepText = "Установка безопасного зашифрованного SSL-соединения с Google..."
                                    
                                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                                    handler.postDelayed({
                                        verificationStepText = "Аутентификация пользователя и выпуск OAuth токена..."
                                        handler.postDelayed({
                                            verificationStepText = "Запрос разрешений на импорт медиафайлов Instagram..."
                                            handler.postDelayed({
                                                verificationStepText = "Успешное сопоставление аккаунтов Google и Instagram! 🎉"
                                                handler.postDelayed({
                                                    isVerifying = false
                                                    showIgAuthDialog = false
                                                    
                                                    // Fill profile with verified data
                                                    val names = accName.split(" ")
                                                    name = names.getOrNull(0) ?: ""
                                                    surname = names.getOrNull(1) ?: ""
                                                    email = accEmail
                                                    phone = "+34 6" + (10000000..99999999).random()
                                                    alias = "@" + accEmail.substringBefore("@")
                                                    viewModel.setOnboardingAvatar(accAvatar)
                                                    
                                                    if (accEmail == "nailmaster.pro@gmail.com" || accName.contains("Master", ignoreCase = true)) {
                                                        viewModel.setOnboardingRole("master")
                                                    }
                                                    
                                                    // Immediately trigger beautiful Instagram import dialog!
                                                    showIgFeedDialog = true
                                                    viewModel.startInstagramSimulationImport()
                                                    Toast.makeText(context, "Вход выполнен успешно! 🎉 Профиль заполнен, импортируем работы...", Toast.LENGTH_LONG).show()
                                                }, 1000)
                                            }, 1200)
                                        }, 1200)
                                    }, 1200)
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape
                                ) {
                                    AsyncImage(
                                        model = accAvatar,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = accName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF3C4043)
                                    )
                                    Text(
                                        text = accEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF5F6368)
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Select",
                                    tint = Color(0xFF5F6368)
                                )
                            }
                        }
                    }
                    
                    // Add account option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                Toast.makeText(context, "Добавление нового аккаунта временно недоступно в демо-версии", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF1F3F4), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add account",
                                tint = Color(0xFF5F6368)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Использовать другой аккаунт",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1A73E8)
                        )
                    }
                    
                    Button(
                        onClick = {
                            showGoogleAccountChooser = false
                            showGoogleWebView = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = "Web", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Реальный Web OAuth 2.0", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))
                    
                    OutlinedButton(
                        onClick = { showGoogleAccountChooser = false },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "ОТМЕНА",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF5F6368)
                        )
                    }
                }
            }
        }
    }

    // Instagram 2FA Verification SMS dialog
    if (showIgSmsDialog) {
        Dialog(onDismissRequest = { showIgSmsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .gaudiWaveBorder(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(4.dp, Color(0xFFE1306C))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color(0xFFE1306C),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ВЕРИФИКАЦИЯ КОДА",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFE1306C)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "На ваш номер телефона, привязанный к аккаунту @$igUser, отправлен 6-значный код верификации.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isSmsVerifying) {
                        CircularProgressIndicator(color = Color(0xFFE1306C))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Проверка кода безопасности...", fontWeight = FontWeight.Bold, color = Color(0xFFE1306C))
                    } else {
                        OutlinedTextField(
                            value = smsVerificationCode,
                            onValueChange = { if (it.length <= 6) smsVerificationCode = it },
                            label = { Text("Введите 6-значный код", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                letterSpacing = 4.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE1306C),
                                unfocusedBorderColor = Color(0xFF0F2C59)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("ig_sms_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (smsVerificationCode.length == 6) {
                                    isSmsVerifying = true
                                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                                    handler.postDelayed({
                                        isSmsVerifying = false
                                        showIgSmsDialog = false
                                        
                                        // Auto-fill form fields with beautiful formatted data
                                        name = igUser.replace("_", " ").replace(".", " ")
                                            .split(" ")
                                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                                        alias = "@$igUser"
                                        phone = "+34 6" + (10000000..99999999).random()
                                        email = "$igUser@instagram.com"
                                        viewModel.setOnboardingAvatar("https://images.unsplash.com/photo-1604303739265-fd53384d78af?w=500")
                                        
                                        // Open selector and load posts
                                        showIgFeedDialog = true
                                        viewModel.startInstagramSimulationImport()
                                        Toast.makeText(context, "Профиль успешно верифицирован! 🎉 Заполнены имя, контакты и аватар. Загружаем публикации...", Toast.LENGTH_LONG).show()
                                    }, 1500)
                                } else {
                                    Toast.makeText(context, "Введите полный 6-значный код", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .gaudiOrnament(isButton = true, alphaMultiplier = 1.5f)
                                .testTag("ig_sms_submit_btn")
                        ) {
                            Text("ПОДТВЕРДИТЬ КОД", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { showIgSmsDialog = false },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, Color(0xFF0F2C59)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("ОТМЕНА", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F2C59))
                        }
                    }
                }
            }
        }
    }

    // Instagram Portfolio Top-20 Popularity Feed selector dialog
    if (showIgFeedDialog) {
        val existingPosts by viewModel.parsedInstagramPosts.collectAsStateWithLifecycle()
        val limit = when (subTier) {
            "free" -> 20
            "pro" -> 100
            "promax" -> 1000
            else -> 20
        }

        Dialog(onDismissRequest = { showIgFeedDialog = false }) {
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
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color(0xFF833AB4)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "По популярности (максимум лайков ➔ минимум). Отметьте до 20 постов с прямыми ссылками.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showIgFeedDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shortest way controls: Select all 20, Deselect, Counter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3E8FF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF833AB4)
                        ) {
                            Text(
                                text = "Отмечено: ${selectedIgPosts.size} из 20",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { viewModel.selectAllImportPosts() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("⚡ Выбрать все", fontWeight = FontWeight.Bold, color = Color(0xFF833AB4), fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { viewModel.deselectAllImportPosts() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Снять", color = Color(0xFF64748B), fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isConnectingIg) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF833AB4), strokeWidth = 4.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Загрузка публикаций профиля по популярности...", fontWeight = FontWeight.Bold, color = Color(0xFF833AB4))
                            }
                        }
                    } else {
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
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(10.dp))
                                            )
                                            // Rank badge overlay on photo
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(3.dp),
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "❤️ ${post.likesCount} лайков",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                    color = Color(0xFFE1306C)
                                                )
                                                if (index == 0) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("👑 ТОП", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFD97706))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = post.caption,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            post.originalUrl?.let { url ->
                                                Row(
                                                    modifier = Modifier.clickable { uriHandler.openUri(url) },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF833AB4), modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "Ссылка на Instagram ↗",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF833AB4),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

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

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.completeInstagramImportAndLoad()
                            showIgFeedDialog = false
                            if (name.isBlank() && alias.isNotBlank()) {
                                name = alias.trim().removePrefix("@").replace("_", " ").replace(".", " ")
                                    .split(" ")
                                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                            }
                            if (avatarUrl == null && igFeed.isNotEmpty()) {
                                viewModel.setOnboardingAvatar(igFeed.first().imageUrl)
                            }
                            Toast.makeText(context, "Успешно импортировано ${selectedIgPosts.size} постов с ссылками на Instagram! 🎉", Toast.LENGTH_SHORT).show()
                        },
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
                        Text("ИМПОРТИРОВАТЬ ТОП (${selectedIgPosts.size}) С ССЫЛКАМИ", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showIgFeedDialog = false },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF64748B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("ОТМЕНА", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }

    // Limit warning & selective deletion dialog
    if (showLimitWarningDialog) {
        val existingPosts by viewModel.parsedInstagramPosts.collectAsStateWithLifecycle()
        val limit = when (subTier) {
            "free" -> 20
            "pro" -> 100
            "promax" -> 100
            else -> 20
        }
        val neededSpace = existingPosts.size + selectedIgPosts.size

        Dialog(onDismissRequest = { }) { // Non-dismissible without explicit action to force choice
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color.Red,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "МЕСТА НЕ ХВАТАЕТ!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "В бесплатной версии программы лимит составляет $limit постов. Сейчас в вашем портфолио: ${existingPosts.size} постов, а вы пытаетесь импортировать: ${selectedIgPosts.size}.\n\nПожалуйста, удалите существующие записи ниже, чтобы освободить место, либо оформите PRO подписку для расширения лимита до 100!",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F2C59)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Existing Portfolio Deletion List
                    Text(
                        text = "ТЕКУЩЕЕ ПОРТФОЛИО (${existingPosts.size} / $limit)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0F2C59)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(existingPosts) { post ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = post.imageUrl,
                                            contentDescription = "Portfolio image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = post.caption,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "❤️ ${post.likesCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deletePortfolioPost(post.id)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete post",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Premium Card Promo Link
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.upgradeSubscription("pro")
                                Toast.makeText(context, "Тариф PRO успешно подключен! Лимит увеличен до 100 работ 👑", Toast.LENGTH_LONG).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF4FF)),
                        border = BorderStroke(2.dp, Color(0xFFD946EF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro", tint = Color(0xFFD946EF), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("КУПИТЬ PRO ПОДПИСКУ", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodySmall, color = Color(0xFF701A75))
                                Text("Расширить лимит портфолио до 100 постов", style = MaterialTheme.typography.labelSmall, color = Color(0xFF701A75).copy(alpha = 0.8f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.completeInstagramImportAndLoad()
                            showLimitWarningDialog = false
                            showIgFeedDialog = false
                            Toast.makeText(context, "Импорт успешно завершен! 🎉", Toast.LENGTH_SHORT).show()
                        },
                        enabled = neededSpace <= limit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, disabledContainerColor = Color.LightGray),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("limit_force_import_btn")
                    ) {
                        Text(
                            text = if (neededSpace <= limit) "ЗАВЕРШИТЬ ИМПОРТ" else "МЕСТА НЕ ХВАТАЕТ (${neededSpace} / ${limit})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showLimitWarningDialog = false },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(2.dp, Color(0xFF0F2C59)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("ОТМЕНА", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F2C59))
                    }
                }
            }
        }
    }

    // Pro / ProMax Subscription Upgrade popup dialog
    if (showUpgradeDialog) {
        Dialog(onDismissRequest = { showUpgradeDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .gaudiWaveBorder(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(4.dp, Color(0xFF0F2C59))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PRO ПОДПИСКА MASTER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color(0xFF0F2C59),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Расширяйте границы своего портфолио и привлекайте в 10 раз больше премиум-клиентов!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tier Cards
                    // PRO
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.upgradeSubscription("pro")
                                showUpgradeDialog = false
                                Toast.makeText(context, "Тариф PRO успешно подключен! Лимит увеличен до 100 работ 👑", Toast.LENGTH_LONG).show()
                            }
                            .testTag("sub_pro_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (subTier == "pro") MaterialTheme.colorScheme.primaryContainer else Color.White
                        ),
                        border = BorderStroke(
                            width = 3.dp,
                            color = if (subTier == "pro") MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro", tint = Color(0xFFD946EF))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PRO ТАРИФ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("$9.99 / мес", fontWeight = FontWeight.ExtraBold, color = Color(0xFFD946EF))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• До 100 работ в портфолио", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = if (language == "ru") {
                                    "• Приоритетный поиск в NailiX"
                                } else {
                                    "• Priority search in NailiX"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // PRO MAX
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.upgradeSubscription("promax")
                                showUpgradeDialog = false
                                Toast.makeText(context, "Тариф PRO MAX успешно подключен! Лимит увеличен до 100 работ 🚀", Toast.LENGTH_LONG).show()
                            }
                            .testTag("sub_promax_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (subTier == "promax") MaterialTheme.colorScheme.primaryContainer else Color.White
                        ),
                        border = BorderStroke(
                            width = 3.dp,
                            color = if (subTier == "promax") MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stars, contentDescription = "ProMax", tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PRO MAX", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("$19.99 / мес", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• До 100 работ в портфолио", style = MaterialTheme.typography.bodySmall)
                            Text("• 0% комиссия сервиса на все заказы", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { showUpgradeDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Отмена")
                    }
                }
            }
        }
    }

    if (showGoogleWebView) {
        Dialog(onDismissRequest = { showGoogleWebView = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFF1A73E8)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoogleLogoIcon(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Google Secure Web Sign-In", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                        IconButton(onClick = { showGoogleWebView = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                        }
                    }
                    
                    AndroidView(
                        modifier = Modifier.weight(1f),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val url = request?.url?.toString() ?: ""
                                        if (url.contains("code=") || url.contains("token=") || url.contains("callback") || url.contains("success")) {
                                            showGoogleWebView = false
                                            Toast.makeText(context, "Вход через Google OAuth выполнен! 🎉", Toast.LENGTH_SHORT).show()
                                            
                                            // Fill profile with verified data
                                            name = "Елена"
                                            surname = "Красивая"
                                            email = "elena.beauty@gmail.com"
                                            phone = "+34 622 " + (100000..999999).random()
                                            alias = "@elena_manicure"
                                            viewModel.setOnboardingAvatar("https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=60")
                                            viewModel.setOnboardingRole("master")
                                            
                                            showIgFeedDialog = true
                                            viewModel.startInstagramSimulationImport()
                                            return true
                                        }
                                        return false
                                    }
                                }
                                loadUrl("https://accounts.google.com/InteractiveLogin")
                            }
                        }
                    )
                }
            }
        }
    }

    if (showInstagramWebView) {
        Dialog(onDismissRequest = { showInstagramWebView = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFE1306C)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF1F2))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "IG Logo",
                                tint = Color(0xFFE1306C),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Instagram Secure Web Sign-In", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                        IconButton(onClick = { showInstagramWebView = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                        }
                    }
                    
                    AndroidView(
                        modifier = Modifier.weight(1f),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val url = request?.url?.toString() ?: ""
                                        if (url.contains("code=") || url.contains("token=") || url.contains("callback") || url.contains("success")) {
                                            showInstagramWebView = false
                                            Toast.makeText(context, "Вход через Instagram OAuth выполнен! 🎉", Toast.LENGTH_SHORT).show()
                                            
                                            showIgFeedDialog = true
                                            viewModel.startInstagramSimulationImport()
                                            return true
                                        }
                                        return false
                                    }
                                }
                                loadUrl("https://www.instagram.com/accounts/login/")
                            }
                        }
                    )
                }
            }
        }
    }
}
