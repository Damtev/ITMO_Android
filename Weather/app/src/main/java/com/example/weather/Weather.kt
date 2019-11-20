package com.example.weather

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

data class MainInfo(
    @Json(name = "temp") val temp: Float,
    @Json(name = "pressure") val pressure: String,
    @Json(name = "humidity") val humidity: String
)

data class Icon(
    @Json(name = "icon") val iconName: String
)

data class DetailedWeather(
    @Json(name = "weather") val icon: List<Icon>,
    @Json(name = "main") val mainInfo: MainInfo,
    @Json(name = "visibility") val visibility: String,
    @Json(name = "dt") val time: Long
)

data class Temperature(
    @Json(name = "min") val minTemp: Float,
    @Json(name = "max") val maxTemp: Float
)

data class DailyWeather(
    @Json(name = "weather") val icon: List<Icon>,
    @Json(name = "dt") val date: Long,
    @Json(name = "temp") val temperature: Temperature
)

data class Forecast(
    @Json(name = "list") val dailyWeathers: List<DailyWeather>
)

@Entity
@Parcelize
data class WeatherTemplate(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "day") val day: String?,
    @ColumnInfo(name = "temp") val temp: String,
    @ColumnInfo(name = "pressure") val pressure: String?,
    @ColumnInfo(name = "visibility") val visibility: String?,
    @ColumnInfo(name = "humidity") val humidity: String?,
    @ColumnInfo(name = "iconName") val iconId: Int
) : Parcelable