package ru.mesh.expressive.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mesh_expressive_prefs", Context.MODE_PRIVATE)

    var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit().putString("auth_token", value).apply()

    var personId: Long
        get() = prefs.getLong("person_id", 2778403L)
        set(value) = prefs.edit().putLong("person_id", value).apply()

    var profileId: String
        get() = prefs.getString("profile_id", "17486681") ?: "17486681"
        set(value) = prefs.edit().putString("profile_id", value).apply()

    var studentId: Long
        get() = prefs.getLong("student_id", 17486681L)
        set(value) = prefs.edit().putLong("student_id", value).apply()

    var classUid: String
        get() = prefs.getString("class_uid", "ba95ea86-d870-4924-9345-1b9c021c82f6") ?: "ba95ea86-d870-4924-9345-1b9c021c82f6"
        set(value) = prefs.edit().putString("class_uid", value).apply()

    var classUnitId: Long
        get() = prefs.getLong("class_unit_id", 2073368L)
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

    fun logout() {
        prefs.edit().remove("auth_token").apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
