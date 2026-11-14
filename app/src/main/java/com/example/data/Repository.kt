package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

// Model data classes for static UI features
data class ServiceItem(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val durationMin: Int,
    val description: String,
    val imageUrl: String = ""
)

fun ServiceEntity.toServiceItem(): ServiceItem {
    return ServiceItem(
        id = id,
        name = name,
        category = category,
        price = price,
        durationMin = durationMin,
        description = description,
        imageUrl = imageUrl
    )
}

data class BaseDesignItem(
    val id: Int,
    val title: String,
    val length: String, // "Короткие", "Средние", "Длинные"
    val shape: String,  // "Овал", "Квадрат", "Миндаль", "Стилет"
    val palette: String, // "Нюдовый", "Яркий", "Темный", "Френч", "Глиттер"
    val description: String,
    val imgUrl: String,
    val isGenerated: Boolean = false
)

data class InstagramPost(
    val id: Int,
    val imageUrl: String,
    val caption: String,
    val originalUrl: String?, // null if user wants "without links to original"
    val dateStr: String,
    val likesCount: Int,
    val username: String = "victoria_nails_studio"
)

class AppRepository(private val db: AppDatabase) {

    val bookingDao = db.bookingDao()
    val loyaltyDao = db.loyaltyDao()
    val reminderDao = db.reminderDao()
    val favoriteDesignDao = db.favoriteDesignDao()
    val serviceDao = db.serviceDao()
    val referredFriendDao = db.referredFriendDao()
    val loyaltyTransactionDao = db.loyaltyTransactionDao()
    val localizationDao = db.localizationDao()
    val masterNailArtDao = db.masterNailArtDao()

    // Database flow exposures
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()
    val loyaltyState: Flow<UserLoyalty?> = loyaltyDao.getLoyaltyFlow()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val favoriteDesigns: Flow<List<FavoriteDesign>> = favoriteDesignDao.getAllFavorites()
    val dynamicServices: Flow<List<ServiceItem>> = serviceDao.getAllServices().map { list ->
        list.map { it.toServiceItem() }
    }
    val allReferredFriends: Flow<List<ReferredFriend>> = referredFriendDao.getAllReferredFriends()
    val allLoyaltyTransactions: Flow<List<LoyaltyTransaction>> = loyaltyTransactionDao.getAllTransactions()
    val allLocalizations: Flow<List<LocalizationEntity>> = localizationDao.getAllLocalizations()
    val allMasterNailArts: Flow<List<MasterNailArtEntity>> = masterNailArtDao.getAllMasterNailArts()

    // Database write operations
    suspend fun insertBooking(booking: Booking): Long {
        val bookingId = bookingDao.insertBooking(booking)
        
        // Award loyalty points for booking (e.g. 10% of booking price as loyalty points)
        val loyalty = loyaltyDao.getLoyaltyDirect()
        if (loyalty != null) {
            val awardedPoints = (booking.price * 0.1).toInt()
            val newPoints = loyalty.points + awardedPoints
            // Update tier level based on points
            val newLevel = when {
                newPoints >= 1000 -> "Platinum"
                newPoints >= 500 -> "Gold"
                else -> "Silver"
            }
            loyaltyDao.updateLoyalty(loyalty.copy(points = newPoints, level = newLevel))
            if (awardedPoints > 0) {
                loyaltyTransactionDao.insertTransaction(
                    LoyaltyTransaction(
                        title = "Кэшбэк за '${booking.serviceName}' 💅",
                        points = awardedPoints
                    )
                )
            }
        }
        return bookingId
    }

    suspend fun completeBooking(id: Int) {
        bookingDao.updateBookingStatus(id, "Выполнена")
    }

    suspend fun updateBookingStatus(id: Int, status: String) {
        bookingDao.updateBookingStatus(id, status)
    }

