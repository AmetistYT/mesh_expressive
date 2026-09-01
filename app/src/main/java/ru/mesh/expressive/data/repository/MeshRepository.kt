package ru.mesh.expressive.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        RewardItem(id = "r_1", title = "Неоновая тема профиля", description = "Уникальный визуальный стиль в дневнике", costStars = 100),
        RewardItem(id = "r_2", title = "Значок Отличника", description = "Особая рамка вокруг аватара", costStars = 150),
        RewardItem(id = "r_3", title = "Подарок другу", description = "Отправить 50 звезд однокласснику", costStars = 50)
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

    val attendance = AttendanceSummary(totalLessons = 45, attendedLessons = 44, excusedAbsences = 1, unexcusedAbsences = 0, percentage = 97.8)
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

    private val _rewards = MutableStateFlow<List<RewardItem>>(emptyList())
    val rewards: StateFlow<List<RewardItem>> = _rewards.asStateFlow()

    private val _profileRewards = MutableStateFlow<List<ProfileRewardItem>>(emptyList())
    val profileRewards: StateFlow<List<ProfileRewardItem>> = _profileRewards.asStateFlow()

    private val _works = MutableStateFlow<List<WorkItem>>(emptyList())
    val works: StateFlow<List<WorkItem>> = _works.asStateFlow()

    private val _ratingInfo = MutableStateFlow(RatingInfo())
    val ratingInfo: StateFlow<RatingInfo> = _ratingInfo.asStateFlow()

    private val _mealsBalance = MutableStateFlow(MealsBalance())
    val mealsBalance: StateFlow<MealsBalance> = _mealsBalance.asStateFlow()

    private val _attendance = MutableStateFlow(AttendanceSummary())
    val attendance: StateFlow<AttendanceSummary> = _attendance.asStateFlow()

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
        sessionManager.cachedAttendance?.let { _attendance.value = it }
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
                            subject = item.subjectName ?: "Предмет",
                            description = item.description ?: "Задание",
                            date = dateAssigned,
                            dueDate = formattedDueDate,
                            isDone = item.isDone == true,
                            hasDigitalTest = false
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

        // 4. Оценки и сводки по предметам (Mobile API)
        try {
            if (dynamicStudentId > 0) {
                val marksResp = MeshNetworkClient.familyMobileApi.getSubjectMarksShort(
                    bearerToken, dynamicProfileId, "familymp", "diary-mobile", dynamicStudentId
                )
                if (marksResp.isSuccessful && marksResp.body()?.payload != null) {
                    val subjectsList = marksResp.body()!!.payload.orEmpty()
                    val summaries = subjectsList.map { subj ->
                        val avg = subj.average?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                        val marksList = subj.marks.orEmpty().map { m ->
                            MarkItem(
                                id = m.id?.toString() ?: "",
                                subject = subj.subjectName ?: "",
                                value = m.value?.toIntOrNull() ?: 5,
                                weight = m.weight ?: 1.0,
                                date = m.date ?: "",
                                topic = m.controlFormName ?: m.comment ?: "",
                                isExam = m.isExam == true
                            )
                        }
                        SubjectSummary(
                            subject = subj.subjectName ?: "Предмет",
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
                } else {
                    val rawMarksResp = MeshNetworkClient.familyMobileApi.getMarks(
                        bearerToken, dynamicProfileId, "familymp", "diary-mobile", dynamicStudentId, "2026-08-01", todayStr
                    )
                    if (rawMarksResp.isSuccessful && !rawMarksResp.body().isNullOrEmpty()) {
                        val rawMarks = rawMarksResp.body()!!
                        val grouped = rawMarks.groupBy { it.subjectName ?: "Предмет" }
                        val summaries = grouped.map { (subjName, markItems) ->
                            val marksList = markItems.map { m ->
                                MarkItem(
                                    id = m.id?.toString() ?: "",
                                    subject = subjName,
                                    value = m.value?.toIntOrNull() ?: 5,
                                    weight = m.weight ?: 1.0,
                                    date = m.date ?: "",
                                    topic = m.controlFormName ?: m.comment ?: "",
                                    isExam = m.isExam == true
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
                val ratingResp = MeshNetworkClient.gamificationApi.getPersonsRating(
                    bearerToken, dynamicProfileId, "familymp", "diary-mobile",
                    PersonsRatingRequestBody(filters = ClassUidFilter(classUid = activeClassUid))
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
                bearerToken, dynamicProfileId, "familymp", "diary-mobile"
            )
            if (rewardsResp.isSuccessful && rewardsResp.body() != null) {
                _rewards.value = rewardsResp.body()!!.items
                sessionManager.cachedRewards = rewardsResp.body()!!.items
            }

            val feedResp = MeshNetworkClient.gamificationApi.getProfileRewards(
                bearerToken, dynamicProfileId, "familymp", "diary-mobile", actualGamifId
            )
            if (feedResp.isSuccessful && feedResp.body() != null) {
                _profileRewards.value = feedResp.body()!!
                sessionManager.cachedProfileRewards = feedResp.body()!!
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
            // 7. Рейтинг успеваемости
            if (activeGuid.isNotBlank() && _studentProfile.value.classUnitId > 0) {
                val ratingResp = MeshNetworkClient.ratingApi.getClassRank(
                    bearerToken, "familymp", "mobile", activeGuid, _studentProfile.value.classUnitId, todayStr
                )
                if (ratingResp.isSuccessful && ratingResp.body() != null) {
                    _ratingInfo.value = ratingResp.body()!!
                    sessionManager.cachedRatingInfo = ratingResp.body()!!
                }
            }
        } catch (_: Exception) {}

        try {
            // 8. Посещаемость
            if (dynamicStudentId > 0) {
                val attResp = MeshNetworkClient.familyWebApi.getAttendance(
                    cleanToken, dynamicProfileId, "familyweb", dynamicStudentId, "2026-08-01", todayStr
                )
                if (attResp.isSuccessful && attResp.body() != null) {
                    val updated = _attendance.value.copy(
                        totalLessons = attResp.body()!!.attendance.size
                    )
                    _attendance.value = updated
                    sessionManager.cachedAttendance = updated
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

        try {
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

            val sendResp = MeshNetworkClient.gamificationApi.sendRewardGift(
                bearerToken, profileId, "familymp", "diary-mobile",
                senderGamifId,
                rewardId,
                SendRewardGiftRequest(
                    comment = comment,
                    recipientProfileIds = listOf(recipient.id),
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
                return@withContext Pair(true, "Подарок успешно отправлен для ${recipient.firstName} ${recipient.lastName}!")
            } else {
                return@withContext Pair(false, "Ошибка сервера при отправке подарка (${sendResp.code()})")
            }
        } catch (e: Exception) {
            return@withContext Pair(false, "Ошибка: ${e.localizedMessage ?: "не удалось отправить"}")
        }
    }

    fun logout() {
        sessionManager.logout()
        sessionManager.clear()
        loadDemoData()
    }
}
