package me.cugarete.investowl

import android.content.Context
import me.cugarete.investowl.core.database.AppDatabase
import me.cugarete.investowl.core.database.SessionManager
import me.cugarete.investowl.core.database.WatchlistDao
import me.cugarete.investowl.core.network.InvestowlApi
import me.cugarete.investowl.core.network.NetworkClient

class AppContainer(context: Context) {

    val sessionManager: SessionManager = SessionManager(context)

    private val database: AppDatabase = AppDatabase.create(context)

    val watchlistDao: WatchlistDao = database.watchlistDao()

    val api: InvestowlApi = NetworkClient.createApi(
        cacheDir = context.cacheDir,
        baseUrl = "https://theinvestowl.cugarete.me"
    )
}
