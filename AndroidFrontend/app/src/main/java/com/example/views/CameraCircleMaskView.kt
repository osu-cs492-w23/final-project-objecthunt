package com.example.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CameraCircleMaskView : View {

    private val paint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    private lateinit var rectF: RectF

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        val cx = rectF.centerX()
        val cy = rectF.centerY()
        val radius = Math.min(rectF.width(), rectF.height()) / 2
        canvas.drawCircle(cx, cy, radius.toFloat(), paint)
    }
}