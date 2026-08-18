# Технический отчет по реверс-инжинирингу экосистемы «МЭШ Дневник»

## 1. Что такое МЭШ и «Дневник МЭШ»

**МЭШ (Московская электронная школа)** — это государственная цифровая образовательная экосистема г. Москвы, разработанная Департаментом образования и науки города Москвы (ДОНМ) совместно с Департаментом информационных технологий города Москвы (ДИТ).

**«Дневник МЭШ» (`ru.mes.dnevnik`)** — флагманское мобильное приложение для школьников, родителей и студентов колледжей Москвы. Оно предоставляет доступ к:
* Электронному журналу и дневнику (оценки, коэффициенты веса, средний балл, итоговые отметки за четверти/триместры/полугодия).
* Расписанию уроков, звонков, номерам кабинетов, ФИО учителей и заменам.
* Домашним заданиям (включая прикрепленные файлы, цифровые ДЗ — ЦДЗ, тесты).
* Сервису «Москвёнок» (питание и проход в школу): баланс лицевого счета, меню буфета и столовой, оформление предзаказов горячего питания, лимиты трат, история прохода через турникеты в реальном времени.
* Интеграции с ЕМИАС: автоматическая передача медицинских справок о болезни (форма 095/у) из поликлиник без необходимости приносить бумажные документы.
* Уведомлению об отсутствии («Ребенок не придет в школу») — подача заявления без звонков учителю.
* Библиотеке МЭШ: интерактивные сценарии уроков, тесты, электронные учебники.
* Рейтингам и геймификации: рейтинг в классе/параллели, система наград, ачивок и виртуальных подарков.
* Портфолио учащегося и олимпиадам (сервис «Горизонты»).
* Дополнительному образованию (кружки, секции, электронные договоры).

---

## 2. Анализ пакетов, обнаруженных на устройстве

В результате сканирования устройства через ADB обнаружено два приложения:

1. **`ru.mes.dnevnik`** — официальное релизное приложение «Дневник МЭШ» от ДИТ Москвы.
   * **Версия**: 3.79.13 (Code: `1778843041`)
   * **Target SDK**: 36 (Android 16 preview), **Min SDK**: 23 (Android 6.0)
   * **Архитектура**: Split APKs (`base.apk`, `split_config.arm64_v8a.apk`, `split_config.ru.apk`, `split_config.xxhdpi.apk`).
   * **Технологический стек**: React Native (New Architecture: TurboModules + Fabric renderer + NitroModules), скомпилированный в Hermes Bytecode v96.

2. **`ru.mesh.client`** — кастомный легковесный нативный клиент для МЭШ.
   * **Версия**: 1.0 (Code: `1`)
   * **Архитектура**: Монолитный APK.
   * **Технологический стек**: 100% Native Kotlin + Jetpack Compose + Dagger Hilt + Retrofit 2 + Coroutines/Flow. Реализует чистое обращение к API МЭШ без телеметрии и рекламы.

---

## 3. Архитектура и стек технологий официального клиента (`ru.mes.dnevnik`)

### 3.1. Frontend и рантайм
* **Ядро**: React Native 0.7x+ на движке **Hermes** (байткод версии 96, 92 627 скомпилированных функций в `index.android.bundle`).
* **Архитектура React Native**: New Architecture (Fabric Render Pipeline, C++ TurboModules, NitroModules, React Native Reanimated v3 Worklets).
* **State Management**: Redux, Redux-Saga (саги для управления сайд-эффектами: `authMeshSaga`, `refreshSudirTokenSaga` и др.), Reselect.
* **Локальное хранилище**: `react-native-mmkv` (высокопроизводительный key-value storage на базе mmap) + `expo-secure-store` / `react-native-keychain` для хранения токенов в Android KeyStore.
* **UI и компоненты**: Expo Modules (`expo-modules-core`), Lottie анимации, PagerView, React Navigation Native Stack.

### 3.2. Нативные библиотеки (`lib/arm64-v8a/`)
* **Картография и гео**: `libmaps-mobile.so` (Yandex MapKit SDK) — отображение школ, построение маршрутов, трекинг школьного транспорта.
* **Шифрование и безопасность**:
  * `libconceal.so` — Facebook Conceal (быстрое блочное шифрование AES-GCM локальных данных).
  * `libcrypto.so`, `libssl.so`, `libQuickCrypto.so` — криптография, TLS, валидация JWT/JWK.
  * `libtool-checker.so` — детектирование Root-прав, эмуляторов, отладчиков.
  * `libxhook.so`, `libtrhook2.so` — низкоуровневые хуки PLT для мониторинга вызовов.
* **Обработка документов и сканирование**:
  * `libpdfium.so`, `libpdfiumandroid.so` — движок рендеринга PDF для просмотра табелей, выписок, справок.
  * `libbarhopper_v3.so`, `libVisionCamera.so` — ML Kit Barcode Scanning для распознавания QR-кодов.

### 3.3. Динамические обновления (OTA Updates)
Приложение настроено на прием динамических обновлений JavaScript-бандла в обход Google Play / RuStore через собственный сервер CodePush:
* **CodePush Server URL**: `https://school.mos.ru/mobile/codepush`
* **Deployment Key**: `XoKKFkO8c3ZFPBn6CxMckl02DD29MbdBDNUBO`

### 3.4. Аналитика, телеметрия и пуш-уведомления
* **Yandex AppMetrica**: App ID `4175440`, API Key `eb082b29-d821-450f-8da8-3f4260720058`.
* **VK / OK Tracer**: APM и трекинг крешей (`libtracernative.so`, `ru.ok.tracer`).
* **VK MyTracker**: SDK Key `77826086913498829922`.
* **Sentry**: Мониторинг фронтенд-ошибок (`libsentry.so`).
* **RuStore Push SDK**: Project ID `6KFQOiMy3Z6hrB4ZKL1nj7otMURfVouY`.
* **RuStore Remote Config**: App ID `92e1ace2-6bee-4029-bac9-31ca5d04ba9b`.

