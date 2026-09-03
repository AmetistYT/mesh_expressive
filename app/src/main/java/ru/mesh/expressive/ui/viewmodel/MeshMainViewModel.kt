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
    DASHBOARD, SCHEDULE, HOMEWORK, MARKS, GIFTS, RATING, CLASSMATES, ATTENDANCE, MEALS, SETTINGS, AUTH
}

enum class DashboardDay {
    TODAY, TOMORROW
}

enum class MarksViewMode {
    BY_SUBJECT, BY_DATE
}

class MeshMainViewModel(
    private val repository: MeshRepository,
    private val sessionManager: SessionManager
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
    val mealsBalance: StateFlow<MealsBalance> = repository.mealsBalance
    val attendance: StateFlow<AttendanceSummary> = repository.attendance

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
