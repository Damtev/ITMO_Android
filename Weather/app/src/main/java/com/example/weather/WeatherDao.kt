package com.example.weather

import androidx.room.*

@Dao
interface WeatherDao {
    @Query("SELECT * FROM WeatherTemplate")
    fun getAll(): List<WeatherTemplate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg data: WeatherTemplate)
}