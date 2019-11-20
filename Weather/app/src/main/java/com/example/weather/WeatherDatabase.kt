package com.example.weather

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeatherTemplate::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun getWeatherDao(): WeatherDao
}