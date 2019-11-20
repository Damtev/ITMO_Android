package com.example.weather

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

const val LOG_TAG = "OpenWeather API"

class MainActivity : AppCompatActivity() {

    private var todayCall: Call<DetailedWeather>? = null
    private var forecastCall: Call<Forecast>? = null
    private var resultReceiver: WeatherResultReceiver? = null

    private lateinit var days: Array<TextView>
    private lateinit var temperatures: Array<TextView>
    private var pics: Array<ImageView>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultReceiver = savedInstanceState?.getParcelable("receiver") ?: WeatherResultReceiver(
            Handler()
        )
        resultReceiver!!.activity = this


        days = arrayOf(second_day, third_day, fourth_day, fifth_day)
        temperatures = arrayOf(second_temp, third_temp, fourth_temp, fifth_temp)
        if (second_pic != null) {
            pics = arrayOf(second_pic!!, third_pic!!, fourth_pic!!, fifth_pic!!)
        }

        if (haveInternetConnection()) {
            loadWeatherFromApi()
        } else {
            loadWeatherFromDatabase()
        }
    }

    private fun loadWeatherFromApi() {
        progress_bar.visibility = ProgressBar.VISIBLE
        todayCall = WeatherApp.app.openWeatherApi.getCurrentWeather(
            resources.getString(R.string.city_id),
            resources.getString(R.string.API_KEY),
            resources.getString(R.string.units)
        )
        todayCall?.enqueue(object : Callback<DetailedWeather> {
            override fun onFailure(call: Call<DetailedWeather>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Can't load today weather",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(LOG_TAG, "Failed with", t)
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<DetailedWeather>,
                response: Response<DetailedWeather>
            ) {
                val body = response.body()
                if (body != null) {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
                    val dateFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)

                    calendar.timeInMillis = body.time * 1000
                    today_day.text = dateFormat.format(calendar.time)

                    today_temperature.text = body.mainInfo.temp.toInt().toString() +
                            resources.getString(R.string.celsius)
                    today_pressure.text =
                        body.mainInfo.pressure + resources.getString(R.string.mb)
                    today_visibility.text = body.visibility
                    today_humidity.text = body.mainInfo.humidity + "%"
                    val iconId = resources.getIdentifier(
                        "ic_" + body.icon[0].iconName,
                        "drawable",
                        packageName
                    )
                    today_pic.setImageResource(iconId)
                    WeatherDatabaseLoadingIntentService.uploadWeatherToDatabase(
                        this@MainActivity,
                        resultReceiver!!,
                        arrayOf(
                            WeatherTemplate(
                                0,
                                today_day.text.toString(),
                                today_temperature.text.toString(),
                                today_pressure.text.toString(),
                                today_visibility.text.toString(),
                                today_humidity.text.toString(),
                                iconId
                            )
                        )
                    )

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Can't load today weather",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        forecastCall = WeatherApp.app.openWeatherApi.getForecast(
            resources.getString(R.string.city_id),
            resources.getString(R.string.API_KEY),
            resources.getString(R.string.units),
            resources.getInteger(R.integer.count)
        )
        forecastCall?.enqueue(object : Callback<Forecast> {
            override fun onFailure(call: Call<Forecast>, t: Throwable) {
                progress_bar.visibility = ProgressBar.GONE
                Toast.makeText(this@MainActivity, "Can't load forecast", Toast.LENGTH_SHORT)
                    .show()
                Log.d(LOG_TAG, "Failed with", t)
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<Forecast>,
                response: Response<Forecast>
            ) {
                val body = response.body()
                if (body != null) {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
                    val dateFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

                    val weather = Array<WeatherTemplate?>(5) { null }
                    for (i in days.indices) {
                        calendar.timeInMillis = body.dailyWeathers[i + 1].date * 1000
                        days[i].text = dateFormat.format(calendar.time)
                        val temperature = body.dailyWeathers[i + 1].temperature
                        temperatures[i].text =
                            ((temperature.minTemp + temperature.maxTemp) / 2).toInt().toString() +
                                    resources.getString(R.string.celsius)
                        val iconId = resources.getIdentifier(
                            "ic_" + body.dailyWeathers[i + 1].icon[0].iconName,
                            "drawable",
                            packageName
                        )
                        if (pics != null) {
                            pics!![i].setImageResource(iconId)
                        }

                        weather[i] = WeatherTemplate(
                            i + 1,
                            days[i].text.toString(),
                            temperatures[i].text.toString(),
                            null,
                            null,
                            null,
                            iconId
                        )
                    }
                    WeatherDatabaseLoadingIntentService.uploadWeatherToDatabase(
                        this@MainActivity,
                        resultReceiver!!,
                        weather
                    )
                } else {
                    Toast.makeText(this@MainActivity, "Can't load forecast", Toast.LENGTH_SHORT)
                        .show()
                }
                progress_bar.visibility = ProgressBar.GONE
            }
        })
    }

    private fun loadWeatherFromDatabase() {
        progress_bar.visibility = ProgressBar.VISIBLE
        WeatherDatabaseLoadingIntentService.loadWeatherFromDatabase(
            this,
            resultReceiver!!
        )
    }

    private fun haveInternetConnection(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    override fun onResume() {
        resultReceiver = WeatherResultReceiver(Handler()).apply {
            activity = this@MainActivity
        }
        if (haveInternetConnection()) {
            loadWeatherFromApi()
        }
        super.onResume()
    }

    override fun onPause() {
        resultReceiver!!.activity = null
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("receiver", resultReceiver)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        todayCall?.cancel()
        todayCall = null
        forecastCall?.cancel()
        forecastCall = null
    }

    private class WeatherResultReceiver(handler: Handler) : ResultReceiver(handler) {

        var activity: MainActivity? = null

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (activity != null) {
                when (resultCode) {
                    RESULT_LOADED -> {
                        val weather: ArrayList<WeatherTemplate> =
                            resultData!!.getParcelableArrayList<WeatherTemplate>("weather")!!
                        val detailedWeather = weather[0]
                        activity!!.today_day.text = detailedWeather.day
                        activity!!.today_pic.setImageResource(detailedWeather.iconId)
                        activity!!.today_pressure.text = detailedWeather.pressure
                        activity!!.today_visibility.text = detailedWeather.visibility
                        activity!!.today_humidity.text = detailedWeather.humidity
                        activity!!.today_temperature.text = detailedWeather.temp

                        for (i in activity!!.days.indices) {
                            val dayWeather = weather[i + 1]
                            activity!!.days[i].text = dayWeather.day
                            activity!!.temperatures[i].text = dayWeather.temp
                            if (activity!!.pics != null) {
                                activity!!.pics!![i].setImageResource(dayWeather.iconId)
                            }
                        }

                    }
                    RESULT_UPLOADED -> {
                        // do nothing
                    }
                    RESULT_ERROR -> {
                        Toast.makeText(
                            activity!!,
                            "Can't load weather from database",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                activity!!.progress_bar.visibility = ProgressBar.GONE
            }
            super.onReceiveResult(resultCode, resultData)
        }
    }
}
