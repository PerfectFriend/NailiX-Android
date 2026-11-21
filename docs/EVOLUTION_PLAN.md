# 🚀 План рефакторинга и эволюции NailiX до бета-теста

**Статус:** ✅ Основные этапы выполнены и верифицированы (`20 ноября 2026 г.`).  
**Цель:** превратить прототип **NailiX** в честный, модульный и отказоустойчивый бета-продукт без симуляций, заглушек и скрытых fallback-данных, готовый к многопользовательскому бета-тестированию на реальных Android-устройствах.

---

## Этап 1. Устранение технических недочётов и соответствие Google Play / A11y — ✅ Выполнено
1. **Разрешения и выбор медиафайлов (Google Play Policy):**
   - ✅ Удалено неиспользуемое разрешение `android.permission.CAMERA` из `AndroidManifest.xml`.
   - ✅ Выбор аватара и фото работ переведён на zero-permission системный `ActivityResultContracts.PickVisualMedia()` и `PickMultipleVisualMedia(20)`.
2. **Доступность (Accessibility) и системная навигация:**
   - ✅ Увеличены интерактивные области кнопок (`cancel_booking_btn`, оценки звёздами, кнопки быстрого старта) минимум до `48.dp × 48.dp`.
   - ✅ Добавлен `BackHandler` на второстепенных вкладках `MainScreen.kt` для возврата на главный экран.
3. **Устранение устаревших API:**
   - ✅ Заменены `Icons.Default.Send`, `ArrowForward`, `SendToMobile`, `VolumeUp` на `Icons.AutoMirrored.Filled.*`.
   - ✅ Обновлены `SecondaryTabRow`, `TabRowDefaults.SecondaryIndicator`, `Locale.forLanguageTag(...)`, `AppDatabase.fallbackToDestructiveMigration(true)`.
   - ✅ Приведены в порядок комментарии в `AndroidManifest.xml` (брендинг **NailiX**).

---

## Этап 2. Архитектурный рефакторинг и модульность — ✅ Выполнено
1. **`OnboardingScreen.kt` (3 565 → 252 строки):**
   - ✅ Вынесены модули в `ui/screens/onboarding/`: `OnboardingVisuals.kt`, `OnboardingDialogs.kt`, `OnboardingSteps.kt`, `OnboardingStepMaster.kt`.
2. **`MainDialogs.kt` (2 675 строк → удалён и разбит по доменам):**
   - ✅ Вынесены в `ui/dialogs/`: `LocationAndMasterDialogs.kt`, `GlobalSettingsDialog.kt`, `ProfileDialogs.kt`, `RadioSelectorDialog.kt`, `BookingsAndQrDialogs.kt`.
3. **Контроллеры и репозитории из `MainViewModel.kt`:**
   - ✅ Выделены `RadioPlayerController.kt`, `CloudSyncRepository.kt`, `AuthRepository.kt`, `NailAiRepository.kt`.

---

## Этап 3. Честная реализация всех демо-функций — ✅ Выполнено
| Модуль / Функция | Реализовано в бета-версии |
| :--- | :--- |
| **Облачная БД и Auth** | ✅ Google Sign-In (`CredentialManager` + `GetSignInWithGoogleOption`) и именованная база **Cloud Firestore** с изоляцией по `uid` и правилами `firestore.rules`. |
| **AI-дизайн и ИИ-аватар** | ✅ **Firebase AI Logic (`Firebase.ai` + App Check)** (`gemini-3.8-flash`, `gemini-nano-banana-2.1`) с таймаутом 60 с и явным отображением ошибок (без подменных URL). |
| **Уведомления и FCM** | ✅ Нативный `google-services.json`, получение реального FCM-токена, `WorkManager`-напоминания с безопасной инициализацией и честный статус ошибок. |
| **Импорт из Instagram и галереи** | ✅ Честный парсер без подставных фото + мульти-выбор реальных фото из галереи (`PickMultipleVisualMedia`). |
| **Суверенная нода (`SovereignNodeManager`)** | ✅ Реальный TCP/SOCKS5-пинг (`Socket.connect`), честный статус `Офлайн`, экспорт JSON-снимка Room с SHA-256 контрольной суммой и очередь событий. |
| **Реферальная программа** | ✅ Реферальные коды `NLX-...` в Firestore с одноразовой атомарной активацией другим аккаунтом и системным Share Sheet. |
| **Оформление записи** | ✅ Удалены мёртвый рекламный баннер и макеты банковских карт; честное подтверждение записи в студии (`studio_visit` / `studio_transfer`). |
| **QR-билет визита** | ✅ Стандартный ISO/IEC 18004 QR-код (ZXing) с 16-символьным криптографическим токеном и проверкой подписи. |

---

## Этап 4. Тестирование и бета-релиз — ✅ Проверено
- ✅ `FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js` — **7/7 тестов пройдено**.
- ✅ `DeployRules` — правила `firestore.rules` развёрнуты в Firebase-проекте `gen-lang-client-0136622039`.
- ✅ `gradle :app:testDebugUnitTest --console=plain` — **12/12 unit и Robolectric CUJ-тестов пройдено**.
- ✅ Сборка отладочного APK (`:app:assembleDebug`) и верификация проекта через `compile_applet`.
