package ru.mesh.expressive.data.model

import com.google.gson.annotations.SerializedName

/**
 * Профиль учащегося
 */
data class StudentProfile(
    val id: String = "17486681",
    val personId: Long = 2778403L,
    val contingentGuid: String = "3473f068-8ec0-47a1-920a-a18e75d6c389",
    val classUid: String = "ba95ea86-d870-4924-9345-1b9c021c82f6",
    val classUnitId: Long = 2073368L,
    val classLevelId: Int = 7,
    val firstName: String = "Семён",
    val lastName: String = "Софель",
    val middleName: String = "Максимович",
    val className: String = "7-В",
    val schoolName: String = "ГБОУ Школа № 315",
    val gpa: Double = 0.0,
    val avatarUrl: String? = null
)

/**
 * Элемент расписания уроков
 */
data class LessonScheduleItem(
    val id: String,
    val subject: String,
    val lessonNumber: Int,
    val startTime: String,
    val endTime: String,
    val room: String,
    val teacherName: String,
    val isOngoing: Boolean = false,
    val isCanceled: Boolean = false,
    val attendanceStatus: AttendanceType = AttendanceType.PRESENT,
    val mark: Int? = null,
    val markWeight: Double = 1.0,
    val homework: String? = null
)

enum class AttendanceType {
    PRESENT, ABSENT_EXCUSED, ABSENT_ILLNESS, ABSENT_UNEXCUSED, LATE
}

/**
 * Домашнее задание
 */
data class HomeworkItem(
    val id: String,
    val subject: String,
    val description: String,
    val date: String,
    val dueDate: String,
    var isDone: Boolean = false,
    val hasDigitalTest: Boolean = false,
    val digitalTestUrl: String? = null
)

/**
 * Оценка
 */
data class MarkItem(
    val id: String,
    val subject: String,
    val value: Int,
    val weight: Double = 1.0,
    val date: String,
    val topic: String,
    val isExam: Boolean = false
)

/**
 * Сводка по предмету
 */
data class SubjectSummary(
    val subject: String,
    val averageMark: Double,
    val marks: List<MarkItem>,
    val targetMark: Double = 4.60,
    val teacher: String = "Учитель предмета"
)

/**
 * Геймификация: Профиль и звезды
 */
data class GamificationProfile(
    @SerializedName("id")
    val id: Long? = 275590L,

    @SerializedName("gamificationId")
    val gamificationId: String? = "AAE75590",

    @SerializedName("balance")
    val coinsCount: Int = 5,

    @SerializedName("spentPoints")
    val coinsSpent: Int = 530,

    @SerializedName("level")
    val level: Int = 1,

    @SerializedName("points")
    val currentXp: Int = 0,

    @SerializedName("nextLevelXp")
    val nextLevelXp: Int = 1000,

    val dailyGiftAvailable: Boolean = false,

    val infiniteStarsOverride: Boolean = false
)

/**
 * Одноклассник (Мой класс)
 */
data class ClassmateItem(
    val profileId: Long,
    val gamificationId: String,
    val firstName: String,
    val lastName: String,
    val rank: Int,
    val spentStars: Int,
    val isBirthdayToday: Boolean = false,
    val isCurrentUser: Boolean = false
)

/**
 * DTO для POST /persons/rating
 */
data class PersonsRatingRequestBody(
    @SerializedName("filters")
    val filters: ClassUidFilter
)

data class ClassUidFilter(
    @SerializedName("classUid")
    val classUid: String
)

data class PersonsRatingResponse(
    @SerializedName("totalItems")
    val totalItems: Int = 0,
    @SerializedName("pageNumber")
    val pageNumber: Int = 1,
    @SerializedName("pageSize")
    val pageSize: Int = 50,
    @SerializedName("content")
    val content: List<PersonRatingItem> = emptyList()
)

data class PersonRatingItem(
    @SerializedName("profileId")
    val profileId: Long,
    @SerializedName("gamificationId")
    val gamificationId: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("spentPoints")
    val spentPoints: Int,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("isReceiveRewardsAllowed")
    val isReceiveRewardsAllowed: Boolean = true,
    @SerializedName("isShowRewardsAllowed")
    val isShowRewardsAllowed: Boolean = true
)

/**
 * DTO для POST /persons/search
 */
data class PersonsSearchFilterBody(
    @SerializedName("filters")
    val filters: ClassUidFilter,
    @SerializedName("sorting")
    val sorting: PersonsSorting = PersonsSorting()
)

data class PersonsSorting(
    @SerializedName("orderBy")
    val orderBy: String = "firstName",
    @SerializedName("direction")
    val direction: String = "ASC"
)

data class PersonSearchItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("gamificationId")
    val gamificationId: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("isReceiveRewardsAllowed")
    val isReceiveRewardsAllowed: Boolean = true,
    @SerializedName("isShowRewardsAllowed")
    val isShowRewardsAllowed: Boolean = true,
    @SerializedName("isBirthdayToday")
    val isBirthdayToday: Boolean = false
)

/**
 * Лидер в рейтинге щедрости
 */
data class StarLeaderItem(
    val rank: Int,
    val name: String,
    val className: String,
    val spentStars: Int,
    val gamificationId: String = "",
    val isCurrentUser: Boolean = false
)

/**
 * Награда магазина
 */
data class RewardItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("price")
    val costStars: Int = 100,
    @SerializedName("imageUrl")
    val iconName: String? = null,
    @SerializedName("isUnlocked")
    val isUnlocked: Boolean = false
)

/**
 * Задание за звезды
 */
data class WorkItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("coinsCount")
    val rewardStars: Int = 50,
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false
)

/**
 * Рейтинг успеваемости
 */
data class RatingInfo(
    @SerializedName("rankPlace")
    val classRank: Int = 0,

    @SerializedName("studentsCount")
    val totalInClass: Int = 0,

    @SerializedName("parallelRank")
    val parallelRank: Int = 0,

    @SerializedName("totalInParallel")
    val totalInParallel: Int = 0,

    @SerializedName("rankFigure")
    val score: Int = 0,

    @SerializedName("rankDelta")
    val rankChange: Int = 0
)

/**
 * Москвёнок (Питание и карта)
 */
data class MealsBalance(
    val clientBalanceRub: Double = 18.15,
    val dailyLimitRub: Double? = null,
    val hotMealSubscribed: Boolean = false,
    val cardId: String = "138810049",
    val transactions: List<MealTransaction> = emptyList()
)

data class MealTransaction(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("amount")
    val amountRub: Double,
    @SerializedName("date")
    val timestamp: String,
    @SerializedName("isDebit")
    val isDebit: Boolean = true
)

/**
 * Посещаемость и ЕМИАС
 */
data class AttendanceSummary(
    val totalLessons: Int = 0,
    val attendedLessons: Int = 0,
    val excusedAbsences: Int = 0,
    val unexcusedAbsences: Int = 0,
    val percentage: Double = 100.0,
    val emiasCertificates: List<EmiasRecord> = emptyList()
)

data class EmiasRecord(
    @SerializedName("id")
    val id: String,
    @SerializedName("certificateNumber")
    val certificateNumber: String,
    @SerializedName("clinicName")
    val clinicName: String,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String,
    @SerializedName("diagnosis")
    val diagnosis: String,
    @SerializedName("physicalCultureExemptionUntil")
    val physicalCultureExemptionUntil: String = "",
    @SerializedName("status")
    val status: String = "Действительна"
)
