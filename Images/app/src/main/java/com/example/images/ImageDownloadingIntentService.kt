package com.example.images

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.os.ResultReceiver
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

const val ACTION_LIST = "com.example.images.action.LIST"
const val ACTION_FULL = "com.example.images.action.FULL"

const val RESULT_LIST = 1
const val RESULT_FULL = 2
const val RESULT_ERROR = -1

@Parcelize
data class Image(
    val id: String,
    val description: String,
    val previewBitmap: Bitmap,
    val regularURL: String
) : Parcelable

class ImageDownloadingIntentService : IntentService("ImageDownloadingIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_LIST -> {
                val imageResultReceiver = intent.getParcelableExtra<ResultReceiver>("receiver")
                val downloaded = downloadList()
                val bundle = Bundle()
                if (downloaded != null) {
                    bundle.putParcelableArrayList("images", downloaded)
                    imageResultReceiver?.send(RESULT_LIST, bundle)
                } else {
                    imageResultReceiver?.send(RESULT_ERROR, bundle)
                }
            }
            ACTION_FULL -> {
                val bundle = Bundle()
                val id = intent.getStringExtra("id")!!
                val regularURL = intent.getStringExtra("regularURL")
                val path = intent.getStringExtra("path")!!
                val imageResultReceiver = intent.getParcelableExtra<ResultReceiver>("receiver")
                val localFile = File(path)
                val localPath = localFile.absolutePath

                if (!localFile.parentFile.exists() && !localFile.parentFile.mkdirs()) {
                    throw SecurityException("Couldn't make directory $localPath")
                }

                try {
                    val inputStream = URL(regularURL).openStream().buffered()
                    val outputStream = FileOutputStream(path).buffered()

                    var i = inputStream.read()
                    while (i != -1) {
                        outputStream.write(i)
                        i = inputStream.read()
                    }

                    inputStream.close()
                    outputStream.close()

                    bundle.putString("id", id)
                    bundle.putString("path", path)
                    imageResultReceiver?.send(RESULT_FULL, bundle)
                } catch (e: Exception) {
                    imageResultReceiver?.send(RESULT_ERROR, bundle)
                }
            }
        }
    }

    private fun downloadList(): ArrayList<Image>? {
        return try {
            val connection = URL(
                "${API_URL}page=${page}&per_page=${PER_PAGE}&client_id=$ACCESS_KEY"
            ).openConnection() as HttpURLConnection
            val imagesJSON = JSONArray(connection.inputStream.reader().readText())
            val images: ArrayList<Image> = arrayListOf()
            for (i in 0 until imagesJSON.length()) {
                val imageJSON = imagesJSON.getJSONObject(i)
                val id = imageJSON["id"].toString()
                val getDescription = imageJSON["alt_description"].toString()
                val description =
                    if (getDescription != "null") getDescription else DESCRIPTION_UNAVAILABLE
                val fullURL = imageJSON.getJSONObject("urls")["regular"].toString()
                val previewURL = imageJSON.getJSONObject("urls")["small"].toString()

                val inputStream = URL(previewURL).openStream()
                val data = inputStream.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                images.add(Image(id, description, bitmap, fullURL))
            }
            images
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        private const val PER_PAGE = 10
        private const val API_URL = "https://api.unsplash.com/photos/?"
        private const val ACCESS_KEY = "72194eba1a8e0e3d8935ce7f1ce89e95e34f53fa8997f1d2aa6e58ead278ea4a"
        private const val DESCRIPTION_UNAVAILABLE = "Описание недоступно"

        const val INTERNET_UNAVAILABLE_ERROR_MESSAGE = "Нет подключения к Интернету, невозможно загрузить данные"
        const val DOWNLOADING_ERROR_MESSAGE = "Произошла ошибка при загрузке данных"

        var page = 1
        val items: ArrayList<Image> = arrayListOf()
        val cache = HashMap<String, String>()

        fun startActionList(
            context: Context,
            imageResultReceiver: ResultReceiver
        ) {
            val intent = Intent(context, ImageDownloadingIntentService::class.java).apply {
                action = ACTION_LIST
                putExtra("receiver", imageResultReceiver)
            }
            context.startService(intent)
        }

        fun startActionFull(
            context: Context,
            id: String,
            fullURL: String,
            path: String,
            imageResultReceiver: ResultReceiver
        ) {
            val intent = Intent(context, ImageDownloadingIntentService::class.java).apply {
                action = ACTION_FULL
                putExtra("id", id)
                putExtra("regularURL", fullURL)
                putExtra("path", path)
                putExtra("receiver", imageResultReceiver)
            }
            context.startService(intent)
        }
    }
}