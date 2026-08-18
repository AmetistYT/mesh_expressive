package ru.mesh.expressive.data.remote

import com.google.gson.GsonBuilder
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
        @Body request: Map<String, String> = emptyMap()
    ): Response<List<ProfileRewardItem>>
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

object MeshNetworkClient {
    private const val BASE_FAMILY_WEB = "https://school.mos.ru/api/family/web/v1/"
    private const val BASE_GAMIFICATION = "https://school.mos.ru/api/gamification/v1/"
    private const val BASE_RATING = "https://school.mos.ru/api/ej/rating/v1/"
    private const val BASE_MEALS = "https://school.mos.ru/api/food/meals/v3/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
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
}
