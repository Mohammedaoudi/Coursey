package com.example.firstaidfront.config

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val IP = NetworkConfig.IP

    const val BASE_URL = "http://$IP:8888/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun createClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(context))
            .build()
    }

    fun <T> create(serviceClass: Class<T>, context: Context): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(serviceClass)
    }
}