---

## 4. Архитектура бэкенда и API МЭШ

Бэкенд МЭШ представляет собой микросервисную инфраструктуру на базе доменов `mos.ru`.

### 4.1. Авторизация и Single Sign-On (СУДИР / Mos.ru SSO)
Авторизация происходит через СУДИР (Система управления доступом к информационным ресурсам города Москвы):
* **IDP Host**: `https://login.mos.ru` (Тестовый: `https://login-tech.mos.ru`)
* **OAuth2 Endpoints**:
  * `/sps/oauth/ae` — авторизация мобильного клиента.
  * `/sps/oauth/te` — Token Exchange (обмен временного кода/токена на JWT access/refresh токены).
  * `/sps/api` — управление сессиями, профилями пользователя и привязанными детьми.
  * `/logout/esia` — выход с инвалидацией сессии ЕСИА (Госуслуги).

### 4.2. Сетевые протоколы и базовые микросервисы

| Сервис | Base URL | Назначение |
| :--- | :--- | :--- |
| **Family Mobile API** | `https://school.mos.ru/api/family/mobile/v1/` | Основной API дневника: профили, оценки, ДЗ, расписание |
| **Electronic Journal Rating** | `https://school.mos.ru/api/ej/rating/v1/` | Рейтинги учащихся, динамика успеваемости, средневзвешенные баллы |
| **Gamification & Alice AI** | `https://school.mos.ru/alice/v1/` | AI-помощник, ачивки, задания, виртуальные награды |
| **Meals & Cafeteria (Москвёнок)** | `https://school.mos.ru/api/food/meals/v3/` | Баланс питания, меню, предварительный заказ блюд, лимиты |
| **School Entrances (Турникеты)** | `https://school.mos.ru/api/pass/entrances/v1/` | Фиксация входов и выходов через школьные турникеты |
| **Materials & Library** | `https://school.mos.ru/api/family/materials/v1/` | Электронные учебники, материалы библиотеки МЭШ |
| **Additional Education** | `https://school.mos.ru/api/circles/family/v1/` | Кружки, спортивные секции, внеурочная деятельность |
| **Events & Calendar** | `https://school.mos.ru/api/eventcalendar/v1/` | Школьный календарь, каникулы, праздники, мероприятия |
| **EMIAS Integration** | `/api/ej/core/family/v1/emias_medical_recommendations` | Медицинские справки, освобождения по болезни |
| **MosPay Payment Gateway** | `https://pay.mos.ru` | Пополнение лицевого счета питания и оплата кружков |
| **News & Announcements** | `https://school.mos.ru/api/news/v2/` | Школьные и городские образовательные новости |

### 4.3. Обязательные HTTP-заголовки запросов
Для выполнения авторизованных запросов клиент передает:
* `Authorization: Bearer <JWT_ACCESS_TOKEN>` — токен пользователя.
* `x-mes-subsystem: family` — идентификатор подсистемы (семья/родители/ученики).
* `Profile-Id: <PROFILE_ID>` — ID активного профиля (ученика или родителя).
* `Auth-Token: <JWT>` — дублирующий токен для некоторых микросервисов.

### 4.4. Ключевые REST эндпоинты

```http
# Получение списка профилей (родитель и привязанные дети)
GET https://school.mos.ru/api/family/mobile/v1/profiles
Headers: Authorization, x-mes-subsystem

# Расписание уроков на определенную дату
GET https://school.mos.ru/api/family/mobile/v1/lesson_schedule_items?student_id={id}&date=YYYY-MM-DD
Headers: Authorization, x-mes-subsystem, Profile-Id

# Оценки учащегося за интервал дат
GET https://school.mos.ru/api/family/mobile/v1/marks?student_id={id}&from=YYYY-MM-DD&to=YYYY-MM-DD
Headers: Authorization, x-mes-subsystem, Profile-Id

# Домашние задания за интервал дат
GET https://school.mos.ru/api/family/mobile/v1/homeworks?student_id={id}&from=YYYY-MM-DD&to=YYYY-MM-DD
Headers: Authorization, x-mes-subsystem, Profile-Id

# Баланс лицевого счета школьного питания (Москвёнок)
GET https://school.mos.ru/api/food/meals/v3/clients/balance?clientIds={clientId}
Headers: Authorization, X-Mes-Subsystem: family

# Краткий рейтинг ученика в классе/параллели
GET https://school.mos.ru/api/ej/rating/v1/rank/rank/short?personId={personId}&beginDate=YYYY-MM-DD&endDate=YYYY-MM-DD
Headers: Authorization

# Поиск и сдача заданий в модуле геймификации
POST https://school.mos.ru/alice/v1/profiles/{profileId}/works/search
Headers: Authorization
Body: { "filters": {}, "pagination": { "limit": 20, "offset": 0 } }
```

---

## 5. Итог

Приложение «Дневник МЭШ» — это крупный клиент комплексной городской платформы. Внутри него интегрированы десятки независимых микросервисов Правительства Москвы: от электронного дневника и библиотеки до медицинских справок ЕМИАС, турникетов СКУД и биллинга питания «Москвёнок».

Исходный код и дампы доступны локально в директории:
* Исходники декомпиляции официального приложения: `/home/ametist1337/mesh_reverse/dnevnik_src/`
* Таблица строк и ключей: `/home/ametist1337/mesh_reverse/dnevnik_strings.txt`
* Исходники нативного клиента: `/home/ametist1337/mesh_reverse/mesh_client_src/`
