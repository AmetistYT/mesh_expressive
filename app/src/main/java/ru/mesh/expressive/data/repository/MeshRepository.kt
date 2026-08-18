package ru.mesh.expressive.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.mesh.expressive.data.local.SessionManager
import ru.mesh.expressive.data.model.*
import ru.mesh.expressive.data.remote.MeshNetworkClient
import ru.mesh.expressive.data.remote.WorksSearchRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AutoCompleteResult(
    val completedCount: Int,
    val totalStarsEarned: Int,
    val message: String
)

class MeshRepository(private val sessionManager: SessionManager) {

    private val _studentProfile = MutableStateFlow(
        StudentProfile(
            id = "17486681",
            personId = 2778403L,
            contingentGuid = "3473f068-8ec0-47a1-920a-a18e75d6c389",
            firstName = "Семён",
            lastName = "Софель",
            middleName = "Максимович",
            className = "7-В",
            schoolName = "ГБОУ Школа № 315",
            gpa = 0.0
        )
    )
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _scheduleToday = MutableStateFlow<List<LessonScheduleItem>>(emptyList())
    val scheduleToday: StateFlow<List<LessonScheduleItem>> = _scheduleToday.asStateFlow()

    private val _scheduleTomorrow = MutableStateFlow<List<LessonScheduleItem>>(emptyList())
    val scheduleTomorrow: StateFlow<List<LessonScheduleItem>> = _scheduleTomorrow.asStateFlow()

    private val _homeworkList = MutableStateFlow<List<HomeworkItem>>(emptyList())
    val homeworkList: StateFlow<List<HomeworkItem>> = _homeworkList.asStateFlow()

    private val _subjectSummaries = MutableStateFlow<List<SubjectSummary>>(emptyList())
    val subjectSummaries: StateFlow<List<SubjectSummary>> = _subjectSummaries.asStateFlow()

    private val _gamificationProfile = MutableStateFlow(
        GamificationProfile(
            id = 275590L,
            gamificationId = "AAE75590",
            coinsCount = if (sessionManager.infiniteStarsOverride) 999999999 else 5,
            coinsSpent = 0,
            level = 1,
            currentXp = 0,
            nextLevelXp = 1000,
            dailyGiftAvailable = false,
            infiniteStarsOverride = sessionManager.infiniteStarsOverride
        )
    )
    val gamificationProfile: StateFlow<GamificationProfile> = _gamificationProfile.asStateFlow()

    private val _starLeaders = MutableStateFlow<List<StarLeaderItem>>(emptyList())
    val starLeaders: StateFlow<List<StarLeaderItem>> = _starLeaders.asStateFlow()

    private val _rewards = MutableStateFlow<List<RewardItem>>(emptyList())
    val rewards: StateFlow<List<RewardItem>> = _rewards.asStateFlow()

    private val _works = MutableStateFlow<List<WorkItem>>(emptyList())
    val works: StateFlow<List<WorkItem>> = _works.asStateFlow()

    private val _ratingInfo = MutableStateFlow(RatingInfo(0, 0, 0, 0, 0, 0))
    val ratingInfo: StateFlow<RatingInfo> = _ratingInfo.asStateFlow()

    private val _mealsBalance = MutableStateFlow(
        MealsBalance(
            clientBalanceRub = 18.15,
            dailyLimitRub = null,
            hotMealSubscribed = false,
            cardId = "138810049",
            transactions = emptyList()
        )
    )
    val mealsBalance: StateFlow<MealsBalance> = _mealsBalance.asStateFlow()

    private val _attendance = MutableStateFlow(
        AttendanceSummary(
            totalLessons = 0,
            attendedLessons = 0,
            excusedAbsences = 0,
            unexcusedAbsences = 0,
            percentage = 100.0,
            emiasCertificates = emptyList()
        )
    )
    val attendance: StateFlow<AttendanceSummary> = _attendance.asStateFlow()