    suspend fun deleteBooking(id: Int, bookingPrice: Double) {
        bookingDao.deleteBookingById(id)
        reminderDao.deleteRemindersForBooking(id)
        
        // Deduct points since the booking was deleted/cancelled
        val loyalty = loyaltyDao.getLoyaltyDirect()
        if (loyalty != null) {
            val pointsToDeduct = (bookingPrice * 0.1).toInt()
            val newPoints = maxOf(0, loyalty.points - pointsToDeduct)
            val newLevel = when {
                newPoints >= 1000 -> "Platinum"
                newPoints >= 500 -> "Gold"
                else -> "Silver"
            }
            loyaltyDao.updateLoyalty(loyalty.copy(points = newPoints, level = newLevel))
            if (pointsToDeduct > 0) {
                loyaltyTransactionDao.insertTransaction(
                    LoyaltyTransaction(
                        title = "Отмена начисления: '${bookingPrice}' ❌",
                        points = -pointsToDeduct
                    )
                )
            }
        }
    }

    suspend fun addReminder(reminder: Reminder) = reminderDao.insertReminder(reminder)

    suspend fun markReminderSent(id: Int) = reminderDao.markAsSent(id)

    suspend fun addFavoriteDesign(designId: Int) = favoriteDesignDao.addFavorite(FavoriteDesign(designId))

    suspend fun removeFavoriteDesign(designId: Int) = favoriteDesignDao.removeFavorite(designId)

    suspend fun subscribeMailingList(email: String, phone: String) {
        val loyalty = loyaltyDao.getLoyaltyDirect() ?: UserLoyalty()
        // Subscribed users receive 100 bonus points!
        val updatedLoyalty = loyalty.copy(
            isMailingSubscribed = true,
            subscribedEmail = email,
            subscribedPhone = phone,
            points = loyalty.points + 100
        )
        loyaltyDao.insertOrUpdateLoyalty(updatedLoyalty)
        loyaltyTransactionDao.insertTransaction(
            LoyaltyTransaction(
                title = "Бонус за подписку на рассылку 📩",
                points = 100
            )
        )
    }

    suspend fun claimReferral(code: String) {
        val loyalty = loyaltyDao.getLoyaltyDirect() ?: UserLoyalty()
        // Claiming referral awards 200 points to user and increments referred friends count
        val updatedLoyalty = loyalty.copy(
            points = loyalty.points + 200,
            friendsReferred = loyalty.friendsReferred + 1
        )
        loyaltyDao.insertOrUpdateLoyalty(updatedLoyalty)
        loyaltyTransactionDao.insertTransaction(
            LoyaltyTransaction(
                title = "Активация кода подруги: $code 🎁",
                points = 200
            )
        )
        referredFriendDao.insertReferredFriend(
            ReferredFriend(
                name = "Подруга ($code)",
                status = "Ввели код подруги 🎉",
                rewardPoints = 200,
                date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            )
        )
    }

    suspend fun simulateReferralInvite(friendName: String) {
        val loyalty = loyaltyDao.getLoyaltyDirect() ?: UserLoyalty()
        val newFriendsCount = loyalty.friendsReferred + 1
        val isFirstVisit = (1..2).random() == 1
        val status = if (isFirstVisit) "Завершила визит 🎉" else "Зарегистрировалась 📱"
        val bonus = if (isFirstVisit) 200 else 50 // Registered friend gives 50 points, completed visit gives 200 points!
        
        val updatedLoyalty = loyalty.copy(
            points = loyalty.points + bonus,
            friendsReferred = newFriendsCount
        )
        loyaltyDao.insertOrUpdateLoyalty(updatedLoyalty)
        
        referredFriendDao.insertReferredFriend(
            ReferredFriend(
                name = friendName,
                status = status,
                rewardPoints = bonus,
                date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            )
        )
        
        loyaltyTransactionDao.insertTransaction(
            LoyaltyTransaction(
                title = "Приглашение: $friendName ($status)",
                points = bonus
            )
        )
    }

