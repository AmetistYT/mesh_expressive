# Дневник МЭШ Expressive (Mesh Expressive)

> **Современный, быстрый и абсолютно свободный от трекеров клиент Московской электронной школы (МЭШ) в дизайн-системе Material Design 3 Expressive.**
>
> *вайбкод*

[![Android](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?style=flat)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg?style=flat)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3_Expressive-4285F4.svg?style=flat)](https://developer.android.com/jetpack/compose)
[![Zero Trackers](https://img.shields.io/badge/Trackers-0_(Pure)-00C853.svg?style=flat)](https://github.com/AmetistYT)
[![Vibecoded](https://img.shields.io/badge/Style-вайбкод-purple.svg)](https://github.com/AmetistYT)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## Главные особенности и возможности

- **Авто-сдача заданий за звезды**: Мгновенная сдача всех доступных ЦДЗ и заданий со звездами в один клик через прямой API (`POST /works/{workId}/points`).
- **0 трекеров и 100% приватность**: Полностью вырезаны все встроенные модули слежки официального клиента:
  - `Yandex AppMetrica` (телеметрия кликов и действий)
  - `Спутник МЭШ` (фоновый геотрекинг местоположения)
  - `VK MyTracker` и `OK Tracer` (рекламные профили)
  - `Sentry APM` и `Varioqub` (удаленные дампы и A/B конфиги)
- **Material 3 Expressive & Monet**:
  - Адаптивная динамическая тема под обои устройства (Material You).
  - 7-лепестковые контурные формы кнопок и аватаров (`M3Cookie7Shape`).
  - Волновые индикаторы прогресса и пружинная физика нажатий (`spring(dampingRatio = 0.6f)`).
- **Москвёнок (Питание)**:
  - Отображение точного баланса карты в реальном времени.
  - Поддержка статуса дневного лимита («Не установлен» / сумма).
  - Просмотр истории покупок и операций в буфете.
- **Геймификация, Звезды и Рейтинг щедрости**:
  - Актуальный баланс звезд (`balance`), уровень и опыт (`points / nextLevelXp`).
  - **Рейтинг щедрости**: корректный расчет лидерборда класса по реально потраченным звездам (`spentPoints`).
  - Автоматическая валидация статуса ежедневного подарка (+150 звезд).
- **Удобная авторизация**:
  - Вход через официальный шлюз `mos.ru` (SSO WebView).
  - **Root-экстрактор**: автоматическое бесшовное извлечение активной сессии из официального приложения `ru.mes.dnevnik`.
  - Ручной ввод и инспектор JWT-токена в настройках.

---

## Реверс-инжиниринг официального приложения

В папке [`decompiled/`](decompiled/) опубликованы материалы анализа и декомпиляции официального клиента `ru.mes.dnevnik`:
- [`decompiled/dnevnik_decompiled.js`](decompiled/dnevnik_decompiled.js) — полный декомпилированный JS-бандл React Native официального клиента (Hermes bytecode -> JS).
- [`decompiled/dnevnik_strings.txt`](decompiled/dnevnik_strings.txt) — полный пул строковых констант и эндпоинтов.
- [`decompiled/mesh_reverse_engineering_report.md`](decompiled/mesh_reverse_engineering_report.md) — подробный отчет об архитектуре API МЭШ, токенах и протоколах.

### Спецификация боевых эндпоинтов МЭШ

| Сервис | Метод | URL / Эндпоинт | Обязательные заголовки |
|---|---|---|---|
| **Профиль ученика** | `GET` | `https://school.mos.ru/api/family/web/v1/profile` | `Auth-Token: <token>`, `Profile-Id: <id>`, `X-Mes-Subsystem: familyweb` |
| **Звезды / Баланс** | `GET` | `https://school.mos.ru/api/gamification/v1/profiles?personId={contingentGuid}` | `Authorization: Bearer <token>`, `Profile-id: <id>`, `X-Mes-Subsystem: familymp`, `client-type: diary-mobile` |
| **Ежедневный подарок**| `POST` | `https://school.mos.ru/api/gamification/v1/rewards/system_gift` | `Authorization: Bearer <token>`, `Profile-id: <id>`, `X-Mes-Subsystem: familymp`, `client-type: diary-mobile` |
| **Москвёнок (Баланс)**| `GET` | `https://school.mos.ru/api/food/meals/v3/clients/balance?clientIds={"personId":"{contingentGuid}"}` | `Authorization: Bearer <token>`, `Profile-id: <id>`, `X-Mes-Subsystem: familymp`, `client-type: diary-mobile` |
| **Рейтинг успеваемости**| `GET` | `https://school.mos.ru/api/ej/rating/v1/rank/rankShort?personId={contingentGuid}&beginDate={from}&endDate={to}` | `Authorization: Bearer <token>`, `X-Mes-Subsystem: familymp`, `client-type: mobile` |
| **Посещаемость** | `GET` | `https://school.mos.ru/api/family/web/v1/attendance?student_id={id}&from={from}&to={to}` | `Auth-Token: <token>`, `Profile-Id: <id>`, `X-Mes-Subsystem: familyweb` |

---

## Сборка из исходного кода

### Требования:
- JDK 17 или новее
- Android SDK (API 35 / Build-Tools 35.0.0)

### Команды для сборки:
```bash
# Клонирование репозитория
git clone https://github.com/AmetistYT/mesh_expressive.git
cd mesh_expressive

# Сборка Debug APK
./gradlew assembleDebug

# Установка на подключенное устройство
./gradlew installDebug
```

---

## Безопасность и Лицензия

Проект распространяется под свободной лицензией **MIT**. Токены авторизации и персональные данные хранятся исключительно локально на устройстве пользователя в защищенном хранилище и никогда не передаются сторонним серверам.