    suspend fun saveAuthToken(token: String) {
        sessionManager.authToken = token
        fetchRemoteData()
    }

    private fun updateStarLeadersList(spentStars: Int) {
        if (spentStars > 0) {
            _starLeaders.value = listOf(
                StarLeaderItem(
                    rank = 1,
                    name = "${_studentProfile.value.firstName} ${_studentProfile.value.lastName} (Вы)",
                    className = _studentProfile.value.className,
                    spentStars = spentStars,
                    level = _gamificationProfile.value.level,
                    isCurrentUser = true
                )
            )
        } else {
            _starLeaders.value = listOf(
                StarLeaderItem(
                    rank = 1,
                    name = "${_studentProfile.value.firstName} ${_studentProfile.value.lastName} (Вы)",
                    className = _studentProfile.value.className,
                    spentStars = 0,
                    level = _gamificationProfile.value.level,
                    isCurrentUser = true
                )
            )
        }
    }

    suspend fun fetchRemoteData() = withContext(Dispatchers.IO) {
        val rawToken = sessionManager.authToken ?: return@withContext
        val cleanToken = if (rawToken.startsWith("Bearer ")) rawToken.substring(7) else rawToken
        val bearerToken = "Bearer $cleanToken"
        val profileId = sessionManager.profileId.toLongOrNull() ?: 17486681L
        val studentId = sessionManager.studentId
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        try {
            // 1. Профиль учащегося из Web API
            val profileResp = MeshNetworkClient.familyWebApi.getProfile(cleanToken, profileId, "familyweb")
            if (profileResp.isSuccessful && profileResp.body() != null) {
                val body = profileResp.body()!!
                val child = body.children.firstOrNull()
                val prof = body.profile
                if (child != null) {
                    _studentProfile.value = _studentProfile.value.copy(
                        id = child.id?.toString() ?: _studentProfile.value.id,
                        contingentGuid = child.contingentGuid ?: _studentProfile.value.contingentGuid,
                        firstName = child.firstName ?: _studentProfile.value.firstName,
                        lastName = child.lastName ?: _studentProfile.value.lastName,
                        middleName = child.middleName ?: _studentProfile.value.middleName,
                        className = child.className ?: _studentProfile.value.className,
                        schoolName = child.school?.shortName ?: child.school?.name ?: _studentProfile.value.schoolName
                    )
                } else if (prof != null) {
                    _studentProfile.value = _studentProfile.value.copy(
                        id = prof.id?.toString() ?: _studentProfile.value.id,
                        firstName = prof.firstName ?: _studentProfile.value.firstName,
                        lastName = prof.lastName ?: _studentProfile.value.lastName,
                        middleName = prof.middleName ?: _studentProfile.value.middleName
                    )
                }
            }
        } catch (_: Exception) {}

        val activeGuid = _studentProfile.value.contingentGuid

        try {
            // 2. Геймификация и Звезды
            val gamifResp = MeshNetworkClient.gamificationApi.getGamificationProfile(
                bearerToken, profileId, "familymp", "diary-mobile", activeGuid
            )
            if (gamifResp.isSuccessful && gamifResp.body() != null) {
                val g = gamifResp.body()!!
                _gamificationProfile.value = g.copy(
                    infiniteStarsOverride = sessionManager.infiniteStarsOverride,
                    coinsCount = if (sessionManager.infiniteStarsOverride) 999999999 else g.coinsCount,
                    dailyGiftAvailable = false
                )
                updateStarLeadersList(g.coinsSpent)
            } else {
                updateStarLeadersList(_gamificationProfile.value.coinsSpent)
            }

            val gamifId = _gamificationProfile.value.id?.toString() ?: profileId.toString()
            val worksResp = MeshNetworkClient.gamificationApi.searchWorks(
                bearerToken, profileId, "familymp", "diary-mobile", gamifId, WorksSearchRequest(profileId = gamifId)
            )
            if (worksResp.isSuccessful && worksResp.body() != null) {
                _works.value = worksResp.body()!!.items
            }

            val rewardsResp = MeshNetworkClient.gamificationApi.searchRewards(
                bearerToken, profileId, "familymp", "diary-mobile"
            )
            if (rewardsResp.isSuccessful && rewardsResp.body() != null) {
                _rewards.value = rewardsResp.body()!!.items
            }
        } catch (_: Exception) {}

        try {
            // 3. Москвёнок
            val clientIdsJson = "{\"personId\":\"$activeGuid\"}"
            val mealsResp = MeshNetworkClient.mealsApi.getBalance(
                bearerToken, profileId, "familymp", "diary-mobile", clientIdsJson
            )
            if (mealsResp.isSuccessful && !mealsResp.body().isNullOrEmpty()) {
                val b = mealsResp.body()!!.first()
                if (b.balance != null) {
                    val rawBalance = b.balance
                    val balanceRub = rawBalance.toDouble() / 100.0
                    _mealsBalance.value = _mealsBalance.value.copy(
                        clientBalanceRub = balanceRub,
                        dailyLimitRub = b.expenseConstraints?.expenseDayLimit,
                        cardId = b.contractId?.toString() ?: _mealsBalance.value.cardId
                    )
                }
            }

            val txResp = MeshNetworkClient.mealsApi.getDayBalanceInfo(
                bearerToken, profileId, "familymp", "diary-mobile", activeGuid, "2026-08-01T00:00:00", 30
            )
            if (txResp.isSuccessful && txResp.body() != null && txResp.body()!!.items.isNotEmpty()) {
                _mealsBalance.value = _mealsBalance.value.copy(
                    transactions = txResp.body()!!.items
                )
            }
        } catch (_: Exception) {}

        try {
            // 4. Рейтинг успеваемости
            val ratingResp = MeshNetworkClient.ratingApi.getClassRank(
                bearerToken, "familymp", "mobile", activeGuid, 2073368L, todayStr
            )
            if (ratingResp.isSuccessful && ratingResp.body() != null) {
                _ratingInfo.value = ratingResp.body()!!
            }
        } catch (_: Exception) {}

        try {
            // 5. Расписание уроков
            val schedResp = MeshNetworkClient.familyWebApi.getSchedule(
                cleanToken, profileId, "familyweb", studentId, todayStr
            )
            if (schedResp.isSuccessful && schedResp.body() != null) {
                _scheduleToday.value = schedResp.body()!!
            }
        } catch (_: Exception) {}

        try {
            // 6. Посещаемость
            val attResp = MeshNetworkClient.familyWebApi.getAttendance(
                cleanToken, profileId, "familyweb", studentId, "2026-08-01", todayStr
            )
            if (attResp.isSuccessful && attResp.body() != null) {
                _attendance.value = _attendance.value.copy(
                    totalLessons = attResp.body()!!.attendance.size
                )
            }
        } catch (_: Exception) {}
    }

