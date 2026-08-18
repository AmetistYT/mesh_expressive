package ru.mesh.expressive.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.mesh.expressive.data.model.*
import java.util.concurrent.TimeUnit

data class WorksSearchRequest(
    val profileId: String = "",
    val filters: Map<String, Any> = emptyMap(),
    val pagination: Pagination = Pagination()
)

data class Pagination(
    val pageNumber: Int = 1,
    val pageSize: Int = 50
)

data class WorksSearchResponse(
    val items: List<WorkItem> = emptyList()
)

data class RewardsSearchResponse(
    val items: List<RewardItem> = emptyList()
)

data class WebProfileResponse(
    @SerializedName("profile")
    val profile: WebProfileData? = null,
    @SerializedName("children")
    val children: List<WebChildData> = emptyList()
)

data class WebProfileData(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("middle_name")
    val middleName: String? = null,
    @SerializedName("snils")
    val snils: String? = null
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

data class DayBalanceResponse(
    @SerializedName("items")
    val items: List<MealTransaction> = emptyList()
)

data class AttendanceResponse(
    @SerializedName("student_id")
    val studentId: Long? = null,
    @SerializedName("attendance")
    val attendance: List<Any> = emptyList()
)

interface MeshFamilyWebApi {
    @GET("profile")
    suspend fun getProfile(
        @Header("Auth-Token") token: String,
        @Header("Profile-Id") profileId: Long,
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
    ): Response<AttendanceResponse>

    @GET("schedule")
    suspend fun getSchedule(
        @Header("Auth-Token") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familyweb",
        @Query("student_id") studentId: Long,
        @Query("date") date: String
    ): Response<List<LessonScheduleItem>>

    @GET("marks")
    suspend fun getMarks(
        @Header("Auth-Token") token: String,
        @Header("Profile-Id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familyweb",
        @Query("student_id") studentId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<List<MarkItem>>
}

interface MeshRatingApi {
    @GET("rank/class")
    suspend fun getClassRank(
        @Header("Authorization") token: String,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "mobile",
        @Query("personId") personId: String,
        @Query("classUnitId") classUnitId: Long,
        @Query("date") date: String
    ): Response<RatingInfo>

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
        @Header("client-type") clientType: String = "diary-mobile"
    ): Response<RewardsSearchResponse>
}

interface MeshMealsApi {
    @GET("clients/balance")
    suspend fun getBalance(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("clientIds") clientIds: String
    ): Response<List<ClientBalanceResponse>>

    @GET("day-balance-info/v2")
    suspend fun getDayBalanceInfo(
        @Header("Authorization") token: String,
        @Header("Profile-id") profileId: Long,
        @Header("X-Mes-Subsystem") subsystem: String = "familymp",
        @Header("client-type") clientType: String = "diary-mobile",
        @Query("person_id") personId: String,
        @Query("from") from: String,
        @Query("limit") limit: Int = 30
    ): Response<DayBalanceResponse>
}

object MeshNetworkClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val familyWebApi: MeshFamilyWebApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/api/family/web/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeshFamilyWebApi::class.java)
    }

    val ratingApi: MeshRatingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/api/ej/rating/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeshRatingApi::class.java)
    }

    val gamificationApi: MeshGamificationApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/api/gamification/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeshGamificationApi::class.java)
    }

    val mealsApi: MeshMealsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://school.mos.ru/api/food/meals/v3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeshMealsApi::class.java)
    }
}
