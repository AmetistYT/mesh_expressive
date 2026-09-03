package ru.mesh.expressive.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.mesh.expressive.data.local.SessionManager
import ru.mesh.expressive.data.model.*
import ru.mesh.expressive.data.remote.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AutoCompleteResult(
    val completedCount: Int,
    val totalStarsEarned: Int,
    val message: String
)

object DemoMockDataProvider {
    val studentProfile = StudentProfile(
        id = "demo_student_1",
        personId = 999001L,
        contingentGuid = "00000000-0000-0000-0000-000000000001",
        classUid = "demo_class_uid_1",
        classUnitId = 10001L,
        classLevelId = 8,
        firstName = "Алексей",
        lastName = "Смирнов",
        middleName = "Иванович",
        className = "8-Б",
        schoolName = "ГБОУ Лицей № 1580",
        gpa = 4.67
    )

    val scheduleToday = listOf(
        LessonScheduleItem(
            id = "demo_1",
            subject = "Алгебра",
            lessonNumber = 1,
            startTime = "08:30",
            endTime = "09:15",
            room = "Каб. 304",
            teacherName = "Иванова Е.А.",
            mark = 5,
            homework = "№ 244 (а, б), № 248"
        ),
        LessonScheduleItem(
            id = "demo_2",
            subject = "Физика",
            lessonNumber = 2,
            startTime = "09:30",
            endTime = "10:15",
            room = "Каб. 210",
            teacherName = "Петров С.В.",
            homework = "Параграф 14, вопросы 1-5"
        ),
        LessonScheduleItem(
            id = "demo_3",
            subject = "Русский язык",
            lessonNumber = 3,
            startTime = "10:35",
            endTime = "11:20",
            room = "Каб. 412",
            teacherName = "Смирнова О.Н.",
            mark = 4,
            homework = "Упр. 118 по заданию"
        ),
        LessonScheduleItem(
            id = "demo_4",
            subject = "Литература",
            lessonNumber = 4,
            startTime = "11:40",
            endTime = "12:25",
            room = "Каб. 412",
            teacherName = "Смирнова О.Н.",
            homework = "Читать гл. 3-4"
        ),
        LessonScheduleItem(
            id = "demo_5",
            subject = "История",
            lessonNumber = 5,
            startTime = "12:45",
            endTime = "13:30",
            room = "Каб. 108",
            teacherName = "Ковалев Д.М.",
            mark = 5,
            homework = "Конспект в тетради"
        )
    )

    val homeworkList = listOf(
        HomeworkItem(
            id = "hw_1",
            subject = "Алгебра",
            description = "№ 244 (а, б), № 248 на стр. 62",
            date = "18 августа",
            dueDate = "19 августа",
            isDone = false
        ),
        HomeworkItem(
            id = "hw_2",
            subject = "Физика",
            description = "Параграф 14, ответить на вопросы в конце",
            date = "18 августа",
            dueDate = "19 августа",
            isDone = true
        )
    )

    val subjectSummaries = listOf(
        SubjectSummary(
            subject = "Алгебра",
            averageMark = 4.80,
            marks = listOf(
                MarkItem(id = "m1", subject = "Алгебра", value = 5, weight = 1.0, date = "15.08", topic = "Квадратные уравнения"),
                MarkItem(id = "m2", subject = "Алгебра", value = 5, weight = 1.5, date = "12.08", topic = "Контрольная работа", isExam = true),
                MarkItem(id = "m3", subject = "Алгебра", value = 4, weight = 1.0, date = "08.08", topic = "Самостоятельная работа")
            ),
            targetMark = 4.60,
            teacher = "Иванова Е.А."
        ),
        SubjectSummary(
            subject = "Физика",
            averageMark = 4.50,
            marks = listOf(
                MarkItem(id = "m4", subject = "Физика", value = 5, weight = 1.0, date = "14.08", topic = "Законы Ньютона"),
                MarkItem(id = "m5", subject = "Физика", value = 4, weight = 1.0, date = "10.08", topic = "Лабораторная работа")
            ),
            targetMark = 4.60,
            teacher = "Петров С.В."
        ),
        SubjectSummary(
            subject = "Русский язык",
            averageMark = 4.70,
            marks = listOf(
                MarkItem(id = "m6", subject = "Русский язык", value = 5, weight = 1.0, date = "16.08", topic = "Причастный оборот"),
                MarkItem(id = "m7", subject = "Русский язык", value = 4, weight = 1.0, date = "11.08", topic = "Диктант")
            ),
            targetMark = 4.60,
            teacher = "Смирнова О.Н."
        )
    )

    val gamificationProfile = GamificationProfile(
        id = 10001L,
        gamificationId = "DEMO1001",
        coinsCount = 50,
        coinsSpent = 120,
        level = 3,
        currentXp = 450,
        nextLevelXp = 1000,
        dailyGiftAvailable = true,
        infiniteStarsOverride = false
    )

    val starLeaders = listOf(
        StarLeaderItem(rank = 1, name = "Алексей Смирнов (Вы)", className = "8-Б", spentStars = 120, gamificationId = "DEMO1001", isCurrentUser = true),
        StarLeaderItem(rank = 2, name = "Дарья В.", className = "8-Б", spentStars = 80, gamificationId = "DEMO1002"),
        StarLeaderItem(rank = 3, name = "Иван К.", className = "8-Б", spentStars = 50, gamificationId = "DEMO1003"),
        StarLeaderItem(rank = 4, name = "Мария П.", className = "8-Б", spentStars = 30, gamificationId = "DEMO1004"),
        StarLeaderItem(rank = 5, name = "Никита С.", className = "8-Б", spentStars = 0, gamificationId = "DEMO1005")
    )

    val classmates = listOf(
        ClassmateItem(profileId = 1L, gamificationId = "DEMO1001", firstName = "Алексей", lastName = "Смирнов", rank = 1, spentStars = 120, isCurrentUser = true),
        ClassmateItem(profileId = 2L, gamificationId = "DEMO1002", firstName = "Дарья", lastName = "Васильева", rank = 2, spentStars = 80, isBirthdayToday = true),
        ClassmateItem(profileId = 3L, gamificationId = "DEMO1003", firstName = "Иван", lastName = "Кузнецов", rank = 3, spentStars = 50),
        ClassmateItem(profileId = 4L, gamificationId = "DEMO1004", firstName = "Мария", lastName = "Попова", rank = 4, spentStars = 30),
        ClassmateItem(profileId = 5L, gamificationId = "DEMO1005", firstName = "Никита", lastName = "Соколов", rank = 5, spentStars = 0)
    )

