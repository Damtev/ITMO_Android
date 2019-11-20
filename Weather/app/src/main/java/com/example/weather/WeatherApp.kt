package com.example.weather

import android.app.Application
import androidx.room.Room

class WeatherApp : Application() {
    lateinit var openWeatherApi: OpenWeatherApi
        private set

    lateinit var database: WeatherDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        openWeatherApi = createOpenWeatherApi()
        database = Room.databaseBuilder(applicationContext, WeatherDatabase::class.java, DATABASE_NAME).build()
        app = this
    }

    companion object {
        lateinit var app: WeatherApp
            private set

        const val DATABASE_NAME = "weather.db"
    }
}