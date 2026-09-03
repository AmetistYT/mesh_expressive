package ru.mesh.expressive.data.remote

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.mesh.expressive.data.model.*
import java.util.concurrent.TimeUnit

data class WorksSearchRequest(
    @SerializedName("profileId")
    val profileId: String = "",
    @SerializedName("pagination")
    val pagination: PaginationDTO = PaginationDTO(),
    @SerializedName("filters")
    val filters: WorksFiltersDTO = WorksFiltersDTO()
)

data class PaginationDTO(
    @SerializedName("pageNumber")
    val pageNumber: Int = 1,
    @SerializedName("pageSize")
    val pageSize: Int = 20
)

data class WorksFiltersDTO(
    @SerializedName("states")
    val states: List<String> = listOf("ACCESSIBLE")
)

data class WorksSearchResponse(
    @SerializedName("content")
    val items: List<WorkItem> = emptyList()
)

data class RewardsSearchResponse(
    @SerializedName("content")
    val items: List<RewardItem> = emptyList()
)

data class RewardsSearchRequestBody(
    @SerializedName("pagination")
    val pagination: RewardsPaginationDTO = RewardsPaginationDTO(),
    @SerializedName("filters")
    val filters: RewardsFiltersDTO = RewardsFiltersDTO()
)

data class RewardsPaginationDTO(
    @SerializedName("pageNumber")
    val pageNumber: Int = 1,
    @SerializedName("pageSize")
    val pageSize: Int = 50
)

data class RewardsFiltersDTO(
    @SerializedName("rewardTypes")
    val rewardTypes: List<String> = listOf("GIFT"),
    @SerializedName("statuses")
    val statuses: List<String> = listOf("ACTIVE"),
    @SerializedName("isEmptyHidden")
    val isEmptyHidden: Boolean = true
)

data class WebProfileResponse(
    @SerializedName("profile")
    val profile: WebUserProfile? = null,
    @SerializedName("children")
    val children: List<WebChildData> = emptyList()
)

data class WebUserProfile(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("middle_name")
    val middleName: String? = null
)

data class WebChildData(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("middle_name")
    val middleName: String? = null,
    @SerializedName("class_name")
    val className: String? = null,
    @SerializedName("class_uid")
    val classUid: String? = null,
    @SerializedName("class_unit_id")
    val classUnitId: Long? = null,
    @SerializedName("class_level_id")
    val classLevelId: Int? = null,
    @SerializedName("school")
    val school: WebSchoolData? = null,
    @SerializedName("contingent_guid")
    val contingentGuid: String? = null
)

data class WebSchoolData(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("short_name")
    val shortName: String? = null
)

data class ClientBalanceResponse(
    @SerializedName("balance")
    val balance: Int? = null,
    @SerializedName("contractId")
    val contractId: Long? = null,
    @SerializedName("clientId")
    val clientId: Any? = null,
    @SerializedName("expenseConstraints")
    val expenseConstraints: ExpenseConstraintsDTO? = null
)

data class ExpenseConstraintsDTO(
    @SerializedName("expenseDayLimit")
    val expenseDayLimit: Double? = null,
    @SerializedName("balanceThreshold")
    val balanceThreshold: Double? = null
)

data class OrdersHistoryResponse(
    @SerializedName("hasNext")
    val hasNext: Boolean = false,
    @SerializedName("orders")
    val orders: List<OrderItemDTO> = emptyList()
)

data class OrderItemDTO(
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("deliveredAt")
    val deliveredAt: String? = null,
    @SerializedName("price")
    val price: Int? = null,
    @SerializedName("totalPrice")
    val totalPrice: Int? = null,
    @SerializedName("items")
    val items: List<OrderSubItemDTO> = emptyList()
)

data class OrderSubItemDTO(
    @SerializedName("complex")
    val complex: ComplexDTO? = null,
    @SerializedName("dish")
    val dish: DishDTO? = null
)

data class ComplexDTO(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("price")
    val price: Int? = null
)

data class DishDTO(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("price")
    val price: Int? = null
)

data class DayBalanceResponse(
    @SerializedName("items")
    val items: List<MealTransaction> = emptyList()
)

data class AttendanceResponseDTO(
    @SerializedName("attendance")
    val attendance: List<AttendanceDayItemDTO> = emptyList(),
    @SerializedName("days_count")
    val daysCount: Int? = null,
    @SerializedName("year_description")
    val yearDescription: String? = null
)

