package ru.mesh.expressive.data.local

import android.content.Context
import android.content.SharedPreferences

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.mesh.expressive.data.model.*

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mesh_expressive_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit().putString("auth_token", value).apply()

    var personId: Long
        get() = prefs.getLong("person_id", 0L)
        set(value) = prefs.edit().putLong("person_id", value).apply()

    var profileId: String
        get() = prefs.getString("profile_id", "") ?: ""
        set(value) = prefs.edit().putString("profile_id", value).apply()

    var studentId: Long
        get() = prefs.getLong("student_id", 0L)
        set(value) = prefs.edit().putLong("student_id", value).apply()

    var classUid: String
        get() = prefs.getString("class_uid", "") ?: ""
        set(value) = prefs.edit().putString("class_uid", value).apply()

    var classUnitId: Long
        get() = prefs.getLong("class_unit_id", 0L)
        set(value) = prefs.edit().putLong("class_unit_id", value).apply()

    val isLoggedIn: Boolean
        get() = !authToken.isNullOrBlank()

    var infiniteStarsOverride: Boolean
        get() = prefs.getBoolean("infinite_stars_override", false)
        set(value) = prefs.edit().putBoolean("infinite_stars_override", value).apply()

    var isMonetEnabled: Boolean
        get() = prefs.getBoolean("is_monet_enabled", true)
        set(value) = prefs.edit().putBoolean("is_monet_enabled", value).apply()

    var isCompactSchedule: Boolean
        get() = prefs.getBoolean("is_compact_schedule", false)
        set(value) = prefs.edit().putBoolean("is_compact_schedule", value).apply()

    var showWeightedGpa: Boolean
        get() = prefs.getBoolean("show_weighted_gpa", true)
        set(value) = prefs.edit().putBoolean("show_weighted_gpa", value).apply()

    var hideCompletedQuests: Boolean
        get() = prefs.getBoolean("hide_completed_quests", false)
        set(value) = prefs.edit().putBoolean("hide_completed_quests", value).apply()

    var enableSpringPhysics: Boolean
        get() = prefs.getBoolean("enable_spring_physics", true)
        set(value) = prefs.edit().putBoolean("enable_spring_physics", value).apply()

    var hideEmptyScheduleDays: Boolean
        get() = prefs.getBoolean("hide_empty_schedule_days", false)
        set(value) = prefs.edit().putBoolean("hide_empty_schedule_days", value).apply()

    var gpaTargetScore: Float
        get() = prefs.getFloat("gpa_target_score", 4.60f)
        set(value) = prefs.edit().putFloat("gpa_target_score", value).apply()

    var autoRefreshIntervalMinutes: Int
        get() = prefs.getInt("auto_refresh_interval_minutes", 15)
        set(value) = prefs.edit().putInt("auto_refresh_interval_minutes", value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean("is_onboarding_completed", false)
        set(value) = prefs.edit().putBoolean("is_onboarding_completed", value).apply()

    var marksViewMode: String
        get() = prefs.getString("marks_view_mode", "BY_SUBJECT") ?: "BY_SUBJECT"
        set(value) = prefs.edit().putString("marks_view_mode", value).apply()

    var startTab: String
        get() = prefs.getString("start_tab", "DASHBOARD") ?: "DASHBOARD"
        set(value) = prefs.edit().putString("start_tab", value).apply()

    var hapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean("haptic_feedback_enabled", true)
        set(value) = prefs.edit().putBoolean("haptic_feedback_enabled", value).apply()

    var hideCompletedHomework: Boolean
        get() = prefs.getBoolean("hide_completed_homework", false)
        set(value) = prefs.edit().putBoolean("hide_completed_homework", value).apply()

    var themeMode: String
        get() = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString("theme_mode", value).apply()

    // ======================== Cache Storage ========================

    private fun saveJson(key: String, value: Any?) {
        if (value == null) {
            prefs.edit().remove(key).apply()
        } else {
            try {
                val json = gson.toJson(value)
                prefs.edit().putString(key, json).apply()
            } catch (_: Exception) {}
        }
    }

    private inline fun <reified T> getJson(key: String): T? {
        val json = prefs.getString(key, null) ?: return null
        return try {
            val type = object : TypeToken<T>() {}.type
            gson.fromJson<T>(json, type)
        } catch (_: Exception) {
            null
        }
    }

    var cachedProfile: StudentProfile?
        get() = getJson("cached_profile")
        set(value) = saveJson("cached_profile", value)

    var cachedScheduleToday: List<LessonScheduleItem>?
        get() = getJson("cached_schedule_today")
        set(value) = saveJson("cached_schedule_today", value)

    var cachedScheduleTomorrow: List<LessonScheduleItem>?
        get() = getJson("cached_schedule_tomorrow")
        set(value) = saveJson("cached_schedule_tomorrow", value)

    var cachedWeekSchedule: Map<String, List<LessonScheduleItem>>?
        get() = getJson("cached_week_schedule")
        set(value) = saveJson("cached_week_schedule", value)

    var cachedHomeworkList: List<HomeworkItem>?
        get() = getJson("cached_homework_list")
        set(value) = saveJson("cached_homework_list", value)

    var cachedSubjectSummaries: List<SubjectSummary>?
        get() = getJson("cached_subject_summaries")
        set(value) = saveJson("cached_subject_summaries", value)

    var cachedGamificationProfile: GamificationProfile?
        get() = getJson("cached_gamification_profile")
        set(value) = saveJson("cached_gamification_profile", value)

    var cachedStarLeaders: List<StarLeaderItem>?
        get() = getJson("cached_star_leaders")
        set(value) = saveJson("cached_star_leaders", value)

    var cachedClassmates: List<ClassmateItem>?
        get() = getJson("cached_classmates")
        set(value) = saveJson("cached_classmates", value)

    var cachedWorks: List<WorkItem>?
        get() = getJson("cached_works")
        set(value) = saveJson("cached_works", value)

    var cachedRewards: List<RewardItem>?
        get() = getJson("cached_rewards")
        set(value) = saveJson("cached_rewards", value)

    var cachedProfileRewards: List<ProfileRewardItem>?
        get() = getJson("cached_profile_rewards")
        set(value) = saveJson("cached_profile_rewards", value)

    var cachedMealsBalance: MealsBalance?
        get() = getJson("cached_meals_balance")
        set(value) = saveJson("cached_meals_balance", value)

    var cachedRatingInfo: RatingInfo?
        get() = getJson("cached_rating_info")
        set(value) = saveJson("cached_rating_info", value)

    var cachedAcademicClassRanks: List<AcademicClassRankItem>?
        get() = getJson("cached_academic_ranks")
        set(value) = saveJson("cached_academic_ranks", value)

    var cachedAttendance: AttendanceSummary?
        get() = getJson("cached_attendance")
        set(value) = saveJson("cached_attendance", value)

    var cachedPeriodsSchedules: List<PeriodScheduleItemDTO>?
        get() = getJson("cached_periods_schedules")
        set(value) = saveJson("cached_periods_schedules", value)

    var cachedPortfolioAchievements: List<PortfolioAchievementItem>?
        get() = getJson("cached_portfolio_achievements")
        set(value) = saveJson("cached_portfolio_achievements", value)

    fun logout() {
        prefs.edit().remove("auth_token").apply()
        clearCache()
    }

    fun clearCache() {
        prefs.edit()
            .remove("cached_profile")
            .remove("cached_schedule_today")
            .remove("cached_schedule_tomorrow")
            .remove("cached_homework_list")
            .remove("cached_subject_summaries")
            .remove("cached_gamification_profile")
            .remove("cached_star_leaders")
            .remove("cached_classmates")
            .remove("cached_works")
            .remove("cached_rewards")
            .remove("cached_profile_rewards")
            .remove("cached_meals_balance")
            .remove("cached_rating_info")
            .remove("cached_attendance")
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
