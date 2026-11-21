# 🔍 Технический аудит и отчёт о бета-готовности NailiX

**Дата обновления:** 20 ноября 2026 г.  
**Версия приложения:** `1.0.0` (`versionCode = 1`, `minSdk = 24`, `targetSdk = 36`, `compileSdk = 36.1`)  
**Идентификатор сборки (`applicationId`):** `com.aistudio.victoriastudio.mncur` (`namespace = "com.example"`)  
**Облачная конфигурация Firebase:** проект `gen-lang-client-0136622039`, именованная БД Firestore `ai-studio-android-victoria-3277c870-e266-464c-9e44-de82742cbab0`

---

## 1. Сводка статуса (Executive Summary)

| Направление | Статус | Краткий итог |
| :--- | :---: | :--- |
| **Сборка и KSP** | ✅ Пройдено | KSP `2.3.6`, `:app:compileDebugKotlin`, `:app:assembleDebug` и `:app:testDebugUnitTest` проходят успешно (`BUILD SUCCESSFUL`). |
| **Firebase Auth + Firestore + Rules** | ✅ Внедрено | Google Sign-In (`CredentialManager`), zero-trust `firestore.rules` (задеплоены с `lat`/`lon` и переносом записей), `7/7` тестов в эмуляторе пройдены (`node --test firestore.test.js`). |
| **Двусторонняя облачная синхронизация** | ✅ Внедрено | Полная двусторонняя синхронизация `bookings` (`clientUid` + `masterUid`), `services`, `nailArts`, `masters` (включая `lat`/`lon`) и перенос времени записи между Room и Firestore. |
| **Геолокация и маршруты** | ✅ Внедрено | Реальный Android `Geocoder` без лишних разрешений (адреса Дома, Работы и студий мастеров) + 1-тап маршруты в Google Maps (`geo:`). |
| **Firebase AI Logic (Text + Vision)** | ✅ Внедрено | `configureAppCheck(this, intent)`, `NailAiRepository` (`gemini-3.8-flash` текст + мультимодальный анализ фото через `PickVisualMedia`, `gemini-nano-banana-2.1`). |
| **Устранение симуляций / fallback** | ✅ Очищено | Удалены `quickStartAsGuest`, `simulateNotificationTrigger`, `simulateAiAvatarGeneration`, `simulateReferralInvite`, `startInstagramSimulationImport`, фейковые OAuth-диалоги, фейковый эскроу и AdMob-заглушки. |
| **Google Play Policy, A11y и Адаптив** | ✅ Исправлено | Zero-permission `PickVisualMedia`, цели касания `>= 48.dp`, `BackHandler` на второстепенных вкладках, адаптивный `NavigationRail` при `width >= 600.dp`. |
| **Модульность UI** | ✅ Рефакторинг | Монолиты `MainDialogs.kt` и `OnboardingScreen.kt` разбиты на модули (`ui/dialogs/`, `ui/screens/onboarding/`) + `RadioPlayerController.kt` + `LocationGeocoderHelper.kt`. |
| **Тесты** | ✅ Пройдено | `CoreEnginesUnitTest`, `NailixRobolectricCujTest`, `ExampleRobolectricTest`, `ExampleUnitTest`, `GreetingScreenshotTest` — 13/13 тестов пройдено. |

---

## 2. Проверка сборки, правил безопасности и тестов

1. **Firestore Security Rules (`firestore.rules` + `firestore.test.js`):**
   - Выполнено 7 наборов тестов на локальном эмуляторе (`FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js`): все 7 пройдены (`pass 7`, `fail 0`).
   - Проверены: отказ анонимного/неавторизованного доступа, изоляция профилей `users/{uid}`, неизменяемость `referralCode` и `loyalty_transactions`, изоляция записей `bookings` по `clientUid`/`masterUid` с разрешённым переносом `date`/`timeSlot` только для подтверждённых записей и блокировкой терминального статуса, публичный каталог `masters`/`services`/`nailArts`/`reviews` с валидацией `lat`/`lon` и контролем владельца, и одноразовая атомарная активация реферальных кодов `NLX-...`.
   - Правила задеплоены в Firebase (`DeployRules`).