data class AttendanceDayItemDTO(
    @SerializedName("date")
    val date: String = "",
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("notified")
    val notified: Boolean? = null,
    @SerializedName("reason_id")
    val reasonId: Int? = null,
    @SerializedName("is_system")
    val isSystem: Boolean? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("parent_profile_id")
    val parentProfileId: Long? = null,
    @SerializedName("lessons")
    val lessons: List<AttendanceLessonItemDTO> = emptyList()
)

data class AttendanceLessonItemDTO(
    @SerializedName("subject_id")
    val subjectId: Long? = null,
    @SerializedName("subject_name")
    val subjectName: String? = null,
    @SerializedName("bell_id")
    val bellId: Long? = null,
    @SerializedName("notified")
    val notified: Boolean? = null,
    @SerializedName("reason_id")
    val reasonId: Int? = null,
    @SerializedName("schedule_item_id")
    val scheduleItemId: Long? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("lesson_education_type")
    val lessonEducationType: String? = null,
    @SerializedName("health_status")
    val healthStatus: String? = null
)

data class EmiasMedicalRecommendationDTO(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("date")
    val date: String = "",
    @SerializedName("student_profile_id")
    val studentProfileId: Long = 0L,
    @SerializedName("subject_ids")
    val subjectIds: List<Long> = emptyList(),
    @SerializedName("type")
    val type: String = "SICK"
)

interface MeshFamilyWebApi {
    @GET("profile")
    suspend fun getProfile(
        @Header("Auth-Token") token: String,
        @Header("Profile-Id") profileId: Long = 0L,
        @Header("X-Mes-Subsystem") subsystem: String = "familyweb"
    ): Response<WebProfileResponse>

    @GET("attendance")
    suspend fun getAttendance(
        @Header("Auth-Token") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familyweb",
        @Query("student_id") studentId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<AttendanceResponseDTO>
}

interface MeshEmiasApi {
    @GET("api/ej/core/family/v1/emias_medical_recommendations")
    suspend fun getEmiasMedicalRecommendations(
        @Header("Authorization") token: String,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Header("Profile-Id") profileId: Long = 0L,
        @Query("person_ids") personIds: String? = null,
        @Query("class_unit_id") classUnitId: Long? = null,
        @Query("student_profile_id") studentProfileId: Long? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50
    ): Response<List<EmiasMedicalRecommendationDTO>>
}

data class VisitListResponseDTO(
    @SerializedName("payload")
    val payload: List<DateVisitsDTO> = emptyList()
)

data class DateVisitsDTO(
    @SerializedName("date")
    val date: String = "",
    @SerializedName("visits")
    val visits: List<VisitItemDTO> = emptyList()
)

data class VisitItemDTO(
    @SerializedName("in")
    val timeIn: String? = null,
    @SerializedName("out")
    val timeOut: String? = null,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("isIncomplete")
    val isIncomplete: Boolean? = false
)

interface MeshEntrancesApi {
    @GET("api/pass/entrances/v1/visit_durations")
    suspend fun getVisitDurations(
        @Header("Authorization") token: String,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Header("Profile-Id") profileId: Long = 0L,
        @Query("personId") personId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<VisitListResponseDTO>
}

data class MeshAvatarDTO(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("url")
    val url: String = "",
    @SerializedName("default")
    val isDefault: Boolean = false
)

interface MeshAvatarApi {
    @GET("api/avatarmanagement/v1/{userUuid}")
    suspend fun getAvatars(
        @Header("Authorization") token: String,
        @Header("Auth-Token") authToken: String,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("userUuid") userUuid: String
    ): Response<List<MeshAvatarDTO>>

    @Multipart
    @POST("api/avatarmanagement/v1/{userUuid}")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Header("Auth-Token") authToken: String,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("userUuid") userUuid: String,
        @Part file: MultipartBody.Part,
        @Part("is_default") isDefault: RequestBody
    ): Response<MeshAvatarDTO>

    @DELETE("api/avatarmanagement/v1/{userUuid}/{avatarId}")
    suspend fun deleteAvatar(
        @Header("Authorization") token: String,
        @Header("Auth-Token") authToken: String,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("userUuid") userUuid: String,
        @Path("avatarId") avatarId: Long
    ): Response<Unit>

    @PATCH("api/avatarmanagement/v1/{userUuid}/{avatarId}")
    suspend fun setDefaultAvatar(
        @Header("Authorization") token: String,
        @Header("Auth-Token") authToken: String,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("userUuid") userUuid: String,
        @Path("avatarId") avatarId: Long,
        @Query("is_default") isDefault: Boolean = true
    ): Response<Unit>
}

interface MeshFamilyMobileApi {
    @GET("profile")
    suspend fun getMobileProfile(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile"
    ): Response<WebProfileResponse>