    suspend fun spendBonusPoints(amount: Int, reason: String) {
        val loyalty = loyaltyDao.getLoyaltyDirect()
        if (loyalty != null && loyalty.points >= amount) {
            val updatedLoyalty = loyalty.copy(points = loyalty.points - amount)
            loyaltyDao.updateLoyalty(updatedLoyalty)
            loyaltyTransactionDao.insertTransaction(
                LoyaltyTransaction(
                    title = "Списание: $reason 🛍️",
                    points = -amount
                )
            )
        }
    }

    suspend fun addBonusPoints(amount: Int, reason: String) {
        val loyalty = loyaltyDao.getLoyaltyDirect()
        if (loyalty != null) {
            val updatedLoyalty = loyalty.copy(points = loyalty.points + amount)
            loyaltyDao.updateLoyalty(updatedLoyalty)
            loyaltyTransactionDao.insertTransaction(
                LoyaltyTransaction(
                    title = "Начисление: $reason 💎",
                    points = amount
                )
            )
        }
    }

    // Initialize loyalty table if empty
    suspend fun ensureLoyaltyInitialized() {
        val current = loyaltyDao.getLoyaltyDirect()
        if (current == null) {
            loyaltyDao.insertOrUpdateLoyalty(UserLoyalty(points = 250, friendsReferred = 1))
            
            // Insert default transactions
            loyaltyTransactionDao.insertTransaction(LoyaltyTransaction(title = "Приветственный бонус при регистрации 🌸", points = 150))
            loyaltyTransactionDao.insertTransaction(LoyaltyTransaction(title = "Подписка на уведомления в Telegram 📢", points = 100))
            
            // Insert a default referred friend
            referredFriendDao.insertReferredFriend(
                ReferredFriend(
                    name = "Анна Смирнова",
                    status = "Зарегистрировалась 📱",
                    rewardPoints = 50,
                    date = "12.07.2026"
                )
            )
        }
    }

    suspend fun insertService(service: ServiceEntity): Long = serviceDao.insertService(service)
    suspend fun updateServicePrice(id: Int, price: Double) = serviceDao.updateServicePrice(id, price)
    suspend fun deleteService(id: Int) = serviceDao.deleteService(id)