2. **Unit и Robolectric CUJ-тесты (`gradle :app:testDebugUnitTest --console=plain`):**
   - `CoreEnginesUnitTest`: расчёт длительности и пересечения слотов (`SmartSchedulerEngine`), честный офлайн-пинг `SovereignNodeManager.pingNode()` по закрытому локальному порту и офлайн-очередь событий.
   - `NailixRobolectricCujTest`: криптографическая проверка и отклонение поддельного QR-билета + генерация `128×128` ZXing ISO/IEC 18004 битмапа (`VisitTicketCryptoManager`), детерминированный SHA-256 снимок `SovereignNodeManager`, безопасный жизненный цикл `RadioPlayerController` без ошибки `(-38, 0)`, а также полные CUJ-сценарии `MainViewModel`: быстрый старт, подтверждение записи в студии, перенос записи (`rescheduleBooking`), повторная запись в 1 тап (`repeatBooking`) и поиск/бронирование ближайшего окна (`findNextAvailableQuickSlot` + `quickBookNextAvailableSlot`).
3. **Замечания компилятора:**
   - Устранены устаревшие вызовы (`AutoMirrored` иконки, `SecondaryTabRow`, `TabRowDefaults.SecondaryIndicator`, `Locale.forLanguageTag`, `fallbackToDestructiveMigration(true)`, избыточный `inline` в `CloudSyncRepository`).
   - Остаются 3 информативных предупреждения совместимости в `firebase-messaging` из зафиксированной версии BoM (`onNewToken` override и `FirebaseMessaging.getInstance().token`).

---

## 3. Архитектурный обзор интеграций

| Компонент | Реализация в бета-сборке |
| :--- | :--- |
| **Аутентификация (`AuthRepository`, `AuthScreen`)** | Одиночная опция `GetSignInWithGoogleOption` / `GetGoogleIdOption` через `CredentialManager`, обработка `GetCredentialCancellationException`, реальный выход в `GlobalSettingsDialog`. |
| **Облачная синхронизация (`CloudSyncRepository`)** | Именованный инстанс `FirebaseFirestore.getInstance(getString(R.string.firestore_database_id))`, потоковые подписки `bookings` (`clientUid` + `masterUid`), `services`, `nailArts`, `masters`, `reviews`, перенос записей и `ServerTimestampBehavior.ESTIMATE`. |
| **Геокодирование и маршруты (`LocationGeocoderHelper`)** | Нативный Android `Geocoder` (API 33+ `GeocodeListener` и fallback для API <33) для автоопределения координат по адресу без лишних разрешений + `ACTION_VIEW` (`geo:`) в Google Maps. |
| **ИИ-стилист, Vision и аватары (`NailAiRepository`)** | Firebase AI Logic (`Firebase.ai` + App Check), текстовый и мультимодальный анализ фото рук/референсов через `gemini-3.8-flash` (с масштабированием Bitmap до 1024px) и `gemini-nano-banana-2.1`, таймаут `60_000L`. |
| **Уведомления (`FcmNotificationManager`, `AppFirebaseMessagingService`)** | Нативная инициализация через `google-services.json`, каналы уведомлений, защищённая инициализация `WorkManager` для напоминаний за 24 часа и явный статус `fcmError` при отсутствии Google Play Services. |
| **Импорт работ (`InstagramRealScraper` + галерея)** | Честный парсер без подставных фото + системный `PickMultipleVisualMedia(20)` для загрузки реальных фото с устройства. |
| **Суверенная нода (`SovereignNodeManager`)** | Реальный TCP/SOCKS5 (`127.0.0.1:9050`) `Socket.connect` с замером RTT, честный статус `Офлайн` при ошибке и экспорт SHA-256 JSON-снимка Room. |
| **Подтверждение записи и QR** | Подтверждение оплаты в студии (`studio_visit` / `studio_transfer`), перенос времени, повтор записи в 1 тап, быстрое ближайшее окно и стандартный сканируемый QR ISO/IEC 18004 через ZXing. |
