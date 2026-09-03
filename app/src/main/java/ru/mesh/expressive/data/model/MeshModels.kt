package ru.mesh.expressive.data.model

import com.google.gson.annotations.SerializedName

/**
 * Профиль учащегося
 */
data class StudentProfile(
    val id: String = "",
    val personId: Long = 0L,
    val contingentGuid: String = "",
    val classUid: String = "",
    val classUnitId: Long = 0L,
    val classLevelId: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val className: String = "",
    val schoolName: String = "",
    val gpa: Double = 0.0,
    val avatarUrl: String? = null,
    val avatarId: Long = 0L
)

/**
 * Элемент расписания уроков
 */
data class LessonScheduleItem(
    val id: String = "",
    val subject: String = "",
    val lessonNumber: Int = 1,
    val startTime: String = "",
    val endTime: String = "",
    val room: String = "",
    val teacherName: String = "",
    val isOngoing: Boolean = false,
    val isCanceled: Boolean = false,
    val attendanceStatus: AttendanceType = AttendanceType.PRESENT,
    val mark: Int? = null,
    val markWeight: Double = 1.0,
    val homework: String? = null
)

// ======================== Mobile API DTOs ========================

data class LessonScheduleItemResponseDTO(
    @SerializedName("id") val id: Any? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("begin_time") val beginTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("subject_id") val subjectId: Long? = null,
    @SerializedName("room_name") val roomName: String? = null,
    @SerializedName("room_number") val roomNumber: String? = null,
    @SerializedName("building_name") val buildingName: String? = null,
    @SerializedName("teacher") val teacher: TeacherResponseDTO? = null,
    @SerializedName("lesson_homeworks") val lessonHomeworks: List<LessonHomeworkResponseDTO>? = null,
    @SerializedName("marks") val marks: List<SimpleMarkResponseDTO>? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("is_missed_lesson") val isMissedLesson: Boolean? = null,
    @SerializedName("nonattendance_reason_id") val nonattendanceReasonId: Int? = null
)

data class TeacherResponseDTO(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("middle_name") val middleName: String? = null
)

data class LessonHomeworkResponseDTO(
    @SerializedName("homework") val homework: String? = null,
    @SerializedName("homework_entry_student_id") val homeworkEntryStudentId: Long? = null,
    @SerializedName("is_done") val isDone: Boolean? = null,
    @SerializedName("date_assigned_on") val dateAssignedOn: String? = null,
    @SerializedName("date_prepared_for") val datePreparedFor: String? = null
)