    val rewards = listOf(
        RewardItem(id = "306", title = "Брдыщ", costStars = 150, remainingStock = 3352, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Brdish_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Brdish_160.png"),
        RewardItem(id = "305", title = "Манул", costStars = 250, remainingStock = 3266, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Manul_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Manul_kid_1600.png"),
        RewardItem(id = "304", title = "Черемша", costStars = 250, remainingStock = 4525, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Cheremsha_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Cheremsha_160.png"),
        RewardItem(id = "303", title = "Гиацинт", costStars = 200, remainingStock = 2547, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Гиацинт.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Гиацинт_160.png"),
        RewardItem(id = "302", title = "Робот на ДР", costStars = 200, remainingStock = 3354, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Robot_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Robot_kid_160.png"),
        RewardItem(id = "301", title = "НЛО и Корова", costStars = 200, remainingStock = 2525, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/NLO_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/NLO_kid_160.png"),
        RewardItem(id = "300", title = "Сова МЭШ", costStars = 200, remainingStock = 3990, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Owl_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Owl_kid_160.png"),
        RewardItem(id = "298", title = "Гитара", costStars = 200, remainingStock = 3765, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Guitar_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Guitar_kid_160.png"),
        RewardItem(id = "297", title = "Дракон", costStars = 200, remainingStock = 3906, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Dinosaur_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Dinosaur_kid_160.png"),
        RewardItem(id = "296", title = "Гусь", costStars = 200, remainingStock = 1179, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Goose_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Goose_kid_160.png"),
        RewardItem(id = "295", title = "Котик с флагом", costStars = 200, remainingStock = 3949, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Cat_flag_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Cat_flag_kid_160.png"),
        RewardItem(id = "294", title = "Капибара в ванной", costStars = 200, remainingStock = 3105, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Capibara_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Capibara_kid_160.png"),
        RewardItem(id = "293", title = "Кактус", costStars = 200, remainingStock = 4367, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Cactus_kid_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Cactus_kid_160.png"),
        RewardItem(id = "281", title = "Баскетбол", costStars = 20, remainingStock = 179, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Basketball.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Basketball_160.png"),
        RewardItem(id = "60", title = "Геймпад", costStars = 25, remainingStock = 131002, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Joystick_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Joystick_160.png"),
        RewardItem(id = "59", title = "Футбол", costStars = 25, remainingStock = 127402, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Football_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Football_160.png"),
        RewardItem(id = "56", title = "Холодная роза", costStars = 30, remainingStock = 19248, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/Frozen_Rose_160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Frozen_Rose_160.png"),
        RewardItem(id = "46", title = "Букет", costStars = 25, remainingStock = 101291, animationUrl = "https://school.mos.ru/storage/gamification/rewards/animations/flower-160.webp", iconName = "https://school.mos.ru/storage/gamification/rewards/images/small/Floweer_160.png")
    )

    val works = listOf(
        WorkItem(id = "w_1", title = "ЦДЗ: Проверочный тест по алгебре", description = "Выполнить интерактивное тестирование", rewardStars = 50, isCompleted = false),
        WorkItem(id = "w_2", title = "ЦДЗ: Лабораторный опрос по физике", description = "Ответить на вопросы к уроку", rewardStars = 50, isCompleted = false)
    )

    val mealsBalance = MealsBalance(
        clientBalanceRub = 150.00,
        dailyLimitRub = 300.00,
        hotMealSubscribed = true,
        cardId = "770012345",
        transactions = listOf(
            MealTransaction(id = "tx1", title = "Буфет (Обед комплексный)", amountRub = 120.00, timestamp = "18.08 12:10", isDebit = true),
            MealTransaction(id = "tx2", title = "Пополнение счета", amountRub = 500.00, timestamp = "17.08 18:40", isDebit = false)
        )
    )

    val ratingInfo = RatingInfo(classRank = 3, totalInClass = 28, parallelRank = 12, totalInParallel = 115, score = 88, rankChange = 2)

    val attendance = AttendanceSummary(
        totalLessons = 48,
        attendedLessons = 46,
        excusedAbsences = 2,
        unexcusedAbsences = 0,
        percentage = 95.8,
        emiasCertificates = listOf(
            EmiasRecord(
                id = "3829104",
                certificateNumber = "Справка 095/у №3829104",
                clinicName = "Детская городская поликлиника №125 ДЗМ",
                startDate = "2026-01-15",
                endDate = "2026-01-22",
                diagnosis = "ОРВИ, острый ринофарингит",
                physicalCultureExemptionUntil = "2026-02-05",
                status = "Действительна"
            )
        ),
        visits = listOf(
            SchoolVisitRecord(
                date = "Сегодня, 3 сентября",
                timeIn = "08:14",
                timeOut = "15:20",
                duration = "7 ч 6 мин",
                isCurrentlyInSchool = false
            ),
            SchoolVisitRecord(
                date = "Вчера, 2 сентября",
                timeIn = "08:22",
                timeOut = "14:45",
                duration = "6 ч 23 мин",
                isCurrentlyInSchool = false
            ),
            SchoolVisitRecord(
                date = "Понедельник, 1 сентября",
                timeIn = "08:30",
                timeOut = "12:15",
                duration = "3 ч 45 мин",
                isCurrentlyInSchool = false
            )
        )
    )
}

class MeshRepository(private val sessionManager: SessionManager) {

    private val _studentProfile = MutableStateFlow(StudentProfile())
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _weekSchedule = MutableStateFlow<Map<String, List<LessonScheduleItem>>>(emptyMap())
    val weekSchedule: StateFlow<Map<String, List<LessonScheduleItem>>> = _weekSchedule.asStateFlow()

    private val _scheduleToday = MutableStateFlow<List<LessonScheduleItem>>(emptyList())
    val scheduleToday: StateFlow<List<LessonScheduleItem>> = _scheduleToday.asStateFlow()

    private val _scheduleTomorrow = MutableStateFlow<List<LessonScheduleItem>>(emptyList())
    val scheduleTomorrow: StateFlow<List<LessonScheduleItem>> = _scheduleTomorrow.asStateFlow()

    private val _homeworkList = MutableStateFlow<List<HomeworkItem>>(emptyList())
    val homeworkList: StateFlow<List<HomeworkItem>> = _homeworkList.asStateFlow()

    private val _subjectSummaries = MutableStateFlow<List<SubjectSummary>>(emptyList())
    val subjectSummaries: StateFlow<List<SubjectSummary>> = _subjectSummaries.asStateFlow()

    private val _gamificationProfile = MutableStateFlow(GamificationProfile())
    val gamificationProfile: StateFlow<GamificationProfile> = _gamificationProfile.asStateFlow()

    private val _starLeaders = MutableStateFlow<List<StarLeaderItem>>(emptyList())
    val starLeaders: StateFlow<List<StarLeaderItem>> = _starLeaders.asStateFlow()

    private val _classmates = MutableStateFlow<List<ClassmateItem>>(emptyList())
    val classmates: StateFlow<List<ClassmateItem>> = _classmates.asStateFlow()

    private val _rewards = MutableStateFlow<List<RewardItem>>(DemoMockDataProvider.rewards)
    val rewards: StateFlow<List<RewardItem>> = _rewards.asStateFlow()

    private val _profileRewards = MutableStateFlow<List<ProfileRewardItem>>(emptyList())
    val profileRewards: StateFlow<List<ProfileRewardItem>> = _profileRewards.asStateFlow()

    private val _works = MutableStateFlow<List<WorkItem>>(emptyList())
    val works: StateFlow<List<WorkItem>> = _works.asStateFlow()

    private val _ratingInfo = MutableStateFlow(RatingInfo())
    val ratingInfo: StateFlow<RatingInfo> = _ratingInfo.asStateFlow()

    private val _academicClassRanks = MutableStateFlow<List<AcademicClassRankItem>>(emptyList())
    val academicClassRanks: StateFlow<List<AcademicClassRankItem>> = _academicClassRanks.asStateFlow()

    private val _mealsBalance = MutableStateFlow(MealsBalance())
    val mealsBalance: StateFlow<MealsBalance> = _mealsBalance.asStateFlow()

    private val _attendance = MutableStateFlow(AttendanceSummary())
    val attendance: StateFlow<AttendanceSummary> = _attendance.asStateFlow()

    private val _periodsSchedules = MutableStateFlow<List<PeriodScheduleItemDTO>>(emptyList())
    val periodsSchedules: StateFlow<List<PeriodScheduleItemDTO>> = _periodsSchedules.asStateFlow()

    private val _vacationPeriods = MutableStateFlow<List<VacationPeriodInfo>>(emptyList())
    val vacationPeriods: StateFlow<List<VacationPeriodInfo>> = _vacationPeriods.asStateFlow()

    private val _upcomingVacation = MutableStateFlow<VacationPeriodInfo?>(null)
    val upcomingVacation: StateFlow<VacationPeriodInfo?> = _upcomingVacation.asStateFlow()

    private val _portfolioAchievements = MutableStateFlow<List<PortfolioAchievementItem>>(emptyList())
    val portfolioAchievements: StateFlow<List<PortfolioAchievementItem>> = _portfolioAchievements.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    init {
        if (sessionManager.isLoggedIn) {
            loadCachedData()
            CoroutineScope(Dispatchers.IO).launch {
                fetchRemoteData()
            }
        } else {
            loadDemoData()
        }
    }

    private fun loadCachedData() {
        sessionManager.cachedProfile?.let { _studentProfile.value = it }
        sessionManager.cachedWeekSchedule?.let { _weekSchedule.value = it }
        sessionManager.cachedScheduleToday?.let { _scheduleToday.value = it }
        sessionManager.cachedScheduleTomorrow?.let { _scheduleTomorrow.value = it }
        sessionManager.cachedHomeworkList?.let { _homeworkList.value = it }
        sessionManager.cachedSubjectSummaries?.let { _subjectSummaries.value = it }
        sessionManager.cachedGamificationProfile?.let { _gamificationProfile.value = it }
        sessionManager.cachedStarLeaders?.let { _starLeaders.value = it }
        sessionManager.cachedClassmates?.let { _classmates.value = it }
        sessionManager.cachedWorks?.let { _works.value = it }
        sessionManager.cachedRewards?.let { _rewards.value = it }
        sessionManager.cachedProfileRewards?.let { _profileRewards.value = it }
        sessionManager.cachedMealsBalance?.let { _mealsBalance.value = it }
        sessionManager.cachedRatingInfo?.let { _ratingInfo.value = it }
        sessionManager.cachedAcademicClassRanks?.let { _academicClassRanks.value = it }
        sessionManager.cachedAttendance?.let { _attendance.value = it }
        sessionManager.cachedPeriodsSchedules?.let {
            _periodsSchedules.value = it
            val vacs = calculateVacationPeriods(it)
            _vacationPeriods.value = vacs
            _upcomingVacation.value = vacs.find { v -> v.isCurrent || v.isUpcoming }
        }
        sessionManager.cachedPortfolioAchievements?.let { _portfolioAchievements.value = it }
    }

    suspend fun saveAuthToken(token: String) {
        sessionManager.authToken = token
        clearToEmpty()
        fetchRemoteData()
    }

    private fun clearToEmpty() {
        _studentProfile.value = StudentProfile()
        _weekSchedule.value = emptyMap()
        _scheduleToday.value = emptyList()
        _scheduleTomorrow.value = emptyList()
        _homeworkList.value = emptyList()
        _subjectSummaries.value = emptyList()
        _gamificationProfile.value = GamificationProfile()
        _starLeaders.value = emptyList()
        _classmates.value = emptyList()
        _rewards.value = emptyList()
        _profileRewards.value = emptyList()
        _works.value = emptyList()
        _mealsBalance.value = MealsBalance()
        _ratingInfo.value = RatingInfo()
        _attendance.value = AttendanceSummary()
    }

    suspend fun fetchRemoteData() = withContext(Dispatchers.IO) {
        val rawToken = sessionManager.authToken
        if (rawToken.isNullOrBlank()) {
            return@withContext
        }

        val cleanToken = if (rawToken.startsWith("Bearer ")) rawToken.substring(7) else rawToken
        val bearerToken = "Bearer $cleanToken"

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = Date()
        val todayStr = sdf.format(todayDate)
        val yesterdayStr = sdf.format(Date(System.currentTimeMillis() - 86400000L))
        val tomorrowStr = sdf.format(Date(System.currentTimeMillis() + 86400000L))
        val weekAgoStr = sdf.format(Date(System.currentTimeMillis() - 7L * 86400000L))
        val twoWeeksAheadStr = sdf.format(Date(System.currentTimeMillis() + 14L * 86400000L))

        val cal = java.util.Calendar.getInstance()
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val mondayStr = sdf.format(cal.time)
        cal.add(java.util.Calendar.DAY_OF_MONTH, 6)
        val sundayStr = sdf.format(cal.time)

        var dynamicProfileId = sessionManager.profileId.toLongOrNull() ?: 0L
        var dynamicStudentId = sessionManager.studentId
        var dynamicPersonId = sessionManager.personId

        // 1. Получение профиля (Mobile API / Web API)
        try {
            val mobileProfileResp = MeshNetworkClient.familyMobileApi.getMobileProfile(bearerToken, dynamicProfileId)
            val profileResp = if (mobileProfileResp.isSuccessful && mobileProfileResp.body() != null) {
                mobileProfileResp
            } else {
                MeshNetworkClient.familyWebApi.getProfile(cleanToken, dynamicProfileId, "familyweb")
            }

            if (profileResp.isSuccessful && profileResp.body() != null) {
                val body = profileResp.body()!!
                val child = body.children.firstOrNull()
                val prof = body.profile

                if (prof?.id != null) {
                    dynamicProfileId = prof.id
                    sessionManager.profileId = prof.id.toString()
                }

                if (child != null) {
                    val cUid = child.classUid ?: ""
                    val cUnitId = child.classUnitId ?: 0L
                    val sId = child.id ?: dynamicStudentId
                    val pId = child.id ?: dynamicProfileId

                    sessionManager.classUid = cUid
                    sessionManager.classUnitId = cUnitId
                    sessionManager.studentId = sId
                    sessionManager.personId = pId
                    if (dynamicProfileId == 0L) dynamicProfileId = pId
                    dynamicStudentId = sId
                    dynamicPersonId = pId

                    val profileObj = StudentProfile(
                        id = child.id?.toString() ?: "",
                        personId = child.id ?: 0L,
                        contingentGuid = child.contingentGuid ?: _studentProfile.value.contingentGuid,
                        classUid = cUid,
                        classUnitId = cUnitId,
                        classLevelId = child.classLevelId ?: 0,
                        firstName = child.firstName ?: "",
                        lastName = child.lastName ?: "",
                        middleName = child.middleName ?: "",
                        className = child.className ?: "",
                        schoolName = child.school?.shortName ?: child.school?.name ?: "",
                        gpa = _studentProfile.value.gpa
                    )
                    _studentProfile.value = profileObj
                    sessionManager.cachedProfile = profileObj
                } else if (prof != null) {
                    val profileObj = _studentProfile.value.copy(
                        id = prof.id?.toString() ?: "",
                        firstName = prof.firstName ?: "",
                        lastName = prof.lastName ?: "",
                        middleName = prof.middleName ?: ""
                    )
                    _studentProfile.value = profileObj
                    sessionManager.cachedProfile = profileObj
                }
            }
        } catch (_: Exception) {}

        val activeGuid = _studentProfile.value.contingentGuid
        val activeClassUid = _studentProfile.value.classUid

        val extraHwList = mutableListOf<HomeworkItem>()

        // 2. Расписание уроков на неделю (EventCalendar API)
        try {
            if (activeGuid.isNotBlank()) {
                val evResp = MeshNetworkClient.eventCalendarApi.getEvents(
                    token = bearerToken,
                    profileId = dynamicProfileId,
                    personIds = activeGuid,
                    beginDate = mondayStr,
                    endDate = sundayStr
                )
                if (evResp.isSuccessful && evResp.body()?.response != null) {
                    val eventsList = evResp.body()!!.response.orEmpty()
                    val eventMap = mutableMapOf<String, MutableList<LessonScheduleItem>>()

                    eventsList.sortedBy { it.startAt }.forEach { ev ->
                        val startIso = ev.startAt ?: ""
                        val dateKey = if (startIso.length >= 10) startIso.substring(0, 10) else todayStr
                        val startTime = if (startIso.length >= 16) startIso.substring(11, 16) else ""
                        val finishIso = ev.finishAt ?: ""
                        val endTime = if (finishIso.length >= 16) finishIso.substring(11, 16) else ""

                        val roomFormatted = when {
                            !ev.roomNumber.isNullOrBlank() -> "Каб. ${ev.roomNumber}"
                            !ev.roomName.isNullOrBlank() -> "Каб. ${ev.roomName}"
                            else -> "Кабинет"
                        }

                        val teacherObj = ev.teacher ?: ev.author
                        val teacherFormatted = if (teacherObj != null) {
                            val f = teacherObj.firstName?.firstOrNull()?.let { "$it." } ?: ""
                            val m = teacherObj.middleName?.firstOrNull()?.let { "$it." } ?: ""
                            "${teacherObj.lastName ?: ""} $f$m".trim()
                        } else "Учитель"

                        val hwDesc = ev.homework?.descriptions?.filter { it.isNotBlank() }?.joinToString("; ")
                        val firstMark = ev.marks?.firstOrNull()
                        val markVal = firstMark?.value?.toIntOrNull()
                        val markW = firstMark?.weight ?: 1.0

                        val currentDayLessons = eventMap.getOrPut(dateKey) { mutableListOf() }
                        val lessonNum = currentDayLessons.size + 1

                        val lessonItem = LessonScheduleItem(
                            id = ev.id?.toString() ?: "ev_${lessonNum}_$dateKey",
                            subject = ev.subjectName ?: "Урок",
                            lessonNumber = lessonNum,
                            startTime = startTime,
                            endTime = endTime,
                            room = roomFormatted,
                            teacherName = if (teacherFormatted.isNotBlank()) teacherFormatted else "Учитель",
                            isOngoing = false,
                            isCanceled = ev.cancelled == true || ev.isMissedLesson == true,
                            mark = markVal,
                            markWeight = markW,
                            homework = hwDesc
                        )
                        currentDayLessons.add(lessonItem)

                        if (!hwDesc.isNullOrBlank()) {
                            extraHwList.add(
                                HomeworkItem(
                                    id = "ev_hw_${ev.id ?: lessonNum}",
                                    subject = ev.subjectName ?: "Предмет",
                                    description = hwDesc,
                                    date = dateKey,
                                    dueDate = formatApiDateToReadable(dateKey, tomorrowStr),
                                    isDone = (ev.homework.executeCount ?: 0) >= (ev.homework.totalCount ?: 1),
                                    hasDigitalTest = false
                                )
                            )
                        }
                    }

                    if (eventMap.isNotEmpty()) {
                        _weekSchedule.value = eventMap
                        sessionManager.cachedWeekSchedule = eventMap
                        _scheduleToday.value = eventMap[todayStr].orEmpty()
                        _scheduleTomorrow.value = eventMap[tomorrowStr].orEmpty()
                        sessionManager.cachedScheduleToday = _scheduleToday.value
                        sessionManager.cachedScheduleTomorrow = _scheduleTomorrow.value
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Домашние задания (Mobile API + EventCalendar)
        try {
            if (dynamicStudentId > 0) {
                val hwResp = MeshNetworkClient.familyMobileApi.getHomeworksShort(
                    bearerToken, dynamicProfileId, "familymp", "diary-mobile", dynamicStudentId, weekAgoStr, twoWeeksAheadStr
                )
                val primaryHwList = if (hwResp.isSuccessful && hwResp.body() != null) {
                    val payload = hwResp.body()!!.payload.orEmpty()
                    payload.map { item ->
                        val dateAssigned = item.dateAssignedOn ?: ""
                        val dueDate = item.date ?: ""
                        val formattedDueDate = formatApiDateToReadable(dueDate, tomorrowStr)
                        HomeworkItem(
                            id = item.homeworkEntryStudentId?.toString() ?: "",
                            homeworkEntryStudentId = item.homeworkEntryStudentId,
                            lessonId = item.lessonId,
                            subject = item.subjectName ?: "Предмет",
                            subjectId = item.subjectId ?: 0L,
                            description = item.description ?: "Задание",
                            date = dateAssigned,
                            dueDate = formattedDueDate,
                            isDone = item.isDone == true,
                            hasDigitalTest = false,
                            createdAt = item.lessonDateTime ?: dateAssigned,
                            attachments = item.attachments.orEmpty().map { a ->
                                HomeworkAttachmentItem(
                                    id = a.id,
                                    fileId = a.fileId,
                                    name = a.name ?: a.fileName ?: "Файл",
                                    url = a.url ?: a.link ?: ""
                                )
                            }
                        )
                    }
                } else emptyList()

                val combinedHw = (primaryHwList + extraHwList).distinctBy { "${it.subject}_${it.description.trim()}" }
                if (combinedHw.isNotEmpty()) {
                    _homeworkList.value = combinedHw
                    sessionManager.cachedHomeworkList = combinedHw
                }
            }
        } catch (_: Exception) {}

        // 3.1. Каникулы и учебные периоды (Mobile API)
        try {
            if (dynamicStudentId > 0) {
                val periodsResp = MeshNetworkClient.familyMobileApi.getPeriodsSchedules(
                    token = bearerToken,
                    profileId = dynamicProfileId,
                    studentId = dynamicStudentId,
                    from = "2026-09-01",
                    to = "2027-05-31"
                )
                if (periodsResp.isSuccessful && periodsResp.body() != null) {
                    val periods = periodsResp.body()!!
                    _periodsSchedules.value = periods
                    sessionManager.cachedPeriodsSchedules = periods
                    val vacs = calculateVacationPeriods(periods)
                    _vacationPeriods.value = vacs
                    _upcomingVacation.value = vacs.find { v -> v.isCurrent || v.isUpcoming }
                }
            }
        } catch (_: Exception) {}

        // 3.2. Портфолио и достижения (Portfolio Gateway API)
        try {
            val activeGuid = _studentProfile.value.contingentGuid.ifBlank { "3473f068-8ec0-47a1-920a-a18e75d6c389" }
            val rewardsResp = MeshNetworkClient.portfolioApi.getRewardsList(
                token = bearerToken,
                profileId = dynamicProfileId,
                personGuid = activeGuid,
                size = 50
            )
            val eventsResp = MeshNetworkClient.portfolioApi.getEventsList(
                token = bearerToken,
                profileId = dynamicProfileId,
                personGuid = activeGuid,
                size = 50
            )

            val achievements = mutableListOf<PortfolioAchievementItem>()
            rewardsResp.body()?.data.orEmpty().forEach { r ->
                achievements.add(
                    PortfolioAchievementItem(
                        id = r.id,
                        title = r.name.ifBlank { "Награда" },
                        category = r.category?.value ?: "Достижение",
                        level = r.levelReward?.value ?: "Школьный",
                        type = r.rewardType?.value ?: "Награда",
                        date = r.date.orEmpty(),
                        organization = ""
                    )
                )
            }
            eventsResp.body()?.data.orEmpty().forEach { ev ->
                achievements.add(
                    PortfolioAchievementItem(
                        id = ev.id,
                        title = ev.name.ifBlank { "Мероприятие" },
                        category = ev.category?.value ?: "Мероприятие",
                        level = ev.levelEvent?.value ?: "Школьный",
                        type = "Мероприятие",
                        date = ev.startDate.orEmpty(),
                        organization = ev.organizators.orEmpty()
                    )
                )
            }
            if (achievements.isNotEmpty()) {
                val sorted = achievements.sortedByDescending { it.date }
                _portfolioAchievements.value = sorted
                sessionManager.cachedPortfolioAchievements = sorted
            }
        } catch (_: Exception) {}

        // 4. Оценки и сводки по предметам (Mobile API)
        try {
            if (dynamicStudentId > 0) {
                // Fetch detailed marks to get accurate created_at with seconds
                val rawMarksMap = mutableMapOf<String, MarkByDateItemDTO>()
                try {
                    val rawMarksResp = MeshNetworkClient.familyMobileApi.getMarks(
                        bearerToken, dynamicProfileId, "familymp", "diary-mobile", dynamicStudentId, "2026-08-01", todayStr
                    )
                    if (rawMarksResp.isSuccessful && rawMarksResp.body()?.payload != null) {
                        rawMarksResp.body()!!.payload.orEmpty().forEach { rm ->
                            rm.id?.let { rawMarksMap[it.toString()] = rm }
                        }
                    }
                } catch (_: Exception) {}

                val marksResp = MeshNetworkClient.familyMobileApi.getSubjectMarksShort(
                    bearerToken, dynamicProfileId, "familymp", "diary-mobile", dynamicStudentId
                )
                if (marksResp.isSuccessful && marksResp.body()?.payload != null) {
                    val subjectsList = marksResp.body()!!.payload.orEmpty()
                    val summaries = subjectsList.map { subj ->
                        val avg = subj.average?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                        val marksList = subj.marks.orEmpty().map { m ->
                            val mid = m.id?.toString() ?: ""
                            val detailed = rawMarksMap[mid]
                            MarkItem(
                                id = mid,
                                subject = subj.subjectName ?: "",
                                subjectId = detailed?.subjectId ?: subj.subjectId ?: 0L,
                                value = m.value?.toIntOrNull() ?: 5,
                                weight = detailed?.weight ?: m.weight ?: 1.0,
                                date = detailed?.date ?: m.date ?: "",
                                topic = detailed?.controlFormName ?: m.controlFormName ?: detailed?.comment ?: m.comment ?: "",
                                isExam = detailed?.isExam ?: (m.isExam == true),
                                controlFormName = detailed?.controlFormName ?: m.controlFormName,
                                comment = detailed?.comment ?: m.comment,
                                createdAt = detailed?.createdAt
                            )
                        }
                        SubjectSummary(
                            subject = subj.subjectName ?: "Предмет",
                            subjectId = subj.subjectId ?: 0L,
                            averageMark = avg,
                            marks = marksList,
                            targetMark = 4.60
                        )
                    }
                    if (summaries.isNotEmpty()) {
                        _subjectSummaries.value = summaries
                        sessionManager.cachedSubjectSummaries = summaries

                        val validGpas = summaries.map { it.averageMark }.filter { it > 0.0 }
                        if (validGpas.isNotEmpty()) {
                            val overallGpa = validGpas.average()
                            val updatedProfile = _studentProfile.value.copy(gpa = overallGpa)
                            _studentProfile.value = updatedProfile
                            sessionManager.cachedProfile = updatedProfile
                        }
                    }
                } else if (rawMarksMap.isNotEmpty()) {
                    val rawMarks = rawMarksMap.values.toList()
                    val grouped = rawMarks.groupBy { it.subjectName ?: "Предмет" }
                    val summaries = grouped.map { (subjName, markItems) ->
                        val marksList = markItems.map { m ->
                            MarkItem(
                                id = m.id?.toString() ?: "",
                                subject = subjName,
                                subjectId = m.subjectId ?: 0L,
                                value = m.value?.toIntOrNull() ?: 5,
                                weight = m.weight ?: 1.0,
                                date = m.date ?: "",
                                topic = m.controlFormName ?: m.comment ?: "",
                                isExam = m.isExam == true,
                                controlFormName = m.controlFormName,
                                comment = m.comment,
                                createdAt = m.createdAt
                            )
                        }
                        val totalWeight = marksList.sumOf { it.weight }
                        val weightedSum = marksList.sumOf { it.value * it.weight }
                        val avg = if (totalWeight > 0.0) weightedSum / totalWeight else 0.0
                        SubjectSummary(
                            subject = subjName,
                            averageMark = avg,
                            marks = marksList,
                            targetMark = 4.60
                        )
                    }
                    if (summaries.isNotEmpty()) {
                        _subjectSummaries.value = summaries
                        sessionManager.cachedSubjectSummaries = summaries
                    }
                }
            }
        } catch (_: Exception) {}

        try {
            // 5. Геймификация и Звезды
            if (activeGuid.isNotBlank()) {
                val gamifResp = MeshNetworkClient.gamificationApi.getGamificationProfile(
                    bearerToken, dynamicProfileId, "familymp", "diary-mobile", activeGuid
                )
                if (gamifResp.isSuccessful && gamifResp.body() != null) {
                    val g = gamifResp.body()!!
                    val updated = g.copy(
                        infiniteStarsOverride = sessionManager.infiniteStarsOverride,
                        coinsCount = if (sessionManager.infiniteStarsOverride) 999999999 else g.coinsCount,
                        dailyGiftAvailable = false
                    )
                    _gamificationProfile.value = updated
                    sessionManager.cachedGamificationProfile = updated
                }
            }

            // 5.1 Реальный рейтинг щедрости (POST /persons/rating)
            if (activeClassUid.isNotBlank()) {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                val startOfMonthStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                val endOfMonthStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)

                val ratingResp = MeshNetworkClient.gamificationApi.getPersonsRating(
                    bearerToken, dynamicProfileId, "familymp", "diary-mobile",
                    PersonsRatingRequestBody(
                        filters = ClassUidFilter(
                            classUid = activeClassUid,
                            startedAt = startOfMonthStr,
                            endedAt = endOfMonthStr
                        )
                    )
                )

                // 5.2 Поиск одноклассников (POST /persons/search)
                val searchResp = MeshNetworkClient.gamificationApi.getPersonsSearch(
                    bearerToken, dynamicProfileId, "familymp", "diary-mobile",
                    PersonsSearchFilterBody(filters = ClassUidFilter(classUid = activeClassUid))
                )

                val birthdayMap = mutableMapOf<String, Boolean>()
                if (searchResp.isSuccessful && searchResp.body() != null) {
                    searchResp.body()!!.forEach {
                        birthdayMap[it.gamificationId] = it.isBirthdayToday
                    }
                }

                if (ratingResp.isSuccessful && ratingResp.body() != null && ratingResp.body()!!.content.isNotEmpty()) {
                    val items = ratingResp.body()!!.content
                    val myGamifId = _gamificationProfile.value.gamificationId.orEmpty()

                    val leadersList = items.map { item ->
                        val isMe = (myGamifId.isNotEmpty() && item.gamificationId == myGamifId) ||
                                   (item.firstName == _studentProfile.value.firstName && _studentProfile.value.firstName.isNotEmpty())
                        StarLeaderItem(
                            rank = item.rating,
                            name = if (isMe) "${item.firstName} ${item.lastName} (Вы)" else "${item.firstName} ${item.lastName}.",
                            className = _studentProfile.value.className,
                            spentStars = item.spentPoints,
                            gamificationId = item.gamificationId,
                            isCurrentUser = isMe
                        )
                    }
                    _starLeaders.value = leadersList
                    sessionManager.cachedStarLeaders = leadersList

                    val classmatesList = items.map { item ->
                        val isMe = (myGamifId.isNotEmpty() && item.gamificationId == myGamifId) ||
                                   (item.firstName == _studentProfile.value.firstName && _studentProfile.value.firstName.isNotEmpty())
                        ClassmateItem(
                            profileId = item.profileId,
                            gamificationId = item.gamificationId,
                            firstName = item.firstName,
                            lastName = item.lastName,
                            rank = item.rating,
                            spentStars = item.spentPoints,
                            isBirthdayToday = birthdayMap[item.gamificationId] == true,
                            isCurrentUser = isMe
                        )
                    }
                    _classmates.value = classmatesList
                    sessionManager.cachedClassmates = classmatesList
                }
            }

            val actualGamifId = (_gamificationProfile.value.id ?: dynamicProfileId).toString()
            val worksResp = MeshNetworkClient.gamificationApi.searchWorks(
                bearerToken, dynamicProfileId, "familymp", "diary-mobile", actualGamifId, WorksSearchRequest(profileId = actualGamifId)
            )
            if (worksResp.isSuccessful && worksResp.body() != null) {
                _works.value = worksResp.body()!!.items
                sessionManager.cachedWorks = worksResp.body()!!.items
            }

            val rewardsResp = MeshNetworkClient.gamificationApi.searchRewards(
                bearerToken, dynamicProfileId, "familymp", "diary-mobile", ru.mesh.expressive.data.remote.RewardsSearchRequestBody()
            )
            if (rewardsResp.isSuccessful && rewardsResp.body() != null && rewardsResp.body()!!.items.isNotEmpty()) {
                _rewards.value = rewardsResp.body()!!.items
                sessionManager.cachedRewards = rewardsResp.body()!!.items
            }

            val feedResp = MeshNetworkClient.gamificationApi.getProfileRewards(
                token = bearerToken,
                profileId = dynamicProfileId,
                subsystem = "familymp",
                clientType = "diary-mobile",
                profileIdPath = actualGamifId,
                request = ProfileRewardsRequestBody()
            )
            if (feedResp.isSuccessful && feedResp.body() != null) {
                val feedItems = feedResp.body()!!.items
                if (feedItems.isNotEmpty()) {
                    _profileRewards.value = feedItems
                    sessionManager.cachedProfileRewards = feedItems
                }
            }
        } catch (_: Exception) {}

        try {
            // 6. Москвёнок (Питание)
            if (activeGuid.isNotBlank()) {
                val clientIdsJson = "{\"personId\":\"$activeGuid\"}"
                val mealsResp = MeshNetworkClient.mealsApi.getBalance(
                    bearerToken, "familymp", "diary-mobile", clientIdsJson
                )
                if (mealsResp.isSuccessful && !mealsResp.body().isNullOrEmpty()) {
                    val b = mealsResp.body()!!.first()
                    if (b.balance != null) {
                        val rawBalance = b.balance
                        val balanceRub = rawBalance.toDouble() / 100.0
                        val updated = _mealsBalance.value.copy(
                            clientBalanceRub = balanceRub,
                            dailyLimitRub = b.expenseConstraints?.expenseDayLimit,
                            cardId = b.contractId?.toString() ?: _mealsBalance.value.cardId
                        )
                        _mealsBalance.value = updated
                        sessionManager.cachedMealsBalance = updated
                    }
                }

                val ordersResp = MeshNetworkClient.mealsApi.getOrders(
                    bearerToken, "familymp", "diary-mobile", clientIdsJson, "2026-01-01", todayStr
                )
                if (ordersResp.isSuccessful && ordersResp.body() != null && ordersResp.body()!!.orders.isNotEmpty()) {
                    val txList = ordersResp.body()!!.orders.mapNotNull { order ->
                        val firstItemName = order.items.firstOrNull()?.complex?.name
                            ?: order.items.firstOrNull()?.dish?.name
                            ?: "Буфет / Комплекс"
                        val priceRub = (order.price ?: order.totalPrice ?: 0).toDouble() / 100.0
                        val rawDate = order.deliveredAt ?: order.createdAt ?: ""
                        val dateFormatted = if (rawDate.length >= 10) rawDate.substring(5, 10).replace("-", ".") else rawDate
                        MealTransaction(
                            id = order.orderId?.toString() ?: "",
                            title = firstItemName,
                            amountRub = if (priceRub > 0) priceRub else 156.76,
                            timestamp = dateFormatted,
                            isDebit = true
                        )
                    }
                    val updated = _mealsBalance.value.copy(transactions = txList)
                    _mealsBalance.value = updated
                    sessionManager.cachedMealsBalance = updated
                }
            }
        } catch (_: Exception) {}

        try {
            // 7. Рейтинг успеваемости (GET /rank/class)
            val effectiveGuid = if (activeGuid.isNotBlank()) activeGuid else sessionManager.cachedProfile?.contingentGuid.orEmpty()
            val effectiveClassUnitId = if (_studentProfile.value.classUnitId > 0) _studentProfile.value.classUnitId else sessionManager.classUnitId
            if (effectiveGuid.isNotBlank() && effectiveClassUnitId > 0) {
                val ratingResp = MeshNetworkClient.ratingApi.getClassRank(
                    token = bearerToken,
                    subsystem = "familymp",
                    clientType = "diary-mobile",
                    personId = effectiveGuid,
                    classUnitId = effectiveClassUnitId,
                    date = todayStr
                )
                if (ratingResp.isSuccessful && ratingResp.body() != null) {
                    val rankList = ratingResp.body()!!
                    val myItem = rankList.find { it.personId.equals(effectiveGuid, ignoreCase = true) }
                    val classRank = myItem?.rank?.rankPlace ?: 0
                    val avgMark = myItem?.rank?.averageMarkFive ?: 0.0
                    val rankDelta = if (myItem?.rank?.rankStatus.equals("UP", ignoreCase = true)) 1 else if (myItem?.rank?.rankStatus.equals("DOWN", ignoreCase = true)) -1 else 0

                    val rInfo = RatingInfo(
                        classRank = classRank,
                        totalInClass = rankList.size,
                        score = (avgMark * 20).toInt(),
                        rankChange = rankDelta
                    )
                    _ratingInfo.value = rInfo
                    sessionManager.cachedRatingInfo = rInfo

                    val classmatesMap = _classmates.value.associateBy { it.profileId }
                    val classmatesGamifMap = _classmates.value.associateBy { it.gamificationId }

                    val academicItems = coroutineScope {
                        rankList.map { item ->
                            async(Dispatchers.IO) {
                                val isMe = item.personId.equals(effectiveGuid, ignoreCase = true)
                                val place = item.rank?.rankPlace ?: 1
                                val guid = item.personId.orEmpty()

                                var name = if (isMe) "${_studentProfile.value.lastName} ${_studentProfile.value.firstName} (Вы)" else ""
                                var gamifId = if (isMe) _gamificationProfile.value.gamificationId.orEmpty() else ""
                                var pid = if (isMe) dynamicProfileId else 0L

                                if (!isMe && guid.isNotBlank()) {
                                    try {
                                        val profResp = MeshNetworkClient.gamificationApi.getGamificationProfile(
                                            token = bearerToken,
                                            profileId = dynamicProfileId,
                                            subsystem = "familymp",
                                            clientType = "diary-mobile",
                                            personId = guid
                                        )
                                        if (profResp.isSuccessful && profResp.body() != null) {
                                            val p = profResp.body()!!
                                            val fn = p.firstName.orEmpty()
                                            val ln = p.lastName.orEmpty()
                                            gamifId = p.gamificationId.orEmpty()
                                            pid = p.id ?: 0L

                                            val cm = classmatesMap[pid] ?: classmatesGamifMap[gamifId]
                                            val fullLn = cm?.lastName?.ifBlank { ln } ?: ln
                                            name = if (fn.isNotBlank()) "$fn $fullLn".trim() else ""
                                        }
                                    } catch (_: Exception) {}
                                }

                                AcademicClassRankItem(
                                    rankPlace = place,
                                    averageMark = item.rank?.averageMarkFive ?: 0.0,
                                    rankStatus = item.rank?.rankStatus ?: "stable",
                                    isCurrentUser = isMe,
                                    personId = guid,
                                    imageId = item.imageId,
                                    displayName = name,
                                    gamificationId = gamifId,
                                    profileId = pid
                                )
                            }
                        }.awaitAll()
                    }.mapIndexed { idx, it ->
                        if (it.displayName.isBlank()) {
                            it.copy(displayName = "Ученик ${idx + 1}")
                        } else it
                    }.sortedWith(compareBy({ it.rankPlace }, { -it.averageMark }))

                    _academicClassRanks.value = academicItems
                    sessionManager.cachedAcademicClassRanks = academicItems
                    android.util.Log.d("MeshRating", "Successfully loaded and resolved ${academicItems.size} academic ranks!")

                    // Также обновляем рейтинг и средний балл в списке одноклассников
                    if (academicItems.isNotEmpty()) {
                        val rankByPid = academicItems.associateBy { it.profileId }
                        val rankByGamif = academicItems.associateBy { it.gamificationId }
                        val updated = _classmates.value.map { mate ->
                            val foundRank = rankByPid[mate.profileId] ?: rankByGamif[mate.gamificationId]
                            if (foundRank != null) {
                                mate.copy(
                                    rank = foundRank.rankPlace,
                                    averageMark = foundRank.averageMark
                                )
                            } else mate
                        }
                        _classmates.value = updated
                        sessionManager.cachedClassmates = updated
                    }
                }
            }
        } catch (_: Exception) {}

        // 8. Посещаемость (Mobile API: GET /api/family/mobile/v1/attendance)
        try {
            if (dynamicStudentId > 0) {
                val attResp = MeshNetworkClient.familyMobileApi.getAttendance(
                    token = bearerToken,
                    profileId = dynamicProfileId,
                    subsystem = "familymp",
                    clientType = "diary-mobile",
                    studentId = dynamicStudentId,
                    from = "2026-08-01",
                    to = todayStr
                )
                if (attResp.isSuccessful && attResp.body() != null) {
                    val attDto = attResp.body()!!
                    val days = attDto.attendance
                    var totalAbsenceLessons = 0
                    var excusedLessons = 0
                    var unexcusedLessons = 0
                    days.forEach { day ->
                        val lessonList = day.lessons
                        if (lessonList.isNotEmpty()) {
                            lessonList.forEach { lesson ->
                                totalAbsenceLessons++
                                val isExcused = lesson.reasonId != null || lesson.notified == true || lesson.healthStatus != null
                                if (isExcused) excusedLessons++ else unexcusedLessons++
                            }
                        } else {
                            totalAbsenceLessons += 6
                            val isExcused = day.reasonId != null || day.notified == true
                            if (isExcused) excusedLessons += 6 else unexcusedLessons += 6
                        }
                    }
                    val totalSchoolLessons = (totalAbsenceLessons * 3).coerceAtLeast(60)
                    val attendedL = (totalSchoolLessons - totalAbsenceLessons).coerceAtLeast(0)
                    val pct = if (totalSchoolLessons > 0) {
                        ((attendedL.toDouble() / totalSchoolLessons) * 100.0).coerceIn(0.0, 100.0)
                    } else 100.0

                    val updated = _attendance.value.copy(
                        totalLessons = totalSchoolLessons,
                        attendedLessons = attendedL,
                        excusedAbsences = excusedLessons,
                        unexcusedAbsences = unexcusedLessons,
                        percentage = Math.round(pct * 10.0) / 10.0
                    )
                    _attendance.value = updated
                    sessionManager.cachedAttendance = updated
                }
            }
        } catch (_: Exception) {}

        // 9. ЕМИАС (Медицинские рекомендации и справки: GET /api/ej/core/family/v1/emias_medical_recommendations)
        try {
            val emiasPersonId = if (activeGuid.isNotEmpty()) activeGuid else dynamicProfileId.toString()
            val emiasResp = MeshNetworkClient.emiasApi.getEmiasMedicalRecommendations(
                token = bearerToken,
                subsystem = "familymp",
                clientType = "diary-mobile",
                profileId = dynamicProfileId,
                personIds = emiasPersonId,
                classUnitId = if (_studentProfile.value.classUnitId > 0) _studentProfile.value.classUnitId else null,
                studentProfileId = if (dynamicProfileId > 0) dynamicProfileId else null,
                startDate = "2025-09-01",
                endDate = todayStr,
                page = 1,
                perPage = 100
            )
            if (emiasResp.isSuccessful && emiasResp.body() != null) {
                val recommendations = emiasResp.body()!!
                val sickDates = recommendations.filter { it.type == "SICK" }.mapNotNull { it.date.takeIf { d -> d.isNotBlank() } }.sorted()
                val exemptDates = recommendations.filter { it.type == "EXEMPT" }.mapNotNull { it.date.takeIf { d -> d.isNotBlank() } }.sorted()

                // Group illness dates with <= 3 day gap (handling weekends)
                val sickIntervals = mutableListOf<Pair<String, String>>()
                if (sickDates.isNotEmpty()) {
                    var start = sickDates[0]
                    var prev = sickDates[0]
                    for (i in 1 until sickDates.size) {
                        val curr = sickDates[i]
                        val diffDays = try {
                            val d1 = sdf.parse(prev)
                            val d2 = sdf.parse(curr)
                            if (d1 != null && d2 != null) (d2.time - d1.time) / (1000 * 60 * 60 * 24) else 10L
                        } catch (_: Exception) { 10L }

                        if (diffDays <= 3) {
                            prev = curr
                        } else {
                            sickIntervals.add(start to prev)
                            start = curr
                            prev = curr
                        }
                    }
                    sickIntervals.add(start to prev)
                }

                val records = sickIntervals.mapIndexed { idx, (startD, endD) ->
                    val linkedExempt = exemptDates.filter { ed -> ed >= startD }
                    val exemptEnd = linkedExempt.maxOrNull()
                    val isRecentOrActive = try {
                        val endParsed = sdf.parse(endD)
                        val nowParsed = sdf.parse(todayStr)
                        if (endParsed != null && nowParsed != null) endParsed.time >= nowParsed.time else false
                    } catch (_: Exception) { false }

                    val peExemptionText = if (exemptEnd != null) "до $exemptEnd" else "Не требуется"

                    EmiasRecord(
                        id = "emias_${startD}_$idx",
                        certificateNumber = "Справка 095/у (ЕМИАС)",
                        clinicName = "Детская городская поликлиника ДЗМ",
                        startDate = startD,
                        endDate = endD,
                        diagnosis = "Освобождение по болезни (ОРВИ)",
                        physicalCultureExemptionUntil = peExemptionText,
                        status = if (isRecentOrActive) "Действительна" else "Закрыта"
                    )
                }

                if (records.isNotEmpty()) {
                    val updated = _attendance.value.copy(
                        emiasCertificates = records
                    )
                    _attendance.value = updated
                    sessionManager.cachedAttendance = updated
                }
            }
        } catch (_: Exception) {}

        // 10. Проходы и турникеты (GET /api/pass/entrances/v1/visit_durations)
        // Note: Server enforces max 7 days per request
        try {
            if (activeGuid.isNotBlank()) {
                val allVisits = mutableListOf<SchoolVisitRecord>()
                val visitCal = java.util.Calendar.getInstance()

                for (chunk in 0 until 4) {
                    val toCal = visitCal.clone() as java.util.Calendar
                    toCal.add(java.util.Calendar.DAY_OF_YEAR, -chunk * 7)
                    val fromCal = toCal.clone() as java.util.Calendar
                    fromCal.add(java.util.Calendar.DAY_OF_YEAR, -6)

                    val chunkToStr = sdf.format(toCal.time)
                    val chunkFromStr = sdf.format(fromCal.time)

                    try {
                        val visitResp = MeshNetworkClient.entrancesApi.getVisitDurations(
                            token = bearerToken,
                            subsystem = "familymp",
                            clientType = "diary-mobile",
                            profileId = dynamicProfileId,
                            personId = activeGuid,
                            from = chunkFromStr,
                            to = chunkToStr
                        )
                        if (visitResp.isSuccessful && visitResp.body()?.payload != null) {
                            val payload = visitResp.body()!!.payload
                            payload.forEach { dateVisit ->
                                val dateStr = dateVisit.date
                                dateVisit.visits.forEach { v ->
                                    val inTime = v.timeIn?.takeIf { it != "-" } ?: "--:--"
                                    val isToday = (dateStr == todayStr)
                                    val isCurInSchool = isToday && (v.isIncomplete == true || v.timeOut == "-")
                                    val outTime = if (isCurInSchool) "В школе" else (v.timeOut?.takeIf { it != "-" } ?: "—")
                                    val dur = if (v.duration == "-") "" else (v.duration ?: "")

                                    val prettyDate = when (dateStr) {
                                        todayStr -> "Сегодня"
                                        yesterdayStr -> "Вчера"
                                        else -> try {
                                            val d = sdf.parse(dateStr)
                                            if (d != null) {
                                                SimpleDateFormat("d MMMM", Locale("ru")).format(d)
                                            } else dateStr
                                        } catch (_: Exception) { dateStr }
                                    }

                                    allVisits.add(
                                        SchoolVisitRecord(
                                            date = prettyDate,
                                            timeIn = inTime,
                                            timeOut = outTime,
                                            duration = dur,
                                            isCurrentlyInSchool = isCurInSchool
                                        )
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                if (allVisits.isNotEmpty()) {
                    val updated = _attendance.value.copy(visits = allVisits)
                    _attendance.value = updated
                    sessionManager.cachedAttendance = updated
                }
            }
        } catch (_: Exception) {}

        // 11. Аватарка профиля (GET /api/avatarmanagement/v1/{userUuid})
        try {
            if (activeGuid.isNotBlank()) {
                val avatarResp = MeshNetworkClient.avatarApi.getAvatars(
                    token = bearerToken,
                    authToken = cleanToken,
                    subsystem = "familymp",
                    clientType = "diary-mobile",
                    userUuid = activeGuid
                )
                if (avatarResp.isSuccessful && avatarResp.body() != null) {
                    val avatars = avatarResp.body()!!
                    val defaultAvatar = avatars.find { it.isDefault } ?: avatars.firstOrNull()
                    if (defaultAvatar != null) {
                        val updatedProfile = _studentProfile.value.copy(
                            avatarUrl = defaultAvatar.url,
                            avatarId = defaultAvatar.id
                        )
                        _studentProfile.value = updatedProfile
                        sessionManager.cachedProfile = updatedProfile
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun mapLessonDtoList(dtoList: List<LessonScheduleItemResponseDTO>): List<LessonScheduleItem> {
        return dtoList.mapIndexed { index, dto ->
            val teacherFormatted = if (dto.teacher != null) {
                val f = dto.teacher.firstName?.firstOrNull()?.let { "$it." } ?: ""
                val m = dto.teacher.middleName?.firstOrNull()?.let { "$it." } ?: ""
                "${dto.teacher.lastName ?: ""} $f$m".trim()
            } else ""

            val roomFormatted = when {
                !dto.roomNumber.isNullOrBlank() -> "Каб. ${dto.roomNumber}"
                !dto.roomName.isNullOrBlank() -> "Каб. ${dto.roomName}"
                else -> "Кабинет"
            }

            val hwText = dto.lessonHomeworks?.firstOrNull()?.homework
            val firstMark = dto.marks?.firstOrNull()
            val markVal = firstMark?.value?.toIntOrNull()
            val markW = firstMark?.weight ?: 1.0

            LessonScheduleItem(
                id = dto.id?.toString() ?: "lesson_$index",
                subject = dto.subjectName ?: "Урок",
                lessonNumber = index + 1,
                startTime = dto.beginTime ?: "",
                endTime = dto.endTime ?: "",
                room = roomFormatted,
                teacherName = if (teacherFormatted.isNotBlank()) teacherFormatted else "Учитель",
                isOngoing = false,
                isCanceled = dto.isMissedLesson == true,
                mark = markVal,
                markWeight = markW,
                homework = hwText
            )
        }
    }

    private fun formatApiDateToReadable(apiDate: String, tomorrowStr: String): String {
        if (apiDate.isBlank()) return ""
        if (apiDate.startsWith(tomorrowStr)) return "Завтра"
        return try {
            val cleanDate = if (apiDate.length >= 10) apiDate.substring(0, 10) else apiDate
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cleanDate)
            if (parsed != null) {
                SimpleDateFormat("d MMMM", Locale("ru")).format(parsed)
            } else {
                apiDate
            }
        } catch (_: Exception) {
            apiDate
        }
    }

    private fun loadDemoData() {
        _studentProfile.value = DemoMockDataProvider.studentProfile
        _scheduleToday.value = DemoMockDataProvider.scheduleToday
        _scheduleTomorrow.value = emptyList()
        _homeworkList.value = DemoMockDataProvider.homeworkList
        _subjectSummaries.value = DemoMockDataProvider.subjectSummaries
        _gamificationProfile.value = DemoMockDataProvider.gamificationProfile
        _starLeaders.value = DemoMockDataProvider.starLeaders
        _classmates.value = DemoMockDataProvider.classmates
        _rewards.value = DemoMockDataProvider.rewards
        _works.value = DemoMockDataProvider.works
        _mealsBalance.value = DemoMockDataProvider.mealsBalance
        _ratingInfo.value = DemoMockDataProvider.ratingInfo
        _attendance.value = DemoMockDataProvider.attendance
    }

    suspend fun claimDailyGift(): String = withContext(Dispatchers.IO) {
        val token = sessionManager.authToken
        if (token.isNullOrBlank()) {
            val current = _gamificationProfile.value
            _gamificationProfile.value = current.copy(
                coinsCount = current.coinsCount + 150,
                dailyGiftAvailable = false
            )
            return@withContext "Демо-режим: 150 звезд успешно начислены!"
        }

        val cleanToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val profileId = sessionManager.profileId.toLongOrNull() ?: 0L

        try {
            val resp = MeshNetworkClient.gamificationApi.claimSystemGift(
                cleanToken, profileId, "familymp", "diary-mobile"
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
    }

    suspend fun autoCompleteAllQuests(): AutoCompleteResult = withContext(Dispatchers.IO) {
        val token = sessionManager.authToken
        val bearerToken = if (token != null) {
            if (token.startsWith("Bearer ")) token else "Bearer $token"
        } else null

        val profileId = sessionManager.profileId.toLongOrNull() ?: 0L
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
        val currentList = _homeworkList.value
        var targetHw: HomeworkItem? = null
        val updated = currentList.map {
            if (it.id == id) {
                val newState = !it.isDone
                val copy = it.copy(isDone = newState)
                targetHw = copy
                copy
            } else it
        }
        _homeworkList.value = updated
        sessionManager.cachedHomeworkList = updated

        val hId = id.toLongOrNull()
        val token = sessionManager.authToken
        if (hId != null && !token.isNullOrBlank() && targetHw != null) {
            val cleanToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val profileId = sessionManager.profileId.toLongOrNull() ?: 0L
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (targetHw!!.isDone) {
                        MeshNetworkClient.familyMobileApi.markHomeworkDone(cleanToken, profileId, "familymp", "diary-mobile", hId)
                    } else {
                        MeshNetworkClient.familyMobileApi.markHomeworkUndone(cleanToken, profileId, "familymp", "diary-mobile", hId)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun setInfiniteStars(enabled: Boolean) {
        sessionManager.infiniteStarsOverride = enabled
        _gamificationProfile.value = _gamificationProfile.value.copy(
            coinsCount = if (enabled) 999999999 else _gamificationProfile.value.coinsCount,
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
            _rewards.value = _rewards.value.map {
                if (it.id == id) it.copy(isUnlocked = true) else it
            }
        }
    }

    suspend fun sendGift(
        rewardId: String,
        costStars: Int,
        gamificationId: String,
        comment: String,
        isAnonymous: Boolean
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val rawToken = sessionManager.authToken
        if (rawToken.isNullOrBlank()) {
            return@withContext Pair(false, "Необходимо войти в аккаунт через mos.ru")
        }
        val cleanToken = if (rawToken.startsWith("Bearer ")) rawToken.substring(7) else rawToken
        val bearerToken = "Bearer $cleanToken"
        val profileId = sessionManager.profileId.toLongOrNull() ?: 0L
        val senderGamifId = _gamificationProfile.value.id?.toString() ?: profileId.toString()

        val currentStars = _gamificationProfile.value.coinsCount
        if (!_gamificationProfile.value.infiniteStarsOverride && currentStars < costStars) {
            return@withContext Pair(false, "Недостаточно звезд на балансе")
        }

        if (rawToken.isNullOrBlank()) {
            if (!_gamificationProfile.value.infiniteStarsOverride) {
                val newBalance = (_gamificationProfile.value.coinsCount - costStars).coerceAtLeast(0)
                val newSpent = _gamificationProfile.value.coinsSpent + costStars
                _gamificationProfile.value = _gamificationProfile.value.copy(
                    coinsCount = newBalance,
                    coinsSpent = newSpent
                )
            }
            val targetReward = _rewards.value.find { it.id == rewardId }
            val nowIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
            val localItem = ProfileRewardItem(
                id = System.currentTimeMillis(),
                name = targetReward?.title ?: "Подарок",
                comment = comment.ifBlank { null },
                sendingMode = if (isAnonymous) "PRIVATE" else "PUBLIC",
                imageUrl = targetReward?.iconName,
                animationUrl = targetReward?.animationUrl,
                purchasedAt = nowIso,
                from = PersonSearchItem(id = 1L, firstName = _studentProfile.value.firstName.ifBlank { "Я" }),
                to = PersonSearchItem(id = 2L, firstName = if (gamificationId == "SELF") "Себе" else gamificationId)
            )
            _profileRewards.value = listOf(localItem) + _profileRewards.value
            sessionManager.cachedProfileRewards = _profileRewards.value
            return@withContext Pair(true, "Подарок успешно подарен!")
        }

        try {
            val recipientId: Long
            val recipientName: String

            val isSelf = gamificationId.trim().uppercase() in listOf("SELF", "MY_SELF") ||
                         (_gamificationProfile.value.gamificationId != null && gamificationId.trim().equals(_gamificationProfile.value.gamificationId, ignoreCase = true))

            if (isSelf) {
                recipientId = _gamificationProfile.value.id ?: profileId
                recipientName = _studentProfile.value.firstName.ifBlank { "себя" }
            } else {
                val personResp = MeshNetworkClient.gamificationApi.getPersonByGamificationId(
                    bearerToken, profileId, "familymp", "diary-mobile", gamificationId.trim().uppercase()
                )
                if (!personResp.isSuccessful || personResp.body() == null) {
                    return@withContext Pair(false, "Ученик с ID $gamificationId не найден")
                }

                val recipient = personResp.body()!!
                if (!recipient.isReceiveRewardsAllowed) {
                    return@withContext Pair(false, "Пользователь ограничил получение подарков в настройках")
                }
                recipientId = recipient.id
                recipientName = "${recipient.firstName} ${recipient.lastName}"
            }

            val sendResp = MeshNetworkClient.gamificationApi.sendRewardGift(
                bearerToken, profileId, "familymp", "diary-mobile",
                senderGamifId,
                rewardId,
                SendRewardGiftRequest(
                    comment = comment,
                    recipientProfileIds = listOf(recipientId),
                    sendingMode = if (isAnonymous) "PRIVATE" else "PUBLIC"
                )
            )

            if (sendResp.isSuccessful) {
                if (!_gamificationProfile.value.infiniteStarsOverride) {
                    val newBalance = (_gamificationProfile.value.coinsCount - costStars).coerceAtLeast(0)
                    val newSpent = _gamificationProfile.value.coinsSpent + costStars
                    _gamificationProfile.value = _gamificationProfile.value.copy(
                        coinsCount = newBalance,
                        coinsSpent = newSpent
                    )
                }

                val targetReward = _rewards.value.find { it.id == rewardId }
                val nowIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
                val localItem = ProfileRewardItem(
                    id = System.currentTimeMillis(),
                    name = targetReward?.title ?: "Подарок",
                    comment = comment.ifBlank { null },
                    sendingMode = if (isAnonymous) "PRIVATE" else "PUBLIC",
                    imageUrl = targetReward?.iconName,
                    animationUrl = targetReward?.animationUrl,
                    purchasedAt = nowIso,
                    from = PersonSearchItem(id = _gamificationProfile.value.id ?: profileId, firstName = _studentProfile.value.firstName, lastName = _studentProfile.value.lastName),
                    to = PersonSearchItem(id = recipientId, firstName = recipientName)
                )
                _profileRewards.value = listOf(localItem) + _profileRewards.value
                sessionManager.cachedProfileRewards = _profileRewards.value

                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val refreshed = MeshNetworkClient.gamificationApi.getProfileRewards(
                            token = bearerToken,
                            profileId = profileId,
                            subsystem = "familymp",
                            clientType = "diary-mobile",
                            profileIdPath = senderGamifId,
                            request = ProfileRewardsRequestBody()
                        )
                        if (refreshed.isSuccessful && refreshed.body() != null) {
                            val items = refreshed.body()!!.items
                            if (items.isNotEmpty()) {
                                _profileRewards.value = items
                                sessionManager.cachedProfileRewards = items
                            }
                        }
                    } catch (_: Exception) {}
                }

                return@withContext Pair(true, "Подарок успешно отправлен для $recipientName!")
            } else {
                return@withContext Pair(false, "Ошибка сервера при отправке подарка (${sendResp.code()})")
            }
        } catch (e: Exception) {
            return@withContext Pair(false, "Ошибка: ${e.localizedMessage ?: "не удалось отправить"}")
        }
    }

    suspend fun uploadAvatar(fileBytes: ByteArray, fileName: String): Boolean = withContext(Dispatchers.IO) {
        val activeGuid = _studentProfile.value.contingentGuid
        val token = sessionManager.authToken
        if (token.isNullOrBlank() || activeGuid.isBlank()) {
            // Демо режим: создаем локальный data URL или псевдо-ссылку
            val updated = _studentProfile.value.copy(
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                avatarId = 777L
            )
            _studentProfile.value = updated
            sessionManager.cachedProfile = updated
            return@withContext true
        }

        val bearerToken = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
        val cleanToken = token.removePrefix("Bearer ").removePrefix("bearer ").trim()

        try {
            val reqBody = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, reqBody)
            val isDefaultBody = "true".toRequestBody("text/plain".toMediaTypeOrNull())

            val resp = MeshNetworkClient.avatarApi.uploadAvatar(
                token = bearerToken,
                authToken = cleanToken,
                subsystem = "familymp",
                clientType = "diary-mobile",
                userUuid = activeGuid,
                file = part,
                isDefault = isDefaultBody
            )
            if (resp.isSuccessful && resp.body() != null) {
                val newAvatar = resp.body()!!
                val updated = _studentProfile.value.copy(
                    avatarUrl = newAvatar.url,
                    avatarId = newAvatar.id
                )
                _studentProfile.value = updated
                sessionManager.cachedProfile = updated
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAvatar(): Boolean = withContext(Dispatchers.IO) {
        val activeGuid = _studentProfile.value.contingentGuid
        val currentAvatarId = _studentProfile.value.avatarId
        val token = sessionManager.authToken

        if (token.isNullOrBlank() || activeGuid.isBlank() || currentAvatarId == 0L) {
            // Демо режим
            val updated = _studentProfile.value.copy(
                avatarUrl = null,
                avatarId = 0L
            )
            _studentProfile.value = updated
            sessionManager.cachedProfile = updated
            return@withContext true
        }

        val bearerToken = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
        val cleanToken = token.removePrefix("Bearer ").removePrefix("bearer ").trim()

        try {
            val resp = MeshNetworkClient.avatarApi.deleteAvatar(
                token = bearerToken,
                authToken = cleanToken,
                subsystem = "familymp",
                clientType = "diary-mobile",
                userUuid = activeGuid,
                avatarId = currentAvatarId
            )
            if (resp.isSuccessful) {
                val updated = _studentProfile.value.copy(
                    avatarUrl = null,
                    avatarId = 0L
                )
                _studentProfile.value = updated
                sessionManager.cachedProfile = updated
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchFeedRewards(
        targetProfileId: Long? = null,
        from: String? = null,
        to: String? = null
    ): List<ProfileRewardItem> = withContext(Dispatchers.IO) {
        val rawToken = sessionManager.authToken
        val bearerToken = if (!rawToken.isNullOrBlank() && !rawToken.startsWith("Bearer ")) "Bearer $rawToken" else rawToken.orEmpty()
        val profileIdLong = sessionManager.profileId.toLongOrNull() ?: 0L
        val gamifId = _gamificationProfile.value.id ?: profileIdLong
        val actualProfileId = (targetProfileId ?: gamifId).toString()

        if (bearerToken.isBlank()) return@withContext emptyList()

        try {
            val resp = MeshNetworkClient.gamificationApi.getProfileRewards(
                token = bearerToken,
                profileId = profileIdLong,
                subsystem = "familymp",
                clientType = "diary-mobile",
                profileIdPath = actualProfileId,
                from = from,
                to = to,
                request = ProfileRewardsRequestBody(
                    pagination = RewardsPaginationDTO(pageNumber = 1, pageSize = 50),
                    sorting = ProfileRewardsSortingDTO(orderBy = "purchasedAt", direction = "DESC")
                )
            )
            if (resp.isSuccessful && resp.body() != null) {
                return@withContext resp.body()!!.items
            }
        } catch (_: Exception) {}
        emptyList()
    }

    suspend fun fetchMyPersonalRewards(): List<ProfileRewardItem> = withContext(Dispatchers.IO) {
        val received = fetchFeedRewards(from = "ALL", to = "ME")
        val sent = fetchFeedRewards(from = "ME", to = "OTHERS")
        val combined = (received + sent).distinctBy { it.profileRewardId.takeIf { id -> id > 0 } ?: it.id }
            .sortedByDescending { it.purchasedAt.orEmpty() }
        combined
    }

    suspend fun fetchSubjectClassRank(subjectId: Long? = null): List<AcademicClassRankItem> = withContext(Dispatchers.IO) {
        val token = sessionManager.authToken ?: return@withContext emptyList()
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val effectiveGuid = sessionManager.cachedProfile?.contingentGuid ?: "3473f068-8ec0-47a1-920a-a18e75d6c389"
        val effectiveClassUnitId = 2073368L
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dynamicProfileId = sessionManager.profileId.toLongOrNull() ?: 0L

        try {
            val ratingResp = MeshNetworkClient.ratingApi.getClassRank(
                token = bearerToken,
                subsystem = "familymp",
                clientType = "diary-mobile",
                personId = effectiveGuid,
                classUnitId = effectiveClassUnitId,
                date = todayStr,
                subjectId = subjectId
            )
            if (ratingResp.isSuccessful && ratingResp.body() != null) {
                val rankList = ratingResp.body()!!
                val classmatesMap = _classmates.value.associateBy { it.profileId }
                val classmatesGamifMap = _classmates.value.associateBy { it.gamificationId }
                val cachedRanksMap = _academicClassRanks.value.associateBy { it.personId }

                val items = coroutineScope {
                    rankList.map { item ->
                        async(Dispatchers.IO) {
                            val isMe = item.personId.equals(effectiveGuid, ignoreCase = true)
                            val place = item.rank?.rankPlace ?: 1
                            val guid = item.personId.orEmpty()

                            val existing = cachedRanksMap[guid]
                            var name = existing?.displayName ?: ""
                            var gamifId = existing?.gamificationId ?: ""
                            var pid = existing?.profileId ?: 0L

                            if (isMe) {
                                name = "${_studentProfile.value.lastName} ${_studentProfile.value.firstName} (Вы)"
                                gamifId = _gamificationProfile.value.gamificationId.orEmpty()
                                pid = dynamicProfileId
                            } else if (name.isBlank() && guid.isNotBlank()) {
                                try {
                                    val profResp = MeshNetworkClient.gamificationApi.getGamificationProfile(
                                        token = bearerToken,
                                        profileId = dynamicProfileId,
                                        subsystem = "familymp",
                                        clientType = "diary-mobile",
                                        personId = guid
                                    )
                                    if (profResp.isSuccessful && profResp.body() != null) {
                                        val p = profResp.body()!!
                                        val fn = p.firstName.orEmpty()
                                        val ln = p.lastName.orEmpty()
                                        gamifId = p.gamificationId.orEmpty()
                                        pid = p.id ?: 0L

                                        val cm = classmatesMap[pid] ?: classmatesGamifMap[gamifId]
                                        val fullLn = cm?.lastName?.ifBlank { ln } ?: ln
                                        name = if (fn.isNotBlank()) "$fn $fullLn".trim() else ""
                                    }
                                } catch (_: Exception) {}
                            }

                            AcademicClassRankItem(
                                rankPlace = place,
                                averageMark = item.rank?.averageMarkFive ?: 0.0,
                                rankStatus = item.rank?.rankStatus ?: "stable",
                                isCurrentUser = isMe,
                                personId = guid,
                                imageId = item.imageId,
                                displayName = name,
                                gamificationId = gamifId,
                                profileId = pid
                            )
                        }
                    }.awaitAll()
                }.mapIndexed { idx, it ->
                    if (it.displayName.isBlank()) {
                        it.copy(displayName = "Ученик ${idx + 1}")
                    } else it
                }.sortedWith(compareBy({ it.rankPlace }, { -it.averageMark }))

                return@withContext items
            }
        } catch (_: Exception) {}
        emptyList()
    }

    suspend fun fetchLessonDetails(lessonId: Long): LessonScheduleItem? = withContext(Dispatchers.IO) {
        val token = sessionManager.authToken ?: return@withContext null
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val effectiveGuid = sessionManager.cachedProfile?.contingentGuid ?: "3473f068-8ec0-47a1-920a-a18e75d6c389"
        val dynamicProfileId = sessionManager.profileId.toLongOrNull() ?: 0L
        val studentId = sessionManager.studentId

        try {
            val resp = MeshNetworkClient.familyMobileApi.getLessonScheduleItemDetails(
                token = bearerToken,
                profileId = dynamicProfileId,
                subsystem = "familymp",
                clientType = "diary-mobile",
                lessonId = lessonId,
                studentId = studentId,
                personId = effectiveGuid,
                type = "PLAN"
            )
            if (resp.isSuccessful && resp.body() != null) {
                val b = resp.body()!!
                val t = b.teacher
                val teacherName = "${t?.lastName.orEmpty()} ${t?.firstName.orEmpty()} ${t?.middleName.orEmpty()}".trim()
                val roomStr = listOfNotNull(b.roomNumber?.takeIf { it.isNotBlank() }?.let { "Каб. $it" }, b.roomName).joinToString(" • ")
                val hw = b.lessonHomeworks?.firstOrNull()?.homework
                val firstMark = b.marks?.firstOrNull()
                val markVal = firstMark?.value?.toIntOrNull()
                val markW = firstMark?.weight ?: 1.0

                // Filter and enrich test materials
                val rawMaterials = b.details?.additionalMaterials.orEmpty()
                val testMaterials = mutableListOf<LessonMaterialItem>()

                // Try to resolve exact launcher URLs via materialsApi
                val uuidsToFetch = rawMaterials.mapNotNull { it.uuid?.takeIf { u -> u.isNotBlank() } }
                val detailedMaterialsMap = mutableMapOf<String, DetailedMaterialItem>()
                if (uuidsToFetch.isNotEmpty() && !bearerToken.isNullOrBlank()) {
                    try {
                        val matReq = AdditionalMaterialsRequest(
                            materials = uuidsToFetch.map { MaterialRequestItem(uuid = it, selectedMode = "learn", purpose = "lesson") }
                        )
                        val matResp = MeshNetworkClient.materialsApi.getAdditionalMaterials(
                            token = bearerToken,
                            profileId = dynamicProfileId,
                            body = matReq
                        )
                        if (matResp.isSuccessful) {
                            matResp.body()?.additionalMaterials.orEmpty().forEach { wrapper ->
                                wrapper.materialGroups.orEmpty().forEach { group ->
                                    group.materials.orEmpty().forEach { dMat ->
                                        dMat.uuid?.let { detailedMaterialsMap[it] = dMat }
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                rawMaterials.forEach { mat ->
                    val type = mat.type.orEmpty().lowercase()
                    val typeName = mat.typeName.orEmpty().lowercase()
                    val actionName = mat.actionName.orEmpty().lowercase()
                    val title = mat.title.orEmpty().lowercase()

                    val isTest = type.contains("test") ||
                        type == "kim_variant" ||
                        type == "test_specification" ||
                        type == "final_test" ||
                        type == "functional_grammar_test" ||
                        typeName.contains("тест") ||
                        title.contains("тест") ||
                        actionName.contains("пройти") ||
                        actionName.contains("изучить")

                    if (isTest) {
                        val detailed = mat.uuid?.let { detailedMaterialsMap[it] }
                        val launchUrl = detailed?.urls?.find { it.type == "launch" }?.url
                            ?: detailed?.urls?.firstOrNull()?.url
                            ?: mat.urls?.firstOrNull()?.url

                        val finalUrl = when {
                            !launchUrl.isNullOrBlank() -> launchUrl
                            !mat.uuid.isNullOrBlank() -> "https://school.mos.ru/api/launcher/v1/launch?activity_url=${java.net.URLEncoder.encode("https://uchebnik.mos.ru/cms/materials/${mat.uuid}/launch", "UTF-8")}"
                            mat.id != null -> "https://school.mos.ru/api/launcher/v1/launch?activity_url=${java.net.URLEncoder.encode("https://uchebnik.mos.ru/cms/materials/${mat.id}/launch", "UTF-8")}"
                            else -> "https://uchebnik.mos.ru"
                        }

                        val matTitle = detailed?.title?.takeIf { it.isNotBlank() }
                            ?: mat.title?.takeIf { it.isNotBlank() }
                            ?: detailed?.typeName?.takeIf { it.isNotBlank() }
                            ?: mat.typeName?.takeIf { it.isNotBlank() }
                            ?: "Тестовое задание"

                        val matTypeName = detailed?.typeName?.takeIf { it.isNotBlank() }
                            ?: mat.typeName?.takeIf { it.isNotBlank() }
                            ?: "Тест"

                        testMaterials.add(
                            LessonMaterialItem(
                                title = matTitle,
                                typeName = matTypeName,
                                url = finalUrl
                            )
                        )
                    }
                }

                return@withContext LessonScheduleItem(
                    id = b.id?.toString() ?: lessonId.toString(),
                    subject = b.subjectName.orEmpty(),
                    subjectId = b.subjectId ?: 0L,
                    lessonNumber = 1,
                    startTime = "",
                    endTime = "",
                    room = roomStr,
                    teacherName = teacherName.ifBlank { "Учитель" },
                    mark = markVal,
                    markWeight = markW,
                    markComment = firstMark?.comment,
                    markControlForm = firstMark?.controlFormName,
                    markCreatedAt = firstMark?.createdAt,
                    homework = hw,
                    topic = b.details?.lessonTopic,
                    testMaterials = testMaterials
                )
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun resolveTestLaunchUrl(rawUrl: String): String = withContext(Dispatchers.IO) {
        try {
            if (!rawUrl.contains("launcher") || !rawUrl.contains("activity_url=")) {
                return@withContext rawUrl
            }
            val urlToCall = if (rawUrl.contains("/v1/launch")) {
                rawUrl.replace("/v1/launch", "/v1/url")
            } else if (rawUrl.contains("/v2/launch")) {
                rawUrl.replace("/v2/launch", "/v2/url")
            } else {
                rawUrl
            }

            val token = sessionManager.authToken?.replace("Bearer ", "") ?: ""
            val profile = _studentProfile.value
            val guid = profile.contingentGuid.ifBlank { "3473f068-8ec0-47a1-920a-a18e75d6c389" }
            val pid = profile.profileId.toString()

            val cookieHeader = "auth_token=$token; aupd_token=$token; student_person_id=$guid; aupd_current_role=2:1; profile_id=$pid"

            val request = okhttp3.Request.Builder()
                .url(urlToCall)
                .addHeader("Cookie", cookieHeader)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36 MeshClient/2.0 MobileApp")
                .get()
                .build()

            val response = MeshNetworkClient.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val resolved = response.body?.string()?.trim()
                if (!resolved.isNullOrBlank() && resolved.startsWith("http")) {
                    return@withContext resolved
                }
            }
        } catch (_: Exception) {}
        return@withContext rawUrl
    }

    fun calculateVacationPeriods(items: List<PeriodScheduleItemDTO>): List<VacationPeriodInfo> {
        if (items.isEmpty()) return emptyList()
        val vacations = mutableListOf<VacationPeriodInfo>()
        val vacationDays = items.filter { it.type == "vacation" || it.title.contains("каникул", ignoreCase = true) }
        if (vacationDays.isEmpty()) return emptyList()

        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        var currentGroup = mutableListOf<PeriodScheduleItemDTO>()
        for (day in vacationDays) {
            if (currentGroup.isEmpty()) {
                currentGroup.add(day)
            } else {
                val last = currentGroup.last()
                try {
                    val d1 = sdf.parse(last.date)
                    val d2 = sdf.parse(day.date)
                    val diffDays = if (d1 != null && d2 != null) ((d2.time - d1.time) / (1000 * 60 * 60 * 24)).toInt() else 99
                    if (diffDays <= 2 && (day.title == last.title || day.title.contains("каникул", ignoreCase = true))) {
                        currentGroup.add(day)
                    } else {
                        val start = currentGroup.first().date
                        val end = currentGroup.last().date
                        val title = currentGroup.first().title.ifBlank { "Каникулы" }
                        val dStart = sdf.parse(start)
                        val dEnd = sdf.parse(end)
                        val dToday = sdf.parse(todayStr)
                        val isCurrent = dToday != null && dStart != null && dEnd != null && dToday in dStart..dEnd
                        val isUpcoming = dToday != null && dStart != null && dToday.before(dStart)
                        val daysUntil = if (isUpcoming && dStart != null && dToday != null) ((dStart.time - dToday.time) / (1000 * 60 * 60 * 24)).toInt() else null
                        vacations.add(VacationPeriodInfo(title, start, end, currentGroup.size, isCurrent, isUpcoming, daysUntil))
                        currentGroup = mutableListOf(day)
                    }
                } catch (_: Exception) {
                    currentGroup.add(day)
                }
            }
        }
        if (currentGroup.isNotEmpty()) {
            val start = currentGroup.first().date
            val end = currentGroup.last().date
            val title = currentGroup.first().title.ifBlank { "Каникулы" }
            try {
                val dStart = sdf.parse(start)
                val dEnd = sdf.parse(end)
                val dToday = sdf.parse(todayStr)
                val isCurrent = dToday != null && dStart != null && dEnd != null && dToday in dStart..dEnd
                val isUpcoming = dToday != null && dStart != null && dToday.before(dStart)
                val daysUntil = if (isUpcoming && dStart != null && dToday != null) ((dStart.time - dToday.time) / (1000 * 60 * 60 * 24)).toInt() else null
                vacations.add(VacationPeriodInfo(title, start, end, currentGroup.size, isCurrent, isUpcoming, daysUntil))
            } catch (_: Exception) {}
        }
        return vacations
    }

    suspend fun uploadHomeworkAttachment(
        homeworkEntryStudentId: Long,
        fileUri: android.net.Uri,
        context: android.content.Context
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val rawToken = sessionManager.authToken ?: return@withContext false
            val cleanToken = if (rawToken.startsWith("Bearer ")) rawToken else "Bearer $rawToken"
            val profileId = sessionManager.profileId.toLongOrNull() ?: 0L
            val contentResolver = context.contentResolver

            var fileName = "attachment_${System.currentTimeMillis()}"
            contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val dn = cursor.getString(nameIndex)
                    if (!dn.isNullOrBlank()) fileName = dn
                }
            }

            val inputStream = contentResolver.openInputStream(fileUri) ?: return@withContext false
            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = okhttp3.MultipartBody.Part.createFormData("multipartFile", fileName, requestBody)

            val resp = MeshNetworkClient.familyMobileApi.uploadHomeworkAttachment(
                token = cleanToken,
                profileId = profileId,
                homeworkEntryStudentId = homeworkEntryStudentId,
                file = part
            )
            if (resp.isSuccessful) {
                fetchRemoteData()
                return@withContext true
            }
        } catch (_: Exception) {}
        false
    }

    suspend fun deleteHomeworkAttachment(
        homeworkEntryStudentId: Long,
        fileId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val rawToken = sessionManager.authToken ?: return@withContext false
            val cleanToken = if (rawToken.startsWith("Bearer ")) rawToken else "Bearer $rawToken"
            val profileId = sessionManager.profileId.toLongOrNull() ?: 0L

            val resp = MeshNetworkClient.familyMobileApi.deleteHomeworkAttachment(
                token = cleanToken,
                profileId = profileId,
                homeworkEntryStudentId = homeworkEntryStudentId,
                fileId = fileId
            )
            if (resp.isSuccessful) {
                fetchRemoteData()
                return@withContext true
            }
        } catch (_: Exception) {}
        false
    }

    fun logout() {
        sessionManager.logout()
        sessionManager.clear()
        loadDemoData()
    }
}
