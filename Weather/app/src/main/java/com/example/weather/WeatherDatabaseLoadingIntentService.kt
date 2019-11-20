package com.example.weather

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver

const val ACTION_LOAD = "com.example.weather.action.LOAD"
const val ACTION_UPLOAD = "com.example.weather.action.UPLOAD"

const val RESULT_LOADED = 1
const val RESULT_UPLOADED = 0
const val RESULT_ERROR = -1

class WeatherDatabaseLoadingIntentService : IntentService("WeatherDatabaseLoadingIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_LOAD -> {
                val weatherResultReceiver = intent.getParcelableExtra<ResultReceiver>("receiver")
                val bundle = Bundle()
                val weather = WeatherApp.app.database.getWeatherDao().getAll()
                if (weather.isEmpty()) {
                    weatherResultReceiver?.send(RESULT_ERROR, bundle)
                } else {
                    bundle.putParcelableArrayList("weather", ArrayList(weather))
                    weatherResultReceiver?.send(RESULT_LOADED, bundle)
                }
            }
            ACTION_UPLOAD -> {
                val weatherResultReceiver = intent.getParcelableExtra<ResultReceiver>("receiver")
                val weather = intent.getParcelableArrayExtra("weather")!!.map { i -> i as WeatherTemplate }
                WeatherApp.app.database.getWeatherDao().insert(*weather.toTypedArray())
                weatherResultReceiver?.send(RESULT_UPLOADED, Bundle())
            }
        }
    }

    companion object {
        fun loadWeatherFromDatabase(
            context: Context,
            weatherResultReceiver: ResultReceiver
        ) {
            val intent = Intent(context, WeatherDatabaseLoadingIntentService::class.java).apply {
                action = ACTION_LOAD
                putExtra("receiver", weatherResultReceiver)
            }
            context.startService(intent)
        }

        fun uploadWeatherToDatabase(
            context: Context,
            weatherResultReceiver: ResultReceiver,
            weather: Array<WeatherTemplate?>
        ) {
            val intent = Intent(context, WeatherDatabaseLoadingIntentService::class.java).apply {
                action = ACTION_UPLOAD
                putExtra("receiver", weatherResultReceiver)
                putExtra("weather", weather)
            }
            context.startService(intent)
        }
    }
}