    suspend fun ensureLocalizationsInitialized() {
        val current = localizationDao.getAllLocalizationsDirect()
        if (current.isEmpty()) {
            val list = mutableListOf<LocalizationEntity>()
            val langCodes = listOf("ru", "en", "es", "zh", "fr", "de", "pt", "it", "tr", "ja")
            val translations = mapOf(
                "app_title" to listOf(
                    "Студия Ногтей Виктория", "Victoria Nail Studio", "Victoria Estudio de Uñas", "维多利亚美甲沙龙",
                    "Studio de Manucure Victoria", "Victoria Nagelstudio", "Victoria Estúdio de Unhas", "Victoria Studio di Unghie",
                    "Victoria Tırnak Stüdyosu", "ビクトリアネイルスタジオ"
                ),
                "upcoming_bookings" to listOf(
                    "Предстоящие записи", "Upcoming Bookings", "Próximas citas", "即将долаг预约",
                    "Rendez-vous à venir", "Anstehende Buchungen", "Próximos Compromissos", "Prossimi Appuntamenti",
                    "Yaklaşan Randevular", "今後の予約"
                ),
                "no_bookings" to listOf(
                    "Нет предстоящих записей.\nЗапишитесь к нашим лучшим мастерам! 💅", "No upcoming bookings yet.\nBook your next premium service! 💅",
                    "No hay próximas citas.\n¡Reserva tu servicio premium! 💅", "暂无预约。\n快来向我们最好的技师预约吧！💅",
                    "Aucun rendez-vous à venir.\nRéservez votre service premium ! 💅", "Noch keine anstehenden Termine.\nBuchen Sie Ihren Premium-Service! 💅",
                    "Sem compromissos agendados.\nAgende seu serviço premium! 💅", "Nessun appuntamento imminente.\nPrenota il tuo servizio premium! 💅",
                    "Henüz yaklaşan randevu yok.\nPremium hizmetiniz için yer ayırtın! 💅", "予約はまだありません。\nプレミアムサービスを予約しましょう！💅"
                ),
                "my_bonuses" to listOf(
                    "Мои Бонусы", "My Bonuses", "Mis Bonos", "我的积分",
                    "Mes Bonus", "Meine Boni", "Meus Bônus", "I Miei Bonus",
                    "Bonuslarım", "マイボーナス"
                ),
                "bonus_balance" to listOf(
                    "Баланс бонусов:", "Bonus balance:", "Balance de bonos:", "积分余额：",
                    "Solde des bonus :", "Bonusguthaben:", "Saldo de bônus:", "Bilancio dei bonus:",
                    "Bonus bakiyesi:", "ボーナス残高："
                ),
                "club_tier" to listOf(
                    "Статус клуба:", "Club tier:", "Nivel de club:", "会员等级：",
                    "Niveau du club :", "Club-Stufe:", "Nível do clube:", "Livello del club:",
                    "Kulüp seviyesi:", "クラブ会員ランク："
                ),
                "invite_friends" to listOf(
                    "🎁 Пригласить друга", "🎁 Invite Friends", "🎁 Invitar Amigos", "🎁 邀请好友",
                    "🎁 Parrainer des Amis", "🎁 Freunde einladen", "🎁 Convidar Amigos", "🎁 Invita Amici",
                    "🎁 Arkadaşlarını Davet Et", "🎁 友達を招待する"
                ),
                "share_code_desc" to listOf(
                    "Поделитесь кодом и получите по 200 бонусов!", "Share code to earn 200 bonus points each!", "¡Comparte el código para ganar 200 puntos cada uno!", "分享邀请码，双方均受奖200积分！",
                    "Partagez le code et gagnez 200 bonus chacun !", "Teilen Sie den Code, um jeweils 200 Boni zu erhalten!", "Compartilhe o código e ganhem 200 bônus cada!", "Condividi il codice per ricevere 200 bonus ciascuno!",
                    "Kodu paylaşın, her ikiniz de 200 bonus kazanın!", "コードを共有して、お互いに200ボーナスを獲得しましょう！"
                ),
                "exclusive_offers" to listOf(
                    "✨ Дополнительные акции", "✨ Exclusive Offers", "✨ Ofertas exclusivas", "✨ 专属优惠",
                    "✨ Offres exclusives", "✨ Exklusive Angebote", "✨ Ofertas Exclusivas", "✨ Offerte Esclusive",
                    "✨ Özel Kampanyalar", "✨ 特別オファー"
                ),
                "subscribe_desc" to listOf(
                    "Подпишитесь на рассылку акций и получите 100 бонусов!", "Subscribe to get hot deals and 100 welcome bonus points!", "¡Suscríbete para ofertas y recibe 100 puntos!", "订阅特惠资讯，立即获取100迎新积分！",
                    "Abonnez-vous aux offres et recevez 100 points bonus !", "Abonnieren Sie Angebote und erhalten Sie 100 Willkommensboni!", "Inscreva-se para promoções e ganhe 100 pontos!", "Iscriviti per le offerte e ottieni 100 punti bonus!",
                    "Kampanyalara abone olun ve 100 bonus kazanın!", "ニュースレターを購読して、100ウェルカムボーナスを獲得しましょう！"
                ),
                "subscribe_btn" to listOf(
                    "Подписаться", "Subscribe", "Suscribirse", "订阅",
                    "S'abonner", "Abonnieren", "Inscrever-se", "Iscriviti",
                    "Abone Ol", "購読する"
                ),
                "dismiss_btn" to listOf(
                    "Закрыть", "Close", "Cerrar", "关闭",
                    "Fermer", "Schließen", "Fechar", "Chiudi",
                    "Kapat", "閉じる"
                ),
                "global_settings" to listOf(
                    "Глобальные настройки", "Global Settings", "Ajustes globales", "全局设置",
                    "Paramètres globaux", "Globale Einstellungen", "Configurações Globais", "Impostazioni Globali",
                    "Genel Ayarlar", "一般設定"
                ),
                "interface_language" to listOf(
                    "Язык интерфейса", "Interface Language", "Idioma de la interfaz", "界面语言",
                    "Langue de l'interface", "Schnittstellensprache", "Idioma da Interface", "Lingua dell'Interfaccia",
                    "Arayüz Dili", "表示言語"
                ),
                "main_currency" to listOf(
                    "Основная валюта", "Main Currency", "Moneda principal", "主要货币",
                    "Devise principale", "Hauptwährung", "Moeda Principal", "Valuta Principale",
                    "Ana Para Birimi", "決済通貨"
                ),
                "booking_details" to listOf(
                    "Детали записи", "Booking Details", "Detalles de reserva", "预约详情",
                    "Détails de réservation", "Buchungsdetails", "Detalhes do Agendamento", "Dettagli Prenotazione",
                    "Randevu Detayları", "予約詳細"
                ),
                "profile_manager" to listOf(
                    "Менеджер профилей", "Profile Manager", "Gestor de perfiles", "个人资料管理",
                    "Gestionnaire de profils", "Profil-Manager", "Gerenciador de Perfis", "Gestione Profili",
                    "Profil Yöneticisi", "プロフィール管理"
                ),
                "subscription_tier" to listOf(
                    "Тарифный план:", "Subscription Tier:", "Nivel de suscripción:", "订阅级别：",
                    "Niveau d'abonnement :", "Abonnement-Stufe:", "Nível de Assinatura:", "Piano Abbonamento:",
                    "Abonelik Seviyesi:", "サブスクリプションプラン："
                ),
                "create_new_profile" to listOf(
                    "Создать новый профиль", "Create New Profile", "Crear nuevo perfil", "创建新个人资料",
                    "Créer un nouveau profil", "Neues Profil erstellen", "Criar Novo Perfil", "Crea Nuovo Profilo",
                    "Yeni Profil Oluştur", "新規プロフィール作成"
                ),
                "currency_desc" to listOf(
                    "Выберите валюту, в которой будут идти расчеты и отображаться цены по умолчанию.",
                    "Select the default currency for calculations and price tags.",
                    "Seleccione la moneda predeterminada para cálculos y precios.",
                    "选择用于计算和价格显示的默认货币。",
                    "Sélectionnez la devise par défaut pour les calculs et les tarifs.",
                    "Wählen Sie die Standardwährung für Berechnungen und Preise.",
                    "Selecione a moeda padrão para cálculos e preços.",
                    "Seleziona la valuta predefinita per calcoli e prezzi.",
                    "Hesaplamalar ve fiyat etiketleri için varsayılan para birimini seçin.",
                    "デフォルトの通貨を選択してください。"
                ),
                "trust_seal" to listOf(
                    "⭐️ Знак доверия NailiX", "⭐️ Nailix Trust Seal", "⭐️ Sello de confianza Nailix", "⭐️ Nailix 信任印章",
                    "⭐️ Sceau de confiance Nailix", "⭐️ Nailix Vertrauenssiegel", "⭐️ Selo de Confiança Nailix", "⭐️ Sigillo di Garanzia Nailix",
                    "⭐️ Nailix Güven Mührü", "⭐️ Nailix 信頼マーク"
                )
            )

            for ((key, values) in translations) {
                for (i in 0 until langCodes.size) {
                    val code = langCodes[i]
                    val value = values.getOrNull(i) ?: values.first()
                    list.add(LocalizationEntity(key, code, value))
                }
            }
            localizationDao.insertLocalizations(list)
        }
    }

