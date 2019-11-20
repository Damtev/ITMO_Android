package com.example.weather

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://openweathermap.org/data/2.5/"

interface OpenWeatherApi {
    @GET("weather")
    fun getCurrentWeather(
        @Query("id") id: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Call<DetailedWeather>

    @GET("forecast/daily")
    fun getForecast(
        @Query("id") id: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("count") count: Int
    ): Call<Forecast>
}

fun createOpenWeatherApi(): OpenWeatherApi {
    val client = OkHttpClient()
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val retrofit = Retrofit.Builder()
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()
    return retrofit.create(OpenWeatherApi::class.java)
}
