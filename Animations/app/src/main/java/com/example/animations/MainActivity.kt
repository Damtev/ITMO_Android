package com.example.animations

import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val alphaAnimation = AlphaAnimation(1f, 0.5f)
        alphaAnimation.repeatCount = AlphaAnimation.INFINITE
        alphaAnimation.duration = 1000L
        text.animation = alphaAnimation
    }
}