    @GET("attendance")
    suspend fun getAttendance(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("student_id") studentId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<AttendanceResponseDTO>

    @GET("lesson_schedule_items")
    suspend fun getLessonScheduleItems(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("student_id") studentId: Long,
        @Query("person_id") personId: String,
        @Query("date") date: String
    ): Response<List<LessonScheduleItemResponseDTO>>

    @GET("lesson_schedule_items/{lesson_id}")
    suspend fun getLessonScheduleItemDetails(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("lesson_id") lessonId: Long,
        @Query("student_id") studentId: Long,
        @Query("person_id") personId: String,
        @Query("type") type: String = "PLAN"
    ): Response<LessonDetailDTO>

    @GET("homeworks/short")
    suspend fun getHomeworksShort(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("student_id") studentId: Long,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("sort_column") sortColumn: String = "date",
        @Query("sort_direction") sortDirection: String = "asc"
    ): Response<HomeworksShortResponseDTO>

    @POST("homeworks/{homework_entry_student_id}/done")
    suspend fun markHomeworkDone(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("homework_entry_student_id") homeworkId: Long
    ): Response<Unit>

    @DELETE("homeworks/{homework_entry_student_id}/done")
    suspend fun markHomeworkUndone(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("homework_entry_student_id") homeworkId: Long
    ): Response<Unit>

    @GET("marks")
    suspend fun getMarks(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("student_id") studentId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<MarksResponseDTO>

    @GET("subject_marks/short")
    suspend fun getSubjectMarksShort(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("student_id") studentId: Long
    ): Response<SubjectMarksShortResponseDTO>

    @GET("periods_schedules")
    suspend fun getPeriodsSchedules(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("student_id") studentId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<List<PeriodScheduleItemDTO>>

    @Multipart
    @POST("homeworks/{homework_entry_student_id}/attachment")
    suspend fun uploadHomeworkAttachment(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Path("homework_entry_student_id") homeworkEntryStudentId: Long,
        @Query("type") lessonType: String = "lesson",
        @Part file: MultipartBody.Part
    ): Response<okhttp3.ResponseBody>

    @DELETE("homeworks/{homework_entry_student_id}/attachment/{file_id}")
    suspend fun deleteHomeworkAttachment(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Path("homework_entry_student_id") homeworkEntryStudentId: Long,
        @Path("file_id") fileId: Long
    ): Response<okhttp3.ResponseBody>
}

interface MeshEventCalendarApi {
    @GET("api/events")
    suspend fun getEvents(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Header("x-mes-role") role: String = "student",
        @Query("person_ids") personIds: String,
        @Query("begin_date") beginDate: String,
        @Query("end_date") endDate: String,
        @Query("expand") expand: String = "homework,materials,marks"
    ): Response<EventCalendarResponse>
}

interface MeshRatingApi {
    @GET("rank/class")
    suspend fun getClassRank(
        @Header("Authorization") token: String,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("personId") personId: String,
        @Query("classUnitId") classUnitId: Long,
        @Query("date") date: String,
        @Query("subjectId") subjectId: Long? = null
    ): Response<List<ClassRankPersonItem>>

    @GET("rank/rankShort")
    suspend fun getRatingShort(
        @Header("Authorization") token: String,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "mobile",
        @Query("personId") personId: String,
        @Query("beginDate") beginDate: String,
        @Query("endDate") endDate: String
    ): Response<RatingInfo>
}

interface MeshGamificationApi {
    @GET("profiles")
    suspend fun getGamificationProfile(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("personId") personId: String
    ): Response<GamificationProfile>

    @POST("rewards/system_gift")
    suspend fun claimSystemGift(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile"
    ): Response<Unit>

    @POST("profiles/{profileId}/works/search")
    suspend fun searchWorks(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("profileId") gamificationProfileId: String,
        @Body request: WorksSearchRequest = WorksSearchRequest()
    ): Response<WorksSearchResponse>

    @PATCH("profiles/{profileId}/works/{workId}/points")
    suspend fun updateWorkPoints(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("profileId") gamificationProfileId: String,
        @Path("workId") workId: String
    ): Response<Unit>

    @POST("rewards/search")
    suspend fun searchRewards(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Body request: RewardsSearchRequestBody = RewardsSearchRequestBody()
    ): Response<RewardsSearchResponse>

    @POST("persons/rating")
    suspend fun getPersonsRating(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Body request: PersonsRatingRequestBody
    ): Response<PersonsRatingResponse>

    @POST("persons/search")
    suspend fun getPersonsSearch(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Body request: PersonsSearchFilterBody
    ): Response<List<PersonSearchItem>>

    @GET("persons")
    suspend fun getPersonByGamificationId(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("gamificationId") gamificationId: String
    ): Response<PersonSearchItem>

    @POST("profiles/{profileId}/rewards/{rewardId}")
    suspend fun sendRewardGift(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("profileId") senderProfileId: String,
        @Path("rewardId") rewardId: String,
        @Body request: SendRewardGiftRequest
    ): Response<Unit>

    @POST("profiles/{profileId}/rewards")
    suspend fun getProfileRewards(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("profileId") profileIdPath: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Body request: ProfileRewardsRequestBody = ProfileRewardsRequestBody()
    ): Response<ProfileRewardsResponse>
}

interface MeshMealsApi {
    @GET("clients/balance")
    suspend fun getBalance(
        @Header("Authorization") token: String,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("clientIds") clientIds: String
    ): Response<List<ClientBalanceResponse>>

    @GET("orders")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("clientId") clientId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<OrdersHistoryResponse>
}

data class AdditionalMaterialsRequest(
    @SerializedName("materials") val materials: List<MaterialRequestItem>
)

data class MaterialRequestItem(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("selected_mode") val selectedMode: String = "learn",
    @SerializedName("purpose") val purpose: String = "lesson"
)

data class AdditionalMaterialsResponse(
    @SerializedName("additional_materials") val additionalMaterials: List<AdditionalMaterialGroupWrapper>? = null
)

data class AdditionalMaterialGroupWrapper(
    @SerializedName("purpose") val purpose: String? = null,
    @SerializedName("material_groups") val materialGroups: List<MaterialGroupItem>? = null
)

data class MaterialGroupItem(
    @SerializedName("selected_mode") val selectedMode: String? = null,
    @SerializedName("materials") val materials: List<DetailedMaterialItem>? = null
)

data class DetailedMaterialItem(
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("type_name") val typeName: String? = null,
    @SerializedName("urls") val urls: List<LessonMaterialUrlDTO>? = null,
    @SerializedName("action_name") val actionName: String? = null
)

interface MeshMaterialsApi {
    @POST("api/family/materials/v1/additional_materials")
    suspend fun getAdditionalMaterials(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Header("x-mes-globalroleid") globalRoleId: String = "2",
        @Body body: AdditionalMaterialsRequest
    ): Response<AdditionalMaterialsResponse>
}

object MeshNetworkClient {
    private const val BASE_FAMILY_WEB = "https://school.mos.ru/api/family/web/v1/"
    private const val BASE_FAMILY_MOBILE = "https://school.mos.ru/api/family/mobile/v1/"
    private const val BASE_EVENT_CALENDAR = "https://school.mos.ru/api/eventcalendar/v1/"
    private const val BASE_GAMIFICATION = "https://school.mos.ru/api/gamification/v1/"
    private const val BASE_RATING = "https://school.mos.ru/api/ej/rating/v1/"
    private const val BASE_MEALS = "https://school.mos.ru/api/food/meals/v3/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val familyWebApi: MeshFamilyWebApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_FAMILY_WEB)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshFamilyWebApi::class.java)
    }

    val familyMobileApi: MeshFamilyMobileApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_FAMILY_MOBILE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshFamilyMobileApi::class.java)
    }