data class SimpleMarkResponseDTO(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("weight") val weight: Double? = null,
    @SerializedName("is_exam") val isExam: Boolean? = null,
    @SerializedName("is_point") val isPoint: Boolean? = null,
    @SerializedName("control_form_name") val controlFormName: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class HomeworksShortResponseDTO(
    @SerializedName("payload") val payload: List<HomeworksShortItemDTO>? = null
)

data class HomeworksShortItemDTO(
    @SerializedName("homework_entry_student_id") val homeworkEntryStudentId: Long? = null,
    @SerializedName("subject_id") val subjectId: Long? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("date_assigned_on") val dateAssignedOn: String? = null,
    @SerializedName("is_done") val isDone: Boolean? = null,
    @SerializedName("materials_amount") val materialsAmount: com.google.gson.JsonElement? = null,
    @SerializedName("has_written_answer") val hasWrittenAnswer: Boolean? = null,
    @SerializedName("type") val type: String? = null
)

// ======================== EventCalendar API DTOs ========================

data class EventCalendarResponse(
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("response") val response: List<EventCalendarItemDTO>? = null
)

data class EventCalendarItemDTO(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("source_id") val sourceId: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("start_at") val startAt: String? = null,
    @SerializedName("finish_at") val finishAt: String? = null,
    @SerializedName("cancelled") val cancelled: Boolean? = false,
    @SerializedName("is_missed_lesson") val isMissedLesson: Boolean? = false,
    @SerializedName("lesson_type") val lessonType: String? = null,
    @SerializedName("room_name") val roomName: String? = null,
    @SerializedName("room_number") val roomNumber: String? = null,
    @SerializedName("subject_id") val subjectId: Long? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("teacher") val teacher: TeacherResponseDTO? = null,
    @SerializedName("author") val author: TeacherResponseDTO? = null,
    @SerializedName("homework") val homework: EventHomeworkDTO? = null,
    @SerializedName("marks") val marks: List<EventMarkDTO>? = null
)

data class EventHomeworkDTO(
    @SerializedName("presence_status_id") val presenceStatusId: Int? = null,
    @SerializedName("total_count") val totalCount: Int? = null,
    @SerializedName("execute_count") val executeCount: Int? = null,
    @SerializedName("descriptions") val descriptions: List<String>? = null
)

data class EventMarkDTO(
    @SerializedName("value") val value: String? = null,
    @SerializedName("weight") val weight: Double? = 1.0,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("is_exam") val isExam: Boolean? = false
)

data class SubjectMarksShortResponseDTO(
    @SerializedName("payload") val payload: List<SubjectMarksShortItemDTO>? = null
)

data class SubjectMarksShortItemDTO(
    @SerializedName("subject_id") val subjectId: Long? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("average") val average: String? = null,
    @SerializedName("dynamic") val dynamic: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("marks") val marks: List<MarkWithDateDTO>? = null,
    @SerializedName("periods") val periods: List<SubjectMarksPeriodItemDTO>? = null
)

data class MarkWithDateDTO(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("weight") val weight: Double? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("control_form_name") val controlFormName: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("is_exam") val isExam: Boolean? = null
)

data class SubjectMarksPeriodItemDTO(
    @SerializedName("title") val title: String? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("marks") val marks: List<MarkWithDateDTO>? = null
)

data class MarkByDateItemDTO(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("weight") val weight: Double? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("subject_id") val subjectId: Long? = null,
    @SerializedName("control_form_name") val controlFormName: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("is_exam") val isExam: Boolean? = null
)

enum class AttendanceType {
    PRESENT, ABSENT_EXCUSED, ABSENT_ILLNESS, ABSENT_UNEXCUSED, LATE
}

/**
 * Домашнее задание
 */
data class HomeworkItem(
    val id: String = "",
    val subject: String = "",
    val description: String = "",
    val date: String = "",
    val dueDate: String = "",
    var isDone: Boolean = false,
    val hasDigitalTest: Boolean = false,
    val digitalTestUrl: String? = null
)

/**
 * Оценка
 */
data class MarkItem(
    val id: String = "",
    val subject: String = "",
    val value: Int = 5,
    val weight: Double = 1.0,
    val date: String = "",
    val topic: String = "",
    val isExam: Boolean = false
)

/**
 * Сводка по предмету
 */
data class SubjectSummary(
    val subject: String = "",
    val averageMark: Double = 0.0,
    val marks: List<MarkItem> = emptyList(),
    val targetMark: Double = 4.60,
    val teacher: String = "Учитель предмета"
)

/**
 * Геймификация: Профиль и звезды
 */
data class GamificationProfile(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("gamificationId")
    val gamificationId: String? = null,

    @SerializedName("balance")
    val coinsCount: Int = 0,

    @SerializedName("spentPoints")
    val coinsSpent: Int = 0,

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
    val profileId: Long = 0L,
    val gamificationId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val rank: Int = 0,
    val spentStars: Int = 0,
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
    val profileId: Long = 0L,
    @SerializedName("gamificationId")
    val gamificationId: String = "",
    @SerializedName("rating")
    val rating: Int = 0,
    @SerializedName("spentPoints")
    val spentPoints: Int = 0,
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String = "",
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
    val id: Long = 0L,
    @SerializedName("gamificationId")
    val gamificationId: String = "",
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String = "",
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
    val rank: Int = 0,
    val name: String = "",
    val className: String = "",
    val spentStars: Int = 0,
    val gamificationId: String = "",
    val isCurrentUser: Boolean = false
)

/**
 * Награда магазина
 */
data class RewardItem(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val title: String = "",
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("points")
    val costStars: Int = 100,
    @SerializedName("imageUrl")
    val iconName: String? = null,
    @SerializedName("animationUrl")
    val animationUrl: String? = null,
    @SerializedName("rewardType")
    val rewardType: String? = "GIFT",
    @SerializedName("balance")
    val remainingStock: Int? = null,
    @SerializedName("isUnlocked")
    val isUnlocked: Boolean = false
)

data class ProfileRewardItem(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("profileRewardId")
    val profileRewardId: Long = 0L,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("sendingMode")
    val sendingMode: String = "PUBLIC",
    @SerializedName("from")
    val from: PersonSearchItem? = null,
    @SerializedName("to")
    val to: PersonSearchItem? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("animationUrl")
    val animationUrl: String? = null,
    @SerializedName("purchasedAt")
    val purchasedAt: String? = null
)

data class SendRewardGiftRequest(
    @SerializedName("comment")
    val comment: String = "",
    @SerializedName("recipientProfileIds")
    val recipientProfileIds: List<Long> = emptyList(),
    @SerializedName("sendingMode")
    val sendingMode: String = "PUBLIC"
)

/**
 * Задание за звезды
 */
data class WorkItem(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val title: String = "",
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
    val clientBalanceRub: Double = 0.0,
    val dailyLimitRub: Double? = null,
    val hotMealSubscribed: Boolean = false,
    val cardId: String = "",
    val transactions: List<MealTransaction> = emptyList()
)

data class MealTransaction(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("amount")
    val amountRub: Double = 0.0,
    @SerializedName("date")
    val timestamp: String = "",
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
    val emiasCertificates: List<EmiasRecord> = emptyList(),
    val visits: List<SchoolVisitRecord> = emptyList()
)

data class SchoolVisitRecord(
    val date: String = "",
    val timeIn: String = "",
    val timeOut: String = "",
    val duration: String = "",
    val isCurrentlyInSchool: Boolean = false
)

data class EmiasRecord(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("certificateNumber")
    val certificateNumber: String = "",
    @SerializedName("clinicName")
    val clinicName: String = "",
    @SerializedName("startDate")
    val startDate: String = "",
    @SerializedName("endDate")
    val endDate: String = "",
    @SerializedName("diagnosis")
    val diagnosis: String = "",
    @SerializedName("physicalCultureExemptionUntil")
    val physicalCultureExemptionUntil: String = "",
    @SerializedName("status")
    val status: String = "Действительна"
)
