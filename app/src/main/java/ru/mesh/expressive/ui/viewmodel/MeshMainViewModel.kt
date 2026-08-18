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
    DASHBOARD, SCHEDULE, HOMEWORK, MARKS, GIFTS, RATING, ATTENDANCE, MEALS, SETTINGS, AUTH
}

enum class DashboardDay {
    TODAY, TOMORROW
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

    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile
    val scheduleToday: StateFlow<List<LessonScheduleItem>> = repository.scheduleToday
    val scheduleTomorrow: StateFlow<List<LessonScheduleItem>> = repository.scheduleTomorrow
    val homeworkList: StateFlow<List<HomeworkItem>> = repository.homeworkList
    val subjectSummaries: StateFlow<List<SubjectSummary>> = repository.subjectSummaries
    val gamificationProfile: StateFlow<GamificationProfile> = repository.gamificationProfile
    val starLeaders: StateFlow<List<StarLeaderItem>> = repository.starLeaders
    val rewards: StateFlow<List<RewardItem>> = repository.rewards
    val works: StateFlow<List<WorkItem>> = repository.works
    val ratingInfo: StateFlow<RatingInfo> = repository.ratingInfo
    val mealsBalance: StateFlow<MealsBalance> = repository.mealsBalance
    val attendance: StateFlow<AttendanceSummary> = repository.attendance

    val isLoggedIn: Boolean
        get() = sessionManager.isLoggedIn

    val currentAuthToken: String?
        get() = sessionManager.authToken

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

    fun unlockReward(id: String) {
        repository.unlockReward(id)
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
