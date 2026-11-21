package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.skinOrnament
import com.example.ui.theme.skinWaveBorder

internal data class OnboardingColors(
    val isDarkTheme: Boolean,
    val cardBg: Color,
    val cardBorder: Color,
    val cardTitle: Color,
    val innerCardBg: Color,
    val innerCardBorder: Color,
    val innerCardText: Color,
    val bodyTextColor: Color,
    val inputBgColor: Color
)

internal fun onboardingColorsFor(isDarkTheme: Boolean): OnboardingColors = OnboardingColors(
    isDarkTheme = isDarkTheme,
    cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE0F2FE),
    cardBorder = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F),
    cardTitle = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F2C59),
    innerCardBg = if (isDarkTheme) Color(0xFF0F172A) else Color.White,
    innerCardBorder = if (isDarkTheme) Color(0xFF334155) else Color(0xFF94A3B8),
    innerCardText = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF1D539F),
    bodyTextColor = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF334155),
    inputBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
)

internal fun Modifier.gaudiOrnament(
    isButton: Boolean = false,
    alphaMultiplier: Float = 1.0f,
    isDark: Boolean = false,
    skinCode: String = "gaudi"
): Modifier = this.skinOrnament(
    skinCode = skinCode,
    isButton = isButton,
    alphaMultiplier = alphaMultiplier,
    isDark = isDark
)

internal fun Modifier.gaudiWaveBorder(
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

            drawArc(
                color = Color(0xFFEA4335),
                startAngle = 180f,
                sweepAngle = 135f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = sizeToDraw,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = Color(0xFFFBBC05),
                startAngle = 135f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = sizeToDraw,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = Color(0xFF34A853),
                startAngle = 0f,
                sweepAngle = 135f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = sizeToDraw,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = Color(0xFF4285F4),
                startAngle = 315f,
                sweepAngle = 45f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = sizeToDraw,
                style = Stroke(width = strokeWidth)
            )
            drawLine(
                color = Color(0xFF4285F4),
                start = Offset(w * 0.5f, h * 0.5f),
                end = Offset(w * 0.9f, h * 0.5f),
                strokeWidth = strokeWidth
            )
        }
    }
}

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
        "complete_reg" to "Готово",
        "services_templates" to "Услуги",
        "skip" to "Пропустить"
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
        "complete_reg" to "Done",
        "services_templates" to "Services",
        "skip" to "Skip"
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
        "complete_reg" to "Listo",
        "services_templates" to "Servicios",
        "skip" to "Omitir"
    ),
    "zh" to mapOf(
        "welcome" to "NAILIX",
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
        "ai_synth" to "AI 头像",
        "enter_prompt" to "描述您的头像...",
        "generating" to "正在生成...",
        "complete_reg" to "完成",
        "services_templates" to "服务项目"
    ),
    "hi" to mapOf(
        "welcome" to "NAILIX",
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
        "ai_synth" to "एआई अवतार",
        "enter_prompt" to "अवतार का विवरण...",
        "generating" to "बनाया जा रहा है...",
        "complete_reg" to "पूर्ण",
        "services_templates" to "सेवाएं"
    ),
    "ar" to mapOf(
        "welcome" to "NAILIX",
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
        "ai_synth" to "رمز ذكاء اصطناعي",
        "enter_prompt" to "وصف الصورة...",
        "generating" to "جاري الإنشاء...",
        "complete_reg" to "تم",
        "services_templates" to "الخدمات"
    ),
    "pt" to mapOf(
        "welcome" to "NAILIX",
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
        "ai_synth" to "Avatar de IA",
        "enter_prompt" to "Descrição do avatar...",
        "generating" to "Gerando...",
        "complete_reg" to "Concluir",
        "services_templates" to "Serviços"
    ),
    "fr" to mapOf(
        "welcome" to "NAILIX",
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
        "ai_synth" to "Avatar IA",
        "enter_prompt" to "Description de l'avatar...",
        "generating" to "Génération...",
        "complete_reg" to "Terminer",
        "services_templates" to "Services"
    ),
    "he" to mapOf(
        "welcome" to "NAILIX",
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
        "ai_synth" to "אוואטר בינה מלאכותית",
        "enter_prompt" to "תיאור האוואטר...",
        "generating" to "מייצר...",
        "complete_reg" to "סיום",
        "services_templates" to "שירותים"
    ),
    "tr" to mapOf(
        "welcome" to "NAILIX",
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
        "ai_synth" to "Yapay Zeka Avatarı",
        "enter_prompt" to "Avatar açıklaması...",
        "generating" to "Üretiliyor...",
        "complete_reg" to "Tamamla",
        "services_templates" to "Hizmetler"
    )
)