    suspend fun claimDailyGift(): String = withContext(Dispatchers.IO) {
        val token = sessionManager.authToken
        val bearerToken = if (token != null) {
            if (token.startsWith("Bearer ")) token else "Bearer $token"
        } else null
        val profileId = sessionManager.profileId.toLongOrNull() ?: 17486681L

        if (bearerToken != null) {
            try {
                val resp = MeshNetworkClient.gamificationApi.claimSystemGift(
                    bearerToken, profileId, "familymp", "diary-mobile"
                )
                if (resp.isSuccessful) {
                    val current = _gamificationProfile.value
                    _gamificationProfile.value = current.copy(
                        coinsCount = current.coinsCount + 150,
                        dailyGiftAvailable = false
                    )
                    return@withContext "150 звезд успешно зачислены на ваш баланс!"
                } else {
                    _gamificationProfile.value = _gamificationProfile.value.copy(
                        dailyGiftAvailable = false
                    )
                    return@withContext "Ежедневный подарок уже был получен ранее."
                }
            } catch (_: Exception) {
                _gamificationProfile.value = _gamificationProfile.value.copy(
                    dailyGiftAvailable = false
                )
                return@withContext "Ежедневный подарок уже был получен ранее."
            }
        } else {
            _gamificationProfile.value = _gamificationProfile.value.copy(
                dailyGiftAvailable = false
            )
            return@withContext "Ежедневный подарок уже был получен ранее."
        }
    }

