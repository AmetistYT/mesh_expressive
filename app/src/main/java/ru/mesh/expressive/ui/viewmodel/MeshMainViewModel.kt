package ru.mesh.expressive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.mesh.expressive.data.local.SessionManager
import ru.mesh.expressive.data.model.*
import ru.mesh.expressive.data.repository.AutoCompleteResult
import ru.mesh.expressive.data.repository.MeshRepository

enum class MainTab {
    DASHBOARD, SCHEDULE, HOMEWORK, MARKS, GIFTS, RATING, CLASSMATES, ATTENDANCE, MEALS, PORTFOLIO, SETTINGS, AUTH
}

enum class DashboardDay {
    TODAY, TOMORROW
}

enum class MarksViewMode {
    BY_SUBJECT, BY_DATE
}

class MeshMainViewModel(
    val repository: MeshRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _currentTab = MutableStateFlow(
        if (sessionManager.isLoggedIn) MainTab.DASHBOARD else MainTab.AUTH
    )
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _dashboardDay = MutableStateFlow(DashboardDay.TODAY)
    val dashboardDay: StateFlow<DashboardDay> = _dashboardDay.asStateFlow()

    private val _isAutoCompleting = MutableStateFlow(false)
    val isAutoCompleting: StateFlow<Boolean> = _isAutoCompleting.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isCompactSchedule = MutableStateFlow(sessionManager.isCompactSchedule)
    val isCompactSchedule: StateFlow<Boolean> = _isCompactSchedule.asStateFlow()

    private val _showWeightedGpa = MutableStateFlow(sessionManager.showWeightedGpa)
    val showWeightedGpa: StateFlow<Boolean> = _showWeightedGpa.asStateFlow()

    private val _hideCompletedQuests = MutableStateFlow(sessionManager.hideCompletedQuests)
    val hideCompletedQuests: StateFlow<Boolean> = _hideCompletedQuests.asStateFlow()

    private val _enableSpringPhysics = MutableStateFlow(sessionManager.enableSpringPhysics)
    val enableSpringPhysics: StateFlow<Boolean> = _enableSpringPhysics.asStateFlow()

    private val _hideEmptyScheduleDays = MutableStateFlow(sessionManager.hideEmptyScheduleDays)
    val hideEmptyScheduleDays: StateFlow<Boolean> = _hideEmptyScheduleDays.asStateFlow()

    private val _gpaTargetScore = MutableStateFlow(sessionManager.gpaTargetScore)
    val gpaTargetScore: StateFlow<Float> = _gpaTargetScore.asStateFlow()

    private val _autoRefreshMinutes = MutableStateFlow(sessionManager.autoRefreshIntervalMinutes)
    val autoRefreshMinutes: StateFlow<Int> = _autoRefreshMinutes.asStateFlow()

    private val _startTab = MutableStateFlow(sessionManager.startTab)
    val startTab: StateFlow<String> = _startTab.asStateFlow()

    private val _hapticFeedbackEnabled = MutableStateFlow(sessionManager.hapticFeedbackEnabled)
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

    private val _hideCompletedHomework = MutableStateFlow(sessionManager.hideCompletedHomework)
    val hideCompletedHomework: StateFlow<Boolean> = _hideCompletedHomework.asStateFlow()

    private val _themeMode = MutableStateFlow(sessionManager.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile
    val weekSchedule: StateFlow<Map<String, List<LessonScheduleItem>>> = repository.weekSchedule
    val scheduleToday: StateFlow<List<LessonScheduleItem>> = repository.scheduleToday
    val scheduleTomorrow: StateFlow<List<LessonScheduleItem>> = repository.scheduleTomorrow
    val homeworkList: StateFlow<List<HomeworkItem>> = repository.homeworkList
    val subjectSummaries: StateFlow<List<SubjectSummary>> = repository.subjectSummaries
    val gamificationProfile: StateFlow<GamificationProfile> = repository.gamificationProfile
    val starLeaders: StateFlow<List<StarLeaderItem>> = repository.starLeaders
    val classmates: StateFlow<List<ClassmateItem>> = repository.classmates
    val rewards: StateFlow<List<RewardItem>> = repository.rewards
    val profileRewards: StateFlow<List<ProfileRewardItem>> = repository.profileRewards
    val works: StateFlow<List<WorkItem>> = repository.works
    val ratingInfo: StateFlow<RatingInfo> = repository.ratingInfo
    val academicClassRanks: StateFlow<List<AcademicClassRankItem>> = repository.academicClassRanks
    val mealsBalance: StateFlow<MealsBalance> = repository.mealsBalance
    val attendance: StateFlow<AttendanceSummary> = repository.attendance
    val periodsSchedules: StateFlow<List<PeriodScheduleItemDTO>> = repository.periodsSchedules
    val vacationPeriods: StateFlow<List<VacationPeriodInfo>> = repository.vacationPeriods
    val upcomingVacation: StateFlow<VacationPeriodInfo?> = repository.upcomingVacation
    val portfolioAchievements: StateFlow<List<PortfolioAchievementItem>> = repository.portfolioAchievements
    val isOffline: StateFlow<Boolean> = repository.isOffline

    private val _selectedRatingSubjectId = MutableStateFlow<Long?>(null)
    val selectedRatingSubjectId: StateFlow<Long?> = _selectedRatingSubjectId.asStateFlow()

    private val _subjectAcademicRanks = MutableStateFlow<List<AcademicClassRankItem>>(emptyList())
    val subjectAcademicRanks: StateFlow<List<AcademicClassRankItem>> = _subjectAcademicRanks.asStateFlow()

    private val _isSubjectRankLoading = MutableStateFlow(false)
    val isSubjectRankLoading: StateFlow<Boolean> = _isSubjectRankLoading.asStateFlow()

    fun selectRatingSubject(subjectId: Long?) {
        _selectedRatingSubjectId.value = subjectId
        if (subjectId == null) {
            _subjectAcademicRanks.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSubjectRankLoading.value = true
            try {
                val list = repository.fetchSubjectClassRank(subjectId)
                _subjectAcademicRanks.value = list
            } finally {
                _isSubjectRankLoading.value = false
            }
        }
    }

    private val _customFeedItems = MutableStateFlow<List<ProfileRewardItem>?>(null)
    val customFeedItems: StateFlow<List<ProfileRewardItem>?> = _customFeedItems.asStateFlow()

    private val _isFeedLoading = MutableStateFlow(false)
    val isFeedLoading: StateFlow<Boolean> = _isFeedLoading.asStateFlow()

    fun loadPersonalFeed() {
        viewModelScope.launch {
            _isFeedLoading.value = true
            try {
                val items = repository.fetchMyPersonalRewards()
                _customFeedItems.value = items
            } finally {
                _isFeedLoading.value = false
            }
        }
    }

    fun loadPersonFeedByPid(pid: Long) {
        viewModelScope.launch {
            _isFeedLoading.value = true
            try {
                val received = repository.fetchFeedRewards(targetProfileId = pid, from = "ALL", to = "ME")
                val sent = repository.fetchFeedRewards(targetProfileId = pid, from = "ME", to = "OTHERS")
                val combined = (received + sent).distinctBy { it.profileRewardId.takeIf { id -> id > 0 } ?: it.id }
                    .sortedByDescending { it.purchasedAt.orEmpty() }
                _customFeedItems.value = combined
            } finally {
                _isFeedLoading.value = false
            }
        }
    }

    fun clearCustomFeed() {
        _customFeedItems.value = null
    }

    private val _showOnboardingGuide = MutableStateFlow(!sessionManager.isOnboardingCompleted)
    val showOnboardingGuide: StateFlow<Boolean> = _showOnboardingGuide.asStateFlow()

    private val _marksViewMode = MutableStateFlow(
        if (sessionManager.marksViewMode == "BY_DATE") MarksViewMode.BY_DATE else MarksViewMode.BY_SUBJECT
    )
    val marksViewMode: StateFlow<MarksViewMode> = _marksViewMode.asStateFlow()

    fun setMarksViewMode(mode: MarksViewMode) {
        _marksViewMode.value = mode
        sessionManager.marksViewMode = mode.name
    }

    init {
        viewModelScope.launch {
            scheduleToday.collect { lessons ->
                if (lessons.isNotEmpty()) {
                    _dashboardDay.value = computeSmartDefaultDay(lessons)
                }
            }
        }
        if (sessionManager.isLoggedIn) {
            refreshData()
        }
    }

    fun computeSmartDefaultDay(todayLessons: List<LessonScheduleItem>): DashboardDay {
        val cal = java.util.Calendar.getInstance()
        val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(java.util.Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMinute

        val lastLesson = todayLessons.maxByOrNull { it.lessonNumber }
        if (lastLesson != null && lastLesson.endTime.isNotBlank()) {
            val parts = lastLesson.endTime.split(":")
            if (parts.size >= 2) {
                val endHour = parts[0].toIntOrNull() ?: 15
                val endMinute = parts[1].toIntOrNull() ?: 0
                val lessonEndMinutes = endHour * 60 + endMinute
                return if (currentTimeMinutes >= lessonEndMinutes) DashboardDay.TOMORROW else DashboardDay.TODAY
            }
        }
        return if (currentTimeMinutes >= 15 * 60) DashboardDay.TOMORROW else DashboardDay.TODAY
    }

    val isLoggedIn: Boolean
        get() = sessionManager.isLoggedIn

    val currentAuthToken: String?
        get() = sessionManager.authToken

    fun completeOnboardingGuide() {
        sessionManager.isOnboardingCompleted = true
        _showOnboardingGuide.value = false
    }

    fun restartOnboardingGuide() {
        _showOnboardingGuide.value = true
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectDashboardDay(day: DashboardDay) {
        _dashboardDay.value = day
    }

    fun toggleHomework(id: String) {
        repository.toggleHomework(id)
    }

    fun claimDailyGift(onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            val resultMessage = repository.claimDailyGift()
            onResult(resultMessage)
        }
    }

    fun toggleInfiniteStars(enabled: Boolean) {
        repository.setInfiniteStars(enabled)
    }

    fun toggleCompactSchedule(enabled: Boolean) {
        sessionManager.isCompactSchedule = enabled
        _isCompactSchedule.value = enabled
    }

    fun toggleShowWeightedGpa(enabled: Boolean) {
        sessionManager.showWeightedGpa = enabled
        _showWeightedGpa.value = enabled
    }

    fun toggleHideCompletedQuests(enabled: Boolean) {
        sessionManager.hideCompletedQuests = enabled
        _hideCompletedQuests.value = enabled
    }

    fun toggleEnableSpringPhysics(enabled: Boolean) {
        sessionManager.enableSpringPhysics = enabled
        _enableSpringPhysics.value = enabled
    }

    fun toggleHideEmptyScheduleDays(enabled: Boolean) {
        sessionManager.hideEmptyScheduleDays = enabled
        _hideEmptyScheduleDays.value = enabled
    }

    fun setGpaTargetScore(target: Float) {
        sessionManager.gpaTargetScore = target
        _gpaTargetScore.value = target
    }

    fun setAutoRefreshMinutes(minutes: Int) {
        sessionManager.autoRefreshIntervalMinutes = minutes
        _autoRefreshMinutes.value = minutes
    }

    fun setStartTab(tabName: String) {
        sessionManager.startTab = tabName
        _startTab.value = tabName
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        sessionManager.hapticFeedbackEnabled = enabled
        _hapticFeedbackEnabled.value = enabled
    }

    fun toggleHideCompletedHomework(enabled: Boolean) {
        sessionManager.hideCompletedHomework = enabled
        _hideCompletedHomework.value = enabled
    }

    fun setThemeMode(mode: String) {
        sessionManager.themeMode = mode
        _themeMode.value = mode
    }

    fun unlockReward(id: String) {
        repository.unlockReward(id)
    }

    fun sendGift(
        rewardId: String,
        costStars: Int,
        gamificationId: String,
        comment: String,
        isAnonymous: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (success, message) = repository.sendGift(
                rewardId = rewardId,
                costStars = costStars,
                gamificationId = gamificationId,
                comment = comment,
                isAnonymous = isAnonymous
            )
            onResult(success, message)
        }
    }

    fun saveAuthToken(token: String) {
        viewModelScope.launch {
            repository.saveAuthToken(token)
            _currentTab.value = MainTab.DASHBOARD
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchRemoteData()
            _isRefreshing.value = false
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.logout()
            sessionManager.clear()
            _currentTab.value = MainTab.AUTH
        }
    }

    fun logout() {
        repository.logout()
        _currentTab.value = MainTab.AUTH
    }

    fun autoCompleteAllQuests(onCompleted: (AutoCompleteResult) -> Unit) {
        viewModelScope.launch {
            _isAutoCompleting.value = true
            val result = repository.autoCompleteAllQuests()
            _isAutoCompleting.value = false
            onCompleted(result)
        }
    }

    fun uploadAvatar(bytes: ByteArray, fileName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.uploadAvatar(bytes, fileName)
            onResult(success)
        }
    }

    fun deleteAvatar(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.deleteAvatar()
            onResult(success)
        }
    }

    private val _isGiftSendActive = MutableStateFlow(false)
    val isGiftSendActive: StateFlow<Boolean> = _isGiftSendActive.asStateFlow()

    private val _selectedGiftForSend = MutableStateFlow<RewardItem?>(null)
    val selectedGiftForSend: StateFlow<RewardItem?> = _selectedGiftForSend.asStateFlow()

    private val _targetRecipientGamifId = MutableStateFlow<String?>(null)
    val targetRecipientGamifId: StateFlow<String?> = _targetRecipientGamifId.asStateFlow()

    fun openGiftSend(gift: RewardItem? = null, recipientGamifId: String? = null) {
        _selectedGiftForSend.value = gift
        _targetRecipientGamifId.value = recipientGamifId
        _isGiftSendActive.value = true
    }

    fun closeGiftSend() {
        _isGiftSendActive.value = false
        _selectedGiftForSend.value = null
        _targetRecipientGamifId.value = null
    }

    private val _selectedLessonForDetails = MutableStateFlow<LessonScheduleItem?>(null)
    val selectedLessonForDetails: StateFlow<LessonScheduleItem?> = _selectedLessonForDetails.asStateFlow()

    private val _isLessonDetailsLoading = MutableStateFlow(false)
    val isLessonDetailsLoading: StateFlow<Boolean> = _isLessonDetailsLoading.asStateFlow()

    private val _selectedMarkLesson = MutableStateFlow<LessonScheduleItem?>(null)
    val selectedMarkLesson: StateFlow<LessonScheduleItem?> = _selectedMarkLesson.asStateFlow()

    private val _markSubjectRanks = MutableStateFlow<List<AcademicClassRankItem>>(emptyList())
    val markSubjectRanks: StateFlow<List<AcademicClassRankItem>> = _markSubjectRanks.asStateFlow()

    private val _isMarkSubjectRanksLoading = MutableStateFlow(false)
    val isMarkSubjectRanksLoading: StateFlow<Boolean> = _isMarkSubjectRanksLoading.asStateFlow()

    fun openLessonDetails(lesson: LessonScheduleItem) {
        _selectedLessonForDetails.value = lesson
        val lessonIdLong = lesson.id.replace("ev_", "").substringBefore("_").toLongOrNull()
        if (lessonIdLong != null && lessonIdLong > 0) {
            viewModelScope.launch {
                _isLessonDetailsLoading.value = true
                val detailed = repository.fetchLessonDetails(lessonIdLong)
                if (detailed != null) {
                    _selectedLessonForDetails.value = lesson.copy(
                        room = if (detailed.room.isNotBlank()) detailed.room else lesson.room,
                        teacherName = if (detailed.teacherName.isNotBlank() && detailed.teacherName != "Учитель") detailed.teacherName else lesson.teacherName,
                        homework = detailed.homework ?: lesson.homework,
                        topic = detailed.topic ?: lesson.topic,
                        mark = detailed.mark ?: lesson.mark,
                        markWeight = if (detailed.mark != null) detailed.markWeight else lesson.markWeight,
                        markComment = detailed.markComment ?: lesson.markComment,
                        markControlForm = detailed.markControlForm ?: lesson.markControlForm,
                        markCreatedAt = detailed.markCreatedAt ?: lesson.markCreatedAt,
                        testMaterials = detailed.testMaterials
                    )
                }
                _isLessonDetailsLoading.value = false
            }
        }
    }

    fun closeLessonDetails() {
        _selectedLessonForDetails.value = null
    }

    fun openMarkDetails(lesson: LessonScheduleItem) {
        _selectedMarkLesson.value = lesson
        val subjId = lesson.subjectId.takeIf { it > 0 }
            ?: subjectSummaries.value.find { it.subject.equals(lesson.subject, ignoreCase = true) }?.subjectId

        viewModelScope.launch {
            _isMarkSubjectRanksLoading.value = true
            val ranks = repository.fetchSubjectClassRank(subjId)
            _markSubjectRanks.value = ranks
            _isMarkSubjectRanksLoading.value = false
        }
    }

    fun openMarkDetails(mark: ru.mesh.expressive.data.model.MarkItem) {
        val pseudoLesson = LessonScheduleItem(
            id = mark.id,
            subject = mark.subject,
            subjectId = mark.subjectId,
            mark = mark.value,
            markWeight = mark.weight,
            markComment = mark.comment,
            markControlForm = mark.controlFormName,
            markCreatedAt = mark.createdAt
        )
        openMarkDetails(pseudoLesson)
    }

    fun closeMarkDetails() {
        _selectedMarkLesson.value = null
        _markSubjectRanks.value = emptyList()
    }

    private val _selectedHomeworkForDetails = MutableStateFlow<ru.mesh.expressive.data.model.HomeworkItem?>(null)
    val selectedHomeworkForDetails: StateFlow<ru.mesh.expressive.data.model.HomeworkItem?> = _selectedHomeworkForDetails.asStateFlow()

    fun openHomeworkDetails(homework: ru.mesh.expressive.data.model.HomeworkItem) {
        _selectedHomeworkForDetails.value = homework
    }

    fun closeHomeworkDetails() {
        _selectedHomeworkForDetails.value = null
    }

    private val _isAttachmentUploading = MutableStateFlow(false)
    val isAttachmentUploading: StateFlow<Boolean> = _isAttachmentUploading.asStateFlow()

    fun uploadHomeworkAttachment(homeworkEntryStudentId: Long, fileUri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _isAttachmentUploading.value = true
            repository.uploadHomeworkAttachment(homeworkEntryStudentId, fileUri, context)
            _isAttachmentUploading.value = false
            _selectedHomeworkForDetails.value = homeworkList.value.find { it.homeworkEntryStudentId == homeworkEntryStudentId }
        }
    }

    fun deleteHomeworkAttachment(homeworkEntryStudentId: Long, fileId: Long) {
        viewModelScope.launch {
            repository.deleteHomeworkAttachment(homeworkEntryStudentId, fileId)
            _selectedHomeworkForDetails.value = homeworkList.value.find { it.homeworkEntryStudentId == homeworkEntryStudentId }
        }
    }

    private val _activeTestExecutionUrl = MutableStateFlow<String?>(null)
    val activeTestExecutionUrl: StateFlow<String?> = _activeTestExecutionUrl.asStateFlow()

    private val _activeTestExecutionTitle = MutableStateFlow<String?>(null)
    val activeTestExecutionTitle: StateFlow<String?> = _activeTestExecutionTitle.asStateFlow()

    fun openTestExecution(url: String, title: String = "Тестовое задание") {
        _activeTestExecutionTitle.value = title
        viewModelScope.launch {
            val resolvedUrl = repository.resolveTestLaunchUrl(url)
            _activeTestExecutionUrl.value = resolvedUrl
        }
    }

    fun closeTestExecution() {
        _activeTestExecutionUrl.value = null
        _activeTestExecutionTitle.value = null
    }

    class Factory(
        private val repository: MeshRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MeshMainViewModel::class.java)) {
                return MeshMainViewModel(repository, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
