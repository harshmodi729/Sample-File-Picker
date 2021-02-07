package com.harsh.fileselector.data

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    companion object {
        private const val BASE_URL = "https://file.io"
        private var INSTANCE: Retrofit? = null

        fun getInstance(): Retrofit {
            if (INSTANCE == null) {
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(Gson()))
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(getHttpClient())
                    .build()
            }
            return INSTANCE!!
        }

        private fun getHttpClient(): OkHttpClient {
            val httpClient = OkHttpClient.Builder()
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            httpClient.addInterceptor(logging)
            return httpClient.build()
        }
    }
}