    suspend fun autoCompleteAllQuests(): AutoCompleteResult = withContext(Dispatchers.IO) {
        val token = sessionManager.authToken
        val bearerToken = if (token != null) {
            if (token.startsWith("Bearer ")) token else "Bearer $token"
        } else null

        val profileId = sessionManager.profileId.toLongOrNull() ?: 17486681L
        val gamifId = _gamificationProfile.value.id?.toString() ?: profileId.toString()

        var completed = 0
        var starsEarned = 0

        val currentWorks = _works.value
        val uncompleted = currentWorks.filter { !it.isCompleted }

        for (work in uncompleted) {
            if (bearerToken != null) {
                try {
                    val resp = MeshNetworkClient.gamificationApi.updateWorkPoints(
                        bearerToken,
                        profileId,
                        "familymp",
                        "diary-mobile",
                        gamifId,
                        work.id
                    )
                    if (resp.isSuccessful) {
                        completed++
                        starsEarned += work.rewardStars
                    }
                } catch (_: Exception) {}
            } else {
                completed++
                starsEarned += work.rewardStars
            }
        }

        if (completed > 0) {
            _works.value = currentWorks.map { it.copy(isCompleted = true) }
            val currentCoins = _gamificationProfile.value.coinsCount
            _gamificationProfile.value = _gamificationProfile.value.copy(
                coinsCount = currentCoins + starsEarned
            )
        }

        AutoCompleteResult(
            completedCount = completed,
            totalStarsEarned = starsEarned,
            message = if (completed > 0)
                "Успешно отправлено $completed заданий! Зачислено +$starsEarned звезд."
            else
                "Нет доступных заданий для отправки."
        )
    }

    fun toggleHomework(id: String) {
        _homeworkList.value = _homeworkList.value.map {
            if (it.id == id) it.copy(isDone = !it.isDone) else it
        }
    }

    fun setInfiniteStars(enabled: Boolean) {
        sessionManager.infiniteStarsOverride = enabled
        _gamificationProfile.value = _gamificationProfile.value.copy(
            coinsCount = if (enabled) 999999999 else 5,
            infiniteStarsOverride = enabled
        )
    }

    fun unlockReward(id: String) {
        val reward = _rewards.value.find { it.id == id } ?: return
        val profile = _gamificationProfile.value
        if (profile.coinsCount >= reward.costStars || profile.infiniteStarsOverride) {
            val newSpent = profile.coinsSpent + reward.costStars
            if (!profile.infiniteStarsOverride) {
                _gamificationProfile.value = profile.copy(
                    coinsCount = profile.coinsCount - reward.costStars,
                    coinsSpent = newSpent
                )
            }
            updateStarLeadersList(newSpent)
            _rewards.value = _rewards.value.map {
                if (it.id == id) it.copy(isUnlocked = true) else it
            }
        }
    }

    fun logout() {
        sessionManager.logout()
        _studentProfile.value = StudentProfile(
            id = "17486681",
            personId = 2778403L,
            contingentGuid = "3473f068-8ec0-47a1-920a-a18e75d6c389",
            firstName = "Семён",
            lastName = "Софель",
            middleName = "Максимович",
            className = "7-В",
            schoolName = "ГБОУ Школа № 315",
            gpa = 0.0
        )
        _scheduleToday.value = emptyList()
        _scheduleTomorrow.value = emptyList()
        _homeworkList.value = emptyList()
        _subjectSummaries.value = emptyList()
        _works.value = emptyList()
        _rewards.value = emptyList()
        _starLeaders.value = emptyList()
    }
}
