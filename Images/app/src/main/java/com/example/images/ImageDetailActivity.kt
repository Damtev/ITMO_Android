package com.example.images

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.images.ImageDownloadingIntentService.Companion.cache
import kotlinx.android.synthetic.main.activity_image_detail.*
import kotlinx.android.synthetic.main.activity_main.*

class ImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        val id = intent.getStringExtra("id")!!
        val path = cache[id]
        val bitmap = BitmapFactory.decodeFile(path)
        image_full.setImageBitmap(bitmap)
    }
}
