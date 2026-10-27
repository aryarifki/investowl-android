package me.cugarete.investowl.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val DEFAULT_BASE_URL = "https://theinvestowl.cugarete.me"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Interceptor yang memastikan caching header dari FastAPI dihormati secara offline dan online.
     */
    class CacheResponseInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val originalResponse = chain.proceed(request)

            val serverCacheControl = originalResponse.header("Cache-Control")
            return if (serverCacheControl.isNullOrEmpty()) {
                // Default fallback: 30s cache jika server tidak menyertakan
                originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=30, s-maxage=1800, stale-while-revalidate=30")
                    .build()
            } else {
                originalResponse
            }
        }
    }

    fun createOkHttpClient(cacheDir: File? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addNetworkInterceptor(CacheResponseInterceptor())

        if (cacheDir != null) {
            val cacheSize = 50L * 1024L * 1024L // 50 MB disk cache
            builder.cache(Cache(File(cacheDir, "http_cache"), cacheSize))
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(logging)

        return builder.build()
    }

    fun createRetrofit(
        okHttpClient: OkHttpClient,
        baseUrl: String = DEFAULT_BASE_URL
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    fun createApi(
        cacheDir: File? = null,
        baseUrl: String = DEFAULT_BASE_URL
    ): InvestowlApi {
        val client = createOkHttpClient(cacheDir)
        val retrofit = createRetrofit(client, baseUrl)
        return retrofit.create(InvestowlApi::class.java)
    }
}