    suspend fun ensureServicesInitialized() {
        val current = serviceDao.getAllServicesDirect()
        if (current.isEmpty()) {
            val defaultServices = listOf(
                ServiceEntity(name = "Классический маникюр", category = "Маникюр", price = 1200.0, durationMin = 45, description = "Обработка кутикулы, придание формы ногтям, легкий массаж рук с питательным маслом.", imageUrl = "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "Аппаратный маникюр", category = "Маникюр", price = 1500.0, durationMin = 50, description = "Профессиональная аппаратная чистка без замачивания, идеальное выравнивание пластины.", imageUrl = "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "Маникюр с покрытием гель-лак", category = "Маникюр", price = 2200.0, durationMin = 75, description = "Классический или комбинированный маникюр + стойкое покрытие гель-лак под кутикулу в 1-2 слоя.", imageUrl = "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "SMART Педикюр комплекс", category = "Педикюр", price = 3000.0, durationMin = 90, description = "Инновационный дисковый педикюр стопы + обработка пальчиков с укреплением ногтевой пластины.", imageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "Экспресс Педикюр (пальчики)", category = "Педикюр", price = 1800.0, durationMin = 50, description = "Быстрая эстетическая обработка ногтей ног и покрытие гель-лаком.", imageUrl = "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "Наращивание ногтей (акригель)", category = "Моделирование", price = 3500.0, durationMin = 120, description = "Удлинение ногтевой пластины на формах премиальным гипоаллергенным акригелем.", imageUrl = "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "Коррекция наращенных ногтей", category = "Моделирование", price = 2500.0, durationMin = 90, description = "Снятие старого материала, ремонт трещин, поднятие клюющих ногтей, выравнивание формы.", imageUrl = "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "СПА-Уход 'Глубокое Питание'", category = "СПА-уход", price = 1100.0, durationMin = 35, description = "Горячее парафиновое обертывание, сахарный пилинг, массаж с эфирными маслами ши и кокоса.", imageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "Сложный дизайн ногтей (все пальчики)", category = "Дизайн", price = 1200.0, durationMin = 40, description = "Ручная роспись, акварель, лепка, сложная геометрия, градиент аэрографом.", imageUrl = "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60"),
                ServiceEntity(name = "Минималистичный дизайн (слайдеры/стемпинг)", category = "Дизайн", price = 600.0, durationMin = 20, description = "Лаконичные наклейки, точечные рисунки или стемпинг-узоры на 2-4 ногтя.", imageUrl = "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60")
            )
            serviceDao.insertServices(defaultServices)
        }
    }

