package com.example.animations

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.content.res.use
import kotlin.math.max
import kotlin.math.min

class LoadingAnimationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pointSize = dp(6f)
    private var rectCornerRadius = dp(2.5f)
    private var margin = dp(16f)
    private var delay = 1000L
    private var duration = 300L
    private var scaleTo = 1.3f
    private var color = 0xFFe1e3e6.toInt()

    init {
        if (attrs != null) {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.LoadingAnimationView)

            pointSize = dp(
                attributes.getFloat(
                    R.styleable.LoadingAnimationView_pointSize,
                    6f
                )
            )
            rectCornerRadius = dp(
                attributes.getFloat(
                    R.styleable.LoadingAnimationView_rectCornerRadius,
                    2.5f
                )
            )
            margin = dp(
                attributes.getFloat(
                    R.styleable.LoadingAnimationView_margin,
                    16f
                )
            )
            delay = attributes.getInt(
                R.styleable.LoadingAnimationView_delay,
                1000
            )
                .toLong()
            duration =
                attributes.getInt(
                    R.styleable.LoadingAnimationView_duration,
                    300
                ).toLong()
            scaleTo =
                attributes.getFloat(
                    R.styleable.LoadingAnimationView_scaleTo,
                    1.3f
                )
            color = attributes.getColor(
                R.styleable.LoadingAnimationView_color,
                0xFFe1e3e6.toInt()
            )

            attributes.recycle()
        }
    }

    private val pointDiff = dp(2f)
    private val indentHorizontal = dp(2f)
    private val indentVertical = dp(16f)
    private val rectSize = dp(22f)
    private val width = 2 * indentHorizontal + rectSize + margin + 3 * pointSize + 2 * pointDiff
    private val height = 2 * indentVertical + max(rectSize, pointSize * 3 + pointDiff * 2)
    private val scaleFrom = 1f

    private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = this@LoadingAnimationView.color
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = this@LoadingAnimationView.color
    }

    private val desiredWidth = width
    private val desiredHeight = height

    private val rectF = RectF()

    private var crossRotation: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var pointScales = Array(4) { scaleFrom }

    private val animators = Array<ValueAnimator>(5) { i ->
        if (i == 0) {
            ValueAnimator.ofFloat(0.0F, 180F).apply {
                addUpdateListener { crossRotation = it.animatedValue as Float }
            }
        } else {
            ValueAnimator.ofFloat(scaleFrom, scaleTo, scaleFrom).apply {
                addUpdateListener {
                    pointScales[i - 1] = it.animatedValue as Float
                    invalidate()
                }
            }
        }
    }

    private var animator: Animator? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        animator?.cancel()
        animator = AnimatorSet().apply {
            interpolator = PathInterpolator(0.25F, 0.1F, 0.25F, 1F)
            playSequentially(animators.toList())
            startDelay = delay
            duration = this@LoadingAnimationView.duration
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    start()
                }
            })
            start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getSize(widthMeasureSpec, desiredWidth.toInt()),
            getSize(heightMeasureSpec, desiredHeight.toInt())
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var save = canvas.save()

        var cx = indentHorizontal + rectSize / 2
        var cy = indentVertical + rectSize / 2

        canvas.rotate(crossRotation, cx, cy)

        var l = cx - rectSize / 2
        var t = cy - pointSize / 2

        rectF.set(l, t, l + rectSize, t + pointSize)
        canvas.drawRoundRect(rectF, rectCornerRadius, rectCornerRadius, rectPaint)

        canvas.restoreToCount(save)

        save = canvas.save()

        canvas.rotate(crossRotation, cx, cy)

        l = cx - pointSize / 2
        t = cy - rectSize / 2

        rectF.set(l, t, l + pointSize, t + rectSize)
        canvas.drawRoundRect(rectF, rectCornerRadius, rectCornerRadius, rectPaint)

        canvas.restoreToCount(save)

        cx += rectSize / 2 + margin + pointSize / 2
        drawPoint(canvas, cx, cy, 0)
        cx += pointSize + pointDiff
        cy -= pointSize + pointDiff
        drawPoint(canvas, cx, cy, 1)
        cx += pointSize + pointDiff
        cy += pointSize + pointDiff
        drawPoint(canvas, cx, cy, 2)
        cx -= pointSize + pointDiff
        cy += pointSize + pointDiff
        drawPoint(canvas, cx, cy, 3)
    }

    private fun drawPoint(canvas: Canvas, cx: Float, cy: Float, i: Int) {
        val save = canvas.save()

        val l = -pointSize / 2
        val t = -pointSize / 2

        canvas.translate(cx, cy)
        canvas.scale(pointScales[i], pointScales[i])
        rectF.set(l, t, l + pointSize, t + pointSize)
        canvas.drawRoundRect(rectF, rectCornerRadius, rectCornerRadius, pointPaint)

        canvas.restoreToCount(save)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        animator?.cancel()
        animator = null
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = AnimationState(super.onSaveInstanceState())
        state.crossRotation = crossRotation
        state.pointScales = pointScales
        state.crossPlayingTime = animators[0].currentPlayTime
        state.pointsPlayingTimes = animators.sliceArray(1..4).map { valueAnimator ->
            valueAnimator.currentPlayTime
        }.toTypedArray()

        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        state as AnimationState
        crossRotation = state.crossRotation
        pointScales = state.pointScales
        animators[0].currentPlayTime = state.crossPlayingTime
        for (i in 0..3) {
            animators[i + 1].currentPlayTime = state.pointsPlayingTimes[i]
        }

        super.onRestoreInstanceState(state.superState)
    }

    private fun getSize(measureSpec: Int, desired: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.AT_MOST -> min(size, desired)
            MeasureSpec.EXACTLY -> size
            MeasureSpec.UNSPECIFIED -> desired
            else -> desired
        }
    }

    private fun dp(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }


    //TODO: абсолютно бесполезно(?), надо научиться хранить аниматор(?)
    private class AnimationState : BaseSavedState {
        var crossRotation: Float = 0f
        var pointScales = Array(4) { 0f }
        var crossPlayingTime: Long = 0L
        var pointsPlayingTimes = Array(4) { 0L }
        lateinit var animator: Animator

        constructor(superState: Parcelable?) : super(superState)
        constructor(parcel: Parcel) : super(parcel) {
            crossRotation = parcel.readFloat()
            for (i in 0..3) {
                pointScales[i] = parcel.readFloat()
            }
            crossPlayingTime = parcel.readLong()
            for (i in 0..3) {
                pointsPlayingTimes[i] = parcel.readLong()
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            out.writeFloat(crossRotation)
            for (scale in pointScales) {
                out.writeFloat(scale)
            }
            out.writeLong(crossPlayingTime)
            for (time in pointsPlayingTimes) {
                out.writeLong(time)
            }
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<AnimationState> {
                override fun createFromParcel(source: Parcel): AnimationState =
                    AnimationState(source)

                override fun newArray(size: Int): Array<AnimationState?> = arrayOfNulls(size)
            }
        }
    }

}