    val eventCalendarApi: MeshEventCalendarApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_EVENT_CALENDAR)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshEventCalendarApi::class.java)
    }

    val gamificationApi: MeshGamificationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_GAMIFICATION)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshGamificationApi::class.java)
    }

    val ratingApi: MeshRatingApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_RATING)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshRatingApi::class.java)
    }

    val mealsApi: MeshMealsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_MEALS)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshMealsApi::class.java)
    }

    val emiasApi: MeshEmiasApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshEmiasApi::class.java)
    }

    val entrancesApi: MeshEntrancesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshEntrancesApi::class.java)
    }

    val avatarApi: MeshAvatarApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshAvatarApi::class.java)
    }

    val materialsApi: MeshMaterialsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshMaterialsApi::class.java)
    }

    val portfolioApi: MeshPortfolioApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MeshPortfolioApi::class.java)
    }
}

interface MeshPortfolioApi {
    @GET("api/portfolio/app/persons/{personId}/rewards/list")
    suspend fun getRewardsList(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("personId") personGuid: String,
        @Query("size") size: Int = 50
    ): Response<PortfolioRewardResponse>

    @GET("api/portfolio/app/persons/{personId}/events/list")
    suspend fun getEventsList(
        @Header("Authorization") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("x-mes-subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Path("personId") personGuid: String,
        @Query("size") size: Int = 50
    ): Response<PortfolioEventResponse>
}