    // Static Services List (Price List)
    val services = listOf(
        ServiceItem(1, "Классический маникюр", "Маникюр", 1200.0, 45, "Обработка кутикулы, придание формы ногтям, легкий массаж рук с питательным маслом."),
        ServiceItem(2, "Аппаратный маникюр", "Маникюр", 1500.0, 50, "Профессиональная аппаратная чистка без замачивания, идеальное выравнивание пластины."),
        ServiceItem(3, "Маникюр с покрытием гель-лак", "Маникюр", 2200.0, 75, "Классический или комбинированный маникюр + стойкое покрытие гель-лак под кутикулу в 1-2 слоя."),
        ServiceItem(4, "SMART Педикюр комплекс", "Педикюр", 3000.0, 90, "Инновационный дисковый педикюр стопы + обработка пальчиков с укреплением ногтевой пластины."),
        ServiceItem(5, "Экспресс Педикюр (пальчики)", "Педикюр", 1800.0, 50, "Быстрая эстетическая обработка ногтей ног и покрытие гель-лаком."),
        ServiceItem(6, "Наращивание ногтей (акригель)", "Моделирование", 3500.0, 120, "Удлинение ногтевой пластины на формах премиальным гипоаллергенным акригелем."),
        ServiceItem(7, "Коррекция наращенных ногтей", "Моделирование", 2500.0, 90, "Снятие старого материала, ремонт трещин, поднятие клюющих ногтей, выравнивание формы."),
        ServiceItem(8, "СПА-Уход 'Глубокое Питание'", "СПА-уход", 1100.0, 35, "Горячее парафиновое обертывание, сахарный пилинг, массаж с эфирными маслами ши и кокоса."),
        ServiceItem(9, "Сложный дизайн ногтей (все пальчики)", "Дизайн", 1200.0, 40, "Ручная роспись, акварель, лепка, сложная геометрия, градиент аэрографом."),
        ServiceItem(10, "Минималистичный дизайн (слайдеры/стемпинг)", "Дизайн", 600.0, 20, "Лаконичные наклейки, точечные рисунки или стемпинг-узоры на 2-4 ногтя.")
    )

