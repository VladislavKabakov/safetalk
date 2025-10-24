package com.example.messengerchat.data.api

import com.example.messengerchat.constants.HttpClientConstants.AUTH_HEADER
import com.example.messengerchat.constants.HttpClientConstants.BASE_URL
import com.example.messengerchat.constants.HttpClientConstants.CONNECT_TIMEOUT
import com.example.messengerchat.constants.HttpClientConstants.READ_TIMEOUT
import com.example.messengerchat.constants.HttpClientConstants.WRITE_TIMEOUT
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var authToken: String? = null

    fun setAuthToken(token: String) {
        authToken = token
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            authToken?.let {
                requestBuilder.header(AUTH_HEADER, "Bearer $it")
            }

            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}