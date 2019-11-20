package com.example.images

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.images.ImageDownloadingIntentService.Companion.DOWNLOADING_ERROR_MESSAGE
import com.example.images.ImageDownloadingIntentService.Companion.INTERNET_UNAVAILABLE_ERROR_MESSAGE
import com.example.images.ImageDownloadingIntentService.Companion.cache
import com.example.images.ImageDownloadingIntentService.Companion.items
import com.example.images.ImageDownloadingIntentService.Companion.page
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var imageListAdapter: ImagesListRecyclerViewAdapter
    private var resultReceiver: ImageResultReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultReceiver = savedInstanceState?.getParcelable("receiver") ?: ImageResultReceiver(Handler()).apply {
            bind()
        }
        imageListAdapter = ImagesListRecyclerViewAdapter(items)
        imageListAdapter.onClickListener = {
            downloadFull(it)
        }
        image_list.adapter = imageListAdapter
        image_load.setOnClickListener {
            downloadList()
        }

        if (items.isEmpty()) {
            downloadList()
        }
    }

    override fun onResume() {
        resultReceiver = ImageResultReceiver(Handler()).apply {
            bind()
        }
        super.onResume()
    }

    override fun onPause() {
        resultReceiver!!.unBind()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("receiver", resultReceiver)
        super.onSaveInstanceState(outState)
    }

    private fun downloadList() {
        if (!checkInternetConnection()) {
            Toast.makeText(this, INTERNET_UNAVAILABLE_ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
            return
        }
        image_progress_bar.visibility = ProgressBar.VISIBLE
        ImageDownloadingIntentService.startActionList(
            this,
            resultReceiver!!
        )
    }

    private fun downloadFull(image: Image) {
        val id = image.id
        val path = cacheDir.absolutePath + "/${id}.jpg"
        val intent = Intent(this, ImageDetailActivity::class.java).apply {
            putExtra("id", id)
            putExtra("path", path)
        }
        val file = File(path)
        if (!cache.containsKey(id) && !file.exists()) {
            if (!checkInternetConnection()) {
                Toast.makeText(this, INTERNET_UNAVAILABLE_ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
                return
            }
            image_progress_bar.visibility = ProgressBar.VISIBLE
            ImageDownloadingIntentService.startActionFull(
                this,
                id,
                image.regularURL,
                path,
                resultReceiver!!
            )
        } else {
            image_progress_bar.visibility = ProgressBar.VISIBLE
            cache[id] = path
            startActivity(intent)
            image_progress_bar.visibility = ProgressBar.GONE
        }
    }

    private fun checkInternetConnection(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    private inner class ImageResultReceiver(handler: Handler) : ResultReceiver(handler) {

        private var isBinded = false

        fun bind() {
            isBinded = true
        }

        fun unBind() {
            isBinded = false
        }

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (isBinded) {
                when (resultCode) {
                    RESULT_LIST -> {
                        val images: ArrayList<Image> =
                            resultData!!.getParcelableArrayList<Image>("images")!!
                        items.addAll(images)
                        ++page
                        imageListAdapter.notifyDataSetChanged()
                        image_progress_bar.visibility = ProgressBar.INVISIBLE
                    }
                    RESULT_FULL -> {
                        val id = resultData!!.getString("id")!!
                        val path = resultData.getString("path")!!
                        val intent =
                            Intent(this@MainActivity, ImageDetailActivity::class.java).apply {
                                putExtra("id", id)
                            }
                        cache[id] = path
                        startActivity(intent)
                        image_progress_bar.visibility = ProgressBar.GONE
                    }
                    RESULT_ERROR -> {
                        image_progress_bar.visibility = ProgressBar.GONE
                        Toast.makeText(
                            this@MainActivity,
                            DOWNLOADING_ERROR_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                super.onReceiveResult(resultCode, resultData)
            }
        }
    }
}