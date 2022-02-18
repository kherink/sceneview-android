package io.github.sceneview.sample.arpositiontracking

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.plus
import dev.romainguy.kotlin.math.Float3

class PositionTrackingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr,
    defStyleRes) {

    var canvasWidth: Int = 0
    var canvasHeight: Int = 0

    val userAvatar = Rect(0, 0, 20, 20)
    val userPaint = Paint(ANTI_ALIAS_FLAG).apply { color = Color.RED }

    var userPosition: Float3 = Float3()
        set(value) {
            field = value
            invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        canvasWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        canvasHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(canvasWidth, canvasHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            //center screen
            val center = Point(canvasWidth / 2, canvasHeight / 2)
            val centered = userAvatar.plus(center)
            val userOffsetDp = Point((userPosition.x * 100).toInt(), (userPosition.z * 100).toInt())

            Log.i("PositionTrackingView", "userOffsetDp: $userOffsetDp")

            drawRect(centered.plus(userOffsetDp), userPaint)
        }
    }
}