    // Static Base Designs Gallery for inspiration
    val baseDesigns = listOf(
        BaseDesignItem(1, "Свадебный Френч", "Короткие", "Овал", "Френч", "Нежная классика с перламутровым отливом и тончайшей белой линией улыбки.", "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(2, "Жемчужный Миндаль", "Средние", "Миндаль", "Нюдовый", "Благородная полупрозрачная база цвета увядшей розы с изысканной жемчужной втиркой.", "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(3, "Королевский Бордо", "Короткие", "Квадрат", "Темный", "Глубокий винный оттенок с бархатным матовым топом на четком мягком квадрате.", "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(4, "Коралловое Лето", "Средние", "Овал", "Яркий", "Сочный неоново-коралловый оттенок с легким глянцевым финишем, заряжающий настроением.", "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(5, "Звездное Серебро", "Длинные", "Миндаль", "Глиттер", "Градиентная растяжка из серебряного шиммера от кутикулы к свободному краю.", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(6, "Неоновая Геометрия", "Длинные", "Квадрат", "Яркий", "Ярко-салатовый фон с тонкими графичными линиями черного гель-лака на безымянном пальце.", "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(7, "Изумрудная Тайна", "Длинные", "Миндаль", "Темный", "Глубокий благородный изумрудный цвет с добавлением мелких хлопьев золотой потали.", "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(8, "Лаконичные Точки", "Короткие", "Овал", "Нюдовый", "Ультраминимализм: камуфлирующая база персикового цвета с одной глянцевой точкой у основания ногтя.", "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(9, "Голографический Стилет", "Длинные", "Стилет", "Глиттер", "Экстравагантная острая форма с плотным покрытием радужным светоотражающим глиттером.", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60"),
        BaseDesignItem(10, "Готический Черный Матовый", "Средние", "Стилет", "Темный", "Угольно-черный цвет в велюровом исполнении с инкрустацией черными стразами у кутикулы.", "https://images.unsplash.com/photo-1607779097040-26e80aa78e66?w=500&auto=format&fit=crop&q=60")
    )

    // Simulated Instagram parsed portfolio items
    val mockInstagramPosts = listOf(
        InstagramPost(
            1,
            "https://images.unsplash.com/photo-1604303739265-fd53384d78af?w=600&auto=format&fit=crop&q=60",
            "Освежили весенний маникюр для прекрасной Елены! 💅 Комбинированный маникюр + укрепление базой + нежный молочный оттенок с акварельным стемпингом. #маникюрспб #комбиманикюр #стемпинг",
            "https://instagram.com/p/C7W90axILsN/",
            "04.07.2026",
            124
        ),
        InstagramPost(
            2,
            "https://images.unsplash.com/photo-1610992015732-2449b76344cc?w=600&auto=format&fit=crop&q=60",
            "Дерзкий неон для нашей постоянной гостьи! 🔥 Четкий мягкий квадрат с кислотно-оранжевыми торцами на нюдовой подложке. Как вам такой тренд этого лета? #неоновыйманикюр #френчногти #дизайнногтей",
            "https://instagram.com/p/C8Z71axMNbY/",
            "02.07.2026",
            218
        ),
        InstagramPost(
            3,
            "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=60",
            "Аппаратный SMART педикюр со спа-уходом от Карины. Идеальные пяточки, мягкий пилинг и сияющие пальчики. Готовы покорять босоножками! 👡 #смартпедикюр #педикюрспб #спауход ног",
            "https://instagram.com/p/C9Y62asXPlM/",
            "30.06.2026",
            95
        ),
        InstagramPost(
            4,
            "https://images.unsplash.com/photo-1632345031435-8797b2d58045?w=600&auto=format&fit=crop&q=60",
            "Элегантная втирка Единорог 🦄 на нежно-розовом гель-лаке. Блеск переливается всеми цветами радуги под разным углом. Завораживает с первого взгляда! #втиркананогтях #нежныйдизайн #маникюридеи",
            "https://instagram.com/p/C1A80axLKsK/",
            "25.06.2026",
            340
        ),
        InstagramPost(
            5,
            "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=600&auto=format&fit=crop&q=60",
            "Восстановление сломанного ногтя + моделирование акригелем. Вернули идеальную длину и сделали ультрамодный матовый графит. Листайте карусель, чтобы посмотреть ДО 👉 #наращиваниеногтей #ремонтногтя #акригель",
            "https://instagram.com/p/C5F22axOImI/",
            "18.06.2026",
            156
        )
    )

    suspend fun ensureMasterNailArtsInitialized() {
        val existing = masterNailArtDao.getAllMasterNailArtsDirect()
        if (existing.isEmpty()) {
            val list = listOf(
                MasterNailArtEntity(
                    title = "Galaxy Nebula Cosmic Art",
                    masterName = "Elena",
                    complexity = "Masterpiece",
                    extraPrice = 45.0,
                    imageUrl = "https://images.unsplash.com/photo-1604654894610-df63bc536371?q=80&w=600&auto=format&fit=crop",
                    likes = 124,
                    views = 430,
                    description = "Deep celestial space background. Hand-drawn astronomical violet nebulas, ultra micro-glitter, constellation star lines in real silver paint, and stellar rhinestones."
                ),
                MasterNailArtEntity(
                    title = "Baroque Gold Filigree",
                    masterName = "Maria",
                    complexity = "Hard",
                    extraPrice = 35.0,
                    imageUrl = "https://images.unsplash.com/photo-1632345031435-8797b2d58045?q=80&w=600&auto=format&fit=crop",
                    likes = 89,
                    views = 280,
                    description = "Ornate architectural art featuring 3D gilded filigree moldings over a premium matte maroon wine backdrop."
                ),
                MasterNailArtEntity(
                    title = "Japanese Sakura Petals",
                    masterName = "Yuki",
                    complexity = "Medium",
                    extraPrice = 20.0,
                    imageUrl = "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?q=80&w=600&auto=format&fit=crop",
                    likes = 156,
                    views = 510,
                    description = "Soft milky gradient base with detailed, delicate miniature hand-painted cherry blossoms and golden foil details."
                ),
                MasterNailArtEntity(
                    title = "Minimalist Line Geometry",
                    masterName = "Anna",
                    complexity = "Easy",
                    extraPrice = 10.0,
                    imageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?q=80&w=600&auto=format&fit=crop",
                    likes = 67,
                    views = 195,
                    description = "Sleek and classy semi-translucent nude background with razor-thin black brush strokes forming minimal geometric layouts."
                ),
                MasterNailArtEntity(
                    title = "Unicorn Holographic Prism",
                    masterName = "Chloe",
                    complexity = "Medium",
                    extraPrice = 18.0,
                    imageUrl = "https://images.unsplash.com/photo-1607606325015-acf121387212?q=80&w=600&auto=format&fit=crop",
                    likes = 210,
                    views = 680,
                    description = "Futuristic liquid chrome mirror finish. Refracts into full rainbow prisms at every angle."
                ),
                MasterNailArtEntity(
                    title = "Art Deco Pearl Accent",
                    masterName = "Sophia",
                    complexity = "Hard",
                    extraPrice = 30.0,
                    imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?q=80&w=600&auto=format&fit=crop",
                    likes = 94,
                    views = 310,
                    description = "1920s-inspired vintage look. Cream shimmer base embellished with hand-placed micro-bead pearls and symmetrical gold foil borders."
                )
            )
            masterNailArtDao.insertMasterNailArts(list)
        }
    }
}
