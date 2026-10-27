package me.cugarete.investowl.core.network

import me.cugarete.investowl.core.model.*
import retrofit2.Response
import retrofit2.http.*

interface InvestowlApi {

    // --- Auth & Users ---
    @POST("/api/users/sync")
    suspend fun syncUser(@Body request: UserSyncRequest): UserRead

    @GET("/api/users/")
    suspend fun getUsers(): List<UserRead>

    @PATCH("/api/users/{userId}/approve")
    suspend fun approveUser(
        @Path("userId") userId: Int,
        @Body request: UserApprovalRequest
    ): UserRead

    @DELETE("/api/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: Int): Map<String, String>

    @GET("/api/users/{email}/watchlist")
    suspend fun getWatchlist(@Path("email") email: String): UserWatchlistResponse

    @PUT("/api/users/{email}/watchlist")
    suspend fun updateWatchlist(
        @Path("email") email: String,
        @Body request: UserWatchlistUpdate
    ): UserWatchlistResponse

    @GET("/api/users/settings/auth-gate")
    suspend fun getAuthGate(): AuthGateStatus

    @PATCH("/api/users/settings/auth-gate")
    suspend fun updateAuthGate(@Body request: AuthGateUpdateRequest): AuthGateStatus

    // --- Stocks & Price ---
    @GET("/api/stocks/{ticker}/history")
    suspend fun getStockHistory(
        @Path("ticker") ticker: String,
        @Query("limit") limit: Int = 500,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): PriceHistoryResponse

    // --- Broker Flow ---
    @GET("/api/broker-flow/{ticker}/latest")
    suspend fun getLatestBrokerFlow(@Path("ticker") ticker: String): BrokerFlowRow

    @GET("/api/broker-flow/{ticker}/history")
    suspend fun getBrokerFlowHistory(
        @Path("ticker") ticker: String,
        @Query("limit") limit: Int = 250,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): BrokerFlowHistoryResponse

    @GET("/api/broker-flow/{ticker}/summary")
    suspend fun getBrokerFlowSummary(
        @Path("ticker") ticker: String,
        @Query("days") days: Int = 30
    ): BrokerFlowSummary

    @GET("/api/broker-flow/whales")
    suspend fun getWhales(): List<BrokerWhaleListItem>

    @GET("/api/broker-flow/whales/{brokerCode}")
    suspend fun getWhaleDetail(
        @Path("brokerCode") brokerCode: String,
        @Query("days") days: Int = 20,
        @Query("limit") limit: Int = 20
    ): BrokerWhalesDetailResponse

    // --- Bandarmology ---
    @GET("/api/bandar/universe")
    suspend fun getUniverse(): TickerUniverseResponse

    @GET("/api/bandar/universe/{mode}")
    suspend fun getUniverseByMode(@Path("mode") mode: String): TickerUniverseResponse

    @GET("/api/bandar/detail/{ticker}")
    suspend fun getTickerDetail(
        @Path("ticker") ticker: String,
        @Query("window_days") windowDays: Int = 20,
        @Query("horizon") horizon: Int = 10
    ): TickerDetailResponse

    @POST("/api/bandar/clear-cache")
    suspend fun clearBandarCache(): Map<String, String>

    // --- Foreign Flow & Macro ---
    @GET("/api/forensic/foreign-exodus")
    suspend fun getForeignExodusRadar(
        @Query("days") days: Int = 45
    ): ForeignExodusRadarResponse

    @GET("/api/foreign-flow/{ticker}")
    suspend fun getForeignFlowDetail(
        @Path("ticker") ticker: String,
        @Query("lookback_days") lookbackDays: Int = 60
    ): ForeignFlowDetailResponse

    @GET("/api/foreign-flow/sectors/summary")
    suspend fun getSectorsSummary(): SectorRotationSummary

    // --- AI Signal Scanner ---
    @GET("/api/signal/daily")
    suspend fun getDailySignals(
        @Query("limit") limit: Int = 100
    ): DailySignalsResponse

    @GET("/api/signal/active")
    suspend fun getActiveSignals(
        @Query("limit") limit: Int = 50
    ): ActiveSignalsResponse

    @POST("/api/signal/refresh")
    suspend fun refreshSignals(): Map<String, String>

    @GET("/api/signal/backtest/stats")
    suspend fun getBacktestStats(
        @Query("horizon") horizon: Int = 10
    ): BacktestStatsResponse

    @GET("/api/signal/backtest/history")
    suspend fun getBacktestHistory(
        @Query("limit") limit: Int = 100
    ): List<BacktestHistoryItem>

    // --- Ownership KSEI & IDX BAE ---
    @GET("/api/v1/ownership/stock/{ticker}")
    suspend fun getStockOwnership(@Path("ticker") ticker: String): StockOwnershipResponse

    @GET("/api/v1/ownership/staging/overview")
    suspend fun getStagingOverview(): StagingOverviewResponse

    @GET("/api/v1/ownership/staging/stock/{ticker}")
    suspend fun getStagingStockProfile(@Path("ticker") ticker: String): StagingStockProfileResponse

    @GET("/api/v1/ownership/staging/parity/{ticker}")
    suspend fun getStagingParity(@Path("ticker") ticker: String): StagingParityResponse

    @GET("/api/v1/ownership/investors")
    suspend fun searchInvestors(@Query("q") query: String): InvestorSearchResponse

    @GET("/api/v1/ownership/investors/detail")
    suspend fun getInvestorDetail(@Query("name") name: String): InvestorDetailResponse

    @GET("/api/v1/ownership/groups")
    suspend fun getCongloGroups(): CongloGroupsResponse

    @GET("/api/v1/ownership/groups/{groupId}")
    suspend fun getCongloGroupDetail(@Path("groupId") groupId: String): CongloGroupDetailResponse

    // --- System Logs ---
    @GET("/api/logs/daily")
    suspend fun getDailyLogs(): DailyLogsResponse

    @GET("/api/logs/jobs/status")
    suspend fun getJobsStatus(): JobsStatusResponse

    @POST("/api/logs/jobs/{jobName}/run")
    suspend fun runJob(
        @Path("jobName") jobName: String,
        @Body request: JobRunRequest = JobRunRequest()
    ): Map<String, String>

    @POST("/api/logs/jobs/{jobName}/stop")
    suspend fun stopJob(@Path("jobName") jobName: String): Map<String, String>

    @GET("/api/logs/view")
    suspend fun viewLog(
        @Query("file") file: String,
        @Query("lines") lines: Int = 200
    ): LogViewResponse

    @GET("/api/logs/cache/stats")
    suspend fun getCacheStats(): CacheStatsResponse

    @POST("/api/logs/cache/purge")
    suspend fun purgeCache(@Body request: Map<String, String>): Map<String, String>
}
